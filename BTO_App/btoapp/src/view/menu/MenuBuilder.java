package view.menu;

import java.util.Map;

/**
 * Interface for menu builders.
 * Builds menu options based on user role.
 */
public interface MenuBuilder {
    /**
     * Builds the menu options.
     * 
     * @return Map of option numbers to option descriptions
     */
    Map<Integer, String> buildMenu();
}