package com.bto.integration;

import com.bto.model.User;
import com.bto.model.Applicant;
import com.bto.model.HDBManager;
import com.bto.model.HDBOfficer;
import com.bto.model.Project;
import com.bto.model.Application;
import com.bto.model.Enquiry;
import com.bto.model.Report;
import java.util.List;
import java.util.Map;
import java.util.Date;
import java.text.SimpleDateFormat;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test cases for HDB Manager functionality
 * Covers test cases 17-23 from Appendix A
 */
public class ManagerTests extends BaseTest {
    
    /**
     * Test Case 17: Create, Edit, and Delete BTO Project Listings
     * Expected: Managers should be able to add new projects, modify existing project details,
     * and remove projects from the system
     */
    @Test
    public void testCase17_CreateEditAndDeleteBTOProjectListings() {
        String testName = "Create, Edit, and Delete BTO Project Listings";
        System.out.println("\nTest Case 17: " + testName);
        
        try {
            // Login as manager
            User manager = authController.login("S5678901G", "password");
            assertNotNull("Should be able to log in as HDB Manager", manager);
            assertTrue("Should get a HDBManager instance", manager instanceof HDBManager);
            
            // Create a new project
            String projectName = "Test Project " + System.currentTimeMillis();
            String neighborhood = "Woodlands";
            Map<String, Integer> flatTypes = Map.of(
                "2-Room", 5,
                "3-Room", 10
            );
            // Create dates for project application period
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            Date openingDate = sdf.parse("15/05/2025");
            Date closingDate = sdf.parse("20/06/2025");
            int officerSlots = 3;
            
            // Create project
            boolean createResult = projectController.createProject(
                (HDBManager)manager, projectName, neighborhood, flatTypes, 
                openingDate, closingDate, officerSlots);
            
            assertTrue("Manager should be able to create new project", createResult);
            printPass(testName, "Successfully created new project");
            
            // Check if project exists
            Project createdProject = projectController.getProjectByName(projectName);
            assertNotNull("Should be able to retrieve created project", createdProject);
            
            if (createdProject == null) {
                printFail(testName, "Created project not found in system");
                return;
            }
            
            // Edit project
            String newNeighborhood = "Punggol";
            boolean editResult = projectController.editProject(
                (HDBManager)manager, projectName, "neighborhood", newNeighborhood);
            
            assertTrue("Manager should be able to edit project", editResult);
            
            if (!editResult) {
                printFail(testName, "Could not edit project");
                
                // Clean up
                projectController.deleteProject((HDBManager)manager, projectName);
                return;
            }
            
            // Verify edit
            Project editedProject = projectController.getProjectByName(projectName);
            assertNotNull("Should be able to retrieve edited project", editedProject);
            assertEquals("Project neighborhood should be updated", newNeighborhood, editedProject.getNeighborhood());
            
            if (editedProject != null && editedProject.getNeighborhood().equals(newNeighborhood)) {
                printPass(testName, "Successfully edited project neighborhood");
            } else {
                printFail(testName, "Project edit not reflected in system");
            }
            
            // Delete project
            boolean deleteResult = projectController.deleteProject((HDBManager)manager, projectName);
            
            assertTrue("Manager should be able to delete project", deleteResult);
            
            if (!deleteResult) {
                printFail(testName, "Could not delete project");
                return;
            }
            
            // Verify deletion
            Project deletedProject = projectController.getProjectByName(projectName);
            assertNull("Deleted project should not be found", deletedProject);
            
            if (deletedProject == null) {
                printPass(testName, "Successfully deleted project");
            } else {
                printFail(testName, "Project still exists after deletion");
                
                // Clean up if still exists
                projectController.deleteProject((HDBManager)manager, projectName);
            }
            
        } catch (Exception e) {
            fail("Exception occurred: " + e.getMessage());
            printFail(testName, "Exception occurred: " + e.getMessage());
        }
    }
    
