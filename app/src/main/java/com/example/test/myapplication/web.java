package com.example.test.myapplication;

/**
 * Created by wangzheng on 4/14/17.
 */

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.view.KeyEvent;
import android.webkit.WebBackForwardList;
import android.webkit.WebView;
import android.webkit.WebSettings;
import android.webkit.WebViewClient;

public class web extends AppCompatActivity {

    WebView webView;
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
            return true;
        }
    }


}