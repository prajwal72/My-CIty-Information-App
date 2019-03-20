package in.gov.sih.mycityapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class railway_web_activity extends AppCompatActivity {

    String train_no="18181",day_no;
    WebView mwebview;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_railway_web_activity);

        Bundle bundle = getIntent().getExtras();
        String temp_no = bundle.getString("train_no");
        if(!temp_no.equals("")||temp_no==null){
            train_no=temp_no;
        }
        day_no=bundle.getString("train_day");
        if(day_no.equals("Day 1")){
            day_no="today";
        }
        else {
            day_no="yesterday";
        }

        mwebview=findViewById(R.id.rail_web);
        WebSettings webSettings = mwebview.getSettings();
        webSettings.setJavaScriptEnabled(true);
        String url="https://trainstatus.info/running-status/"+train_no+"-"+day_no;
        Log.e("htht",url);
        mwebview.setWebViewClient(new WebViewClient());
        mwebview.loadUrl(url);

//        mwebview.setWebViewClient(new WebViewClient() {
//            @Override
//            public void onLoadResource(WebView view, String imageURL) {
//                super.onLoadResource(view, imageURL);
//
//            }
//
//            public void onPageFinished(WebView view, String imageURL) {
//                // do your stuff here
//            }
//        });
    }
}
