package se.kth.anderslm.noninsensortest;

import java.util.Set;

import android.os.Bundle;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.support.v4.content.AsyncTaskLoader;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	public static final int REQUEST_ENABLE_BT = 42;
	private boolean isStopped=false;
	private PollDataTask getData=null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		dataView = (TextView) findViewById(R.id.dataView);

		bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (bluetoothAdapter == null) {
			showToast("This device do not support Bluetooth");
			this.finish();
		}
		
		//System.out.println(getBit(7));
	}
	

	@Override
	protected void onStart() {
		super.onStart();
		dataView.setText(R.string.data);
		initBluetooth();
	}

	@Override
	protected void onStop() {
		super.onStop();
		// TODO: stop ongoing BT communication
	}

	public void onPollButtonClicked(View view) {
//
		if(isStopped){
			isStopped = false;
			getData.cancel(true);
		}else{
			isStopped=true;
			launchTask();
		}
		
		
	}
	
	public void launchTask(){

		if (noninDevice != null) {
			getData =new PollDataTask(this, noninDevice);
			getData.execute();
		} else {
			showToast("No Nonin sensor found");
		}
	
	}
	

	protected void displayData(CharSequence data) {
		dataView.setText(data);
		
		launchTask();
	}

	private void initBluetooth() {
		if (!bluetoothAdapter.isEnabled()) {
			Intent enableBtIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		} else {
			getNoninDevice();
		}
	}

	// callback for BluetoothAdapter.ACTION_REQUEST_ENABLE (called via
	// initBluetooth)
	protected void onActivityResult(int requestCode, int resultCode,
			Intent result) {
		super.onActivityResult(requestCode, resultCode, result);

		if (requestCode == REQUEST_ENABLE_BT) {
			if (bluetoothAdapter.isEnabled()) {
				getNoninDevice();
			} else {
				showToast("Bluetooth is turned off.");
			}
		}
	}

	private void getNoninDevice() {
		noninDevice = null;
		Set<BluetoothDevice> pairedBTDevices = bluetoothAdapter
				.getBondedDevices();
		if (pairedBTDevices.size() > 0) {
			// the last Nonin device, if any, will be selected...
			for (BluetoothDevice device : pairedBTDevices) {
				String name = device.getName();
				if (name.contains("Nonin")) {
					noninDevice = device;
					showToast("Paired device: " + name);
					return;
				}
			}
		}
		if (noninDevice == null) {
			showToast("No paired Nonin devices found!\r\n"
					+ "Please pair a Nonin BT device with this device.");
		}
	}

	private BluetoothAdapter bluetoothAdapter = null;
	private BluetoothDevice noninDevice = null;

	private TextView dataView;

	void showToast(final CharSequence msg) {
		Toast toast = Toast.makeText(this, msg, Toast.LENGTH_LONG);
		toast.show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
}
