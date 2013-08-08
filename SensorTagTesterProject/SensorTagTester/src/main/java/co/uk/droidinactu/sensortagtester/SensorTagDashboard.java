package co.uk.droidinactu.sensortagtester;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class SensorTagDashboard extends Activity implements ActionBar.OnNavigationListener {

    private SensorTagReadings currReadings=new SensorTagReadings();

    /**
     * The serialization (saved instance state) Bundle key representing the
     * current dropdown position.
     */
    private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up the action bar to show a dropdown list.
        final ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

        // Set up the dropdown list navigation in the action bar.
        actionBar.setListNavigationCallbacks(
                // Specify a SpinnerAdapter to populate the dropdown list.
                new ArrayAdapter<String>(
                        actionBar.getThemedContext(),
                        android.R.layout.simple_list_item_1,
                        android.R.id.text1,
                        new String[]{
                                getString(R.string.title_section1),
                                getString(R.string.title_section2),
                                getString(R.string.title_section3),
                        }),
                this);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        // Restore the previously serialized current dropdown position.
        if (savedInstanceState.containsKey(STATE_SELECTED_NAVIGATION_ITEM)) {
            getActionBar().setSelectedNavigationItem(
                    savedInstanceState.getInt(STATE_SELECTED_NAVIGATION_ITEM));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // Serialize the current dropdown position.
        outState.putInt(STATE_SELECTED_NAVIGATION_ITEM,
                getActionBar().getSelectedNavigationIndex());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.sensor_tag_dashboard, menu);
        return true;
    }

    @Override
    public boolean onNavigationItemSelected(int position, long id) {
        // When the given dropdown item is selected, show its contents in the
        // container view.
        Fragment fragment;
        Bundle args;
        switch (position) {
            case 0:
                fragment = new SensorValuesSectionFragment();
                args = new Bundle();
                args.putInt(DummySectionFragment.ARG_SECTION_NUMBER, position + 1);
                fragment.setArguments(args);
                getFragmentManager().beginTransaction()
                        .replace(R.id.container, fragment)
                        .commit();
                break;
            default:
                fragment = new DummySectionFragment();
                args = new Bundle();
                args.putInt(DummySectionFragment.ARG_SECTION_NUMBER, position + 1);
                fragment.setArguments(args);
                getFragmentManager().beginTransaction()
                        .replace(R.id.container, fragment)
                        .commit();
        }
        return true;
    }

    /**
     * A dummy fragment representing a section of the app, but that simply
     * displays dummy text.
     */
    public static class DummySectionFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        public static final String ARG_SECTION_NUMBER = "section_number";

        public DummySectionFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_sensor_tag_dashboard_dummy, container, false);
            TextView dummyTextView = (TextView) rootView.findViewById(R.id.section_label);
            dummyTextView.setText("Dummy" + Integer.toString(getArguments().getInt(ARG_SECTION_NUMBER)));
            return rootView;
        }
    }

    /**
     * This fragment displays the values read from the SensorTag.
     */
    public static class SensorValuesSectionFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        public static final String ARG_SECTION_NUMBER = "section_number";
        TextView sensvals_txtTitle;
        TextView sensvals_txtLbl1;
        TextView sensvals_txtLbl2;
        TextView sensvals_txtLbl3;
        TextView sensvals_txtLbl4;
        TextView sensvals_txtLbl5;

        TextView sensvals_txtValue1;
        TextView sensvals_txtValue2;
        TextView sensvals_txtValue3;
        TextView sensvals_txtValue4;
        TextView sensvals_txtValue5;

        /**
         *
         */
        public void updateValues(SensorTagReadings readngs){

        }


        /**
         *
         */
        public SensorValuesSectionFragment() {
        }

        /**
         *
         * @param inflater
         * @param container
         * @param savedInstanceState
         * @return
         */
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_sensor_tag_dashboard_sensors, container, false);
            sensvals_txtTitle = (TextView) rootView.findViewById(R.id.sensvals_txtTitle);
            sensvals_txtLbl1 = (TextView) rootView.findViewById(R.id.sensvals_txtLbl1);
            sensvals_txtLbl2 = (TextView) rootView.findViewById(R.id.sensvals_txtLbl2);
            sensvals_txtLbl3 = (TextView) rootView.findViewById(R.id.sensvals_txtLbl3);
            sensvals_txtLbl4 = (TextView) rootView.findViewById(R.id.sensvals_txtLbl4);
            sensvals_txtLbl5 = (TextView) rootView.findViewById(R.id.sensvals_txtLbl5);
            sensvals_txtValue1 = (TextView) rootView.findViewById(R.id.sensvals_txtValue1);
            sensvals_txtValue2 = (TextView) rootView.findViewById(R.id.sensvals_txtValue2);
            sensvals_txtValue3 = (TextView) rootView.findViewById(R.id.sensvals_txtValue3);
            sensvals_txtValue4 = (TextView) rootView.findViewById(R.id.sensvals_txtValue4);
            sensvals_txtValue5 = (TextView) rootView.findViewById(R.id.sensvals_txtValue5);
            sensvals_txtTitle.setText("Sensors " + Integer.toString(getArguments().getInt(ARG_SECTION_NUMBER)));
            return rootView;
        }
    }

}
