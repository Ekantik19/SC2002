package controller.interfaces;

import model.HDBOfficer;
import model.enums.FlatType;

/**
 * Interface for Booking Controller in the BTO Management System.
 * Defines methods to manage flat bookings.
 * 
 * @author Your Name
 * @version 1.0
 */
public interface IBookingController {
    
    /**
     * Books a flat for an approved application.
     * 
     * @param applicationId The ID of the application to book a flat for
     * @param officer The officer booking the flat
     * @return true if the booking was successful, false otherwise
     */
    boolean bookFlat(String applicationId, HDBOfficer officer);
    
    /**
     * Updates the remaining flat count after a booking.
     * 
     * @param projectId The project ID
     * @param flatType The type of flat booked
     * @return true if update successful, false otherwise
     */
    boolean updateFlatAvailability(String projectId, FlatType flatType);
}