    /**
     * Test Case 18: Single Project Management per Application Period
     * Expected: System prevents assignment of more than one project to a manager 
     * within the same application dates
     */
    @Test
    public void testCase18_SingleProjectManagementPerApplicationPeriod() {
        String testName = "Single Project Management per Application Period";
        System.out.println("\nTest Case 18: " + testName);
        
        try {
            // Login as manager
            User manager = authController.login("S5678901G", "password");
            assertNotNull("Should be able to log in as HDB Manager", manager);
            assertTrue("Should get a HDBManager instance", manager instanceof HDBManager);
            
            // Create first project
            String projectName1 = "Test Project 1-" + System.currentTimeMillis();
            String neighborhood1 = "Woodlands";
            Map<String, Integer> flatTypes1 = Map.of(
                "2-Room", 5,
                "3-Room", 10
            );
            // Create dates for project application period
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            Date openingDate = sdf.parse("15/05/2025");
            Date closingDate = sdf.parse("20/06/2025");
            int officerSlots1 = 3;
            
            // Create first project
            boolean createResult1 = projectController.createProject(
                (HDBManager)manager, projectName1, neighborhood1, flatTypes1, 
                openingDate, closingDate, officerSlots1);
            
            assertTrue("Manager should be able to create first project", createResult1);
            printPass(testName, "Successfully created first project");
            
            // Try to create second project with overlapping dates (should fail)
            String projectName2 = "Test Project 2-" + System.currentTimeMillis();
            String neighborhood2 = "Punggol";
            Map<String, Integer> flatTypes2 = Map.of(
                "2-Room", 3,
                "3-Room", 7
            );
            // Same dates as first project
            int officerSlots2 = 2;
            
            boolean createResult2 = projectController.createProject(
                (HDBManager)manager, projectName2, neighborhood2, flatTypes2, 
                openingDate, closingDate, officerSlots2);
            
            assertFalse("System should prevent creating second project with overlapping dates", createResult2);
            
            if (!createResult2) {
                printPass(testName, "System prevented creating second project with overlapping dates");
            } else {
                printFail(testName, "System incorrectly allowed creating second project with overlapping dates");
                
                // Clean up second project
                projectController.deleteProject((HDBManager)manager, projectName2);
            }
            
            // Create project with non-overlapping dates (should succeed)
            Date laterOpeningDate = sdf.parse("15/07/2025");
            Date laterClosingDate = sdf.parse("20/08/2025");
            
            boolean createResult3 = projectController.createProject(
                (HDBManager)manager, projectName2, neighborhood2, flatTypes2, 
                laterOpeningDate, laterClosingDate, officerSlots2);
            
            assertTrue("Manager should be able to create project with non-overlapping dates", createResult3);
            
            if (createResult3) {
                printPass(testName, "System correctly allowed creating project with non-overlapping dates");
                
                // Clean up second project
                projectController.deleteProject((HDBManager)manager, projectName2);
            } else {
                printFail(testName, "Could not create project with non-overlapping dates");
            }
            
            // Clean up first project
            projectController.deleteProject((HDBManager)manager, projectName1);
            
        } catch (Exception e) {
            fail("Exception occurred: " + e.getMessage());
            printFail(testName, "Exception occurred: " + e.getMessage());
        }
    }
    
