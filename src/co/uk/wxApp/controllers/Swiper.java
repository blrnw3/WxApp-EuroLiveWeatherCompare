package co.uk.wxApp.controllers;

import android.view.MotionEvent;
import android.view.GestureDetector.SimpleOnGestureListener;

/**
 * Customised gesture listener to deal with simple left/right swiping to change an integer variable
 * and call a GUI update method from the Activity that received the swipe. <br />
 * e.g. for changing screens that display different content depending on an array index
 * 
 * @author Ben LR <br />
 * Inspired by http://stackoverflow.com/questions/937313/android-basic-gesture-detection
 */
public final class Swiper extends SimpleOnGestureListener {
	
	private int variableToChange;
	private int limit;
	private Swipeable s;
	
	/**
	 * @param variableToChange the integer variable to change on detection of swipe action
	 * @param limit the maximum value of that integer
	 * @param s the swipe-enabled <code>Activity</code> that implements <code>Swipeable</code>
	 */
	public Swiper(int variableToChange, int limit, Swipeable s) {
		this.variableToChange = variableToChange;
		this.s = s;
		this.limit = limit;
	}
	
	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		int minXDistance = 50;
		int maxYdeviation = 250;
		int minSpeed = 200;
		
		try {
			int distance = (int) (e1.getX() - e2.getX());
			if(Math.abs(distance) > minXDistance
					&& Math.abs(velocityX) > minSpeed
					&& Math.abs(e1.getY() - e2.getY()) < maxYdeviation) {
				changeInt(distance > 0);
				s.updateGUI(variableToChange);
			}
		} catch(Exception e) {}

		return false;
	}

	@Override
	public boolean onDown(MotionEvent e) {
		return false;
	}
	
	/** Change the integer variable up or down, and wrap around at the ends
	 * @param isIncrement direction of change
	*/
	private void changeInt(boolean isIncrement) {
		if(isIncrement) {
			variableToChange =  (variableToChange < limit) ? (variableToChange + 1) : 0;
		} else {
			variableToChange =  (variableToChange > 0) ? (variableToChange - 1) : limit;
		}
	}

}