package co.uk.wxApp.controllers;

/**
 * Common methods for swiping left/right
 * @author Ben LR
 */
public interface Swipeable {
	
	/**
	 * Called when a valid left or right swipe is made (see <code>Swiper</code> class) <br />
	 * @param integerToChange the integer changed by swipe action that will affect the GUI
	 */
	abstract void updateGUI(int integerToChange);

}
