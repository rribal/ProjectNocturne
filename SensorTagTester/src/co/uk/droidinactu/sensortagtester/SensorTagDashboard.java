package co.uk.droidinactu.sensortagtester;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import co.uk.droidinactu.sensortagtester.constants.SampleGattAttributes;
import co.uk.droidinactu.sensortagtester.constants.TiBleConstants;

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

		public void updateTemperature(final String newValue) {
			sensvals_txtValue1.setText(newValue);
		}

		/**
         *
         */
		public void updateValues(final SensorTagReadings readngs) {

		}
	}

	private static final String LOG_TAG = SensorTagDashboard.class.getSimpleName();
	private final SensorTagReadings currReadings = new SensorTagReadings();
	private ImageView sensortagConnectedImg;
	private BluetoothAdapter mBluetoothAdapter;

	/**
	 * The serialization (saved instance state) Bundle key representing the
	 * current dropdown position.
	 */
	private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";

	private Handler mHandler;
	private String mDeviceName;
	private String mDeviceAddress;
	private BluetoothLeService mBluetoothLeService;
	private boolean mScanning;
	private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
	private final boolean mConnected = false;

	private static final int REQUEST_ENABLE_BT = 1;
	private BluetoothDevice mBtDevice;
	private BluetoothGatt mBluetoothGatt;
	private int mConnectionState = STATE_DISCONNECTED;
	private static final int STATE_DISCONNECTED = 0;
	private static final int STATE_CONNECTING = 1;
	private static final int STATE_CONNECTED = 2;

	private List<BluetoothGattService> mServiceList = null;
	private final Map<UUID, BluetoothGattCharacteristic> mCharacteristicList = new HashMap<UUID, BluetoothGattCharacteristic>();

	public final static String ACTION_GATT_CONNECTED = "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
	public final static String ACTION_GATT_DISCONNECTED = "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
	public final static String ACTION_GATT_SERVICES_DISCOVERED = "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
	public final static String ACTION_DATA_AVAILABLE = "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
	public final static String EXTRA_DATA = "com.example.bluetooth.le.EXTRA_DATA";

	// Stops scanning after 10 seconds.
	private static final long SCAN_PERIOD = 10000;

	private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
		@Override
		// Result of a characteristic read operation
		public void onCharacteristicRead(final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic,
				final int status) {
			if (status == BluetoothGatt.GATT_SUCCESS) {
				if (characteristic.getUuid().equals(UUID.fromString(TiBleConstants.IRTEMPERATURE_DATA_UUID))) {
					// update temp display
					sensorValsfragment.updateTemperature("" + characteristic.getValue());
				}
			}
		};

		@Override
		public void onConnectionStateChange(final BluetoothGatt gatt, final int status, final int newState) {
			if (newState == BluetoothProfile.STATE_CONNECTED) {
				mConnectionState = STATE_CONNECTED;
				Log.i(LOG_TAG, "PollingService :: Connected to GATT server.");
				Log.i(LOG_TAG,
						"PollingService :: Attempting to start service discovery: " + mBluetoothGatt.discoverServices());

			} else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
				mConnectionState = STATE_DISCONNECTED;
				Log.i(LOG_TAG, "PollingService :: Disconnected from GATT server.");
			}
		}

		@Override
		// New services discovered
		public void onServicesDiscovered(final BluetoothGatt gatt, final int status) {
			Log.i(LOG_TAG, "PollingService :: onServicesDiscovered()");
			if (status == BluetoothGatt.GATT_SUCCESS) {
				mServiceList = mBluetoothGatt.getServices();
				parseGattServices(mServiceList);
			} else {
				Log.w(LOG_TAG, "PollingService :: onServicesDiscovered received: " + status);
			}
		}
	};

	// Device scan callback.
	private final BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
		@Override
		public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
			mBtDevice = device;
			main_txt_device_name.setText(mBtDevice.getName());
			main_txt_device_address.setText(mBtDevice.getAddress());

			Log.i(LOG_TAG, "PollingService :: Found device [" + mBtDevice.getName() + "]");
			if (mBtDevice.getName().equalsIgnoreCase("SensorTag")) {
				scanLeDevice(false);
				mBluetoothGatt = device.connectGatt(getApplication(), false, mGattCallback);
			}
		}
	};

	// Code to manage Service lifecycle.
	private final ServiceConnection mServiceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(final ComponentName componentName, final IBinder service) {
			mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
			if (!mBluetoothLeService.initialize()) {
				Log.e(LOG_TAG, "Unable to initialize Bluetooth");
				finish();
			}
			// Automatically connects to the device upon successful start-up
			// Initialisation.
			mBluetoothLeService.connect(mDeviceAddress);
		}

		@Override
		public void onServiceDisconnected(final ComponentName componentName) {
			mBluetoothLeService = null;
		}
	};

	private final String LIST_NAME = "NAME";
	private final String LIST_UUID = "UUID";
	private TextView main_txt_device_name;
	private TextView main_txt_device_address;

	SensorValuesSectionFragment sensorValsfragment = new SensorValuesSectionFragment();

	private void checkBluetoothAvailable() {
		// Use this check to determine whether BLE is supported on the device.
		// Then you can selectively disable BLE-related features.
		if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
			Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
			finish();
		}

		// Initializes a Bluetooth adapter. For API level 18 and above, get a
		// reference to BluetoothAdapter through BluetoothManager.
		final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
		mBluetoothAdapter = bluetoothManager.getAdapter();

		// Checks if Bluetooth is supported on the device.
		if (mBluetoothAdapter == null) {
			Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
			finish();
			return;
		}
	}

	private BluetoothGattCharacteristic getCharacteristic(final BluetoothGattService gattService,
			final String charUuidStr) {
		return getCharacteristic(gattService, UUID.fromString(charUuidStr));
	}

	private BluetoothGattCharacteristic getCharacteristic(final BluetoothGattService gattService, final UUID charUuid) {
		final List<BluetoothGattCharacteristic> tmpCharList = gattService.getCharacteristics();
		for (final BluetoothGattCharacteristic bleChar : tmpCharList) {
			if (bleChar.getUuid().equals(charUuid)) { return bleChar; }
		}
		return null;
	}

	@Override
	protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
		// Ensures Bluetooth is enabled on the device. If Bluetooth is not
		// currently enabled, fire an intent to display a dialog asking the user
		// to grant permission to enable it.
		if (!mBluetoothAdapter.isEnabled()) {
			if (!mBluetoothAdapter.isEnabled()) {
				final Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
				startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
			}
		} else {
			scanLeDevice(true);
		}
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mHandler = new Handler();

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

		checkBluetoothAvailable();

		sensortagConnectedImg = (ImageView) findViewById(R.id.main_img_connected);

		main_txt_device_name = (TextView) findViewById(R.id.main_txt_device_name);
		main_txt_device_address = (TextView) findViewById(R.id.main_txt_device_address);
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
			args = new Bundle();
			args.putInt(DummySectionFragment.ARG_SECTION_NUMBER, position + 1);
			sensorValsfragment.setArguments(args);
			getFragmentManager().beginTransaction().replace(R.id.container, sensorValsfragment).commit();
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
		// currently enabled, fire an intent to display a dialog asking the user
		// to grant permission to enable it.
		if (!mBluetoothAdapter.isEnabled()) {
			final Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		} else {
			scanLeDevice(true);
		}
	}

	@Override
	public void onSaveInstanceState(final Bundle outState) {
		// Serialize the current dropdown position.
		outState.putInt(STATE_SELECTED_NAVIGATION_ITEM, getActionBar().getSelectedNavigationIndex());
	}

	private void parseGattServices(final List<BluetoothGattService> gattServices) {
		if (gattServices == null) { return; }
		String uuid = null;
		final String unknownServiceString = "unknown_service";
		final String unknownCharaString = "unknown_characteristic";
		final ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();
		final ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData = new ArrayList<ArrayList<HashMap<String, String>>>();
		mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

		// Loops through available GATT Services.
		for (final BluetoothGattService gattService : gattServices) {
			final HashMap<String, String> currentServiceData = new HashMap<String, String>();
			uuid = gattService.getUuid().toString();

			if (uuid.equalsIgnoreCase(TiBleConstants.IRTEMPERATURE_SERV_UUID)) {
				// enableTempSensor
				final byte[] data = new byte[1];
				data[0] = (byte) Integer.parseInt("1", 16);
				BluetoothGattCharacteristic mChar = getCharacteristic(gattService,
						TiBleConstants.IRTEMPERATURE_CONF_UUID);
				mChar.setValue(data);
				mBluetoothGatt.writeCharacteristic(mChar);

				mChar = getCharacteristic(gattService, TiBleConstants.IRTEMPERATURE_DATA_UUID);
				final boolean tempVal = mBluetoothGatt.readCharacteristic(mChar);

			} else if (uuid.equalsIgnoreCase(TiBleConstants.ACCELEROMETER_SERV_UUID)) {
				// get accelerometer data
				final List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
				for (final BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
					Log.i(LOG_TAG,
							"accel characteristic ["
									+ SampleGattAttributes.lookup(gattCharacteristic.getUuid().toString(),
											unknownCharaString) + "]");
				}
			}

		}
	}

	private void scanLeDevice(final boolean enable) {
		if (enable) {
			// Stops scanning after a pre-defined scan period.
			mHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					mScanning = false;
					mBluetoothAdapter.stopLeScan(mLeScanCallback);
				}
			}, SCAN_PERIOD);

			mScanning = true;
			mBluetoothAdapter.startLeScan(mLeScanCallback);
		} else {
			mScanning = false;
			mBluetoothAdapter.stopLeScan(mLeScanCallback);
		}
	}

	private void updateConnectionState(final int sensortagConnected) {
		// TODO Auto-generated method stub

	}
}
