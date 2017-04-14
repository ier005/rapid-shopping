package com.example.test.myapplication;


import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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
    SearchView searchView;

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.layout.menu, menu);
        MenuItem menuItem = menu.findItem(R.id.search);
        searchView = (SearchView) MenuItemCompat.getActionView(menuItem);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener()
        {
            @Override
            public boolean onQueryTextSubmit(String query)
            {
                pd = ProgressDialog.show(MainActivity.this, "Searching", "Wait plz... :)");
                final String keyword = query;

                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        Document doc;
                        try {
                            String url1 = "http://search.yhd.com/c0-0/k";
                            String url2 = "/?tp=2279.1.12.0.3.LhNe3k^-10-CDFEj";
                            doc = Jsoup.connect(url1 + keyword + url2).get();
                            String urlp = "";
                            Elements items = doc.select("div.mod_search_pro");
                            goods.clear();
                            for (Element item : items) {
                                Good good = new Good();
                                good.name = item.select("p.proName.clearfix").text();
                                good.price  = item.select("p.proPrice").select("em").text();
                                urlp = item.select("a img[style]").attr("src")+item.select("a img[style]").attr("original");
                                good.image = getBitmap("http:"+urlp);
                                good.logo = R.drawable.yhd;
                                good.site = "yhd";
                                goods.add(good);
                            }
                            handler.sendEmptyMessage(0x0001);
                        } catch (Exception e) {
                            Toast.makeText(MainActivity.this, "Error: " + e, Toast.LENGTH_SHORT).show();
                        }
                    }
                }).start();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText)
            {
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


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

        goodsList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int positon, long id) {
                Good good = goods.get(positon);
                if (db.insertGood(good) != -1) {
                    Toast.makeText(MainActivity.this, "收藏成功 :)", Toast.LENGTH_SHORT).show();
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
            viewHolder.logo.setImageResource(good.logo);
            return convertView;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId()) {
            case R.id.collection:
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, CollectionActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
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
