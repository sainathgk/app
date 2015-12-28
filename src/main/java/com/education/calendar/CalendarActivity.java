/*
 * Copyright (C) 2011 Chris Gao <chris@exina.net>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.education.calendar;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.education.schoolapp.R;

public class CalendarActivity extends AppCompatActivity implements CalendarView.OnCellTouchListener, View.OnClickListener {
    public static final String MIME_TYPE = "vnd.android.cursor.dir/vnd.exina.android.calendar.date";
    CalendarView mView = null;
    TextView mHit;
    Handler mHandler = new Handler();
    private Button mPreviousMonth;
    private Button mNextMonth;
    private Button mCurrentMonth;
    private TextView mMonthDisplay;
    private static String[] monthArray = {"January", "February", "March", "April", "May", "June", "July",
            "August", "September", "October", "November", "December"};

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        setTitle(R.string.action_calendar);

        mView = (CalendarView) findViewById(R.id.calendar);
        mView.setOnCellTouchListener(this);

        if (getIntent().getAction().equals(Intent.ACTION_PICK))
            findViewById(R.id.hint).setVisibility(View.INVISIBLE);

        mMonthDisplay = (TextView) findViewById(R.id.month_display);

        mMonthDisplay.setText(String.format("%s  %d", monthArray[mView.getMonth()], mView.getYear()));

        mPreviousMonth = (Button) findViewById(R.id.previous_month);
        mPreviousMonth.setOnClickListener(this);

        mNextMonth = (Button) findViewById(R.id.next_month);
        mNextMonth.setOnClickListener(this);

        mCurrentMonth = (Button) findViewById(R.id.current_month);
        mCurrentMonth.setOnClickListener(this);
    }

    public void onTouch(Cell cell) {
        Intent intent = getIntent();
        String action = intent.getAction();
        if (action.equals(Intent.ACTION_PICK) || action.equals(Intent.ACTION_GET_CONTENT)) {
            int year = mView.getYear();
            int month = mView.getMonth();
            int day = cell.getDayOfMonth();

            // FIX issue 6: make some correction on month and year
            if (cell instanceof CalendarView.GrayCell) {
                // oops, not pick current month...
                if (day < 15) {
                    // pick one beginning day? then a next month day
                    if (month == 11) {
                        month = 0;
                        year++;
                    } else {
                        month++;
                    }

                } else {
                    // otherwise, previous month
                    if (month == 0) {
                        month = 11;
                        year--;
                    } else {
                        month--;
                    }
                }
            }

            Intent ret = new Intent();
            ret.putExtra("year", year);
            ret.putExtra("month", month);
            ret.putExtra("day", day);
            this.setResult(RESULT_OK, ret);
            finish();
            return;
        }
        /*int day = cell.getDayOfMonth();
        if(mView.firstDay(day))
			mView.previousMonth();
		else if(mView.lastDay(day))
			mView.nextMonth();
		else
			return;*/

        mHandler.post(new Runnable() {
            public void run() {
                Toast.makeText(CalendarActivity.this, DateUtils.getMonthString(mView.getMonth(), DateUtils.LENGTH_LONG) + " " + mView.getYear(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    public void onClick(View v) {

        int buttonId = v.getId();

        switch (buttonId) {
            case R.id.previous_month:
                mView.previousMonth();
                break;

            case R.id.next_month:
                mView.nextMonth();
                break;

            case R.id.current_month:
                mView.goToday();
                break;
        }

        mMonthDisplay.setText(String.format("%s  %d", monthArray[mView.getMonth()], mView.getYear()));
    }
}