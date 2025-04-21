package view.menu;

/**
 *
 * Interface on what can be done on the menu view
 */
public interface MenuAction {
    /**
     * Executes the menu action.
     * 
     * @return true to continue execution, false to exit
     */
    boolean execute();
}