package controller.interfaces;

import model.Application;
import model.HDBOfficer;
import model.Receipt;
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
     * Generates a receipt for a booked flat.
     * 
     * @param applicationId The ID of the booked application
     * @param officer The officer generating the receipt
     * @return The receipt for the booking, or null if booking not found
     */
    Receipt generateBookingReceipt(String applicationId, HDBOfficer officer);
    
    /**
     * Gets booking information for an application.
     * 
     * @param applicationId The ID of the application
     * @return The application with booking details if available, null otherwise
     */
    Application getBookingDetails(String applicationId);
    
    /**
     * Updates the remaining flat count after a booking.
     * 
     * @param projectId The project ID
     * @param flatType The type of flat booked
     * @return true if update successful, false otherwise
     */
    boolean updateFlatAvailability(String projectId, FlatType flatType);
}