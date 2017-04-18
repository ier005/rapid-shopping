package com.example.test.myapplication;

/**
 * Created by wangzheng on 4/14/17.
 */

import android.content.Intent;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;


import android.support.v7.widget.ShareActionProvider;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebBackForwardList;
import android.webkit.WebView;
import android.webkit.WebSettings;
import android.webkit.WebViewClient;

public class web extends AppCompatActivity {

    WebView webView;
    private ShareActionProvider mShareActionProvider;
    Intent shareIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.web);
        Intent intent = getIntent();
        webView= (WebView) findViewById(R.id.webView);

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);//允许运行JavaScript

        webView.loadUrl(intent.getStringExtra("url"));             //加载外网
        webView.setWebViewClient(new HelloWebViewClient ());

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate menu resource file.
        getMenuInflater().inflate(R.menu.menu_share, menu);

        // Locate MenuItem with ShareActionProvider
        MenuItem item = menu.findItem(R.id.action_share);

        // Fetch and store ShareActionProvider
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);

        shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");


        // Return true to display menu
        return true;
    }

    @Override
    //设置回退
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        WebBackForwardList List = webView.copyBackForwardList();
        if ((keyCode == KeyEvent.KEYCODE_BACK) &&webView.canGoBack()) {
            //if(webView.getUrl()!=List.getItemAtIndex(2).getUrl()) {
                webView.goBack(); //调用goBack()返回WebView的上一页面
                return true;
           /* }
            else
            {
                web.this.finish();
                return true;
            }*/
        }
        else
            {
                web.this.finish();
                return true;
            }
      //return false;
    }

    //Web视图
    private class HelloWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);

            shareIntent.putExtra(Intent.EXTRA_TEXT, webView.getUrl());
            mShareActionProvider.setShareIntent(shareIntent);

            return true;
        }
    }


}