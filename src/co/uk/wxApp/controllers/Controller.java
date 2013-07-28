package co.uk.wxApp.controllers;

import co.uk.wxApp.R;
import co.uk.wxApp.views.HelpAc;
import co.uk.wxApp.views.Maptivity;
import co.uk.wxApp.views.Prefs;
import co.uk.wxApp.views.RankAc;
import android.content.Context;
import android.content.Intent;

/**
 * Handles Activity Launching
 * @author Ben LR
 *
 */
public class Controller {
	
	public static void activityLauncher(int id, Context c) {
		Class classID = null;
		switch(id) {
			case (R.id.settingsBtn):
			case (R.id.menu_settings):
				classID = Prefs.class;
			break;
			case (R.id.HelpBtn):
			case (R.id.menu_help):
				classID = HelpAc.class;
			break;
			case (R.id.LocationsBtn2):
				classID = Maptivity.class;
			break;
			case (R.id.RankPageBtn):
				classID = RankAc.class;
			break;
		}
		if(classID != null) {
			Intent intent = new Intent(c, classID);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			c.startActivity(intent);
		} else {
//			System.err.println("No valid Activity found");
		}
	}

}
