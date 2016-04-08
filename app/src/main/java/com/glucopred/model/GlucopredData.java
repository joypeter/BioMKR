package com.glucopred.model;

import java.util.Date;
import io.realm.RealmObject;
import io.realm.annotations.Ignore;

/**
 * Created by Ju on 2016/3/31.
 */
public class GlucopredData extends RealmObject {
    private long timestamp;
    private double value;

    @Ignore
    private Date dateTime;

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public double getValue() {
        return this.value;
    }

    public Date getDateTime() {
        if (dateTime == null)
            dateTime = new Date(timestamp);
        return this.dateTime;
    }
}
