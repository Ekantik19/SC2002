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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * The ApplicationDataManager handles the interaction between the application and the application text file.
 * It is responsible for adding, loading, and updating application information.
 */
public class ApplicationDataManager {
    
    private static final String filePath = "src/main/java/com/resources/ApplicationList.txt";
    //"C:\Users\luisa\OneDrive\Documents\GitHub\SC2002\BTO_App\btoapp\src\main\java\com\resources"
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");
    
    /**
     * Load all applications from the text file.
     * 
     * @param applicants List of applicants to link with applications
     * @param projects List of projects to link with applications
     * @return List of Application objects
     */
    public static List<Application> loadApplications(List<Applicant> applicants, List<Project> projects) {
        List<Application> applications = new ArrayList<>();
        
        File file = new File(filePath);
        // Check if the file exists
        if (!file.exists()) {
            try {
                // Create the file and write the header
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                    writer.write("ApplicantID\tProjectName\tFlatType\tStatus\tWithdrawalRequested\tFlatTypeBooked\tApplicationDate");
                    writer.newLine();
                }
                System.out.println("Created new ApplicationList.txt file with header");
            } catch (IOException e) {
                System.err.println("Error creating ApplicationList.txt file: " + e.getMessage());
            }
            return applications; // Return empty list since the file was just created
        }
        
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line = br.readLine(); // Skip header line
            
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\t");
                if (parts.length >= 5) {
                    String applicantID = parts[0];
                    String projectName = parts[1];
                    String flatType = parts[2];
                    String status = parts[3];
                    boolean withdrawalRequested = Boolean.parseBoolean(parts[4]);
                    
                    // Find applicant and project
                    Applicant applicant = null;
                    for (Applicant a : applicants) {
                        if (a.getUserID().equals(applicantID)) {
                            applicant = a;
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
                    
                    // Create application if applicant and project are found
                    if (applicant != null && project != null) {
                        Application application = new Application(applicant, project);
                        application.setFlatType(flatType);
                        application.updateStatus(status);
                        application.setWithdrawalRequested(withdrawalRequested);
                        
                        // Set booked flat type if available
                        if (parts.length > 5 && status.equals(Application.STATUS_BOOKED)) {
                            String flatTypeBooked = parts[5];
                            application.setFlatTypeBooked(flatTypeBooked);
                            
                            // Update applicant information
                            applicant.setBookedFlatType(flatTypeBooked);
                            applicant.setBookedProject(projectName);
                        }
                        
                        // Set application date if available
                        if (parts.length > 6) {
                            try {
                                Date applicationDate = DATE_FORMAT.parse(parts[6]);
                                application.setApplicationDate(applicationDate);
                            } catch (ParseException e) {
                                System.err.println("Error parsing application date: " + e.getMessage());
                            }
                        }
                        
                        applications.add(application);
                        applicant.setCurrentApplication(application);
                        project.addApplication(application);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading applications: " + e.getMessage());
        }
        
        return applications;
    }
    
    /**
     * Add a new application to the text file.
     * 
     * @param application The application to add
     */
    public static void addApplication(Application application) {
        // Check if the file exists
        File file = new File(filePath);
        if (!file.exists()) {
            try {
                // Create the file and write the header
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                    writer.write("ApplicantID\tProjectName\tFlatType\tStatus\tWithdrawalRequested\tFlatTypeBooked\tApplicationDate");
                    writer.newLine();
                }
                System.out.println("Created new ApplicationList.txt file with header");
            } catch (IOException e) {
                System.err.println("Error creating ApplicationList.txt file: " + e.getMessage());
                return;
            }
        }
        
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath, true))) {
            StringBuilder line = new StringBuilder();
            
            line.append(application.getApplicant().getUserID()).append("\t");
            line.append(application.getProject().getProjectName()).append("\t");
            line.append(application.getFlatType()).append("\t");
            line.append(application.getStatus()).append("\t");
            line.append(application.isWithdrawalRequested());
            
            // Add booked flat type if status is BOOKED
            if (Application.STATUS_BOOKED.equals(application.getStatus())) {
                line.append("\t").append(application.getFlatTypeBooked());
            } else {
                line.append("\t"); // Empty column for flat type booked
            }
            
            // Add application date
            if (application.getApplicationDate() != null) {
                line.append("\t").append(DATE_FORMAT.format(application.getApplicationDate()));
            }
            
            bw.write(line.toString());
            bw.newLine();
        } catch (IOException e) {
            System.err.println("Error adding application to file: " + e.getMessage());
        }
    }
    
    /**
     * Update an application's information in the text file.
     * 
     * @param updatedApplication The application with updated information
     */
    public static void updateApplication(Application updatedApplication) {
        // Check if the file exists
        File file = new File(filePath);
        if (!file.exists()) {
            addApplication(updatedApplication);
            return;
        }
        
        try (BufferedReader br = new BufferedReader(new FileReader(filePath));
             BufferedWriter bw = new BufferedWriter(new FileWriter("temp.txt"))) {
            
            String line;
            String header = br.readLine(); // Read header
            bw.write(header); // Write header to temp file
            bw.newLine();
            
            boolean found = false;
            
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\t");
                if (parts.length >= 2 && parts[0].equals(updatedApplication.getApplicant().getUserID()) && 
                    parts[1].equals(updatedApplication.getProject().getProjectName())) {
                    // Found the application to update, write updated information
                    StringBuilder updatedLine = new StringBuilder();
                    
                    updatedLine.append(updatedApplication.getApplicant().getUserID()).append("\t");
                    updatedLine.append(updatedApplication.getProject().getProjectName()).append("\t");
                    updatedLine.append(updatedApplication.getFlatType()).append("\t");
                    updatedLine.append(updatedApplication.getStatus()).append("\t");
                    updatedLine.append(updatedApplication.isWithdrawalRequested());
                    
                    // Add booked flat type if status is BOOKED
                    if (Application.STATUS_BOOKED.equals(updatedApplication.getStatus())) {
                        updatedLine.append("\t").append(updatedApplication.getFlatTypeBooked());
                    } else {
                        updatedLine.append("\t"); // Empty column for flat type booked
                    }
                    
                    // Add application date
                    if (updatedApplication.getApplicationDate() != null) {
                        updatedLine.append("\t").append(DATE_FORMAT.format(updatedApplication.getApplicationDate()));
                    }
                    
                    bw.write(updatedLine.toString());
                    found = true;
                } else {
                    // Not the application to update, write original line
                    bw.write(line);
                }
                bw.newLine();
            }
            
            // If the application wasn't found, add it
            if (!found) {
                StringBuilder newLine = new StringBuilder();
                
                newLine.append(updatedApplication.getApplicant().getUserID()).append("\t");
                newLine.append(updatedApplication.getProject().getProjectName()).append("\t");
                newLine.append(updatedApplication.getFlatType()).append("\t");
                newLine.append(updatedApplication.getStatus()).append("\t");
                newLine.append(updatedApplication.isWithdrawalRequested());
                
                // Add booked flat type if status is BOOKED
                if (Application.STATUS_BOOKED.equals(updatedApplication.getStatus())) {
                    newLine.append("\t").append(updatedApplication.getFlatTypeBooked());
                } else {
                    newLine.append("\t"); // Empty column for flat type booked
                }
                
                // Add application date
                if (updatedApplication.getApplicationDate() != null) {
                    newLine.append("\t").append(DATE_FORMAT.format(updatedApplication.getApplicationDate()));
                }
                
                bw.write(newLine.toString());
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error updating application in file: " + e.getMessage());
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
     * Remove an application from the text file.
     * 
     * @param application The application to remove
     */
    public static void removeApplication(Application application) {
        // Check if the file exists
        File file = new File(filePath);
        if (!file.exists()) {
            return; // Nothing to remove
        }
        
        try (BufferedReader br = new BufferedReader(new FileReader(filePath));
             BufferedWriter bw = new BufferedWriter(new FileWriter("temp.txt"))) {
            
            String line;
            String header = br.readLine(); // Read header
            bw.write(header); // Write header to temp file
            bw.newLine();
            
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\t");
                if (parts.length >= 2 && parts[0].equals(application.getApplicant().getUserID()) && 
                    parts[1].equals(application.getProject().getProjectName())) {
                    // Skip this line (the application to remove)
                    continue;
                }
                // Write all other lines
                bw.write(line);
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error removing application from file: " + e.getMessage());
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
     * Update an application's status in the text file.
     * 
     * @param application The application to update
     * @param newStatus The new status to set
     */
    public static void updateApplicationStatus(Application application, String newStatus) {
        application.updateStatus(newStatus);
        updateApplication(application);
    }
    
    /**
     * Set withdrawal request flag for an application.
     * 
     * @param application The application to update
     * @param withdrawalRequested The withdrawal request flag
     */
    public static void setWithdrawalRequested(Application application, boolean withdrawalRequested) {
        application.setWithdrawalRequested(withdrawalRequested);
        updateApplication(application);
    }
    
    /**
     * Book a flat for an application.
     * 
     * @param application The application to update
     * @param flatType The flat type to book
     * @return True if booking was successful, false otherwise
     */
    public static boolean bookFlat(Application application, String flatType) {
        // Check if application status is successful
        if (!Application.STATUS_SUCCESSFUL.equals(application.getStatus())) {
            return false;
        }
        
        // Check if the flat type is valid for the project
        Project project = application.getProject();
        if (!project.hasFlatType(flatType) || project.getRemainingUnits(flatType) <= 0) {
            return false;
        }
        
        // Book the flat
        application.setFlatTypeBooked(flatType);
        
        // Decrement available units
        project.decrementUnits(flatType);
        
        // Update application status to booked
        application.updateStatus(Application.STATUS_BOOKED);
        
        // Update applicant's booked flat information
        Applicant applicant = (Applicant) application.getApplicant();
        applicant.setBookedFlatType(flatType);
        applicant.setBookedProject(project.getProjectName());
        
        // Update application in file
        updateApplication(application);
        
        return true;
    }
    
    /**
     * Check if an applicant has an existing application.
     * 
     * @param applicantID The ID of the applicant to check
     * @return True if applicant has an existing application, false otherwise
     */
    public static boolean hasExistingApplication(String applicantID) {
        // Check if the file exists
        File file = new File(filePath);
        if (!file.exists()) {
            return false;
        }
        
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            br.readLine(); // Skip header
            
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\t");
                if (parts.length >= 1 && parts[0].equals(applicantID)) {
                    return true;
                }
            }
        } catch (IOException e) {
            System.err.println("Error checking for existing application: " + e.getMessage());
        }
        
        return false;
    }
}