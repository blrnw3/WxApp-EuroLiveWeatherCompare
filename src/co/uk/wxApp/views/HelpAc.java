package co.uk.wxApp.views;

import co.uk.wxApp.R;
import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

/**
 * Help and documentation pages in a <code>TabView</code>
 * @author Ben Lee-Rodgers
 */
public class HelpAc extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_help);
		
		TabHost tabHost = (TabHost) findViewById(R.id.tabHost);
		tabHost.setup();

		//Set up the tabs
		TabSpec spec1 = tabHost.newTabSpec("Navigation");
		spec1.setContent(R.id.tab1);
		spec1.setIndicator("Navigation");

		TabSpec spec2 = tabHost.newTabSpec("Data");
		spec2.setIndicator("Data");
		spec2.setContent(R.id.tab2);

		TabSpec spec3 = tabHost.newTabSpec("About");
		spec3.setIndicator("About");
		spec3.setContent(R.id.tab3);

		tabHost.addTab(spec1);
		tabHost.addTab(spec2);
		tabHost.addTab(spec3);
		
		
		//Add html page to each TabView
		TextView navigation = (TextView)findViewById(R.id.txt1);
		navigation.setText(Html.fromHtml(getString(R.string.help_navigation)));
		
		TextView data = (TextView)findViewById(R.id.txt2);
		data.setText(Html.fromHtml(getString(R.string.help_data)));
		
		TextView about = (TextView)findViewById(R.id.txt3);
		about.setText(Html.fromHtml(getString(R.string.help_about)));
		about.setMovementMethod(LinkMovementMethod.getInstance()); //make href links work
	}

}