package com.suvidha.Activities;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.suvidha.Fragments.HistoryFragment;
import com.suvidha.Fragments.HomeFragment;
import com.suvidha.Models.EssentialsRequestModel;
import com.suvidha.Models.GetOrdersModel;
import com.suvidha.R;
import com.suvidha.Utilities.APIClient;
import com.suvidha.Utilities.ApiInterface;
import com.suvidha.Utilities.SharedPrefManager;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.suvidha.Utilities.Utils.APP_CHARGE;
import static com.suvidha.Utilities.Utils.DELIVERY_CHARGE;
import static com.suvidha.Utilities.Utils.allOrders;
import static com.suvidha.Utilities.Utils.createProgressDialog;
import static com.suvidha.Utilities.Utils.getAccessToken;
import static com.suvidha.Utilities.Utils.local_zone_name;
import static com.suvidha.Utilities.Utils.zonesList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int PERMISSION_ID = 001;
    private static final String TAG = "MainActivity";

    private LinearLayout locationLayout;
    private TextView nodeName;
    private BottomNavigationView navigationView;
    FusedLocationProviderClient mFusedLocationClient;
    private ApiInterface apiInterface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        intialiseRetrofit();
        setListeners();
        getEssentials();
        setBottomNavigation();
    }

    private void getAllOrders() {
        Call<GetOrdersModel> listCall = apiInterface.getAllOrders(getAccessToken(this));
        listCall.enqueue(new Callback<GetOrdersModel>() {
            @Override
            public void onResponse(Call<GetOrdersModel> call, Response<GetOrdersModel> response) {
                if (response.body().status == 200) {

                    allOrders.clear();
                    allOrders.addAll(response.body().id);
                    if (getSupportFragmentManager().findFragmentById(R.id.frame_container) instanceof HistoryFragment) {
                        NotifyFragment callBack = (NotifyFragment) getSupportFragmentManager().findFragmentById(R.id.frame_container);
                        callBack.notifyDataLoaded();
                    }
//                    Collections.sort(allOrders,Collections.reverseOrder());
                } else {
//                    Toast.makeText(MainActivity.this, "Failed to get your history", Toast.LENGTH_SHORT).show();
                    Log.e("LOL", "Failed to get your history");
                }
            }

            @Override
            public void onFailure(Call<GetOrdersModel> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Failed to connect to the server", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public Dialog dialog;
    public ProgressBar progressBar;
    private void getEssentials() {
        if (dialog == null) {
            dialog = createProgressDialog(this, "Please wait");
        }
        progressBar = dialog.findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.VISIBLE);
        ImageView staticProgress = dialog.findViewById(R.id.static_progress);
        staticProgress.setVisibility(View.GONE);
        dialog.show();
        Call<EssentialsRequestModel> essentialsRequestModelCall = apiInterface.getEssentials(getAccessToken(this));
//        Log.e(TAG,"Response Error "+"LOL");
        essentialsRequestModelCall.enqueue(new Callback<EssentialsRequestModel>() {
            @Override
            public void onResponse(Call<EssentialsRequestModel> call, Response<EssentialsRequestModel> response) {

                try {
                    if (response.body().status == 200) {

                        dialog.dismiss();
                        onStart();
                        zonesList.clear();
                        zonesList.addAll(response.body().id.zones);
                        //response.body().id.shop_types;
                        APP_CHARGE = response.body().id.cess_rate;
                        DELIVERY_CHARGE = response.body().id.delivery_cost;

                        local_zone_name = SharedPrefManager.getInstance(MainActivity.this).getInt(SharedPrefManager.Key.ZONE_KEY);
                        nodeName.setText(zonesList.get(local_zone_name).name);
                    } else {
                        TextView msg = dialog.findViewById(R.id.progress_msg);
                        msg.setText("Try Again");
                    }
                } catch (Exception e) {
                    Log.e(TAG, String.valueOf(e.getStackTrace()));
                    TextView msg = dialog.findViewById(R.id.progress_msg);
                    msg.setText("Try Again");
                }

            }

            @Override
            public void onFailure(Call<EssentialsRequestModel> call, Throwable t) {
                Log.e(TAG, "Response Error " + t.getMessage());
                TextView msg = dialog.findViewById(R.id.progress_msg);
                msg.setText("Try Again");
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        progressBar = dialog.findViewById(R.id.progress_bar);
                        progressBar.setVisibility(View.INVISIBLE);
                        ImageView staticProgress = dialog.findViewById(R.id.static_progress);
                        staticProgress.setVisibility(View.VISIBLE);
                        staticProgress.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                getEssentials();
                            }
                        });
                    }
                },500);

                Toast.makeText(MainActivity.this, "Failed to connect to the server", Toast.LENGTH_SHORT).show();
            }
        });
    }

        private void intialiseRetrofit() {
            apiInterface = APIClient.getApiClient().create(ApiInterface.class);
        }

    private void setBottomNavigation() {
        loadFragment(new HomeFragment());
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Fragment fragment;
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    fragment = new HomeFragment();
                    loadFragment(fragment);
                    return true;
                case R.id.navigation_history:
                    fragment = new HistoryFragment();
                    loadFragment(fragment);
                    return true;
            }
            return false;
        }
    };

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void setListeners() {
        locationLayout.setOnClickListener(this);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
    }

    private void init() {
        nodeName = findViewById(R.id.node_name);
        locationLayout = findViewById(R.id.node_location_layout);

    }
    //suvidhajamshedhpur@gmail.com

    private void startDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_zone);
        setRecyclerViewInDialog(dialog);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
