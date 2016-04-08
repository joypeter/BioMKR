package com.glucopred.model;

import android.content.Context;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

/**
 * Created by Ju on 2016/3/31.
 */
public class HistorianAgent {
    private Realm realm;
    private int maxStoreDays = 7;
    private static long INTERVAL_MILLESECONDS = 1000 * 30;              //every 30 seconds one signal
    private static long DAY_MILLISECONDS = 60 * 60 * 24 * 1000;
    private static long HOUR_MILLISECONDS = 60 * 60 * 1000;

    public HistorianAgent(Context context) {
        RealmConfiguration realmConfig = new RealmConfiguration.Builder(context).build();
        realm = Realm.getInstance(realmConfig);

        //for test
        //clearAllData();

        keepWeekData();

        //for test
        simulateData();
    }

    public void simulateData() {
        try {
            Random random = new Random();

            long now = (new Date()).getTime();
            long timestamp =  now - DAY_MILLISECONDS * maxStoreDays;          //start from one week ago

            long firstTimestamp = 0;
            if (realm.where(GlucopredData.class).count() > 0)
                firstTimestamp = realm.where(GlucopredData.class).min("timestamp").longValue();//findAllSorted("timestamp").first().getTimestamp();
            long endTimestamp = now;
            if (firstTimestamp > 0)
                endTimestamp = firstTimestamp;

            realm.beginTransaction();
            while (timestamp < endTimestamp) {
                double value = 5.0 + random.nextInt(10) / 10.0;

                GlucopredData gd = realm.createObject(GlucopredData.class);
                gd.setTimestamp(timestamp);
                gd.setValue(value);

                timestamp += INTERVAL_MILLESECONDS;
            }
            realm.commitTransaction();

            int size = getDataSize();
        } catch (Exception ex) {
            return;
        }
    }

    public void pushCurrent(double value) {
        try {
            Date date = new Date();
            long timestamp = date.getTime();

            // Add a data
            addLast(timestamp, value);

        } catch (Exception ex) {
        }
    }

    public void pushNewData(Date date, double value) {
        try {
            long timestamp = date.getTime();

            addLast(timestamp, value);
        } catch (Exception ex) {
        }
    }

    public int getDataSize() {
        int size = realm.allObjects(GlucopredData.class).size();
        return size;
    }

    public void clearAllData() {
        try {
            realm.beginTransaction();
            realm.allObjects(GlucopredData.class).clear();
            realm.commitTransaction();
        } catch (Exception ex) {
            return;
        }
    }

    public void keepWeekData() {
        try {
            long firstTimeStamp = (new Date()).getTime() - DAY_MILLISECONDS * maxStoreDays;

            RealmResults<GlucopredData> results = realm.where(GlucopredData.class).lessThan("timestamp", firstTimeStamp).findAll();
            if (results.size() <=0)
                return;

            realm.beginTransaction();
            results.clear();
            realm.commitTransaction();
        } catch (Exception ex) {
            return;
        }
    }

    public ArrayList<TrendData> getWeekData() {
        ArrayList<TrendData> dataList = new ArrayList<TrendData>();

        long timestamp = (new Date()).getTime() - DAY_MILLISECONDS * maxStoreDays;

        for (int i = 0; i <= maxStoreDays; i++) {
            TrendData data = new TrendData();

            Date time = new Date(timestamp);
            double value = getDayAverage(time);

            DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            String timeString = df.format(time);

            data.setTimeString(timeString);
            data.setValue(value);
            dataList.add(data);

            timestamp += DAY_MILLISECONDS;
        }

        return dataList;
    }

    public ArrayList<TrendData> getYesterdayData() {
        ArrayList<TrendData> dataList = new ArrayList<TrendData>();

        long timestamp = (new Date()).getTime() - DAY_MILLISECONDS;
        timestamp = getDayStart(new Date(timestamp));

        for (int i = 0; i < 24; i++) {
            TrendData data = new TrendData();

            Date time = new Date(timestamp);
            double value = getHourAverage(time);

            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            String timeString = df.format(time);

            data.setTimeString(timeString);
            data.setValue(value);
            dataList.add(data);

            timestamp += HOUR_MILLISECONDS;
        }

        return dataList;
    }

