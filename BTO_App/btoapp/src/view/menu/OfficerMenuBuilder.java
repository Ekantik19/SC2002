package view.menu;

import java.util.LinkedHashMap;
import java.util.Map;

import model.HDBOfficer;

/**
 * Menu builder for HDB Officer users.
 */
public class OfficerMenuBuilder implements MenuBuilder {
    private HDBOfficer officer;
    
    /**
     * Constructor for OfficerMenuBuilder.
     * 
     * @param officer The HDB Officer
     */
    public OfficerMenuBuilder(HDBOfficer officer) {
        this.officer = officer;
    }
    
    @Override
    public Map<Integer, String> buildMenu() {
        Map<Integer, String> menuOptions = new LinkedHashMap<>();
        
        menuOptions.put(1, "View Available Projects");
        menuOptions.put(2, "Register for Project");
        menuOptions.put(3, "View Registration Status");
        menuOptions.put(4, "View Assigned Project Details");
        menuOptions.put(5, "Process Flat Booking");
        menuOptions.put(6, "Generate Booking Receipt");
        menuOptions.put(7, "View and Reply to Enquiries");
        
        // Show applicant options if the officer is also an applicant
        if (officer.hasActiveApplication()) {
            menuOptions.put(8, "View My Application (as Applicant)");
            menuOptions.put(9, "Submit Application Withdrawal (as Applicant)");
        }
        
        menuOptions.put(10, "Change Password");
        menuOptions.put(0, "Logout");
        
        return menuOptions;
    }
}