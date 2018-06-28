package com.syst.apps.myjournalalc;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class MyDbHelper extends SQLiteOpenHelper {


    private static final String DATABASE_NAME = "myJdb";
    private static final int DATABASE_VERSION = 1;
    private static MyDbHelper getInstance;
    Context mContext;
    private static SharedPreferences sharedPref;


    static final String JR_TB = "journals";
    static final String JMOD = "jmod";
    static final String JCON = "jcont";
    static final String JDATE = "jdate";
    static final String JID = "_id_";
    static final String JKEY = "_jkey_";

    private final String CREATE_JRTB = "CREATE TABLE IF NOT EXISTS "
            + JR_TB + "("
            + JID+" INTEGER PRIMARY KEY, "
            + JDATE + " VARCHAR, "
            + JKEY + " VARCHAR, "
            + JMOD + " VARCHAR(1) default 0, "
            + JCON + " VARCHAR, UNIQUE("+JDATE+") ON CONFLICT IGNORE )";

    public MyDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
    }

    public static synchronized MyDbHelper getInstance(Context context) {
        if (getInstance == null) {
            getInstance = new MyDbHelper(context.getApplicationContext());
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        }
        return getInstance;
    }
    public MyDbHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public MyDbHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, DatabaseErrorHandler errorHandler) {
        super(context, name, factory, version, errorHandler);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_JRTB);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        onCreate(db);
        super.onOpen(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public boolean addJournal(Journal j) {
        try {
            showLog(j.toString()+"\tisDigit: "+j.getJid());

            SQLiteDatabase db = this.getWritableDatabase();

            ContentValues values = new ContentValues();
            if(!j.getJid().equals(""))
                values.put(JID, j.getJid());
            values.put(JDATE, j.getJdate());
            values.put(JKEY, j.getJkey());
            values.put(JCON, j.getJentry());
            values.put(JMOD, j.getJmod());
            long swid = 0;
            if(j.getJid().equals("")){
                // insert row
                //showLog("Insertion Called");
                swid = db.insert(JR_TB, null, values);
                if(swid>0)
                    j.setJid(swid+"");
                showLog("Insertion Called Result: "+swid);
            }
            else {
                swid = db.update(JR_TB,values,JID+"=?",new String[]{j.getJid()});
                if(swid <= 0)
                   swid = db.insert(JR_TB, null, values);  
            }

            if(swid>0)
                return true;
            else
                return false;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public ArrayList<Journal> getJournals(int number) {
        ArrayList<Journal> nots = new ArrayList<>();
        try{
            SQLiteDatabase db = this.getReadableDatabase();
            String selQuery = "SELECT * FROM "+JR_TB +"  ORDER BY datetime("+JDATE+") DESC ";
            Cursor c = db.rawQuery(selQuery,null);
            //wrd.showLog("getVV  "+c.getCount());
            if(c.getCount()>0 && c.moveToFirst()) {
                do {
                    Journal j = new Journal();
                    j.setJid(c.getInt(c.getColumnIndex(JID)) + "");
                    j.setJentry(c.getString(c.getColumnIndex(JCON)));
                    j.setJdate(c.getString(c.getColumnIndex(JDATE)));
                    j.setJkey(c.getString(c.getColumnIndex(JKEY)));
                    j.setJmod(c.getString(c.getColumnIndex(JMOD)));
                    nots.add(j);

                }while (c.moveToNext());
                c.close();
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }

        return nots;
    }

    void showLog(String msg){
        Log.e("myDbHelper",msg);
    }
    void showSnack(String msg, View v){
        Snackbar.make(v,msg,Snackbar.LENGTH_SHORT).show();
    }
    public static synchronized SharedPreferences getSharedPref(Context c) {
        if(sharedPref == null){
            sharedPref = PreferenceManager.getDefaultSharedPreferences(c);
        }
        return sharedPref;
    }
    public void saveStrSharedPref(String s, String value){
        SharedPreferences.Editor edit = getSharedPref(mContext).edit();
        edit.putString(s, value);
        edit.commit();
    }

    public boolean delJournal(Journal j) {
        SQLiteDatabase db = this.getWritableDatabase();
        if(db.delete(JR_TB,JKEY+"=?",new String[]{j.getJkey()})>0)
            return true;
        else
            return false;

    }
    float getTextSize(int size) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,20, mContext.getResources().getDisplayMetrics());
    }
}
