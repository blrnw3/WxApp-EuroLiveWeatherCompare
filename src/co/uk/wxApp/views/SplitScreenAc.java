package co.uk.wxApp.views;

import java.io.IOException;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.util.TypedValue;
import android.view.Display;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import co.uk.wxApp.R;
import co.uk.wxApp.controllers.Controller;
import co.uk.wxApp.controllers.DBAdapter;
import co.uk.wxApp.controllers.Swipeable;
import co.uk.wxApp.controllers.Swiper;
import co.uk.wxApp.models.Data;
import co.uk.wxApp.models.Library;
import co.uk.wxApp.models.UpdaterService;

/**
 * The main screen/Activity. The launcher destination and home to the tile view comparing the weather in nine European cities.
 * @version 1.0, Jan 2013
 * @author Ben Lee-Rodgers <br />
 * <b>Sources:</b><ul>
 * <li>Settings: http://developer.android.com/guide/topics/data/data-storage.html</li>
 * <li>Services inc. binding and broadcasters: Beginning Android Application Development - WEI-MENG LEE, 2012</li>
 * <li>Preferences: Android developer guide</li>
 * <li>Concept of pre-loaded database - android book</li>
 * <li>Getting screen dimensions: //http://stackoverflow.com/questions/1016896/android-how-to-get-screen-dimensions</li>
 * </ul>
 */
public class SplitScreenAc extends Activity implements OnClickListener, Swipeable {
	
	private UpdaterService serviceBinder;
	private Intent intent;
	private Context cx;
	private IntentFilter intentFilter;

	private int wxVariable;

	private DBAdapter db;

	private ProgressBar loadingBar;
	private TextView[] locationData;
	private ImageButton[] locationBtns;
	private ImageButton[] variableBtns;

	/**
	 * CONSIDER MOVING THIS STRUCTURE TO THE DATA CLASS SINGLETON
	 */
	private String[][] data = new String[Data.CITY_LIMIT][Data.VARIABLE_NUM];

	private ServiceConnection connection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			//---called when the connection is made---
			serviceBinder = ((UpdaterService.MyBinder)service).getService();
			//---assign the objects to the service through the serviceBinder object---
			serviceBinder.db = db;
			serviceBinder.data = data;
			serviceBinder.cx = cx;
			serviceBinder.loadingBar = loadingBar;
			startService(intent);

			//Initialise data with that last stored while service is updating from Web
			db.open();
			serviceBinder.getDatafromDB();
			db.close();
		}
		public void onServiceDisconnected(ComponentName className) {
			serviceBinder = null;
		}
	};

	private GestureDetector gestureDetector;
	View.OnTouchListener gestureListener;
	
