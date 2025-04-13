package view.menu;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Menu builder for HDB Manager users.
 */
public class ManagerMenuBuilder implements MenuBuilder {
    @Override
    public Map<Integer, String> buildMenu() {
        Map<Integer, String> menuOptions = new LinkedHashMap<>();
        
        menuOptions.put(1, "Create New Project");
        menuOptions.put(2, "View All Projects");
        menuOptions.put(3, "View My Projects");
        menuOptions.put(4, "Update Project Details");
        menuOptions.put(5, "Toggle Project Visibility");
        menuOptions.put(6, "Manage Officer Registrations");
        menuOptions.put(7, "Manage Applications");
        menuOptions.put(8, "Manage Withdrawal Requests");
        menuOptions.put(9, "Generate Reports");
        menuOptions.put(10, "View and Reply to Enquiries");
        menuOptions.put(11, "Change Password");
        menuOptions.put(0, "Logout");
        
        return menuOptions;
    }
}
