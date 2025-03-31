package com.bto.model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Singleton class responsible for managing data persistence.
 */
public class DataManager {
    private static DataManager instance;
    
    private List<User> users;
    private List<Project> projects;
    private List<Application> applications;
    private List<Enquiry> enquiries;
    
    // File paths
    private static final String USERS_FILE = "users.txt";
    private static final String PROJECTS_FILE = "projects.txt";
    private static final String APPLICATIONS_FILE = "applications.txt";
    private static final String ENQUIRIES_FILE = "enquiries.txt";

    private String dataDirectory = "main/java/com/resources/";
    
    /**
     * Private constructor for singleton pattern.
     */
    private DataManager() {
        users = new ArrayList<>();
        projects = new ArrayList<>();
        applications = new ArrayList<>();
        enquiries = new ArrayList<>();
    }

    public static DataManager createDataManager() {
        return new DataManager();
    }
    
    // /**
    //  * Get the singleton instance.
    //  * 
    //  * @return The DataManager instance
    //  */
    // public static DataManager getInstance() {
    //     if (instance == null) {
    //         instance = new DataManager();
    //     }
    //     return instance;
    // }

    /**
     * Load all data from files.
     */
    public void loadData() {
        loadUsers();
        loadProjects();
        loadApplications();
        loadEnquiries();
    }
    
    /**
     * Save all data to files.
     */
    public void saveData() {
        saveUsers();
        saveProjects();
        saveApplications();
        saveEnquiries();
    }
    
    /**
     * Authenticate a user.
     * 
     * @param userID The user ID to authenticate
     * @param password The password to verify
     * @return The authenticated User, or null if authentication fails
     */
    public User authenticate(String userID, String password) {
        for (User user : users) {
            if (user.getUserID().equals(userID) && user.authenticate(password)) {
                 // Set the DataManager for the user before returning it
                user.setDataManager(this);
                return user;
            }
        }
        return null;
    }
    
