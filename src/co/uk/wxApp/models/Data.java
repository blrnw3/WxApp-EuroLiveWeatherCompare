package co.uk.wxApp.models;

import java.util.Arrays;
import co.uk.wxApp.R;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * The 'M' (Model)in the MVC design pattern (but with some Controller-ness). Also uses the Singleton concept. <br />
 * It stores and sets the application settings, and any globally needed constants.
 * 	A unit conversion method is placed here for convenience.
 * @author Ben Lee-Rodgers
 */
public final class Data {
	
	//Some constants used for the colour-coding
	private final static String[] tempcols = {"000093","0000cc","035CB5","10AFE4", "00FFC4","07EA57","C8FB11", "FFDE00","FF9933","F65A17", "DA103C" };
	private final static String[] humicols = { "F3E2A9","F7D358","FFBF00", "D7DF01","A5DF00","74DF00", "31B404","329511","0B6121" };
	private final static String[] presscols = tempcols;
	private final static String[] windcols = { "D9FDFC","B1FFFF","66FF94", "99FF00","99CC00","CCCC00", "FFCC00","FF9900","FF6600", "FF0000" };
	private final static String[] raincols = { "94939A","918AA7","CCFFFF", "99FFCC","9EDFFD","9AACFF", "7980FF","3F48F9","010EFE", "050EAB","050D97","0B0B3B" };

	private final static boolean[] temptxts = { true,true,true,false, false,false,false, false,false,true, true };
	private final static boolean[] humitxts = { false,false,false, false,false,false, false,false, true };
	private final static boolean[] presstxts = temptxts;
	private final static boolean[] windtxts = { false,false,false, false,false,false, false,false,false, true };
	private final static boolean[] raintxts = { false,false,false, false,false,false, false,true,true, true,true,true };

	private final static double[] tempvals = { -10,-5,0,5, 10,15,20, 25,30,35 };
	private final static double[] humivals = { 30,40,50, 60,70,80, 90,97 };
	private final static double[] presvals = { 970,980,990, 1000,1010,1015, 1020,1030,1040 };
	private final static double[] windvals = { 1,2,4, 7,10,15, 20,30,40 };
	private final static double[] rainvals = { 0,0.2,0.6, 1,2,5, 10,15,20, 25,50 };

	//Collect into globally-available arrays
	/** Background colours of tiles */
	public final static String[][] DATA_COLOURS = { tempcols, raincols, windcols, humicols, presscols };
	/** Foreground text-colour on tiles */
	public final static boolean[][] DATA_TEXT_ISWHITES = { temptxts, raintxts, windtxts, humitxts, presstxts };
	/** Limits for each colour band */
	public final static double[][] DATA_THRESHOLDS = { tempvals, rainvals, windvals, humivals, presvals };

//	public static final String SETTINGS_FILENAME = "settings";
	/** Local SQLite database name */
	public static final String DB_NAME = "wxapp4";
	/** Frequency in minutes used by the web server to update its database */
	public static final int SERVER_FREQ = 30;
	
	/** Number of cities displayed at one time */
	public static final int CITY_LIMIT = 9;
	/** Names of all the weather variables */
	public static final String[] VARIABLE_NAMES = { "Temperature", "24hr Rainfall", "Wind Speed",
		"Humidity", "Pressure", "Weather Conds", "Last Updated" };
	/** Number of different weather variables*/
	public static final int VARIABLE_NUM = VARIABLE_NAMES.length;

	/** Icons used by the 'condition' weather variable */
	public static final int[] WX_ICONS = { R.drawable.clear, R.drawable.nt_clear, R.drawable.partlycloudy,
		R.drawable.nt_partlycloudy, R.drawable.cloudy, R.drawable.rain,
		R.drawable.snow, R.drawable.fog6, R.drawable.tstorms };
	/** Labels for each icon used by the 'condition' weather variable*/
	public static final String[] WX_ICON_LABELS = { "Clear / Sunny", "Clear Night", "Partly Cloudy",
		"Partly Cloudy", "Cloudy / Overcast", "Rain / Showers",
		"Snow / Ice", "Foggy / Hazy / Misty", "Thundery / T-Storm" };
	
	/** Text sizes used where scaling based on screen size is necessary */
	public static enum textSizes { SMALL, MEDIUM, LARGE };
	private float textSizeSmall;
	private float textSize;
	private float textSizeLarge;
	
	private boolean unitTisMetric;
	private boolean unitRisMetric;
	private boolean unitWisMetric;
	private boolean unitWisKnots;
	private boolean unitPisMetric;
	private String unitW;
	
	private String[] cities;
	private int[] orders;
	
	private int updateFreq;
	private boolean wifiOnly;
	private boolean showToasts;
	private boolean updated;
	
	private static Data instance;

	private Data() {
		cities = new String[CITY_LIMIT];
	}
	
	/** Implementation of singleton design pattern (useful in Android as it is difficult to pass
	 * references to objects between activities */
	public static Data getInstance() {
		if(instance == null) {
			instance = new Data();
		}
		return instance;
	}

