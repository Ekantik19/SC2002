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
     * Loads officer data from the OfficerList.txt file.
     * 
     * @return true if the data was successfully loaded, false otherwise
     */
    public boolean loadOfficerData() {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            // Skip header line
            reader.readLine();
            
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\t");
                
                // Ensure we have all required data
                if (parts.length < 5) {
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
            }
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
     * Updates an existing officer in the data manager.
     * 
     * @param officer The officer with updated information
     * @return true if the officer was successfully updated, false otherwise
     */
    public boolean updateOfficer(HDBOfficer officer) {
        if (officer == null || !validateNRICFormat(officer.getNric())) {
            System.out.println("Invalid officer or NRIC format");
            return false;
        }
        
        String nric = officer.getNric().toUpperCase();
        
        // If the officer doesn't exist, add a log message
        if (!officersMap.containsKey(nric)) {
            System.out.println("Officer not found. Adding new officer: " + nric);
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
}