    /**
     * Load users from file.
     */
    private void loadUsers() {
        try (BufferedReader reader = new BufferedReader(new FileReader(dataDirectory + USERS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\t");
                if (parts.length >= 5) {
                    String userID = parts[0];
                    String password = parts[1];
                    String role = parts[2];
                    int age = Integer.parseInt(parts[3]);
                    String maritalStatus = parts[4];
                    
                    User user;
                    if ("Applicant".equals(role)) {
                        user = new Applicant(userID, password, age, maritalStatus);
                    } else if ("HDBOfficer".equals(role)) {
                        user = new HDBOfficer(userID, password, age, maritalStatus);
                    } else if ("HDBManager".equals(role)) {
                        user = new HDBManager(userID, password, age, maritalStatus);
                    } else {
                        continue; // Skip invalid roles
                    }
                    
                    user.setDataManager(this);
                
                    users.add(user);
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading users: " + e.getMessage());
        }
    }
    
    /**
     * Save users to file.
     */
    private void saveUsers() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(dataDirectory + USERS_FILE))) {
            for (User user : users) {
                String role;
                if (user instanceof Applicant) {
                    role = "Applicant";
                } else if (user instanceof HDBManager) {
                    role = "HDBManager";
                } else if (user instanceof HDBOfficer) {
                    role = "HDBOfficer";
                } else {
                    continue; // Skip invalid types
                }
                
                writer.write(user.getUserID() + "\t" + 
                            "password" + "\t" + // Don't save actual password for demo
                            role + "\t" +
                            user.getAge() + "\t" +
                            user.getMaritalStatus());
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error saving users: " + e.getMessage());
        }
    }
    
    /**
     * Load projects from file.
     */
    private void loadProjects() {
        // Implementation similar to loadUsers
    }
    
    /**
     * Save projects to file.
     */
    private void saveProjects() {
        // Implementation similar to saveUsers
    }
    
    /**
     * Load applications from file.
     */
    private void loadApplications() {
        // Implementation similar to loadUsers
    }
    
    /**
     * Save applications to file.
     */
    private void saveApplications() {
        // Implementation similar to saveUsers
    }
    
    /**
     * Load enquiries from file.
     */
    private void loadEnquiries() {
        try (BufferedReader reader = new BufferedReader(new FileReader(ENQUIRIES_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\t");
                if (parts.length >= 5) {
                    String enquiryId = parts[0];
                    String userID = parts[1];
                    int projectId = Integer.parseInt(parts[2]);
                    String enquiryText = parts[3];
                    String response = parts.length > 4 ? parts[4] : "";
                    
                    // Find the user and project
                    User user = getUserByNRIC(userID);
                    Project project = getProjectById(projectId);
                    
                    if (user != null && project != null) {
                        Enquiry enquiry = new Enquiry(user, project, enquiryText);
                        enquiry.setEnquiryId(enquiryId);
                        if (!response.isEmpty()) {
                            enquiry.addResponse(response);
                        }
                        enquiries.add(enquiry);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading enquiries: " + e.getMessage());
        }
    }
    
    /**
     * Save enquiries to file.
     */
    private void saveEnquiries() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(ENQUIRIES_FILE))) {
            for (Enquiry enquiry : enquiries) {
                writer.write(enquiry.getEnquiryId() + "\t" + 
                            enquiry.getUser().getUserID() + "\t" +
                            enquiry.getProject().getProjectID() + "\t" +
                            enquiry.getEnquiryText() + "\t" +
                            (enquiry.getResponse() != null ? enquiry.getResponse() : ""));
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error saving enquiries: " + e.getMessage());
        }
    }
    
    /**
     * Get a user by NRIC.
     * 
     * @param userID The NRIC/UserID of the user to find
     * @return The user with the specified NRIC, or null if not found
     */
    public User getUserByNRIC(String userID) {
        for (User user : users) {
            if (user.getUserID().equals(userID)) {
                // Set the DataManager for the user before returning it
                user.setDataManager(this);
                return user;
            }
        }
        return null;
    }
    
    /**
     * Get a project by ID.
     * 
     * @param projectId The ID of the project to find
     * @return The project with the specified ID, or null if not found
     */
    public Project getProjectById(int projectId) {
        for (Project project : projects) {
            if (project.getProjectID() == projectId) {
                return project;
            }
        }
        return null;
    }
    
    /**
     * Get all projects that an applicant has applied for.
     * 
     * @param applicant The applicant
     * @return A list of projects the applicant has applied for
     */
    public List<Project> getAppliedProjects(Applicant applicant) {
        List<Project> appliedProjects = new ArrayList<>();
        
        for (Application application : applications) {
            if (application.getApplicant().getUserID().equals(applicant.getUserID())) {
                appliedProjects.add(application.getProject());
            }
        }
        
        return appliedProjects;
    }
    
    // Other methods for data access
    public List<User> getAllUsers() {
        return users;
    }
    
    public List<Project> getAllProjects() {
        return projects;
    }
    
    public List<Application> getAllApplications() {
        return applications;
    }
    
    /**
     * Get all enquiries.
     * 
     * @return A list of all enquiries
     */
    public List<Enquiry> getAllEnquiries() {
        return enquiries;
    }
    
    public void addUser(User user) {
        users.add(user);
        saveUsers();
    }
    
    public void addProject(Project project) {
        projects.add(project);
        saveProjects();
    }
    
    public void addApplication(Application application) {
        applications.add(application);
        saveApplications();
    }
    
    /**
     * Save an enquiry.
     * 
     * @param enquiry The enquiry to save
     */
    public void saveEnquiry(Enquiry enquiry) {
        // Check if this is an update or a new enquiry
        boolean isUpdate = false;
        for (int i = 0; i < enquiries.size(); i++) {
            if (enquiries.get(i).getEnquiryId().equals(enquiry.getEnquiryId())) {
                enquiries.set(i, enquiry);
                isUpdate = true;
                break;
            }
        }
        
        if (!isUpdate) {
            enquiries.add(enquiry);
        }
        
        saveEnquiries();
    }
    
    /**
     * Update an existing enquiry.
     * 
     * @param enquiry The enquiry to update
     */
    public void updateEnquiry(Enquiry enquiry) {
        for (int i = 0; i < enquiries.size(); i++) {
            if (enquiries.get(i).getEnquiryId().equals(enquiry.getEnquiryId())) {
                enquiries.set(i, enquiry);
                break;
            }
        }
        saveEnquiries();
    }
    
    /**
     * Delete an enquiry.
     * 
     * @param enquiry The enquiry to delete
     */
    public void deleteEnquiry(Enquiry enquiry) {
        enquiries.removeIf(e -> e.getEnquiryId().equals(enquiry.getEnquiryId()));
        saveEnquiries();
    }
//////////////

    private List<Report> reports = new ArrayList<>();

    /**
     * Save a report.
     * 
     * @param report The report to save
     */
    public void saveReport(Report report) {
        reports.add(report);
        // Implement file persistence if needed
    }

    /**
     * Get all reports.
     * 
     * @return A list of all reports
     */
    public List<Report> getAllReports() {
        return reports;
    }

    public void setDataDirectory(String directory) {
        this.dataDirectory = directory;
    }
}
