package view.menu;

public interface MenuAction {
    /**
     * Executes the menu action.
     * 
     * @return true to continue execution, false to exit
     */
    boolean execute();
}