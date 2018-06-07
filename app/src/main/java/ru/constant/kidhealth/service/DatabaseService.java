package ru.constant.kidhealth.service;

import android.content.Context;
import android.support.annotation.NonNull;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.raizlabs.android.dbflow.config.DatabaseDefinition;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.language.OperatorGroup;
import com.raizlabs.android.dbflow.sql.language.OrderBy;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.raizlabs.android.dbflow.structure.Model;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;
import com.raizlabs.android.dbflow.structure.database.transaction.ProcessModelTransaction;
import com.raizlabs.android.dbflow.structure.database.transaction.Transaction;

import net.vrallev.android.cat.Cat;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import ru.constant.kidhealth.Constants;
import ru.constant.kidhealth.domain.models.DayAction;
import ru.constant.kidhealth.domain.models.DayActionType;
import ru.constant.kidhealth.domain.models.DayAction_Table;
import ru.constant.kidhealth.domain.models.WeekDay;
import ru.kazantsev.template.util.DBFlowUtils;

public class DatabaseService {

    public enum Action {INSERT, UPDATE, DELETE, UPSERT}

    private Context mContext;

    public DatabaseService(Context mContext) {
        this.mContext = mContext;
    }

    public void insertOrUpdateScheduleForWeek(Map<WeekDay, List<DayAction>> weekDayActions) {
        List<DayAction> oldActions = SQLite.select().from(DayAction.class).queryList();
        List<DayAction> newActions = new ArrayList<>();
        for (WeekDay weekDay : WeekDay.values()) {
            newActions.addAll(weekDayActions.get(weekDay));
        }
        DBFlowUtils.dbFlowDelete(DayAction.class);
        oldActions.retainAll(newActions);
        Stream.of(newActions).forEach(action -> {
            if(action.isValid() && !oldActions.contains(action)) {
                oldActions.add(action);
            }
        });
        doAction(Action.INSERT, oldActions);
    }

    public void insertOrUpdateScheduleForDay(WeekDay weekDay, List<DayAction> newActions) {
        List<DayAction> oldActions = SQLite.select().from(DayAction.class).where(DayAction_Table.dayOfWeek.eq(weekDay)).queryList();
        DBFlowUtils.dbFlowDelete(DayAction.class, DayAction_Table.dayOfWeek.eq(weekDay));
        oldActions.retainAll(newActions);
        Stream.of(newActions).forEach(action -> {
            if(action.isValid() && !oldActions.contains(action)) {
                oldActions.add(action);
            }
        });
        doAction(Action.INSERT, oldActions);
    }

    public List<DayAction> getDayActions(WeekDay weekDay) {
        return SQLite.select()
                .from(DayAction.class)
                .where(DayAction_Table.dayOfWeek.eq(weekDay))
                .orderBy(DayAction_Table.start, true)
                .queryList();
    }

    public DayAction getDayAction(String actionId) {
        return DBFlowUtils.dbFlowFindFirst(DayAction.class, DayAction_Table.id.eq(actionId));
    }

    public DayAction nextDayAction(DateTime now) {
        return SQLite.select()
                .from(DayAction.class)
                .where(DayAction_Table.start.greaterThan(now))
                .and(DayAction_Table.active.eq(true))
                .and(DayAction_Table.prevDayAction_id.isNull())
                .and(OperatorGroup.clause().or(DayAction_Table.finished.eq(false)).or(DayAction_Table.finished.isNull()))
                .and(OperatorGroup.clause().or(DayAction_Table.started.eq(false)).or(DayAction_Table.started.isNull()))
                //.and(OperatorGroup.clause().or(DayAction_Table.type.eq(DayActionType.EDUCATION)).or(DayAction_Table.type.eq(DayActionType.TRAINING)))
                .and(OperatorGroup.clause().or(DayAction_Table.notified.eq(false)).or(DayAction_Table.notified.isNull()))
                .orderBy(DayAction_Table.start, true)
                .querySingle();
    }

    public void notifyDayAction(DayAction action) {
        action.setNotified(true);
        doAction(Action.UPDATE, action);
    }

    public void startDayAction(DayAction dayAction) {
        if(dayAction != null && dayAction.isValid()) {
            dayAction.forAll(action -> {
                action.setStarted(true);
                action.setStopped(false);
                action.setFinished(false);
                doAction(Action.UPDATE, action);
            });
        }
    }

    public void stopDayAction(DayAction dayAction) {
        if(dayAction != null && dayAction.isValid()) {
            dayAction.forAll(action -> {
                action.setStopped(true);
                action.setStarted(false);
                action.setFinished(false);
                doAction(Action.UPDATE, action);
            });
        }
    }

    public void finishDayAction(DayAction dayAction) {
        if(dayAction != null && dayAction.isValid()) {
            dayAction.forAll(action -> {
                action.setStarted(true);
                action.setStopped(false);
                action.setFinished(true);
                doAction(Action.UPDATE, action);
            });
        }
    }


    public void postponeDayAction(DayAction dayAction) {
        if(dayAction != null && dayAction.isValid()) {
            dayAction.forAll(action -> {
                action.setPostponed(true);
                action.setStarted(false);
                action.setStopped(false);
                action.setFinished(false);
                doAction(Action.UPDATE, action);
            });
        }
    }


    public <C extends BaseModel> C doAction(Action action, C value) {
        return doAction(action, value, false);
    }

    public <C extends BaseModel> void doAction(Action action, Collection<C> list) {
        doAction(action, list, false);
    }

    public <C extends BaseModel> void doAction(Action action, Collection<C> list, boolean async) {
        ProcessModelTransaction<C> processModelTransaction =
                new ProcessModelTransaction.Builder<>(new ProcessModelTransaction.ProcessModel<C>() {
                    @Override
                    public void processModel(C model, DatabaseWrapper wrapper) {
                        doAction(action, model);
                    }
                }).addAll(list).build();
        DatabaseDefinition database = FlowManager.getDatabase(Constants.App.DATABASE_NAME);
        Transaction transaction = database.beginTransactionAsync(processModelTransaction).error(new Transaction.Error() {
            @Override
            public void onError(@NonNull Transaction transaction, @NonNull Throwable error) {
                Cat.e("Error in DB operation:" + action + ".", error);
            }
        }).build();
        if (async) {
            transaction.execute();
        } else {
            transaction.executeSync();
        }
    }


    public <C extends BaseModel> C doAction(Action action, C value, boolean async) {
        boolean result = false;
        Model model = value;
        if(async) {
            model = value.async();
        }
        switch (action) {
            case INSERT:
                result = model.insert() > 0;
                break;
            case UPDATE:
                result = model.update();
                break;
            case DELETE:
                result = model.delete();
                break;
            case UPSERT:
                result = model.save();
                break;
        }
        if (!result) {
            Cat.e("Error in DB operation:" + action + ". See log for more info. Class:" + value.getClass());
        }
        return value;
    }

}
