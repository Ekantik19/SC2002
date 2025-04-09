package com.bto.controller;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import com.bto.controller.abstracts.AAuthenticationController;
import com.bto.model.Applicant;
import com.bto.model.HDBManager;
import com.bto.model.HDBOfficer;
import com.bto.model.User;

/**
 * Controller for user authentication in the BTO Management System.
 * 
 * @author Your Name
 * @version 1.0
 */
public class AuthenticationController extends AAuthenticationController {
    
    /**
     * Constructor for AuthenticationController.
     */
    public AuthenticationController() {
        super();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean loadUserData(String filePath) {
        if (!validateNotNullOrEmpty(filePath, "File Path")) {
            return false;
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            // Skip header line
            reader.readLine();
            
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\t");
                
                if (parts.length < 5) {
                    System.out.println("Invalid data format: " + line);
                    continue;
                }
                
                String name = parts[0];
                String nric = parts[1];
                int age;
                try {
                    age = Integer.parseInt(parts[2]);
                } catch (NumberFormatException e) {
                    System.out.println("Invalid age format: " + parts[2]);
                    continue;
                }
                String maritalStatus = parts[3];
                String password = parts[4];
                String role = parts.length > 5 ? parts[5] : "Applicant";
                
                // Create user based on role
                User user;
                switch (role.trim()) {
                    case "Manager":
                        user = new HDBManager(name, nric, age, maritalStatus, password);
                        break;
                    case "Officer":
                        user = new HDBOfficer(name, nric, age, maritalStatus, password);
                        break;
                    default:
                        user = new Applicant(name, nric, age, maritalStatus, password);
                }
                
                // Add user to map
                addUser(user);
            }
            
            return true;
        } catch (IOException e) {
            System.out.println("Error loading user data: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean saveUserData(String filePath) {
        if (!validateNotNullOrEmpty(filePath, "File Path")) {
            return false;
        }
        
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            // Write header
            writer.write("Name\tNRIC\tAge\tMarital Status\tPassword\tRole");
            writer.newLine();
            
            // Write users
            for (User user : userMap.values()) {
                writer.write(user.getName() + "\t");
                writer.write(user.getNric() + "\t");
                writer.write(user.getAge() + "\t");
                writer.write(user.getMaritalStatus() + "\t");
                
                // We don't have direct access to the password, but in a real implementation
                // we would store the hashed password here
                writer.write("password\t");
                
                // Write role
                writer.write(user.getRole().getDisplayName());
                writer.newLine();
            }
            
            return true;
        } catch (IOException e) {
            System.out.println("Error saving user data: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Creates a new applicant user.
     * 
     * @param name The name of the applicant
     * @param nric The NRIC of the applicant
     * @param age The age of the applicant
     * @param maritalStatus The marital status of the applicant
     * @param password The password for the applicant
     * @return The created applicant if successful, null otherwise
     */
    public Applicant createApplicant(String name, String nric, int age, String maritalStatus, String password) {
        // Validate input
        if (!validateNotNullOrEmpty(name, "Name") || 
            !validateNotNullOrEmpty(nric, "NRIC") || 
            !validateNotNullOrEmpty(maritalStatus, "Marital Status") || 
            !validateNotNullOrEmpty(password, "Password")) {
            return null;
        }
        
        // Validate NRIC format
        if (!validateNRICFormat(nric)) {
            System.out.println("Invalid NRIC format");
            return null;
        }
        
        // Check if user already exists
        if (userMap.containsKey(nric.toUpperCase())) {
            System.out.println("User with this NRIC already exists");
            return null;
        }
        
        // Create new applicant
        Applicant applicant = new Applicant(name, nric, age, maritalStatus, password);
        
        // Add to map
        addUser(applicant);
        
        return applicant;
    }
    
    /**
     * Creates a new officer user.
     * 
     * @param name The name of the officer
     * @param nric The NRIC of the officer
     * @param age The age of the officer
     * @param maritalStatus The marital status of the officer
     * @param password The password for the officer
     * @return The created officer if successful, null otherwise
     */
    public HDBOfficer createOfficer(String name, String nric, int age, String maritalStatus, String password) {
        // Validate input
        if (!validateNotNullOrEmpty(name, "Name") || 
            !validateNotNullOrEmpty(nric, "NRIC") || 
            !validateNotNullOrEmpty(maritalStatus, "Marital Status") || 
            !validateNotNullOrEmpty(password, "Password")) {
            return null;
        }
        
        // Validate NRIC format
        if (!validateNRICFormat(nric)) {
            System.out.println("Invalid NRIC format");
            return null;
        }
        
        // Check if user already exists
        if (userMap.containsKey(nric.toUpperCase())) {
            System.out.println("User with this NRIC already exists");
            return null;
        }
        
        // Create new officer
        HDBOfficer officer = new HDBOfficer(name, nric, age, maritalStatus, password);
        
        // Add to map
        addUser(officer);
        
        return officer;
    }
    
    /**
     * Creates a new manager user.
     * 
     * @param name The name of the manager
     * @param nric The NRIC of the manager
     * @param age The age of the manager
     * @param maritalStatus The marital status of the manager
     * @param password The password for the manager
     * @return The created manager if successful, null otherwise
     */
    public HDBManager createManager(String name, String nric, int age, String maritalStatus, String password) {
        // Validate input
        if (!validateNotNullOrEmpty(name, "Name") || 
            !validateNotNullOrEmpty(nric, "NRIC") || 
            !validateNotNullOrEmpty(maritalStatus, "Marital Status") || 
            !validateNotNullOrEmpty(password, "Password")) {
            return null;
        }
        
        // Validate NRIC format
        if (!validateNRICFormat(nric)) {
            System.out.println("Invalid NRIC format");
            return null;
        }
        
        // Check if user already exists
        if (userMap.containsKey(nric.toUpperCase())) {
            System.out.println("User with this NRIC already exists");
            return null;
        }
        
        // Create new manager
        HDBManager manager = new HDBManager(name, nric, age, maritalStatus, password);
        
        // Add to map
        addUser(manager);
        
        return manager;
    }
}