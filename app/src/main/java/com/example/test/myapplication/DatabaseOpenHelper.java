package com.example.test.myapplication;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;

import java.io.ByteArrayOutputStream;


public class DatabaseOpenHelper extends SQLiteOpenHelper {

    SharedPreferences user;
    Context ctx;

    public DatabaseOpenHelper(Context context, String name, int version)
    {
        super(context, name, null, version);
        ctx = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        db.execSQL("CREATE TABLE collection (id integer primary key autoincrement, name varchar(200), price varchar(20), image blob, site varchar(10), url varchar(100), user varchar(50))");
        db.execSQL("CREATE TABLE users (id integer primary key autoincrement, email varchar(50), password varchar(50), name varchar(50))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        db.execSQL("DROP TABLE IF EXISTS collection");
        db.execSQL("DROP TABLE IF EXISTS users");
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
        cv.put("url", good.url);
        user = ctx.getSharedPreferences("user", 0);
        String name = user.getString("name", null);
        if (name == null) {
            return -1;
        }
        else {
            cv.put("user", name);
        }
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
        user = ctx.getSharedPreferences("user", 0);
        String name = user.getString("name", null);
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(false, "collection", null, "user=?", new String[] {name}, null, null, null, null);
        return cursor;
    }

    public String check_login(String email, String password)
    {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query("users", null, "email=? AND password=?", new String[] {email, password}, null, null, null);
        if (cursor.getCount() == 1) {
            cursor.moveToFirst();
            return cursor.getString(cursor.getColumnIndex("name"));
        }
        else {
            return null;
        }
    }

    public boolean signup(String email, String password, String name)
    {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query("users", null, "email=?", new String[] {email}, null, null, null);
        if (cursor.getCount() > 0) {
            return false;
        }
        else {
            ContentValues cv = new ContentValues();
            cv.put("email", email);
            cv.put("password", password);
            cv.put("name", name);
            long result = db.insert("users", null, cv);
            if (result != -1)
                return true;
            else
                return false;
        }
    }

}
