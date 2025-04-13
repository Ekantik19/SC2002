package view;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import controller.ApplicationController;
import controller.EnquiryController;
import controller.ProjectController;
import model.User;
import view.abstracts.ARenderView;
import view.interfaces.IBTOView;
import view.menu.MenuAction;
import view.menu.MenuBuilder;
import view.menu.MenuBuilderFactory;

/**
 * MainMenuView provides the main menu interface for the BTO Management System.
 * It displays different options based on the user's role and handles navigation.
 * 
 * @author Your Name
 * @version 1.0
 */
public class MainMenuView extends ARenderView implements IBTOView {
    
    private User currentUser;
    private MenuNavigator menuNavigator;
    private MenuBuilder menuBuilder;
    private Map<Integer, MenuAction> menuActions;
    private Scanner scanner;
    
    /**
     * Constructor for MainMenuView.
     * 
     * @param currentUser The currently logged-in user
     * @param projectController Controller for project operations
     * @param applicationController Controller for application operations
     * @param enquiryController Controller for enquiry operations
     */
    public MainMenuView(User currentUser, 
                       ProjectController projectController,
                       ApplicationController applicationController,
                       EnquiryController enquiryController) {
        this.currentUser = currentUser;
        this.scanner = new Scanner(System.in);
        
        // Initialize menu builder based on user role
        this.menuBuilder = MenuBuilderFactory.createMenuBuilder(currentUser);
        
        // Initialize menu navigator
        this.menuNavigator = new MenuNavigator(
            currentUser, 
            projectController, 
            applicationController, 
            enquiryController
        );
        
        // Build menu actions
        this.menuActions = new HashMap<>();
    }
    
    @Override
    public void display() {
        boolean exit = false;
        
        while (!exit) {
            printHeader("MAIN MENU - " + currentUser.getRole().getDisplayName());
            
            // Get and display menu options for current user
            Map<Integer, String> menuOptions = menuBuilder.buildMenu();
            
            // Display menu options
            for (Map.Entry<Integer, String> option : menuOptions.entrySet()) {
                System.out.println(option.getKey() + ". " + option.getValue());
            }
            
            System.out.print("\nEnter your choice: ");
            int choice;
            try {
                choice = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                showError("Invalid input. Please enter a number.");
                continue;
            }
            
            // Handle navigation
            if (choice == 0) {
                showMessage("Logging out...");
                exit = true;
            } else {
                // Navigate to the selected option
                boolean continueExecution = menuNavigator.navigate(choice);
                if (!continueExecution) {
                    exit = true;
                }
            }
        }
    }
    
    @Override
    public void refreshData() {
        // Nothing to refresh in main menu
    }
    
    @Override
    public boolean handleNavigation(int option) {
        // Now delegated to MenuNavigator
        return true;
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