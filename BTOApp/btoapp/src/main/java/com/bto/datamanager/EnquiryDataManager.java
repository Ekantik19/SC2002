package com.bto.datamanager;

import com.bto.enquiry.Enquiry;
import com.bto.model.Applicant;
import com.bto.model.Project;
import com.bto.controller.ProjectController;
import com.bto.controller.AuthenticationController;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Manages data persistence for Enquiries using a text file.
 * Follows Single Responsibility Principle by handling only file I/O operations.
 */
public class EnquiryDataManager {
    private static final String ENQUIRY_FILE_PATH = "EnquiryList.txt";
    private static final String DELIMITER = "\t";
    
    private ProjectController projectController;
    private AuthenticationController authController;

    /**
     * Constructor with dependencies for resolving project and applicant references.
     * 
     * @param projectController Used to retrieve project information
     * @param authController Used to retrieve applicant information
     */
    public EnquiryDataManager(ProjectController projectController, 
                               AuthenticationController authController) {
        this.projectController = projectController;
        this.authController = authController;
    }

    /**
     * Loads enquiries from the text file.
     * 
     * @return List of Enquiry objects loaded from the file
     */
    public List<Enquiry> loadEnquiries() {
        List<Enquiry> enquiries = new ArrayList<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");

        try (BufferedReader reader = new BufferedReader(new FileReader(ENQUIRY_FILE_PATH))) {
            // Skip header line
            String line = reader.readLine();
            
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(DELIMITER, -1);
                
                // Ensure we have enough fields
                if (fields.length < 6) continue;
                
                String enquiryId = fields[0].trim();
                String applicantNric = fields[1].trim();
                String projectName = fields.length > 2 ? fields[2].trim() : "";
                String enquiryText = fields.length > 3 ? fields[3].trim() : "";
                
                // Parse timestamp
                Date submissionDate = null;
                try {
                    submissionDate = dateFormat.parse(fields[4].trim());
                } catch (ParseException e) {
                    System.err.println("Could not parse date for enquiry: " + enquiryId);
                    continue;
                }
                
                String status = fields.length > 5 ? fields[5].trim() : "";
                
                // Retrieve applicant and project
                Applicant applicant = (Applicant) authController.getUserByNRIC(applicantNric);
                Project project = projectName.isEmpty() ? null : 
                    projectController.getProjectByName(projectName);
                
                if (applicant == null) {
                    System.err.println("Could not find applicant with NRIC: " + applicantNric);
                    continue;
                }
                
                // Create Enquiry object
                Enquiry enquiry = new Enquiry(enquiryId, applicant, project, enquiryText, submissionDate);
                
                // Set reply if available in later fields
                if (fields.length > 7) {
                    String responder = fields[6].trim();
                    String replyText = fields[7].trim();
                    if (!replyText.isEmpty()) {
                        enquiry.setReply(replyText);
                    }
                }
                
                enquiries.add(enquiry);
            }
        } catch (IOException e) {
            System.err.println("Error reading enquiry file: " + e.getMessage());
        }
        
        return enquiries;
    }

    /**
     * Saves the list of enquiries to the text file.
     * 
     * @param enquiries List of Enquiry objects to save
     * @return true if save was successful, false otherwise
     */
    public boolean saveEnquiries(List<Enquiry> enquiries) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(ENQUIRY_FILE_PATH))) {
            // Write header
            writer.write("Enquiry ID\tApplicant NRIC\tProject Name\tEnquiry Text\tTimestamp\tStatus\tResponder\tReply");
            writer.newLine();
            
            // Write each enquiry
            for (Enquiry enquiry : enquiries) {
                String projectName = enquiry.getProject() != null ? 
                    enquiry.getProject().getProjectName() : "";
                
                String responder = "";
                String reply = "";
                if (enquiry.isAnswered()) {
                    reply = enquiry.getReply();
                    // In a real system, you might want to track who responded
                    responder = "System"; // or retrieve from a more sophisticated tracking method
                }
                
                String line = String.format("%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s", 
                    enquiry.getEnquiryId(),
                    enquiry.getApplicant().getNric(),
                    projectName,
                    enquiry.getEnquiryText(),
                    dateFormat.format(enquiry.getSubmissionDate()),
                    enquiry.isAnswered() ? "Responded" : "Open",
                    responder,
                    reply
                );
                
                writer.write(line);
                writer.newLine();
            }
            
            return true;
        } catch (IOException e) {
            System.err.println("Error writing enquiry file: " + e.getMessage());
            return false;
        }
    }

    /**
     * Adds a single enquiry to the existing file.
     * 
     * @param enquiry Enquiry to add
     * @return true if addition was successful, false otherwise
     */
    public boolean addEnquiry(Enquiry enquiry) {
        List<Enquiry> existingEnquiries = loadEnquiries();
        existingEnquiries.add(enquiry);
        return saveEnquiries(existingEnquiries);
    }

    /**
     * Updates an existing enquiry in the file.
     * 
     * @param updatedEnquiry Updated Enquiry object
     * @return true if update was successful, false otherwise
     */
    public boolean updateEnquiry(Enquiry updatedEnquiry) {
        List<Enquiry> existingEnquiries = loadEnquiries();
        
        for (int i = 0; i < existingEnquiries.size(); i++) {
            if (existingEnquiries.get(i).getEnquiryId().equals(updatedEnquiry.getEnquiryId())) {
                existingEnquiries.set(i, updatedEnquiry);
                return saveEnquiries(existingEnquiries);
            }
        }
        
        return false;
    }

    /**
     * Deletes an enquiry from the file.
     * 
     * @param enquiryId ID of the enquiry to delete
     * @return true if deletion was successful, false otherwise
     */
    public boolean deleteEnquiry(String enquiryId) {
        List<Enquiry> existingEnquiries = loadEnquiries();
        
        boolean removed = existingEnquiries.removeIf(
            enquiry -> enquiry.getEnquiryId().equals(enquiryId)
        );
        
        return removed && saveEnquiries(existingEnquiries);
    }
}