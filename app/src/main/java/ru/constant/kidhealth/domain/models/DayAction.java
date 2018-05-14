package ru.constant.kidhealth.domain.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ColumnIgnore;
import com.raizlabs.android.dbflow.annotation.ConflictAction;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

import org.joda.time.DateTime;
import org.joda.time.DateTimeFieldType;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import ru.constant.kidhealth.database.MyDatabase;
import ru.constant.kidhealth.database.converter.DateTimeConverter;
import ru.kazantsev.template.domain.Validatable;
import ru.kazantsev.template.util.TextUtils;

@Data
@EqualsAndHashCode(of = {"id","startTime","finishTime","dayOfWeek"}, callSuper = false)
@ToString(of = {"id","startTime","finishTime","dayOfWeek"})
@Table(database = MyDatabase.class, allFields = true, updateConflict = ConflictAction.REPLACE, insertConflict = ConflictAction.REPLACE)
public class DayAction extends BaseModel implements Parcelable, Serializable, Validatable {

    private static DateTimeFormatter inputFormat = DateTimeFormat.forPattern("HH:mm:ss");

    @PrimaryKey
    String id;
    String title;
    String description;
    String type;
    Boolean active;
    WeekDay dayOfWeek;
    String startTime;
    String finishTime;
    String actionDate;
    String duration;
    String comment;
    Boolean notified = false;
    Boolean started = false;
    Boolean finished = false;

    @ColumnIgnore
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

    public Boolean getNotified() {
        return notified == null ? false :notified;
    }

    public Boolean getStarted() {
        return started == null ? false :started;
    }

    public Boolean getFinished() {
        return finished == null ? false :finished;
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

    @Override
    public boolean isValid() {
        invalidateTime();
        return TextUtils.notEmpty(id) && title != null && invalidated;
    }
}
