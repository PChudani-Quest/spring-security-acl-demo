package acl.security;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.acls.domain.AccessControlEntryImpl;
import org.springframework.security.acls.domain.GrantedAuthoritySid;
import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.model.AccessControlEntry;
import org.springframework.security.acls.model.Acl;
import org.springframework.security.acls.model.AclService;
import org.springframework.security.acls.model.AlreadyExistsException;
import org.springframework.security.acls.model.NotFoundException;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.ObjectIdentityGenerator;
import org.springframework.security.acls.model.Permission;
import org.springframework.security.acls.model.Sid;

import acl.dao.AccessPolicyDao;
import acl.model.AccessPolicy;
import acl.model.AccessPolicy.SecurityIdentity;
import acl.util.CollectionUtils;
import acl.util.Function;

/**
 * Implementation of {@link AclService} using Google Data Store as persistence mechanism
 * 
 * @author Petr Giecek, Petr Chudanic
 */
public class SimpleAclService implements AclService {

    /** Object identity generator. */
    private final ObjectIdentityGenerator objectIdentityGenerator;

    /** Access policy DAO. */
    private final AccessPolicyDao accessPolicyDao;

    /**
     * Creates {@link SimpleAclService} with the specified details.
     * 
     * @param objectIdentityGenerator object identity generator
     * @param accessPolicyDao access policy DAO
     */
    public SimpleAclService(ObjectIdentityGenerator objectIdentityGenerator, AccessPolicyDao accessPolicyDao) {
        this.objectIdentityGenerator = objectIdentityGenerator;
        this.accessPolicyDao = accessPolicyDao;
    }

    /**
     * Inserts {@link AccessControlEntry} for given {@link Sid}, domain object and {@link Permission}
     * 
     * @param sid security identity
     * @param domainObject domain object to create access control entry for
     * @param permission permission
     * @throws AlreadyExistsException If same access control entry already exists
     */
    public void insertAce(Sid sid, Object domainObject, Permission permission) throws AlreadyExistsException {

        final ObjectIdentity objectIdentity = new ObjectIdentityImpl(domainObject);

        final String objectType = objectIdentity.getType();
        final Long objectId = (Long) objectIdentity.getIdentifier();
        final SecurityIdentity securityIdentity = mapToSecurityIdentity(sid);

        final List<AccessPolicy> accessPolicies = this.accessPolicyDao.getForDomainObject(
                securityIdentity, objectType, objectId);

        final Map<ObjectIdentity, SimpleAcl> acls = sortOutByObject(accessPolicies);

        // since we only asked for policies for the given object, no more than one ACL should be available
        if (acls.size() > 1) {
            throw new IllegalStateException(String.format("Only ACL expected, found: %d", Integer.valueOf(acls.size())));
        }

        final SimpleAcl acl = acls.get(objectIdentity);

        // make sure the ACE does not exist
        if (acl != null) {
            AccessControlEntry ace = new AccessControlEntryImpl(objectId, acl, sid, permission, true, true, true);

            if (acl.contains(ace))
            {
                throw new AlreadyExistsException(String.format("ACE %1$s already exists", ace));
            }
        }

        final String permissionString = permission.toString();

        // create access policy for the given ACE
        final AccessPolicy accessPolicy = new AccessPolicy(securityIdentity, objectType, objectId, permissionString);

        this.accessPolicyDao.save(accessPolicy);
    }

    /**
     * Maps {@link Sid} instance to respective {@link SecurityIdentity} instance.
     * 
     * @param sid the {@link Sid} instance to map
     * @return the respective {@link SecurityIdentity} instance
     */
    static SecurityIdentity mapToSecurityIdentity(Sid sid) {
        if (sid instanceof PrincipalSid) {
            final String principal = ((PrincipalSid) sid).getPrincipal();
            return SecurityIdentity
                    .valueOf(SecurityIdentity.Type.USER, principal);
        }
        else if (sid instanceof GrantedAuthoritySid) {
            final String authority = ((GrantedAuthoritySid) sid).getGrantedAuthority();
            return SecurityIdentity
                    .valueOf(SecurityIdentity.Type.ROLE, authority);
        }
        else {
            throw new IllegalArgumentException("Unsupported SID: " + sid.getClass());
        }
    }

    /**
     * Maps {@link Sid} instances to respective {@link SecurityIdentity} instances.
     * 
     * @param sids the {@link Sid} instances to map
     * @return the respective {@link SecurityIdentity} instances
     */
    private static List<SecurityIdentity> mapToSecurityIdentities(List<Sid> sids) {
        return CollectionUtils.transform(sids,
                new Function<Sid, SecurityIdentity>() {
                    @Override
                    public SecurityIdentity apply(Sid sid) {
                        return mapToSecurityIdentity(sid);
                    }
                });
    }

