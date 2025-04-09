package com.bto.integration;

import org.junit.After;
import org.junit.Before;

import com.bto.controller.ApplicationController;
import com.bto.controller.AuthController;
import com.bto.controller.EnquiryController;
import com.bto.controller.ProjectController;
import com.bto.controller.ReportController;
import com.bto.model.DataManager;

/**
 * Base class for all test cases
 * Provides common setup and utility methods
 */
public class BaseTest {
    protected DataManager dataManager;
    protected AuthController authController;
    protected ProjectController projectController;
    protected ApplicationController applicationController;
    protected EnquiryController enquiryController;
    protected ReportController reportController;
    
    /**
     * Default constructor
     * Note: Primary initialization is done in setUp() method
     */
    public BaseTest() {
        // Empty constructor - initialization moved to setUp()
    }
    
    /**
     * Set up before each test method is executed
     * This method is automatically called before each test method
     */
    @Before
    public void setUp() {
        // Initialize with test data directory
        //dataManager = new DataManager("test/java/com/resources/");
        dataManager = new DataManager();
        dataManager.setDataDirectory("test/java/com/resources/");
        
        // Initialize controllers
        authController = new AuthController(dataManager);
        projectController = new ProjectController(dataManager,authController);
        applicationController = new ApplicationController(dataManager,authController,projectController);
        enquiryController = new EnquiryController(dataManager);
        reportController=new ReportController(dataManager);
    }
    
    /**
     * Clean up after each test method
     * This method is automatically called after each test method
     */
    @After
    public void tearDown() {
        // Clean up resources if needed
        // This can be left empty if no specific cleanup is required
    }
    
    /**
     * Print pass message with test name
     * @param testName The test name or description
     * @param message Additional details about the pass
     */
    protected void printPass(String testName, String message) {
        System.out.println("✓ PASS: " + testName + " - " + message);
    }
    
    /**
     * Print fail message with test name
     * @param testName The test name or description
     * @param message Additional details about the failure
     */
    protected void printFail(String testName, String message) {
        System.out.println("✗ FAIL: " + testName + " - " + message);
    }
    
    /**
     * Reset the system to initial state during a test
     * This can be called manually when a test needs a fresh state
     */
    protected void resetSystem() {
        // Close and reinitialize data manager and controllers
        //dataManager = new DataManager("test/dataList/");
        dataManager = new DataManager();
        dataManager.setDataDirectory("main/java/com/resources/");

        authController = new AuthController(dataManager);
        projectController = new ProjectController(dataManager,authController);
        applicationController = new ApplicationController(dataManager,authController,projectController);
        enquiryController = new EnquiryController(dataManager);
        reportController=new ReportController(dataManager);
    }
}