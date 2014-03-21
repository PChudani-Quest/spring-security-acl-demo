package acl.model;

/**
 * Simple project data model
 * 
 * @author Petr Chudanic
 */
public class Project {

    /** Project ID */
    private long id;
    /** Description */
    private String description;
    /** User owning this project */
    private User user;

    /**
     * Returns this project's ID
     * 
     * @return project ID
     */
    public long getId() {
        return this.id;
    }

    /**
     * Sets this project ID
     * 
     * @param id project ID
     */
    public void setId(long id) {
        this.id = id;
    }

    /**
     * Returns this projects description
     * 
     * @return the description
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * Sets the description
     * 
     * @param description the description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Returns the owning user
     * 
     * @return the user
     */
    public User getUser() {
        return this.user;
    }

    /**
     * Sets the owning user
     * 
     * @param user the user
     */
    public void setUser(User user) {
        this.user = user;
    }
}