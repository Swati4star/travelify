package tie.hackathon.travelguide;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.ToxicBakery.viewpager.transforms.AccordionTransformer;
import com.afollestad.materialdialogs.MaterialDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import Util.Constants;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class FunFacts extends AppCompatActivity {

    String id, name;
    ViewPager vp;
    MaterialDialog dialog;
    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fun_facts);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent i = getIntent();
        id = i.getStringExtra("id_");
        name = i.getStringExtra("name_");
        mHandler = new Handler(Looper.getMainLooper());

        vp = (ViewPager) findViewById(R.id.vp);

        getCityFacts();

        getSupportActionBar().hide();


    }

    public void getCityFacts() {

        dialog = new MaterialDialog.Builder(FunFacts.this)
                .title(R.string.app_name)
                .content("Please wait...")
                .progress(true, 0)
                .show();

        // to fetch city names
        String uri = Constants.apilink + "city_facts.php?id=" + id;
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
                        List<Fragment> fList = new ArrayList<Fragment>();

                        try {
                            JSONObject ob = new JSONObject(response.body().string());
                            JSONArray ar = ob.getJSONArray("facts");

                            for (int i = 0; i < ar.length(); i++)
                                fList.add(Funfact_fragment.newInstance(ar.getJSONObject(i).getString("image"),
                                        ar.getJSONObject(i).getString("fact"), name));
                            vp.setAdapter(new MyPageAdapter(getSupportFragmentManager(), fList));
                            vp.setPageTransformer(true, new AccordionTransformer());

                        } catch (JSONException e1) {
                            e1.printStackTrace();
                            Log.e("heer", e1.getMessage() + " ");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        dialog.dismiss();
                    }
                });

            }
        });
    }


    class MyPageAdapter extends FragmentPagerAdapter {
        private List<Fragment> fragments;

        public MyPageAdapter(FragmentManager fm, List<Fragment> fragments) {
            super(fm);
            this.fragments = fragments;
        }

        @Override
        public Fragment getItem(int position) {
            return this.fragments.get(position);
        }

        @Override
        public int getCount() {
            return this.fragments.size();
        }
    }


}
