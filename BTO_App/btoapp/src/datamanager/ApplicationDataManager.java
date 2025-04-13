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
    private String filePath;
    
    /**
     * Constructor for ApplicationDataManager.
     * 
     * @param applicantDataManager The ApplicantDataManager to use for resolving applicant references
     * @param projectDataManager The ProjectDataManager to use for resolving project references
     */
    public ApplicationDataManager(ApplicantDataManager applicantDataManager, ProjectDataManager projectDataManager) {
        this.applicationMap = new HashMap<>();
        this.applicantDataManager = applicantDataManager;
        this.projectDataManager = projectDataManager;
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
        System.out.println("DEBUG: Loading applications from: " + filePath);
        applicationMap.clear();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            // Skip header line
            String headerLine = reader.readLine();
            
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(DELIMITER);
                
                // Skip empty lines or lines with insufficient data
                if (parts.length < 5 || line.trim().isEmpty()) {
                    continue;
                }
                
                String applicantNric = parts[0];
                String projectName = parts[1];
                String statusStr = parts[2];
                String flatTypeStr = parts[3];
                String bookingDateStr = parts[4];
                
                // Get the applicant and project objects
                Applicant applicant = applicantDataManager.findApplicantByNRIC(applicantNric);
                Project project = projectDataManager.getProjectByName(projectName);
                
                // Skip if either applicant or project is not found
                if (applicant == null || project == null) {
                    System.out.println("Skipping application: Applicant or Project not found - " + 
                                      "NRIC: " + applicantNric + ", Project: " + projectName);
                    continue;
                }
                
                // Parse application status
                ApplicationStatus status = ApplicationStatus.valueOf(statusStr.toUpperCase());
                
                // Parse flat type
                FlatType flatType = FlatType.valueOf(flatTypeStr.replace("-", "_").toUpperCase());
                
                // Parse booking date if available
                Date bookingDate = null;
                if (!bookingDateStr.trim().isEmpty()) {
                    try {
                        bookingDate = DATE_FORMAT.parse(bookingDateStr);
                    } catch (ParseException e) {
                        System.out.println("Invalid date format: " + bookingDateStr);
                    }
                }
                
                // Generate application ID
                String applicationId = generateApplicationId(applicantNric, projectName);
                
                // Create application object
                Application application = new Application(applicationId, applicant, project, flatType);
                
                // Set application status and date
                if (status == ApplicationStatus.SUCCESSFUL) {
                    application.approve();
                } else if (status == ApplicationStatus.BOOKED) {
                    application.approve();
                    application.bookFlat();
                    
                    // Update applicant's booked project and flat type
                    applicant.setBookedProject(project);
                    applicant.setBookedFlatType(flatType);
                } else if (status == ApplicationStatus.UNSUCCESSFUL) {
                    application.reject();
                }
                
                // Store in map
                applicationMap.put(applicationId, application);
                
                // Update applicant's current application reference
                if (application.isActive()) {
                    applicant.setCurrentApplication(application);
                }
                
                // Add application to project
                project.addApplication(application);
            }
            
            System.out.println("DEBUG: Loaded " + applicationMap.size() + " applications");
            return true;
        } catch (IOException e) {
            System.out.println("Error loading application data: " + e.getMessage());
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
            
            // Write application data
            for (Application application : applicationMap.values()) {
                writer.write(application.getApplicant().getNric() + DELIMITER);
                writer.write(application.getProject().getProjectName() + DELIMITER);
                writer.write(application.getStatus().name() + DELIMITER);
                writer.write(application.getSelectedFlatType().getDisplayName() + DELIMITER);
                
                // Write booking date if available
                if (application.getStatus() == ApplicationStatus.BOOKED) {
                    writer.write(DATE_FORMAT.format(application.getApplicationDate()));
                } else {
                    writer.write("");
                }
                
                writer.newLine();
            }
            
            return true;
        } catch (IOException e) {
            System.out.println("Error saving application data: " + e.getMessage());
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
            return false;
        }
        
        if (!applicationMap.containsKey(application.getApplicationId())) {
            return false;
        }
        
        applicationMap.put(application.getApplicationId(), application);
        return saveApplicationData();
    }
    
    /**
     * Removes an application from the system.
     * 
     * @param applicationId The ID of the application to remove
     * @return true if the application was successfully removed, false otherwise
     */
    public boolean removeApplication(String applicationId) {
        if (applicationId == null || !applicationMap.containsKey(applicationId)) {
            return false;
        }
        
        applicationMap.remove(applicationId);
        return saveApplicationData();
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
            if (application.getProject().getProjectName().equals(projectName)) {
                projectApplications.add(application);
            }
        }
        
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
        
        return applicantApplications;
    }
    
    /**
     * Gets all applications with a specific status.
     * 
     * @param status The status to filter by
     * @return A list of applications with the specified status
     */
    public List<Application> getApplicationsByStatus(ApplicationStatus status) {
        List<Application> statusApplications = new ArrayList<>();
        
        for (Application application : applicationMap.values()) {
            if (application.getStatus() == status) {
                statusApplications.add(application);
            }
        }
        
        return statusApplications;
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