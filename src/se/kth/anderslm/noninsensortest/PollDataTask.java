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
		if(this.isCancelled()){
			System.out.println("ENDEEDEDE");
		}else{
			// an ongoing discovery will slow down the connection
			adapter.cancelDiscovery();

			BluetoothSocket socket = null;
			try {
				socket = noninDevice
						.createRfcommSocketToServiceRecord(STANDARD_SPP_UUID);
				socket.connect();

				InputStream is = socket.getInputStream();
				OutputStream os = socket.getOutputStream();

				os.write(FORMAT);
				os.flush();
				byte[] reply = new byte[1];
				is.read(reply);

				if (reply[0] == ACK) {
					byte[] frame = new byte[4]; // this -obsolete- format specifies
												// 4 bytes per frame
					
					is.read(frame);
					//frame[1]=(byte)130;
					int value1 = unsignedByteToInt(frame[1]);
					int value2 = unsignedByteToInt(frame[2]);
					System.out.println("Plus: "+unsignedByteToInt(frame[1])+"Puls hex: "+frame[1]+" O2 hex: "+frame[2]+" something"+unsignedByteToInt(frame[3])+"something2"+unsignedByteToInt(frame[0]));
					output = value1 + "; " + value2 + "\r\n";
				}
			} catch (Exception e) {
				output = e.getMessage();
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
		return (int) b & 0xFF;
	}
}
