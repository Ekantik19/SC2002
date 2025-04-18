package view;

import controller.AuthenticationController;
import java.util.Scanner;
import model.User;
import view.abstracts.ARenderView;
import view.interfaces.ViewInterface;

/**
 * View for changing user password.
 */
public class PasswordChangeView extends ARenderView implements ViewInterface{
    private User currentUser;
    private Scanner scanner;
    private AuthenticationController authController;
    
    /**
     * Constructor for PasswordChangeView.
     * 
     * @param currentUser The currently logged-in user
     */
    public PasswordChangeView(User currentUser,AuthenticationController authcontroller) {
        this.currentUser = currentUser;
        this.authController=authcontroller;
        this.scanner = new Scanner(System.in);
    }

    public void display() {
        printHeader("CHANGE PASSWORD");
        
        System.out.print("Enter current password: ");
        String currentPassword = scanner.nextLine();
        
        System.out.print("Enter new password: ");
        String newPassword = scanner.nextLine();
        
        System.out.print("Confirm new password: ");
        String confirmPassword = scanner.nextLine();
        
        if (!newPassword.equals(confirmPassword)) {
            showError("New passwords do not match. Password change cancelled.");
            return;
        }
        
        // Call model to change password
        boolean success = authController.changePassword(currentUser.getNric(), currentPassword, newPassword);
        
        if (success) {
            showMessage("Password changed successfully.");
        } else {
            showError("Failed to change password. Please check your current password.");
        }
    }
    
    @Override
    public void showMessage(String message) {
        System.out.println("\n>>> " + message);
    }
    
    @Override
    public void showError(String error) {
        System.out.println("\n!!! ERROR: " + error);
    }
}