    /**
     * Test Case 19: Toggle Project Visibility
     * Expected: Changes in visibility should be reflected accurately in the project list visible to applicants
     */
    @Test
    public void testCase19_ToggleProjectVisibility() {
        String testName = "Toggle Project Visibility";
        System.out.println("\nTest Case 19: " + testName);
        
        try {
            // Login as manager
            User manager = authController.login("S5678901G", "password");
            assertNotNull("Should be able to log in as HDB Manager", manager);
            assertTrue("Should get a HDBManager instance", manager instanceof HDBManager);
            
            // Login as applicant
            User applicant = authController.login("S1234567A", "password");
            assertNotNull("Should be able to log in as applicant", applicant);
            assertTrue("Should get an Applicant instance", applicant instanceof Applicant);
            
            // Get available projects
            List<Project> managerProjects = projectController.getAllProjects((HDBManager)manager);
            assertFalse("Should have projects available for testing", managerProjects.isEmpty());
            
            Project project = managerProjects.get(0);
            String projectName = project.getProjectName();
            
            // Get initial visibility
            boolean initialVisibility = project.isVisible();
            
            // Toggle visibility to opposite of current value
            boolean toggleResult = projectController.toggleProjectVisibility(
                (HDBManager)manager, projectName, !initialVisibility);
            
            assertTrue("Manager should be able to toggle project visibility", toggleResult);
            
            // Verify visibility change in manager's view
            Project updatedProjectManager = projectController.getProjectByName(projectName);
            assertNotNull("Should be able to retrieve updated project", updatedProjectManager);
            assertEquals("Project visibility should be toggled", !initialVisibility, updatedProjectManager.isVisible());
            
            if (updatedProjectManager.isVisible() == !initialVisibility) {
                printPass(testName, "Project visibility correctly toggled in manager's view");
            } else {
                printFail(testName, "Project visibility not correctly toggled in manager's view");
            }
            
            // Verify visibility change in applicant's view
            List<Project> applicantProjects = projectController.getVisibleProjects((Applicant)applicant);
            boolean projectVisibleToApplicant = applicantProjects.stream()
                .anyMatch(p -> p.getProjectName().equals(projectName));
            
            assertEquals("Project visibility to applicant should match toggled setting", !initialVisibility, projectVisibleToApplicant);
            
            if (projectVisibleToApplicant == !initialVisibility) {
                printPass(testName, "Project visibility correctly reflected in applicant's view");
            } else {
                printFail(testName, "Project visibility not correctly reflected in applicant's view");
            }
            
            // Restore original visibility
            toggleResult = projectController.toggleProjectVisibility(
                (HDBManager)manager, projectName, initialVisibility);
            assertTrue("Manager should be able to restore original visibility", toggleResult);
            
        } catch (Exception e) {
            fail("Exception occurred: " + e.getMessage());
            printFail(testName, "Exception occurred: " + e.getMessage());
        }
    }
    
    /**
     * Test Case 20: View All and Filtered Project Listings
     * Expected: Managers should see all projects and be able to apply filters to narrow down to their own projects
     */
    @Test
    public void testCase20_ViewAllAndFilteredProjectListings() {
        String testName = "View All and Filtered Project Listings";
        System.out.println("\nTest Case 20: " + testName);
        
        try {
            // Login as primary manager
            User manager1 = authController.login("S5678901G", "password");
            assertNotNull("Should be able to log in as primary HDB Manager", manager1);
            assertTrue("Should get a HDBManager instance", manager1 instanceof HDBManager);
            
            // Login as secondary manager (if available)
            User manager2 = authController.login("T8765432F", "password");
            assertNotNull("Should be able to log in as secondary HDB Manager", manager2);
            assertTrue("Should get a HDBManager instance", manager2 instanceof HDBManager);
            
            // Get all projects visible to manager1
            List<Project> allProjects = projectController.getAllProjects((HDBManager)manager1);
            
            assertFalse("Should have projects available for testing", allProjects.isEmpty());
            
            printPass(testName, "Manager can view all projects: " + allProjects.size() + " projects found");
            
            // Create a new project for manager1 for testing filters
            String projectName = "Filter Test Project-" + System.currentTimeMillis();
            String neighborhood = "Tampines";
            Map<String, Integer> flatTypes = Map.of(
                "2-Room", 3,
                "3-Room", 5
            );
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            Date openingDate = sdf.parse("15/09/2025");
            Date closingDate = sdf.parse("20/10/2025");
            int officerSlots = 2;
            
            boolean createResult = projectController.createProject(
                (HDBManager)manager1, projectName, neighborhood, flatTypes, 
                openingDate, closingDate, officerSlots);
            
            assertTrue("Manager should be able to create test project for filtering", createResult);
            
            // Get projects created by manager1
            List<Project> manager1Projects = projectController.getManagerProjects((HDBManager)manager1);
            
            boolean filterWorking = manager1Projects.stream()
                .anyMatch(p -> p.getProjectName().equals(projectName));
            
            assertTrue("Filter should show manager's own projects", filterWorking);
            
            if (filterWorking) {
                printPass(testName, "Filter correctly shows manager's own projects");
            } else {
                printFail(testName, "Filter does not correctly show manager's own projects");
            }
            
            // Clean up test project
            boolean deleteResult = projectController.deleteProject((HDBManager)manager1, projectName);
            assertTrue("Manager should be able to delete test project", deleteResult);
            
        } catch (Exception e) {
            fail("Exception occurred: " + e.getMessage());
            printFail(testName, "Exception occurred: " + e.getMessage());
        }
    }
    
