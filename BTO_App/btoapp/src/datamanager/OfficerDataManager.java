package datamanager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import model.HDBOfficer;
import utils.FilePathConfig;

/**
 * Data manager for handling HDBOfficer data operations.
 * Responsible for reading from and writing to the OfficerList.txt file.
 * 
 * @author Your Name
 * @version 1.0
 */
public class OfficerDataManager {
    
    private Map<String, HDBOfficer> officersMap;
    private String filePath;
    
    /**
     * Constructor for OfficerDataManager.
     * Initializes the officers map.
     */
    public OfficerDataManager() {
        this.officersMap = new HashMap<>();
        this.filePath = FilePathConfig.OFFICER_LIST_PATH;
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
     * Sets a new file path for the officer data file.
     * 
     * @param filePath The new file path
     */
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
    
    /**
     * Loads officer data from the OfficerList.txt file.
     * 
     * @return true if the data was successfully loaded, false otherwise
     */
    public boolean loadOfficerData() {
        System.out.println("DEBUG: Loading officers from: " + filePath);
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            // Skip header line
            reader.readLine();
            
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\t");
                
                // Ensure we have all required data
                if (parts.length < 5) {
                    System.out.println("Invalid officer data format: " + line);
                    continue;
                }
                
                String name = parts[0];
                String nric = parts[1].toUpperCase(); // Convert NRIC to uppercase
                
                // Parse age
                int age;
                try {
                    age = Integer.parseInt(parts[2]);
                } catch (NumberFormatException e) {
                    System.out.println("Invalid age format for officer: " + name);
                    continue;
                }
                
                String maritalStatus = parts[3];
                String password = parts[4];
                
                // Create HDBOfficer object
                HDBOfficer officer = new HDBOfficer(name, nric, age, maritalStatus, password);
                
                // Add to map using uppercase NRIC
                officersMap.put(nric, officer);
                System.out.println("DEBUG: Loaded officer: " + name + ", NRIC: " + nric);
            }
            
            System.out.println("DEBUG: Loaded " + officersMap.size() + " officers");
            return true;
        } catch (IOException e) {
            System.out.println("Error loading officer data: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Saves officer data to the OfficerList.txt file.
     * 
     * @return true if the data was successfully saved, false otherwise
     */
    public boolean saveOfficerData() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            // Write header
            writer.write("Name\tNRIC\tAge\tMarital Status\tPassword");
            writer.newLine();
            
            // Write officers
            for (HDBOfficer officer : officersMap.values()) {
                writer.write(String.format("%s\t%s\t%d\t%s\t%s",
                                          officer.getName(),
                                          officer.getNric(),
                                          officer.getAge(),
                                          officer.getMaritalStatus(),
                                          officer.getPassword()));
                writer.newLine();
            }
            
            return true;
        } catch (IOException e) {
            System.out.println("Error saving officer data: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Adds an officer to the data manager.
     * 
     * @param officer The officer to add
     * @return true if the officer was successfully added, false otherwise
     */
    public boolean addOfficer(HDBOfficer officer) {
        if (officer == null || officersMap.containsKey(officer.getNric())) {
            return false;
        }
        
        officersMap.put(officer.getNric(), officer);
        return true;
    }
    
    /**
     * Updates an existing officer in the data manager.
     * 
     * @param officer The officer with updated information
     * @return true if the officer was successfully updated, false otherwise
     */
    public boolean updateOfficer(HDBOfficer officer) {
        if (officer == null || !validateNRICFormat(officer.getNric())) {
            System.out.println("DEBUG: Invalid officer or NRIC format");
            return false;
        }
        
        String nric = officer.getNric().toUpperCase();
        
        // If the officer doesn't exist, add a log message
        if (!officersMap.containsKey(nric)) {
            System.out.println("DEBUG: Officer not found in map. Adding new officer: " + nric);
        }
        
        // Always update/add the officer to the map
        officersMap.put(nric, officer);
        return true;
    }

    /**
     * Validates NRIC format.
     * 
     * @param nric The NRIC to validate
     * @return true if the NRIC is valid, false otherwise
     */
    private boolean validateNRICFormat(String nric) {
        if (nric == null) {
            return false;
        }
        
        // NRIC should start with S or T, followed by 7 digits, and end with a letter
        return nric.matches("^[ST]\\d{7}[A-Z]$");
    }
    
    /**
     * Removes an officer from the data manager.
     * 
     * @param nric The NRIC of the officer to remove
     * @return true if the officer was successfully removed, false otherwise
     */
    public boolean removeOfficer(String nric) {
        if (nric == null || !officersMap.containsKey(nric)) {
            return false;
        }
        
        officersMap.remove(nric);
        return true;
    }
    
    /**
     * Gets an officer by NRIC.
     * 
     * @param nric The NRIC of the officer to retrieve
     * @return The officer if found, null otherwise
     */
    public HDBOfficer getOfficerByNric(String nric) {
        return officersMap.get(nric);
    }
    
    /**
     * Gets all officers in the data manager.
     * 
     * @return A list of all officers
     */
    public List<HDBOfficer> getAllOfficers() {
        return new ArrayList<>(officersMap.values());
    }
    
    /**
     * Checks if an officer with the given NRIC exists.
     * 
     * @param nric The NRIC to check
     * @return true if an officer with the given NRIC exists, false otherwise
     */
    public boolean officerExists(String nric) {
        return officersMap.containsKey(nric);
    }
    
    /**
     * Gets the count of officers in the data manager.
     * 
     * @return The number of officers
     */
    public int getOfficerCount() {
        return officersMap.size();
    }
    
    /**
     * Gets officers by their marital status.
     * 
     * @param maritalStatus The marital status to filter by
     * @return A list of officers with the specified marital status
     */
    public List<HDBOfficer> getOfficersByMaritalStatus(String maritalStatus) {
        List<HDBOfficer> result = new ArrayList<>();
        
        for (HDBOfficer officer : officersMap.values()) {
            if (officer.getMaritalStatus().equalsIgnoreCase(maritalStatus)) {
                result.add(officer);
            }
        }
        
        return result;
    }
    
    /**
     * Gets officers within an age range.
     * 
     * @param minAge The minimum age (inclusive)
     * @param maxAge The maximum age (inclusive)
     * @return A list of officers within the specified age range
     */
    public List<HDBOfficer> getOfficersByAgeRange(int minAge, int maxAge) {
        List<HDBOfficer> result = new ArrayList<>();
        
        for (HDBOfficer officer : officersMap.values()) {
            int age = officer.getAge();
            if (age >= minAge && age <= maxAge) {
                result.add(officer);
            }
        }
        
        return result;
    }
    
    /**
     * Gets officers assigned to a specific project.
     * 
     * @param projectId The ID of the project
     * @return A list of officers assigned to the specified project
     */
    public List<HDBOfficer> getOfficersByProject(String projectId) {
        List<HDBOfficer> result = new ArrayList<>();
        
        for (HDBOfficer officer : officersMap.values()) {
            if (officer.isProjectAssigned() && 
                officer.getAssignedProject() != null &&
                officer.getAssignedProject().getProjectName().equals(projectId)) {
                result.add(officer);
            }
        }
        
        return result;
    }
    
    /**
     * Gets officers with pending registration for any project.
     * 
     * @return A list of officers with pending registration
     */
    public List<HDBOfficer> getOfficersWithPendingRegistration() {
        List<HDBOfficer> result = new ArrayList<>();
        
        for (HDBOfficer officer : officersMap.values()) {
            if (officer.getAssignedProject() != null && !officer.isRegistrationApproved()) {
                result.add(officer);
            }
        }
        
        return result;
    }
    
    /**
     * Gets officers with approved registration for any project.
     * 
     * @return A list of officers with approved registration
     */
    public List<HDBOfficer> getOfficersWithApprovedRegistration() {
        List<HDBOfficer> result = new ArrayList<>();
        
        for (HDBOfficer officer : officersMap.values()) {
            if (officer.isProjectAssigned()) {
                result.add(officer);
            }
        }
        
        return result;
    }
    
    /**
     * Validates an officer's credentials.
     * 
     * @param nric The NRIC of the officer
     * @param password The password to validate
     * @return The officer if credentials are valid, null otherwise
     */
    public HDBOfficer validateOfficerCredentials(String nric, String password) {
        HDBOfficer officer = getOfficerByNric(nric);
        
        if (officer != null && officer.authenticate(nric, password)) {
            return officer;
        }
        
        return null;
    }
}