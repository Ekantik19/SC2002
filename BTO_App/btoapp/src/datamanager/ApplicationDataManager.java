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
import model.Applicant;
import model.Application;
import model.HDBOfficer;
import model.Project;
import model.enums.ApplicationStatus;
import model.enums.FlatType;
import utils.FilePathConfig;

/**
 * ApplicationDataManager handles the reading and writing of application data
 * to/from the application list file. It is responsible for loading applications
 * from file into the system and saving application data back to the file.
 * 
 * @author Your Name
 * @version 1.0
 */
public class ApplicationDataManager {

    private static final String DELIMITER = "\t";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");
    
    private Map<String, Application> applicationMap;
    private ApplicantDataManager applicantDataManager;
    private ProjectDataManager projectDataManager;
    private OfficerDataManager officerDataManager;
    private String filePath;
    
    /**
    * Constructor for ApplicationDataManager.
    * 
    * Initializes the data manager with:
    * - A new HashMap to store applications
    * - References to related data managers
    * - Configured file path for application list storage
    * 
    * @param applicantDataManager Manager for handling applicant-related data
    * @param projectDataManager Manager for handling project-related data
    * @param officerDataManager Manager for handling officer-related data
    */
    public ApplicationDataManager(ApplicantDataManager applicantDataManager, ProjectDataManager projectDataManager, OfficerDataManager officerDataManager) {
        this.applicationMap = new HashMap<>();
        this.applicantDataManager = applicantDataManager;
        this.projectDataManager = projectDataManager;
        this.officerDataManager = officerDataManager;
        this.filePath = FilePathConfig.APPLICATION_LIST_PATH;
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
     * Sets a new file path for the application data file.
     * 
     * @param filePath The new file path
     */
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
    
    /**
     * Loads application data from the application list file.
     * 
     * @return true if the data was successfully loaded, false otherwise
     */
    public boolean loadApplicationData() {
        applicationMap.clear();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            // Skip header line
            String headerLine = reader.readLine();
            
            String line;
            int lineNumber = 2; // Start from line 2 after header
            while ((line = reader.readLine()) != null) {
                // Skip empty lines
                if (line.trim().isEmpty()) {
                    lineNumber++;
                    continue;
                }
                
                String[] parts = line.split("\t");
                
                // Skip lines with insufficient data
                if (parts.length < 4) {
                    lineNumber++;
                    continue;
                }
                
                // Trim all parts to remove whitespace
                String applicantNric = parts[0].trim();
                String projectName = parts[1].trim();
                String statusStr = parts[2].trim();
                String flatTypeStr = parts[3].trim();
                String bookingDateStr = parts.length > 4 ? parts[4].trim() : "";
                
                // Validate and parse application status
                ApplicationStatus status;
                try {
                    status = ApplicationStatus.valueOf(statusStr.toUpperCase());
                } catch (IllegalArgumentException e) {
                    System.out.println("Invalid application status on line " + lineNumber + 
                                       ": " + statusStr);
                    lineNumber++;
                    continue;
                }
                
                // Parse flat type using custom fromString method
                FlatType flatType = FlatType.fromString(flatTypeStr);
                if (flatType == null) {
                    System.out.println("Invalid flat type on line " + lineNumber + 
                                       ": " + flatTypeStr);
                    lineNumber++;
                    continue;
                }
                
                // Get the applicant and project objects
                Applicant applicant = applicantDataManager.findApplicantByNRIC(applicantNric);
                
                // If applicant not found, try finding as an officer
                if (applicant == null) {
                    HDBOfficer officer = officerDataManager.getOfficerByNric(applicantNric);
                    if (officer != null) {
                        // Convert officer to applicant if needed
                        applicant = new Applicant(
                            officer.getName(), 
                            officer.getNric(), 
                            officer.getAge(), 
                            officer.getMaritalStatus(), 
                            officer.getPassword()
                        );
                    }
                }
                
                Project project = projectDataManager.getProjectByName(projectName);
                
                // Skip if either applicant or project is not found
                if (applicant == null || project == null) {
                    lineNumber++;
                    continue;
                }
                
                // Generate application ID
                String applicationId = generateApplicationId(applicantNric, projectName);
                
                // Create application object
                Application application = new Application(applicationId, applicant, project, flatType);
                
                // Set application status
                switch (status) {
                    case PENDING:
                        // Do nothing for pending status
                        break;
                    case SUCCESSFUL:
                        application.approve();
                        break;
                    case BOOKED:
                        application.approve();
                        application.bookFlat();
                        applicant.setBookedProject(project);
                        applicant.setBookedFlatType(flatType);
                        if (!bookingDateStr.isEmpty()) {
                            try {
                                Date bookingDate = new SimpleDateFormat("dd/MM/yyyy").parse(bookingDateStr);
                                application.setBookingDate(bookingDate);
                            } catch (ParseException e) {
                                System.out.println("Invalid booking date format on line " + lineNumber + 
                                                ": " + bookingDateStr);
                                // Set current date as fallback
                                application.setBookingDate(new Date());
                                System.out.println("Using current date as fallback");
                            }
                        } else {
                            // If no booking date was provided, set the current date
                            application.setBookingDate(new Date());
                        }
                        break;
                    case UNSUCCESSFUL:
                        application.reject();
                        break;
                }
                
                // Store in map
                applicationMap.put(applicationId, application);
                
                // Update applicant's current application reference
                if (application.isActive()) {
                    applicant.setCurrentApplication(application);
                    applicantDataManager.updateApplicant(applicant);
                }
                
                // Add application to project
                project.addApplication(application);
                
                lineNumber++;
            }
            
            for (String key : applicationMap.keySet()) {
                Application app = applicationMap.get(key);
            }
            
            return !applicationMap.isEmpty();
        } catch (IOException e) {
            System.out.println("Error loading application data: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    /**
     * Updates a single application's status directly in the file without rewriting everything.
     * 
     * @param applicationId The ID of the application to update
     * @param newStatus The new status to set
     * @return true if successful, false otherwise
     */
    public boolean updateApplicationStatusInFile(String applicationId, ApplicationStatus newStatus) {
        
        try {
            // First, read the current file contents
            List<String> fileLines = new ArrayList<>();
            String headerLine = null;
            
            try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
                // Read header line
                headerLine = reader.readLine();
                fileLines.add(headerLine);
                
                // Read rest of file
                String line;
                while ((line = reader.readLine()) != null) {
                    fileLines.add(line);
                }
            }
            
            // Now, find and update the specific application line
            boolean found = false;
            for (int i = 1; i < fileLines.size(); i++) {  // Start at 1 to skip header
                String line = fileLines.get(i);
                String[] parts = line.split(DELIMITER);
                
                if (parts.length >= 3) {
                    // Check if this line contains our application
                    // We don't have the ID directly in the file, so we need to construct it
                    String nric = parts[0].trim();
                    String projectName = parts[1].trim();
                    
                    // Generate the application ID
                    String lineAppId = generateApplicationId(nric, projectName);
                    
                    if (lineAppId.equals(applicationId)) {
                        
                        // Construct the updated line with new status
                        StringBuilder newLine = new StringBuilder();
                        newLine.append(parts[0]).append(DELIMITER);  // NRIC
                        newLine.append(parts[1]).append(DELIMITER);  // Project
                        newLine.append(newStatus.name()).append(DELIMITER);  // New Status
                        
                        // Keep the rest of the parts the same
                        for (int j = 3; j < parts.length; j++) {
                            newLine.append(parts[j]);
                            if (j < parts.length - 1) {
                                newLine.append(DELIMITER);
                            }
                        }
                        
                        // Update the line in our list
                        fileLines.set(i, newLine.toString());
                        found = true;
                        break;
                    }
                }
            }
            
            if (!found) {
                System.out.println("Application not found in file: " + applicationId);
                return false;
            }
            
            // Write the updated file contents back
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
                for (String line : fileLines) {
                    writer.write(line);
                    writer.newLine();
                }
            }
            
            System.out.println("Successfully updated application status");
            return true;
        } catch (IOException e) {
            System.out.println("ERROR updating application status in file: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Saves application data to the application list file.
     * 
     * @return true if the data was successfully saved, false otherwise
     */
    public boolean saveApplicationData() {
        
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            // Write header
            writer.write("Applicant NRIC" + DELIMITER + "Project Name" + DELIMITER + 
                        "Application Status" + DELIMITER + "Flat Type" + DELIMITER + "Booking Date");
            writer.newLine();
            
            // Create a backup copy of the current application statuses
            Map<String, ApplicationStatus> statusBackup = new HashMap<>();
            for (String appId : applicationMap.keySet()) {
                Application app = applicationMap.get(appId);
                statusBackup.put(appId, app.getStatus());
            }
            
            // Write application data one by one
            for (String appId : applicationMap.keySet()) {
                Application application = applicationMap.get(appId);
                ApplicationStatus status = statusBackup.get(appId);
                
                writer.write(application.getApplicant().getNric() + DELIMITER);
                writer.write(application.getProject().getProjectName() + DELIMITER);
                writer.write(status.name() + DELIMITER);
                writer.write(application.getSelectedFlatType().getDisplayName() + DELIMITER);
                
                // Write booking date if booked
                if (status == ApplicationStatus.BOOKED) {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                    Date bookingDate = application.getBookingDate();
                    if (bookingDate != null) {
                        writer.write(dateFormat.format(bookingDate));
                    } else {
                        writer.write("");
                    }
                } else {
                    writer.write("");
                }
                
                writer.newLine();
            }
            
            System.out.println("Successfully saved applications");
            return true;
        } catch (IOException e) {
            System.out.println("ERROR saving application data: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Adds a new application to the system.
     * 
     * @param application The application to add
     * @return true if the application was successfully added, false otherwise
     */
    public boolean addApplication(Application application) {
        if (application == null || application.getApplicationId() == null) {
            return false;
        }
        
        applicationMap.put(application.getApplicationId(), application);
        return saveApplicationData();
    }
    
    /**
     * Updates an existing application in the system.
     * 
     * @param application The application to update
     * @return true if the application was successfully updated, false otherwise
     */
    public boolean updateApplication(Application application) {
        if (application == null || application.getApplicationId() == null) {
            System.out.println("Cannot update null application or application with null ID");
            return false;
        }
        
        if (!applicationMap.containsKey(application.getApplicationId())) {
            System.out.println("Application with ID " + application.getApplicationId() + " not found.");
            return false;
        }
        
        // Update the specific application in the map
        applicationMap.put(application.getApplicationId(), application);
        
        // Instead of saving all applications immediately, return true to indicate the update was successful
        // Let the caller decide if and when to save all applications
        return true;
    }

    /**
     * Updates an application and saves all applications to the file.
     * 
     * @param application The application to update
     * @return true if the application was successfully updated and saved, false otherwise
     */
    public boolean updateAndSaveApplication(Application application) {
        boolean updated = updateApplication(application);
        if (updated) {
            return saveApplicationData();
        }
        return false;
    }
    
    /**
     * Gets an application by its ID.
     * 
     * @param applicationId The ID of the application to retrieve
     * @return The application if found, null otherwise
     */
    public Application getApplicationById(String applicationId) {
        return applicationMap.get(applicationId);
    }
    
    /**
     * Gets all applications for a specific project.
     * 
     * @param projectName The name of the project
     * @return A list of applications for the specified project
     */
    public List<Application> getApplicationsByProject(String projectName) {
        
        List<Application> projectApplications = new ArrayList<>();
        
        for (Application application : applicationMap.values()) {
            System.out.println("Checking application - Project: " + 
                            (application.getProject() != null ? 
                            application.getProject().getProjectName() : "null"));
            
            if (application.getProject() != null && 
                application.getProject().getProjectName().equals(projectName)) {
                projectApplications.add(application);
            }
        }
        
        System.out.println("Found " + projectApplications.size() + 
                        " applications for project: " + projectName);
        
        return projectApplications;
    }
    
    /**
     * Gets all applications for a specific applicant.
     * 
     * @param applicantNric The NRIC of the applicant
     * @return A list of applications for the specified applicant
     */
    public List<Application> getApplicationsByApplicant(String applicantNric) {
        List<Application> applicantApplications = new ArrayList<>();
        
        for (Application application : applicationMap.values()) {
            
            if (application.getApplicant().getNric().equals(applicantNric)) {
                applicantApplications.add(application);
            }
        }
        
        System.out.println("Found " + applicantApplications.size() + 
                        " applications for NRIC: " + applicantNric);
        
        return applicantApplications;
    }
    
    /**
     * Helper method to generate an application ID.
     * 
     * @param nric The NRIC of the applicant
     * @param projectName The name of the project
     * @return A unique application ID
     */
    private String generateApplicationId(String nric, String projectName) {
        return "APP-" + nric.substring(1, 8) + "-" + 
               projectName.substring(0, Math.min(3, projectName.length())).toUpperCase();
    }
    
    /**
     * Gets all applications in the system.
     * 
     * @return A list of all applications
     */
    public List<Application> getAllApplications() {
        return new ArrayList<>(applicationMap.values());
    }
}