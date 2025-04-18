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

public class EnquiryDataManager extends DataManager {
    
    private static final String DELIMITER = "\t";
    private Map<String, Enquiry> enquiryMap;
    private List<Applicant> applicantsList;
    private List<Project> projectsList;
    private String filePath;
    
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
        
        // Debug - print all applicants for verification
        System.out.println("DEBUG: Applicants in EnquiryDataManager:");
        for (Applicant app : applicantsList) {
            System.out.println("DEBUG: Applicant: " + app.getName() + ", NRIC: " + app.getNric());
        }
        
        // Debug - print all projects for verification
        System.out.println("DEBUG: Projects in EnquiryDataManager:");
        for (Project proj : projectsList) {
            System.out.println("DEBUG: Project: " + (proj.getProjectName() != null ? proj.getProjectName() : "null"));
        }
    }
    
    public List<Enquiry> loadEnquiries() {
        System.out.println("DEBUG: Loading enquiries from: " + filePath);
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
                System.out.println("DEBUG: Enquiry header: " + header);
            }
            
            int lineCount = 0;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                
                lineCount++;
                System.out.println("DEBUG: Processing enquiry line " + lineCount + ": " + line);
                
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
                        System.out.println("DEBUG: Added enquiry: " + enquiry.getEnquiryId());
                    } else {
                        System.out.println("DEBUG: Failed to parse enquiry from line");
                    }
                } catch (Exception e) {
                    System.out.println("DEBUG: Error parsing enquiry: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            
            System.out.println("DEBUG: Processed " + lineCount + " lines");
            System.out.println("DEBUG: Loaded " + enquiryMap.size() + " enquiries");
            return new ArrayList<>(enquiryMap.values());
        } catch (IOException e) {
            System.out.println("Error loading enquiry data: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    private Enquiry parseEnquiryFromLine(String[] parts) {
        if (parts.length < 6) {
            System.out.println("DEBUG: Invalid enquiry data format (not enough fields): " + parts.length);
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
                System.out.println("DEBUG: Applicant not found using list lookup: " + applicantNric);
                return null;
            }
            
            // Find project (optional)
            Project project = null;
            if (!projectName.isEmpty()) {
                project = findProjectInList(projectName);
                if (project == null) {
                    System.out.println("DEBUG: Project not found: " + projectName);
                }
            }
            
            // Parse timestamp
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            Date submissionDate;
            try {
                submissionDate = dateFormat.parse(timestampStr);
            } catch (ParseException e) {
                System.out.println("DEBUG: Error parsing date: " + e.getMessage());
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
            System.out.println("DEBUG: Error in parseEnquiryFromLine: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    private Applicant findApplicantInList(String nric) {
        if (nric == null) return null;
        
        String normalizedNric = nric.trim();
        System.out.println("DEBUG: Looking for applicant with NRIC: '" + normalizedNric + "'");
        
        for (Applicant app : applicantsList) {
            // Try multiple matching strategies
            String appNric = app.getNric();
            if (appNric == null) continue;
            
            // Case 1: Direct comparison
            if (appNric.equals(normalizedNric)) {
                System.out.println("DEBUG: Found applicant (exact match): " + app.getName());
                return app;
            }
            
            // Case 2: Trimmed comparison
            if (appNric.trim().equals(normalizedNric)) {
                System.out.println("DEBUG: Found applicant (after trimming): " + app.getName());
                return app;
            }
            
            // Case 3: Case-insensitive comparison
            if (appNric.trim().equalsIgnoreCase(normalizedNric)) {
                System.out.println("DEBUG: Found applicant (case-insensitive): " + app.getName());
                return app;
            }
        }
        
        System.out.println("DEBUG: No applicant found with NRIC: " + normalizedNric);
        System.out.println("DEBUG: Available NRICs: " + getAvailableNRICs());
        return null;
    }
    
    private String getAvailableNRICs() {
        StringBuilder sb = new StringBuilder();
        for (Applicant app : applicantsList) {
            if (sb.length() > 0) sb.append(", ");
            sb.append("'").append(app.getNric()).append("'");
        }
        return sb.toString();
    }
    
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
            
            System.out.println("DEBUG: Saved " + enquiries.size() + " enquiries to file");
            return true;
        } catch (IOException e) {
            System.out.println("Error saving enquiry data: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean addEnquiry(Enquiry enquiry) {
        if (enquiry == null || enquiry.getEnquiryId() == null) {
            System.out.println("DEBUG: Cannot add null enquiry or enquiry with null ID");
            return false;
        }
        
        enquiryMap.put(enquiry.getEnquiryId(), enquiry);
        
        // Ensure enquiry is associated with applicant
        enquiry.getApplicant().addEnquiry(enquiry);
        
        // Ensure enquiry is associated with project if applicable
        if (enquiry.getProject() != null) {
            enquiry.getProject().addEnquiry(enquiry);
        }
        
        System.out.println("DEBUG: Added enquiry to map: " + enquiry.getEnquiryId());
        
        // Save to file
        return saveEnquiries(getAllEnquiries());
    }
    
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
    
    public Enquiry getEnquiryById(String enquiryId) {
        return enquiryMap.get(enquiryId);
    }
    
    public List<Enquiry> getAllEnquiries() {
        return new ArrayList<>(enquiryMap.values());
    }
    
    public List<Enquiry> getEnquiriesByApplicant(String applicantNric) {
        List<Enquiry> result = new ArrayList<>();
        
        System.out.println("DEBUG: Getting enquiries for applicant NRIC: " + applicantNric);
        
        for (Enquiry enquiry : enquiryMap.values()) {
            String enquiryApplicantNric = enquiry.getApplicant().getNric();
            System.out.println("DEBUG: Checking enquiry " + enquiry.getEnquiryId() + 
                              " with applicant NRIC: " + enquiryApplicantNric);
            
            if (enquiryApplicantNric.equals(applicantNric)) {
                result.add(enquiry);
                System.out.println("DEBUG: Added enquiry to result: " + enquiry.getEnquiryId());
            }
        }
        
        System.out.println("DEBUG: Found " + result.size() + " enquiries for applicant: " + applicantNric);
        return result;
    }
}