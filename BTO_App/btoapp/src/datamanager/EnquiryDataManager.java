package datamanager;

import enquiry.Enquiry;
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
import model.Applicant;
import model.Project;
import utils.FilePathConfig;

/**
* Data manager for handling enquiry-related operations in the BTO Management System.
* 
* Manages the lifecycle of enquiries including:
* - Loading enquiries from file
* - Saving enquiries to file
* - Adding, updating, and deleting enquiries
* - Retrieving enquiries by various criteria
* 
* Integrates with applicant and project data managers to maintain 
* relationships between enquiries, applicants, and projects.
* 
* @author Your Name
* @version 1.0
*/
public class EnquiryDataManager extends DataManager {
    
    private static final String DELIMITER = "\t";
    private Map<String, Enquiry> enquiryMap;
    private List<Applicant> applicantsList;
    private List<Project> projectsList;
    private String filePath;
    
    /**
    * Constructor for EnquiryDataManager.
    * 
    * Initializes the data manager with:
    * - A new HashMap to store enquiries
    * - Lists of applicants and projects for flexible lookup
    * - Configured file path for enquiry list storage
    * 
    * Converts input maps to lists for more flexible applicant and project reference.
    * 
    * @param applicantsMap Map of applicants with NRIC as key
    * @param projectsMap Map of projects with project name as key
    */
    public EnquiryDataManager(Map<String, Applicant> applicantsMap, Map<String, Project> projectsMap) {
        this.enquiryMap = new HashMap<>();
        this.applicantsList = new ArrayList<>();
        this.projectsList = new ArrayList<>();
        this.filePath = FilePathConfig.ENQUIRY_LIST_PATH;
        
        // Convert maps to lists for more flexible lookup
        if (applicantsMap != null) {
            this.applicantsList.addAll(applicantsMap.values());
        }
        
        if (projectsMap != null) {
            this.projectsList.addAll(projectsMap.values());
        }

    }

    /**
    * Loads enquiries from the configured file path.
    * 
    * Reads enquiry data from a tab-delimited file, parsing each line
    * into an Enquiry object. Handles potential parsing errors 
    * and associates enquiries with applicants and projects.
    * 
    * @return List of loaded Enquiry objects
    */
    public List<Enquiry> loadEnquiries() {
        enquiryMap.clear();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            
            // Skip header line
            String header = reader.readLine();
            if (header != null) {
                // Remove any BOM characters
                if (header.startsWith("\uFEFF")) {
                    header = header.substring(1);
                }
            }
            
            int lineCount = 0;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                
                lineCount++;
                
                try {
                    String[] parts;
                    if (line.contains("\t")) {
                        parts = line.split("\t");
                    } else {
                        parts = line.split("\\s{2,}");
                    }
                    
                    Enquiry enquiry = parseEnquiryFromLine(parts);
                    if (enquiry != null) {
                        enquiryMap.put(enquiry.getEnquiryId(), enquiry);
                    } else {
                        System.out.println("Failed to parse enquiry from line");
                    }
                } catch (Exception e) {
                    System.out.println("Error parsing enquiry: " + e.getMessage());
                    e.printStackTrace();
                }
            }
    
