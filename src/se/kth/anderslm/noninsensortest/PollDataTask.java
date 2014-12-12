package se.kth.anderslm.noninsensortest;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.AsyncTask;

class PollDataTask extends AsyncTask<Void, Void, String> {

	protected PollDataTask(MainActivity activity, BluetoothDevice noninDevice) {
		this.activity = activity;
		this.noninDevice = noninDevice;
		this.adapter = BluetoothAdapter.getDefaultAdapter();
	}

	/**
	 * A simple example: poll one frame of data from the Nonin sensor
	 */
	@Override
	protected String doInBackground(Void... v) {
		String output = "";
		//System.out.println("Do in background"); 
		if(this.isCancelled()){
			System.out.println("Cancellelleldededdd");
			
			
		}else{
			// an ongoing discovery will slow down the connection
			adapter.cancelDiscovery();

			BluetoothSocket socket = null;
			try {

				//System.out.println("Debug0");
				socket = noninDevice
						.createRfcommSocketToServiceRecord(STANDARD_SPP_UUID);

				//System.out.println("Debug0.5");
				socket.connect();
				
				
				//System.out.println("Debug1");
				
				InputStream is = socket.getInputStream();
				OutputStream os = socket.getOutputStream();

				os.write(FORMAT);
				os.flush();
				byte[] reply = new byte[1]; 
				is.read(reply);

				//System.out.println("Debug2");
				if (reply[0] == ACK) {
					byte[] frame = new byte[4]; // this -obsolete- format specifies
												// 4 bytes per frame
					
					is.read(frame);
					
					int value1 = unsignedByteToInt(frame[1]);
					int value2 = unsignedByteToInt(frame[2]);
					if(isBitSet(frame[0],0)){
						value1=value1+127;
					}
					
					output = value1 + "," + value2;
				}
			} catch (Exception e) {
				output = "Error1"+e.getMessage();
			} finally {
				try {
					if (socket != null)
						socket.close();
				} catch (Exception e) {
				}
			}
		}
		return output;
	}

	/**
	 * update the UI (executed on the main thread)
	 */
	@Override
	protected void onPostExecute(String output) {
		activity.displayData(output);
	}

	// The byte sequence to set sensor to a basic, and obsolete, format
	//private static final byte[] FORMAT = { 0x44, 0x31 };
	private static final byte[] FORMAT = { 0x02,0x70,0x04,0x02,0x08,0x00,(byte)0x7E,0x03 };
	//private static final byte[] FORMAT = { 0x02,0x70,0x04,0x02,0x0D,0x00,(byte)0x7E,0x03 };
	private static final byte ACK = 0x06; // ACK from Nonin sensor

	private static final UUID STANDARD_SPP_UUID = UUID
			.fromString("00001101-0000-1000-8000-00805F9B34FB");

	private MainActivity activity;
	private BluetoothDevice noninDevice;
	private BluetoothAdapter adapter;

	// NB! Java does not support unsigned types
	private int unsignedByteToInt(byte b) {
		//b=(byte) 130;
		System.out.println("Byte values: " +Integer.toBinaryString((int) b));
		System.out.println("Byte ints " +b);
		
		return (int) b & 0xFF;
		
	}
	
	public byte getBit(byte pulseVal, int position){
		return (byte) ((pulseVal >> position) & 1);
	}
	
	public boolean isBitSet(byte pulseVal, int pos){
		return (pulseVal & (1 << pos)) != 0;
	}

}
