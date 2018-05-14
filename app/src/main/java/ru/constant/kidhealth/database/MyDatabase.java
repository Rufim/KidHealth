package ru.constant.kidhealth.database;

import com.raizlabs.android.dbflow.annotation.ConflictAction;
import com.raizlabs.android.dbflow.annotation.Database;

import ru.constant.kidhealth.Constants;

@Database(name = Constants.App.DATABASE_NAME, version = Constants.App.DATABASE_VERSION, insertConflict = ConflictAction.REPLACE, updateConflict = ConflictAction.REPLACE)
public class MyDatabase {

}