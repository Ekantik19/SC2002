package com.bto.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

import com.bto.model.Applicant;
import com.bto.model.HDBOfficer;
import com.bto.model.Project;


//8 and 9
public class ApplicantTests extends BaseTest {

    @Test
    public void testSingleFlatBookingRestriction() {
        // Retrieve a married applicant (e.g., Sarah)
        Applicant applicant = (Applicant) dataManager.authenticate("T7654321B", "password");
        assertNotNull("Applicant should be found", applicant);
        
        // Get the project
        Project project = projectController.getProjectByName("Acacia Breeze");
        assertNotNull("Project should be found", project);
        
        // First, apply for the project and get it approved
        boolean applyResult = applicationController.applyForProject(applicant, "Acacia Breeze", "3-Room");
        assertTrue("Project application should be successful", applyResult);
        
        // Simulate HDB Officer booking the first flat
        HDBOfficer officer = (HDBOfficer) dataManager.authenticate("Daniel", "password");
        assertNotNull("Officer should be found", officer);
        
        boolean firstBookingResult = applicationController.bookFlat(officer, applicant.getUserID(), "Acacia Breeze", "3-Room");
        assertTrue("First flat booking should be successful", firstBookingResult);
        
        // Attempt a second booking (should fail)
        boolean secondBookingResult = applicationController.bookFlat(officer, applicant.getUserID(), "Acacia Breeze", "2-Room");
        assertFalse("Second flat booking should be prevented", secondBookingResult);
    }

    @Test
    public void testEnquirySubmission() {
        // Retrieve an applicant
        Applicant applicant = (Applicant) dataManager.authenticate("S1234567A", "password");
        assertNotNull("Applicant should be found", applicant);
        
        // Get the project
        Project project = projectController.getProjectByName("Acacia Breeze");
        assertNotNull("Project should be found", project);
        
        // Submit an enquiry
        int submitResult = enquiryController.submitEnquiry(applicant, project, "What are the payment terms?");
        assertEquals("Enquiry submission should be successful", 1, submitResult);
    }

    @Test
    public void testEnquiryEditing() {
        // Retrieve an applicant
        Applicant applicant = (Applicant) dataManager.authenticate("S1234567A", "password");
        assertNotNull("Applicant should be found", applicant);
        
        // Get the project
        Project project = projectController.getProjectByName("Acacia Breeze");
        assertNotNull("Project should be found", project);
        
        // First, submit an enquiry
        enquiryController.submitEnquiry(applicant, project, "Initial enquiry text");
        
        // Edit the enquiry
        boolean editResult = enquiryController.editEnquiry(
            applicant, 
            "Acacia Breeze", 
            "Initial enquiry text", 
            "Updated enquiry text"
        );
        assertTrue("Enquiry editing should be successful", editResult);
    }

    @Test
    public void testEnquiryDeletion() {
        // Retrieve an applicant
        Applicant applicant = (Applicant) dataManager.authenticate("S1234567A", "password");
        assertNotNull("Applicant should be found", applicant);
        
        // Get the project
        Project project = projectController.getProjectByName("Acacia Breeze");
        assertNotNull("Project should be found", project);
        
        // First, submit an enquiry
        enquiryController.submitEnquiry(applicant, project, "Enquiry to be deleted");
        
        // Delete the enquiry
        boolean deleteResult = enquiryController.deleteEnquiry(
            applicant, 
            "Acacia Breeze", 
            "Enquiry to be deleted"
        );
        assertTrue("Enquiry deletion should be successful", deleteResult);
    }
}