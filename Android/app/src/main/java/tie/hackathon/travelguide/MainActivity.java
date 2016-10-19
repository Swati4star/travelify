package tie.hackathon.travelguide;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.MenuItem;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import Util.Constants;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private BeaconManager beaconManager;
    private Region region;
    SharedPreferences sharedPreferences;
    Boolean discovered = false;
    String beaconmajor;
    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        //Initially city fragment
        Fragment fragment;
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragment = new City_fragment();
        fragmentManager.beginTransaction().replace(R.id.inc, fragment).commit();

        mHandler = new Handler(Looper.getMainLooper());

        // If beacon detected, open activity
        final Intent intent = getIntent();
        if (intent.getBooleanExtra(Constants.IS_BEACON, false)) {
            Intent intent1 = new Intent(MainActivity.this, DetectedBeacon.class);
            intent1.putExtra(Constants.CUR_UID, intent.getStringExtra(Constants.CUR_UID));
            intent1.putExtra(Constants.CUR_MAJOR, intent.getStringExtra(Constants.CUR_MAJOR));
            intent1.putExtra(Constants.CUR_MINOR, intent.getStringExtra(Constants.CUR_MINOR));
            intent1.putExtra(Constants.IS_BEACON, true);
            startActivity(intent1);
        }


        // Start beacon ranging
        beaconManager = new BeaconManager(this);
        region = new Region("Minion region", UUID.fromString(Constants.UID), null, null);

        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                beaconManager.startRanging(region);
            }
        });

        beaconManager.setRangingListener(new BeaconManager.RangingListener() {
            @Override
            public void onBeaconsDiscovered(Region region, List<Beacon> list) {
                if (!discovered && list.size() > 0) {
                    Beacon nearestBeacon = list.get(0);
                    beaconmajor = Integer.toString(nearestBeacon.getMajor());
                    Log.e("Discovered", "Nearest places: " + nearestBeacon.getMajor());
                    discovered = true;
                    Intent intent1 = new Intent(MainActivity.this, DetectedBeacon.class);
                    intent1.putExtra(Constants.CUR_UID, " ");
                    intent1.putExtra(Constants.CUR_MAJOR, beaconmajor);
                    intent1.putExtra(Constants.CUR_MINOR, " ");
                    intent1.putExtra(Constants.IS_BEACON, true);
                    startActivity(intent1);
                }
            }


        });

        // Get user's login id
        String isid = sharedPreferences.getString(Constants.UID, null);
        if (isid == null)
            getloginid();


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        Fragment fragment;
        FragmentManager fragmentManager = getSupportFragmentManager();

        if (id == R.id.nav_travel) {

            fragment = new Travel_fragment();
            fragmentManager.beginTransaction().replace(R.id.inc, fragment).commit();

        } else if (id == R.id.nav_city) {

            fragment = new City_fragment();
            fragmentManager.beginTransaction().replace(R.id.inc, fragment).commit();

        } else if (id == R.id.nav_utility) {

            fragment = new utilities_fragment();
            fragmentManager.beginTransaction().replace(R.id.inc, fragment).commit();
        } else if (id == R.id.nav_changecity) {
            Intent i = new Intent(MainActivity.this, SelectCity.class);
            startActivity(i);

        } else if (id == R.id.nav_emergency) {
            fragment = new Emergency_fragment();
            fragmentManager.beginTransaction().replace(R.id.inc, fragment).commit();

        } else if (id == R.id.nav_signout) {

            sharedPreferences
                    .edit()
                    .putString(Constants.USER_ID, null)
                    .apply();

            Intent i = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(i);
            finish();

        }


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    public void getloginid() {

        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        String id = telephonyManager.getDeviceId();
        String uri = Constants.apilink + "login.php?device_id=" + id;
        Log.e("executing", uri + " ");


        //Set up client
        OkHttpClient client = new OkHttpClient();
        //Execute request
        final Request request = new Request.Builder()
                .url(uri)
                .build();
        //Setup callback
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("Request Failed", "Message : " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {

                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JSONObject json = new JSONObject(response.body().string());
                            String uid = json.getString("user_id");
                            sharedPreferences
                                    .edit()
                                    .putString(Constants.UID, uid)
                                    .apply();
                            Log.e("here", "commitin" + uid);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.e("erro", e.getMessage() + " ");
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                });

            }
        });
    }
}
