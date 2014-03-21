package acl.security;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.acls.model.AccessControlEntry;
import org.springframework.security.acls.model.Acl;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.Permission;
import org.springframework.security.acls.model.Sid;


/**
 * Implementation of {@link Acl}.
 * 
 * @author Petr Chudanic, Petr Giecek
 */
public class SimpleAcl implements Acl {

    /** Access control entries. */
    private final List<AccessControlEntry> aces = new ArrayList<AccessControlEntry>();

    /**
     * Helper for inserting the ACE into this ACL. Since ACL should be immutable after retrieved, the method is not
     * accessible from outside the package.
     * 
     * @param ace the ACE to insert
     */
    void insert(AccessControlEntry ace) {
        this.aces.add(ace);
    }

    /**
     * Helper for deleting the ACE into this ACL. Since ACL should be immutable after retrieved, the method is not
     * accessible from outside the package.
     * 
     * @param ace the ACE to insert
     */
    void delete(AccessControlEntry ace) {
        this.aces.remove(ace);
    }

    /**
     * Helper to determine whether this ACL contains the specified ACE.
     * 
     * @param ace the ACE to check
     * @return {@code true} if this ACL contains the specified ACE, otherwise {@code false}
     */
    boolean contains(AccessControlEntry ace) {
        return this.aces.contains(ace);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final SimpleAcl simpleAcl = (SimpleAcl) o;

        if (!this.aces.equals(simpleAcl.aces)) {
            return false;
        }

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return this.aces.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SimpleAclImpl{");
        sb.append("aces=").append(this.aces);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public ObjectIdentity getObjectIdentity() {
        // not supported
        throw new UnsupportedOperationException("The method is not implemented");
    }

    @Override
    public Sid getOwner() {
        // not supported
        throw new UnsupportedOperationException("The method is not implemented");
    }

    @Override
    public Acl getParentAcl() {
        // not supported
        throw new UnsupportedOperationException("The method is not implemented");
    }

    @Override
    public boolean isEntriesInheriting() {
        // not supported
        throw new UnsupportedOperationException("The method is not implemented");
    }

    @Override
    public boolean isGranted(List<Permission> permission, List<Sid> sids, boolean administrativeMode) {
        for (Permission p : permission) {
            for (Sid sid : sids) {
                // attempt to find exact match for this permission mask and SID
                for (AccessControlEntry ace : this.aces) {

                    if ((ace.getPermission().getMask() == p.getMask()) && ace.getSid().equals(sid)) {
                        // found a matching ACE
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public boolean isSidLoaded(List<Sid> sids) {
        // implement me
        throw new UnsupportedOperationException("The method is not implemented");
    }

    @Override
    public List<AccessControlEntry> getEntries() {
        return this.aces;
    }

}
