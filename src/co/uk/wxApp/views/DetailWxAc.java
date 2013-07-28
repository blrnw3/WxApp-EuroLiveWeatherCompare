package co.uk.wxApp.views;

import co.uk.wxApp.R;
import co.uk.wxApp.controllers.Controller;
import co.uk.wxApp.controllers.Swipeable;
import co.uk.wxApp.controllers.Swiper;
import co.uk.wxApp.models.Data;
import co.uk.wxApp.models.Library;
import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * The screen for showing the detail for a particular city. City can be changed on swipe.
 * @author Ben LR <br />
 * 	Gesture handling from http://stackoverflow.com/questions/937313/android-basic-gesture-detection 
 *
 */
public class DetailWxAc extends Activity implements OnClickListener, Swipeable {

    private TextView[] variableData;
	private String[][] data = new String[Data.CITY_LIMIT][];
	private int city;

    private GestureDetector gestureDetector;
    View.OnTouchListener gestureListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_wx);

        // Gesture detection
        gestureDetector = new GestureDetector(this.getApplicationContext(),
        		new Swiper(city, Data.CITY_LIMIT - 1, this));
        gestureListener = new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        };
        //Add detector to background
        LinearLayout screen = (LinearLayout) findViewById(R.id.DetailLinLay);
        screen.setOnClickListener(this); 
        screen.setOnTouchListener(gestureListener);
        
        //Get data array, and the city that was clicked
        Bundle extras = getIntent().getExtras();
        for(int i = 0; i < data.length; i++) { // can't pass 2D arrays
        	 data[i] = extras.getStringArray("data"+i);
		}
		city = extras.getInt("city");
        
        variableData = new TextView[] {
				(TextView) findViewById(R.id.textTp), //temp
				(TextView) findViewById(R.id.textRn), //rain
				(TextView) findViewById(R.id.textWd), //wind
				(TextView) findViewById(R.id.textRh), //hum
				(TextView) findViewById(R.id.textPr), //pressure
				(TextView) findViewById(R.id.textSk), //sky
		};
        
        updateGUI(city);
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    	Data.getInstance().updateSettings(getBaseContext());
    }
    
    //Implementing Swipeable method
    public void updateGUI(int newInteger) {
    	city = newInteger;
    	int order = Data.getInstance().getOrders()[city];
    	String cityName = Data.getInstance().getCity(order);
		
		TextView heading = (TextView) findViewById(R.id.detailHeading);
		heading.setText("Detail for " + cityName);
		heading.setTextSize( TypedValue.COMPLEX_UNIT_DIP, 
				(float) (Data.getInstance().getTextSize(Data.textSizes.LARGE) * 1.2) );
		
		TextView timeTxt = (TextView) findViewById(R.id.detailTime);
		timeTxt.setText( Library.dateFormat( data[order][Data.VARIABLE_NUM-1]) );
		timeTxt.setTextSize( TypedValue.COMPLEX_UNIT_DIP, 
				Data.getInstance().getTextSize(Data.textSizes.MEDIUM) );
        
        for(int i = 0; i < variableData.length; i++) {
        	double value = Double.parseDouble( data[order][i] );
        	String hexColour = "aaaaaa";
			String dataText = "N/A";

			if(i == 5) { //weather conditions icon
				variableData[i].setText( Data.WX_ICON_LABELS[(int) value] );
				variableData[i].setTextColor( Color.WHITE );
				variableData[i].setTextSize( TypedValue.COMPLEX_UNIT_DIP, 
						Data.getInstance().getTextSize(Data.textSizes.LARGE) );
			}
			else {
				int level = Library.valueColour(value, i);
				
				if(value > -100) { //value is not faulty
					hexColour = Data.DATA_COLOURS[i][level];
					dataText = Data.getInstance().convert(value, i);
				}

				variableData[i].setText(dataText);
				variableData[i].setTextColor( Color.parseColor("#" + hexColour) );
				variableData[i].setTextSize( TypedValue.COMPLEX_UNIT_DIP, 
				Data.getInstance().getTextSize(Data.textSizes.LARGE) );
			}
        }
    }
    
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_detail_wx, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	Controller.activityLauncher(item.getItemId(), getApplicationContext());
    	return true;
    }


	public void onClick(View arg0) {
		//Needed by OnClickListener
	}

}