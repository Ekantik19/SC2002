package view.interfaces;

/**
 * A contract for views in the BTO Management System to implement.
 * Defines core rendering methods for application navigation and display.
 * 
 * @version 1.0
 */
public interface ViewInterface {
    void display();
    void showMessage(String message);
    void showError(String error);
}