    /**
     * Test Case 21: Manage HDB Officer Registrations
     * Expected: Managers handle officer registrations effectively, with system updates reflecting changes accurately
     */
    @Test
    public void testCase21_ManageHDBOfficerRegistrations() {
        String testName = "Manage HDB Officer Registrations";
        System.out.println("\nTest Case 21: " + testName);
        
        try {
            // Login as manager
            User manager = authController.login("S5678901G", "password");
            assertNotNull("Should be able to log in as HDB Manager", manager);
            assertTrue("Should get a HDBManager instance", manager instanceof HDBManager);
            
            // Login as officer
            User officer = authController.login("T2109876H", "password");
            assertNotNull("Should be able to log in as HDB Officer", officer);
            assertTrue("Should get a HDBOfficer instance", officer instanceof HDBOfficer);
            
            // Get project to work with
            List<Project> projects = projectController.getAllProjects((HDBManager)manager);
            assertFalse("Should have projects available for testing", projects.isEmpty());
            
            Project project = projects.get(0);
            String projectName = project.getProjectName();
            
            // Get initial officer slot count
            int initialSlots = project.getAvailableOfficerSlots();
            
            // Officer registers for project
            boolean registerResult = projectController.registerOfficerForProject(
                (HDBOfficer)officer, projectName);
            
            assertTrue("Officer should be able to register for project", registerResult);
            
            // Manager views pending registrations
            List<HDBOfficer> pendingOfficers = projectController.getPendingOfficerRegistrations(
                (HDBManager)manager, projectName);
            
            boolean officerInPendingList = pendingOfficers.stream()
                .anyMatch(o -> o.getNRIC().equals(((HDBOfficer)officer).getNRIC()));
            
            assertTrue("Officer should appear in pending registrations list", officerInPendingList);
            
            if (officerInPendingList) {
                printPass(testName, "Manager can view pending officer registrations");
            } else {
                printFail(testName, "Manager cannot view pending officer registrations");
                
                // Clean up registration
                projectController.removeOfficerFromProject((HDBManager)manager, 
                    projectName, ((HDBOfficer)officer).getNRIC());
                return;
            }
            
            // Manager approves registration
            boolean approveResult = projectController.approveOfficerRegistration(
                (HDBManager)manager, projectName, ((HDBOfficer)officer).getNRIC());
            
            assertTrue("Manager should be able to approve officer registration", approveResult);
            
            // Verify officer slots updated correctly
            Project updatedProject = projectController.getProjectByName(projectName);
            assertNotNull("Should be able to retrieve updated project", updatedProject);
            
            int newSlots = updatedProject.getAvailableOfficerSlots();
            assertEquals("Officer slots should decrease by 1 after approval", initialSlots - 1, newSlots);
            
            if (newSlots == initialSlots - 1) {
                printPass(testName, "Officer slots correctly updated after approval");
            } else {
                printFail(testName, "Officer slots not correctly updated after approval");
            }
            
            // Verify officer is in approved list
            List<HDBOfficer> approvedOfficers = projectController.getApprovedOfficers(
                (HDBManager)manager, projectName);
            
            boolean officerInApprovedList = approvedOfficers.stream()
                .anyMatch(o -> o.getNRIC().equals(((HDBOfficer)officer).getNRIC()));
            
            assertTrue("Officer should appear in approved officers list", officerInApprovedList);
            
            if (officerInApprovedList) {
                printPass(testName, "Officer correctly appears in approved list");
            } else {
                printFail(testName, "Officer not appearing in approved list");
            }
            
            // Clean up registration
            boolean removeResult = projectController.removeOfficerFromProject((HDBManager)manager, 
                projectName, ((HDBOfficer)officer).getNRIC());
            assertTrue("Manager should be able to remove officer from project", removeResult);
            
        } catch (Exception e) {
            fail("Exception occurred: " + e.getMessage());
            printFail(testName, "Exception occurred: " + e.getMessage());
        }
    }
    
