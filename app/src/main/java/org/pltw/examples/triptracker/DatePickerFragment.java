package org.pltw.examples.triptracker;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.DatePicker;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class DatePickerFragment extends DialogFragment {

    public static final String EXTRA_DATE = "org.pltw.examples.triptracker.DATE";
    public static final String EXTRA_DATE_LABEL = "org.pltw.examples.triptracker.DATE_LABEL";

    private Date mDate;
    private int mDateLabelId;

    public static DatePickerFragment newInstance(Date date, int dateLabel) {
        Bundle args = new Bundle();
        args.putSerializable(EXTRA_DATE, date);
        args.putSerializable(EXTRA_DATE_LABEL, dateLabel);

        DatePickerFragment fragment = new DatePickerFragment();
        fragment.setArguments(args);

        return fragment;
    }

    private void sendResult(int resultCode) {
        if (getTargetFragment() == null) return;

        Intent i = new Intent();
        i.putExtra(EXTRA_DATE, mDate);
        i.putExtra(EXTRA_DATE_LABEL, mDateLabelId);

        getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode, i);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        mDate = (Date)getArguments().getSerializable(EXTRA_DATE);
        mDateLabelId = getArguments().getInt(EXTRA_DATE_LABEL);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(mDate);
        int year = calendar.get(Calendar.YEAR);
        int monthOfYear = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        View v = getActivity().getLayoutInflater().inflate(R.layout.dialog_date, null);

        DatePicker datePicker = (DatePicker)v.findViewById(R.id.dialog_datePicker);
        datePicker.setCalendarViewShown(false);

        MyOnDateChangedListener dateChangedListener = new MyOnDateChangedListener();
        datePicker.init(year, monthOfYear, dayOfMonth, dateChangedListener);

        MyOnClickListener onClickListener = new MyOnClickListener();
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        dialogBuilder.setView(v);
        dialogBuilder.setTitle(mDateLabelId);
        dialogBuilder.setPositiveButton(android.R.string.ok, onClickListener);

        return dialogBuilder.create();
    }

    private class MyOnDateChangedListener implements DatePicker.OnDateChangedListener {

        @Override
        public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            mDate = new GregorianCalendar(year, monthOfYear, dayOfMonth).getTime();
            getArguments().putSerializable(EXTRA_DATE, mDate);
        }
    }

    private class MyOnClickListener implements DialogInterface.OnClickListener {

        @Override
        public void onClick(DialogInterface dialog, int which) {
            sendResult(Activity.RESULT_OK);
        }
    }
}