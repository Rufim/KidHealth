package ru.constant.kidhealth.domain.models;

import android.support.annotation.NonNull;

import org.joda.time.DateTime;

import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class LogStatus implements Serializable, Comparable{

    private static DateTime nullDate = new DateTime(0,1,1,0,0,0);

    private int id;
    private ActionStatus status;
    private DateTime dateTime;

    @Override
    public int compareTo(@NonNull Object o) {
        if(!(o instanceof LogStatus)) throw new IllegalArgumentException();
        LogStatus other = (LogStatus) o;
        DateTime left = other.dateTime;
        DateTime right = dateTime;
        if(right == null) {
            right = nullDate;
        }
        if(left == null) {
            left = nullDate;
        }
        return -right.compareTo(left); //desc
    }
}
