package com.bto.integration;

import java.util.List;
import com.bto.model.User;
import com.bto.model.Applicant;
import com.bto.model.HDBManager;
import com.bto.model.Project;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test cases for project visibility functionality
 * Covers test cases 5-7 from Appendix A
 */
public class ProjectVisibilityTests extends BaseTest {
    
    /**
     * Test Case 5: Project Visibility Based on User Group and Toggle
     * Expected: Projects are visible to users based on their age, marital status and visibility setting
     */
    @Test
    public void testCase5_ProjectVisibilityBasedOnUserGroup() {
        String testName = "Project Visibility Based on User Group and Toggle";
        System.out.println("\nTest Case 5: " + testName);
        
        try {
            // Login as a single applicant (John: 35, Single)
            User singleApplicant = authController.login("S1234567A", "password");
            assertNotNull("Should be able to log in as single applicant", singleApplicant);
            assertTrue("Single applicant login should return an Applicant instance", singleApplicant instanceof Applicant);
            
            // Get visible projects for single applicant
            List<Project> singleProjects = projectController.getVisibleProjects((Applicant)singleApplicant);
            assertNotNull("Visible projects list should not be null", singleProjects);
            
            // Check if single applicant can see appropriate projects (2-Room only)
            boolean canSeeTwoRoom = false;
            boolean canSeeThreeRoom = false;
            
            for (Project project : singleProjects) {
                if (project.hasFlatType("2-Room")) canSeeTwoRoom = true;
                if (project.hasFlatType("3-Room")) canSeeThreeRoom = true;
            }
            
            assertTrue("Single applicant (35+) should see 2-Room flats", canSeeTwoRoom);
            assertFalse("Single applicant (35+) should not see 3-Room flats", canSeeThreeRoom);
            
            if (canSeeTwoRoom && !canSeeThreeRoom) {
                printPass(testName, "Single applicant (35+) can see 2-Room flats only");
            } else if (!canSeeTwoRoom) {
                printFail(testName, "Single applicant (35+) cannot see 2-Room flats");
            } else if (canSeeThreeRoom) {
                printFail(testName, "Single applicant (35+) can see 3-Room flats (should not)");
            }
            
            // Login as a married applicant (James: 30, Married)
            User marriedApplicant = authController.login("T2345678D", "password");
            assertNotNull("Should be able to log in as married applicant", marriedApplicant);
            assertTrue("Married applicant login should return an Applicant instance", marriedApplicant instanceof Applicant);
            
            // Get visible projects for married applicant
            List<Project> marriedProjects = projectController.getVisibleProjects((Applicant)marriedApplicant);
            assertNotNull("Visible projects list should not be null", marriedProjects);
            
            // Check if married applicant can see both flat types
            canSeeTwoRoom = false;
            canSeeThreeRoom = false;
            
            for (Project project : marriedProjects) {
                if (project.hasFlatType("2-Room")) canSeeTwoRoom = true;
                if (project.hasFlatType("3-Room")) canSeeThreeRoom = true;
            }
            
            assertTrue("Married applicant should see 2-Room flats", canSeeTwoRoom);
            assertTrue("Married applicant should see 3-Room flats", canSeeThreeRoom);
            
            if (canSeeTwoRoom && canSeeThreeRoom) {
                printPass(testName, "Married applicant can see both 2-Room and 3-Room flats");
            } else {
                printFail(testName, "Married applicant cannot see all eligible flat types");
            }
            
            // Toggle visibility test
            // Login as manager
            User manager = authController.login("S5678901G", "password");
            assertNotNull("Should be able to log in as HDB Manager", manager);
            assertTrue("Manager login should return an HDBManager instance", manager instanceof HDBManager);
            
            // Toggle visibility off for a project
            String projectName = "Acacia Breeze";
            boolean toggleResult = projectController.toggleProjectVisibility((HDBManager)manager, projectName, false);
            assertTrue("Should be able to toggle project visibility", toggleResult);
            
            // Check if project is now invisible to applicants
            List<Project> projectsAfterToggle = projectController.getVisibleProjects((Applicant)singleApplicant);
            boolean projectVisible = projectsAfterToggle.stream()
                .anyMatch(p -> p.getProjectName().equals(projectName));
            
            assertFalse("Project should be invisible after toggling off", projectVisible);
            
            if (!projectVisible) {
                printPass(testName, "Project successfully hidden after visibility toggle off");
            } else {
                printFail(testName, "Project still visible after toggle off");
            }
            
            // Toggle visibility back on for other tests
            toggleResult = projectController.toggleProjectVisibility((HDBManager)manager, projectName, true);
            assertTrue("Should be able to toggle project visibility back on", toggleResult);
            
        } catch (Exception e) {
            fail("Exception occurred: " + e.getMessage());
            printFail(testName, "Exception occurred: " + e.getMessage());
        }
    }
    
