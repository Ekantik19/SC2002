package com.bto.model;

import java.util.ArrayList;
import java.util.List;

/**
 * DataManager serves as a facade for all the specialized data managers.
 * It coordinates data operations across different entity types and provides a unified interface.
 */
public class DataManager {
    private List<User> users;
    private List<Project> projects;
    private List<Application> applications;
    private List<Enquiry> enquiries;
    private List<Report> reports;
    
    private String dataDirectory = "src/main/java/com/resources/";
    //"C:\Users\luisa\OneDrive\Documents\GitHub\SC2002\BTO_App\btoapp\src\main\java\com\resources"
    
    /**
     * Constructor for DataManager.
     */
    public DataManager() {
        users = new ArrayList<>();
        projects = new ArrayList<>();
        applications = new ArrayList<>();
        enquiries = new ArrayList<>();
        reports = new ArrayList<>();
    }
    
    /**
     * Initialize the data manager and load all data.
     */
    public void initialize() {
        loadData();
    }
    
    /**
     * Load all data from files.
     */
    public void loadData() {
        System.out.println("Loading data from: " + dataDirectory);
        
        // Load users (applicants, officers, managers)
        List<Applicant> applicants = ApplicantDataManager.loadApplicants();
        List<HDBOfficer> officers = OfficerDataManager.loadOfficers();
        List<HDBManager> managers = ManagerDataManager.loadManagers();
        
        // Add all users to the users list
        users.addAll(applicants);
        users.addAll(officers);
        users.addAll(managers);
        
        // Set data manager reference in each user
        for (User user : users) {
            user.setDataManager(this);
        }
        
        // Load projects
        projects = ProjectDataManager.loadProjects(managers, officers);
        
        // Load applications
        applications = ApplicationDataManager.loadApplications(applicants, projects);
        
        // Load enquiries
        enquiries = EnquiryDataManager.loadEnquiries(users, projects);
        
        System.out.println("Data loading complete.");
    }
    
    /**
     * Save all data to files.
     */
    public void saveData() {
        // Save all users
        List<Applicant> applicants = new ArrayList<>();
        List<HDBOfficer> officers = new ArrayList<>();
        List<HDBManager> managers = new ArrayList<>();
        
        // Separate users by type
        for (User user : users) {
            if (user instanceof Applicant) {
                applicants.add((Applicant) user);
            } else if (user instanceof HDBOfficer) {
                officers.add((HDBOfficer) user);
            } else if (user instanceof HDBManager) {
                managers.add((HDBManager) user);
            }
        }
        
        // Use individual data managers to save each type
        for (Applicant applicant : applicants) {
            ApplicantDataManager.updateApplicant(applicant);
        }
        
        for (HDBOfficer officer : officers) {
            OfficerDataManager.updateOfficer(officer);
        }
        
        for (HDBManager manager : managers) {
            ManagerDataManager.updateManager(manager);
        }
        
        // Save projects
        for (Project project : projects) {
            ProjectDataManager.updateProject(project);
        }
        
        // Save applications
        for (Application application : applications) {
            ApplicationDataManager.updateApplication(application);
        }
        
        // Save enquiries (would need specialized save methods)
        // This is simplified as a demonstration
        
        System.out.println("Data saving complete.");
    }
    
    /**
     * Authenticate a user.
     * 
     * @param userID The user ID to authenticate
     * @param password The password to verify
     * @return User object if login successful, null otherwise
     */
    public User authenticate(String userID, String password) {
        for (User user : users) {
            if (user.getUserID().equals(userID) && user.authenticate(password)) {
                return user;
            }
        }
        return null;
    }
    
    /**
     * Get a user by NRIC.
     * 
     * @param userID The NRIC to search for
     * @return The user, or null if not found
     */
    public User getUserByNRIC(String userID) {
        for (User user : users) {
            if (user.getUserID().equals(userID)) {
                return user;
            }
        }
        return null;
    }
    
