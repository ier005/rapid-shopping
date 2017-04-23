package com.example.test.myapplication;


import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
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
    SearchView searchView;
    WebView web_view;
    String query_sort;
    int flag = -1;
    SharedPreferences user;

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu, menu);
        MenuItem menuItem = menu.findItem(R.id.search);
        searchView = (SearchView) MenuItemCompat.getActionView(menuItem);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener()
        {
            @Override
            public boolean onQueryTextSubmit(String query)
            {
                query_sort = query;
                pd = ProgressDialog.show(MainActivity.this, "Searching", "Wait plz... :)");
                final String keyword = query;
                flag = 0;
                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        Document doc_yhd,doc_ymx,doc_jd;
                        try {

                            String url_jd = "https://search.jd.com/Search?keyword="+keyword+"&enc=utf-8";
                            String url_yhd = "https://search.yhd.com/c0-0/k"+keyword;
                            String url_ymx = "https://www.amazon.cn/s/ref=nb_sb_noss_2?__mk_zh_CN=亚马逊网站&url=search-alias%3Daps&field-keywords="+keyword+"&rh=i%3Aaps%2Ck%3A";


                            doc_jd = Jsoup.connect(url_jd).get();
                            doc_yhd = Jsoup.connect(url_yhd).get();
                            doc_ymx = Jsoup.connect(url_ymx).get();
                            String urlp_yhd = "";
                            String urlp_ymx = "";
                            String urlp_jd = "";
                            Elements items_jd = doc_jd.select("li.gl-item");
                            Elements items_yhd = doc_yhd.select("div.mod_search_pro");
                            Elements items_ymx = doc_ymx.select("li.s-result-item");
                            goods.clear();
                            
                            for (int i = 0 ;i<5 && i<items_yhd.size();i++) {
                                Good good_yhd = new Good();
                                good_yhd.name = items_yhd.select("p.proName.clearfix").get(i).text();
                                good_yhd.price  = items_yhd.select("p.proPrice").select("em").get(i).text();
                                urlp_yhd = items_yhd.select("a img[style]").get(i).attr("src")+items_yhd.select("a img[style]").get(i).attr("original");
                                good_yhd.image = getBitmap("http:"+urlp_yhd);
                                good_yhd.logo = R.drawable.yhd;
                                good_yhd.url = items_yhd.select("p.proName.clearfix").select("a").get(i).attr("href");
                                good_yhd.site = "yhd";
                                goods.add(good_yhd);
                            }

                            for (int i = 0 ;i<5 && i<items_ymx.size();i++) {
                                Good good_ymx = new Good();
                                good_ymx.name = items_ymx.select(".s-access-title").get(i).text();
                                good_ymx.price = items_ymx.select("span.s-price").get(i).text();
                                urlp_ymx = items_ymx.select("img").get(i).attr("src");
                                good_ymx.image = getBitmap(urlp_ymx);
                                good_ymx.logo = R.drawable.ymx;
                                good_ymx.url = items_ymx.select("a").get(i).attr("href");
                                good_ymx.site = "ymx";
                                goods.add(good_ymx);

                            }

                            for(int i =0 ;i<5 && i<items_jd.size();i++){
                                Good good_jd = new Good();
                                urlp_jd = items_jd.select("div.p-img a img").get(i).attr("src")+items_jd.select("div.p-img a img").get(i).attr("data-lazy-img");
                                good_jd.name = items_jd.select("div.p-name").select("a").get(i).text();
                                good_jd.price = items_jd.select("div.p-price").get(i).text();
                                good_jd.image = getBitmap("http:"+urlp_jd);
                                good_jd.logo = R.drawable.jd;
                                good_jd.url = items_jd.select("div.p-img").get(i).attr("href");
                                good_jd.site = "jd";
                                goods.add(good_jd);
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

        db = new DatabaseOpenHelper(this, "rp_db.db3", 3);
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
                }
                else {
                    Toast.makeText(MainActivity.this, "操作失败 :(", Toast.LENGTH_SHORT).show();
                }

                return true;
            }
        });

        user = getSharedPreferences("user", 0);
        String name = user.getString("name", null);
        if (name == null) {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        }

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
            case R.id.sort_all:
                if(flag==0 || flag==-1) return super.onOptionsItemSelected(item);
                flag=0;
                pd = ProgressDialog.show(MainActivity.this, "Sorting", "Wait plz... :)");
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String keyword = query_sort;
                        Document doc_yhd,doc_ymx,doc_jd;
                        try {

                            String url_jd = "https://search.jd.com/Search?keyword="+keyword+"&enc=utf-8";
                            String url_yhd = "https://search.yhd.com/c0-0/k"+keyword;
                            String url_ymx = "https://www.amazon.cn/s/ref=nb_sb_noss_2?__mk_zh_CN=亚马逊网站&url=search-alias%3Daps&field-keywords="+keyword+"&rh=i%3Aaps%2Ck%3A";

                            doc_jd = Jsoup.connect(url_jd).get();
                            doc_yhd = Jsoup.connect(url_yhd).get();
                            doc_ymx = Jsoup.connect(url_ymx).get();
                            String urlp_yhd = "";
                            String urlp_ymx = "";
                            String urlp_jd = "";
                            Elements items_jd = doc_jd.select("li.gl-item");
                            Elements items_yhd = doc_yhd.select("div.mod_search_pro");
                            Elements items_ymx = doc_ymx.select("li.s-result-item");
                            goods.clear();

                            for (int i = 0 ;i<5 && i<items_yhd.size();i++) {
                                Good good_yhd = new Good();
                                good_yhd.name = items_yhd.select("p.proName.clearfix").get(i).text();
                                good_yhd.price  = items_yhd.select("p.proPrice").select("em").get(i).text();
                                urlp_yhd = items_yhd.select("a img[style]").get(i).attr("src")+items_yhd.select("a img[style]").get(i).attr("original");
                                good_yhd.image = getBitmap("http:"+urlp_yhd);
                                good_yhd.logo = R.drawable.yhd;
                                good_yhd.url = items_yhd.select("p.proName.clearfix").select("a").get(i).attr("href");
                                good_yhd.site = "yhd";
                                goods.add(good_yhd);
                            }
                            for (int i = 0 ;i<5 && i<items_ymx.size();i++) {
                                Good good_ymx = new Good();
                                good_ymx.name = items_ymx.select(".s-access-title").get(i).text();
                                good_ymx.price = items_ymx.select("span.s-price").get(i).text();
                                urlp_ymx = items_ymx.select("img").get(i).attr("src");
                                good_ymx.image = getBitmap(urlp_ymx);
                                good_ymx.logo = R.drawable.ymx;
                                good_ymx.url = items_ymx.select("a").get(i).attr("href");
                                good_ymx.site = "ymx";
                                goods.add(good_ymx);

                            }

                            for(int i =0 ;i<5 && i<items_jd.size();i++){
                                Good good_jd = new Good();
                                urlp_jd = items_jd.select("div.p-img a img").get(i).attr("src")+items_jd.select("div.p-img a img").get(i).attr("data-lazy-img");
                                good_jd.name = items_jd.select("div.p-name").select("a").get(i).text();
                                good_jd.price = items_jd.select("div.p-price").get(i).text();
                                good_jd.image = getBitmap("http:"+urlp_jd);
                                good_jd.logo = R.drawable.jd;
                                good_jd.url = items_jd.select("div.p-img").get(i).attr("href");
                                good_jd.site = "jd";
                                goods.add(good_jd);
                            }


                            handler.sendEmptyMessage(0x0001);
                        } catch (Exception e) {
                            Toast.makeText(MainActivity.this, "Error: " + e, Toast.LENGTH_SHORT).show();
                        }
                    }
                }).start();
                return true;

            case R.id.sort_asc:
                if(flag==1 || flag==-1) return super.onOptionsItemSelected(item);
                flag = 1;
                pd = ProgressDialog.show(MainActivity.this, "Sorting", "Wait plz... :)");
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String keyword = query_sort;
                        Document doc_yhd,doc_ymx,doc_jd;
                        try {

                            String url_jd = "https://search.jd.com/Search?keyword="+keyword+"&enc=utf-8"+"&psort="+"2"+"&click=0";//销量3新品5价格升序2价格降序1
                            String url_yhd = "http://search.yhd.com/c0-0-0/b/a-s"+"3"+"-v4-p1-price-d0-f0-m1-rt0-pid-mid0-k"+keyword;//销量2新品6,价格升序3降序4
                            String url_ymx = "https://www.amazon.cn/s/ref=sr_st_"+"price-asc-rank"+"?__mk_zh_CN=亚马逊网站&url=search-alias%3Daps&field-keywords="+keyword+"&sort="+"price-asc-rank";
                            //relevanceblender相关性 date_desc_rank新品  price-asc-rank升序 price-desc-rank降序

                            doc_jd = Jsoup.connect(url_jd).get();
                            doc_yhd = Jsoup.connect(url_yhd).get();
                            doc_ymx = Jsoup.connect(url_ymx).get();
                            String urlp_yhd = "";
                            String urlp_ymx = "";
                            String urlp_jd = "";
                            Elements items_jd = doc_jd.select("li.gl-item");
                            Elements items_yhd = doc_yhd.select("div.mod_search_pro");
                            Elements items_ymx = doc_ymx.select("li.s-result-item");
                            goods.clear();

                            for (int i = 0 ;i<5 && i<items_yhd.size();i++) {
                                Good good_yhd = new Good();
                                good_yhd.name = items_yhd.select("p.proName.clearfix").get(i).text();
                                good_yhd.price  = items_yhd.select("p.proPrice").select("em").get(i).text();
                                urlp_yhd = items_yhd.select("a img[style]").get(i).attr("src")+items_yhd.select("a img[style]").get(i).attr("original");
                                good_yhd.image = getBitmap("http:"+urlp_yhd);
                                good_yhd.logo = R.drawable.yhd;
                                good_yhd.url = items_yhd.select("p.proName.clearfix").select("a").get(i).attr("href");
                                good_yhd.site = "yhd";
                                goods.add(good_yhd);
                            }
                            for (int i = 0 ;i<5 && i<items_ymx.size();i++) {
                                Good good_ymx = new Good();
                                good_ymx.name = items_ymx.select(".s-access-title").get(i).text();
                                good_ymx.price = items_ymx.select("span.s-price").get(i).text();
                                urlp_ymx = items_ymx.select("img").get(i).attr("src");
                                good_ymx.image = getBitmap(urlp_ymx);
                                good_ymx.logo = R.drawable.ymx;
                                good_ymx.url = items_ymx.select("a").get(i).attr("href");
                                good_ymx.site = "ymx";
                                goods.add(good_ymx);

                            }

                            for(int i =0 ;i<5 && i<items_jd.size();i++){
                                Good good_jd = new Good();
                                urlp_jd = items_jd.select("div.p-img a img").get(i).attr("src")+items_jd.select("div.p-img a img").get(i).attr("data-lazy-img");
                                good_jd.name = items_jd.select("div.p-name").select("a").get(i).text();
                                good_jd.price = items_jd.select("div.p-price").get(i).text();
                                good_jd.image = getBitmap("http:"+urlp_jd);
                                good_jd.logo = R.drawable.jd;
                                good_jd.url = items_jd.select("div.p-img").get(i).attr("href");
                                good_jd.site = "jd";
                                goods.add(good_jd);
                            }


                            handler.sendEmptyMessage(0x0001);
                        } catch (Exception e) {
                            Toast.makeText(MainActivity.this, "Error: " + e, Toast.LENGTH_SHORT).show();
                        }
                    }
                }).start();
                return true;
            case R.id.sort_desc:
                if(flag==2 || flag==-1) return super.onOptionsItemSelected(item);
                flag=2;
                pd = ProgressDialog.show(MainActivity.this, "Sorting", "Wait plz... :)");
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String keyword = query_sort;
                        Document doc_yhd,doc_ymx,doc_jd;
                        try {

                            String url_jd = "https://search.jd.com/Search?keyword="+keyword+"&enc=utf-8"+"&psort="+"1"+"&click=0";//销量3新品5价格升序2价格降序1
                            String url_yhd = "http://search.yhd.com/c0-0-0/b/a-s"+"4"+"-v4-p1-price-d0-f0-m1-rt0-pid-mid0-k"+keyword;//销量2新品6,价格升序3降序4
                            String url_ymx = "https://www.amazon.cn/s/ref=sr_st_"+"price-desc-rank"+"?__mk_zh_CN=亚马逊网站&url=search-alias%3Daps&field-keywords="+keyword+"&sort="+"price-desc-rank";
                            //relevanceblender相关性 date_desc_rank新品  price-asc-rank升序 price-desc-rank降序

                            doc_jd = Jsoup.connect(url_jd).get();
                            doc_yhd = Jsoup.connect(url_yhd).get();
                            doc_ymx = Jsoup.connect(url_ymx).get();
                            String urlp_yhd = "";
                            String urlp_ymx = "";
                            String urlp_jd = "";
                            Elements items_jd = doc_jd.select("li.gl-item");
                            Elements items_yhd = doc_yhd.select("div.mod_search_pro");
                            Elements items_ymx = doc_ymx.select("li.s-result-item");
                            goods.clear();

                            for (int i = 0 ;i<5 && i<items_yhd.size();i++) {
                                Good good_yhd = new Good();
                                good_yhd.name = items_yhd.select("p.proName.clearfix").get(i).text();
                                good_yhd.price  = items_yhd.select("p.proPrice").select("em").get(i).text();
                                urlp_yhd = items_yhd.select("a img[style]").get(i).attr("src")+items_yhd.select("a img[style]").get(i).attr("original");
                                good_yhd.image = getBitmap("http:"+urlp_yhd);
                                good_yhd.logo = R.drawable.yhd;
                                good_yhd.url = items_yhd.select("p.proName.clearfix").select("a").get(i).attr("href");
                                good_yhd.site = "yhd";
                                goods.add(good_yhd);
                            }
                            for (int i = 0 ;i<5 && i<items_ymx.size();i++) {
                                Good good_ymx = new Good();
                                good_ymx.name = items_ymx.select(".s-access-title").get(i).text();
                                good_ymx.price = items_ymx.select("span.s-price").get(i).text();
                                urlp_ymx = items_ymx.select("img").get(i).attr("src");
                                good_ymx.image = getBitmap(urlp_ymx);
                                good_ymx.logo = R.drawable.ymx;
                                good_ymx.url = items_ymx.select("a").get(i).attr("href");
                                good_ymx.site = "ymx";
                                goods.add(good_ymx);

                            }

                            for(int i =0 ;i<5 && i<items_jd.size();i++){
                                Good good_jd = new Good();
                                urlp_jd = items_jd.select("div.p-img a img").get(i).attr("src")+items_jd.select("div.p-img a img").get(i).attr("data-lazy-img");
                                good_jd.name = items_jd.select("div.p-name").select("a").get(i).text();
                                good_jd.price = items_jd.select("div.p-price").get(i).text();
                                good_jd.image = getBitmap("http:"+urlp_jd);
                                good_jd.logo = R.drawable.jd;
                                good_jd.url = items_jd.select("div.p-img").get(i).attr("href");
                                good_jd.site = "jd";
                                goods.add(good_jd);
                            }


                            handler.sendEmptyMessage(0x0001);
                        } catch (Exception e) {
                            Toast.makeText(MainActivity.this, "Error: " + e, Toast.LENGTH_SHORT).show();
                        }
                    }
                }).start();
                return true;
            case R.id.sort_sale:
                if(flag==3 || flag==-1) return super.onOptionsItemSelected(item);
                flag=3;
                pd = ProgressDialog.show(MainActivity.this, "Sorting", "Wait plz... :)");
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String keyword = query_sort;
                        Document doc_yhd,doc_ymx,doc_jd;
                        try {

                            String url_jd = "https://search.jd.com/Search?keyword="+keyword+"&enc=utf-8"+"&psort="+"3"+"&click=0";//销量3新品5价格升序2价格降序1
                            String url_yhd = "http://search.yhd.com/c0-0-0/b/a-s"+"2"+"-v4-p1-price-d0-f0-m1-rt0-pid-mid0-k"+keyword;//销量2新品6,价格升序3降序4
                            String url_ymx = "https://www.amazon.cn/s/ref=sr_st_"+"popularity-rank"+"?__mk_zh_CN=亚马逊网站&url=search-alias%3Daps&field-keywords="+keyword+"&sort="+"popularity-rank";
                            //relevanceblender相关性 date_desc_rank新品  price-asc-rank升序 price-desc-rank降序

                            doc_jd = Jsoup.connect(url_jd).get();
                            doc_yhd = Jsoup.connect(url_yhd).get();
                            doc_ymx = Jsoup.connect(url_ymx).get();
                            String urlp_yhd = "";
                            String urlp_ymx = "";
                            String urlp_jd = "";
                            Elements items_jd = doc_jd.select("li.gl-item");
                            Elements items_yhd = doc_yhd.select("div.mod_search_pro");
                            Elements items_ymx = doc_ymx.select("li.s-result-item");
                            goods.clear();

                            for (int i = 0 ;i<5 && i<items_yhd.size();i++) {
                                Good good_yhd = new Good();
                                good_yhd.name = items_yhd.select("p.proName.clearfix").get(i).text();
                                good_yhd.price  = items_yhd.select("p.proPrice").select("em").get(i).text();
                                urlp_yhd = items_yhd.select("a img[style]").get(i).attr("src")+items_yhd.select("a img[style]").get(i).attr("original");
                                good_yhd.image = getBitmap("http:"+urlp_yhd);
                                good_yhd.logo = R.drawable.yhd;
                                good_yhd.url = items_yhd.select("p.proName.clearfix").select("a").get(i).attr("href");
                                good_yhd.site = "yhd";
                                goods.add(good_yhd);
                            }
                            for (int i = 0 ;i<5 && i<items_ymx.size();i++) {
                                Good good_ymx = new Good();
                                good_ymx.name = items_ymx.select(".s-access-title").get(i).text();
                                good_ymx.price = items_ymx.select("span.s-price").get(i).text();
                                urlp_ymx = items_ymx.select("img").get(i).attr("src");
                                good_ymx.image = getBitmap(urlp_ymx);
                                good_ymx.logo = R.drawable.ymx;
                                good_ymx.url = items_ymx.select("a").get(i).attr("href");
                                good_ymx.site = "ymx";
                                goods.add(good_ymx);

                            }

                            for(int i =0 ;i<5 && i<items_jd.size();i++){
                                Good good_jd = new Good();
                                urlp_jd = items_jd.select("div.p-img a img").get(i).attr("src")+items_jd.select("div.p-img a img").get(i).attr("data-lazy-img");
                                good_jd.name = items_jd.select("div.p-name").select("a").get(i).text();
                                good_jd.price = items_jd.select("div.p-price").get(i).text();
                                good_jd.image = getBitmap("http:"+urlp_jd);
                                good_jd.logo = R.drawable.jd;
                                good_jd.url = items_jd.select("div.p-img").get(i).attr("href");
                                good_jd.site = "jd";
                                goods.add(good_jd);
                            }


                            handler.sendEmptyMessage(0x0001);
                        } catch (Exception e) {
                            Toast.makeText(MainActivity.this, "Error: " + e, Toast.LENGTH_SHORT).show();
                        }
                    }
                }).start();
                return true;
            case R.id.sort_new:
                if(flag==4 || flag==-1) return super.onOptionsItemSelected(item);
                flag=4;
                pd = ProgressDialog.show(MainActivity.this, "Sorting", "Wait plz... :)");
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String keyword = query_sort;
                        Document doc_yhd,doc_ymx,doc_jd;
                        try {

                            String url_jd = "https://search.jd.com/Search?keyword="+keyword+"&enc=utf-8"+"&psort="+"5"+"&click=0";//销量3新品5价格升序2价格降序1
                            String url_yhd = "http://search.yhd.com/c0-0-0/b/a-s"+"6"+"-v4-p1-price-d0-f0-m1-rt0-pid-mid0-k"+keyword;//销量2新品6,价格升序3降序4
                            String url_ymx = "https://www.amazon.cn/s/ref=sr_st_"+"date-desc-rank"+"?__mk_zh_CN=亚马逊网站&url=search-alias%3Daps&field-keywords="+keyword+"&sort="+"date-desc-rank";
                            //relevanceblender相关性 date-desc-rank新品  price-asc-rank升序 price-desc-rank降序

                            doc_jd = Jsoup.connect(url_jd).get();
                            doc_yhd = Jsoup.connect(url_yhd).get();
                            doc_ymx = Jsoup.connect(url_ymx).get();
                            String urlp_yhd = "";
                            String urlp_ymx = "";
                            String urlp_jd = "";
                            Elements items_jd = doc_jd.select("li.gl-item");
                            Elements items_yhd = doc_yhd.select("div.mod_search_pro");
                            Elements items_ymx = doc_ymx.select("li.s-result-item");
                            goods.clear();

                            for (int i = 0 ;i<5 && i<items_yhd.size();i++) {
                                Good good_yhd = new Good();
                                good_yhd.name = items_yhd.select("p.proName.clearfix").get(i).text();
                                good_yhd.price  = items_yhd.select("p.proPrice").select("em").get(i).text();
                                urlp_yhd = items_yhd.select("a img[style]").get(i).attr("src")+items_yhd.select("a img[style]").get(i).attr("original");
                                good_yhd.image = getBitmap("http:"+urlp_yhd);
                                good_yhd.logo = R.drawable.yhd;
                                good_yhd.url = items_yhd.select("p.proName.clearfix").select("a").get(i).attr("href");
                                good_yhd.site = "yhd";
                                goods.add(good_yhd);
                            }
                            for (int i = 0 ;i<5 && i<items_ymx.size();i++) {
                                Good good_ymx = new Good();
                                good_ymx.name = items_ymx.select(".s-access-title").get(i).text();
                                good_ymx.price = items_ymx.select("span.s-price").get(i).text();
                                urlp_ymx = items_ymx.select("img").get(i).attr("src");
                                good_ymx.image = getBitmap(urlp_ymx);
                                good_ymx.logo = R.drawable.ymx;
                                good_ymx.url = items_ymx.select("a").get(i).attr("href");
                                good_ymx.site = "ymx";
                                goods.add(good_ymx);

                            }

                            for(int i =0 ;i<5 && i<items_jd.size();i++){
                                Good good_jd = new Good();
                                urlp_jd = items_jd.select("div.p-img a img").get(i).attr("src")+items_jd.select("div.p-img a img").get(i).attr("data-lazy-img");
                                good_jd.name = items_jd.select("div.p-name").select("a").get(i).text();
                                good_jd.price = items_jd.select("div.p-price").get(i).text();
                                good_jd.image = getBitmap("http:"+urlp_jd);
                                good_jd.logo = R.drawable.jd;
                                good_jd.url = items_jd.select("div.p-img").get(i).attr("href");
                                good_jd.site = "jd";
                                goods.add(good_jd);
                            }


                            handler.sendEmptyMessage(0x0001);
                        } catch (Exception e) {
                            Toast.makeText(MainActivity.this, "Error: " + e, Toast.LENGTH_SHORT).show();
                        }
                    }
                }).start();
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
