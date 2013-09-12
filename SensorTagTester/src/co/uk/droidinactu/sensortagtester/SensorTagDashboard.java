package co.uk.droidinactu.sensortagtester;

import static android.bluetooth.BluetoothGattCharacteristic.FORMAT_UINT8;
import static android.bluetooth.BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE;
import static android.bluetooth.BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import co.uk.droidinactu.sensortagtester.constants.BleServices;
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

		public void updateGyroscope(final String newValue) {
			sensvals_txtValue2.setText(newValue);
		}

		public void updateKeys(final String newValue) {
			sensvals_txtValue3.setText(newValue);
		}

		public void updateTemperature(final String newValue) {
			sensvals_txtValue1.setText(newValue);
		}

	}

	private static final String LOG_TAG = SensorTagDashboard.class.getSimpleName();
	private ImageView sensortagConnectedImg;
	private BluetoothAdapter mBluetoothAdapter;

	/**
	 * The serialisation (saved instance state) Bundle key representing the
	 * current dropdown position.
	 */
	private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";

	private final WriteQueue writeQueue = new WriteQueue();
	private Handler mHandler;
	private String mDeviceName;
	private String mDeviceAddress;
	private boolean mScanning = false;
	private boolean mDeviceFound = false;
	private boolean mDeviceConnected = false;

	private static final int REQUEST_ENABLE_BT = 1;
	private BluetoothDevice mBtDeviceST;
	private BluetoothDevice mBtDeviceHrm;
	private BluetoothGatt mBluetoothGattST;
	private BluetoothGatt mBluetoothGattHrm;
	private int mConnectionState = STATE_DISCONNECTED;
	private static final int STATE_DISCONNECTED = 0;
	private static final int STATE_CONNECTING = 1;
	private static final int STATE_CONNECTED = 2;

	private List<BluetoothGattService> mServiceList = null;
	private List<BluetoothGattService> mServiceListHrm = null;

	public final static String ACTION_GATT_CONNECTED = "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
	public final static String ACTION_GATT_DISCONNECTED = "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
	public final static String ACTION_GATT_SERVICES_DISCOVERED = "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
	public final static String ACTION_DATA_AVAILABLE = "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
	public final static String EXTRA_DATA = "com.example.bluetooth.le.EXTRA_DATA";

	// Stops scanning after 10 seconds.
	private static final long SCAN_PERIOD = 15000;

	private final BluetoothGattCallback mGattCallbackHrm = new BluetoothGattCallback() {

		@Override
		public void onCharacteristicChanged(final BluetoothGatt aGatt, final BluetoothGattCharacteristic aCharacteristic) {
			Log.v(LOG_TAG,
					"mGattCallbackHrm :: onCharacteristicChanged() [UUID:"
							+ SampleGattAttributes.lookup(aCharacteristic.getUuid().toString(), aCharacteristic
									.getUuid().toString()) + "]");
		}

		@Override
		public void onConnectionStateChange(final BluetoothGatt aGatt, final int aStatus, final int aNewState) {
			Log.v(LOG_TAG, "mGattCallbackHrm :: onConnectionStateChange()");
			super.onConnectionStateChange(aGatt, aStatus, aNewState);
		}

		@Override
		public void onServicesDiscovered(final BluetoothGatt aGatt, final int aStatus) {
			Log.v(LOG_TAG, "mGattCallbackHrm :: onServicesDiscovered()");
			try {
				if (aStatus == BluetoothGatt.GATT_SUCCESS) {
					mServiceListHrm = mBluetoothGattHrm.getServices();
					parseGattServicesHrm(mServiceListHrm);
				} else {
					Log.w(LOG_TAG, "BluetoothGattCallback :: onServicesDiscovered received: " + aStatus);
				}
			} catch (final Exception e) {
				Log.e(LOG_TAG, "", e);
			}
		}

	};

	private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
		@Override
		public void onCharacteristicChanged(final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
			Log.v(LOG_TAG,
					"BluetoothGattCallback :: onCharacteristicChanged() [UUID:"
							+ SampleGattAttributes.lookup(characteristic.getUuid().toString(), characteristic.getUuid()
									.toString()) + "]");

			try {
				if (characteristic.getUuid().equals(UUID.fromString(TiBleConstants.IRTEMPERATURE_DATA_UUID))) {
					Log.v(LOG_TAG, "BluetoothGattCallback :: onCharacteristicChanged() : IRTEMPERATURE_DATA_UUID");

					// Ambient temp is offset 2
					final int offset = 2;
					final Integer lowerByte = characteristic.getIntValue(FORMAT_UINT8, offset);
					final Integer upperByte = characteristic.getIntValue(FORMAT_UINT8, offset + 1);
					final double value = ((upperByte << 8) + lowerByte) / 128.0;
					// update display
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							final String str1Fmt = "%s °C";
							sensorValsfragment.updateTemperature(String.format(str1Fmt, value));
						}
					});
				} else if (characteristic.getUuid().equals(UUID.fromString(TiBleConstants.ACCELEROMETER_DATA_UUID))) {
					Log.v(LOG_TAG, "BluetoothGattCallback :: onCharacteristicChanged() : ACCELEROMETER_DATA_UUID");
				} else if (characteristic.getUuid().equals(UUID.fromString(TiBleConstants.BAROMETER_DATA_UUID))) {
					Log.v(LOG_TAG, "BluetoothGattCallback :: onCharacteristicChanged() : BAROMETER_DATA_UUID");
				} else if (characteristic.getUuid().equals(UUID.fromString(TiBleConstants.GYROSCOPE_DATA_UUID))) {
					Log.v(LOG_TAG, "BluetoothGattCallback :: onCharacteristicChanged() : GYROSCOPE_DATA_UUID");

					// NB: x,y,z has a weird order.
					final float y = MathUtils.shortSignedAtOffset(characteristic, 0) * (500f / 65536f) * -1;
					final float x = MathUtils.shortSignedAtOffset(characteristic, 2) * (500f / 65536f);
					final float z = MathUtils.shortSignedAtOffset(characteristic, 4) * (500f / 65536f);

					final DecimalFormat decimal = new DecimalFormat("+0.00;-0.00");
					final String msg = "X: " + decimal.format(x) + "/s" + "\nY: " + decimal.format(y) + "/s" + "\nZ: "
							+ decimal.format(z) + "/s";

					Log.v(LOG_TAG, "BluetoothGattCallback :: onCharacteristicRead() : Gyroscope Data [" + msg + "]");
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							sensorValsfragment.updateGyroscope(msg);
						}
					});

				} else if (characteristic.getUuid().equals(UUID.fromString(TiBleConstants.HUMIDITY_DATA_UUID))) {
					Log.v(LOG_TAG, "BluetoothGattCallback :: onCharacteristicChanged() : HUMIDITY_DATA_UUID");
				} else if (characteristic.getUuid().equals(UUID.fromString(TiBleConstants.MAGNETOMETER_DATA_UUID))) {
					Log.v(LOG_TAG, "BluetoothGattCallback :: onCharacteristicChanged() : MAGNETOMETER_DATA_UUID");
				} else if (characteristic.getUuid().equals(UUID.fromString(TiBleConstants.SIMPLE_KEYS_KEYPRESSED_UUID))) {
					Log.v(LOG_TAG, "BluetoothGattCallback :: onCharacteristicChanged() : SIMPLE_KEYS_KEYPRESSED_UUID");
					/*
					 * The key state is encoded into 1 unsigned byte. bit 0
					 * designates the right key. bit 1 designates the left key.
					 * bit 2 designates the side key.
					 * 
					 * Weird, in the userguide left and right are opposite.
					 */
					final Integer encodedInteger = characteristic.getIntValue(FORMAT_UINT8, 0);

					String msg = "Unknown";
					switch (encodedInteger) {
					case 0:
						msg = "OFF_OFF";
						break;
					case 1:
						msg = "OFF_ON";
						break;
					case 2:
						msg = "ON_OFF";
						break;
					case 3:
						msg = "ON_ON";
						break;
					}

					Log.v(LOG_TAG, "BluetoothGattCallback :: onCharacteristicRead() : " + msg + " Switch Pressed");
					final String tmpMsg = msg;
					// update display
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							sensorValsfragment.updateKeys(tmpMsg);
						}
					});
				}
			} catch (final Exception e) {
				Log.e(LOG_TAG, "BluetoothGattCallback :: onCharacteristicChanged()", e);
			}
			// writeQueue.issue();
		};

		@Override
		// Result of a characteristic read operation
		public void onCharacteristicRead(final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic,
				final int status) {
			Log.v(LOG_TAG,
					"BluetoothGattCallback :: onCharacteristicRead() [UUID:"
							+ SampleGattAttributes.lookup(characteristic.getUuid().toString(), characteristic.getUuid()
									.toString()) + "]");
			try {
				if (status == BluetoothGatt.GATT_SUCCESS) {
					if (characteristic.getUuid().equals(UUID.fromString(TiBleConstants.IRTEMPERATURE_DATA_UUID))) {
						Log.v(LOG_TAG, "BluetoothGattCallback :: onCharacteristicRead() : IRTEMPERATURE_DATA_UUID");

						// Ambient temp is offset 2
						final int offset = 2;
						final double value = MathUtils.shortUnsignedAtOffset(characteristic, offset) / 128.0;
						Log.v(LOG_TAG, "BluetoothGattCallback :: onCharacteristicRead() : Temperature Data : " + value);
						// update temp display
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								sensorValsfragment.updateTemperature(value + "°C");
							}
						});
					} else if (characteristic.getUuid().equals(UUID.fromString(TiBleConstants.ACCELEROMETER_DATA_UUID))) {
						Log.v(LOG_TAG, "BluetoothGattCallback :: onCharacteristicRead() : ACCELEROMETER_DATA_UUID");
					} else if (characteristic.getUuid().equals(UUID.fromString(TiBleConstants.BAROMETER_DATA_UUID))) {
						Log.v(LOG_TAG, "BluetoothGattCallback :: onCharacteristicRead() : BAROMETER_DATA_UUID");
					} else if (characteristic.getUuid().equals(UUID.fromString(TiBleConstants.GYROSCOPE_DATA_UUID))) {
						Log.v(LOG_TAG, "BluetoothGattCallback :: onCharacteristicRead() : GYROSCOPE_DATA_UUID");

						// NB: x,y,z has a weird order.
						final float y = MathUtils.shortSignedAtOffset(characteristic, 0) * (500f / 65536f) * -1;
						final float x = MathUtils.shortSignedAtOffset(characteristic, 2) * (500f / 65536f);
						final float z = MathUtils.shortSignedAtOffset(characteristic, 4) * (500f / 65536f);

						final DecimalFormat decimal = new DecimalFormat("+0.00;-0.00");
						final String msg = "X: " + decimal.format(x) + "�/s" + "\nY: " + decimal.format(y) + "�/s"
								+ "\nZ: " + decimal.format(z) + "�/s";

						Log.v(LOG_TAG, "BluetoothGattCallback :: onCharacteristicRead() : Gyroscope Data [" + msg + "]");

					} else if (characteristic.getUuid().equals(UUID.fromString(TiBleConstants.HUMIDITY_DATA_UUID))) {
						Log.v(LOG_TAG, "BluetoothGattCallback :: onCharacteristicRead() : HUMIDITY_DATA_UUID");
					} else if (characteristic.getUuid().equals(UUID.fromString(TiBleConstants.MAGNETOMETER_DATA_UUID))) {
						Log.v(LOG_TAG, "BluetoothGattCallback :: onCharacteristicRead() : MAGNETOMETER_DATA_UUID");
					} else if (characteristic.getUuid().equals(
							UUID.fromString(TiBleConstants.SIMPLE_KEYS_KEYPRESSED_UUID))) {
						Log.v(LOG_TAG, "BluetoothGattCallback :: onCharacteristicRead() : SIMPLE_KEYS_KEYPRESSED_UUID");
					}
				}
			} catch (final Exception e) {
				Log.e(LOG_TAG, "BluetoothGattCallback :: onCharacteristicRead()", e);
			}
			writeQueue.issue();
		}

		@Override
		public void onCharacteristicWrite(final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic,
				final int status) {
			try {
				Log.v(LOG_TAG,
						"BluetoothGattCallback :: onCharacteristicWrite() [status:"
								+ status
								+ "] "
								+ BluetoothGattUtils.decodeReturnCode(status)
								+ " [UUID:"
								+ SampleGattAttributes.lookup(characteristic.getUuid().toString(), characteristic
										.getUuid().toString()) + "]");
				super.onCharacteristicWrite(gatt, characteristic, status);

				if (characteristic.getUuid().equals(UUID.fromString(TiBleConstants.IRTEMPERATURE_CONF_UUID))) {
					Log.v(LOG_TAG, "BluetoothGattCallback :: onCharacteristicWrite() trying to read temperature ");
					final boolean tempValRead = sensorTemperatureRead(
							gatt.getService(UUID.fromString(TiBleConstants.IRTEMPERATURE_SERV_UUID)), characteristic);
					Log.v(LOG_TAG, "BluetoothGattCallback :: onCharacteristicWrite() trying to read temperature "
							+ (tempValRead ? "worked" : "failed"));

				} else if (characteristic.getUuid().equals(UUID.fromString(TiBleConstants.GYROSCOPE_CONF_UUID))) {
					Log.v(LOG_TAG, "BluetoothGattCallback :: onCharacteristicWrite() trying to read gyroscope ");
					final boolean tempValRead = sensorGyroscopeRead(
							gatt.getService(UUID.fromString(TiBleConstants.GYROSCOPE_SERV_UUID)), characteristic);
					Log.v(LOG_TAG, "BluetoothGattCallback :: onCharacteristicWrite() trying to read gyroscope "
							+ (tempValRead ? "worked" : "failed"));

				}

			} catch (final Exception e) {
				Log.e(LOG_TAG, "BluetoothGattCallback :: onCharacteristicWrite()", e);
			}
			writeQueue.issue();
		}

		@Override
		public void onConnectionStateChange(final BluetoothGatt gatt, final int status, final int newState) {
			Log.v(LOG_TAG, "BluetoothGattCallback :: onConnectionStateChange()");
			try {
				if (newState == BluetoothProfile.STATE_CONNECTED) {
					mConnectionState = STATE_CONNECTED;
					mDeviceConnected = true;
					if (mBluetoothGattST != gatt) {
						Log.v(LOG_TAG, "BluetoothGattCallback :: updating GATT object");
						mBluetoothGattST = gatt;
						mBtDeviceST = mBluetoothGattST.getDevice();
					}
					Log.v(LOG_TAG, "BluetoothGattCallback :: Connected to GATT server on device ["
							+ gatt.getDevice().getName() + "]");
					Log.v(LOG_TAG, "BluetoothGattCallback :: Attempting to start service discovery: "
							+ mBluetoothGattST.discoverServices());
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							main_txt_device_connected.setText("Connected");
						}
					});

				} else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
					mConnectionState = STATE_DISCONNECTED;
					mDeviceConnected = false;
					mDeviceFound = false;
					Log.v(LOG_TAG, "BluetoothGattCallback :: Disconnected from GATT server");
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							main_txt_device_connected.setText("Disconnected");
						}
					});
					scanLeDevice(true);
				}
			} catch (final Exception e) {
				Log.e(LOG_TAG, "", e);
			}
		}

		@Override
		public void onDescriptorRead(final BluetoothGatt gatt, final BluetoothGattDescriptor descriptor,
				final int status) {
			Log.v(LOG_TAG, "BluetoothGattCallback :: onDescriptorRead()");
			try {
				super.onDescriptorRead(gatt, descriptor, status);
			} catch (final Exception e) {
				Log.e(LOG_TAG, "", e);
			}
			writeQueue.issue();
		}

		@Override
		public void onDescriptorWrite(final BluetoothGatt gatt, final BluetoothGattDescriptor descriptor,
				final int status) {
			Log.v(LOG_TAG, "BluetoothGattCallback :: onDescriptorWrite()");
			try {
				super.onDescriptorWrite(gatt, descriptor, status);
			} catch (final Exception e) {
				Log.e(LOG_TAG, "", e);
			}
			writeQueue.issue();
		}

		@Override
		// New services discovered
		public void onServicesDiscovered(final BluetoothGatt gatt, final int status) {
			Log.v(LOG_TAG, "BluetoothGattCallback :: onServicesDiscovered()");
			try {
				if (status == BluetoothGatt.GATT_SUCCESS) {
					mServiceList = mBluetoothGattST.getServices();
					parseGattServices(mServiceList);
				} else {
					Log.w(LOG_TAG, "BluetoothGattCallback :: onServicesDiscovered received: " + status);
				}
			} catch (final Exception e) {
				Log.e(LOG_TAG, "", e);
			}
		}
	};
	// Device scan callback.
	private final BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
		@Override
		public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
			Log.v(LOG_TAG, "BluetoothGattCallback::onLeScan()");
			mBtDeviceST = device;
			Log.v(LOG_TAG, "BluetoothGattCallback::onLeScan() Found device [" + mBtDeviceST.getName() + "]");
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					main_txt_device_name.setText(mBtDeviceST.getName());
					main_txt_device_address.setText(mBtDeviceST.getAddress());
					main_txt_device_connected.setText("Disconnected");
				}
			});
			mDeviceFound = true;

			if (mBtDeviceST.getName().equalsIgnoreCase("SensorTag")) {
				scanLeDevice(false);
				Log.v(LOG_TAG,
						"BluetoothGattCallback::onLeScan() attempting to connect to device [" + mBtDeviceST.getName()
								+ "]");
				mBtDeviceST = device;
				mBluetoothGattST = device.connectGatt(getApplication(), false, mGattCallback);
			} else if (mBtDeviceST.getName().equalsIgnoreCase("HRM")) {
				Log.v(LOG_TAG,
						"BluetoothGattCallback::onLeScan() attempting to connect to device [" + mBtDeviceST.getName()
								+ "]");
				mBtDeviceHrm = device;
				mBluetoothGattHrm = device.connectGatt(getApplication(), false, mGattCallbackHrm);

			}
		}
	};

	private final String LIST_NAME = "NAME";
	private final String LIST_UUID = "UUID";
	private TextView main_txt_device_name;
	private TextView main_txt_device_connected;
	private TextView main_txt_device_address;
	private final SensorValuesSectionFragment sensorValsfragment = new SensorValuesSectionFragment();
	private final HashMap<String, String> currentServiceData = new HashMap<String, String>();

	private void checkBluetoothAvailable() {
		Log.v(LOG_TAG, "checkBluetoothAvailable()");
		// Use this check to determine whether BLE is supported on the device.
		// Then you can selectively disable BLE-related features.
		if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
			Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
			finish();
		}

		// Initialises a Bluetooth adapter. For API level 18 and above, get a
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

	private BluetoothGattCharacteristic getCharacteristic(final BluetoothGattService gattService, final UUID charUuid) {
		Log.v(LOG_TAG, "getCharacteristic()");
		final List<BluetoothGattCharacteristic> tmpCharList = gattService.getCharacteristics();
		for (final BluetoothGattCharacteristic bleChar : tmpCharList) {
			if (bleChar.getUuid().equals(charUuid)) { return bleChar; }
		}
		Log.v(LOG_TAG, "getCharacteristic() [" + charUuid.toString() + "] not found ");
		return null;
	}

	@Override
	protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
		Log.v(LOG_TAG, "onActivityResult()");
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
	protected void onCreate(final Bundle savedInstanceState) {
		Log.v(LOG_TAG, "onCreate()");
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
		main_txt_device_connected = (TextView) findViewById(R.id.main_txt_device_connected);

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

	@Override
	protected void onStart() {
		super.onStart();
	}

	private void parseGattServices(final List<BluetoothGattService> gattServices) {
		Log.v(LOG_TAG, "parseGattServices()");
		if (gattServices == null) { return; }
		String uuid = null;
		final String unknownCharaString = "unknown_characteristic";

		// Loops through available GATT Services.
		for (final BluetoothGattService gattService : gattServices) {
			uuid = gattService.getUuid().toString();

			if (uuid.equalsIgnoreCase(TiBleConstants.IRTEMPERATURE_DATA_UUID)) {
				Log.v(LOG_TAG, "parseGattServices() got IRTEMPERATURE_DATA_UUID");
				// sensorTemperatureRead(gattService);

			} else if (uuid.equalsIgnoreCase(TiBleConstants.IRTEMPERATURE_CONF_UUID)) {
				Log.v(LOG_TAG, "parseGattServices() got IRTEMPERATURE_CONF_UUID");
				// sensorTemperatureEnable(gattService);

			} else if (uuid.equalsIgnoreCase(TiBleConstants.IRTEMPERATURE_SERV_UUID)) {
				Log.v(LOG_TAG, "parseGattServices() got IRTEMPERATURE_SERV_UUID");

				final List<BluetoothGattCharacteristic> gattCharacteristicData = gattService.getCharacteristics();

				for (final BluetoothGattCharacteristic bgc : gattCharacteristicData) {
					if (bgc.getUuid().equals(UUID.fromString(TiBleConstants.IRTEMPERATURE_CONF_UUID))) {
						sensorTemperatureEnable(gattService, bgc);
						sensorTemperatureChangeNotification(gattService, bgc, true);
					}
				}

			} else if (uuid.equalsIgnoreCase(TiBleConstants.ACCELEROMETER_SERV_UUID)) {
				Log.v(LOG_TAG, "parseGattServices() got ACCELEROMETER_SERV_UUID");
				// get accelerometer data
				final List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
				for (final BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
					Log.v(LOG_TAG,
							"accel characteristic ["
									+ SampleGattAttributes.lookup(gattCharacteristic.getUuid().toString(),
											unknownCharaString) + "]");
				}
			} else if (uuid.equalsIgnoreCase(TiBleConstants.BAROMETER_SERV_UUID)) {
				Log.v(LOG_TAG, "parseGattServices() got BAROMETER_SERV_UUID");
			} else if (uuid.equalsIgnoreCase(TiBleConstants.GYROSCOPE_SERV_UUID)) {
				Log.v(LOG_TAG, "parseGattServices() got GYROSCOPE_SERV_UUID");

				final List<BluetoothGattCharacteristic> gattCharacteristicData = gattService.getCharacteristics();

				for (final BluetoothGattCharacteristic bgc : gattCharacteristicData) {
					if (bgc.getUuid().equals(UUID.fromString(TiBleConstants.GYROSCOPE_CONF_UUID))) {
						sensorGyroscopeEnable(gattService, bgc);
						sensorGyroscopeChangeNotification(gattService, bgc, true);
					}
				}

			} else if (uuid.equalsIgnoreCase(TiBleConstants.HUMIDITY_SERV_UUID)) {
				Log.v(LOG_TAG, "parseGattServices() got HUMIDITY_SERV_UUID");
			} else if (uuid.equalsIgnoreCase(TiBleConstants.MAGNETOMETER_SERV_UUID)) {
				Log.v(LOG_TAG, "parseGattServices() got MAGNETOMETER_SERV_UUID");
			} else if (uuid.equalsIgnoreCase(TiBleConstants.SIMPLE_KEYS_SERV_UUID)) {
				Log.v(LOG_TAG, "parseGattServices() got SIMPLE_KEYS_SERV_UUID");

				final List<BluetoothGattCharacteristic> gattCharacteristicData = gattService.getCharacteristics();

				for (final BluetoothGattCharacteristic bgc : gattCharacteristicData) {
					if (bgc.getUuid().equals(UUID.fromString(TiBleConstants.SIMPLE_KEYS_DATA_UUID))) {
						sensorSimpleKeysChangeNotification(gattService, bgc, true);
					}
				}
			}

		}
	}

	private void parseGattServicesHrm(final List<BluetoothGattService> aServiceListHrm) {
		Log.v(LOG_TAG, "parseGattServicesHrm()");
		if (aServiceListHrm == null) { return; }
		String uuid = null;

		// Loops through available GATT Services.
		for (final BluetoothGattService gattService : aServiceListHrm) {
			uuid = gattService.getUuid().toString();

			if (uuid.equalsIgnoreCase(BleServices.HEART_RATE)) {
				Log.v(LOG_TAG, "parseGattServicesHrm() got BleServices.HEART_RATE");

				final List<BluetoothGattCharacteristic> gattCharacteristicData = gattService.getCharacteristics();
				for (final BluetoothGattCharacteristic bgc : gattCharacteristicData) {

				}
			}
		}
	}

	private void scanLeDevice(final boolean enable) {
		Log.v(LOG_TAG, "scanLeDevice(" + (enable ? "true" : "false") + ")");
		if (enable) {
			// Stops scanning after a pre-defined scan period.
			mHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					if (mScanning) {
						Log.v(LOG_TAG, "scanLeDevice() stop scanning after pre-defined period");
						mScanning = false;
						mBluetoothAdapter.stopLeScan(mLeScanCallback);
						main_txt_device_name.setText("Failed to find device");
					}
				}
			}, SCAN_PERIOD);

			Log.v(LOG_TAG, "scanLeDevice() start scanning");
			mScanning = true;
			mBluetoothAdapter.startLeScan(mLeScanCallback);
		} else {
			Log.v(LOG_TAG, "scanLeDevice() stop scanning");
			mScanning = false;
			mBluetoothAdapter.stopLeScan(mLeScanCallback);
		}
	}

	private void sensorGyroscopeChangeNotification(final BluetoothGattService aGattService,
			final BluetoothGattCharacteristic bgc, final boolean enable) {
		Log.v(LOG_TAG, "sensorGyroscopeChangeNotification() queueing");
		final SensorTagWriteRunnable rnble = new SensorTagWriteRunnable() {
			@Override
			public void run() {
				Log.v(LOG_TAG,
						"Queue:GyroChgNotif:sensorGyroscopeChangeNotification() enabling notifications for GYROSCOPE_DATA_UUID ");

				final BluetoothGattCharacteristic dataCharacteristic = aGattService.getCharacteristic(UUID
						.fromString(TiBleConstants.GYROSCOPE_DATA_UUID));

				boolean success = mBluetoothGattST.setCharacteristicNotification(dataCharacteristic, true);
				if (!success) {
					Log.v(LOG_TAG, "Queue:GyroChgNotif:Failed to set the notification status.");
				} else {
					Log.v(LOG_TAG, "Queue:GyroChgNotif:The notification status was changed.");

					final BluetoothGattDescriptor config = dataCharacteristic.getDescriptor(UUID
							.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
					if (config == null) {
						Log.v(LOG_TAG, "Queue:GyroChgNotif:Unable to get config descriptor.");
					} else {
						final byte[] configValue = enable ? ENABLE_NOTIFICATION_VALUE : DISABLE_NOTIFICATION_VALUE;
						success = config.setValue(configValue);
						if (!success) {
							Log.v(LOG_TAG, "Queue:GyroChgNotif:Could not locally store value.");
						}

						success = mBluetoothGattST.writeDescriptor(config);
						if (!success) {
							Log.v(LOG_TAG, "Queue:GyroChgNotif:Initiated a write to descriptor.");
						} else {
							Log.v(LOG_TAG, "Queue:GyroChgNotif:Unable to initiate write.");
						}
					}
				}
			}
		};
		rnble.name = "Enable Gyroscope Sensor Notifications";
		writeQueue.queueRunnable(rnble);
	}

	private void sensorGyroscopeEnable(final BluetoothGattService aGattService, final BluetoothGattCharacteristic bgc) {
		Log.v(LOG_TAG, "sensorGyroscopeEnable() queueing");
		final SensorTagWriteRunnable rnble = new SensorTagWriteRunnable() {
			@Override
			public void run() {
				Log.v(LOG_TAG, "Queue:sensorGyroscopeEnable() writing 7 to Gyroscope_CONF_UUID ");
				final byte[] data = new byte[] { 7 };
				bgc.setValue(data);
				final boolean success = mBluetoothGattST.writeCharacteristic(bgc);
			}
		};
		rnble.name = "Enable Gyroscope Sensor";
		writeQueue.queueRunnable(rnble);
	}

	private boolean sensorGyroscopeRead(final BluetoothGattService aGattService, final BluetoothGattCharacteristic bgc) {
		Log.v(LOG_TAG, "sensorGyroscopeRead() reading Gyroscope_DATA_UUID");
		final List<BluetoothGattCharacteristic> gattCharacteristics = aGattService.getCharacteristics();
		for (final BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
			if (gattCharacteristic.getUuid().equals(UUID.fromString(TiBleConstants.GYROSCOPE_DATA_UUID))) { return mBluetoothGattST
					.readCharacteristic(gattCharacteristic); }
		}
		return false;
	}

	private void sensorSimpleKeysChangeNotification(final BluetoothGattService aGattService,
			final BluetoothGattCharacteristic bgc, final boolean enable) {
		Log.v(LOG_TAG, "sensorTemperatureChangeNotification() queueing");
		final SensorTagWriteRunnable rnble = new SensorTagWriteRunnable() {
			@Override
			public void run() {
				Log.v(LOG_TAG,
						"Queue:KeyPrsdNotif:Service : "
								+ SampleGattAttributes.lookup(aGattService.getUuid().toString(), "keys service??"));
				Log.v(LOG_TAG,
						"Queue:KeyPrsdNotif:bgc     : "
								+ SampleGattAttributes.lookup(bgc.getUuid().toString(), "keys char??"));
				Log.v(LOG_TAG, "Queue:KeyPrsdNotif: enabling notifications for SIMPLE_KEYS_DATA_UUID ");

				final BluetoothGattCharacteristic dataCharacteristic = aGattService.getCharacteristic(UUID
						.fromString(TiBleConstants.SIMPLE_KEYS_DATA_UUID));

				boolean success = mBluetoothGattST.setCharacteristicNotification(dataCharacteristic, true);
				if (success) {
					Log.v(LOG_TAG, "Queue:KeyPrsdNotif:The notification status was changed.");
				} else {
					Log.v(LOG_TAG, "Queue:KeyPrsdNotif:Failed to set the notification status.");
				}

				final BluetoothGattDescriptor config = dataCharacteristic.getDescriptor(UUID
						.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
				if (config == null) {
					Log.v(LOG_TAG, "Queue:KeyPrsdNotif:Unable to get config descriptor.");
					return;
				}

				final byte[] configValue = enable ? ENABLE_NOTIFICATION_VALUE : DISABLE_NOTIFICATION_VALUE;
				success = config.setValue(configValue);
				if (!success) {
					Log.v(LOG_TAG, "Queue:KeyPrsdNotif:Could not locally store value.");
				}

				success = mBluetoothGattST.writeDescriptor(config);
				if (success) {
					Log.v(LOG_TAG, "Queue:KeyPrsdNotif:Initiated a write to descriptor.");
				} else {
					Log.v(LOG_TAG, "Queue:KeyPrsdNotif:Unable to initiate write.");
				}
			}
		};
		rnble.name = "Enable Keypress Notifications";
		writeQueue.queueRunnable(rnble);
	}

	private void sensorTemperatureChangeNotification(final BluetoothGattService aGattService,
			final BluetoothGattCharacteristic bgc, final boolean enable) {
		Log.v(LOG_TAG, "sensorTemperatureChangeNotification() queueing");
		final SensorTagWriteRunnable rnble = new SensorTagWriteRunnable() {
			@Override
			public void run() {
				Log.v(LOG_TAG,
						"Queue:TempChgNotif:Service : "
								+ SampleGattAttributes.lookup(aGattService.getUuid().toString(), "temp service??"));
				Log.v(LOG_TAG,
						"Queue:TempChgNotif:bgc     : "
								+ SampleGattAttributes.lookup(bgc.getUuid().toString(), "temp char??"));
				Log.v(LOG_TAG, "Queue:TempChgNotif: enabling notifications for IRTEMPERATURE_DATA_UUID ");

				final BluetoothGattCharacteristic dataCharacteristic = aGattService.getCharacteristic(UUID
						.fromString(TiBleConstants.IRTEMPERATURE_DATA_UUID));

				boolean success = mBluetoothGattST.setCharacteristicNotification(dataCharacteristic, true);
				if (success) {
					Log.v(LOG_TAG, "Queue:TempChgNotif:The notification status was changed.");
				} else {
					Log.v(LOG_TAG, "Queue:TempChgNotif:Failed to set the notification status.");
				}

				final BluetoothGattDescriptor config = dataCharacteristic.getDescriptor(UUID
						.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
				if (config == null) {
					Log.v(LOG_TAG, "Queue:TempChgNotif:Unable to get config descriptor.");
					return;
				}

				final byte[] configValue = enable ? ENABLE_NOTIFICATION_VALUE : DISABLE_NOTIFICATION_VALUE;
				success = config.setValue(configValue);
				if (!success) {
					Log.v(LOG_TAG, "Queue:TempChgNotif:Could not locally store value.");
				}

				success = mBluetoothGattST.writeDescriptor(config);
				if (success) {
					Log.v(LOG_TAG, "Queue:TempChgNotif:Initiated a write to descriptor.");
				} else {
					Log.v(LOG_TAG, "Queue:TempChgNotif:Unable to initiate write.");
				}
			}
		};
		rnble.name = "Enable Temp Sensor Notifications";
		writeQueue.queueRunnable(rnble);
	}

	private void sensorTemperatureEnable(final BluetoothGattService aGattService, final BluetoothGattCharacteristic bgc) {
		Log.v(LOG_TAG, "sensorTemperatureEnable() queueing");
		final SensorTagWriteRunnable rnble = new SensorTagWriteRunnable() {
			@Override
			public void run() {
				Log.v(LOG_TAG, "Queue:sensorTemperatureEnable() writing 1 to IRTEMPERATURE_CONF_UUID ");
				final byte[] data = new byte[] { 1 };
				bgc.setValue(data);
				final boolean success = mBluetoothGattST.writeCharacteristic(bgc);
			}
		};
		rnble.name = "Enable Temp Sensor";
		writeQueue.queueRunnable(rnble);
	}

	private boolean sensorTemperatureRead(final BluetoothGattService aGattService, final BluetoothGattCharacteristic bgc) {
		Log.v(LOG_TAG, "sensorTemperatureRead() reading IRTEMPERATURE_DATA_UUID");
		final List<BluetoothGattCharacteristic> gattCharacteristics = aGattService.getCharacteristics();
		for (final BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
			if (gattCharacteristic.getUuid().equals(UUID.fromString(TiBleConstants.IRTEMPERATURE_DATA_UUID))) { return mBluetoothGattST
					.readCharacteristic(gattCharacteristic); }
		}

		return false;
	}
}
