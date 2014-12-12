package se.kth.anderslm.noninsensortest;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Set;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	public static final int REQUEST_ENABLE_BT = 42;
	protected boolean isStopped=false;
	protected PollDataTask getData=null;
	//Connect values
	protected Socket requestSocket;
	protected ObjectOutputStream out;
	protected ObjectInputStream in;
	protected String message;
	protected ArrayList<String> file;
	protected File noninValues;
	protected BufferedInputStream get;
	protected FileOutputStream fs;
	
	protected String filename = "NONIN_DATA.txt";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		dataView = (TextView) findViewById(R.id.dataView);
		file = new ArrayList<String>();
		noninValues = new File(this.getFilesDir(),filename);

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
		
		if(isStopped){
			isStopped = false;
			getData.cancel(true);
			
			
		   //old connectionposition
			receiveconnection rc = new receiveconnection();
			rc.execute("");
			
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
	
	private void save(String data){
		try {
			FileWriter fileWriter = new FileWriter(noninValues);
			fileWriter.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	protected void displayData(CharSequence data) {
		
		System.out.println("Data: "+"'"+(String) data+"'");
		
		//System.out.println("Data: "+"'"+((String) data).split(",")[0]+"'");
		int pulseVal=-1;
		int oxyVal=0;
		try{
			pulseVal = Integer.parseInt(((String) data).split(",")[0]);
			oxyVal = Integer.parseInt(((String) data).split(",")[1]);
		}catch(Exception e){
			System.out.println("Error Parse: "+e);
			System.out.println("Skipped add to file");
		}
		
		if((250>pulseVal&&pulseVal>0)&&(100>oxyVal&&oxyVal>=0)){
			file.add((String) data);
			//System.out.println("Added to file");
		}
		
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
	
	

	
	private class receiveconnection extends AsyncTask<String, Void, Void>{

		@Override
		protected Void doInBackground(String... urls) {
			 try{
			    	System.out.println("Trying to connect...");
			    	//Connect initiate
			    	//Log.d("Lab3b", "Trying to connect...");
				    requestSocket = new Socket("193.10.39.200", 6668);
				   // Log.d("Lab3b", "Connected");
		            
		            
		            get = new BufferedInputStream(requestSocket.getInputStream());
		            System.out.println("Connected to localhost in port 6668");
		            fs = new FileOutputStream(noninValues);
			    }catch(Exception e){
			    	e.printStackTrace();
			    }
			 
			 System.out.println("Writing to local file...");
				String appender ="";
				for (String tempArray: file) {
					System.out.println("Debuger tempArray: "+tempArray);
					appender+=(tempArray+"/n");
				}
				System.out.println("saving locally...");
				save(appender);
				System.out.println("Local save complete!");
				
				try{
					int u;
					byte buf[] = new byte[1024];
					while((u = get.read(buf,0,1024))!=-1){
						fs.write(buf,0,u);
					}
					
				}catch(Exception e){
					System.out.println("Opala"+e);
				}
			    
			return null;
			
		}
		
		@Override
		protected void onPostExecute(Void v) {
			try {
				fs.flush();
				
				fs.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
	    }
	}
}
