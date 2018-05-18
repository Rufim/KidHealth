package ru.constant.kidhealth.domain.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.annimon.stream.Stream;
import com.google.gson.annotations.SerializedName;
import com.raizlabs.android.dbflow.annotation.ColumnIgnore;
import com.raizlabs.android.dbflow.annotation.ConflictAction;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

import org.joda.time.DateTime;
import org.joda.time.DateTimeFieldType;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.io.Serializable;
import java.util.SortedSet;
import java.util.TreeSet;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import ru.constant.kidhealth.database.MyDatabase;
import ru.kazantsev.template.domain.Validatable;
import ru.kazantsev.template.util.TextUtils;

@Data
@EqualsAndHashCode(of = {"id", "startTime", "finishTime", "dayOfWeek"}, callSuper = false)
@ToString(of = {"id", "startTime", "finishTime", "dayOfWeek"})
@Table(database = MyDatabase.class, allFields = true, updateConflict = ConflictAction.REPLACE, insertConflict = ConflictAction.REPLACE)
public class DayAction extends BaseModel implements Parcelable, Serializable, Validatable {

    private static DateTimeFormatter timeFormat = DateTimeFormat.forPattern("HH:mm:ss");
    private static DateTimeFormatter dateTimeFormatter = ISODateTimeFormat.dateTime().withZone(DateTimeZone.UTC);

    @PrimaryKey
    String id;
    String title;
    String description;
    DayActionType type;
    Boolean active;
    WeekDay dayOfWeek;
    String actionDate;
    @SerializedName("startDateTime")
    DateTime start;
    @SerializedName("finishDateTime")
    DateTime end;
    String duration;
    String comment;
    @ColumnIgnore
    SortedSet<LogStatus> actionStatuses = new TreeSet<>();
    Boolean notified = false;
    Boolean started = false;
    Boolean stopped = false;
    Boolean finished = false;
    Boolean postponed = false;

    @ColumnIgnore
    transient boolean invalidated = false;

    protected DayAction(Parcel in) {
        id = in.readString();
        title = in.readString();
        description = in.readString();
        byte tmpActive = in.readByte();
        active = tmpActive == 0 ? null : tmpActive == 1;
        actionDate = in.readString();
        duration = in.readString();
        comment = in.readString();
        byte tmpNotified = in.readByte();
        notified = tmpNotified == 0 ? null : tmpNotified == 1;
        byte tmpStarted = in.readByte();
        started = tmpStarted == 0 ? null : tmpStarted == 1;
        byte tmpStopped = in.readByte();
        stopped = tmpStopped == 0 ? null : tmpStopped == 1;
        byte tmpFinished = in.readByte();
        finished = tmpFinished == 0 ? null : tmpFinished == 1;
        byte tmpPostponed = in.readByte();
        postponed = tmpPostponed == 0 ? null : tmpPostponed == 1;
        setStartDateTime(in.readString());
        setFinishDateTime(in.readString());
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

    public void invalidateTime() {
        if (start != null && end != null) {
            if(invalidated) return;
            start = start.withDate(LocalDate.now());
            start = start.withField(DateTimeFieldType.dayOfWeek(), getDayOfWeek().ordinal() + 1);
            end = end.withDate(LocalDate.now());
            end = end.withField(DateTimeFieldType.dayOfWeek(), getDayOfWeek().ordinal() + 1);
            if (getActionStatuses() != null && getActionStatuses().size() > 0) {
                LogStatus status = getActionStatuses().first();
                switch (status.getStatus()) {
                    case STARTED:
                        setStarted(true);
                        setStopped(false);
                        setFinished(false);
                        break;
                    case STOPPED:
                        setStarted(false);
                        setStopped(true);
                        setFinished(false);
                        break;
                    case FINISHED:
                        setStarted(true);
                        setStopped(false);
                        setFinished(true);
                        break;
                }
                setPostponed(Stream.of(getActionStatuses()).anyMatch(stat -> stat.getStatus().equals(ActionStatus.POSTPONED)));
            }
            invalidated = true;
        } else {
            invalidated = false;
        }
    }

    public boolean isRunning() {
        invalidateTime();
        if (!invalidated) return false;
        DateTime now = DateTime.now();
        return isActive() && ((isStarted() && !isStopped() && !isFinished()) || (!isStopped() && !isFinished() && (now.isAfter(start) && now.isBefore(end))));
    }

    public Boolean isNotified() {
        return notified == null ? false : notified;
    }

    public Boolean isStarted() {
        return started == null ? false : started;
    }

    public Boolean isStopped() {
        return stopped == null ? false : stopped;
    }

    public Boolean isFinished() {
        return finished == null ? false : finished;
    }

    public Boolean isActive() {
        return (active == null ? false : active);
    }

    public Boolean isPostponed() {
        return postponed == null ? false : postponed;
    }

    @Override
    public boolean isValid() {
        invalidateTime();
        return TextUtils.notEmpty(id) && title != null && invalidated;
    }

    public DayAction() {
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
        dest.writeByte((byte) (active == null ? 0 : active ? 1 : 2));
        dest.writeString(actionDate);
        dest.writeString(duration);
        dest.writeString(comment);
        dest.writeByte((byte) (notified == null ? 0 : notified ? 1 : 2));
        dest.writeByte((byte) (started == null ? 0 : started ? 1 : 2));
        dest.writeByte((byte) (stopped == null ? 0 : stopped ? 1 : 2));
        dest.writeByte((byte) (finished == null ? 0 : finished ? 1 : 2));
        dest.writeByte((byte) (postponed == null ? 0 : postponed ? 1 : 2));
        dest.writeString(getStartDateTime());
        dest.writeString(getFinishDateTime());
    }


    public String getStartTime() {
        return timeFormat.print(start);
    }

    public String getFinishTime() {
        return timeFormat.print(end);
    }


    public String getStartDateTime() {
        return dateTimeFormatter.print(start);
    }

    public String getFinishDateTime() {
        return dateTimeFormatter.print(end);
    }


    public void setStartDateTime(String startTime) {
        start = dateTimeFormatter.parseDateTime(startTime);
    }


    public void setFinishDateTime(String finishTime) {
        end =dateTimeFormatter.parseDateTime(finishTime);
    }
}
