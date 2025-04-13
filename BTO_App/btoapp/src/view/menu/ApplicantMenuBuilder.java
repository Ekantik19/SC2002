package view.menu;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Menu builder for Applicant users.
 */
public class ApplicantMenuBuilder implements MenuBuilder {
    @Override
    public Map<Integer, String> buildMenu() {
        Map<Integer, String> menuOptions = new LinkedHashMap<>();
        
        menuOptions.put(1, "View Available Projects");
        menuOptions.put(2, "View My Application");
        menuOptions.put(3, "Submit Application Withdrawal Request");
        menuOptions.put(4, "Create New Enquiry");
        menuOptions.put(5, "View My Enquiries");
        menuOptions.put(6, "Change Password");
        menuOptions.put(0, "Logout");
        
        return menuOptions;
    }
}