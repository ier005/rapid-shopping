package com.example.test.myapplication;


import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.List;
import java.util.Vector;

public class MainActivity extends AppCompatActivity {

    Handler handler;
    ListView goodsList;
    Vector<Good> goods = new Vector<>();
    LVAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button bn = (Button) findViewById(R.id.button);
        bn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Document doc;

                        try {
                            TextView editText = (TextView) findViewById(R.id.editText);
                            String url1 = "https://search.jd.com/Search?keyword=";
                            String url2 = "&enc=utf-8&pvid=19314f6c144a42f8aeaf082d2a905af6";
                            doc = Jsoup.connect(url1 + editText.getText() + url2).get();
                            Elements items = doc.select("li.gl-item");
                            goods.clear();
                            for (Element item : items) {
                                Good good = new Good();
                                good.price = item.select("div.p-price").text();
                                good.name = item.select("div.p-name").select("em").text();
                                goods.add(good);
                            }
                            handler.sendEmptyMessage(0x0001);
                        } catch (Exception e) {
                            Toast.makeText(MainActivity.this, "Error: " + e, Toast.LENGTH_SHORT);
                        }
                    }
                }).start();
            }

        });

        adapter = new LVAdapter();
        goodsList = (ListView) findViewById(R.id.goodsList);
        goodsList.setAdapter(adapter);

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 0x0001) {
                    adapter.notifyDataSetChanged();
                }
            }
        };
    }

    class Good
    {
        String name;
        String price;
    }

    class ViewHolder
    {
        TextView name;
        TextView price;
    }

    class LVAdapter extends BaseAdapter
    {
        @Override
        public int getCount()
        {
            return goods.size();
        }

        @Override
        public Object getItem(int position)
        {
            return goods.get(position);
        }

        @Override
        public long getItemId(int position)
        {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            ViewHolder viewHolder;
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.good_item, null);
                viewHolder = new ViewHolder();
                viewHolder.name = (TextView) convertView.findViewById(R.id.tv_name);
                viewHolder.price = (TextView) convertView.findViewById(R.id.tv_price);
                convertView.setTag(viewHolder);
            }
            else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            Good good = goods.get(position);
            viewHolder.name.setText(good.name);
            viewHolder.price.setText(good.price);
            return convertView;
        }
    }

}
