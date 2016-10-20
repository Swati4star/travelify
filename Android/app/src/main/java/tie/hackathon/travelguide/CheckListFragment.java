package tie.hackathon.travelguide;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Paint;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.rey.material.widget.CheckBox;

import java.util.ArrayList;
import java.util.List;

import Util.Constants;
import database.DBHelp;
import database.TableEntry;

/**
 * A checklist fragment that displays travel checklist
 */
public class CheckListFragment extends Fragment {

    CheckListAdapter ad;
    Activity activity;

    List<String> id = new ArrayList<>();
    List<String> task = new ArrayList<>();
    List<String> isdone = new ArrayList<>();
    List<String> base_task = new ArrayList<>();

    DBHelp dbhelp;
    SQLiteDatabase db;

    SharedPreferences sharedPreferences;
    ListView lv;

    public CheckListFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.content_check_list, container, false);

        dbhelp = new DBHelp(getContext());
        db = dbhelp.getWritableDatabase();

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);

        //First time users
        if (sharedPreferences.getString(Constants.ID_ADDED_INDB, "null").equals("null")) {
            base_task.add("Bags");
            base_task.add("Keys");
            base_task.add("Charger");
            base_task.add("Earphones");
            base_task.add("Clothes");
            base_task.add("Food");
            base_task.add("Tickets");
            for (int i = 0; i < base_task.size(); i++) {
                ContentValues insertValues = new ContentValues();
                insertValues.put(TableEntry.COLUMN_NAME, base_task.get(i));
                insertValues.put(TableEntry.COLUMN_NAME_ISDONE, "0");
                db.insert(TableEntry.TABLE_NAME, null, insertValues);
            }
            sharedPreferences.edit().putString(Constants.ID_ADDED_INDB, "yes").apply();
        }

        // Set checklist adapter
        lv = (ListView) v.findViewById(R.id.lv);
        ad = new CheckListAdapter(activity, id, task, isdone);
        lv.setAdapter(ad);

        // Populate checklist
        refresh();

        LinearLayout l = (LinearLayout) v.findViewById(R.id.add);
        l.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                LayoutInflater inflater = (activity).getLayoutInflater();
                builder.setTitle("Add new item");
                builder.setCancelable(false);
                final View dialogv = inflater.inflate(R.layout.dialog, null);
                builder.setView(dialogv)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                TextInputEditText e = (TextInputEditText) dialogv.findViewById(R.id.task);
                                Log.e("ADDED NEW ITEM : ", e.getText() + "a");
                                if (!e.getText().toString().equals("")) {
                                    ContentValues insertValues = new ContentValues();
                                    insertValues.put(TableEntry.COLUMN_NAME, e.getText().toString());
                                    insertValues.put(TableEntry.COLUMN_NAME_ISDONE, "0");
                                    db.insert(TableEntry.TABLE_NAME, null, insertValues);
                                    refresh();
                                }
                            }
                        });
                builder.create();
                builder.show();
            }
        });
        return v;
    }

    /* Populates checklist */
    public void refresh() {
        id.clear();
        task.clear();
        isdone.clear();
        ad.notifyDataSetChanged();

        // Fill cursor
        Cursor c = db.rawQuery("SELECT * FROM " + TableEntry.TABLE_NAME + " ORDER BY " +
                TableEntry.COLUMN_NAME_ISDONE, null);
        if (c.moveToFirst()) {

            do {
                id.add(c.getString(c.getColumnIndex(TableEntry.COLUMN_NAME_ID)));
                task.add(c.getString(c.getColumnIndex(TableEntry.COLUMN_NAME)));
                isdone.add(c.getString(c.getColumnIndex(TableEntry.COLUMN_NAME_ISDONE)));
                Log.e("adding", "vfd" + c.getString(c.getColumnIndex(TableEntry.COLUMN_NAME))
                        + c.getString(c.getColumnIndex(TableEntry.COLUMN_NAME_ISDONE)));
            } while (c.moveToNext());
        }
        c.close();
        ad.notifyDataSetChanged();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (Activity) context;
    }

    /**
     * Adapter for checklist
     */
    public class CheckListAdapter extends ArrayAdapter<String> {

        private final Activity context;
        private final List<String> task, id, isdone;

        DBHelp dbhelp;
        SQLiteDatabase db;

        /**
         * Initiates Checklist Adapter
         *
         * @param context The context referring this class
         * @param id      unigue id
         * @param task    task name for checklist
         * @param done    Boolean : true if item is checked
         */
        CheckListAdapter(Activity context, List<String> id, List<String> task, List<String> done) {
            super(context, R.layout.checklist_item, task);
            this.context = context;
            this.task = task;
            this.id = id;
            this.isdone = done;
        }


        class ViewHolder {
            CheckBox c;
        }


        @Override
        public View getView(final int position, View view, ViewGroup parent) {
            LayoutInflater inflater = context.getLayoutInflater();

            View vi = view;             //trying to reuse a recycled view
            ViewHolder holder;
            if (vi == null) {
                vi = inflater.inflate(R.layout.checklist_item, parent, false);
                holder = new ViewHolder();
                holder.c = (CheckBox) vi.findViewById(R.id.cb1);
                vi.setTag(holder);
            } else {
                holder = (ViewHolder) vi.getTag();
            }

            dbhelp = new DBHelp(context);
            db = dbhelp.getWritableDatabase();

            // Check if item is checked
            if (isdone.get(position).equals("1")) {
                holder.c.setPaintFlags(holder.c.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                holder.c.setChecked(true);
            } else {
                holder.c.setPaintFlags(holder.c.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                holder.c.setChecked(false);
            }
            // set task name
            holder.c.setText(task.get(position));

            holder.c.setOnClickListener(new CheckBox.OnClickListener() {

                @Override
                public void onClick(View view) {
                    CheckBox c2 = (CheckBox) view;
                    if (c2.isChecked()) {
                        String x = "UPDATE " + TableEntry.TABLE_NAME + " SET " + TableEntry.COLUMN_NAME_ISDONE + " = 1 WHERE " +
                                TableEntry.COLUMN_NAME_ID + " IS " + id.get(position);
                        db.execSQL(x);
                        Log.e("UPDATED : ", x + " ");
                    } else {
                        String x = "UPDATE " + TableEntry.TABLE_NAME + " SET " + TableEntry.COLUMN_NAME_ISDONE + " = 0 WHERE " +
                                TableEntry.COLUMN_NAME_ID + " IS " + id.get(position);
                        db.execSQL(x);
                        Log.e("UPDATED : ", x + " ");
                    }
                    refresh();
                }

            });

            return vi;
        }
    }
}
