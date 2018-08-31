package com.agricx.lab.data;

public class Constants {

    public static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;
    public static final int REQUEST_CHECK_SETTINGS = 0x1;
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 5000;
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    public static final String CONNECT_TO_WIFI = "WIFI";
    public static final String CONNECT_TO_MOBILE = "MOBILE";
    public static final String NOT_CONNECT = "NOT_CONNECT";

    public static final String DATABASE_NAME = "AgricxDB";
    public static final int DATABASE_VERSION = 1;

    public static final String TABLE_NAME = "agricxinfo";

    public static final String DB_FETCH_QUERY = "SELECT * FROM " + TABLE_NAME;
    public static final String DB_DELETE_QUERY = "DELETE FROM " + TABLE_NAME;


}
