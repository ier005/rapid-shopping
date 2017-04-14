package com.example.test.myapplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class DatabaseOpenHelper extends SQLiteOpenHelper {

    public DatabaseOpenHelper(Context context, String name, int version)
    {
        super(context, name, null, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        db.execSQL("CREATE TABLE collection (id integer primary key autoincrement, name varchar(200), price varchar(20), image blob, site varchar(10))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        db.execSQL("DROP TABLE IF EXISTS person");
        onCreate(db);
    }

    public long insertGood(Good good)
    {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("image", img(good.image));
        cv.put("name", good.name);
        cv.put("price", good.price);
        cv.put("site", good.site);
        long result = db.insert("collection", null, cv);
        return result;
    }

    private byte[] img(Bitmap image)
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }

    public Cursor getCollection()
    {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(false, "collection", null, null, null, null, null, null, null);
        return cursor;
    }

}
