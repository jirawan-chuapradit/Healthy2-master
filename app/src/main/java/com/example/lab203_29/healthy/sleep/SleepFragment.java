package com.example.lab203_29.healthy.sleep;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.example.lab203_29.healthy.MenuFragment;
import com.example.lab203_29.healthy.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;

import static android.content.Context.MODE_PRIVATE;

public class SleepFragment extends Fragment implements View.OnClickListener {

    private Button backBtn,addBtn;
    SQLiteDatabase myDB;
    ListView _sleepList;
    ArrayList<Sleep> sleeps = new ArrayList<>();
    //Firebase
    private FirebaseAuth fbAuth;
    private String uid,mDayStr,mMonthStr;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sleep,container,false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        backBtn = getView().findViewById(R.id.back_btn);
        addBtn = getView().findViewById(R.id.add_sleep);
        _sleepList = (ListView) getView().findViewById(R.id.sleep_list);

        backBtn.setOnClickListener(this);
        addBtn.setOnClickListener(this);

        SleepAdapter _sleepAdapter = new SleepAdapter(
                getActivity(),
                R.layout.fragment_sleep_item,
                sleeps
        );
        fbAuth = FirebaseAuth.getInstance();
        uid = fbAuth.getCurrentUser().getUid();

        //open to use db
        myDB = getActivity().openOrCreateDatabase("my.db", Context.MODE_PRIVATE, null);

        //create table if not exist
        myDB.execSQL(
                "CREATE TABLE IF NOT EXISTS sleeps (_id INTEGER PRIMARY KEY AUTOINCREMENT, sleep VARCHAR(5), wake VARCHAR(5), process_date INTEGER)"
        );

        /********************************************************
         *  EXAMPLE CODE                                        *
         *  intent: delete value by causes                      *
         *  myDB.delete("sleeps", "_id=6", null);               *
         ********************************************************/

        //query data
        Cursor myCursor = myDB.rawQuery("SELECT _id, sleep, wake, process_date FROM sleeps ORDER BY process_date DESC", null);
        _sleepAdapter.clear();

        //set values to each parameter
        while(myCursor.moveToNext()){
            int _id = myCursor.getInt(0);
            String _timeSleep = myCursor.getString(1);
            String _timeWake = myCursor.getString(2);
            String _date = myCursor.getString(3);

            Log.d("SLEEP", "_id : "+myCursor.getInt(0)+" sleep : "+_timeSleep+" wake : "+_timeWake+" date : "+_date);

            //converse values (type int) milli Seconds to (type String)
            long timeStamp = Long.parseLong(_date);
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(timeStamp);
            int mYear = calendar.get(Calendar.YEAR);
            int mMonth = calendar.get(Calendar.MONTH); //calendars is JANUARY which is 0;
            int mDay = calendar.get(Calendar.DAY_OF_MONTH);
            setdisplay(mDay,mMonth);
            String realDate = mDayStr +"-" + mMonthStr +"-"+ mYear;

            //add parameter to Obj Sleep
            sleeps.add(new Sleep(_id,_timeSleep, _timeWake, realDate));
        }

        _sleepList.setAdapter(_sleepAdapter);
        _sleepList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Sleep sleep = new Sleep();
                sleep = (Sleep) parent.getItemAtPosition(position);
                Log.d("SLEEP ID: ", String.valueOf(sleep.getId()));
                Log.d("SLEEP Date: ", String.valueOf(sleep.getDate()));
                Log.d("SLEEP s_Time: ", sleep.getSleepTime());
                Log.d("SLEEP w_Time:  ", sleep.getWakeUpTime());
                Log.d("SLEEP", "GOTO SLEEP FORM");

                SharedPreferences.Editor prefs = getContext().getSharedPreferences("Healthy",MODE_PRIVATE).edit();
                prefs.putString(uid+"_s_date", sleep.getDate());
                prefs.putString(uid+"_s_time", sleep.getSleepTime());
                prefs.putString(uid+"_w_time", sleep.getWakeUpTime());
                prefs.putInt(uid+"_id",sleep.getId());
                prefs.apply();

                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.main_view, new SleepFormFragment())
                        .addToBackStack(null)
                        .commit();
            }
        });

        myCursor.close();

        /****************************************************************************
         * EXAMPLE CODE                                                             *
         *   Cursor myCursor =                                                      *
         *           myDB.rawQuery("select name, age, is_single from sleeps", null);*
         *while(myCursor.moveToNext()) {                                            *
         *      String name = myCursor.getString(0);                                *
         *     int age = myCursor.getInt(1);                                        *
         *    boolean isSingle = (myCursor.getInt(2)) == 1 ? true:false;            *
         *}                                                                         *
         *   myCursor.close;                                                        *
         ****************************************************************************/


    }

    private void setdisplay(int mDay, int mMonth) {
        if(mDay%10 == mDay){
            mDayStr = "0"+mDay;

        }else {
            mDayStr = String.valueOf(mDay);
        }

        if(mMonth == 0){
            mMonthStr = "Jan";
        }
        else if(mMonth == 1){
            mMonthStr = "Feb";
        }
        else if(mMonth == 2){
            mMonthStr = "Mar";
        }
        else if(mMonth == 3){
            mMonthStr = "Api";
        }
        else if(mMonth == 4){
            mMonthStr = "May";
        }
        else if(mMonth == 5){
            mMonthStr = "Jun";
        }
        else if(mMonth == 6){
            mMonthStr = "Jul";
        }
        else if(mMonth == 7){
            mMonthStr = "Aug";
        }
        else if(mMonth == 8){
            mMonthStr = "Sep";
        }
        else if(mMonth == 9){
            mMonthStr = "Oct";
        }
        else if(mMonth == 10){
            mMonthStr = "Nov";
        }
        else if(mMonth == 11){
            mMonthStr = "Dec";
        }

    }


    @Override
    public void onClick(View v) {
        if(v == backBtn){
            Log.d("SLEEP", "CLICK BACK");
            back();
        }
        else if(v == addBtn){
            Log.d("SLEEP", "CLICK ADD SLEEP");
            addSleep();
        }else if(v == _sleepList){

        }
    }

    private void addSleep() {
        Log.d("SLEEP", "GOTO SLEEP FORM");
        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main_view, new SleepFormFragment())
                .addToBackStack(null)
                .commit();
    }

    private void back() {
        Log.d("SLEEP", "GOTO MENU");
        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main_view, new MenuFragment())
                .addToBackStack(null)
                .commit();
    }
}
