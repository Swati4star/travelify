package tie.hackathon.travelguide;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.ms.square.android.expandabletextview.ExpandableTextView;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import Util.Constants;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class FinalCityInfo extends AppCompatActivity implements View.OnClickListener {

    Intent i;
    String id, tit, image,description,lat,lon;
    ImageView iv,ico;
    TextView title;
    ExpandableTextView des;
    Typeface code,tex,codeb;
    private Handler mHandler;

    TextView fftext,temp,humidity,weatherinfo;
    MaterialDialog dialog;
    LinearLayout funfact, restau, hangout, monum, shopp, trend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_final_city_info);

        code = Typeface.createFromAsset(getAssets(), "fonts/whitney_book.ttf");
        codeb = Typeface.createFromAsset(getAssets(), "fonts/CODE_Bold.otf");
        tex = Typeface.createFromAsset(getAssets(), "fonts/texgyreadventor-regular.otf");
        mHandler = new Handler(Looper.getMainLooper());

        des = (ExpandableTextView) findViewById(R.id.expand_text_view);
        des.setText(getString(R.string.sample_string));

        iv = (ImageView) findViewById(R.id.image);
        title = (TextView) findViewById(R.id.head);
        ico = (ImageView) findViewById(R.id.icon);
        temp = (TextView) findViewById(R.id.temp);
        humidity = (TextView) findViewById(R.id.humidit);
        weatherinfo = (TextView) findViewById(R.id.weatherinfo);

        i = getIntent();
        tit = i.getStringExtra("name_");
        setTitle(tit);
        id = i.getStringExtra("id_");
        image = i.getStringExtra("image_");

        title.setTypeface(codeb);
        title.setText(tit);

        funfact = (LinearLayout) findViewById(R.id.funfact);
        restau = (LinearLayout) findViewById(R.id.restau);
        hangout = (LinearLayout) findViewById(R.id.hangout);
        monum = (LinearLayout) findViewById(R.id.monu);
        shopp = (LinearLayout) findViewById(R.id.shoppp);
        trend = (LinearLayout) findViewById(R.id.trends);


        fftext = (TextView) findViewById(R.id.fftext);
        fftext.setTypeface(code);
        fftext = (TextView) findViewById(R.id.hgtext);
        fftext.setTypeface(code);
        fftext = (TextView) findViewById(R.id.shtext);
        fftext.setTypeface(code);
        fftext = (TextView) findViewById(R.id.mntext);
        fftext.setTypeface(code);
        fftext = (TextView) findViewById(R.id.rstext);
        fftext.setTypeface(code);
        fftext = (TextView) findViewById(R.id.cttext);
        fftext.setTypeface(code);


        funfact.setOnClickListener(this);
        restau.setOnClickListener(this);
        hangout.setOnClickListener(this);
        monum.setOnClickListener(this);
        shopp.setOnClickListener(this);
        trend.setOnClickListener(this);

        cityInfo();

        Picasso.with(this).load(image).into(iv);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home)
            finish();
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onClick(View v) {
        Intent i;
        switch (v.getId()) {
            case R.id.funfact:
                i = new Intent(FinalCityInfo.this, FunFacts.class);
                i.putExtra("id_", id);
                i.putExtra("name_", tit);
                startActivity(i);
                break;


            case R.id.restau:
                i = new Intent(FinalCityInfo.this, PlacesOnMap.class);
                i.putExtra("id_", id);
                i.putExtra("lat_",lat);
                i.putExtra("lng_",lon);
                i.putExtra("name_", tit);
                i.putExtra("type_", "restaurant");
                startActivity(i);
                break;
            case R.id.hang:
                i = new Intent(FinalCityInfo.this, PlacesOnMap.class);
                i.putExtra("lat_",lat);
                i.putExtra("lng_",lon);
                i.putExtra("id_", id);
                i.putExtra("name_", tit);
                i.putExtra("type_", "hangout");
                startActivity(i);
                break;
            case R.id.monu:
                i = new Intent(FinalCityInfo.this, PlacesOnMap.class);
                i.putExtra("lat_",lat);
                i.putExtra("lng_",lon);
                i.putExtra("id_", id);
                i.putExtra("name_", tit);
                i.putExtra("type_", "monument");
                startActivity(i);
                break;
            case R.id.shoppp:

                i = new Intent(FinalCityInfo.this, PlacesOnMap.class);
                i.putExtra("id_", id);
                i.putExtra("name_", tit);
                i.putExtra("lat_",lat);
                i.putExtra("lng_",lon);
                i.putExtra("type_", "shopping");
                startActivity(i);
                break;


            case R.id.trends :
                i = new Intent(FinalCityInfo.this, Tweets.class);
                i.putExtra("id_", id);
                i.putExtra("name_", tit);
                startActivity(i);
                break;


        }

    }


    public void cityInfo() {

        dialog = new MaterialDialog.Builder(FinalCityInfo.this)
                .title(R.string.app_name)
                .content("Please wait...")
                .progress(true, 0)
                .show();

        // to fetch city names
        String uri = Constants.apilink + "city/info.php?id=" + id;
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
                        try {
                            //Tranform the string into a json object
                            JSONObject ob = new JSONObject(response.body().string());

                            description = ob.getString("description");
                            des.setText(description);

                            Picasso.with(FinalCityInfo.this).load(ob.getJSONObject("weather").getString("icon")).into(ico);
                            temp.setText(ob.getJSONObject("weather").getString("temprature")+  (char) 0x00B0 + " C ");
                            humidity.setText("Humidity : " + ob.getJSONObject("weather").getString("humidity"));
                            weatherinfo.setText(ob.getJSONObject("weather").getString("description"));
                            lat = ob.getString("lat");
                            lon = ob.getString("lng");

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.e("erro", e.getMessage() + " ");
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                        dialog.dismiss();
                    }
                });

            }
        });
    }
}
