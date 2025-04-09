package com.bto.model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/**
 * The EnquiryDataManager handles the interaction between the application and the enquiry text file.
 * It is responsible for adding, loading, updating, and deleting enquiries.
 */
public class EnquiryDataManager {
    
    private static final String filePath = "src/main/java/com/resources/EnquiryList.txt";
    //"C:\Users\luisa\OneDrive\Documents\GitHub\SC2002\BTO_App\btoapp\src\main\java\com\resources"
    
    /**
     * Load all enquiries from the text file.
     * 
     * @param users List of users to link with enquiries
     * @param projects List of projects to link with enquiries
     * @return List of Enquiry objects
     */
    public static List<Enquiry> loadEnquiries(List<User> users, List<Project> projects) {
        List<Enquiry> enquiries = new ArrayList<>();
        
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line = br.readLine(); // Skip header line
            
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\t");
                if (parts.length >= 3) {
                    String userID = parts[0];
                    String projectName = parts[1];
                    String enquiryText = parts[2];
                    String response = parts.length > 3 ? parts[3] : null;
                    
                    // Find user and project
                    User user = null;
                    for (User u : users) {
                        if (u.getUserID().equals(userID)) {
                            user = u;
                            break;
                        }
                    }
                    
                    Project project = null;
                    for (Project p : projects) {
                        if (p.getProjectName().equals(projectName)) {
                            project = p;
                            break;
                        }
                    }
                    
                    // Create enquiry if user and project are found
                    if (user != null && project != null) {
                        Enquiry enquiry = new Enquiry(user, project, enquiryText);
                        if (response != null && !response.isEmpty()) {
                            enquiry.addResponse(response);
                        }
                        
                        enquiries.add(enquiry);
                        
                        // If user is an applicant, add to their enquiries list
                        if (user instanceof Applicant) {
                            ((Applicant) user).addEnquiry(enquiry);
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading enquiries: " + e.getMessage());
        }
        
        return enquiries;
    }
    
    /**
     * Add a new enquiry to the text file.
     * 
     * @param enquiry The enquiry to add
     */
    public static void addEnquiry(Enquiry enquiry) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath, true))) {
            StringBuilder line = new StringBuilder();
            
            line.append(enquiry.getUser().getUserID()).append("\t");
            line.append(enquiry.getProject().getProjectName()).append("\t");
            line.append(enquiry.getEnquiryText());
            
            // Add response if available
            if (enquiry.getResponse() != null && !enquiry.getResponse().isEmpty()) {
                line.append("\t").append(enquiry.getResponse());
            }
            
            bw.write(line.toString());
            bw.newLine();
        } catch (IOException e) {
            System.err.println("Error adding enquiry to file: " + e.getMessage());
        }
    }
    
    /**
     * Update an enquiry's information in the text file.
     * 
     * @param updatedEnquiry The enquiry with updated information
     */
    public static void updateEnquiry(Enquiry updatedEnquiry) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath));
             BufferedWriter bw = new BufferedWriter(new FileWriter("temp.txt"))) {
            
            String line;
            String header = br.readLine(); // Read header
            bw.write(header); // Write header to temp file
            bw.newLine();
            
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\t");
                if (parts.length >= 3 && parts[0].equals(updatedEnquiry.getUser().getUserID()) && 
                    parts[1].equals(updatedEnquiry.getProject().getProjectName()) &&
                    parts[2].equals(updatedEnquiry.getEnquiryText())) { 
                    
                    // Found the enquiry to update, write updated information
                    StringBuilder updatedLine = new StringBuilder();
                    
                    updatedLine.append(updatedEnquiry.getUser().getUserID()).append("\t");
                    updatedLine.append(updatedEnquiry.getProject().getProjectName()).append("\t");
                    updatedLine.append(updatedEnquiry.getEnquiryText());
                    
                    // Add response if available
                    if (updatedEnquiry.getResponse() != null && !updatedEnquiry.getResponse().isEmpty()) {
                        updatedLine.append("\t").append(updatedEnquiry.getResponse());
                    }
                    
                    bw.write(updatedLine.toString());
                } else {
                    // Not the enquiry to update, write original line
                    bw.write(line);
                }
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error updating enquiry in file: " + e.getMessage());
            return;
        }
        
        // Replace original file with updated temp file
        File originalFile = new File(filePath);
        File tempFile = new File("temp.txt");
        
        Path originalPath = Paths.get(originalFile.getPath());
        Path tempPath = Paths.get(tempFile.getPath());
        
        try {
            Files.move(tempPath, originalPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            System.err.println("Error replacing original file with updated data: " + e.getMessage());
        }
    }
    
    /**
     * Delete an enquiry from the text file.
     * 
     * @param enquiry The enquiry to delete
     */
    public static void deleteEnquiry(Enquiry enquiry) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath));
             BufferedWriter bw = new BufferedWriter(new FileWriter("temp.txt"))) {
            
            String line;
            String header = br.readLine(); // Read header
            bw.write(header); // Write header to temp file
            bw.newLine();
            
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\t");
                if (parts.length >= 3 && parts[0].equals(enquiry.getUser().getUserID()) && 
                    parts[1].equals(enquiry.getProject().getProjectName()) &&
                    parts[2].equals(enquiry.getEnquiryText())) {
                    // Skip this line (the enquiry to remove)
                    continue;
                }
                // Write all other lines
                bw.write(line);
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error deleting enquiry from file: " + e.getMessage());
            return;
        }
        
        // Replace original file with updated temp file
        File originalFile = new File(filePath);
        File tempFile = new File("temp.txt");
        
        Path originalPath = Paths.get(originalFile.getPath());
        Path tempPath = Paths.get(tempFile.getPath());
        
        try {
            Files.move(tempPath, originalPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            System.err.println("Error replacing original file with updated data: " + e.getMessage());
        }
    }
    
    /**
     * Add a response to an enquiry.
     * 
     * @param enquiry The enquiry to respond to
     * @param response The response text
     */
    public static void addResponse(Enquiry enquiry, String response) {
        enquiry.addResponse(response);
        updateEnquiry(enquiry);
    }
    
    /**
     * Edit an enquiry's text.
     * 
     * @param enquiry The enquiry to edit
     * @param newText The new enquiry text
     */
    public static void editEnquiryText(Enquiry enquiry, String newText) {
        enquiry.updateEnquiry(newText);
        updateEnquiry(enquiry);
    }
    
    /**
     * Get all enquiries for a specific user.
     * 
     * @param userID The ID of the user
     * @return List of enquiries for the user
     */
    public static List<Enquiry> getEnquiriesForUser(String userID) {
        List<Enquiry> userEnquiries = new ArrayList<>();
        
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            br.readLine(); // Skip header
            
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\t");
                if (parts.length >= 3 && parts[0].equals(userID)) {
                    // Would need to convert to Enquiry objects in real implementation
                    // For now, just collecting the lines
                    userEnquiries.add(new Enquiry(null, null, parts[2])); // Placeholder
                }
            }
        } catch (IOException e) {
            System.err.println("Error getting enquiries for user: " + e.getMessage());
        }
        
        return userEnquiries;
    }
    
    /**
     * Get all enquiries for a specific project.
     * 
     * @param projectName The name of the project
     * @return List of enquiries for the project
     */
    public static List<Enquiry> getEnquiriesForProject(String projectName) {
        List<Enquiry> projectEnquiries = new ArrayList<>();
        
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            br.readLine(); // Skip header
            
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\t");
                if (parts.length >= 3 && parts[1].equals(projectName)) {
                    // Would need to convert to Enquiry objects in real implementation
                    // For now, just collecting the lines
                    projectEnquiries.add(new Enquiry(null, null, parts[2])); // Placeholder
                }
            }
        } catch (IOException e) {
            System.err.println("Error getting enquiries for project: " + e.getMessage());
        }
        
        return projectEnquiries;
    }
}