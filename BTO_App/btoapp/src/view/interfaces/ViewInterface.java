package view.interfaces;

/**
 * A contract for views in the BTO Management System to implement.
 * Defines core rendering methods for application navigation and display.
 * 
 * @version 1.0
 */
public interface ViewInterface {

    /**
     * Shows a message to the user.
     *
     * @param message The message to display
     */
    void showMessage(String message);
    /**
     * Shows an error message to the user.
     *
     * @param error The error message to display
     */
    void showError(String error);
}