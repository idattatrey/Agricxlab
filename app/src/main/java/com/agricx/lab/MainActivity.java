package com.agricx.lab;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import com.agricx.lab.data.Constants;
import com.agricx.lab.data.AgricxDbHelper;
import com.agricx.lab.services.NetworkChangeReceiver;
import com.agricx.lab.utils.NetworkUtil;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.jaredrummler.materialspinner.MaterialSpinner;

import java.text.DecimalFormat;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.net.ConnectivityManager.CONNECTIVITY_ACTION;

public class MainActivity extends AppCompatActivity {
    @BindView(R.id.input_first_name)
    EditText etFirstName;
    @BindView(R.id.input_last_name)
    EditText etLastName;
    @BindView(R.id.input_city)
    EditText etCity;
    @BindView(R.id.input_education)
    EditText etEducation;
    @BindView(R.id.input_age)
    EditText etAge;
    @BindView(R.id.input_birth_date)
    EditText etBirthDate;
    @BindView(R.id.spinner_user_type)
    MaterialSpinner spinner;
    @BindView(R.id.input_location)
    EditText etLocation;
    private DatabaseReference mDatabase;

    private String TAG = MainActivity.class.getSimpleName();

    private FusedLocationProviderClient mFusedLocationClient;
    private SettingsClient mSettingsClient;
    private LocationRequest mLocationRequest;
    private LocationSettingsRequest mLocationSettingsRequest;
    private LocationCallback mLocationCallback;
    private Location mCurrentLocation;

    private Boolean mRequestingLocationUpdates;
    private AgricxDbHelper mDbHelper;
    private SQLiteDatabase db;
    IntentFilter intentFilter;
    NetworkChangeReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        mDbHelper = new AgricxDbHelper(getApplicationContext());
        db = mDbHelper.getWritableDatabase();

        if (!(NetworkUtil.getConnectivityStatus(MainActivity.this) > 0)) {
            Toast.makeText(this, "You are not connected to internet, please connect to proceed!", Toast.LENGTH_SHORT).show();
            Cursor cursor = db.rawQuery(Constants.DB_FETCH_QUERY, null);
            while (cursor.moveToNext()) {
                etFirstName.setText(cursor.getString(cursor.getColumnIndexOrThrow("FirstName")));
                etLastName.setText(cursor.getString(cursor.getColumnIndexOrThrow("LastName")));
                etCity.setText(cursor.getString(cursor.getColumnIndexOrThrow("City")));
                etEducation.setText(cursor.getString(cursor.getColumnIndexOrThrow("Education")));
                etAge.setText(cursor.getString(cursor.getColumnIndexOrThrow("Age")));
                etBirthDate.setText(cursor.getString(cursor.getColumnIndexOrThrow("BirthDate")));
                etLocation.setText(cursor.getString(cursor.getColumnIndexOrThrow("Location")));
            }
            cursor.close();
        }


        intentFilter = new IntentFilter();
        intentFilter.addAction(CONNECTIVITY_ACTION);
        receiver = new NetworkChangeReceiver();

        mDatabase = FirebaseDatabase.getInstance().getReference();
        spinner.setItems("Employee", "Admin", "Manager");

        mRequestingLocationUpdates = false;
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mSettingsClient = LocationServices.getSettingsClient(this);