    /**
     * Test Case 22: Approve or Reject BTO Applications and Withdrawals
     * Expected: Approvals and rejections are processed correctly, with system updates to reflect the decision
     */
    @Test
    public void testCase22_ApproveOrRejectBTOApplicationsAndWithdrawals() {
        String testName = "Approve or Reject BTO Applications and Withdrawals";
        System.out.println("\nTest Case 22: " + testName);
        
        try {
            // Login as manager
            User manager = authController.login("S5678901G", "password");
            assertNotNull("Should be able to log in as HDB Manager", manager);
            assertTrue("Should get a HDBManager instance", manager instanceof HDBManager);
            
            // Login as applicant
            User applicant = authController.login("S1234567A", "password");
            assertNotNull("Should be able to log in as applicant", applicant);
            assertTrue("Should get an Applicant instance", applicant instanceof Applicant);
            
            // Get project to work with
            List<Project> projects = projectController.getVisibleProjects((Applicant)applicant);
            assertFalse("Should have projects available for testing", projects.isEmpty());
            
            Project project = projects.get(0);
            String projectName = project.getProjectName();
            String flatType = "2-Room";
            
            // Apply for project
            boolean applyResult = applicationController.applyForProject(
                (Applicant)applicant, projectName, flatType);
            
            assertTrue("Applicant should be able to apply for project", applyResult);
            
            // Get application
            Application application = applicationController.getApplication((Applicant)applicant);
            assertNotNull("Should be able to retrieve application", application);
            
            // Manager approves application
            boolean approveResult = applicationController.approveApplication(
                (HDBManager)manager, application.getApplicationId());
            
            assertTrue("Manager should be able to approve application", approveResult);
            
            // Verify application status updated
            Application updatedApplication = applicationController.getApplication((Applicant)applicant);
            assertNotNull("Should be able to retrieve updated application", updatedApplication);
            assertEquals("Application status should be updated to Successful", "Successful", updatedApplication.getStatus());
            
            if (updatedApplication != null && updatedApplication.getStatus().equalsIgnoreCase("Successful")) {
                printPass(testName, "Application status correctly updated to Successful after approval");
            } else {
                printFail(testName, "Application status not correctly updated after approval");
            }
            
            // Applicant requests withdrawal
            boolean withdrawalRequest = applicationController.requestWithdrawal((Applicant)applicant);
            
            assertTrue("Applicant should be able to request withdrawal", withdrawalRequest);
            
            // Manager approves withdrawal
            boolean approveWithdrawal = applicationController.approveWithdrawal(
                (HDBManager)manager, updatedApplication.getApplicationId());
            
            assertTrue("Manager should be able to approve withdrawal", approveWithdrawal);
            
            // Verify application is withdrawn
            Application afterWithdrawal = applicationController.getApplication((Applicant)applicant);
            assertNull("Application should be withdrawn after manager approval", afterWithdrawal);
            
            if (afterWithdrawal == null) {
                printPass(testName, "Application successfully withdrawn after manager approval");
            } else {
                printFail(testName, "Application not properly withdrawn after manager approval");
                
                // Clean up
                applicationController.withdrawApplication((Applicant)applicant);
            }
            
        } catch (Exception e) {
            fail("Exception occurred: " + e.getMessage());
            printFail(testName, "Exception occurred: " + e.getMessage());
        }
    }
    
