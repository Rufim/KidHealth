package ru.constant.kidhealth.domain.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import org.joda.time.DateTime;

import java.io.Serializable;

import lombok.Data;

@Data
public class LogStatus implements Serializable, Comparable, Parcelable {

    private static DateTime nullDate = new DateTime(0,1,1,0,0,0);

    private int id;
    private ActionStatus status;
    private DateTime dateTime;

    protected LogStatus(Parcel in) {
        id = in.readInt();
        status = ActionStatus.valueOf(in.readString());
        dateTime = DayAction.DATE_TIME_FORMATTER.parseDateTime(in.readString());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(status.getName());
        dest.writeString(DayAction.DATE_TIME_FORMATTER.print(dateTime));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<LogStatus> CREATOR = new Creator<LogStatus>() {
        @Override
        public LogStatus createFromParcel(Parcel in) {
            return new LogStatus(in);
        }

        @Override
        public LogStatus[] newArray(int size) {
            return new LogStatus[size];
        }
    };

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
