package co.uk.wxApp.models;

import java.util.Arrays;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import co.uk.wxApp.controllers.DBAdapter;
import co.uk.wxApp.controllers.Toaster;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.view.View;
import android.widget.ProgressBar;

/**
 * This service runs whenever the app is running. <br />
 * It serves to update the data used by the app, both on a schedule and whenever the user demands.
 * @author Ben Lee-Rodgers <br />
 * <b>Sources:</b>
 * <ul>
 * <li>wifi detection - http://stackoverflow.com/questions/3841317/how-to-see-if-wifi-is-connected-in-android </li>
 * <li>general concepts of a service, and its implementation with async and a timer, as well as using a local database
 *  - textbook: Beginning Android Application Development - WEI-MENG LEE, 2012 </li>
 * <li>calling toasts from timerTask - http://stackoverflow.com/questions/4025082/android-toast-started-from-service-only-displays-once </li>
 *</ul>
 */
public class UpdaterService extends Service {

	private final String SERVER_PATH = "http://nw3weather.co.uk/CP_Solutions/";
	
	//set up the binding to the main activity.
	private final IBinder binder = new MyBinder();
	public DBAdapter db;
	public Context cx;
	public ProgressBar loadingBar;
	public String[][] data;

	private final Timer timer = new Timer();
	private final Handler handler = new Handler();
	private final Intent broadcastIntent = new Intent();

	private boolean manually;
	private boolean manualUpdateOnly;
	private int locked;
	private int lastUpdate;
	private int timerCounter;
	private int timestamp;
//	private int overkill;
	private long start;
	private long end;
	
	private Toaster shortToast;
	private Toaster longToast;



	public class MyBinder extends Binder {
		public UpdaterService getService() {
			return UpdaterService.this;
		}
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return binder;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		broadcastIntent.setAction("STARTED");
		getBaseContext().sendBroadcast(broadcastIntent);
//		System.out.println("SEVICE STARTED");

		//If service is not already running, set up the scheduler
		if(shortToast == null) {
			scheduleUpdate(1); //every minute
		}
		shortToast = new Toaster(this, false);
		longToast = new Toaster(this, true);

		return START_NOT_STICKY;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		timer.cancel();
//		System.out.println("SERVICE DESTROYED");
	}
	
	
	/**
	 * Schedules the auto-updater
	 * @param frequency in minutes
	 */
	private void scheduleUpdate(int frequency) {
		timer.scheduleAtFixedRate( new UpdaterTimer(), 0, 60000 * frequency );
	}

	/** The timer that runs at the specified interval and makes calls to the data update procedure */
	private class UpdaterTimer extends TimerTask {
		@Override
		public void run() {
			handler.post(new Runnable() { //needed to make Toasts
				public void run() {
					updateData(false, false);
				}
			});
		}
	}

	/** Calls the AsyncTask if it is not already running or an update is urgent
	 * @param manually if the user instigated this
	 * @param override force an update
	 */
	public void updateData(boolean manually, boolean override) {
		this.manually = manually;
		if(locked > 0 && !override) {
			if(locked == 1) { //only give feedback on first press - prevents toasts stacking up
				if(manually) { 
					shortToast.pop("Update already in progress!"); 
				}
			}
		} else {
			new BackgroundUpdater(override).execute();
		}
		locked++;
	}
	

	/**
	 * The background task that performs data updates and provides feedback
	 * on the things that can go wrong (no network, web server down or failed to update). <br />
	 * Various schemes are implemented to reduce access to the server as much as possible.
	 * This is for both protection and resource-saving purposes of both the server and the device running the app.
	 * <br />Three checks are performed before the full server data is accessed (which updates every 30 mins):<br />
	 * <ul>
	 * <li>Is new data expected?</li>
	 * <li>Is there a network connection?</li>
	 * <li>Has the server really updated?</li>
	 * </ul>
	 * Failure at one of these points terminates checking of any that remain.
	 * If all these are passed, data is obtained from the server and passed to the local database. <br />
	 * <b>NB:</b> An important distinction is made between manual requests (user presses refresh), and
	 * automatic ones scheduled by the <code>TimerTask</code>. Manual requests bypass the first check
	 * and produce much more user feedback (in the form of Toasts).
	 * 
	 * @author Ben Lee-Rodgers
	 */
	private class BackgroundUpdater extends AsyncTask<Void, Integer, String> {
		private boolean toastable = Data.getInstance().toastable();
		
		public BackgroundUpdater(boolean override) {
			//Hide toasts when returning from another activity or app
			if(override && toastable) {
				Data.getInstance().setToastable(false);
			}
		}

