package co.uk.wxApp.views;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;
import co.uk.wxApp.R;
import co.uk.wxApp.models.Data;

/**
 * The table view - a sortable summary of all cities available
 * @author Ben LR
 */
public class RankAc extends Activity {
	
	private ProgressBar pb;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_rank);
		
		pb = (ProgressBar) findViewById(R.id.progressBar1);
		
		WebView myWebView = (WebView) findViewById(R.id.webView1);
		myWebView.setWebChromeClient(new WebChromeClient());
		
		WebSettings webSettings = myWebView.getSettings();
		webSettings.setJavaScriptEnabled(true);
		
		myWebView.setWebViewClient(new MyWebViewClient(myWebView));
		
		String query = "?cities=";
		for(int i = 0; i < Data.CITY_LIMIT; i++) {
			query += Data.getInstance().getCity(i).replaceAll(" ", "+") + ",";
		}
		
		myWebView.loadUrl("http://nw3weather.co.uk/CP_Solutions/WxApp/" + query);
	}
	
	
	/**
	 * @author http://stackoverflow.com/questions/7772409/set-loadurltimeoutvalue-on-webview
	 */
	private class MyWebViewClient extends WebViewClient {
//		private WebView myWebView;
		Toast toast;

	    public MyWebViewClient(WebView myWebView) {
	        toast = Toast.makeText(getApplicationContext(), "Page loading", Toast.LENGTH_LONG);
	    }
	    
	    @Override
	    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
	    	view.loadUrl("file:///android_asset/rankSample.htm"); //sample version of the requested web page
	    	Toast.makeText(getApplicationContext(), "No network connection.\nDisplaying sample data", Toast.LENGTH_LONG).show();
	    	pb.setVisibility(View.GONE);
	    }

	    @Override
	    public void onPageStarted(WebView view, String url, Bitmap favicon) {
	        new Thread(new Runnable() {
	            public void run() {
	            	toast.show();
	            }
	        }).start();
	    }

	    @Override
	    public void onPageFinished(WebView view, String url) {
	        toast.cancel();
	        pb.setVisibility(View.GONE);
	    }
	}
	
}