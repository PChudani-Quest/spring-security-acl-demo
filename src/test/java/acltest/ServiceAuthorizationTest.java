package acltest;

import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.model.Sid;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import acl.dao.ProjectDao;
import acl.model.Project;
import acl.security.AccessPermission;
import acl.security.SimpleAclService;
import acl.service.ProjectsService;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

/**
 * Tests for service methods authorization
 * 
 * @author Petr Chudanic
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"file:src/main/webapp/WEB-INF/applicationContext.xml", 
        "file:src/main/webapp/WEB-INF/applicationContext-security.xml" 
         })
public class ServiceAuthorizationTest {

    /** Helper class for testing against local app engine services. */
    private static LocalServiceTestHelper localServiceTestHelper;

    /**
     * Class wide set up.
     * 
     * @throws Exception if any error occurs
     */
    @BeforeClass
    public static void setUpClass() throws Exception {
        localServiceTestHelper = new LocalServiceTestHelper(
                new LocalDatastoreServiceTestConfig().setDefaultHighRepJobPolicyUnappliedJobPercentage(0));
    }

    @Before
    public void setup() {
        localServiceTestHelper.setUp();

        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Autowired
    ProjectsService projectService;
    @Autowired
    ProjectDao dao;
    @Autowired
    SimpleAclService aclService;

    @Test(expected = AccessDeniedException.class)
    public void testAddReport() {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("empl1", "pass1"));
        projectService.addProject("springacltutorial");
        // now use user without USER role
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("empl2", "pass2"));

        this.projectService.addProject("springacltutorial");

        fail("should throw AccessDeniedException");

    }

    @Test(expected = AccessDeniedException.class)
    public void testGetReportByIdAccessDenied() {
        // empl1 creates report
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("empl1", "pass1"));

        final long reportId = this.projectService.addProject("springacltutorial");

        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("empl2", "pass2"));

        this.projectService.getById(reportId);

        fail("should throw AccessDeniedException");
    }

    /**
     * Test that VIEWER can access
     */
    @Test
    public void testGetReportByIdAccessGranted() {
        // empl1 creates report
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("empl1", "pass1"));
        Project reportEmpl1 = this.dao.getProjectById(this.projectService.addProject("springacltutorial"));

        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("empl2", "pass2"));
        Sid sid = new PrincipalSid(SecurityContextHolder.getContext().getAuthentication());

        this.aclService.insertAce(sid, reportEmpl1, AccessPermission.VIEWER);

        this.projectService.getById(reportEmpl1.getId());
    }
}
