package com.glucopred.model;

import android.content.Context;

import com.glucopred.utils.Utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * Created by Ju on 2016/3/31.
 */
public class HistorianAgent {
    private Realm realm;
    private int maxStoreDays = 7;
    private static long INTERVAL_MILLISECONDS = 1000 * 30;              //every 30 seconds one signal

    public HistorianAgent(Context context) {
        RealmConfiguration realmConfig = new RealmConfiguration.Builder(context).build();
        realm = Realm.getInstance(realmConfig);

        //for test
        //clearAllData();

        keepWeekData();

        //for test
        simulateData();
    }

    private void simulateData() {
        try {
            Random random = new Random();

            long now = (new Date()).getTime();
            long startTimestamp =  now - Utils.DAY_MILLISECONDS * maxStoreDays;          //start from one week ago
            long endTimestamp = now;
            long timestamp = startTimestamp;

            long firstTimestamp = 0;
            if (realm.where(GlucopredData.class).count() > 0)
                firstTimestamp = realm.where(GlucopredData.class).min("timestamp").longValue();
            if (firstTimestamp > 0)
                endTimestamp = firstTimestamp;

            realm.beginTransaction();
            int i = 0;
            while (timestamp < endTimestamp) {
                double value = 5.0 + random.nextInt(10) / 10.0;

                GlucopredData gd = realm.createObject(GlucopredData.class);
                gd.setTimestamp(timestamp);
                gd.setValue(value);

                timestamp += INTERVAL_MILLISECONDS;

                i++;
                if (i > Utils.DAY_MILLISECONDS / INTERVAL_MILLISECONDS / 4) {
                    FingerPrick fp = realm.createObject(FingerPrick.class);
                    fp.setTimestamp(timestamp);
                    fp.setValue(value);
                    i = 0;
                }
            }
            realm.commitTransaction();
        } catch (Exception ex) {
        }
    }

    private void clearAllData() {
        try {
            realm.beginTransaction();
            realm.allObjects(GlucopredData.class).clear();
            realm.allObjects(FingerPrick.class).clear();
            realm.commitTransaction();
        } catch (Exception ex) {
            return;
        }
    }

    private void keepWeekData() {
        try {
            long firstTimeStamp = (new Date()).getTime() - Utils.DAY_MILLISECONDS * maxStoreDays;

            RealmResults<GlucopredData> results = realm.where(GlucopredData.class).lessThan("timestamp", firstTimeStamp).findAll();
            RealmResults<FingerPrick> fingers = realm.where(FingerPrick.class).lessThan("timestamp", firstTimeStamp).findAll();

            realm.beginTransaction();
            results.clear();
            fingers.clear();
            realm.commitTransaction();
        } catch (Exception ex) {
        }
    }

    public HistorianResults getHistorian(TrendMode mode) {
        HistorianResults historianResults = new HistorianResults(mode);

        switch (mode) {
            case REALTIME:
                long starttime = (new Date()).getTime() - Utils.HOUR_MILLISECONDS * 2;

                historianResults.glucopreds = realm.where(GlucopredData.class).greaterThan("timestamp", starttime).findAllSorted("timestamp");
                historianResults.fingerPricks = realm.where(FingerPrick.class).greaterThan("timestamp", starttime).findAllSorted("timestamp");
                break;
            case TODAY:
                starttime = Utils.getDayStart(new Date());

                historianResults.glucopreds = realm.where(GlucopredData.class).greaterThan("timestamp", starttime).findAllSorted("timestamp");
                historianResults.glucopreds = realm.where(GlucopredData.class).greaterThan("timestamp", starttime).findAllSorted("timestamp");
                break;
            case YESTERDAY:
                starttime = Utils.getDayStart(new Date((new Date()).getTime() - Utils.DAY_MILLISECONDS));
                long endtime = starttime + Utils.DAY_MILLISECONDS;

                historianResults.glucopreds = realm.where(GlucopredData.class).between("timestamp", starttime, endtime).findAllSorted("timestamp");
                historianResults.fingerPricks = realm.where(FingerPrick.class).between("timestamp", starttime, endtime).findAllSorted("timestamp");
                break;
            case WEEK:
                starttime = (new Date()).getTime() - Utils.DAY_MILLISECONDS * 7;

                historianResults.averageGlucopreds = getWeekAverageGlucopred();
                historianResults.fingerPricks = realm.where(FingerPrick.class).greaterThan("timestamp", starttime).findAllSorted("timestamp");
                break;
        }

        return historianResults;
    }

