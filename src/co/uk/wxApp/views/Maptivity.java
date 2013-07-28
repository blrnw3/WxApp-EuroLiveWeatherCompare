package co.uk.wxApp.views;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.Toast;
import co.uk.wxApp.R;
import co.uk.wxApp.controllers.DBAdapter;
import co.uk.wxApp.models.Data;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

/** 
 * The map scren for chnaging the user's city collection
 * @author Ben LR<br />
 * Sources: Beginning Android Application Development - WEI-MENG LEE, 2012 (chapter 9),
 *  and Google (https://developers.google.com/maps/documentation/android/v1/hello-mapview)
  */
public class Maptivity extends MapActivity {
	
	private MapController mc;
	private GeoPoint gp;
	private DBAdapter db;
	private MapView mapView;
	
	//City data
	private int size = 111;
	private String[] names = new String[size];
	private String[] countries = new String[size];
	private double[] lats = new double[size];
	private double[] lngs = new double[size];
	private boolean[] isInUse = new boolean[size];
	private String[] selectedCities = new String[Data.CITY_LIMIT];

	private class MapOverlay extends com.google.android.maps.ItemizedOverlay {
		
		private boolean isChangeable;
		private String newCityName;
		private Context context;
		private ArrayList<OverlayItem> overlays = new ArrayList<OverlayItem>();
		private AlertDialog.Builder selectDialog;
		
		private OnClickListener cityDeselectorListener = new OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				String oldCityName = selectedCities[which];
				selectedCities[which] = newCityName;
				Toast.makeText(context, "You have unset " + oldCityName, Toast.LENGTH_SHORT).show();
				Data.getInstance().setCity(newCityName, which);
				Data.getInstance().setUpdated(true);
				
				//update markers
				int index = Arrays.asList(names).indexOf(newCityName);
				isInUse[index] = true;
//				System.out.println("index: " + index + " - " + names[index]);
				isInUse[ Arrays.asList(names).indexOf(oldCityName) ] = false;
				addLocationMarkers();
				mc.animateTo( new GeoPoint( fullify(lats[index]), fullify(lngs[index]) ) );
//				System.out.println(Data.getInstance().getCity(which) + " index " + which);
			}
		};
		
		/**
		 * @param defaultMarker the pin/marker image
		 * @param context
		 * @param isChangeable is this overlay to be used for current or un-used city markers
		 */
		public MapOverlay(Drawable defaultMarker, Context context, boolean isChangeable) {
			  super(boundCenterBottom(defaultMarker));
			  this.context = context;
			  this.isChangeable = isChangeable;
			  if(isChangeable) {
				  for(int i = 0; i < Data.CITY_LIMIT; i++) {
					  selectedCities[i] = Data.getInstance().getCity(i);
				  }

				  selectDialog = new AlertDialog.Builder(context);
				  selectDialog.setTitle("Which city do you wish to unset?");
				  selectDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					  public void onClick(DialogInterface dialog, int whichButton) {
					  }
				  });
				  selectDialog.setItems(selectedCities, cityDeselectorListener);
			  }
		}
		
		/**
		 * Adds or removes a single marker from a given overlay
		 * @param overlay
		 * @param actionIsAdd add the marker, or remove
		 */
		public void changeOverlay(OverlayItem overlay, boolean actionIsAdd) {
			if(actionIsAdd) {
				overlays.add(overlay);
			} else {
				overlays.remove(overlay);
			}
			populate();
		}
		
		@Override
		protected OverlayItem createItem(int i) {
			return overlays.get(i);
		}
		
		@Override
		public int size() {
			return overlays.size();
		}
		
		@Override
		protected boolean onTap(int index) {
			OverlayItem item = overlays.get(index);
			newCityName = item.getSnippet();
			AlertDialog.Builder dialog = new AlertDialog.Builder(context);
			dialog.setTitle(newCityName + ", " + item.getTitle());
			String message = isChangeable ? "Set this as a new city?" : 
				"This city is currently set";
			dialog.setMessage(message);
			
			if(isChangeable) {
				dialog.setPositiveButton("Set", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						dialog.dismiss();
						selectDialog.show();
					}
				});
			}
			dialog.setNegativeButton("Dismiss", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
				}
			});
			dialog.show();
			return true;
		}

	}
	
	private void getDatafromDB() {
		Cursor c = db.getAllData(false);
		if( c.moveToFirst() ) { //if data exists in database
			int i = 0;
			do {
				names[i] = c.getString(0);
				isInUse[i] = isSelectedCity(names[i]);
				countries[i] = c.getString(1);
				lats[i] = c.getDouble(2);
				lngs[i] = c.getDouble(3);
				i++;
			} while(c.moveToNext());
			size = i;
//			System.out.println("Size: " + size);
		}
//		System.out.println(Arrays.toString(names));
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.maptivity);

		db = new DBAdapter(this.getApplicationContext());
		db.open();
		getDatafromDB();
		db.close();
		
		mapView = (MapView) findViewById(R.id.mapView);
		
		setupMap();

		addLocationMarkers();
	}

	private void setupMap() {
		mapView.setBuiltInZoomControls(true);
		mc = mapView.getController();
		gp = new GeoPoint(50000000, 10000000); // centre map on Europe
		mc.animateTo(gp);
		mc.setZoom(6);
	}

	protected void addLocationMarkers() {
		List<Overlay> listOfOverlays = mapView.getOverlays();
		listOfOverlays.clear();
		
		Drawable unusedMarker = this.getResources().getDrawable(R.drawable.pin_s);
		MapOverlay unusedMapOverlay = new MapOverlay(unusedMarker, this, true);
		listOfOverlays.add(unusedMapOverlay);
		
		Drawable usedMarker = this.getResources().getDrawable(R.drawable.pin_s_cur);
		MapOverlay usedMapOverlay = new MapOverlay(usedMarker, this, false);
		listOfOverlays.add(usedMapOverlay);
		
		for(int i = 0; i < size; i++) {
			GeoPoint point = new GeoPoint(fullify(lats[i]), fullify(lngs[i]) );
			OverlayItem overlayitem = new OverlayItem(point, countries[i], names[i]);
			
			if(isInUse[i]) {
				usedMapOverlay.changeOverlay(overlayitem, true);
			} else {
				unusedMapOverlay.changeOverlay(overlayitem, true);
			}
		}
	}

	private boolean isSelectedCity(String name) {
		for(int i = 0; i < Data.CITY_LIMIT; i++) {
			if(Data.getInstance().getCity(i).equals(name)) {
				return true;
			}
		}
		return false;
	}

	private int fullify(double d) {
		return (int) (1000000 * d);
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

}