            return new ArrayList<>(enquiryMap.values());
        } catch (IOException e) {
            System.out.println("Error loading enquiry data: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
    * Parses a single line of enquiry data into an Enquiry object.
    * 
    * Extracts information such as enquiry ID, applicant, project, 
    * text, timestamp, and status from a parsed line.
    * 
    * @param parts Array of string parts from a parsed line
    * @return Parsed Enquiry object, or null if parsing fails
    */
    private Enquiry parseEnquiryFromLine(String[] parts) {
        if (parts.length < 6) {
            System.out.println("Invalid enquiry data format (not enough fields): " + parts.length);
            return null;
        }
        
        try {
            String enquiryId = parts[0].trim();
            String applicantNric = parts[1].trim();
            String projectName = parts[2].trim();
            String enquiryText = parts[3].trim();
            String timestampStr = parts[4].trim();
            String status = parts[5].trim();
            
            // List-based lookup instead of map lookup
            Applicant applicant = findApplicantInList(applicantNric);
            if (applicant == null) {
                System.out.println("Applicant not found for: " + applicantNric);
                return null;
            }
            
            // Find project (optional)
            Project project = null;
            if (!projectName.isEmpty()) {
                project = findProjectInList(projectName);
                if (project == null) {
                    System.out.println("Project not found: " + projectName);
                }
            }
            
            // Parse timestamp
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            Date submissionDate;
            try {
                submissionDate = dateFormat.parse(timestampStr);
            } catch (ParseException e) {
                System.out.println("Error parsing date: " + e.getMessage());
                submissionDate = new Date();
            }
            
            // Create enquiry
            Enquiry enquiry = new Enquiry(enquiryId, applicant, project, enquiryText, submissionDate);
            
            // Add reply if available
            if (parts.length > 7 && !parts[6].trim().isEmpty()) {
                String responder = parts[6].trim();
                String reply = parts[7].trim();
                enquiry.setReply(reply);
            }
            
            // Add enquiry to applicant
            applicant.addEnquiry(enquiry);
            
            // Add enquiry to project if available
            if (project != null) {
                project.addEnquiry(enquiry);
            }
            
            return enquiry;
        } catch (Exception e) {
            System.out.println("Error in parseEnquiryFromLine: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
    * Finds an Applicant by NRIC using list-based lookup.
    * 
    * Supports multiple matching strategies including:
    * - Exact match
    * - Trimmed match
    * - Case-insensitive match
    * 
    * @param nric NRIC to search for
    * @return Matching Applicant, or null if not found
    */
    private Applicant findApplicantInList(String nric) {
        if (nric == null) return null;
        
        String normalizedNric = nric.trim();
        
        for (Applicant app : applicantsList) {
            // Try multiple matching strategies
            String appNric = app.getNric();
            if (appNric == null) continue;
            
            // Case 1: Direct comparison
            if (appNric.equals(normalizedNric)) {
                return app;
            }
            
            // Case 2: Trimmed comparison
            if (appNric.trim().equals(normalizedNric)) {
                return app;
            }
            
            // Case 3: Case-insensitive comparison
            if (appNric.trim().equalsIgnoreCase(normalizedNric)) {
                return app;
            }
        }
        
        System.out.println("No applicant found with NRIC: " + normalizedNric);
        return null;
    }

    /**
    * Generates a comma-separated string of available NRIC numbers.
    * 
    * @return A string containing all NRIC numbers enclosed in single quotes
    */
    private String getAvailableNRICs() {
        StringBuilder sb = new StringBuilder();
        for (Applicant app : applicantsList) {
            if (sb.length() > 0) sb.append(", ");
            sb.append("'").append(app.getNric()).append("'");
        }
        return sb.toString();
    }
    
    /**
    * Finds a Project by name using list-based lookup.
    * 
    * Supports multiple matching strategies including:
    * - Exact match
    * - Trimmed match
    * - Case-insensitive match
    * 
    * @param name Project name to search for
    * @return Matching Project, or null if not found
    */
    private Project findProjectInList(String name) {
        if (name == null) return null;
        
        String normalizedName = name.trim();
        for (Project proj : projectsList) {
            if (proj.getProjectName() == null) continue;
            
            if (proj.getProjectName().equals(normalizedName) || 
                proj.getProjectName().trim().equals(normalizedName) ||
                proj.getProjectName().trim().equalsIgnoreCase(normalizedName)) {
                return proj;
            }
        }
        return null;
    }
    
    /**
    * Saves a list of enquiries to the configured file path.
    * 
    * Writes enquiry data to a tab-delimited file, including:
    * - Enquiry ID
    * - Applicant NRIC
    * - Project name
    * - Enquiry text
    * - Timestamp
    * - Status
    * - Responder and reply (if applicable)
    * 
    * @param enquiries List of Enquiry objects to save
    * @return true if save is successful, false otherwise
    */
    public boolean saveEnquiries(List<Enquiry> enquiries) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            // Write header
            writer.write("Enquiry ID\tApplicant NRIC\tProject Name\tEnquiry Text\tTimestamp\tStatus\tResponder\tReply");
            writer.newLine();
            
            // Write enquiry data
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            
            for (Enquiry enquiry : enquiries) {
                writer.write(enquiry.getEnquiryId() + DELIMITER);
                writer.write(enquiry.getApplicant().getNric() + DELIMITER);
                
                // Project name (may be null)
                if (enquiry.getProject() != null) {
                    writer.write(enquiry.getProject().getProjectName());
                }
                writer.write(DELIMITER);
                
                // Enquiry text
                writer.write(enquiry.getEnquiryText() + DELIMITER);
                
                // Timestamp
                writer.write(dateFormat.format(enquiry.getSubmissionDate()) + DELIMITER);
                
                // Status
                writer.write((enquiry.isAnswered() ? "Responded" : "Open") + DELIMITER);
                
                // Responder and Reply (if any)
                if (enquiry.isAnswered()) {
                    writer.write("System" + DELIMITER); // Default responder
                    writer.write(enquiry.getReply());
                }
                
                writer.newLine();
            }
            
            return true;
        } catch (IOException e) {
            System.out.println("Error saving enquiry data: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
    * Adds a new enquiry to the data manager.
    * 
    * Associates the enquiry with its applicant and project (if applicable),
    * adds it to the internal map, and saves to file.
    * 
    * @param enquiry Enquiry to add
    * @return true if addition is successful, false otherwise
    */
    public boolean addEnquiry(Enquiry enquiry) {
        if (enquiry == null || enquiry.getEnquiryId() == null) {
            System.out.println("Cannot add null enquiry or enquiry with null ID");
            return false;
        }
        
        enquiryMap.put(enquiry.getEnquiryId(), enquiry);
        
        // Ensure enquiry is associated with applicant
        enquiry.getApplicant().addEnquiry(enquiry);
        
        // Ensure enquiry is associated with project if applicable
        if (enquiry.getProject() != null) {
            enquiry.getProject().addEnquiry(enquiry);
        }
        
        // Save to file
        return saveEnquiries(getAllEnquiries());
    }
    
    /**
    * Updates an existing enquiry in the data manager.
    * 
    * Replaces the existing enquiry in the internal map and saves to file.
    * 
    * @param enquiry Enquiry to update
    * @return true if update is successful, false otherwise
    */
    public boolean updateEnquiry(Enquiry enquiry) {
        if (enquiry == null || enquiry.getEnquiryId() == null) {
            return false;
        }
        
        if (!enquiryMap.containsKey(enquiry.getEnquiryId())) {
            return false;
        }
        
        enquiryMap.put(enquiry.getEnquiryId(), enquiry);
        
        // Save to file
        return saveEnquiries(getAllEnquiries());
    }
    
    /**
    * Deletes an enquiry from the data manager.
    * 
    * Removes the enquiry from:
    * - Internal enquiry map
    * - Applicant's enquiry list
    * - Project's enquiry list (if applicable)
    * 
    * @param enquiryId ID of the enquiry to delete
    * @return true if deletion is successful, false otherwise
    */
    public boolean deleteEnquiry(String enquiryId) {
        if (enquiryId == null || !enquiryMap.containsKey(enquiryId)) {
            return false;
        }
        
        Enquiry enquiry = enquiryMap.get(enquiryId);
        
        // Remove from applicant
        enquiry.getApplicant().removeEnquiry(enquiry);
        
        // Remove from project if applicable
        if (enquiry.getProject() != null) {
            enquiry.getProject().removeEnquiry(enquiry);
        }
        
        // Remove from map
        enquiryMap.remove(enquiryId);
        
        // Save to file
        return saveEnquiries(getAllEnquiries());
    }
    
    /**
    * Retrieves an enquiry by its unique ID.
    * 
    * @param enquiryId ID of the enquiry to retrieve
    * @return Enquiry object, or null if not found
    */
    public Enquiry getEnquiryById(String enquiryId) {
        return enquiryMap.get(enquiryId);
    }
    
    /**
    * Retrieves all enquiries in the data manager.
    * 
    * @return List of all Enquiry objects
    */
    public List<Enquiry> getAllEnquiries() {
        return new ArrayList<>(enquiryMap.values());
    }
    
    /**
    * Retrieves all enquiries for a specific applicant.
    * 
    * @param applicantNric NRIC of the applicant
    * @return List of Enquiry objects for the specified applicant
    */
    public List<Enquiry> getEnquiriesByApplicant(String applicantNric) {
        List<Enquiry> result = new ArrayList<>();
        
        for (Enquiry enquiry : enquiryMap.values()) {
            String enquiryApplicantNric = enquiry.getApplicant().getNric();
            
            if (enquiryApplicantNric.equals(applicantNric)) {
                result.add(enquiry);
            }
        }
        return result;
    }
}