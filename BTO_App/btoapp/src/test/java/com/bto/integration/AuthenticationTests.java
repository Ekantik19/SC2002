package com.bto.integration;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;

import com.bto.model.Applicant;
import com.bto.model.HDBManager;
import com.bto.model.HDBOfficer;
import com.bto.model.User;

/**
 * Test cases for user authentication functionality
 * Covers test cases 1-4 from Appendix A
 */
public class AuthenticationTests extends BaseTest {
    
    /**
     * Test Case 1: Valid User Login
     * Expected: User should be able to access their dashboard based on their roles
     */
    @Test
    public void testCase1_ValidUserLogin() {
        String testName = "Valid User Login";
        System.out.println("\nTest Case 1: " + testName);
        
        try {
            // Test applicant login
            User applicant = authController.login("S1234567A", "password");
            assertNotNull("Applicant login should not return null", applicant);
            assertTrue("Applicant login should return an Applicant instance", applicant instanceof Applicant);
            printPass(testName, "Successfully logged in as Applicant");
            
            // Test HDB Officer login
            User officer = authController.login("T2109876H", "password");
            assertNotNull("Officer login should not return null", officer);
            assertTrue("Officer login should return an HDBOfficer instance", officer instanceof HDBOfficer);
            printPass(testName, "Successfully logged in as HDB Officer");
            
            // Test HDB Manager login
            User manager = authController.login("S5678901G", "password");
            assertNotNull("Manager login should not return null", manager);
            assertTrue("Manager login should return an HDBManager instance", manager instanceof HDBManager);
            printPass(testName, "Successfully logged in as HDB Manager");
        } catch (Exception e) {
            fail("Exception occurred: " + e.getMessage());
            printFail(testName, "Exception occurred: " + e.getMessage());
        }
    }
    
    /**
     * Test Case 2: Invalid NRIC Format
     * Expected: User receives a notification about incorrect NRIC format
     */
    @Test
    public void testCase2_InvalidNRICFormat() {
        String testName = "Invalid NRIC Format";
        System.out.println("\nTest Case 2: " + testName);
        
        try {
            // Try invalid format with incorrect starting letter
            User user1 = authController.login("X1234567A", "password");
            assertNull("System should reject NRIC with invalid starting letter", user1);
            printPass(testName, "System rejected NRIC with invalid starting letter");
            
            // Try invalid format with incorrect length
            User user2 = authController.login("S123456", "password");
            assertNull("System should reject NRIC with incorrect length", user2);
            printPass(testName, "System rejected NRIC with incorrect length");
            
            // Try invalid format with missing check digit
            User user3 = authController.login("S12345678", "password");
            assertNull("System should reject NRIC with missing check digit", user3);
            printPass(testName, "System rejected NRIC with missing check digit");
        } catch (Exception e) {
            if (e.getMessage().toLowerCase().contains("nric") || 
                e.getMessage().toLowerCase().contains("format")) {
                printPass(testName, "System rejected with appropriate message: " + e.getMessage());
            } else {
                fail("Exception not specific to NRIC format: " + e.getMessage());
                printFail(testName, "Exception not specific to NRIC format: " + e.getMessage());
            }
        }
    }
    
    /**
     * Test Case 3: Incorrect Password
     * Expected: System should deny access and alert the user to incorrect password
     */
    @Test
    public void testCase3_IncorrectPassword() {
        String testName = "Incorrect Password";
        System.out.println("\nTest Case 3: " + testName);
        
        try {
            // Try with correct NRIC but wrong password
            User user = authController.login("S1234567A", "wrongpassword");
            assertNull("System should deny access with incorrect password", user);
            printPass(testName, "System denied access with incorrect password");
        } catch (Exception e) {
            if (e.getMessage().toLowerCase().contains("password")) {
                printPass(testName, "System threw appropriate exception: " + e.getMessage());
            } else {
                fail("Exception not specific to password: " + e.getMessage());
                printFail(testName, "Exception not specific to password: " + e.getMessage());
            }
        }
    }
    
    /**
     * Test Case 4: Password Change Functionality
     * Expected: System updates password, prompt re-login and allows login with new credentials
     */
    @Test
    public void testCase4_PasswordChange() {
        String testName = "Password Change";
        System.out.println("\nTest Case 4: " + testName);
        
        try {
            // First login with current password
            User user = authController.login("S1234567A", "password");
            assertNotNull("Should be able to log in to test password change", user);
            
            // Change password to new one
            boolean changeResult = authController.changePassword(user, "password", "newpassword");
            assertTrue("Should be able to change password", changeResult);
            printPass(testName, "Password changed successfully");
            
            // Try to log in with old password (should fail)
            User userWithOldPwd = authController.login("S1234567A", "password");
            assertNull("System should reject old password after change", userWithOldPwd);
            printPass(testName, "System rejected old password after change");
            
            // Try to log in with new password (should succeed)
            User userWithNewPwd = authController.login("S1234567A", "newpassword");
            assertNotNull("Should be able to log in with new password", userWithNewPwd);
            printPass(testName, "Successfully logged in with new password");
            
            // Change back to original password for other tests
            boolean resetResult = authController.changePassword(userWithNewPwd, "newpassword", "password");
            assertTrue("Should be able to revert password for other tests", resetResult);
            printPass(testName, "Successfully reverted password for other tests");
        } catch (Exception e) {
            fail("Exception occurred: " + e.getMessage());
            printFail(testName, "Exception occurred: " + e.getMessage());
        }
    }
}