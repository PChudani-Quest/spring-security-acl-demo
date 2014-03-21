package acl.dao;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Repository;

import acl.model.Project;

/**
 * DAO used to persist {@link Project} in memory. Simplified implementation for demo purposes.
 * 
 * @author Petr Chudanic
 */
@Repository
public class ProjectDao {

    /** Sequence used to generate project identifier */
    private long sequence;
    /** Project instances holder */
    private Map<Long, Project> projects = new HashMap<Long, Project>();

    /**
     * Save project object
     * 
     * @param project the project owner
     */
    public void saveProject(Project project) {
        if (project.getId() == 0) {
            project.setId(++this.sequence);
        }
        this.projects.put(project.getId(), project);
    }

    /**
     * Get project object identified by ID
     * 
     * @param projectId the project ID
     * @return the project object
     */
    public Project getProjectById(long projectId) {
        return this.projects.get(projectId);
    }

}