package utils;

/**
 * Centralized configuration for file paths used in the BTO Management System.
 */
public class FilePathConfig {
    // Absolute paths to the data files
    private static final String BASE_PATH = "C:/Users/luisa/OneDrive/Documents/GitHub/SC2002/BTO_App/btoapp/src/resources/";
    // Applicant-related file paths
    public static final String APPLICANT_LIST_PATH = BASE_PATH + "ApplicantList.txt";
    
    // Manager-related file paths
    public static final String MANAGER_LIST_PATH = BASE_PATH + "ManagerList.txt";
    
    // Officer-related file paths
    public static final String OFFICER_LIST_PATH = BASE_PATH + "OfficerList.txt";
    
    // Project-related file paths
    public static final String PROJECT_LIST_PATH = BASE_PATH + "ProjectList.txt";
    
    // Application-related file paths
    public static final String APPLICATION_LIST_PATH = BASE_PATH + "ApplicationList.txt";
    
    // Enquiry-related file paths
    public static final String ENQUIRY_LIST_PATH = BASE_PATH + "EnquiryList.txt";

    // Private constructor to prevent instantiation
    private FilePathConfig() {
        throw new AssertionError("Cannot be instantiated");
    }
}