//	private GestureLibrary gestureLib;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
//		System.out.println("ACTIVITY CREATED");
		
		loadingBar = (ProgressBar) findViewById(R.id.progressBarUpdating);
		
		screenResolutionFixes();
		
		setupGestureDetection();

		//Service binding
		intentFilter = new IntentFilter();
		intentFilter.addAction("UPDATED");
		intentFilter.addAction("STARTED");
		registerReceiver(intentReceiver, intentFilter);
		
		setupButtons();

		onAppInstall();
		
		db = new DBAdapter(this.getApplicationContext());

		if(serviceBinder != null && serviceBinder.data != null) {
//			System.out.println("SERVICE ALREADY STARTED");
		} else {
			launchService();
		}
	}

	private void setupGestureDetection() {
		gestureDetector = new GestureDetector(this.getApplicationContext(),
				new Swiper(wxVariable, Data.VARIABLE_NUM - 2, this));
		gestureListener = new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				return gestureDetector.onTouchEvent(event);
			}
		};

		LinearLayout screen = (LinearLayout) findViewById(R.id.LinLayoutScreen);
		screen.setOnClickListener(this);
		screen.setOnTouchListener(gestureListener);
	}

	/**
	 * An attempt to improve the experience for different screen resolutions without
	 * 	resorting to writing out 10 different layouts in xml.
	 */
	private void screenResolutionFixes() {
		//Get Screen resolution
		Display display = getWindowManager().getDefaultDisplay(); 
		int w = display.getWidth();
		int h = display.getHeight();
		
//		System.out.println("Width: " + w + ", Height: " + h);
		
		//Fix text size relative to a medium-size screen, using a 50% weighting
		//	on the relative difference in screen dimensions sum
		int size = 14 * (1 + ( ((w + h) / 1280) - 1) / 2 );
		Data.getInstance().setTextSize( Data.textSizes.MEDIUM, size );
		Data.getInstance().setTextSize( Data.textSizes.SMALL, (float) (size * 0.8) );
		Data.getInstance().setTextSize( Data.textSizes.LARGE, (float) (size * 1.3) );
		
		//Fix the cut-off of headings issue that occurs on very small screens
		if(w + h < 600) {
			//http://stackoverflow.com/questions/4472429/change-the-right-margin-of-a-view-programmatically
			LinearLayout ll = (LinearLayout) findViewById(R.id.varBar);
			LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) ll.getLayoutParams();
			params.setMargins(0, 0, 0, 1); //left, top, right, bottom
			ll.setLayoutParams(params);
		}
	}

	private void setupButtons() {
		int[] locationBtnIDs = { R.id.ButtonNW, R.id.ButtonN, R.id.ButtonNE,
			R.id.ButtonW, R.id.ButtonC, R.id.ButtonE,
			R.id.ButtonSW, R.id.ButtonS, R.id.ButtonSE };

		int[] locationDataIDs = { R.id.textViewNW, R.id.textViewN, R.id.textViewNE,
			R.id.textViewW, R.id.textViewC, R.id.textViewE,
			R.id.textViewSW, R.id.textViewS, R.id.textViewSE };

		locationBtns = new ImageButton[Data.CITY_LIMIT];
		locationData = new TextView[Data.CITY_LIMIT];

		for(int i = 0; i < Data.CITY_LIMIT; i++) {
			locationBtns[i] = (ImageButton) findViewById(locationBtnIDs[i]);
			locationData[i] = (TextView) findViewById(locationDataIDs[i]);
		}

		variableBtns = new ImageButton[] {
				(ImageButton) findViewById(R.id.imageButtonTp), //temp
				(ImageButton) findViewById(R.id.imageButtonRn), //rain
				(ImageButton) findViewById(R.id.imageButtonWn), //wind
				(ImageButton) findViewById(R.id.imageButtonRh), //hum
				(ImageButton) findViewById(R.id.imageButtonPr), //pressure
				(ImageButton) findViewById(R.id.imageButtonSk), //sky
		};
		
		for(int i = 0; i < variableBtns.length; i++) {
			variableBtns[i].setOnClickListener( new VariableBtnListener(i) );
			variableBtns[i].setOnTouchListener(gestureListener);
		}

		for(int i = 0; i < locationBtns.length; i++) {
			locationBtns[i].setOnClickListener( new CityBtnListener(i) );
			locationBtns[i].setOnTouchListener(gestureListener);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		Data.getInstance().updateSettings(cx);
		if(serviceBinder != null) { //Need to update the poll rate, and get new city if returning from map
			boolean type = Data.getInstance().isUpdated();
			serviceBinder.updateData(type, !type);
		}
		if(data[0][0] != null) {
			updateGUI(wxVariable);
		}
	}

	private BroadcastReceiver intentReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			updateGUI(wxVariable);
//			System.out.println("BROADCAST RECEIVED");
		}
	};
	

	public void launchActivity(View v) {
//		System.out.println( v.getId() );
		Controller.activityLauncher(v.getId(), cx);
	}


	public void launchDetail(int city) {
		Intent intent = new Intent(this, DetailWxAc.class);
		for(int i = 0; i < data.length; i++) { // can't pass 2D arrays
			intent.putExtra("data"+i, data[i]);
		}
		intent.putExtra("city", city);
		startActivity(intent);
	}

	private void launchService() {
		cx = this.getApplicationContext();
		intent = new Intent(this, UpdaterService.class);
		bindService(intent, connection, Context.BIND_AUTO_CREATE);
	}

	public void backgroundDataUpdate(View v) {
		if(serviceBinder != null) {
			serviceBinder.updateData(true, false);
		}
	}

	private void onAppInstall() {
		String destPath = "/data/data/" + getPackageName() + "/databases/";
		//copy the db from the assets folder into the databases folder, where it can be used
		try {
			Library.copyFileFromAssets( Data.DB_NAME, destPath, getBaseContext() );
		} catch (IOException e) {}
	}

	 public void updateGUI(int newInteger) {
		 wxVariable = newInteger;
		//Tiles
		for(int i = 0; i < Data.CITY_LIMIT; i++) {
			int order = Data.getInstance().getOrders()[i];
			double value = Double.parseDouble( data[order][wxVariable] );
			String city = Data.getInstance().getCity(order);
			String hexColour = "aaaaaa";
			String dataText = "N/A";

			if(wxVariable == 5) { //weather conditions icon
				int icon = Data.WX_ICONS[(int) value];

				locationBtns[i].setBackgroundColor( Color.parseColor("#789aef") );
				locationData[i].setText(city);
				locationData[i].setTextColor(Color.BLACK);
				locationBtns[i].setImageResource(icon);
			}
			else {
				int level = Library.valueColour(value, wxVariable);

				if(value > -100) { //value is not faulty
					hexColour = Data.DATA_COLOURS[wxVariable][level];
					dataText = Data.getInstance().convert(value, wxVariable);
				}

				int text_colour = Data.DATA_TEXT_ISWHITES[wxVariable][level] ? Color.WHITE : Color.BLACK;
				locationBtns[i].setImageResource(R.drawable.blank);
				locationBtns[i].setBackgroundColor( Color.parseColor("#" + hexColour) );
				locationData[i].setText(dataText + "\n\n" + city);
				locationData[i].setTextColor(text_colour);
				locationData[i].setTextSize( TypedValue.COMPLEX_UNIT_DIP,
						Data.getInstance().getTextSize(Data.textSizes.MEDIUM) );
			}
		}

		//Weather variable button bar
		for(ImageButton b : variableBtns) {
			b.setBackgroundDrawable(null); //set to transparent
		}
		variableBtns[wxVariable].setBackgroundColor( Color.parseColor("#ddccdd") ); //highlight selected var

		//Headings
		TextView heading = (TextView) findViewById(R.id.textViewHeading);
		heading.setText("Latest " + Data.VARIABLE_NAMES[wxVariable]);
		heading.setTextSize( TypedValue.COMPLEX_UNIT_DIP, 
				Data.getInstance().getTextSize(Data.textSizes.LARGE) );
		
		TextView subhead = (TextView) findViewById(R.id.textViewTime);
		subhead.setText( "Last updated: " + Library.dateFormat( data[0][Data.VARIABLE_NUM-1]) );
		subhead.setTextSize( TypedValue.COMPLEX_UNIT_DIP, 
				Data.getInstance().getTextSize(Data.textSizes.MEDIUM) );
	}

	private class VariableBtnListener implements OnClickListener {
		int type;

		public VariableBtnListener(int type) {
			this.type = type;
		}

		public void onClick(View v) {
			wxVariable = type;
			updateGUI(wxVariable);
		}
	}

	private class CityBtnListener implements OnClickListener {
		int type;

		public CityBtnListener(int type) {
			this.type = type;
		}

		public void onClick(View v) {
			launchDetail(type);
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
//		System.out.println("ACTIVITY STOPPED");
		Data.getInstance().setToastable(false); //don't want toasts outside of app domain
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
//		System.out.println("ACTIVITY DESTROYED");
		//Data.getInstance().setKillable(true);

		serviceBinder.stopService(intent); // May need to do this to conserve battery
		unregisterReceiver(intentReceiver);
		unbindService(connection);
	}

	public void onClick(View v) {
		//Needed by OnClickListener
	}

}