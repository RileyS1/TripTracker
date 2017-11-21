package org.pltw.examples.triptracker;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.text.format.DateFormat;
import android.widget.ProgressBar;
import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.BackendlessCallback;
import com.backendless.exceptions.BackendlessFault;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TripFragment extends Fragment {

    private Trip mTrip;
    private EditText mNameField;
    private EditText mDescriptionField;
    private Button mStartDateButton;
    private Button mEndDateButton;
    private CheckBox mPublicCheckBox;
    private boolean mEnabled = true;
    private boolean mPublicView = false;

    private static final String TAG = "TripFragment";
    private static final String DIALOG_DATE = "date";
    private static final int REQUEST_START_DATE = 1;
    private static final int REQUEST_END_DATE = 2;
    private static final String DATE_FORMAT = "E MM-dd-yyyy";

    @Override
    public void onCreate(Bundle savedInstanceSate) {
        super.onCreate(savedInstanceSate);
        setHasOptionsMenu(true);
        getActivity().setTitle(R.string.trip_details_text);
        String tripId = (String)getActivity().getIntent().getSerializableExtra(Trip.EXTRA_TRIP_ID);
        mPublicView = getActivity().getIntent().getBooleanExtra(Trip.EXTRA_TRIP_PUBLIC_VIEW, false);

        if (tripId.equals("0")) {
            mTrip = new Trip();
        } else {
            String id, name, desc;
            Date sDate, eDate;
            boolean shared;
            Intent intent;
            intent = getActivity().getIntent();
            id = intent.getStringExtra(Trip.EXTRA_TRIP_ID);
            name = intent.getStringExtra(Trip.EXTRA_TRIP_NAME);
            desc = intent.getStringExtra(Trip.EXTRA_TRIP_DESC);
            sDate = (Date)intent.getSerializableExtra(Trip.EXTRA_TRIP_START_DATE);
            eDate = (Date)intent.getSerializableExtra(Trip.EXTRA_TRIP_END_DATE);
            shared = intent.getBooleanExtra(Trip.EXTRA_TRIP_PUBLIC, false);

            mTrip = new Trip();
            mTrip.setObjectId(id);
            mTrip.setName(name);
            mTrip.setDescription(desc);
            mTrip.setStartDate(sDate);
            mTrip.setEndDate(eDate);
            mTrip.setShared(shared);

            if (mPublicView)
                mEnabled = false;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_trip, parent, false);

        mNameField = (EditText)v.findViewById(R.id.enter_trip_name);
        mNameField.setText(mTrip.getName());
        mNameField.setEnabled(mEnabled);

        mDescriptionField = (EditText)v.findViewById(R.id.enter_trip_description);
        mDescriptionField.setText(mTrip.getDescription());
        mDescriptionField.setEnabled(mEnabled);

        mStartDateButton = (Button)v.findViewById(R.id.start_date_button);
        updateDateView(mStartDateButton, mTrip.getStartDate());
        mStartDateButton.setEnabled(mEnabled);

        mEndDateButton = (Button)v.findViewById(R.id.end_date_button);
        updateDateView(mEndDateButton, mTrip.getEndDate());
        mEndDateButton.setEnabled(mEnabled);

        mPublicCheckBox = (CheckBox)v.findViewById(R.id.trip_public);
        mPublicCheckBox.setChecked(mTrip.isShared());
        mPublicCheckBox.setEnabled(mEnabled);

        if (mEnabled) {
            MyDateOnClickListener startDateListener = new MyDateOnClickListener();
            mStartDateButton.setOnClickListener(startDateListener);

            MyDateOnClickListener endDateListener = new MyDateOnClickListener();
            mEndDateButton.setOnClickListener(endDateListener);
        }

        return v;
    }

    private class MyDateOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {

            FragmentManager fm = getActivity().getSupportFragmentManager();
            DatePickerFragment dialog;

            if (v.getId()==R.id.start_date_button) {
                dialog = DatePickerFragment.newInstance(getDateFromView(mStartDateButton), R.string.start_date_hint);
                dialog.setTargetFragment(TripFragment.this, REQUEST_START_DATE);
            } else {
                dialog = DatePickerFragment.newInstance(getDateFromView(mEndDateButton), R.string.end_date_hint);
                dialog.setTargetFragment(TripFragment.this, REQUEST_END_DATE);
            }
            dialog.show(fm, DIALOG_DATE);

        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_trip_details, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                if (NavUtils.getParentActivityName(getActivity()) != null) {
                    NavUtils.navigateUpFromSameTask(getActivity());
                }
                return true;
            case R.id.action_post:
                if (!mEnabled) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setMessage(R.string.post_error_message);
                    builder.setTitle(R.string.post_error_title);
                    builder.setPositiveButton(android.R.string.ok, null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                } else {
                    updateTrip(item);
                }
                return true;
            case R.id.action_delete:
                if (!mEnabled) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setMessage(R.string.delete_error_message);
                    builder.setTitle(R.string.delete_error_title);
                    builder.setPositiveButton(android.R.string.ok, null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                } else {
                    deleteTrip(item);

                }
                return true;
            case R.id.action_logout:
                Backendless.UserService.logout(new BackendlessCallback<Void>() {
                    @Override
                    public void handleResponse(Void v) {
                        boolean isValidLogin = Backendless.UserService.isValidLogin();
                        if (!isValidLogin) {
                            Log.i(TAG, "Successful logout");
                        }
                    }
                    @Override
                    public void handleFault(BackendlessFault backendlessFault) {
                        Log.i(TAG, "Server reported an error " + backendlessFault.getMessage());
                    }
                });
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_settings:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){

        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        if (requestCode == REQUEST_START_DATE) {
            Date date = (Date)data.getSerializableExtra(DatePickerFragment.EXTRA_DATE);
            updateDateView(mStartDateButton, date);
        } else if (requestCode == REQUEST_END_DATE) {
            Date date = (Date)data.getSerializableExtra(DatePickerFragment.EXTRA_DATE);
            updateDateView(mEndDateButton, date);
        }

    }

    private void updateDateView(Button dateButton, Date date) {
        dateButton.setText(DateFormat.format(DATE_FORMAT, date));
    }

    private Date getDateFromView(Button dateButton) {

        String dateString;
        Date date;

        dateString = dateButton.getText().toString();
        SimpleDateFormat df = new SimpleDateFormat(DATE_FORMAT, Locale.US);

        try {
            date = df.parse(dateString);
        }
        catch (Exception e) {
            date = new Date();
            Log.d(TAG, "Exception: " + e);
        }
        return(date);
    }

    private void updateTrip(MenuItem menuItem) {

        String name, desc;
        Date sDate, eDate;
        boolean shared;

        name = mNameField.getText().toString();
        desc = mDescriptionField.getText().toString();
        sDate = getDateFromView(mStartDateButton);
        eDate = getDateFromView(mEndDateButton);
        shared = mPublicCheckBox.isChecked();

        name = name.trim();
        desc = desc.trim();

        if (name.isEmpty()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(R.string.trip_error_message);
            builder.setTitle(R.string.trip_error_title);
            builder.setPositiveButton(android.R.string.ok, null);
            AlertDialog dialog = builder.create();
            dialog.show();
        }
        else {
            menuItem.setActionView(new ProgressBar(getContext()));

            mTrip.setName(name);
            mTrip.setDescription(desc);
            mTrip.setStartDate(sDate);
            mTrip.setEndDate(eDate);
            mTrip.setShared(shared);
            mTrip.setOwnerId(Backendless.UserService.CurrentUser().getObjectId());

            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    Backendless.Data.of(Trip.class).save(mTrip);
                    getActivity().finish();
                }
            });
            thread.start();
            try {
                thread.join();
            } catch (InterruptedException e) {
                Log.e(TAG, "Saving trip failed: " + e.getMessage());
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage(e.getMessage());
                builder.setTitle(R.string.trip_error_title);
                builder.setPositiveButton(android.R.string.ok, null);
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        }
    }

    private void deleteTrip(MenuItem menuItem) {
        if (mTrip.getObjectId() != null){
            final Trip deleteTrip = mTrip;
            Thread deleteThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    Backendless.Data.of(Trip.class).remove(deleteTrip);
                    getActivity().finish();
                }
            });
            deleteThread.start();
            try {
                deleteThread.join();
            } catch (InterruptedException e) {
                Log.e(TAG, "Deleting trip failed: " + e.getMessage());
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage(e.getMessage());
                builder.setTitle(R.string.delete_error_title);
                builder.setPositiveButton(android.R.string.ok, null);
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        }
    }

}