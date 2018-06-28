package com.syst.apps.myjournalalc;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class Journal implements Serializable{

    private String jid = "";

    public String getJkey() {
        return jkey;
    }

    public void setJkey(String jkey) {
        this.jkey = jkey;
    }

    private String jkey = "";
    private String uid;

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    private String jdate;
    private String jentry;
    private String jmod;

    public Journal(String jid, String uid, String jdate, String jentry, String jmod) {
        this.jid = jid;
        this.uid = uid;
        this.jdate = jdate;
        this.jentry = jentry;
        this.jmod = jmod;
    }

    Journal(){

    }
    public Journal(String jid, String jdate, String jentry, String jmod) {
        this.jid = jid;
        this.jdate = jdate;
        this.jentry = jentry;
        this.jmod = jmod;
    }

    @Override
    public String toString() {
        return "Journal{" +
                "jid='" + jid + '\'' +
                ", jkey='" + jkey + '\'' +
                ", jdate='" + jdate + '\'' +
                ", jentry='" + jentry + '\'' +
                ", jmod='" + jmod + '\'' +
                '}';
    }

    String jday;
    String jmth;

    public void setJdate(String jdate) {
        this.jdate = jdate;
    }



    public Journal(String date, String entry){
        setJdate(date);
        setJentry(entry);
    }
    public String getJid() {
        return jid;
    }

    public void setJid(String jid) {
        this.jid = jid;
    }



    public String getJsum() {
        if(jentry.length() <=100)
            return jentry;
        else
            return jentry.substring(0,100);
    }

    public String getJday() {
        if(!jdate.equals(""))
            return jdate.split("-")[0];
        return "";
    }

    public String getJmth() {
        if(!jdate.equals(""))
            return jdate.split("-")[1];
        return "";
    }

    public String getJdate() {
        return jdate;
    }

    public String getJentry() {
        return jentry;
    }

    public void setJentry(String jentry) {
        this.jentry = jentry;
    }

    public String getJmod() {
        return jmod;
    }

    public void setJmod(String jmod) {
        this.jmod = jmod;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        //result.put("jkey", jkey);
        result.put("jid", jid);
        result.put("jdate", jdate);
        result.put("jentry", jentry);
        result.put("jmod", jmod);


        return result;
    }
}
