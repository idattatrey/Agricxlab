package com.agricx.lab.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.widget.Toast;

import com.agricx.lab.MainActivity;
import com.agricx.lab.data.Constants;
import com.agricx.lab.data.AgricxDbHelper;
import com.agricx.lab.utils.NetworkUtil;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class NetworkChangeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Context mContext = context;
        AgricxDbHelper mDbHelper = new AgricxDbHelper(mContext);
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        String status = NetworkUtil.getConnectivityStatusString(context);
        Log.e("Receiver ", "" + status);
        if (!status.equals(Constants.NOT_CONNECT)) {
            Cursor cursor = db.rawQuery(Constants.DB_FETCH_QUERY, null);
            while (cursor.moveToNext()) {
                mDatabase.child("FirstName").setValue(cursor.getString(cursor.getColumnIndexOrThrow("FirstName")));
                mDatabase.child("LastName").setValue(cursor.getString(cursor.getColumnIndexOrThrow("LastName")));
                mDatabase.child("City").setValue(cursor.getString(cursor.getColumnIndexOrThrow("City")));
                mDatabase.child("Education").setValue(cursor.getString(cursor.getColumnIndexOrThrow("Education")));
                mDatabase.child("Age").setValue(cursor.getString(cursor.getColumnIndexOrThrow("Age")));
                mDatabase.child("BirthDate").setValue(cursor.getString(cursor.getColumnIndexOrThrow("BirthDate")));
                mDatabase.child("UserType").setValue(cursor.getString(cursor.getColumnIndexOrThrow("UserType")));
                mDatabase.child("Location").setValue(cursor.getString(cursor.getColumnIndexOrThrow("Location")));
                Toast.makeText(context, "All your date uploaded!", Toast.LENGTH_SHORT).show();
                db.execSQL(Constants.DB_DELETE_QUERY);
            }
            cursor.close();
            Log.e("Receiver ", "Connection");
        }
    }
}
