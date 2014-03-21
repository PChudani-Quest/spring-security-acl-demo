package acl.model;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Ignore;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Parent;

/**
 * Represents an access policy for a domain object. Since the most up-to-date value for a given security identity is
 * often needed, it is designed so that queries based on security identity are always strong consistent. The security
 * identity is considered to be a parent of the given policy.
 *
 * @author Petr Giecek
 */
@Entity
public class AccessPolicy {

    /**
     * <p>
     * Indirection between actual security objects (e.g. principals, roles, groups etc) and what is actually stored in
     * persistent storage. Security identity is stored as a parent of a respective access policy and hence all queries
     * using a security identity are strong consistent meaning they always return most up-to-date results.
     * </p>
     * <p>
     * Security identity is never stored itself, only as part of a respective access policy.
     * </p>
     *
     * @author Petr Giecek
     */
    @Entity
    public static class SecurityIdentity {

        /**
         * Security identity type.
         *
         * @author Petr Giecek
         */
        public static enum Type {

            /** User security identity */
            USER,

            /** Role security identity. */
            ROLE
        }

        /** Serial version UID. */
        @SuppressWarnings("unused")
        private static final long serialVersionUID = 2L;

        /** Security identity in the form of {type}:{identity}. */
        @Id
        private String sid;

        /** Security identity type. */
        @Ignore
        private Type type;

        /** Actual identity. */
        @Ignore
        private String identity;

        /**
         * Default constructor needed by Objectify.
         */
        private SecurityIdentity() {
            super();
        }

        /**
         * Creates security identity based on provided {@link Key} instance.
         *
         * @param key the {@link Key} instance
         */
        private SecurityIdentity(Key<SecurityIdentity> key) {
            final String sid = key.getName();
            final String[] split = sid.split(":");

            this.type = Type.valueOf(split[0]);
            this.identity = split[1];
            this.sid = sid;
        }

        /**
         * Create security identity with the specified details.
         *
         * @param type security identity type
         * @param identity the actual identity
         */
        private SecurityIdentity(Type type, String identity) {
            this.type = type;
            this.identity = identity;
            this.sid = type.name() + ":" + identity;
        }

        /**
         * Returns a security identity based on provided {@link Key} instance.
         *
         * @param key the {@link Key} instance
         * @return a security identity based on provided {@link Key} instance
         */
        private static SecurityIdentity valueOf(Key<SecurityIdentity> key) {
            return new SecurityIdentity(key);
        }

        /**
         * Returns a security identity representing the given type and actual identity.
         *
         * @param type the security identity type
         * @param identity the actual identity
         * @return a security identity representing the given type and actual identity
         */
        public static SecurityIdentity valueOf(Type type, String identity) {
            return new SecurityIdentity(type, identity);
        }

        /**
         * Returns the security identity as respective {@link Key} instance.
         *
         * @return the respective {@link Key} instance
         */
        private Key<SecurityIdentity> asKey() {
            return Key.create(this);
        }

        /**
         * Returns the security identity type.
         *
         * @return the security identity type
         */
        public Type getType() {
            return this.type;
        }

        /**
         * Returns the actual identity (e.g an email in case of a user or role name in case of a role).
         *
         * @return the actual identity
         */
        public String getIdentity() {
            return this.identity;
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

            final SecurityIdentity that = (SecurityIdentity) o;

            if (this.sid != null ? !this.sid.equals(that.sid) : that.sid != null) {
                return false;
            }

            return true;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int hashCode() {
            return this.sid != null ? this.sid.hashCode() : 0;
        }

    }

    /** Serial version UID. */
    @SuppressWarnings("unused")
    private static final long serialVersionUID = 2L;

    /** Access policy ID. */
    @Id
    private Long id;

    /** Security identity. */
    @Parent
    private Key<SecurityIdentity> securityIdentity;

    /** Domain object type. */
    @Index
    private String objectType;

    /** Domain object ID. */
    @Index
    private Long objectId;

    /** Granted permission. */
    private String permission;

    /**
     * Default constructor,
     */
    public AccessPolicy() {
        super();
    }

    /**
     * Creates {@link AccessPolicy} for the specified domain object.
     *
     * @param securityIdentity the security identity
     * @param objectType the domain object type
     * @param objectId the domain object ID
     * @param permission the granted permission
     */
    public AccessPolicy(SecurityIdentity securityIdentity, String objectType, Long objectId, String permission) {
        this(null, securityIdentity, objectType, objectId, permission);
    }

    /**
     * Creates {@link AccessPolicy} for the specified domain object.
     *
     * @param id the access policy ID
     * @param securityIdentity the security identity
     * @param objectType the domain object type
     * @param objectId the domain object ID
     * @param permission the granted permission
     */
    public AccessPolicy(Long id, SecurityIdentity securityIdentity, String objectType, Long objectId,
            String permission) {
        this.id = id;
        this.securityIdentity = securityIdentity.asKey();
        this.objectType = objectType;
        this.objectId = objectId;
        this.permission = permission;
    }

    /**
     * Returns the access policy ID.
     *
     * @return the access policy ID
     */
    public Long getId() {
        return this.id;
    }

    /**
     * Sets the access policy ID.
     *
     * @param id the access policy ID
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Returns the security identity.
     *
     * @return the security identity
     */
    public SecurityIdentity getSecurityIdentity() {
        return SecurityIdentity.valueOf(this.securityIdentity);
    }

    /**
     * Sets the security identity.
     *
     * @param securityIdentity the security identity
     */
    public void setSecurityIdentity(SecurityIdentity securityIdentity) {
        this.securityIdentity = securityIdentity.asKey();
    }

    /**
     * Returns the domain object type.
     *
     * @return the domain object type
     */
    public String getObjectType() {
        return this.objectType;
    }

    /**
     * Sets the domain object type.
     *
     * @param objectType the domain object type
     */
    public void setObjectType(String objectType) {
        this.objectType = objectType;
    }

    /**
     * Returns the domain object ID.
     *
     * @return the domain object ID
     */
    public Long getObjectId() {
        return this.objectId;
    }

    /**
     * Sets the domain object ID.
     *
     * @param objectId the domain object ID
     */
    public void setObjectId(Long objectId) {
        this.objectId = objectId;
    }

    /**
     * Returns the granted permission.
     *
     * @return the granted permission
     */
    public String getPermission() {
        return this.permission;
    }

    /**
     * Sets the granted permission.
     *
     * @param permission the granted permission
     */
    public void setPermission(String permission) {
        this.permission = permission;
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

        final AccessPolicy that = (AccessPolicy) o;

        if (this.id != null ? !this.id.equals(that.id) : that.id != null) {
            return false;
        }
        if (this.securityIdentity != null ? !this.securityIdentity
                .equals(that.securityIdentity) : that.securityIdentity != null) {
            return false;
        }

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int result = this.id != null ? this.id.hashCode() : 0;
        result = 31 * result + (this.securityIdentity != null ? this.securityIdentity.hashCode() : 0);
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AccessPolicy{");
        sb.append("id=").append(this.id);
        sb.append(", securityIdentity=").append(this.securityIdentity);
        sb.append(", objectType='").append(this.objectType).append('\'');
        sb.append(", objectId=").append(this.objectId);
        sb.append(", permission='").append(this.permission).append('\'');
        sb.append('}');
        return sb.toString();
    }

}
