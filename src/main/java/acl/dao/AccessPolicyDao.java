package acl.dao;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import acl.model.AccessPolicy;
import acl.model.AccessPolicy.SecurityIdentity;

import com.google.appengine.api.datastore.QueryResultIterable;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;

/**
 * Access policy DAO that delegates to Objectify.
 * 
 * @author Petr Giecek
 */
@Repository
public class AccessPolicyDao {

    /** Objectify instance. */
    private final Objectify objectify;

    /** Type of entities this DAO persists */
    private final Class<AccessPolicy> entityType;

    /**
     * Creates {@link AccessPolicyDao}.
     * 
     * @param objectify the {@link Objectify} instance
     */
    @Autowired
    public AccessPolicyDao(Objectify objectify) {
        this.objectify = objectify;
        this.entityType = AccessPolicy.class;
    }

    /**
     * Returns the access policies that apply for the passed security identity and domain object. Must return strongly
     * consistent results.
     *
     * @param securityIdentity the security identity
     * @param objectType the object type
     * @param objectId the object ID
     * @return the access policies that apply for the passed security identity and domain object or empty list if none
     *          found
     */
    public List<AccessPolicy> getForDomainObject(SecurityIdentity securityIdentity, String objectType, Long objectId) {
        return this.objectify.load().type(this.entityType)
                .ancestor(securityIdentity)
                .filter("objectType", objectType)
                .filter("objectId", objectId)
                .list();
    }

    /**
     * Returns the access policies that apply for the passed security identities and domain object. Must return strongly
     * consistent results.
     *
     * @param securityIdentities the security identities
     * @param objectType the object type
     * @param objectId the object ID
     * @return the access policies that apply for the passed security identities and domain object or empty list if none
     *          found
     */
    public List<AccessPolicy> getForDomainObject(List<SecurityIdentity> securityIdentities, String objectType,
            Long objectId) {

        final List<QueryResultIterable<AccessPolicy>> asyncResults = new ArrayList<QueryResultIterable<AccessPolicy>>();

        for (SecurityIdentity securityIdentity : securityIdentities) {
            // start an asynchronous query
            final QueryResultIterable<AccessPolicy> asyncResult = this.objectify.load().type(this.entityType)
                    .ancestor(securityIdentity)
                    .filter("objectType", objectType)
                    .filter("objectId", objectId)
                    .iterable();

            asyncResults.add(asyncResult);
        }

        return collectAsyncResults(asyncResults);
    }

    /**
     * Collects results from several asynchronous queries.
     * 
     * @param results the results from several asynchronous queries
     * @return the collected results in a single list
     */
    private static List<AccessPolicy> collectAsyncResults(List<QueryResultIterable<AccessPolicy>> results) {
        final List<AccessPolicy> completeResults = new ArrayList<AccessPolicy>();
        for (QueryResultIterable<AccessPolicy> partialResult : results) {
            for (AccessPolicy accessPolicy : partialResult) {
                completeResults.add(accessPolicy);
            }
        }
        return completeResults;
    }
    
    /**
     * Store AccessPolicy entity
     * 
     * @param entity entity to save
     * @return the entity key
     */
    public com.google.appengine.api.datastore.Key save(AccessPolicy entity) {
        final Key<AccessPolicy> objKey = this.objectify.save().entity(entity).now();
        return objKey.getRaw();
    }

}
