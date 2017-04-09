package com.example.test.myapplication;

import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        Button bn = (Button) findViewById(R.id.button);
        bn.setOnClickListener(new MyClickListener());
    }

    class MyClickListener implements View.OnClickListener
    {
        TextView txt = (TextView) findViewById(R.id.txt);

        @Override
        public void onClick(View v)
        {
            Document doc = null;
            try {
                doc = Jsoup.connect("https://search.jd.com/Search?keyword=英文&enc=utf-8&pvid=19314f6c144a42f8aeaf082d2a905af6").get();
                String title = doc.title();
                txt.setText(title);
            } catch (Exception e) {
                e.printStackTrace();
                txt.setText("Error: " + e);
            }

        }
    }
}
