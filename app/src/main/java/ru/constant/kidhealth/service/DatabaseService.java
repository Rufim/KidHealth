package ru.constant.kidhealth.service;

import android.content.Context;
import android.support.annotation.NonNull;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.raizlabs.android.dbflow.config.DatabaseDefinition;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.language.OrderBy;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.raizlabs.android.dbflow.structure.Model;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;
import com.raizlabs.android.dbflow.structure.database.transaction.ProcessModelTransaction;
import com.raizlabs.android.dbflow.structure.database.transaction.Transaction;

import net.vrallev.android.cat.Cat;

import org.joda.time.DateTime;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import ru.constant.kidhealth.Constants;
import ru.constant.kidhealth.domain.models.DayAction;
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
        DBFlowUtils.dbFlowDelete(DayAction.class);
        for (WeekDay weekDay : WeekDay.values()) {
            doAction(Action.INSERT, Stream.of(weekDayActions.get(weekDay)).filter(DayAction::isValid).collect(Collectors.toList()));
        }
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
                .orderBy(DayAction_Table.start, true)
                .querySingle();
    }

    public void notifyDayAction(DayAction action) {
        action.setNotified(true);
        doAction(Action.UPDATE, action);
    }

    public void startDayAction(DayAction dayAction) {
        if(dayAction != null && dayAction.isValid()) {
            dayAction.setStarted(true);
            dayAction.setFinished(false);
            doAction(Action.UPDATE, dayAction);
        }
    }

    public void finishDayAction(DayAction dayAction) {
        if(dayAction != null && dayAction.isValid()) {
            dayAction.setStarted(true);
            dayAction.setFinished(true);
            doAction(Action.UPDATE, dayAction);
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
