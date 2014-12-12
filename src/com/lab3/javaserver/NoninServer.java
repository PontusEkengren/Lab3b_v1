package com.lab3.javaserver;
import java.io.*;
import java.net.*;

public class NoninServer {

	public static void main(String[] args) throws Exception {
		
		ServerSocket server = null;
		boolean running = true;
		
		try {
			server = new ServerSocket(6667);
			System.out.println("Server started");
			
			while(running) {
				Socket socket = null; 
				PrintWriter writer = null;
				BufferedReader reader = null;
				int bytesRead;
				int current = 0;
				FileOutputStream fos = null;
				BufferedOutputStream bos = null;
				
				try {
					socket = server.accept();
					System.out.println(socket.getInetAddress());
					
					byte[] fileByteLength = new byte[6022386];
					InputStream is = socket.getInputStream();
					fos = new FileOutputStream("nonin.txt");
					bos = new BufferedOutputStream(fos);
					bytesRead = is.read(fileByteLength,0,fileByteLength.length);
					current = bytesRead;
					System.out.println("Starting download");
					do{
						bytesRead = is.read(fileByteLength,current, (fileByteLength.length-current));
						if(bytesRead >= 0){
							current+=bytesRead;
						}
						System.out.println("Downloading...");
					}while(current < (int)fileByteLength.length);
					System.out.println("Done shizzle");
					bos.write(fileByteLength,0,current);
					bos.flush();
					
					System.out.println("File downloaded ("+current+") bytes read");

				} catch(Exception e) {
					e.printStackTrace();
				}
				finally {
					if (fos != null) fos.close();
					if (bos != null) bos.close();
					socket.close();
					System.out.println("Client socket closed");
				}
			}
		}finally {
			server.close();
			System.out.println("Server stopped");
		}
	}

}