    /**
     * Get a project by name.
     * 
     * @param projectName The name of the project to retrieve
     * @return The project with the specified name, or null if not found
     */
    public Project getProjectByName(String projectName) {
        for (Project project : projects) {
            if (project.getProjectName().equals(projectName)) {
                return project;
            }
        }
        return null;
    }
    
    /**
     * Add a new user.
     * 
     * @param user The user to add
     */
    public void addUser(User user) {
        users.add(user);
        
        if (user instanceof Applicant) {
            ApplicantDataManager.addApplicant((Applicant) user);
        } else if (user instanceof HDBOfficer) {
            OfficerDataManager.addOfficer((HDBOfficer) user);
        } else if (user instanceof HDBManager) {
            ManagerDataManager.addManager((HDBManager) user);
        }
    }
    
    /**
     * Add a new project.
     * 
     * @param project The project to add
     */
    public void addProject(Project project) {
        projects.add(project);
        ProjectDataManager.addProject(project);
    }
    
    /**
     * Add a new application.
     * 
     * @param application The application to add
     */
    public void addApplication(Application application) {
        applications.add(application);
        ApplicationDataManager.addApplication(application);
        
        // Link application to applicant and project
        Applicant applicant = (Applicant) application.getApplicant();
        applicant.setCurrentApplication(application);
        application.getProject().addApplication(application);
    }
    
    /**
     * Save an enquiry.
     * 
     * @param enquiry The enquiry to save
     */
    public void saveEnquiry(Enquiry enquiry) {
        enquiries.add(enquiry);
        EnquiryDataManager.addEnquiry(enquiry);
        
        // Link enquiry to applicant if applicable
        if (enquiry.getUser() instanceof Applicant) {
            ((Applicant) enquiry.getUser()).addEnquiry(enquiry);
        }
    }
    
    /**
     * Update an enquiry.
     * 
     * @param enquiry The enquiry to update
     */
    public void updateEnquiry(Enquiry enquiry) {
        for (int i = 0; i < enquiries.size(); i++) {
            if (enquiries.get(i).equals(enquiry)) {
                enquiries.set(i, enquiry);
                break;
            }
        }
        EnquiryDataManager.updateEnquiry(enquiry);
    }
    
    /**
     * Delete an enquiry.
     * 
     * @param enquiry The enquiry to delete
     */
    public void deleteEnquiry(Enquiry enquiry) {
        enquiries.remove(enquiry);
        EnquiryDataManager.deleteEnquiry(enquiry);
        
        // Remove from applicant's list if applicable
        if (enquiry.getUser() instanceof Applicant) {
            ((Applicant) enquiry.getUser()).getEnquiries().remove(enquiry);
        }
    }
    
    /**
     * Save a report to the in-memory list.
     * Reports are not persisted to disk in this implementation.
     * 
     * @param report The report to save
     */
    public void saveReport(Report report) {
        reports.add(report);
    }
    
    /**
     * Get all users.
     * 
     * @return The list of all users
     */
    public List<User> getAllUsers() {
        return users;
    }
    
    /**
     * Get all projects.
     * 
     * @return The list of all projects
     */
    public List<Project> getAllProjects() {
        return projects;
    }
    
    /**
     * Get all applications.
     * 
     * @return The list of all applications
     */
    public List<Application> getAllApplications() {
        return applications;
    }
    
    /**
     * Get all enquiries.
     * 
     * @return The list of all enquiries
     */
    public List<Enquiry> getAllEnquiries() {
        return enquiries;
    }
    
    /**
     * Get all reports.
     * 
     * @return The list of all reports
     */
    public List<Report> getAllReports() {
        return reports;
    }
    
    /**
     * Set the data directory.
     * 
     * @param directory The directory path
     */
    public void setDataDirectory(String directory) {
        this.dataDirectory = directory;
        if (!this.dataDirectory.endsWith("/")) {
            this.dataDirectory += "/";
        }
    }
}