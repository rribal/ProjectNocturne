package co.uk.droidinactu.sensortagtester;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class SensorTagDashboard extends Activity implements ActionBar.OnNavigationListener {

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
		public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
				final Bundle savedInstanceState) {
			final View rootView = inflater.inflate(R.layout.fragment_sensor_tag_dashboard_dummy, container, false);
			final TextView dummyTextView = (TextView) rootView.findViewById(R.id.section_label);
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
		public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
				final Bundle savedInstanceState) {
			final View rootView = inflater.inflate(R.layout.fragment_sensor_tag_dashboard_sensors, container, false);
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

		/**
         *
         */
		public void updateValues(final SensorTagReadings readngs) {

		}
	}

	private final SensorTagReadings currReadings = new SensorTagReadings();

	private ImageView sensortagConnectedImg;

	private BluetoothAdapter mBluetoothAdapter;

	/**
	 * The serialization (saved instance state) Bundle key representing the
	 * current dropdown position.
	 */
	private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";

	private Handler mHandler;

	private boolean mScanning;

	private static final int REQUEST_ENABLE_BT = 1;

	// Stops scanning after 10 seconds.
	private static final long SCAN_PERIOD = 10000;

	// Device scan callback.
	private final BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

		@Override
		public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					// we've found a device so try to connect
				}
			});
		}
	};

	private void checkBluetoothAvailable() {
		// Use this check to determine whether BLE is supported on the device.
		// Then you can
		// selectively disable BLE-related features.
		if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
			Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
			finish();
		}

		// Initializes a Bluetooth adapter. For API level 18 and above, get a
		// reference to
		// BluetoothAdapter through BluetoothManager.
		final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
		mBluetoothAdapter = bluetoothManager.getAdapter();

		// Checks if Bluetooth is supported on the device.
		if (mBluetoothAdapter == null) {
			Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
			finish();
			return;
		}
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Set up the action bar to show a dropdown list.
		final ActionBar actionBar = getActionBar();
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

		// Set up the dropdown list navigation in the action bar.
		actionBar.setListNavigationCallbacks(
		// Specify a SpinnerAdapter to populate the dropdown list.
				new ArrayAdapter<String>(actionBar.getThemedContext(), android.R.layout.simple_list_item_1,
						android.R.id.text1, new String[] { getString(R.string.title_section1),
								getString(R.string.title_section2), getString(R.string.title_section3), }), this);

		sensortagConnectedImg = (ImageView) findViewById(R.id.main_img_connected);

		checkBluetoothAvailable();
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.sensor_tag_dashboard, menu);
		return true;
	}

	@Override
	public boolean onNavigationItemSelected(final int position, final long id) {
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
			getFragmentManager().beginTransaction().replace(R.id.container, fragment).commit();
			break;
		default:
			fragment = new DummySectionFragment();
			args = new Bundle();
			args.putInt(DummySectionFragment.ARG_SECTION_NUMBER, position + 1);
			fragment.setArguments(args);
			getFragmentManager().beginTransaction().replace(R.id.container, fragment).commit();
		}
		return true;
	}

	@Override
	protected void onPause() {
		super.onPause();
		scanLeDevice(false);
	}

	@Override
	public void onRestoreInstanceState(final Bundle savedInstanceState) {
		// Restore the previously serialized current dropdown position.
		if (savedInstanceState.containsKey(STATE_SELECTED_NAVIGATION_ITEM)) {
			getActionBar().setSelectedNavigationItem(savedInstanceState.getInt(STATE_SELECTED_NAVIGATION_ITEM));
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		// Ensures Bluetooth is enabled on the device. If Bluetooth is not
		// currently enabled,
		// fire an intent to display a dialog asking the user to grant
		// permission to enable it.
		if (!mBluetoothAdapter.isEnabled()) {
			if (!mBluetoothAdapter.isEnabled()) {
				final Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
				startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
			}
		}
		scanLeDevice(true);
	}

	@Override
	public void onSaveInstanceState(final Bundle outState) {
		// Serialize the current dropdown position.
		outState.putInt(STATE_SELECTED_NAVIGATION_ITEM, getActionBar().getSelectedNavigationIndex());
	}

	private void scanLeDevice(final boolean enable) {
		if (enable) {
			// Stops scanning after a pre-defined scan period.
			mHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					mScanning = false;
					mBluetoothAdapter.stopLeScan(mLeScanCallback);
					invalidateOptionsMenu();
				}
			}, SCAN_PERIOD);

			mScanning = true;
			mBluetoothAdapter.startLeScan(mLeScanCallback);
		} else {
			mScanning = false;
			mBluetoothAdapter.stopLeScan(mLeScanCallback);
		}
	}
}
