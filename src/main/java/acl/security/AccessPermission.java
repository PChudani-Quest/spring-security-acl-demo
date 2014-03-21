package acl.security;

import org.springframework.security.acls.domain.AclFormattingUtils;
import org.springframework.security.acls.model.Permission;

/**
 * A set of access permissions.
 * 
 * @author Petr Giecek
 */
public enum AccessPermission implements Permission {

    /** An object viewer. */
    VIEWER(1 << 0, 'V');

    /** The integer bit mask for the permission. */
    private final int mask;

    /** The character to print for each active bit in the mask. */
    private final char code;

    /**
     * Sets the permission mask and uses the specified character for active bits.
     * 
     * @param mask the integer bit mask for the permission
     * @param code the character to print for each active bit in the mask (see {@link Permission#getPattern()})
     */
    private AccessPermission(int mask, char code) {
        this.mask = mask;
        this.code = code;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getMask() {
        return this.mask;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPattern() {
        return AclFormattingUtils.printBinary(this.mask, this.code);
    }

    /**
     * Returns the permission type with the specified mask.
     * 
     * @param mask the integer bit mask for the permission
     * @return the permission type with the specified mask
     * @throws IllegalArgumentException if there is no permission type with the specified mask
     */
    public static Permission fromMask(int mask) {
        for (AccessPermission accessPermission : values()) {
            if (accessPermission.getMask() == mask) {
                return accessPermission;
            }
        }
        throw new IllegalArgumentException("Unknown mask: " + mask);
    }

}
