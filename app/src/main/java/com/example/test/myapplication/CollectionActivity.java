package com.example.test.myapplication;


import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;


public class CollectionActivity extends AppCompatActivity {

    DatabaseOpenHelper db;
    ListView collectionList;
    LVAdapter adapter;
    Cursor cursor;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collection);

        db = new DatabaseOpenHelper(this, "rp_db.db3", 1);
        cursor = db.getCollection();
        collectionList = (ListView) findViewById(R.id.collectionList);
        adapter = new LVAdapter();
        collectionList.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy()
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
            return cursor.getCount();
        }

        @Override
        public Object getItem(int position)
        {
            return position;
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
            cursor.moveToPosition(position);
            viewHolder.name.setText(cursor.getString(cursor.getColumnIndex("name")));
            viewHolder.price.setText(cursor.getString(cursor.getColumnIndex("price")));
            switch (cursor.getString(cursor.getColumnIndex("site"))) {
                case "yhd" :
                    viewHolder.logo.setImageResource(R.drawable.yhd);
            }
            byte[] in = cursor.getBlob((cursor.getColumnIndex("image")));
            viewHolder.image.setImageBitmap(BitmapFactory.decodeByteArray(in, 0, in.length));
            return convertView;
        }
    }

}
