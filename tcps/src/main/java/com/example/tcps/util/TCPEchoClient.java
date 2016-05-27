package com.example.tcps.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

public class TCPEchoClient {

	/**
	 * @param args
	 */
	public void TcpSendData(String IPAddress,int port,String sendData) {

		byte[] data = sendData.getBytes();
		
		// create socket that is connected to server on specified port
		Socket socket = null;
		try {
			socket = new Socket(IPAddress, port);
			
			System.out.println("Connected to server ... sending echo string");

			InputStream in = socket.getInputStream();
			OutputStream out = socket.getOutputStream();
			out.write(data);

			// receive
			int totalBytesRcvd = 0;// total bytes received so far
			int bytesRcvd;// Bytes received in last read

			while (totalBytesRcvd < data.length) {
				bytesRcvd = in.read(data, totalBytesRcvd, data.length
						- totalBytesRcvd);
				if (bytesRcvd == -1) {
					throw new SocketException("Connection closed prematurely");
				}
				totalBytesRcvd += bytesRcvd;
			}
			System.out.println("Received:" + new String(data));

		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (socket != null) {
					socket.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

}
