package com.example.test.myapplication;


import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.webkit.WebView;
import android.webkit.WebSettings;
import android.webkit.WebViewClient;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Vector;

public class MainActivity extends AppCompatActivity {

    Handler handler;
    ListView goodsList;
    Vector<Good> goods = new Vector<>();
    LVAdapter adapter;
    ProgressDialog pd;
    DatabaseOpenHelper db;
    WebView web_view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button bn = (Button) findViewById(R.id.button);
        bn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pd = ProgressDialog.show(MainActivity.this, "Searching", "Wait plz... :)");

                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        Document doc;
                        try {
                            TextView editText = (TextView) findViewById(R.id.editText);
                            String url1 = "http://search.yhd.com/c0-0/k";
                            String url2 = "/?tp=2279.1.12.0.3.LhNe3k^-10-CDFEj";
                            doc = Jsoup.connect(url1 + editText.getText() + url2).get();
                            String urlp = "";
                            Elements items = doc.select("div.mod_search_pro");
                            goods.clear();
                            for (Element item : items) {
                                Good good = new Good();
                                good.name = item.select("p.proName.clearfix").text();
                                good.price  = item.select("p.proPrice").select("em").text();
                                urlp = item.select("a img[style]").attr("src")+item.select("a img[style]").attr("original");
                                good.image = getBitmap("http:"+urlp);
                                good.logo = getDrawable(R.drawable.yhd);
                                good.url =item.select("p.proName.clearfix").select("a").attr("href");
                                good.site = "yhd";
                                goods.add(good);
                            }
                            handler.sendEmptyMessage(0x0001);
                        } catch (Exception e) {
                            Toast.makeText(MainActivity.this, "Error: " + e, Toast.LENGTH_SHORT).show();
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
                    pd.dismiss();
                    adapter.notifyDataSetChanged();
                }
            }
        };

        db = new DatabaseOpenHelper(this, "rp_db.db3", 1);
        goodsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                Intent intent=new Intent(MainActivity.this,web.class);
                intent.putExtra("url",goods.get(position).url);
                startActivity(intent);
            }


        });

        goodsList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int positon, long id) {
                Good good = goods.get(positon);
                if (db.insertGood(good) != -1) {
                    Toast.makeText(MainActivity.this, "收藏成功 :)", Toast.LENGTH_SHORT).show();
                    System.out.println("success");
                }
                else {
                    Toast.makeText(MainActivity.this, "操作失败 :(", Toast.LENGTH_SHORT).show();
                }

                return true;
            }
        });

    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        if (db != null) {
            db.close();
        }
    }


    class ViewHolder
    {
        TextView name;
        TextView price;

        ImageView image;
        ImageView logo;
        TextView url;
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
                viewHolder.image = (ImageView) convertView.findViewById(R.id.good_image);
                viewHolder.logo = (ImageView) convertView.findViewById(R.id.logo);
                convertView.setTag(viewHolder);
            }
            else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            Good good = goods.get(position);
            viewHolder.name.setText(good.name);
            viewHolder.price.setText(good.price);
            viewHolder.image.setImageBitmap(good.image);
            viewHolder.logo.setImageDrawable(good.logo);
            return convertView;
        }
    }

    public Bitmap getBitmap(String path) throws IOException {
        try {
            URL url = new URL(path);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5000);
            conn.setRequestMethod("GET");
            if (conn.getResponseCode() == 200) {
                InputStream inputStream = conn.getInputStream();
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                return bitmap;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
