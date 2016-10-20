package tie.hackathon.travelguide;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;


public class TravelFragment extends Fragment implements View.OnClickListener {

    Activity activity;
    LinearLayout shop, realtime, mytrips;

    public TravelFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View v = inflater.inflate(R.layout.content_travel, container, false);

        mytrips = (LinearLayout) v.findViewById(R.id.mytrips);
        realtime = (LinearLayout) v.findViewById(R.id.realtime);

        realtime.setOnClickListener(this);
        mytrips.setOnClickListener(this);

        return v;
    }


    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);
        this.activity = (Activity) activity;
    }


    @Override
    public void onClick(View view) {
        Intent intent;

        switch (view.getId()) {

            case R.id.realtime:
                intent = new Intent(activity, MapRealTimeActivity.class);
                startActivity(intent);
                break;

            case R.id.mytrips:
                intent = new Intent(activity, MyTrips.class);
                startActivity(intent);
                break;
        }

    }
}
