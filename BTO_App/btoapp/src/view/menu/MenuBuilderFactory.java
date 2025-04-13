package view.menu;

import model.Applicant;
import model.HDBManager;
import model.HDBOfficer;
import model.User;

/**
 * Factory for creating menu builders based on user role.
 */
public class MenuBuilderFactory {
    /**
     * Creates a menu builder for the given user.
     * 
     * @param user The user
     * @return A menu builder for the user's role
     */
    public static MenuBuilder createMenuBuilder(User user) {
        if (user instanceof HDBManager) {
            return new ManagerMenuBuilder();
        } else if (user instanceof HDBOfficer) {
            return new OfficerMenuBuilder((HDBOfficer) user);
        } else if (user instanceof Applicant) {
            return new ApplicantMenuBuilder();
        } else {
            throw new IllegalArgumentException("Unknown user type");
        }
    }
}