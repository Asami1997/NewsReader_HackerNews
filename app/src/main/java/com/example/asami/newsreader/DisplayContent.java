package com.example.asami.newsreader;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class DisplayContent extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_content);


        WebView webView = (WebView) findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient());


        Intent  intent = getIntent();

        //-1 is defult because its impossible to reach in the listview

        int position = intent.getIntExtra("position",-1);


        if(position!=-1)

           {
               webView.loadData(MainActivity.info.get(position),"text/html","UTF-8");


           }


    }
}