    public ArrayList<TrendData> getTodayData() {
        ArrayList<TrendData> dataList = new ArrayList<TrendData>();

        long now = (new Date()).getTime();
        long timestamp = getDayStart(new Date(now));

        while (timestamp < now) {
            TrendData data = new TrendData();

            Date time = new Date(timestamp);
            double value = getHourAverage(time);

            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            String timeString = df.format(time);

            data.setTimeString(timeString);
            data.setValue(value);
            dataList.add(data);

            timestamp += HOUR_MILLISECONDS;
        }

        return dataList;
    }

    public ArrayList<TrendData> getCurrentDataBySeconds(int seconds) {
        ArrayList<TrendData> dataList = new ArrayList<TrendData>();

        long now = (new Date()).getTime();

        long lastTimesatmp = realm.where(GlucopredData.class).max("timestamp").longValue();
        long timestamp = lastTimesatmp - seconds * 1000;

        RealmResults<GlucopredData> results = realm.where(GlucopredData.class).greaterThan("timestamp", timestamp).findAllSorted("timestamp");

        for (int i=0; i<results.size(); i++) {
            TrendData data = new TrendData();
            GlucopredData gd = results.get(i);

            Date time = new Date(gd.getTimestamp());
            DateFormat df = new SimpleDateFormat("HH:mm:ss");
            String timeString = df.format(time);

            double value = gd.getValue();

            data.setTimeString(timeString);
            data.setValue(value);
            dataList.add(data);
        }

        return dataList;
    }

    private void addLast(long timestamp, double value) {
        try {
            realm.beginTransaction();
            GlucopredData gd = realm.createObject(GlucopredData.class);
            gd.setTimestamp(timestamp);
            gd.setValue(value);
            realm.commitTransaction();
        } catch (Exception ex) {
            return;
        }
    }

    /*
    remove the beginning data, if
     */
    private void removeFirst() {
        try {
            GlucopredData firstData = realm.where(GlucopredData.class).findAllSorted("timestamp").first();

            realm.beginTransaction();
            firstData.removeFromRealm();
            realm.commitTransaction();

            //RealmResults<GlucopredData> results2 = realm.where(GlucopredData.class).findAll();
        } catch(Exception ex){
            return;
        }
    }

    private double getDayAverage(Date date) {
        long dayStart = getDayStart(date);
        long dayEnd = getDayEnd(date);

        //RealmResults<GlucopredData> dd = realm.where(GlucopredData.class).between("timestamp", dayStart, dayEnd).findAll();
        double average = realm.where(GlucopredData.class).between("timestamp", dayStart, dayEnd).average("value");
        return average;
    }

    private double getHourAverage(Date date) {
        long hourStart = getHourStart(date);
        long hourEnd = getHourEnd(date);

        double average = realm.where(GlucopredData.class).between("timestamp", hourStart, hourEnd).average("value");
        return average;
    }

    private Long getDayStart(Date date){
        Calendar dayStart = Calendar.getInstance();
        dayStart.setTime(date);
        dayStart.set(Calendar.HOUR_OF_DAY, 0);
        dayStart.set(Calendar.MINUTE, 0);
        dayStart.set(Calendar.SECOND, 0);
        dayStart.set(Calendar.MILLISECOND, 0);
        Date d = dayStart.getTime();
        return d.getTime();
    }

    private Long getDayEnd(Date date){
        Calendar dayEnd = Calendar.getInstance();
        dayEnd.setTime(date);
        dayEnd.set(Calendar.HOUR_OF_DAY, 23);
        dayEnd.set(Calendar.MINUTE, 59);
        dayEnd.set(Calendar.SECOND, 59);
        dayEnd.set(Calendar.MILLISECOND, 999);
        Date d = dayEnd.getTime();
        return d.getTime();
    }

    private long getHourStart(Date date) {
        Calendar hourStart = Calendar.getInstance();
        hourStart.setTime(date);
        hourStart.set(Calendar.MINUTE, 0);
        hourStart.set(Calendar.SECOND, 0);
        hourStart.set(Calendar.MILLISECOND, 0);
        return hourStart.getTime().getTime();
    }

    private long getHourEnd(Date date) {
        Calendar hourEnd = Calendar.getInstance();
        hourEnd.setTime(date);
        hourEnd.set(Calendar.MINUTE, 59);
        hourEnd.set(Calendar.SECOND, 59);
        hourEnd.set(Calendar.MILLISECOND, 999);
        return hourEnd.getTime().getTime();
    }

    public void close() {
        realm.close();
    }
}
