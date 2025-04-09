package com.bto.integration;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;

import com.bto.model.HDBManager;
import com.bto.model.Project;
import com.bto.model.Report;
//17-23
public class ManagerTests extends BaseTest {
    
    @Test
    public void testCase17_CreateEditDeleteBTOProjectListings() {
        String testName = "Create, Edit, and Delete BTO Project Listings";
        System.out.println("\nTest Case 17: " + testName);
        
        try {
            // Login as manager
            HDBManager manager = (HDBManager) authController.login("S5678901G", "password");
            assertNotNull("Should be able to log in as HDB Manager", manager);
            
            // Create project
            String projectName = "TestProject-" + System.currentTimeMillis();
            Map<String, Integer> flatTypes = Map.of(
                "2-Room", 5,
                "3-Room", 10
            );
            
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            Date openingDate = sdf.parse("15/05/2025");
            Date closingDate = sdf.parse("20/06/2025");
            
            boolean createResult = projectController.createProject(
                manager, projectName, "Woodlands", flatTypes, 
                openingDate, closingDate, 3
            );
            
            assertTrue("Manager should create project", createResult);
            
            // Edit project
            boolean editResult = projectController.editProject(
                manager, projectName, "neighborhood", "Punggol"
            );
            
            assertTrue("Manager should edit project", editResult);
            
            // Delete project
            boolean deleteResult = projectController.deleteProject(manager, projectName);
            
            assertTrue("Manager should delete project", deleteResult);
            
        } catch (Exception e) {
            fail("Exception occurred: " + e.getMessage());
        }
    }
    
    @Test
    public void testCase18_SingleProjectManagementPerPeriod() {
        String testName = "Single Project Management per Application Period";
        System.out.println("\nTest Case 18: " + testName);
        
        try {
            // Login as manager
            HDBManager manager = (HDBManager) authController.login("S5678901G", "password");
            assertNotNull("Should be able to log in as HDB Manager", manager);
            
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            Date openingDate = sdf.parse("15/05/2025");
            Date closingDate = sdf.parse("20/06/2025");
            
            Map<String, Integer> flatTypes = Map.of(
                "2-Room", 5,
                "3-Room", 10
            );
            
            // Create first project
            boolean firstProject = projectController.createProject(
                manager, "Project1", "Woodlands", flatTypes, 
                openingDate, closingDate, 3
            );
            
            assertTrue("First project should be created", firstProject);
            
            // Try to create second project with same dates (should fail)
            boolean secondProject = projectController.createProject(
                manager, "Project2", "Punggol", flatTypes, 
                openingDate, closingDate, 3
            );
            
            assertFalse("Second project should not be created", secondProject);
            
        } catch (Exception e) {
            fail("Exception occurred: " + e.getMessage());
        }
    }
    
    @Test
    public void testCase19_ToggleProjectVisibility() {
        String testName = "Toggle Project Visibility";
        System.out.println("\nTest Case 19: " + testName);
        
        try {
            // Login as manager
            HDBManager manager = (HDBManager) authController.login("S5678901G", "password");
            assertNotNull("Should be able to log in as HDB Manager", manager);
            
            // Get a project
            Project project = projectController.getAllProjects(manager).get(0);
            
            // Toggle visibility
            boolean initialVisibility = project.isVisible();
            boolean toggleResult = projectController.toggleProjectVisibility(
                manager, project.getProjectName(), !initialVisibility
            );
            
            assertTrue("Project visibility should be toggled", toggleResult);
            
            // Verify visibility changed
            Project updatedProject = projectController.getProjectByName(project.getProjectName());
            assertEquals("Project visibility should be updated", !initialVisibility, updatedProject.isVisible());
            
        } catch (Exception e) {
            fail("Exception occurred: " + e.getMessage());
        }
    }
    
    @Test
    public void testCase22_ApproveRejectApplications() {
        String testName = "Approve or Reject BTO Applications";
        System.out.println("\nTest Case 22: " + testName);
        
        try {
            // Login as manager and applicant
            HDBManager manager = (HDBManager) authController.login("S5678901G", "password");
            assertNotNull("Should be able to log in as HDB Manager", manager);
            
            // Setup: Assume an application exists
            String applicantId = "S1234567A";
            
            // Approve application
            boolean approveResult = applicationController.processApplication(applicantId, true);
            assertTrue("Manager should approve application", approveResult);
            
            // Reject application (or simulate withdrawal)
            boolean rejectResult = applicationController.processApplication(applicantId, false);
            assertTrue("Manager should process application", rejectResult);
            
        } catch (Exception e) {
            fail("Exception occurred: " + e.getMessage());
        }
    }
    
    @Test
    public void testCase23_GenerateFilteredReports() {
        String testName = "Generate and Filter Reports";
        System.out.println("\nTest Case 23: " + testName);
        
        try {
            // Login as manager
            HDBManager manager = (HDBManager) authController.login("S5678901G", "password");
            assertNotNull("Should be able to log in as HDB Manager", manager);
            
            // Generate overall booking report
            Report generalReport = reportController.generateBookingReport(manager);
            assertNotNull("Should generate general booking report", generalReport);
            
            // Generate filtered reports
            Map<String, Object> singleFilter = Map.of("maritalStatus", "Single");
            Report singleReport = reportController.generateFilteredReport(manager, singleFilter);
            assertNotNull("Should generate filtered report for singles", singleReport);
            
            Map<String, Object> flatTypeFilter = Map.of("flatType", "2-Room");
            Report flatTypeReport = reportController.generateFilteredReport(manager, flatTypeFilter);
            assertNotNull("Should generate filtered report by flat type", flatTypeReport);
            
        } catch (Exception e) {
            fail("Exception occurred: " + e.getMessage());
        }
    }
}