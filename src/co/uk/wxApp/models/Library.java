package co.uk.wxApp.models;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import android.content.Context;

/**
 * A library of static methods used by this app
 * @author Ben Lee-Rodgers
 *
 */
public class Library {
	
	/** Stores the contents of a text file from a URL into an <code>ArrayList</code> of <code>String</code>s
	 * @param url from the web
	 * @return an <code>ArrayList</code> of <code>String</code>s, one per line of the text file; empty array on failure
	 */
	public static String[] readWebpageToArray(String url) {
		String[] lines;
		try {
			BufferedReader br = getBufferedReaderFromURL(url);
			String strLine = br.readLine();
			lines = strLine.split(",<br />");
			br.close();
			return lines;
		} catch (Exception e) {
//			e.printStackTrace();
		}
		return new String[] { };
	}

	
	/**
	 * Gets an integer from a webpage
	 * @param url from the web
	 * @return the raw number, or -1 on failure
	 */
	public static int readWebpageToInt(String url) {
		try {
			BufferedReader br = getBufferedReaderFromURL(url);
			String strLine = br.readLine();
			br.close();
			return Integer.parseInt(strLine);
		} catch (Exception e) {
//			e.printStackTrace();
		}
		return -1;
	}
	
	/**
	 * Source: http://stackoverflow.com/questions/6272405/read-from-url-java<
	 * @param url the web page
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	private static BufferedReader getBufferedReaderFromURL(String url)
			throws MalformedURLException, IOException {
		URL u = new URL(url);
		int timeout = 5000;
		
		URLConnection con = u.openConnection();
		con.setConnectTimeout(timeout);
		con.setReadTimeout(timeout);
		BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
		
		return br;
	}

	
	/**
	 * Copies a file from the assets folder to an internal destination
	 * Modified from textbook - Beginning Android 4 Application Development, Wei-Meng Lee - 2012 
	 * @param asset the filename of the file in the assets folder
	 * @param destination the full destination file path
	 * @param ctx
	 * @throws IOException
	 */
	public static void copyFileFromAssets( String asset, String destination,
			Context ctx ) throws IOException {
		
		File directory = new File(destination);
		directory.mkdir();
		destination += asset;
		File f = new File(destination);
		
//		System.out.println("DB file size: " + f.length());
		if (f.length() < 50000) { //bad or non-existing file, so need to create
			InputStream is = ctx.getAssets().open(asset);
			OutputStream os = new FileOutputStream(destination);
//			System.out.println(f.mkdirs());
//			System.out.println(f.createNewFile());
			//---copy 1K bytes at a time---
			byte[] buffer = new byte[1024];
			int length;
			while( (length = is.read(buffer)) > 0 ) {
				os.write(buffer, 0, length);
			}
			is.close();
			os.close();
		}
	}
	
	/**
	 * Formats a timestamp for use by this app
	 * @param time a UNIX timestamp in seconds
	 * @return a textual representation of the timestamp
	 */
	public static String dateFormat(String time) {
		long mili = Long.parseLong(time) * 1000;
		if(mili < 9999) { //On app install, and before the first update over the web, this will be true
			return "Sample Data";
		}
		Date d = new Date(mili);
		SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, HH:mm zzz");
		return sdf.format(d);
	}
	
	/**
	 * Converts a raw weather variable value to a relative one to be used for accessing an array index (
	 *	whereby the array specifies a colour spectrum ).
	 * @param value the raw value in native (UK) units
	 * @param type the weather variable type:<br />
	 * <b>0</b> - Temperature <br />
	 * <b>1</b> - Rainfall <br />
	 * <b>2</b> - Wind speed <br />
	 * <b>3</b> - Humidity <br />
	 * <b>4</b> - Pressure <br />
	 * @return the variable-independent relative value
	 */
	public static int valueColour(double value, int type) {
		double[] values = Data.DATA_THRESHOLDS[type];
		int i;

		for(i = 0; i < values.length; i++) {
			if(value < values[i]) {
				return i;
			}
		}
		return i;
	}
	
