package com.askokov.module;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import com.askokov.module.fragment.Fragment1;
import com.askokov.module.fragment.Fragment2;
import com.askokov.module.fragment.Fragment3;


public class MainActivity extends FragmentActivity {
    private static final String TAG = "MainActivity";
    private static final String FRAGMENT_NUMBER = "FragmentNumber";
    private static final String FRAGMENT_COUNTER = "FragmentCounter";
    private static final int MAX_COUNTER = 10;
    private static final long REPEAT_TIME = 1000 * 1;
    private int fragmentNumber;
    private int fragmentCounter;
    private Fragment currentFragment;
    private TextView counter;
    private MenuItem menuStart;
    private MenuItem menuStop;
    private AlarmReceiver alarmReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate");
        setContentView(R.layout.main);

        counter = (TextView) findViewById(R.id.textCounter);
        counter.setText("");
        alarmReceiver = new AlarmReceiver();
        IntentFilter alarmFilter = new IntentFilter();
        alarmFilter.addAction(AlarmReceiver.ACTION);
        registerReceiver(alarmReceiver, alarmFilter);

        if (savedInstanceState != null) {
            Log.i(TAG, "onCreate: restore from bundle");
            fragmentNumber = savedInstanceState.getInt(FRAGMENT_NUMBER, 0);
            Log.i(TAG, "onCreate: fragmentNumber=" + fragmentNumber);
            if (fragmentNumber > 0) {
                fragmentCounter = savedInstanceState.getInt(FRAGMENT_COUNTER, MAX_COUNTER);
                Log.i(TAG, "onCreate: fragmentCounter=" + fragmentCounter);

                Fragment fragment = defineCurrentFragment(fragmentNumber);
                changeFragment(fragment);

                setCounterText();

                startAlarm(this);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy");

        stopAlarm(this);
        unregisterReceiver(alarmReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.i(TAG, "onCreateOptionsMenu");
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        Log.i(TAG, "onPrepareOptionsMenu");

        menuStart = menu.findItem(R.id.action_start);
        menuStop = menu.findItem(R.id.action_stop);

        if (fragmentNumber > 0) {
            menuStart.setEnabled(false);
            menuStop.setEnabled(true);
        } else {
            menuStart.setEnabled(true);
            menuStop.setEnabled(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.action_start:
                Log.i(TAG, "Action start");

                fragmentCounter = MAX_COUNTER;
                fragmentNumber = 1;
                Fragment fragment = defineCurrentFragment(fragmentNumber);
                changeFragment(fragment);

                setCounterText();
                startAlarm(this);

                menuStart.setEnabled(false);
                menuStop.setEnabled(true);

                return true;

            case R.id.action_stop:
                Log.i(TAG, "Action stop");

                stopAlarm(this);

                menuStart.setEnabled(true);
                menuStop.setEnabled(false);

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        Log.i(TAG, "onSaveInstanceState: fragmentNumber=" + fragmentNumber + ", fragmentCounter=" + fragmentCounter);

        outState.putInt(FRAGMENT_NUMBER, fragmentNumber);
        outState.putInt(FRAGMENT_COUNTER, fragmentCounter);
        //super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        Log.i(TAG, "onBackPressed: fragmentNumber=" + fragmentNumber + ", fragmentCounter=" + fragmentCounter);
        deleteFragment();
    }

    protected Fragment defineCurrentFragment(int fragmentNumber) {
        Fragment fragment = null;
        if (fragmentNumber == 1) {
            fragment = new Fragment1();
        } else if (fragmentNumber == 2) {
            fragment = new Fragment2();
        } else if (fragmentNumber == 3) {
            fragment = new Fragment3();
        } else {
            Log.i(TAG, "Undefined fragment number: " + fragmentNumber);
        }

        return fragment;
    }

    protected void deleteFragment() {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

        if (currentFragment != null) {
            fragmentTransaction.remove(currentFragment);
            currentFragment = null;
        }
    }

    protected void changeFragment(Fragment fragment) {
        if (fragment != null) {
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

            /*
            if (currentFragment != null) {
                fragmentTransaction.remove(currentFragment);
            }
            */

            //fragmentTransaction.add(R.id.container, currentFragment);
            fragmentTransaction.replace(R.id.container, fragment);
            fragmentTransaction.commit();

            currentFragment = fragment;
        }
    }

    private void setCounterText() {
        counter.setText("" + fragmentCounter);
    }

    protected void startAlarm(Context context) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(AlarmReceiver.ACTION);
        PendingIntent pending = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        long alarmTime = System.currentTimeMillis() + REPEAT_TIME;
        am.cancel(pending);

        am.setInexactRepeating(AlarmManager.RTC_WAKEUP, alarmTime, REPEAT_TIME, pending);

        Log.i(TAG, "Start alarm");
    }

    protected void stopAlarm(Context context) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(AlarmReceiver.ACTION);
        PendingIntent pending = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        am.cancel(pending);

        Log.i(TAG, "Stop alarm");
    }

    class AlarmReceiver extends BroadcastReceiver {
        public static final String ACTION = "com.askokov.countdown.ALARM_RECEIVER";

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "AlarmReceiver: onReceive: fragmentCounter=" + fragmentCounter + ", fragmentNumber=" + fragmentNumber);
            if (currentFragment != null) {
                fragmentCounter--;

                if (fragmentCounter < 0) {
                    fragmentCounter = MAX_COUNTER;
                    fragmentNumber = fragmentNumber == 3 ? fragmentNumber = 1 : ++fragmentNumber;

                    Fragment fragment = defineCurrentFragment(fragmentNumber);
                    changeFragment(fragment);

                    Log.i(TAG, "AlarmReceiver: onReceive: currentFragment=" + currentFragment);
                }
                setCounterText();
            }
        }
    }
}
