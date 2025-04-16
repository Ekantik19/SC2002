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
        int nextOption = 1;
        
        menuOptions.put(nextOption++, "View Available Projects");
        menuOptions.put(nextOption++, "Register for Project");
        menuOptions.put(nextOption++, "View Registration Status");
        menuOptions.put(nextOption++, "View Assigned Project Details");
        menuOptions.put(nextOption++, "Process Flat Booking");
        menuOptions.put(nextOption++, "Generate Booking Receipt");
        menuOptions.put(nextOption++, "View and Reply to Enquiries");
        
        // Show applicant options if the officer is also an applicant
        if (officer.hasActiveApplication()) {
            menuOptions.put(nextOption++, "View My Application (as Applicant)");
            menuOptions.put(nextOption++, "Submit Application Withdrawal (as Applicant)");
        }
        
        menuOptions.put(nextOption++, "Change Password");
        menuOptions.put(0, "Logout");
        
        return menuOptions;
    }
}