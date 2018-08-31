package com.agricx.lab.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class AgricxDbHelper extends SQLiteOpenHelper {
    private static final String SQL_CREATE_ENTRIES =
            String.format("CREATE TABLE %s (FirstName TEXT,LastName TEXT,City TEXT,Education TEXT,Age TEXT,BirthDate TEXT,UserType TEXT,Location TEXT)", Constants.TABLE_NAME);

    private static final String SQL_DELETE_ENTRIES =
            String.format("DROP TABLE IF EXISTS %s", Constants.TABLE_NAME);

    public AgricxDbHelper(Context context) {
        super(context, Constants.DATABASE_NAME, null, Constants.DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