		@Override
		protected String doInBackground(Void... params) {
			manualUpdateOnly = Data.getInstance().getUpdateFreq() > 1000;
			start = new Date().getTime();
			long timeDiff = start - end;
			//System.out.println( timeDiff + " ms since last backy");
			boolean tooQuick = timeDiff < 3000; //prevent server spamming
			if(tooQuick && manually) {
				System.out.println("Too quick! (" + (start-end) + " ms)");
				return null;
			}

			boolean newService = timeDiff > 1000000;
			//don't poll if don't need to or auto-update is off.
			if(!manually && !newService) {
				timerCounter--;
//				System.out.println("Timer cnt: " + timerCounter);
				if(timerCounter > 0 || manualUpdateOnly) {
					//System.out.println("not polling");
					return null;
				}
			} else { // Manual refresh bypasses this check
				publishProgress( (int) (new Date().getTime() - start) );
			}

			//Find network status and abandon update if no connection
			boolean isNetworked;
			ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
			NetworkInfo allInfo = cm.getActiveNetworkInfo();
			isNetworked = ( allInfo != null && allInfo.isConnected() );

			if(isNetworked && Data.getInstance().isWifiOnly() && !manually && !newService) {
				//Override network status with wifi-only status (ignore roaming)
				NetworkInfo wifiInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
				isNetworked = ( wifiInfo != null && wifiInfo.isConnected() );
			}
//			System.out.println("isNetworked: " + Boolean.toString(isNetworked));

			//If local network is good, test out the server and get its accurate time
			timestamp = isNetworked ? Library.readWebpageToInt(SERVER_PATH  + "SQLdown.php?time") : 0;
			if(timestamp < 1) { //Problem detected!
//				String type = Data.getInstance().isWifiOnly() ? "Wifi " : "";
//				System.out.println("No connection on " + type);
				return (manually || newService) ? "Network connection failed!" : null;
			}

			updatePollrate();

			//Perform final check on server to see whether it has really updated
			int serverUpdateTime = Library.readWebpageToInt(SERVER_PATH  + "updated.txt");
			if( serverUpdateTime - lastUpdate < 300 // no new data
					&& !Data.getInstance().isUpdated() ) {  // no change in cities
//				System.out.println("Diff: " + (serverUpdateTime - lastUpdate) +
//						"; server update time: " + serverUpdateTime +
//						"; last update: " + lastUpdate);
				int nextUpdate = (int) Math.ceil( Data.SERVER_FREQ  - (timestamp - serverUpdateTime) / 60 );
				return (manually || timerCounter == -1  || newService) ?
						"No new data.\nUpdate expected in: "  + nextUpdate + " minutes" : null;
			}

			if(!manually) { //manual has previously published
				publishProgress( (int) (new Date().getTime() - start) );
			}

			//Finally, get real, new data from the server
			String query = "SQLdown.php?city=";
			for(int i = 0; i < Data.CITY_LIMIT; i++) {
				query += Data.getInstance().getCity(i).replaceAll(" ", "+") + ",";
			}
			String[] stations = Library.readWebpageToArray(SERVER_PATH  + query);
			//System.out.println( Arrays.toString(stations) );

			//Update local database with this data
			db.open();
			db.update(stations);
			getDatafromDB();
			db.close();

			broadcastIntent.setAction("UPDATED"); //need to update the GUI
			getBaseContext().sendBroadcast(broadcastIntent);

//			if(Data.getInstance().isKillable()) { //Limit updates if app has closed (service runs for 10 updates)
//				overkill++;
//				System.out.println("Overkill: " + overkill);
//			}

			return "Update complete ";
		}

		@Override
		protected void onProgressUpdate(Integer... progress) {
			shortToast.pop("Updating data..."
				);//	+ progress[0] + " ms elapsed for logic"); //only for debugging
			loadingBar.setVisibility(View.VISIBLE);
		}

		@Override
		protected void onPostExecute(String result) {
			end = new Date().getTime();
			if(result != null) { //hide most auto Toasts
				longToast.pop(result 
						);//+ " (" + (end - start) + " ms)"); //only for debugging);
				loadingBar.setVisibility(View.INVISIBLE);
			}

//			if(overkill > 10) {
//				stopSelf();
//			}

			if(!manually) { //Prevent tooQuick returning true if automatic was last executor
				end -= 3000;
			}

			Data.getInstance().setToastable(toastable);
			if(Data.getInstance().isUpdated()) {
				Data.getInstance().setUpdated(false);
			}
			
			locked = 0; //Allow another Async instance
		}
	}
	
	
	/** Populate the data array using the newly-updated local database */
	public void getDatafromDB() {
		Cursor c = db.getAllData(true);
		if( c.moveToFirst() ) {
			for(int i = 0; i < Data.CITY_LIMIT; i++) {
				for(int j = 0; j < Data.VARIABLE_NUM; j++) {
					data[i][j] = c.getString(j+2);
				}
				Data.getInstance().setCity(c.getString(1), i);
				c.moveToNext();
			}
		}
		getLatLongs();
		updatePollrate();
	}

	/** Set the ordering of the cities based on their co-ordinates (obtained from the local db) */
	public void getLatLongs() {
		double[] lats = new double[Data.CITY_LIMIT];
		double[] longs = new double[Data.CITY_LIMIT];

		for(int i = 0; i < Data.CITY_LIMIT; i++) {
			String city = Data.getInstance().getCity(i);
			Cursor c = db.getCityInfo(city);

			if(c.moveToFirst()) {
				lats[i] = c.getDouble(0);
				longs[i] = c.getDouble(1);
			} else {
				lats[i] = i;
				longs[i] = i;
//				System.out.println("Mega fail on retrieving lat long data");
			}
		}
		Data.getInstance().setOrders( Library.citySorter(lats, longs) );
	}

	/** Use accurate server time to get the time to its next update */
	private void updatePollrate() {
		lastUpdate = Integer.parseInt(data[0][Data.VARIABLE_NUM-1]);
		int timeDiff = timestamp - lastUpdate;
//		System.out.println("recency: " + timeDiff);
		timerCounter = (int) (Data.getInstance().getUpdateFreq() - Math.abs( Math.floor(timeDiff / 60) )) + 1;
//		System.out.println("New timer cnt: " + timerCounter);
	}
}