    /**
     * Test Case 6: Project Application
     * Expected: Users can only apply for projects relevant to their group
     */
    @Test
    public void testCase6_ProjectApplication() {
        String testName = "Project Application";
        System.out.println("\nTest Case 6: " + testName);
        
        try {
            // Login as a single applicant (John: 35, Single)
            User singleApplicant = authController.login("S1234567A", "password");
            assertNotNull("Should be able to log in as single applicant", singleApplicant);
            assertTrue("Single applicant login should return an Applicant instance", singleApplicant instanceof Applicant);
            
            // Get project to apply for
            List<Project> singleProjects = projectController.getVisibleProjects((Applicant)singleApplicant);
            assertFalse("Should have visible projects to apply for", singleProjects.isEmpty());
            
            Project project = singleProjects.get(0);
            
            // Attempt to apply for 2-Room (should succeed)
            boolean applyResult2Room = applicationController.applyForProject(
                (Applicant)singleApplicant, project.getProjectName(), "2-Room");
            
            assertTrue("Single applicant (35+) should be able to apply for 2-Room", applyResult2Room);
            
            if (applyResult2Room) {
                printPass(testName, "Single applicant (35+) successfully applied for 2-Room");
                
                // Cancel application for other tests
                boolean withdrawResult = applicationController.withdrawApplication((Applicant)singleApplicant);
                assertTrue("Should be able to withdraw application", withdrawResult);
            } else {
                printFail(testName, "Single applicant (35+) could not apply for 2-Room");
            }
            
            // Attempt to apply for 3-Room (should fail)
            boolean applyResult3Room = applicationController.applyForProject(
                (Applicant)singleApplicant, project.getProjectName(), "3-Room");
            
            assertFalse("Single applicant (35+) should not be able to apply for 3-Room", applyResult3Room);
            
            if (!applyResult3Room) {
                printPass(testName, "Single applicant (35+) prevented from applying for 3-Room");
            } else {
                printFail(testName, "Single applicant (35+) incorrectly allowed to apply for 3-Room");
                // Cancel application for other tests
                applicationController.withdrawApplication((Applicant)singleApplicant);
            }
            
            // Login as a married applicant (James: 30, Married)
            User marriedApplicant = authController.login("T2345678D", "password");
            assertNotNull("Should be able to log in as married applicant", marriedApplicant);
            assertTrue("Married applicant login should return an Applicant instance", marriedApplicant instanceof Applicant);
            
            // Attempt to apply for 2-Room (should succeed)
            boolean marriedApply2Room = applicationController.applyForProject(
                (Applicant)marriedApplicant, project.getProjectName(), "2-Room");
            
            assertTrue("Married applicant should be able to apply for 2-Room", marriedApply2Room);
            
            if (marriedApply2Room) {
                printPass(testName, "Married applicant successfully applied for 2-Room");
                
                // Cancel application for next test
                boolean withdrawResult = applicationController.withdrawApplication((Applicant)marriedApplicant);
                assertTrue("Should be able to withdraw application", withdrawResult);
            } else {
                printFail(testName, "Married applicant could not apply for 2-Room");
            }
            
            // Attempt to apply for 3-Room (should succeed)
            boolean marriedApply3Room = applicationController.applyForProject(
                (Applicant)marriedApplicant, project.getProjectName(), "3-Room");
            
            assertTrue("Married applicant should be able to apply for 3-Room", marriedApply3Room);
            
            if (marriedApply3Room) {
                printPass(testName, "Married applicant successfully applied for 3-Room");
                
                // Cancel application for other tests
                boolean withdrawResult = applicationController.withdrawApplication((Applicant)marriedApplicant);
                assertTrue("Should be able to withdraw application", withdrawResult);
            } else {
                printFail(testName, "Married applicant could not apply for 3-Room");
            }
            
        } catch (Exception e) {
            fail("Exception occurred: " + e.getMessage());
            printFail(testName, "Exception occurred: " + e.getMessage());
        }
    }
    
    /**
     * Test Case 7: Viewing Application Status after Visibility Toggle Off
     * Expected: Applicants continue to have access to their application details
     * regardless of project visibility setting
     */
    @Test
    public void testCase7_ViewingApplicationStatusAfterVisibilityToggleOff() {
        String testName = "Viewing Application Status after Visibility Toggle Off";
        System.out.println("\nTest Case 7: " + testName);
        
        try {
            // Login as an applicant
            User applicant = authController.login("S1234567A", "password");
            assertNotNull("Should be able to log in as applicant", applicant);
            assertTrue("Applicant login should return an Applicant instance", applicant instanceof Applicant);
            
            // Get project to apply for
            List<Project> projects = projectController.getVisibleProjects((Applicant)applicant);
            assertFalse("Should have visible projects to apply for", projects.isEmpty());
            
            Project project = projects.get(0);
            String projectName = project.getProjectName();
            
            // Apply for project
            boolean applyResult = applicationController.applyForProject(
                (Applicant)applicant, projectName, "2-Room");
            
            assertTrue("Should be able to apply for project", applyResult);
            printPass(testName, "Successfully applied for project");
            
            // Login as manager
            User manager = authController.login("S5678901G", "password");
            assertNotNull("Should be able to log in as HDB Manager", manager);
            assertTrue("Manager login should return an HDBManager instance", manager instanceof HDBManager);
            
            // Toggle visibility off for the project
            boolean toggleResult = projectController.toggleProjectVisibility(
                (HDBManager)manager, projectName, false);
            
            assertTrue("Should be able to toggle project visibility", toggleResult);
            
            // Check if applicant can still view their application
            var application = applicationController.getApplication((Applicant)applicant);
            
            assertNotNull("Application should still be accessible after visibility toggle", application);
            assertEquals("Application should be for the correct project", projectName, application.getProjectName());
            
            if (application != null && application.getProjectName().equals(projectName)) {
                printPass(testName, "Applicant can view application after visibility toggled off");
            } else {
                printFail(testName, "Applicant cannot view application after visibility toggled off");
            }
            
            // Toggle visibility back on for other tests
            toggleResult = projectController.toggleProjectVisibility((HDBManager)manager, projectName, true);
            assertTrue("Should be able to toggle project visibility back on", toggleResult);
            
            // Clean up application
            boolean withdrawResult = applicationController.withdrawApplication((Applicant)applicant);
            assertTrue("Should be able to withdraw application", withdrawResult);
            
        } catch (Exception e) {
            fail("Exception occurred: " + e.getMessage());
            printFail(testName, "Exception occurred: " + e.getMessage());
        }
    }
}