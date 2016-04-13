package com.glucopred.model;

import java.util.List;

import io.realm.RealmResults;

/**
 * Created by Ju on 2016/4/13.
 */
public class HistorianResults {
    public HistorianResults(TrendMode trendMode) {
        this.trendMode = trendMode;
    }

    public RealmResults<GlucopredData> getGlucopreds() {
        return glucopreds;
    }

    public void setGlucopreds(RealmResults<GlucopredData> glucopreds) {
        this.glucopreds = glucopreds;
    }

    RealmResults<GlucopredData> glucopreds;

    public RealmResults<FingerPrick> getFingerPricks() {
        return fingerPricks;
    }

    public void setFingerPricks(RealmResults<FingerPrick> fingerPricks) {
        this.fingerPricks = fingerPricks;
    }

    RealmResults<FingerPrick> fingerPricks;

    public List<TrendData> getAverageGlucopreds() {
        return averageGlucopreds;
    }

    public void setAverageGlucopreds(List<TrendData> averageGlucopreds) {
        this.averageGlucopreds = averageGlucopreds;
    }

    List<TrendData> averageGlucopreds;

    public TrendMode getTrendMode() {
        return trendMode;
    }

    public void setTrendMode(TrendMode trendMode) {
        this.trendMode = trendMode;
    }

    TrendMode trendMode;
}
