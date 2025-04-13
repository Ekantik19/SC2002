package datamanager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import enquiry.Enquiry;
import model.Applicant;
import model.Project;
import utils.FilePathConfig;

/**
 * Data manager for Enquiry entities in the BTO Management System.
 * Handles loading and storing enquiry data from/to external files.
 * 
 * @author Your Name
 * @version 1.0
 */
public class EnquiryDataManager extends DataManager {
    
    private static final String DELIMITER = "\t";
    private Map<String, Enquiry> enquiryMap;
    private Map<String, Applicant> applicantsMap;
    private Map<String, Project> projectsMap;
    private String filePath;
    
    /**
     * Constructor for EnquiryDataManager.
     * 
     * @param applicantsMap Map of Applicants by NRIC
     * @param projectsMap Map of Projects by name
     */
    public EnquiryDataManager(Map<String, Applicant> applicantsMap, Map<String, Project> projectsMap) {
        this.enquiryMap = new HashMap<>();
        this.applicantsMap = applicantsMap;
        this.projectsMap = projectsMap;
        this.filePath = FilePathConfig.ENQUIRY_LIST_PATH;
    }
    
    /**
     * Gets the current file path being used.
     * 
     * @return The file path
     */
    public String getFilePath() {
        return filePath;
    }
    
    /**
     * Sets a new file path for the enquiry data file.
     * 
     * @param filePath The new file path
     */
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
    
    /**
     * Loads enquiries from file.
     * 
     * @return List of loaded enquiries
     */
    // public List<Enquiry> loadEnquiries() {
    //     System.out.println("DEBUG: Loading enquiries from: " + filePath);
    //     try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
    //         String line;
            
    //         // Skip header line
    //         reader.readLine();
            
    //         while ((line = reader.readLine()) != null && !line.trim().isEmpty()) {
    //             Enquiry enquiry = parseEnquiryFromLine(line);
    //             if (enquiry != null) {
    //                 enquiryMap.put(enquiry.getEnquiryId(), enquiry);
    //             }
    //         }
            
    //         System.out.println("DEBUG: Loaded " + enquiryMap.size() + " enquiries");
    //         return new ArrayList<>(enquiryMap.values());
    //     } catch (IOException | ParseException e) {
    //         System.out.println("Error loading enquiry data: " + e.getMessage());
    //         return new ArrayList<>();
    //     }
    // }

