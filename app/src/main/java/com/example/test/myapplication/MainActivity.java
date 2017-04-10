package com.example.test.myapplication;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class MainActivity extends AppCompatActivity {

    SearchThread searchThread;
    Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        searchThread = new SearchThread();
        searchThread.start();
        Button bn = (Button) findViewById(R.id.button);
        bn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                searchThread.handler.sendEmptyMessage(0);
            }

        });
        handler = new Handler()
        {
            @Override
            public void handleMessage(Message msg)
            {
                if (msg.what == 0x0001) {
                    TextView txt = (TextView) findViewById(R.id.txt);
                    txt.setText(msg.getData().getString("info"));
                }
            }
        };
    }

    class SearchThread extends Thread
    {
        public Handler handler;

        @Override
        public void run()
        {
            Looper.prepare();
            handler = new Handler()
            {
                @Override
                public void handleMessage(Message m)
                {
                    Document doc = null;
                    String info = "";
                    try
                    {
                        TextView editText = (TextView) findViewById(R.id.editText);
                        String url1 = "https://search.jd.com/Search?keyword=";
                        String url2 = "&enc=utf-8&pvid=19314f6c144a42f8aeaf082d2a905af6";
                        doc = Jsoup.connect(url1 + editText.getText() + url2).get();
                        Elements items = doc.select("li.gl-item");
                        for (Element item : items) {
                            info += item.select("div.p-price").text();
                            info += item.select("div.p-name").select("em").text();
                            info += "\n";
                        }
                    }
                    catch(Exception e)
                    {
                        e.printStackTrace();
                        info = "Error: " + e;
                    }
                    finally
                    {
                        Message msg = new Message();
                        msg.what = 0x0001;
                        Bundle bundle = new Bundle();
                        bundle.putString("info", info);
                        msg.setData(bundle);
                        MainActivity.this.handler.sendMessage(msg);
                    }
                }
            };
            Looper.loop();
        }
    }
}