	/**
	 * My version of Java's <code>Arrays.indexOf()</code>. Mine works on <code>double</code>s, Java's does not. <br />
	 * Finds the index of a double in an array, or -1 if not found.
	 * @param array
	 * @param value
	 * @return
	 */
	public static int indexFind(double[] array, double value) {
		for(int i = 0; i < array.length; i++) {
			if( value == array[i] ) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Get the mapping from a sorted array to its original, unsorted state
	 * @param array unsorted
	 * @return mapping
	 */
	public static int[] getOrdersFromSort(double[] array) {
		int len = array.length;
		int[] orders = new int[len];
		double[] arrayCopy = new double[len];

		System.arraycopy(array, 0, arrayCopy, 0, len);
		Arrays.sort(array);
		for(int i = 0; i < len; i++) {
			orders[i] = indexFind(arrayCopy, array[i]);
		}

		return orders;
	}
	/**
	 * Split an array of length 9 into a 2D, 3x3 array
	 * @param array the 1D array
	 * @return the 2D array
	 */
	public static int[][] splitArray(int[] array) {
		int[][] splits = new int[3][3];
		int len = array.length;
		int j = -1;

		for(int i = 0; i < len; i++) {
			if(i % 3 == 0) {
				j++;
			}
			splits[j][i % 3] = array[i];
		}

		return splits;
	}

	/**
	 * Takes a collection of nine cities and sorts them NW to SE, so that on a 3x3 grid
	 * 	they appear in roughly the correct relative positions according to their co-ordinates. <br />
	 * They are first sorted by longitude, then within each group of three sorted by latitude.
	 * @param lats latitudes of the cities
	 * @param lngs longitudes of the cities
	 * @return the orders of the sorted cities according to a 3x3 grid: W to E, N to S.
	 */
	public static int[] citySorter(double[] lats, double[] lngs) {
		int len = Data.CITY_LIMIT;
		int dim = len / 3;

		int[] orders = new int[len];
		int[] finalOrdersInt = new int[len];

		int[][] splitLngs = new int[dim][dim];
		int[][] splitOrders = new int[dim][dim];
		double[][] splitLats = new double[dim][dim];
		int[][] fullNewOrders = new int[dim][dim];

		//sort by longitude and store the mapping
		orders = getOrdersFromSort(lngs);
		//Split sorted longs into three groups of three (3 x 3 grid)
		splitLngs = splitArray(orders);

		//Store latitudes corresponding to those split longitudes into its own 3x3 
		for(int i = 0; i < dim; i++) {
			for(int j = 0; j < dim; j++) {
				splitLats[i][j] = lats[splitLngs[i][j]];
			}
		}	

		//For each column of longs, sort within by latitude and store the local mapping (0-2)
		for(int i = 0; i < dim; i++) {
			splitOrders[i] = getOrdersFromSort(splitLats[i]);
		}

		//Re-order the 3x3 longitude grid according to the locally sorted latitudes using the local mapping 
		for(int i = 0; i < dim; i++) {
			for(int j = 0; j < dim; j++) {
				fullNewOrders[i][j] = splitLngs[i][splitOrders[i][j]];
			}
		}

		//Convert 3x3 2D grid into a 1D array that can be more easily looped through, and get the N to S, W to E order right
		for(int i = 0; i < dim; i++) {
			for(int j = 0; j < dim; j++) {
				finalOrdersInt[dim*i+j] = fullNewOrders[j][2-i];
			}
		}
		
		//Uncomment to see how it works
		/*
		System.out.println( Arrays.toString(lats));
		System.out.println( Arrays.toString(lngs));
		System.out.println( Arrays.toString(orders));
		System.out.println( Arrays.deepToString(splitLngs) );
		System.out.println( Arrays.deepToString(splitLats) );
		System.out.println( Arrays.deepToString(splitOrders));
		System.out.println( Arrays.deepToString(fullNewOrders));
		System.out.println( Arrays.toString(finalOrdersInt));
		 */

		return finalOrdersInt;
	}
	
}