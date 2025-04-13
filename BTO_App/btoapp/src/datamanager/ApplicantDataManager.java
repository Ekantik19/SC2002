package datamanager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import model.Applicant;
import utils.FilePathConfig;

/**
 * ApplicantDataManager handles the interaction between the application and the ApplicantList.txt file.
 * It is responsible for reading and writing applicant data to the text file.
 * 
 * @author Your Name
 * @version 1.0
 */
public class ApplicantDataManager {
    
    private String filePath;
    private static final String DELIMITER = "\t";
    
    /**
     * Default constructor that uses the file path from FilePathConfig.
     */
    public ApplicantDataManager() {
        this(FilePathConfig.APPLICANT_LIST_PATH);
    }
    
    /**
     * Constructor for ApplicantDataManager with a specified file path.
     * 
     * @param filePath The path to the applicant data file
     */
    public ApplicantDataManager(String filePath) {
        this.filePath = filePath;
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
     * Sets a new file path for the applicant data file.
     * 
     * @param filePath The new file path
     */
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
    
    /**
     * Reads all applicants from the applicant data file.
     * 
     * @return A list of Applicant objects
     */
    // public List<Applicant> readAllApplicants() {
    //     List<Applicant> applicants = new ArrayList<>();

    //     System.out.println("Attempting to read applicants from: " + filePath);
    //     File file = new File(filePath);
    //     System.out.println("File exists: " + file.exists());
    //     System.out.println("Current working directory: " + System.getProperty("user.dir"));
        
    //     try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
    //         String line;
    //         boolean isHeader = true;
            
    //         while ((line = reader.readLine()) != null) {
    //             if (isHeader) {
    //                 isHeader = false;
    //                 continue; // Skip the header row
    //             }
                
    //             String[] parts = line.split(DELIMITER);
    //             if (parts.length >= 5) {
    //                 String name = parts[0];
    //                 String nric = parts[1];
    //                 int age = Integer.parseInt(parts[2]);
    //                 String maritalStatus = parts[3];
    //                 String password = parts[4];
                    
    //                 Applicant applicant = new Applicant(name, nric, age, maritalStatus, password);
    //                 applicants.add(applicant);
    //             }
    //         }
    //     } catch (IOException e) {
    //         System.out.println("Error reading applicant data: " + e.getMessage());
    //     }
        
    //     return applicants;
    // }

    public List<Applicant> readAllApplicants() {
        List<Applicant> applicants = new ArrayList<>();
        
        System.out.println("DEBUG: Reading applicants from: " + filePath);
        File file = new File(filePath);
        System.out.println("DEBUG: File exists: " + file.exists());
        
        if (!file.exists()) {
            System.out.println("DEBUG: ⚠️ WARNING: Applicant file not found at: " + filePath);
            System.out.println("DEBUG: Current working directory: " + System.getProperty("user.dir"));
            return applicants;
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean isHeader = true;
            
            while ((line = reader.readLine()) != null) {
                if (isHeader) {
                    isHeader = false;
                    continue; // Skip the header row
                }
                
                System.out.println("DEBUG: Processing line: " + line);
                
                String[] parts = line.split(DELIMITER);
                if (parts.length >= 5) {
                    String name = parts[0];
                    String nric = parts[1];
                    int age = Integer.parseInt(parts[2]);
                    String maritalStatus = parts[3];
                    String password = parts[4];
                    
                    System.out.println("DEBUG: Creating applicant - Name: " + name + ", NRIC: " + nric + 
                                      ", Password: " + password);
                    
                    Applicant applicant = new Applicant(name, nric, age, maritalStatus, password);
                    applicants.add(applicant);
                }
            }
        } catch (IOException e) {
            System.out.println("DEBUG: ⚠️ ERROR reading applicant data: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("DEBUG: Read " + applicants.size() + " applicants");
        return applicants;
    }
    
    /**
     * Finds an applicant by their NRIC.
     * 
     * @param nric The NRIC to search for
     * @return The applicant if found, null otherwise
     */
    public Applicant findApplicantByNRIC(String nric) {
        List<Applicant> applicants = readAllApplicants();
        
        for (Applicant applicant : applicants) {
            if (applicant.getNric().equals(nric)) {
                return applicant;
            }
        }
        
        return null;
    }
    
    /**
     * Adds a new applicant to the applicant data file.
     * 
     * @param applicant The applicant to add
     * @return true if the applicant was added successfully, false otherwise
     */
    public boolean addApplicant(Applicant applicant) {
        if (findApplicantByNRIC(applicant.getNric()) != null) {
            return false; // Applicant with this NRIC already exists
        }
        
        List<Applicant> applicants = readAllApplicants();
        applicants.add(applicant);
        
        return writeApplicants(applicants);
    }
    
    /**
     * Updates an existing applicant in the applicant data file.
     * 
     * @param applicant The applicant to update
     * @return true if the applicant was updated successfully, false otherwise
     */
    public boolean updateApplicant(Applicant applicant) {
        List<Applicant> applicants = readAllApplicants();
        
        for (int i = 0; i < applicants.size(); i++) {
            if (applicants.get(i).getNric().equals(applicant.getNric())) {
                applicants.set(i, applicant);
                return writeApplicants(applicants);
            }
        }
        
        return false; // Applicant not found
    }
    
    /**
     * Deletes an applicant from the applicant data file.
     * 
     * @param nric The NRIC of the applicant to delete
     * @return true if the applicant was deleted successfully, false otherwise
     */
    public boolean deleteApplicant(String nric) {
        List<Applicant> applicants = readAllApplicants();
        
        for (int i = 0; i < applicants.size(); i++) {
            if (applicants.get(i).getNric().equals(nric)) {
                applicants.remove(i);
                return writeApplicants(applicants);
            }
        }
        
        return false; // Applicant not found
    }
    
    /**
     * Writes the list of applicants to the applicant data file.
     * 
     * @param applicants The list of applicants to write
     * @return true if the applicants were written successfully, false otherwise
     */
    private boolean writeApplicants(List<Applicant> applicants) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            // Write header
            writer.write("Name" + DELIMITER + "NRIC" + DELIMITER + "Age" + DELIMITER + 
                        "Marital Status" + DELIMITER + "Password");
            writer.newLine();
            
            // Write applicant data
            for (Applicant applicant : applicants) {
                writer.write(
                    applicant.getName() + DELIMITER +
                    applicant.getNric() + DELIMITER +
                    applicant.getAge() + DELIMITER +
                    applicant.getMaritalStatus() + DELIMITER +
                    applicant.getPassword()
                );
                writer.newLine();
            }
            
            return true;
        } catch (IOException e) {
            System.out.println("Error writing applicant data: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Updates the password for an applicant.
     * 
     * @param nric The NRIC of the applicant
     * @param newPassword The new password
     * @return true if the password was updated successfully, false otherwise
     */
    public boolean updatePassword(String nric, String newPassword) {
        Applicant applicant = findApplicantByNRIC(nric);
        
        if (applicant != null) {
            applicant.changePassword(newPassword);
            return updateApplicant(applicant);
        }
        
        return false;
    }
    
    /**
     * Validates the NRIC format.
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
}