    public List<Enquiry> loadEnquiries() {
        System.out.println("DEBUG: Loading enquiries from: " + filePath);
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            
            // Skip header line
            String header = reader.readLine();
            System.out.println("DEBUG: Enquiry header: " + header);
            
            while ((line = reader.readLine()) != null && !line.trim().isEmpty()) {
                System.out.println("DEBUG: Processing enquiry line: " + line);
                try {
                    Enquiry enquiry = parseEnquiryFromLine(line);
                    if (enquiry != null) {
                        enquiryMap.put(enquiry.getEnquiryId(), enquiry);
                        System.out.println("DEBUG: Added enquiry: " + enquiry.getEnquiryId());
                    } else {
                        System.out.println("DEBUG: Failed to parse enquiry from line");
                    }
                } catch (Exception e) {
                    System.out.println("DEBUG: Error parsing enquiry: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            
            System.out.println("DEBUG: Loaded " + enquiryMap.size() + " enquiries");
            return new ArrayList<>(enquiryMap.values());
        } catch (IOException e) {
            System.out.println("Error loading enquiry data: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    /**
     * Parses an Enquiry from a line of text.
     * 
     * @param line The line to parse
     * @return The parsed Enquiry, or null if parsing failed
     * @throws ParseException If date parsing fails
     */
    private Enquiry parseEnquiryFromLine(String line) throws ParseException {
        String[] parts = line.split(DELIMITER);
        
        if (parts.length < 6) {
            System.out.println("Invalid enquiry data format: " + line);
            return null;
        }
        
        String enquiryId = parts[0];
        String applicantNric = parts[1];
        String projectName = parts[2];
        String enquiryText = parts[3];
        String timestampStr = parts[4];
        String status = parts[5];
        
        // Get the applicant
        Applicant applicant = applicantsMap.get(applicantNric);
        if (applicant == null) {
            System.out.println("Applicant not found: " + applicantNric);
            return null;
        }
        
        // Get the project (might be empty for general enquiries)
        Project project = null;
        if (!projectName.isEmpty()) {
            project = projectsMap.get(projectName);
            if (project == null) {
                System.out.println("Project not found: " + projectName);
                return null;
            }
        }
        
        // Parse timestamp
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        Date submissionDate = dateFormat.parse(timestampStr);
        
        // Create enquiry
        Enquiry enquiry = new Enquiry(enquiryId, applicant, project, enquiryText, submissionDate);
        
        // Add reply if available
        if (parts.length > 7 && !parts[6].isEmpty()) {
            String responder = parts[6];
            String reply = parts[7];
            
            enquiry.setReply(reply);
        }
        
        // Add enquiry to applicant and project
        applicant.addEnquiry(enquiry);
        if (project != null) {
            project.addEnquiry(enquiry);
        }
        
        return enquiry;
    }
    
    /**
     * Saves all enquiries to file.
     * 
     * @param enquiries List of enquiries to save
     * @return true if saving was successful, false otherwise
     */
    public boolean saveEnquiries(List<Enquiry> enquiries) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FilePathConfig.ENQUIRY_LIST_PATH))) {
            // Write header
            writer.write("Enquiry ID\tApplicant NRIC\tProject Name\tEnquiry Text\tTimestamp\tStatus\tResponder\tReply");
            writer.newLine();
            
            // Write enquiries
            for (Enquiry enquiry : enquiries) {
                StringBuilder sb = new StringBuilder();
                
                // Basic info
                sb.append(enquiry.getEnquiryId()).append(DELIMITER);
                sb.append(enquiry.getApplicant().getNric()).append(DELIMITER);
                
                // Project name (might be null for general enquiries)
                if (enquiry.getProject() != null) {
                    sb.append(enquiry.getProject().getProjectName());
                }
                sb.append(DELIMITER);
                
                // Question and timestamp
                sb.append(enquiry.getEnquiryText()).append(DELIMITER);
                
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                sb.append(dateFormat.format(enquiry.getSubmissionDate())).append(DELIMITER);
                
                // Status
                sb.append(enquiry.isAnswered() ? "Responded" : "Open").append(DELIMITER);
                
                // Reply information
                if (enquiry.isAnswered()) {
                    // In a real system, we would store the responder's NRIC
                    // For simplicity, we're using a placeholder
                    sb.append("Officer").append(DELIMITER);
                    sb.append(enquiry.getReply());
                } else {
                    sb.append(DELIMITER); // Empty responder
                    sb.append(""); // Empty reply
                }
                
                writer.write(sb.toString());
                writer.newLine();
            }
            
            return true;
        } catch (IOException e) {
            System.out.println("Error saving enquiry data: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Adds an enquiry to the data manager.
     * 
     * @param enquiry The enquiry to add
     * @return true if addition was successful, false otherwise
     */
    public boolean addEnquiry(Enquiry enquiry) {
        if (enquiry != null) {
            enquiryMap.put(enquiry.getEnquiryId(), enquiry);
            return saveEnquiries(new ArrayList<>(enquiryMap.values()));
        }
        return false;
    }
    
    /**
     * Updates an enquiry in the data manager.
     * 
     * @param enquiry The enquiry to update
     * @return true if update was successful, false otherwise
     */
    public boolean updateEnquiry(Enquiry enquiry) {
        if (enquiry != null && enquiryMap.containsKey(enquiry.getEnquiryId())) {
            enquiryMap.put(enquiry.getEnquiryId(), enquiry);
            return saveEnquiries(new ArrayList<>(enquiryMap.values()));
        }
        return false;
    }
    
    /**
     * Deletes an enquiry from the data manager.
     * 
     * @param enquiryId The ID of the enquiry to delete
     * @return true if deletion was successful, false otherwise
     */
    public boolean deleteEnquiry(String enquiryId) {
        if (enquiryMap.containsKey(enquiryId)) {
            Enquiry enquiry = enquiryMap.get(enquiryId);
            
            // Remove from applicant and project
            enquiry.getApplicant().removeEnquiry(enquiry);
            if (enquiry.getProject() != null) {
                enquiry.getProject().removeEnquiry(enquiry);
            }
            
            // Remove from map
            enquiryMap.remove(enquiryId);
            
            return saveEnquiries(new ArrayList<>(enquiryMap.values()));
        }
        return false;
    }
    
    /**
     * Gets an enquiry by ID.
     * 
     * @param enquiryId The ID of the enquiry to get
     * @return The enquiry, or null if not found
     */
    public Enquiry getEnquiryById(String enquiryId) {
        return enquiryMap.get(enquiryId);
    }
    
    /**
     * Gets all enquiries.
     * 
     * @return A list of all enquiries
     */
    public List<Enquiry> getAllEnquiries() {
        return new ArrayList<>(enquiryMap.values());
    }
    
    /**
     * Gets all enquiries for a specific applicant.
     * 
     * @param applicantNric The NRIC of the applicant
     * @return A list of enquiries for the applicant
     */
    public List<Enquiry> getEnquiriesByApplicant(String applicantNric) {
        List<Enquiry> result = new ArrayList<>();
        
        for (Enquiry enquiry : enquiryMap.values()) {
            if (enquiry.getApplicant().getNric().equals(applicantNric)) {
                result.add(enquiry);
            }
        }
        
        return result;
    }
    
    /**
     * Gets all enquiries for a specific project.
     * 
     * @param projectName The name of the project
     * @return A list of enquiries for the project
     */
    public List<Enquiry> getEnquiriesByProject(String projectName) {
        List<Enquiry> result = new ArrayList<>();
        
        for (Enquiry enquiry : enquiryMap.values()) {
            if (enquiry.getProject() != null && 
                enquiry.getProject().getProjectName().equals(projectName)) {
                result.add(enquiry);
            }
        }
        
        return result;
    }
}