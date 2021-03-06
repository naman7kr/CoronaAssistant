package com.suvidha.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.os.Bundle;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.suvidha.Models.FetchNgomodel;
import com.suvidha.Models.GetReportsModel;
import com.suvidha.Models.NgoModel;
import com.suvidha.R;


import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.suvidha.Utilities.APIClient;
import com.suvidha.Utilities.ApiInterface;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.Manifest.*;
import static android.content.pm.PackageManager.*;
import static com.suvidha.Utilities.Utils.LOCATION_PERMISSION_CODE;
import static com.suvidha.Utilities.Utils.createAlertDialog;
import static com.suvidha.Utilities.Utils.currentLocation;
import static com.suvidha.Utilities.Utils.getAccessToken;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback,GoogleMap.OnMarkerClickListener
{
    SupportMapFragment mapFragment;
    private FusedLocationProviderClient mFusedLocationClient;
    GoogleMap mMap;
    Location lastKnown;
    ApiInterface apiInterface;
    int flag = 0;
    private static final int GPS_REQUEST_CODE = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        intialiseRetrofit();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);
        if (checkLocationPermission()) {
            //first get current location as quarantine location
            //then open dialog
            if (canGetLocation()) {
                Log.e("TAG","LOLOLO");
                getCurrentLocation();
            } else {
                showSettingsAlert();
            }

        } else {
            requestLocationPermissions();
//                    Toast.makeText(getContexgetLocationUpdatest(), "You don't have location permission", Toast.LENGTH_SHORT).show();
        }

    }
    private boolean checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        return false;
    }
    public boolean canGetLocation() {
        boolean result = true;
        LocationManager lm = null;
        boolean gps_enabled = false;
        boolean network_enabled = false;
        if (lm == null)

            lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

        // exceptions will be thrown if provider is not permitted.
        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {

        }
        try {
            network_enabled = lm
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
        }
        if (gps_enabled == false || network_enabled == false) {
            result = false;
        } else {
            result = true;
        }

        return result;
    }
    public void showSettingsAlert() {
        Dialog dialog = createAlertDialog(this,getResources().getString(R.string.error),getResources().getString(R.string.turn_on_gps),getResources().getString(R.string.back),getResources().getString(R.string.ok));
        dialog.setCancelable(false);
        // Setting Dialog Title
        dialog.findViewById(R.id.dialog_continue).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(
                        Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(intent,GPS_REQUEST_CODE);
                dialog.dismiss();
            }
        });
        dialog.findViewById(R.id.dialog_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
    private void requestLocationPermissions() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                LOCATION_PERMISSION_CODE
        );
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.e("LOL", "WTH");
        if (requestCode == LOCATION_PERMISSION_CODE) {
            Toast.makeText(this, "WTH " + requestCode, Toast.LENGTH_SHORT).show();
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Granted. Start getting the location information
                if (canGetLocation()) {
//                    onIconClick(0);
                    Toast.makeText(this, getResources().getString(R.string.loc_perm_denied), Toast.LENGTH_SHORT).show();
                } else {
                    showSettingsAlert();
                }

            }
        } else {
            Toast.makeText(this, "CODE " + requestCode, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        //  stopService(mServiceIntent);
        super.onDestroy();
    }

    private void intialiseRetrofit() {
        apiInterface = APIClient.getApiClient().create(ApiInterface.class);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMarkerClickListener(this);
        mMap.setBuildingsEnabled(true);

        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        lastKnown = getLastKnownLocation();

        mMap.clear();
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        LatLng syd = new LatLng(lastKnown.getLatitude(), lastKnown.getLongitude());

        // Zoom in the Google Map
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(syd, 5));
        Log.d("readyyy", "yes");

        // getLats();
        Call<FetchNgomodel> getReportsModelCall = apiInterface.get_ngo(getAccessToken(this));
        getReportsModelCall.enqueue(new Callback<FetchNgomodel>() {

            @Override
            public void onResponse(Call<FetchNgomodel > call, Response<FetchNgomodel> response) {
                List<NgoModel> data=  response.body().getId();
                Log.d("ngomodel",getAccessToken(MapsActivity.this));

                Log.d("responsengo0",data.get(0).getName()+" ") ;
                Marker marker = null;
                mMap.clear();

                for(int i=0;i<data.size();i++)
                {
                    for(int j=0;j<data.get(i).activities.size();j++)
                    {
                        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
                        Date strDate = null;
                        try {
                            strDate = sdf.parse(data.get(i).activities.get(j).getDatetime().substring(0,data.get(i).activities.get(j).getDatetime().indexOf(' ')));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        try{

                            SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");


                            String str1 = data.get(i).activities.get(j).getDatetime().substring(0,data.get(i).activities.get(j).getDatetime().indexOf(' '));
                            Date date1 = formatter.parse(str1);

                            Date c = Calendar.getInstance().getTime();
                            System.out.println("Current time => " + c);




                            SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
                            String currentdate = df.format(c);
                            Date date2 = formatter.parse(currentdate);


                            if (date1.compareTo(date2)>0)
                            {
                                System.out.println("date2 is Greater than my date1");
                                float lat=data.get(i).activities.get(j).lat;
                                float lon=data.get(i).activities.get(j).lon;

                                LatLng sydney = new LatLng(lat, lon);

                                // LatLng sydney = new LatLng(location.getLatitude(), location.getLongitude());
                                //marker = new MarkerOptions().position(sydney).title(data.get(i).name);
                                marker = mMap.addMarker(new MarkerOptions().position(sydney).title(data.get(i).name));


                            }
                            else
                                if(date1.compareTo(date2)==0)
                                {
                                    Log.d("timeequal","hua");
                                    try {
                                        String string1 = data.get(i).activities.get(j).getDatetime().substring(data.get(i).activities.get(j).getDatetime().indexOf(' ')+1);
                                        Date time1 = new SimpleDateFormat("HH:mm").parse(string1);
                                        Calendar calendar1 = Calendar.getInstance();
                                        calendar1.setTime(time1);
                                        calendar1.add(Calendar.DATE, 1);



                                        String currentDateAndTime = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date());
                                        Log.d("current date",currentDateAndTime);

                                        String someRandomTime =currentDateAndTime.substring(currentDateAndTime.indexOf(' ')+1);
                                        Date d = new SimpleDateFormat("HH:mm").parse(someRandomTime);
                                        Calendar calendar3 = Calendar.getInstance();
                                        calendar3.setTime(d);
                                        calendar3.add(Calendar.DATE, 1);

                                        Date x = calendar3.getTime();
                                        if (x.before(calendar1.getTime()) ) {
                                            //checkes whether the current time is between 14:49:00 and 20:11:13.
                                            Log.d("timeequal","true");
                                            float lat=data.get(i).activities.get(j).lat;
                                            float lon=data.get(i).activities.get(j).lon;

                                            LatLng sydney = new LatLng(lat, lon);

                                            // LatLng sydney = new LatLng(location.getLatitude(), location.getLongitude());
                                            //marker = new MarkerOptions().position(sydney).title(data.get(i).name);
                                            marker = mMap.addMarker(new MarkerOptions().position(sydney).title(data.get(i).name));



                                        }
                                        else
                                        {

                                            Log.d("timeequal","false");
                                        }
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                        Log.d("timeequal", String.valueOf(e));
                                    }
                                }

                        }catch (ParseException e1){
                            e1.printStackTrace();
                        }
                        if (System.currentTimeMillis() > strDate.getTime()) {


                        }


                        // marker.showInfoWindow();




                    }
                }

//                dialog.dismiss();
            }

            @Override
            public void onFailure(Call<FetchNgomodel> call, Throwable t) {
                Log.d("failedhey", String.valueOf(t));

            }


        });


/*
        LatLng sydney = new LatLng(34.34, 86.7);

        final MarkerOptions marker = new MarkerOptions().position(sydney).title("");
        //  Drawable i=;

            Marker m= mMap.addMarker(marker);}

        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {





            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        if (

    MapsActivity(permission.ACCESS_FINE_LOCATION) != PERMISSION_GRANTED && checkSelfPermission(permission.ACCESS_COARSE_LOCATION) != PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    Activity#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for Activity#requestPermissions for more details.
            return;
        } else {

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            lastKnown = getLastKnownLocation();

            mMap.clear();
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            LatLng sydney = new LatLng(lastKnown.getLatitude(), lastKnown.getLongitude());
            MarkerOptions marker = new MarkerOptions().position(sydney).title("Your current location");
            mMap.addMarker(marker);

            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 16));

        }


        // Add a marker in Sydney and move the camera*/

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private Location getLastKnownLocation() {
        LocationManager mLocationManager;
        mLocationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
        List<String> providers = mLocationManager.getProviders(true);
        Location bestLocation = null;
        for (String provider : providers) {
            if (checkSelfPermission(permission.ACCESS_FINE_LOCATION) != PERMISSION_GRANTED && checkSelfPermission(permission.ACCESS_COARSE_LOCATION) != PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    Activity#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for Activity#requestPermissions for more details.
                ActivityCompat.requestPermissions(MapsActivity.this,new String[]{permission.ACCESS_FINE_LOCATION},0);



            }
            Location l = mLocationManager.getLastKnownLocation(provider);
            if (l == null) {
                continue;
            }
            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                // Found best last known location: %s", l);
                bestLocation = l;
            }
        }
        return bestLocation;
    }
    public Bitmap drawableToBitmap (Drawable drawable) {
        Bitmap bitmap = null;


        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if(bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if(drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;

    }
    public void getLats() {
        Log.d("ngomodel","in");



    }
    @Override
    public boolean onMarkerClick(Marker marker) {
        String ngo= marker.getTitle();
        Log.d("ngonamer",ngo);

        Intent intent=new Intent(MapsActivity.this,NgoActivity.class);
        intent.putExtra("name",ngo);
        intent.putExtra("lat",marker.getPosition().latitude);
        intent.putExtra("lon",marker.getPosition().longitude);
        startActivity(intent);


        return true;
    }
    private void getCurrentLocation() {

        mFusedLocationClient.getLastLocation().addOnCompleteListener(
                new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        Location location = task.getResult();
                        if (location == null) {
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    getCurrentLocation();
                                }
                            }, 1000);

                        } else {
                            currentLocation = new Location(location);
                            if(flag == 0){
                                flag = 1;
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 13.0f));
                            }
//                            Log.e("LOL", "OMG");
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    getCurrentLocation();
                                }
                            }, 1000);

//                            LOCATION_LAT = quarantineLocation.getLatitude();
//                            LOCATION_LON = quarantineLocation.getLongitude();

                        }
                    }
                }
        );
    }
}