        createLocationCallback();
        createLocationRequest();
        buildLocationSettingsRequest();
        startLocationUpdates();
    }

    @OnClick(R.id.imageView)
    public void selectDate() {
        dateDialog();
    }

    @OnClick(R.id.btn_submit)
    public void validateAndSendDetails() {
        boolean valid = true;

        String firstName = etFirstName.getText().toString();
        String lastName = etLastName.getText().toString();
        String city = etCity.getText().toString();
        String education = etEducation.getText().toString();
        String age = etAge.getText().toString();
        String birthDate = etBirthDate.getText().toString();
        String userType = String.valueOf(spinner.getItems().get(spinner.getSelectedIndex()));
        String location = etLocation.getText().toString();

        if (firstName.isEmpty() || firstName.length() < 3) {
            etFirstName.setError("At least 3 characters");
            valid = false;
        } else {
            etFirstName.setError(null);
        }

        if (lastName.isEmpty() || lastName.length() < 3) {
            etLastName.setError("At least 3 characters");
            valid = false;
        } else {
            etLastName.setError(null);
        }

        if (city.isEmpty() || city.length() < 3) {
            etCity.setError("At least 3 characters");
            valid = false;
        } else {
            etCity.setError(null);
        }

        if (education.isEmpty() || education.length() < 3) {
            etEducation.setError("At least 3 characters");
            valid = false;
        } else {
            etEducation.setError(null);
        }

        if (age.isEmpty() || Integer.parseInt(age) < 0) {
            etAge.setError("Enter Valid Age");
            valid = false;
        } else {
            etAge.setError(null);
        }

        if (birthDate.isEmpty() || birthDate.length() < 3) {
            etBirthDate.setError("Enter valid birth date");
            valid = false;
        } else {
            etBirthDate.setError(null);
        }

        if (location.isEmpty()) {
            etLocation.setError("Enter Valid Location");
            valid = false;
        } else {
            etLocation.setError(null);
        }

        if (valid) {
            if (!(NetworkUtil.getConnectivityStatus(MainActivity.this) > 0)) {
                ContentValues values = new ContentValues();
                values.put("FirstName", firstName);
                values.put("LastName", lastName);
                values.put("City", city);
                values.put("Education", education);
                values.put("Age", age);
                values.put("BirthDate", birthDate);
                values.put("UserType", userType);
                values.put("Location", location);
                db.insert(Constants.TABLE_NAME, null, values);
                Toast.makeText(this, "All your data saved successfully!", Toast.LENGTH_SHORT).show();
                resetUi();
            } else {
                mDatabase.child("FirstName").setValue(firstName);
                mDatabase.child("LastName").setValue(lastName);
                mDatabase.child("City").setValue(city);
                mDatabase.child("Education").setValue(education);
                mDatabase.child("Age").setValue(age);
                mDatabase.child("BirthDate").setValue(birthDate);
                mDatabase.child("UserType").setValue(userType);
                mDatabase.child("Location").setValue(location);
                db.rawQuery(Constants.DB_DELETE_QUERY, null);
                Toast.makeText(this, "All your date uploaded successfully!", Toast.LENGTH_SHORT).show();
                resetUi();
            }
        }

    }

    public void dateDialog() {
        final DecimalFormat decimalFormat = new DecimalFormat("00");
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        View dialogView = getLayoutInflater().inflate(R.layout.birth_date_dialog, null);
        DatePicker datePicker = dialogView.findViewById(R.id.datePicker);
        alertDialog.setView(dialogView);

        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", (dialog, which) -> {
            String leaveDate = decimalFormat.format(datePicker.getDayOfMonth()) + "-" + decimalFormat.format(datePicker.getMonth()) + "-" + datePicker.getYear();
            etBirthDate.setText(leaveDate);
        });

        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "CANCEL", (dialog, which) -> Toast.makeText(getApplicationContext(), "You have been cancelled the birth date selection! ", Toast.LENGTH_SHORT).show());

        new Dialog(getApplicationContext());
        alertDialog.show();
    }


    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(Constants.UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(Constants.FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void createLocationCallback() {
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                mCurrentLocation = locationResult.getLastLocation();
                updateLocationUI();
            }
        };
    }

    private void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case Constants.REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        Log.i(TAG, "User agreed to make required location settings changes.");
                        break;
                    case Activity.RESULT_CANCELED:
                        Log.i(TAG, "User chose not to make required location settings changes.");
                        mRequestingLocationUpdates = false;
                        updateUI();
                        break;
                }
                break;
        }
    }

    private void startLocationUpdates() {
        mSettingsClient.checkLocationSettings(mLocationSettingsRequest)
                .addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
                    @SuppressLint("MissingPermission")
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        Log.i(TAG, "All location settings are satisfied.");
                        mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                                mLocationCallback, Looper.myLooper());
                        updateUI();
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        int statusCode = ((ApiException) e).getStatusCode();
                        switch (statusCode) {
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                Log.i(TAG, "Location settings are not satisfied. Attempting to upgrade " +
                                        "location settings ");
                                try {
                                    ResolvableApiException rae = (ResolvableApiException) e;
                                    rae.startResolutionForResult(MainActivity.this, Constants.REQUEST_CHECK_SETTINGS);
                                } catch (IntentSender.SendIntentException sie) {
                                    Log.i(TAG, "PendingIntent unable to execute request.");
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                String errorMessage = "Location settings are inadequate, and cannot be " +
                                        "fixed here. Fix in Settings.";
                                Log.e(TAG, errorMessage);
                                Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                                mRequestingLocationUpdates = false;
                        }

                        updateUI();
                    }
                });
    }

    private void updateUI() {
        updateLocationUI();
    }

    private void updateLocationUI() {
        if (mCurrentLocation != null) {
            etLocation.setText(String.valueOf("Lat: " + mCurrentLocation.getLatitude() + ", Lon: " + mCurrentLocation.getLongitude()));
        }
    }

    private void stopLocationUpdates() {
        if (!mRequestingLocationUpdates) {
            Log.d(TAG, "stopLocationUpdates: updates never requested, no-op.");
            return;
        }
        mFusedLocationClient.removeLocationUpdates(mLocationCallback)
                .addOnCompleteListener(this, task -> mRequestingLocationUpdates = false);
    }

    @Override
    public void onResume() {
        super.onResume();
        registerReceiver(receiver, intentFilter);
        if (mRequestingLocationUpdates && checkPermissions()) {
            startLocationUpdates();
        } else if (!checkPermissions()) {
            requestPermissions();
        }
        updateUI();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
        stopLocationUpdates();
    }

    @Override
    protected void onStart() {
        super.onStart();
        startLocationUpdates();
    }

    private void showSnackbar(final int mainTextStringId, final int actionStringId,
                              View.OnClickListener listener) {
        Snackbar.make(
                findViewById(android.R.id.content),
                getString(mainTextStringId),
                Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(actionStringId), listener).show();
    }

    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        android.Manifest.permission.ACCESS_FINE_LOCATION);
        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.");
            showSnackbar(R.string.permission_rationale,
                    android.R.string.ok, view -> ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            Constants.REQUEST_PERMISSIONS_REQUEST_CODE));
        } else {
            Log.i(TAG, "Requesting permission");
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    Constants.REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.i(TAG, "onRequestPermissionResult");
        if (requestCode == Constants.REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                Log.i(TAG, "User interaction was cancelled.");
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (mRequestingLocationUpdates) {
                    Log.i(TAG, "Permission granted, updates requested, starting location updates");
                    startLocationUpdates();
                }
            } else {
                showSnackbar(R.string.permission_denied_explanation,
                        R.string.settings, view -> {
                            Intent intent = new Intent();
                            intent.setAction(
                                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri uri = Uri.fromParts("package",
                                    BuildConfig.APPLICATION_ID, null);
                            intent.setData(uri);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        });
            }
        }
    }

    public void resetUi() {
        etFirstName.setText("");
        etLastName.setText("");
        etCity.setText("");
        etEducation.setText("");
        etAge.setText("");
        etBirthDate.setText("");
        etLocation.setText("Lon: 0, Lat: 0");
    }

}
