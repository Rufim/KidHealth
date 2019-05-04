package ru.constant.kidhealth.domain.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.google.gson.annotations.SerializedName;
import com.raizlabs.android.dbflow.annotation.ColumnIgnore;
import com.raizlabs.android.dbflow.annotation.ConflictAction;
import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

import org.joda.time.DateTime;
import org.joda.time.DateTimeFieldType;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import ru.constant.kidhealth.database.MyDatabase;
import ru.kazantsev.template.domain.Validatable;
import ru.kazantsev.template.util.Delegate;
import ru.kazantsev.template.util.TextUtils;

@Data
@EqualsAndHashCode(of = {"id", "start", "end", "dayOfWeek"}, callSuper = false)
@ToString(of = {"id", "start", "end", "dayOfWeek"})
@Table(database = MyDatabase.class, allFields = true, updateConflict = ConflictAction.REPLACE, insertConflict = ConflictAction.REPLACE)
public class DayAction extends BaseModel implements Parcelable, Serializable, Validatable {

    static DateTimeFormatter TIME_FORMAT = DateTimeFormat.forPattern("HH:mm:ss");
    static DateTimeFormatter DATE_TIME_FORMATTER = ISODateTimeFormat.dateTime();

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
    @ForeignKey(stubbedRelationship = true)
    DayAction nextDayAction;
    @ForeignKey(stubbedRelationship = true)
    DayAction prevDayAction;
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
        String day = in.readString();
        if(day != null) dayOfWeek = WeekDay.valueOf(day);
        ArrayList<LogStatus> logStatuses = new ArrayList<>();
        in.readTypedList(logStatuses, LogStatus.CREATOR);
        setActionStatuses(new TreeSet<>(logStatuses));
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

    public DayAction nextDayAction() {
        if(nextDayAction ==  null)  {
            return null;
        } else {
            if(!nextDayAction.isValid()) {
                nextDayAction.load();
            }
            return nextDayAction;
        }
    }

    public DayAction prevDayAction() {
        if(prevDayAction ==  null)  {
            return null;
        } else {
            if(!prevDayAction.isValid()) {
                prevDayAction.load();
            }
            return prevDayAction;
        }
    }

    public  SortedSet<LogStatus> getCurrentStatuses() {
        TreeSet<LogStatus> statuses = new TreeSet<>();
        if(getActionStatuses() == null || getActionStatuses() .isEmpty() || getStart() == null) return  statuses;
        final AtomicBoolean first = new AtomicBoolean(true);
        return Stream.of(getActionStatuses())
                .filter(status -> {
                    DateTime statusTime = status.getDateTime();
                    boolean filter = statusTime != null && ((statusTime.isAfter(getFirstAction().getStart()) && statusTime.isBefore(getLastAction().getEnd())) || (ActionStatus.FINISHED.equals(status.getStatus()) && first.get()));
                    first.set(false);
                    return filter;
                })
                .collect(Collectors.toCollection(() -> statuses));
    }

    public void invalidateTime() {
        if (start != null && end != null && getDayOfWeek() != null) {
            if(invalidated) return;
            start = start.withDate(LocalDate.now());
            start = start.withField(DateTimeFieldType.dayOfWeek(), getDayOfWeek().ordinal() + 1);
            end = end.withDate(LocalDate.now());
            end = end.withField(DateTimeFieldType.dayOfWeek(), getDayOfWeek().ordinal() + 1);
            SortedSet<LogStatus> statuses = getCurrentStatuses();
            if (statuses.size() > 0) {
                LogStatus status = statuses.first();
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
                setPostponed(Stream.of(statuses).anyMatch(stat -> stat.getStatus().equals(ActionStatus.POSTPONED)));
            }
            if(prevDayAction != null) {
                notified = true;
            }
            if(prevDayAction != null) {
                prevDayAction.setNextDayAction(this);
            }
            if(nextDayAction != null) {
                nextDayAction.setPrevDayAction(this);
            }
            invalidated = true;
        } else {
            invalidated = false;
        }
    }

    public DayAction getLastAction() {
        DayAction prevAction = nextDayAction();
        if(prevAction == null) return this;
        DayAction result = prevAction;
        while (prevAction != null) {
            result = prevAction;
            result.setActionStatuses(getActionStatuses());
            result.invalidateTime();
            prevAction = prevAction.nextDayAction();
        }
        return result;
    }


    public DayAction getFirstAction() {
        DayAction nextAction = prevDayAction();
        if(nextAction == null) return this;
        DayAction result = nextAction;
        while (nextAction != null) {
            result = nextAction;
            result.setActionStatuses(getActionStatuses());
            result.invalidateTime();
            nextAction = nextAction.prevDayAction();
        }
        return result;
    }

    public boolean isRunning() {
        invalidateTime();
        if (!invalidated) return false;
        DateTime now = DateTime.now();
        DateTime start = getFirstAction().getStart();
        DateTime end = getLastAction().getEnd();
        return isActive()
                && ((isStarted() && !isStopped() && !isFinished()) || (!isStopped() && !isFinished() && (now.isAfter(start) && now.isBefore(end))));
             //   && (getType().equals(DayActionType.TRAINING) || getType().equals(DayActionType.EDUCATION));
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

    public DayAction setNotified(Boolean notified) {
        this.notified = notified;
        return this;
    }

    public DayAction setStarted(Boolean started) {
        this.started = started;
        return this;
    }

    public DayAction setStopped(Boolean stopped) {
        this.stopped = stopped;
        return this;
    }

    public DayAction setFinished(Boolean finished) {
        this.finished = finished;
        return this;
    }

    public DayAction setPostponed(Boolean postponed) {
        this.postponed = postponed;
        return this;
    }

    public void forAll(Delegate<DayAction> delegate) {
        delegate.call(this);
        DayAction action = this;
        while ((action = action.nextDayAction()) != null) {
            delegate.call(action);
        }
        action = this;
        while ((action = action.prevDayAction()) != null) {
            delegate.call(action);
        }
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
        dest.writeString(dayOfWeek != null ? dayOfWeek.name() : null);
        dest.writeTypedList(new ArrayList<>(actionStatuses));
    }


    public String getStartTime() {
        return TIME_FORMAT.print(start);
    }

    public String getFinishTime() {
        return TIME_FORMAT.print(end);
    }


    public String getStartDateTime() {
        return DATE_TIME_FORMATTER.print(start);
    }

    public String getFinishDateTime() {
        return DATE_TIME_FORMATTER.print(end);
    }


    public void setStartDateTime(String startTime) {
        start = DATE_TIME_FORMATTER.parseDateTime(startTime);
    }


    public void setFinishDateTime(String finishTime) {
        end = DATE_TIME_FORMATTER.parseDateTime(finishTime);
    }
}