	//## Units ##//
	public boolean isUnitTisMetric() {
		return unitTisMetric;
	}
	public void setUnitTisMetric(boolean unitTisMetric) {
		this.unitTisMetric = unitTisMetric;
	}
	public boolean isUnitRisMetric() {
		return unitRisMetric;
	}
	public void setUnitRisMetric(boolean unitRisMetric) {
		this.unitRisMetric = unitRisMetric;
	}
	public boolean isUnitWisMetric() {
		return unitWisMetric;
	}
	public void setUnitWisMetric(boolean unitWisMetric) {
		this.unitWisMetric = unitWisMetric;
	}
	public boolean isUnitWisKnots() {
		return unitWisKnots;
	}
	public void setUnitWisKnots(boolean unitWisKnots) {
		this.unitWisKnots = unitWisKnots;
	}
	public boolean isUnitPisMetric() {
		return unitPisMetric;
	}
	public void setUnitPisMetric(boolean unitPisMetric) {
		this.unitPisMetric = unitPisMetric;
	}
	public String getUnitW() {
		return unitW;
	}
	public void setUnitW(String unitW) {
		this.unitW = unitW;
	}

	/** Return one of the user-defined cities (0-8) */
	public String getCity(int index) {
		return cities[index];
	}
	/** Set one of the user-defined cities (0-8) */
	public void setCity(String city, int index) {
		cities[index] = city;
	}
	
	public int getUpdateFreq() {
		return updateFreq;
	}

	public void setUpdateFreq(int updateFreq) {
		this.updateFreq = updateFreq;
	}

	public boolean toastable() {
		return showToasts;
	}

	public void setToastable(boolean showToasts) {
		this.showToasts = showToasts;
	}

//	public boolean isKillable() {
//		return killable;
//	}
//
//	public void setKillable(boolean killable) {
//		this.killable = killable;
//	}

	public boolean isWifiOnly() {
		return wifiOnly;
	}

	public void setWifiOnly(boolean wifiOnly) {
		this.wifiOnly = wifiOnly;
	}

	/** Return the arrangement of tiles on the screen */
	public int[] getOrders() {
		return orders;
	}
	/** Set the arrangement of tiles on the screen */
	public void setOrders(int[] orders) {
		this.orders = orders;
	}

	/** Whether there been a change to the user-defined city collection */
	public boolean isUpdated() {
		return updated;
	}

	/** Inform the <code>updaterService</code> there been a change to the user-defined city collection */
	public void setUpdated(boolean updated) {
		this.updated = updated;
	}
	
	
	/**
	 * Get one of the device-scaled text sizes
	 * @param type the text-size
	 * @return the size
	 */
	public float getTextSize(textSizes type) {
		switch(type) {
			case SMALL: return textSizeSmall; 
			case LARGE: return textSizeLarge;
			default: return textSize;
		}
		
	}
	/**
	 * Set one of the device-scaled text sizes
	 * @param type the text-size
	 * @param size in pixels
	 */
	public void setTextSize(textSizes type, float size) {
		switch(type) {
			case SMALL: this.textSizeSmall = size; break;
			case LARGE: this.textSizeLarge = size; break;
			default: this.textSize = size; break;
		}
		
	}


	/** Update the global application settings */
	public void updateSettings(Context c) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
		
		setUnitTisMetric(Boolean.parseBoolean(prefs.getString("temp", "true")));
		setUnitRisMetric(Boolean.parseBoolean(prefs.getString("rain", "true")));
		setUnitPisMetric(Boolean.parseBoolean(prefs.getString("pres", "true")));
		setUnitW(prefs.getString("wind", "mph"));

		int freq = prefs.getBoolean("autoUpdate", true) ?
				Integer.parseInt(prefs.getString("freq", "30")) : 10000;
		setUpdateFreq(freq);
		setWifiOnly(prefs.getBoolean("wifiOnly", false));
		setToastable(prefs.getBoolean("toasts", true));
	}

	/**
	 * Convert a weather variable value from default units (UK Met Office standard system) to one of a range of other common units,
	 * 	and tidy up the formatting.
	 * @param value the variable to convert
	 * @param type the conversion type (0 Temp, 1 Rain, 2 Wind, 3 Relative Humidity, 4 Atmospheric Pressure)
	 * @return the converted value with its measurement unit
	 */
	public String convert(double value, int type) {
		String unit = "mph";
		int dp = 0;
	
		String[] unitsMetric = { "C", "mm", "", "%", "mb" };
		String[] unitsImperial = { "F", "in", "",  "%", "inHg" };
		int[] dpsMetric = { 0, 1, 0, 0, 0 };
		int[] dpsImperial = { 0, 2, 0, 0, 2 };
		
		if(type == 2) {
			String[] unitWtypes = { "mph", "knt", "kmh", "mps" };
			String[] unitWNames = { "mph", "kt", "km/h", "m/s" };
			double[] unitWconvs = { 1.0, 0.868976, 1.609344, 0.44704 };
			int index = Arrays.asList(unitWtypes).indexOf(getUnitW());
			value *=  unitWconvs[index];
			unit = unitWNames[index];
		} else {
			boolean[] metrics = { isUnitTisMetric(), isUnitRisMetric(), true, true, isUnitPisMetric() };
			double[] conversions = { 5.0/9.0, 25.4, 1, 1, 33.864 };
			
			if(metrics[type]) {
				unit = unitsMetric[type];
				dp = dpsMetric[type];
			} else {
				unit = unitsImperial[type];
				dp = dpsImperial[type];
				value /= conversions[type];
				if(type == 0) {
					value += 32;
				}
			}
		}
		
		return round(value, dp) + " " + unit;

	}
	
	/**
	 * Round a value to a set number of decimal places
	 * @param value
	 * @param places
	 * @return the rounded value
	 */
	public static String round(double value, int places) {
		return String.format("%." + places + "f", value);
	}

}