//lp.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 330/*height value*/, getResources().getDisplayMetrics()); for custom height value
        dialog.getWindow().setAttributes(lp);
        dialog.show();
    }

    void setRecyclerViewInDialog(Dialog dialog) {
        ListView lview = dialog.findViewById(R.id.dialog_zone_lview);
        List<String> items = new ArrayList<>();
        for (int i = 1; i < zonesList.size(); i++) {
            items.add(zonesList.get(i).name);
        }
        ArrayAdapter<String> itemsAdapter =
                new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items);
        lview.setAdapter(itemsAdapter);
        lview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView tv = (TextView) view;
                nodeName.setText(tv.getText().toString());
                local_zone_name = position + 1;
                dialog.dismiss();
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.node_location_layout:
                startDialog();
                break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        getAllOrders();
    }

    public interface NotifyFragment {
        void notifyDataLoaded();
    }
}

//    private boolean checkPermissions() {
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
//                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
//            return true;
//        }
//        return false;
//    }

//    private void requestPermissions() {
//        ActivityCompat.requestPermissions(
//                this,
//                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
//                PERMISSION_ID
//        );
//    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (requestCode == PERMISSION_ID) {
//            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                // Granted. Start getting the location information
//            }
//        }
//    }
//
//    private boolean isLocationEnabled() {
//        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
//        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
//                LocationManager.NETWORK_PROVIDER
//        );
//    }

//    private void getNearestNode() {
//
//        // Execute some code after 1 second has passed
//        nodeProgress.setVisibility(View.VISIBLE);
//        nodeName.setText("");
//        Handler handler = new Handler();
//        handler.postDelayed(new Runnable() {
//            public void run() {
//                if (isNetworkConnected() && checkPermissions())
//                    new LocationStuff().execute();
//                else {
//                    handler.postDelayed(this, 1000);
//                }
//            }
//        }, 1000);
//
//    }

//    private boolean isNetworkConnected() {
//        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
//
//        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
//    }

//    private class LocationStuff extends AsyncTask<Void, Void, Void> {
//
//        private Location currentLocation;
//
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//            nodeProgress.setVisibility(View.VISIBLE);
//            nodeName.setText("");
//            getCurrentLocation();
//        }
//
//        @Override
//        protected Void doInBackground(Void... voids) {
//
//            //get current location
//            while (currentLocation == null) {
////                Log.e("LOL", "lol");
//            }
//
//            nodesLocation = getNodesLocation();
//            //get nodes location
//            //punch cur and nod loc in list
//            return null;
//        }
//
//        @Override
//        protected void onPostExecute(Void aVoid) {
//            super.onPostExecute(aVoid);
//            Log.e("LOC", String.valueOf(currentLocation));
//            Double mDist = Double.MAX_VALUE;
//            NodeModel minDistanceNode = null;
//            for (NodeModel node : nodesLocation) {
//                node.distance = distance(currentLocation.getLatitude(), currentLocation.getLongitude(),
//                        node.location.getLatitude(), node.location.getLongitude(), "K");
//                if (mDist > node.distance) {
//                    mDist = node.distance;
//                    minDistanceNode = node;
//                }
//            }
//            nodeProgress.setVisibility(View.GONE);
//            nodeName.setText(minDistanceNode.name);
//            Log.e("TAG", minDistanceNode.name);
//
//        }
//
//        private double distance(double lat1, double lon1, double lat2, double lon2, String unit) {
//            if ((lat1 == lat2) && (lon1 == lon2)) {
//                return 0;
//            } else {
//                double theta = lon1 - lon2;
//                double dist = Math.sin(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2)) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.cos(Math.toRadians(theta));
//                dist = Math.acos(dist);
//                dist = Math.toDegrees(dist);
//                dist = dist * 60 * 1.1515;
//                if (unit.equals("K")) {
//                    dist = dist * 1.609344;
//                } else if (unit.equals("N")) {
//                    dist = dist * 0.8684;
//                }
//                return (dist);
//            }
//        }
//
//        public List<NodeModel> getNodesLocation() {
//            List<NodeModel> list = new ArrayList<>();
//            list.add(new NodeModel("Bistupur", initLoc(23.981071, 85.363343)));
//            list.add(new NodeModel("Adityapur", initLoc(23.981267, 85.361895)));
//            return list;
//        }
//
//        Location initLoc(Double lat, Double lng) {
//            Location location = new Location("Provider");
//            location.setLatitude(lat);
//            location.setLongitude(lng);
//            return location;
//        }
//
//        private void getCurrentLocation() {
//            if (checkPermissions()) {
//                if (isLocationEnabled()) {
//                    mFusedLocationClient.getLastLocation().addOnCompleteListener(
//                            new OnCompleteListener<Location>() {
//                                @Override
//                                public void onComplete(@NonNull Task<Location> task) {
//                                    Location location = task.getResult();
//                                    if (location == null) {
//                                        //select the node
//                                    } else {
//                                        currentLocation = location;
//
//                                    }
//                                }
//                            }
//                    );
//                } else {
//                    Toast.makeText(getApplicationContext(), "Turn on location", Toast.LENGTH_LONG).show();
//                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
//                    startActivity(intent);
//                }
//            }
//        }
//    }