    /**
     * Test Case 23: Generate and Filter Reports
     * Expected: Accurate report generation with options to filter by various categories
     */
    @Test
    public void testCase23_GenerateAndFilterReports() {
        String testName = "Generate and Filter Reports";
        System.out.println("\nTest Case 23: " + testName);
        
        try {
            // Setup test scenario with approved and booked applications
            setupTestApplications();
            
            // Login as manager
            User manager = authController.login("S5678901G", "password");
            assertNotNull("Should be able to log in as HDB Manager", manager);
            assertTrue("Should get a HDBManager instance", manager instanceof HDBManager);
            
            // Generate general report of all bookings
            Report generalReport = applicationController.generateBookingReport((HDBManager)manager, null);
            
            assertNotNull("Should be able to generate general booking report", generalReport);
            assertFalse("General report should contain bookings", generalReport.getBookings().isEmpty());
            
            if (generalReport != null && !generalReport.getBookings().isEmpty()) {
                printPass(testName, "Successfully generated general booking report");
            } else {
                printFail(testName, "Could not generate general booking report");
                cleanupTestApplications();
                return;
            }
            
            // Generate filtered report by marital status - single
            Report singleReport = applicationController.generateBookingReport(
                (HDBManager)manager, Map.of("maritalStatus", "Single"));
            
            assertNotNull("Should be able to generate filtered report by marital status", singleReport);
            assertFalse("Filtered report should contain bookings", singleReport.getBookings().isEmpty());
            
            boolean onlySingles = singleReport.getBookings().stream()
                .allMatch(b -> b.getApplicantMaritalStatus().equalsIgnoreCase("Single"));
            
            assertTrue("Report should only contain singles", onlySingles);
            
            if (onlySingles) {
                printPass(testName, "Successfully filtered report by Single marital status");
            } else {
                printFail(testName, "Report filter by marital status not working correctly");
            }
            
            // Generate filtered report by flat type
            Report flatTypeReport = applicationController.generateBookingReport(
                (HDBManager)manager, Map.of("flatType", "2-Room"));
            
            assertNotNull("Should be able to generate filtered report by flat type", flatTypeReport);
            assertFalse("Filtered report should contain bookings", flatTypeReport.getBookings().isEmpty());
            
            boolean only2Room = flatTypeReport.getBookings().stream()
                .allMatch(b -> b.getFlatType().equals("2-Room"));
            
            assertTrue("Report should only contain 2-Room flats", only2Room);
            
            if (only2Room) {
                printPass(testName, "Successfully filtered report by flat type");
            } else {
                printFail(testName, "Report filter by flat type not working correctly");
            }
            
            // Clean up test scenario
            cleanupTestApplications();
            
        } catch (Exception e) {
            fail("Exception occurred: " + e.getMessage());
            printFail(testName, "Exception occurred: " + e.getMessage());
            cleanupTestApplications();
        }
    }
    
    /**
     * Setup test applications for report testing
     */
    private void setupTestApplications() {
        try {
            // Create applications with different scenarios for testing reports
            // This would involve creating and approving applications, then booking flats
            // Implementation depends on your system's specific methods
            
            // Login as needed users
            User manager = authController.login("S5678901G", "password");
            User officer = authController.login("T2109876H", "password");
            
            // Set up for single applicant
            User singleApplicant = authController.login("S1234567A", "password");
            Project project = projectController.getVisibleProjects((Applicant)singleApplicant).get(0);
            
            // Apply, approve, and book for single applicant
            applicationController.applyForProject((Applicant)singleApplicant, project.getProjectName(), "2-Room");
            Application singleApp = applicationController.getApplication((Applicant)singleApplicant);
            applicationController.approveApplication((HDBManager)manager, singleApp.getApplicationId());
            applicationController.bookFlat((HDBOfficer)officer, ((Applicant)singleApplicant).getNRIC(), 
                project.getProjectName(), "2-Room");
            
            // Set up for married applicant
            User marriedApplicant = authController.login("T2345678D", "password");
            
            // Apply, approve, and book for married applicant
            applicationController.applyForProject((Applicant)marriedApplicant, project.getProjectName(), "3-Room");
            Application marriedApp = applicationController.getApplication((Applicant)marriedApplicant);
            applicationController.approveApplication((HDBManager)manager, marriedApp.getApplicationId());
            applicationController.bookFlat((HDBOfficer)officer, ((Applicant)marriedApplicant).getNRIC(), 
                project.getProjectName(), "3-Room");
            
        } catch (Exception e) {
            System.out.println("Error setting up test applications: " + e.getMessage());
        }
    }
    
    /**
     * Clean up test applications
     */
    private void cleanupTestApplications() {
        try {
            // Clean up all test applications
            User singleApplicant = authController.login("S1234567A", "password");
            User marriedApplicant = authController.login("T2345678D", "password");
            
            applicationController.withdrawApplication((Applicant)singleApplicant);
            applicationController.withdrawApplication((Applicant)marriedApplicant);
            
        } catch (Exception e) {
            System.out.println("Error cleaning up test applications: " + e.getMessage());
        }
    }
}