package tie.hackathon.travelguide;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.dd.processbutton.FlatButton;
import com.fourmob.datetimepicker.date.DatePickerDialog;
import com.sleepbot.datetimepicker.time.RadialPickerLayout;
import com.sleepbot.datetimepicker.time.TimePickerDialog;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Objects;

import Util.Constants;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class AddNewTrip extends AppCompatActivity implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

    AutoCompleteTextView cityname;
    String nameyet, cityid = "2";
    FlatButton sdate, edate, ok;
    EditText tname;
    public static final String DATEPICKER_TAG1 = "datepicker1", DATEPICKER_TAG2 = "datepicker2";
    String startdate, enddate, tripname, userid;
    MaterialDialog dialog;
    private Handler mHandler;

    SharedPreferences s;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_trip);
        cityname = (AutoCompleteTextView) findViewById(R.id.cityname);
        sdate = (FlatButton) findViewById(R.id.sdate);
        edate = (FlatButton) findViewById(R.id.edate);
        ok = (FlatButton) findViewById(R.id.ok);
        tname = (EditText) findViewById(R.id.tripname);
        s = PreferenceManager.getDefaultSharedPreferences(this);
        userid = s.getString(Constants.USER_ID, "1");
        mHandler = new Handler(Looper.getMainLooper());

        cityname.setThreshold(1);
        cityname.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                nameyet = cityname.getText().toString();
                if (!nameyet.contains(" ")) {
                    Log.e("name", nameyet + " ");
                    tripAutoComplete();
                }

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });


        final Calendar calendar = Calendar.getInstance();
        final DatePickerDialog datePickerDialog = DatePickerDialog.newInstance(this, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), isVibrate());


        sdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                datePickerDialog.setVibrate(isVibrate());
                datePickerDialog.setYearRange(1985, 2028);
                datePickerDialog.setCloseOnSingleTapDay(isCloseOnSingleTapDay());
                datePickerDialog.show(getSupportFragmentManager(), DATEPICKER_TAG1);

            }
        });

        edate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePickerDialog.setVibrate(isVibrate());
                datePickerDialog.setYearRange(1985, 2028);
                datePickerDialog.setCloseOnSingleTapDay(isCloseOnSingleTapDay());
                datePickerDialog.show(getSupportFragmentManager(), DATEPICKER_TAG2);


            }
        });

        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                tripname = tname.getText().toString();

                addTrip();
            }
        });

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


    }

    @Override
    public void onDateSet(DatePickerDialog datePickerDialog, int year, int month, int day) {

        if (Objects.equals(datePickerDialog.getTag(), DATEPICKER_TAG1)) {

            Calendar calendar = new GregorianCalendar(year, month, day);
            startdate = Long.toString(calendar.getTimeInMillis() / 1000);
        }
        if (Objects.equals(datePickerDialog.getTag(), DATEPICKER_TAG2)) {

            Calendar calendar = new GregorianCalendar(year, month, day);
            enddate = Long.toString(calendar.getTimeInMillis() / 1000);
        }

    }

    @Override
    public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute) {}

    public void tripAutoComplete() {

        // to fetch city names
        String uri = Constants.apilink + "city/autocomplete.php?search=" + nameyet.trim();
        Log.e("executing", uri + " ");


        //Set up client
        OkHttpClient client = new OkHttpClient();
        //Execute request
        Request request = new Request.Builder()
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
                        Log.e("YO", "Done");
                        JSONArray arr;
                        final ArrayList list, list1;
                        try {
                            arr = new JSONArray(response.body().string());
                            Log.e("erro", arr + " ");

                            list = new ArrayList<>();
                            list1 = new ArrayList<>();
                            for (int i = 0; i < arr.length(); i++) {
                                try {
                                    list.add(arr.getJSONObject(i).getString("name"));
                                    list1.add(arr.getJSONObject(i).getString("id"));
                                    Log.e("adding", "aff");

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    Log.e("error ", " " + e.getMessage());
                                }
                            }
                            ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>
                                    (getApplicationContext(), R.layout.spinner_layout, list);
                            dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            cityname.setThreshold(1);
                            cityname.setAdapter(dataAdapter);
                            cityname.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                                @Override
                                public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                                    // TODO Auto-generated method stub
                                    cityid = list1.get(arg2).toString();
                                }
                            });
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

    public void addTrip() {

        dialog = new MaterialDialog.Builder(AddNewTrip.this)
                .title(R.string.app_name)
                .content("Please wait...")
                .progress(true, 0)
                .show();

        String uri = Constants.apilink + "trip/add-trip.php?user=" +
                userid +
                "&title=" +
                tripname +
                "&" +
                "start_time=" +
                startdate +
                "&city=" +
                cityid;

        Log.e("addinhg", uri + " ");

        //Set up client
        OkHttpClient client = new OkHttpClient();
        //Execute request
        Request request = new Request.Builder()
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
                        Log.e("YO", "Done");
                        Toast.makeText(AddNewTrip.this, "Trip added", Toast.LENGTH_LONG).show();
                        dialog.dismiss();
                    }
                });

            }
        });
    }

    private boolean isVibrate() {
        return false;
    }

    private boolean isCloseOnSingleTapDay() {
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {


        if (item.getItemId() == android.R.id.home)
            finish();

        return super.onOptionsItemSelected(item);
    }

}
