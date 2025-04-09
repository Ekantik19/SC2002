package com.bto.utils;

/**
 * Centralized configuration for file paths used in the BTO Management System.
 * Provides static final constants for file locations to ensure consistency 
 * and easy maintenance of file paths across the application.
 */
public class FilePathConfig {
    // Applicant-related file paths
    public static final String APPLICANT_LIST_PATH = "src/main/resources/ApplicantList.txt";
    
    // Manager-related file paths
    public static final String MANAGER_LIST_PATH = "src/main/resources/ManagerList.txt";
    
    // Officer-related file paths
    public static final String OFFICER_LIST_PATH = "src/main/resources/OfficerList.txt";
    
    // Project-related file paths
    public static final String PROJECT_LIST_PATH = "src/main/resources/ProjectList.txt";
    
    // Application-related file paths
    public static final String APPLICATION_LIST_PATH = "src/main/resources/Application.txt";
    
    // Enquiry-related file paths
    public static final String ENQUIRY_LIST_PATH = "src/main/resources/Enquiry.txt";

    // Private constructor to prevent instantiation
    private FilePathConfig() {
        throw new AssertionError("Cannot be instantiated");
    }
}