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
 * The ApplicantDataManager handles the interaction between the application and the applicant text file.
 * It is responsible for adding, loading, and updating applicant information.
 */
public class ApplicantDataManager {
    
    private static final String filePath = "src/main/java/com/resources/ApplicantList.txt";
    //"C:\Users\luisa\OneDrive\Documents\GitHub\SC2002\BTO_App\btoapp\src\main\java\com\resources"
    
    /**
     * Load all applicants from the text file.
     * 
     * @return List of Applicant objects
     */
    public static List<Applicant> loadApplicants() {
        List<Applicant> applicants = new ArrayList<>();
        
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
                    
                    Applicant applicant = new Applicant(userID, password, age, maritalStatus);
                    applicants.add(applicant);
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading applicants: " + e.getMessage());
        }
        
        return applicants;
    }
    
    /**
     * Add a new applicant to the text file.
     * 
     * @param applicant The applicant to add
     */
    public static void addApplicant(Applicant applicant) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath, true))) {
            String line = String.join("\t", 
                "Applicant", // Placeholder for name
                applicant.getUserID(), 
                String.valueOf(applicant.getAge()), 
                applicant.getMaritalStatus(),
                "password" // Use default password
            );
            bw.write(line);
            bw.newLine();
        } catch (IOException e) {
            System.err.println("Error adding applicant to file: " + e.getMessage());
        }
    }
    
    /**
     * Update an applicant's information in the text file.
     * 
     * @param updatedApplicant The applicant with updated information
     */
    public static void updateApplicant(Applicant updatedApplicant) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath));
             BufferedWriter bw = new BufferedWriter(new FileWriter("temp.txt"))) {
            
            String line;
            String header = br.readLine(); // Read header
            bw.write(header); // Write header to temp file
            bw.newLine();
            
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\t");
                if (parts.length >= 5 && parts[1].equals(updatedApplicant.getUserID())) {
                    // Found the applicant to update, write updated information
                    String updatedLine = String.join("\t", 
                        "Applicant", // Placeholder for name
                        updatedApplicant.getUserID(), 
                        String.valueOf(updatedApplicant.getAge()), 
                        updatedApplicant.getMaritalStatus(),
                        parts[4] // Keep the existing password
                    );
                    bw.write(updatedLine);
                } else {
                    // Not the applicant to update, write original line
                    bw.write(line);
                }
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error updating applicant in file: " + e.getMessage());
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
     * Remove an applicant from the text file.
     * 
     * @param applicant The applicant to remove
     */
    public static void removeApplicant(Applicant applicant) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath));
             BufferedWriter bw = new BufferedWriter(new FileWriter("temp.txt"))) {
            
            String line;
            String header = br.readLine(); // Read header
            bw.write(header); // Write header to temp file
            bw.newLine();
            
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\t");
                if (parts.length >= 5 && parts[1].equals(applicant.getUserID())) {
                    // Skip this line (the applicant to remove)
                    continue;
                }
                // Write all other lines
                bw.write(line);
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error removing applicant from file: " + e.getMessage());
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
     * Check if an applicant with the given ID exists.
     * 
     * @param userID The ID to check
     * @return true if applicant exists, false otherwise
     */
    public static boolean applicantExists(String userID) {
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
            System.err.println("Error checking applicant existence: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Change an applicant's password.
     * 
     * @param applicant The applicant
     * @param newPassword The new password
     * @return true if successful, false otherwise
     */
    public static boolean changePassword(Applicant applicant, String newPassword) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath));
             BufferedWriter bw = new BufferedWriter(new FileWriter("temp.txt"))) {
            
            String line;
            String header = br.readLine(); // Read header
            bw.write(header); // Write header to temp file
            bw.newLine();
            
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\t");
                if (parts.length >= 5 && parts[1].equals(applicant.getUserID())) {
                    // Found the applicant to update password
                    parts[4] = newPassword;
                    String updatedLine = String.join("\t", parts);
                    bw.write(updatedLine);
                } else {
                    // Not the applicant to update, write original line
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
            System.err.println("Error changing applicant password: " + e.getMessage());
            return false;
        }
    }
}