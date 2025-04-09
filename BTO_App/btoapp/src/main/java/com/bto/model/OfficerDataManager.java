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
 * The OfficerDataManager handles the interaction between the application and the officer text file.
 * It is responsible for adding, loading, and updating HDB officer information.
 */
public class OfficerDataManager {
    
    private static final String filePath = "src/main/java/com/resources/OfficerList.txt";
    //"C:\Users\luisa\OneDrive\Documents\GitHub\SC2002\BTO_App\btoapp\src\main\java\com\resources"
    
    /**
     * Load all HDB officers from the text file.
     * 
     * @return List of HDBOfficer objects
     */
    public static List<HDBOfficer> loadOfficers() {
        List<HDBOfficer> officers = new ArrayList<>();
        
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line = br.readLine(); // Skip header line
            
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\t");
                if (parts.length >= 5) {
                    String name = parts[0]; // Name
                    String userID = parts[1]; // NRIC
                    int age = Integer.parseInt(parts[2]); // Age
                    String maritalStatus = parts[3]; // Marital Status
                    String password = parts[4]; // Password
                    
                    HDBOfficer officer = new HDBOfficer(userID, password, age, maritalStatus);
                    officers.add(officer);
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading officers: " + e.getMessage());
        }
        
        return officers;
    }
    
    /**
     * Add a new HDB officer to the text file.
     * 
     * @param officer The officer to add
     */
    public static void addOfficer(HDBOfficer officer) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath, true))) {
            String line = String.join("\t", 
                "Officer", // Placeholder for name
                officer.getUserID(), 
                String.valueOf(officer.getAge()), 
                officer.getMaritalStatus(),
                "password" // Use default password
            );
            bw.write(line);
            bw.newLine();
        } catch (IOException e) {
            System.err.println("Error adding officer to file: " + e.getMessage());
        }
    }
    
    /**
     * Update an HDB officer's information in the text file.
     * 
     * @param updatedOfficer The officer with updated information
     */
    public static void updateOfficer(HDBOfficer updatedOfficer) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath));
             BufferedWriter bw = new BufferedWriter(new FileWriter("temp.txt"))) {
            
            String line;
            String header = br.readLine(); // Read header
            bw.write(header); // Write header to temp file
            bw.newLine();
            
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\t");
                if (parts.length >= 5 && parts[1].equals(updatedOfficer.getUserID())) {
                    // Found the officer to update, write updated information
                    String updatedLine = String.join("\t", 
                        "Officer", // Placeholder for name
                        updatedOfficer.getUserID(), 
                        String.valueOf(updatedOfficer.getAge()), 
                        updatedOfficer.getMaritalStatus(),
                        parts[4] // Keep the existing password
                    );
                    bw.write(updatedLine);
                } else {
                    // Not the officer to update, write original line
                    bw.write(line);
                }
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error updating officer in file: " + e.getMessage());
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
     * Remove an HDB officer from the text file.
     * 
     * @param officer The officer to remove
     */
    public static void removeOfficer(HDBOfficer officer) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath));
             BufferedWriter bw = new BufferedWriter(new FileWriter("temp.txt"))) {
            
            String line;
            String header = br.readLine(); // Read header
            bw.write(header); // Write header to temp file
            bw.newLine();
            
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\t");
                if (parts.length >= 5 && parts[1].equals(officer.getUserID())) {
                    // Skip this line (the officer to remove)
                    continue;
                }
                // Write all other lines
                bw.write(line);
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error removing officer from file: " + e.getMessage());
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
     * Check if an HDB officer with the given ID exists.
     * 
     * @param userID The ID to check
     * @return true if officer exists, false otherwise
     */
    public static boolean officerExists(String userID) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            br.readLine(); // Skip header
            
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\t");
                if (parts.length >= 5 && parts[1].equals(userID)) {
                    return true;
                }
            }
        } catch (IOException e) {
            System.err.println("Error checking officer existence: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Change an HDB officer's password.
     * 
     * @param officer The officer
     * @param newPassword The new password
     * @return true if successful, false otherwise
     */
    public static boolean changePassword(HDBOfficer officer, String newPassword) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath));
             BufferedWriter bw = new BufferedWriter(new FileWriter("temp.txt"))) {
            
            String line;
            String header = br.readLine(); // Read header
            bw.write(header); // Write header to temp file
            bw.newLine();
            
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\t");
                if (parts.length >= 5 && parts[1].equals(officer.getUserID())) {
                    // Found the officer to update password
                    parts[4] = newPassword;
                    String updatedLine = String.join("\t", parts);
                    bw.write(updatedLine);
                } else {
                    // Not the officer to update, write original line
                    bw.write(line);
                }
                bw.newLine();
            }
            
            // Replace original file with updated temp file
            File originalFile = new File(filePath);
            File tempFile = new File("temp.txt");
            
            Path originalPath = Paths.get(originalFile.getPath());
            Path tempPath = Paths.get(tempFile.getPath());
            
            Files.move(tempPath, originalPath, StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (IOException e) {
            System.err.println("Error changing officer password: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Update an officer's project assignment and registration status.
     * 
     * @param officer The officer to update
     * @param project The project to assign (or null to unassign)
     * @param status The registration status
     */
    public static void updateProjectAssignment(HDBOfficer officer, Project project, String status) {
        // First update the officer's properties
        officer.setAssignedProject(project);
        officer.setRegistrationStatus(status);
        
        // Then update in the file
        updateOfficer(officer);
    }
    
    /**
     * Get an officer by their ID.
     * 
     * @param officerID The ID to look up
     * @return The officer if found, null otherwise
     */
    public static HDBOfficer getOfficerByID(String officerID) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            br.readLine(); // Skip header
            
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\t");
                if (parts.length >= 5 && parts[1].equals(officerID)) {
                    String name = parts[0]; // Name
                    String userID = parts[1]; // NRIC
                    int age = Integer.parseInt(parts[2]); // Age
                    String maritalStatus = parts[3]; // Marital Status
                    String password = parts[4]; // Password
                    
                    return new HDBOfficer(userID, password, age, maritalStatus);
                }
            }
        } catch (IOException e) {
            System.err.println("Error getting officer by ID: " + e.getMessage());
        }
        
        return null;
    }
}