    /**
     * Sorts out the specified policies by a domain object.
     * 
     * @param accessPolicies the access policies to sort out
     * @return a map with domain objects as keys and respective ACLs as values
     */
    private Map<ObjectIdentity, SimpleAcl> sortOutByObject(List<AccessPolicy> accessPolicies) {

        final Map<ObjectIdentity, SimpleAcl> acls = new HashMap<>();

        for (AccessPolicy accessPolicy : accessPolicies) {

            // generate object identity
            final ObjectIdentity objectIdentity = this.objectIdentityGenerator.createObjectIdentity(
                    accessPolicy.getObjectId(), accessPolicy.getObjectType());

            // retrieve respective SID instance
            final SecurityIdentity securityIdentity = accessPolicy.getSecurityIdentity();
            final Sid sid;
            switch (securityIdentity.getType()) {
                case USER:
                    sid = new PrincipalSid(securityIdentity.getIdentity());
                    break;
                case ROLE:
                    sid = new GrantedAuthoritySid(securityIdentity.getIdentity());
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported type: " + securityIdentity.getType());
            }

            // construct permission
            final Permission permission = AccessPermission.valueOf(accessPolicy.getPermission());

            // retrieve ACL for the given object
            SimpleAcl acl = acls.get(objectIdentity);

            // if no such an ACL found, create new one
            if (acl == null) {
                acl = new SimpleAcl();
                acls.put(objectIdentity, acl);
            }

            // insert ACE
            acl.insert(new AccessControlEntryImpl(accessPolicy.getId(), acl, sid, permission, true, true, true));
        }

        return acls;
    }

    @Override
    public List<ObjectIdentity> findChildren(ObjectIdentity parentIdentity) {
        // no hierarchy supported
        throw new UnsupportedOperationException("The method is not implemented");
    }

    @Override
    public Acl readAclById(ObjectIdentity object) throws NotFoundException {
        return readAclById(object, null);
    }

    @Override
    public Acl readAclById(ObjectIdentity object, List<Sid> sids) throws NotFoundException {

        final String objectType = object.getType();
        final Long objectId = (Long) object.getIdentifier();
        final List<SecurityIdentity> securityIdentities = mapToSecurityIdentities(sids);

        final List<AccessPolicy> accessPolicies = this.accessPolicyDao.getForDomainObject(
                securityIdentities, objectType, objectId);

        if (accessPolicies.isEmpty()) {
            throw new NotFoundException(
                    String.format("Unable to find ACL information for object %1$s and security identities %2$s",
                            object, sids));
        }

        final Map<ObjectIdentity, SimpleAcl> acls = sortOutByObject(accessPolicies);

        // since we only asked for policies for the given object, no more than one ACL should be available
        if (acls.size() != 1) {
            throw new IllegalStateException(String.format("Only ACL expected, found: %d", Integer.valueOf(acls.size())));
        }

        return acls.get(object);
    }

    @Override
    public Map<ObjectIdentity, Acl> readAclsById(List<ObjectIdentity> objects) throws NotFoundException {
        return readAclsById(objects, null);
    }

    @Override
    public Map<ObjectIdentity, Acl> readAclsById(List<ObjectIdentity> objects, List<Sid> sids) throws NotFoundException {
        final Map<ObjectIdentity, Acl> resultAcls = new HashMap<>();

        for (ObjectIdentity object : objects) {

            final String objectType = object.getType();
            final Long objectId = (Long) object.getIdentifier();
            final List<SecurityIdentity> securityIdentities = mapToSecurityIdentities(sids);

            final List<AccessPolicy> accessPolicies = this.accessPolicyDao.getForDomainObject(
                    securityIdentities, objectType, objectId);

            if (accessPolicies.isEmpty()) {
                throw new NotFoundException(
                        String.format("Unable to find ACL information for object %1$s and security identities %2$s",
                                object, sids));
            }

            final Map<ObjectIdentity, SimpleAcl> acls = sortOutByObject(accessPolicies);

            // since we only asked for policies for the given object, no more than one ACL should be available
            if (acls.size() != 1) {
                throw new IllegalStateException(String.format("Only ACL expected, found: %d",
                        Integer.valueOf(acls.size())));
            }

            resultAcls.put(object, acls.get(object));

        }

        return resultAcls;
    }

}
