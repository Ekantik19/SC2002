package com.bto.view.interfaces;

/**
 * Interface for role-based access control in views.
 * Provides methods for handling role-specific operations.
 */
public interface RoleBasedViewInterface {
    /**
     * Checks if the current user has the required role.
     * 
     * @param userRole The role to check against
     * @param message The message to display if access is denied
     * @return true if user has the role, false otherwise
     */
    boolean checkAccess(Class<?> userRole, String message);
    
    /**
     * Shows access denied message and returns to menu.
     * 
     * @param message The message to display
     */
    void showAccessDenied(String message);
}