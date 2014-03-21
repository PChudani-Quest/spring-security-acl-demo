package acl.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import acl.dao.ProjectDao;
import acl.model.Project;
import acl.model.User;

/**
 * Project service
 * 
 * @author Petr Chudanic
 */
@Service(value="projectsService")
@Scope(value="prototype")
public class ProjectsService {

    /** Project DAO */
    @Autowired
    private ProjectDao dao;

    /**
     * Create new project and sets current user as its owner
     * 
     * @param description the project description
     * @return the project ID
     */
    // This method should be accessible only to users with role EMPLOYEE
    @PreAuthorize("hasRole('ROLE_USER')")
    public long addProject(String description) {
        final Project project = new Project();
        project.setDescription(description);
        final Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            if (auth.getPrincipal() instanceof UserDetails) {
                project.setUser(new User(((UserDetails) auth.getPrincipal()).getUsername()));
            }
            else {
                project.setUser(new User(auth.getPrincipal().toString()));
            }
        }
        this.dao.saveProject(project);
        return project.getId();
    }

    /**
     * Returns the project based on its ID
     * 
     * @param id the project's ID
     * @return the project
     */
    // Only VIEWERs or project owning user should be able to see Project
    @PostAuthorize("hasPermission(returnObject, 'VIEWER') or (returnObject.user.login == authentication.name)")
    public Project getById(long id) {

        return this.dao.getProjectById(id);
    }

}