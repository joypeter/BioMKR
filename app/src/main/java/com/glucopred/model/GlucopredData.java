package com.glucopred.model;

import java.util.Date;
import io.realm.RealmObject;
import io.realm.annotations.Ignore;

/**
 * Created by Ju on 2016/3/31.
 */
public class GlucopredData extends RealmObject {
    private long timeStamp;
    private double value;

    @Ignore
    private Date dateTime;

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public long getTimeStamp() {
        return this.timeStamp;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public double getValue() {
        return this.value;
    }

    public Date getDateTime() {
        if (dateTime == null)
            dateTime = new Date(timeStamp);
        return this.dateTime;
    }
}
