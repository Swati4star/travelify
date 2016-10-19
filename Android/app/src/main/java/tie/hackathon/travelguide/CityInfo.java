package tie.hackathon.travelguide;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.lucasr.twowayview.TwoWayView;

import java.io.IOException;

import Util.Constants;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class CityInfo extends AppCompatActivity {

    SharedPreferences sharedPreferences;

    ProgressDialog progressDialog;
    TwoWayView lvRest, lvShop, lvTour, lvhang;
    ProgressBar pb1, pb2, pb3, pb4;
    TextView city_info, min, max, pre;
    Intent intent;
    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city_info);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        intent = getIntent();


        mHandler = new Handler(Looper.getMainLooper());
        lvRest = (TwoWayView) findViewById(R.id.lvRestaurants);
        lvTour = (TwoWayView) findViewById(R.id.lvTourists);
        lvShop = (TwoWayView) findViewById(R.id.lvShopping);
        lvhang = (TwoWayView) findViewById(R.id.lvhangout);
        pre = (TextView) findViewById(R.id.pre);
        pb1 = (ProgressBar) findViewById(R.id.pb1);
        pb2 = (ProgressBar) findViewById(R.id.pb2);
        pb3 = (ProgressBar) findViewById(R.id.pb3);
        pb4 = (ProgressBar) findViewById(R.id.pb4);
        min = (TextView) findViewById(R.id.min);
        max = (TextView) findViewById(R.id.max);
        city_info = (TextView) findViewById(R.id.city_info);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        getCityInfo();

        String tit = intent.getStringExtra("name_");
        if (tit == null)
            tit = sharedPreferences.getString(Constants.DESTINATION_CITY, "Delhi");
        setTitle(tit);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home)
            finish();

        return super.onOptionsItemSelected(item);
    }


    public void getCityInfo() {


        progressDialog = new ProgressDialog(CityInfo.this);
        progressDialog.setMessage("Fetching data, Please wait...");
        progressDialog.setIndeterminate(true);
        progressDialog.show();
        Log.e("vdslmvdspo", "started");

        // to fetch city names
        String id = intent.getStringExtra("id_");
        Log.e("cbvsbk", id + " ");
        if (id == null) {
            id = sharedPreferences.getString(Constants.DESTINATION_CITY_ID, "1");
        }
        String uri = Constants.apilink +
                "get-city-info.php?id=" + id;
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
                            JSONObject YTFeed = new JSONObject(String.valueOf(response.body().string()));

                            city_info.setText(YTFeed.getString("description"));


                            min.setText("MIN : " + YTFeed.getJSONObject("weather").getString("min") + " C");
                            max.setText("MAX : " + YTFeed.getJSONObject("weather").getString("max") + " C");

                            if (Double.parseDouble(YTFeed.getJSONObject("weather").getString("min")) < 20) {

                                pre.setText("Do not forget to take extra sweaters.");

                            } else {
                                if (Double.parseDouble(YTFeed.getJSONObject("weather").getString("max")) > 35) {

                                    pre.setText("It seems pretty hot there.");

                                } else {
                                    pre.setText("Enjoy weather.");

                                }
                            }


                            JSONArray YTFeedItems = YTFeed.getJSONArray("food");
                            Log.e("response", YTFeedItems + " ");
                            pb1.setVisibility(View.GONE);
                            lvRest.setAdapter(new City_info_adapter(CityInfo.this, YTFeedItems, R.drawable.restaurant));

                            YTFeedItems = YTFeed.getJSONArray("monuments");
                            Log.e("response", YTFeedItems + " ");
                            pb2.setVisibility(View.GONE);
                            lvTour.setAdapter(new City_info_adapter(CityInfo.this, YTFeedItems, R.drawable.monuments));


                            YTFeedItems = YTFeed.getJSONArray("hangout-places");
                            Log.e("response", YTFeedItems + " ");
                            pb3.setVisibility(View.GONE);
                            lvhang.setAdapter(new City_info_adapter(CityInfo.this, YTFeedItems, R.drawable.hangout));

                            YTFeedItems = YTFeed.getJSONArray("shopping");
                            Log.e("response", YTFeedItems + " ");
                            pb4.setVisibility(View.GONE);
                            lvShop.setAdapter(new City_info_adapter(CityInfo.this, YTFeedItems, R.drawable.shopping));
                            progressDialog.hide();
                        } catch (Exception e) {
                            e.printStackTrace();
                            Log.e("vsdfkvaes", e.getMessage() + " dsv");
                        }
                    }

                });
            }
        });
    }


    public class City_info_adapter extends BaseAdapter {

        Context context;
        JSONArray FeedItems;
        int rd;
        LinearLayout b2;
        private LayoutInflater inflater = null;

        public City_info_adapter(Context context, JSONArray FeedItems, int r) {
            this.context = context;
            this.FeedItems = FeedItems;
            rd = r;

            inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return FeedItems.length();
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            try {
                return FeedItems.getJSONObject(position);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }


        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View vi = convertView;
            if (vi == null)
                vi = inflater.inflate(R.layout.city_infoitem, null);

            TextView Title = (TextView) vi.findViewById(R.id.item_name);
            TextView Description = (TextView) vi.findViewById(R.id.item_address);
            LinearLayout onmap = (LinearLayout) vi.findViewById(R.id.map);
            b2 = (LinearLayout) vi.findViewById(R.id.b2);


            try {
                Title.setText(FeedItems.getJSONObject(position).getString("name"));
                Description.setText(FeedItems.getJSONObject(position).getString("address"));
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e("eroro", e.getMessage() + " ");
            }

            ImageView iv = (ImageView) vi.findViewById(R.id.image);
            iv.setImageResource(rd);


            onmap.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {


                    Intent browserIntent;
                    try {
                        browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps?q=" +
                                FeedItems.getJSONObject(position).getString("name") +
                                "+(name)+@" +
                                FeedItems.getJSONObject(position).getString("lat") +
                                "," +
                                FeedItems.getJSONObject(position).getString("lng")
                        ));
                        context.startActivity(browserIntent);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                }
            });


            b2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {


                    Intent browserIntent;
                    browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.co.in/"

                    ));
                    context.startActivity(browserIntent);


                }
            });
            return vi;
        }

    }


}
