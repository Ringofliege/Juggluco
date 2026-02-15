/*      This file is part of Juggluco, an Android app to receive and display         */
/*      glucose values from Freestyle Libre 2 and 3 sensors.                         */
/*                                                                                   */
/*      Copyright (C) 2021 Jaap Korthals Altes <jaapkorthalsaltes@gmail.com>         */
/*                                                                                   */
/*      Juggluco is free software: you can redistribute it and/or modify             */
/*      it under the terms of the GNU General Public License as published            */
/*      by the Free Software Foundation, either version 3 of the License, or         */
/*      (at your option) any later version.                                          */
/*                                                                                   */
/*      Juggluco is distributed in the hope that it will be useful, but              */
/*      WITHOUT ANY WARRANTY; without even the implied warranty of                   */
/*      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         */
/*      See the GNU General Public License for more details.                         */
/*                                                                                   */
/*      You should have received a copy of the GNU General Public License            */
/*      along with Juggluco. If not, see <https://www.gnu.org/licenses/>.            */
/*                                                                                   */


package tk.glucodata;

import android.app.Activity;
import android.app.KeyguardManager;
import android.os.Build;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import static tk.glucodata.Log.doLog;
import static tk.glucodata.Natives.getisalarm;

public class LockScreenAlarmActivity extends Activity {
    private static final String LOG_ID = "LockScreenAlarm";
    static final String EXTRA_GLUCOSE_VALUE = "glucose_value";
    static final String EXTRA_GLUCOSE_ARROW = "glucose_arrow";
    static final String EXTRA_ALARM_MESSAGE = "alarm_message";
    private boolean alarmStopped = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!getisalarm()) {
            {if(doLog) {Log.i(LOG_ID,"alarm already dismissed, finishing");};};
            finish();
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
            KeyguardManager keyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
            if (keyguardManager != null) {
                keyguardManager.requestDismissKeyguard(this, null);
            }
        } else {
            getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
            );
        }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.lockscreen_alarm);

        String glucoseValue = getIntent().getStringExtra(EXTRA_GLUCOSE_VALUE);
        String glucoseArrow = getIntent().getStringExtra(EXTRA_GLUCOSE_ARROW);
        String alarmMessage = getIntent().getStringExtra(EXTRA_ALARM_MESSAGE);

        TextView valueView = findViewById(R.id.lockscreen_glucose_value);
        TextView arrowView = findViewById(R.id.lockscreen_glucose_arrow);
        TextView messageView = findViewById(R.id.lockscreen_alarm_message);
        Button stopButton = findViewById(R.id.lockscreen_stop_alarm);

        if (glucoseValue != null) {
            valueView.setText(glucoseValue);
            valueView.setContentDescription(glucoseValue + " " + Notify.unitlabel);
        }
        if (glucoseArrow != null) {
            arrowView.setText(glucoseArrow);
        }
        if (alarmMessage != null) {
            messageView.setText(alarmMessage);
        }

        stopButton.setOnClickListener(v -> {
            stopAlarmAndDismiss();
        });
    }

    private void stopAlarmAndDismiss() {
        {if(doLog) {Log.i(LOG_ID,"Stop Alarm from lock screen");};};
        alarmStopped = true;
        Notify.stopalarm();
        finish();
    }

    @Override
    public void onBackPressed() {
        {if(doLog) {Log.i(LOG_ID,"Back pressed on lock screen alarm");};};
        stopAlarmAndDismiss();
    }

    @Override
    protected void onDestroy() {
        if (!alarmStopped && getisalarm()) {
            {if(doLog) {Log.i(LOG_ID,"Lock screen alarm activity destroyed, stopping alarm");};};
            Notify.stopalarm();
        }
        super.onDestroy();
    }
}