    private List<TrendData> getTodayAverageGlucopred() {
        ArrayList<TrendData> dataList = new ArrayList<TrendData>();

        long now = (new Date()).getTime();
        long timestamp = Utils.getDayStart(new Date(now));

        while (timestamp < now) {
            TrendData data = new TrendData();

            Date time = new Date(timestamp);
            double value = getMinuteAverage(time);

            DateFormat df = new SimpleDateFormat("HH:mm");
            String timeString = df.format(time);

            data.setTimeString(timeString);
            data.setValue(value);
            dataList.add(data);

            timestamp += Utils.HOUR_MILLISECONDS;
        }

        return dataList;
    }

    private List<TrendData> getYesterdayAverageGlucopred() {
        List<TrendData> dataList = new ArrayList<TrendData>();

        long timestamp = (new Date()).getTime() - Utils.DAY_MILLISECONDS;
        timestamp = Utils.getDayStart(new Date(timestamp));
        long endstamp = timestamp + Utils.DAY_MILLISECONDS;

        while (timestamp < endstamp) {
            TrendData data = new TrendData();

            Date time = new Date(timestamp);
            double value = getMinuteAverage(time);

            DateFormat df = new SimpleDateFormat("HH:mm");
            String timeString = df.format(time);

            data.setTimeString(timeString);
            data.setValue(value);
            dataList.add(data);

            timestamp += Utils.MINUTE_MILLISECONDS;
        }

        return dataList;
    }

    private List<TrendData> getWeekAverageGlucopred() {
        List<TrendData> dataList = new ArrayList<TrendData>();

        long nowstamp = (new Date()).getTime();
        long timestamp = nowstamp - Utils.DAY_MILLISECONDS * maxStoreDays;

        while (timestamp < nowstamp) {
            TrendData data = new TrendData();

            Date time = new Date(timestamp);
            double value = getHourAverage(time);

            DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            String timeString = df.format(time);

            data.setTimeString(timeString);
            data.setValue(value);
            dataList.add(data);

            timestamp += Utils.HOUR_MILLISECONDS;
        }

        return dataList;
    }

    private List<TrendData> getCurrentGlucopredBySeconds(int seconds) {
        List<TrendData> dataList = new ArrayList<TrendData>();

        long now = (new Date()).getTime();

        long lastTimesatmp = now;
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

    public void pushGlucopred(Date time, double value) {
        try {
            long timestamp = time.getTime();

            addGlucopredData(timestamp, value);
        } catch (Exception ex) {
        }
    }

    public void pushCurrentFinger(Date time, double finger) {
        try {
            long timestamp = time.getTime();

            addFingerPrick(timestamp, finger);
        } catch (Exception ex) {
        }
    }

    private void addGlucopredData(long timestamp, double value) {
        try {
            realm.beginTransaction();
            GlucopredData gd = realm.createObject(GlucopredData.class);
            gd.setTimestamp(timestamp);
            gd.setValue(value);
            realm.commitTransaction();
        } catch (Exception ex) {
        }
    }

    private void addFingerPrick(long timestamp, double value) {
        try {
            FingerPrick latestFinger = realm.where(FingerPrick.class).findAllSorted("timestamp", Sort.DESCENDING).first();
            if (value == latestFinger.getValue())
                return;

            realm.beginTransaction();
            FingerPrick fp = realm.createObject(FingerPrick.class);
            fp.setTimestamp(timestamp);
            fp.setValue(value);
            realm.commitTransaction();
        } catch (Exception ex) {
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
        }
    }

    private double getDayAverage(Date date) {
        long dayStart = Utils.getDayStart(date);
        long dayEnd = Utils.getDayEnd(date);

        //RealmResults<GlucopredData> dd = realm.where(GlucopredData.class).between("timestamp", dayStart, dayEnd).findAll();
        double average = realm.where(GlucopredData.class).between("timestamp", dayStart, dayEnd).average("value");
        return average;
    }

    private double getHourAverage(Date date) {
        long hourStart = Utils.getHourStart(date);
        long hourEnd = Utils.getHourEnd(date);

        double average = realm.where(GlucopredData.class).between("timestamp", hourStart, hourEnd).average("value");
        return average;
    }

    private double getMinuteAverage(Date date) {
        long minuteStart = Utils.getMinuteStart(date);
        long minuteEnd = Utils.getMinuteEnd(date);

        double average = realm.where(GlucopredData.class).between("timestamp", minuteStart, minuteEnd).average("value");
        return average;
    }

    public void close() {
        realm.close();
    }
}
