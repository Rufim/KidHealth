package ru.constant.kidhealth.domain.models;

import android.os.Parcel;
import android.os.Parcelable;

import org.joda.time.DateTime;
import org.joda.time.DateTimeFieldType;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(of = {"id","startTime","finishTime","dayOfWeek"})
@ToString(of = {"id","startTime","finishTime","dayOfWeek"})
public class DayAction implements Parcelable, Serializable {

    private static DateTimeFormatter inputFormat = DateTimeFormat.forPattern("HH:mm:ss");
    
    String id;
    String title;
    String description;
    String type;
    Boolean active;
    WeekDay dayOfWeek;
    String startTime;
    String finishTime;
    String duration;
    String comment;

    transient boolean invalidated = false;
    transient DateTime start;
    transient DateTime end;
    
    public void invalidateTime() {
        if(start == null || end == null || !invalidated) {
            if(startTime != null && finishTime != null) {
                start = inputFormat.parseDateTime(getStartTime());
                end = inputFormat.parseDateTime(getFinishTime());
                start = start.withDate(LocalDate.now());
                start = start.withField(DateTimeFieldType.dayOfWeek(), getDayOfWeek().ordinal() + 1);
                end = end.withDate(LocalDate.now());
                end = end.withField(DateTimeFieldType.dayOfWeek(), getDayOfWeek().ordinal() + 1);
                invalidated = true;
            } else {
                invalidated = false;
            }
        }
    }

    public boolean isRunning() {
        invalidateTime();
        if(!invalidated) return false;
        DateTime now = DateTime.now();
        return getActive() && now.isAfter(start) && now.isBefore(end);
    }
    
    public DayAction() {}

    protected DayAction(Parcel in) {
        id = in.readString();
        title = in.readString();
        description = in.readString();
        type = in.readString();
        byte tmpActive = in.readByte();
        active = tmpActive == 0 ? null : tmpActive == 1;
        startTime = in.readString();
        finishTime = in.readString();
        duration = in.readString();
        comment = in.readString();
        invalidateTime();
    }

    public static final Creator<DayAction> CREATOR = new Creator<DayAction>() {
        @Override
        public DayAction createFromParcel(Parcel in) {
            return new DayAction(in);
        }

        @Override
        public DayAction[] newArray(int size) {
            return new DayAction[size];
        }
    };

    public Boolean getActive() {
        return active == null ? false : active;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(title);
        dest.writeString(description);
        dest.writeString(type);
        dest.writeByte((byte) (active == null ? 0 : active ? 1 : 2));
        dest.writeString(startTime);
        dest.writeString(finishTime);
        dest.writeString(duration);
        dest.writeString(comment);
    }
}
