package com.example.test.myapplication;

import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import org.jsoup.Jsoup

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
            String url = "http://search.jd.com";
            String result = "";
            BufferedReader in = null;
            try {
                URL realUrl = new URL(url);
                URLConnection connection = realUrl.openConnection();
                connection.connect();
                in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                while ((line = in.readLine()) != null) {
                    result += line;
                }
                txt.setText(result);
            }
            catch (Exception e) {

                txt.setText("Wrong!" + e);
                e.printStackTrace();
            }
            finally {
                try {
                    if (in != null)
                        in.close();
                }
                catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
        }
    }
}
