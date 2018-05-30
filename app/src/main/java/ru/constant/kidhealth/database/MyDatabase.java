package ru.constant.kidhealth.database;

import com.raizlabs.android.dbflow.annotation.ConflictAction;
import com.raizlabs.android.dbflow.annotation.Database;
import com.raizlabs.android.dbflow.annotation.Migration;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.SQLiteType;
import com.raizlabs.android.dbflow.sql.migration.AlterTableMigration;

import ru.constant.kidhealth.Constants;
import ru.constant.kidhealth.domain.models.DayAction;
import ru.constant.kidhealth.domain.models.DayAction_Table;

@Database(name = Constants.App.DATABASE_NAME, version = Constants.App.DATABASE_VERSION, insertConflict = ConflictAction.REPLACE, updateConflict = ConflictAction.REPLACE)
public class MyDatabase {
    @Migration(version = 2, database = MyDatabase.class)
    public static class MigrationDayAction extends AlterTableMigration<DayAction> {


        public MigrationDayAction() {
            super(DayAction.class);
        }

        @Override
        public void onPreMigrate() {
            //Fucking right way to do it NO documentation FFFFUUUUUUU!!!!
            addForeignKeyColumn(SQLiteType.TEXT, DayAction_Table.nextDayAction_id.getNameAlias().nameRaw(), FlowManager.getTableName(DayAction.class) + "(`id`) ");
            addForeignKeyColumn(SQLiteType.TEXT, DayAction_Table.prevDayAction_id.getNameAlias().nameRaw(), FlowManager.getTableName(DayAction.class) + "(`id`) ");
        }
    }

}