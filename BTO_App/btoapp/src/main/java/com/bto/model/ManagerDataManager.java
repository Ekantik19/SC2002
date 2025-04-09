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
 * The ManagerDataManager handles the interaction between the application and the manager text file.
 * It is responsible for adding, loading, and updating HDB manager information.
 */
public class ManagerDataManager {
    
    private static final String filePath = "src/main/java/com/resources/ManagerList.txt";
    //"C:\Users\luisa\OneDrive\Documents\GitHub\SC2002\BTO_App\btoapp\src\main\java\com\resources"
    
    /**
     * Load all HDB managers from the text file.
     * 
     * @return List of HDBManager objects
     */
    public static List<HDBManager> loadManagers() {
        List<HDBManager> managers = new ArrayList<>();
        
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
                    
                    HDBManager manager = new HDBManager(userID, password, age, maritalStatus);
                    managers.add(manager);
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading managers: " + e.getMessage());
        }
        
        return managers;
    }
    
    /**
     * Add a new HDB manager to the text file.
     * 
     * @param manager The manager to add
     */
    public static void addManager(HDBManager manager) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath, true))) {
            String line = String.join("\t", 
                "Manager", // Placeholder for name
                manager.getUserID(), 
                String.valueOf(manager.getAge()), 
                manager.getMaritalStatus(),
                "password" // Use default password
            );
            bw.write(line);
            bw.newLine();
        } catch (IOException e) {
            System.err.println("Error adding manager to file: " + e.getMessage());
        }
    }
    
    /**
     * Update an HDB manager's information in the text file.
     * 
     * @param updatedManager The manager with updated information
     */
    public static void updateManager(HDBManager updatedManager) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath));
             BufferedWriter bw = new BufferedWriter(new FileWriter("temp.txt"))) {
            
            String line;
            String header = br.readLine(); // Read header
            bw.write(header); // Write header to temp file
            bw.newLine();
            
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\t");
                if (parts.length >= 5 && parts[1].equals(updatedManager.getUserID())) {
                    // Found the manager to update, write updated information
                    String updatedLine = String.join("\t", 
                        "Manager", // Placeholder for name
                        updatedManager.getUserID(), 
                        String.valueOf(updatedManager.getAge()), 
                        updatedManager.getMaritalStatus(),
                        parts[4] // Keep the existing password
                    );
                    bw.write(updatedLine);
                } else {
                    // Not the manager to update, write original line
                    bw.write(line);
                }
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error updating manager in file: " + e.getMessage());
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
     * Remove an HDB manager from the text file.
     * 
     * @param manager The manager to remove
     */
    public static void removeManager(HDBManager manager) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath));
             BufferedWriter bw = new BufferedWriter(new FileWriter("temp.txt"))) {
            
            String line;
            String header = br.readLine(); // Read header
            bw.write(header); // Write header to temp file
            bw.newLine();
            
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\t");
                if (parts.length >= 5 && parts[1].equals(manager.getUserID())) {
                    // Skip this line (the manager to remove)
                    continue;
                }
                // Write all other lines
                bw.write(line);
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error removing manager from file: " + e.getMessage());
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
     * Check if an HDB manager with the given ID exists.
     * 
     * @param userID The ID to check
     * @return true if manager exists, false otherwise
     */
    public static boolean managerExists(String userID) {
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
            System.err.println("Error checking manager existence: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Change an HDB manager's password.
     * 
     * @param manager The manager
     * @param newPassword The new password
     * @return true if successful, false otherwise
     */
    public static boolean changePassword(HDBManager manager, String newPassword) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath));
             BufferedWriter bw = new BufferedWriter(new FileWriter("temp.txt"))) {
            
            String line;
            String header = br.readLine(); // Read header
            bw.write(header); // Write header to temp file
            bw.newLine();
            
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\t");
                if (parts.length >= 5 && parts[1].equals(manager.getUserID())) {
                    // Found the manager to update password
                    parts[4] = newPassword;
                    String updatedLine = String.join("\t", parts);
                    bw.write(updatedLine);
                } else {
                    // Not the manager to update, write original line
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
            System.err.println("Error changing manager password: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get a manager by their ID.
     * 
     * @param managerID The ID to look up
     * @return The manager if found, null otherwise
     */
    public static HDBManager getManagerByID(String managerID) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            br.readLine(); // Skip header
            
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\t");
                if (parts.length >= 5 && parts[1].equals(managerID)) {
                    String name = parts[0]; // Name
                    String userID = parts[1]; // NRIC
                    int age = Integer.parseInt(parts[2]); // Age
                    String maritalStatus = parts[3]; // Marital Status
                    String password = parts[4]; // Password
                    
                    return new HDBManager(userID, password, age, maritalStatus);
                }
            }
        } catch (IOException e) {
            System.err.println("Error getting manager by ID: " + e.getMessage());
        }
        
        return null;
    }
}