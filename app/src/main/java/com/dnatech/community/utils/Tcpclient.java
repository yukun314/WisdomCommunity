package com.dnatech.community.utils;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

public  class Tcpclient {
	private static String MsgString = "";//接收到的字符串
	private boolean ysnonewstr=false;//是否收到新数据
	public static String localAddress="";
	public static String getLocalAddress() {
		return localAddress;
	}

	public static Handler getmHandler() {
		return mHandler;
	}

	public static void setmHandler(Handler mHandler) {
		Tcpclient.mHandler = mHandler;
	}
	private static Socket socket;
	private static Handler mHandler;


	public Tcpclient(){
		//runt();
	}

	private void runt(){
		new Thread(new Runnable() {
			@Override
			public void run() {
//	            /*	 System.out.println("线程");*/
				chatConnection();
			}
		}).start();
	}

	public boolean isConnected(){
		if(socket != null) {
			return socket.isConnected();
		}
		return false;
	}

	public boolean connectServer(String sip,int port) {

		if(socket == null){
			try {
				// Log.e("111111","tfnpghl"+sip);
				socket = new Socket(sip,port);
				localAddress= socket.getLocalAddress().getHostAddress();

				if (socket.isConnected()){
					runt();//建立线程循环接收服务数据
					//连接成功"
					return true;
				}
				else{
					return false;
				}

			}
			catch (UnknownHostException e) {
				e.printStackTrace();
				return false;
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}

		}
		return false;

	}
	public void chatConnection() {
//		System.out.println("链接成功");
//		InputStream in=null;
//		while (true) {
//			// int	 tem;
//			//byte[] b=new byte[20];
//			try {
//				if(socket.equals(null)){
//					Log.d("size", "对象为空： ");
//					break;
//				}
//				in = socket.getInputStream();
//				ReceiveByte(in);
//
//			} catch (IOException e) {
//				System.out.println(e.getMessage()+" 接收数据循环错误！");
//				e.printStackTrace();
//				break;
//			}
//		}
	}

	public void ReceiveByte(InputStream in) {
		System.out.println("receiveByte 接收到数据处理");
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("生成reader有错误："+e);
		}
		StringBuilder sb = new StringBuilder();
		String line = null;
		System.out.println("aldk");
		System.out.println("reader is null:"+(reader == null));
		try {
			if(reader != null) {
				line = reader.readLine();
				while (line != null) {
					System.out.println("while：" + line);
					sb.append(line + "/n");
					line = reader.readLine();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("是不是有错误啊："+e);
		} finally {
			System.out.println("try结束之后");
//			try {
//				in.close();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
		}
		String m = sb.toString();
		System.out.println("返回的结果:"+m);
		Message message = new Message();
		message.what = 101;
		message.obj = m;
		mHandler.sendMessage(message);
	}

	/*发送给服务器数据*/
	public boolean sendToServer(String sendmsg){
		System.out.println("sendToServer ");
		/*	if(!socket.equals(null) && */
		socket.isOutputShutdown();
//		byte buffer[] = String2Byte(sendmsg);
		byte buffer [] = sendmsg.getBytes();
		OutputStream outputStream ;
		InputStream in;
		int temp = buffer.length;
		try {
			outputStream = socket.getOutputStream();

			outputStream.write(buffer ,0 ,temp);
			outputStream.flush();
			in = socket.getInputStream();
//			ReceiveByte(in);
			//读取服务器返回的消息
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String mess = br.readLine();
			System.out.println("mess:"+mess);
			return true;
		} catch (IOException e) {
			return false;
				/*	e.printStackTrace();*/
		}
	    /*	return true;
	    	}*/

	}
	/**

	 * 十六进制字符串(必需成双)转换成字节数组(byte[])
	 * @param hexString
	 * @return
	 */
	public static byte[] String2Byte(String hexString){
		try {
			if(hexString.length() % 2 ==1)
				return null;
			byte[] ret = new byte[hexString.length()/2];
			for (int i = 0; i < hexString.length(); i+=2) {
				ret[i/2] = Integer.decode("0x"+hexString.substring(i,i+2)).byteValue();
			}
			return ret;
		} catch (Exception e) {
			// TODO: handle exception
			return null;
		}

	}
	/**
	 * 16进制数组转化成字符串(大写字母)，比如[0x03][0x3f]转化成"033F或03 3F"
	 * @param b=需要转换的字节数据，
	 * @boo=是否去掉第一个是零的字节 true=去掉、 false=不去掉
	 * @return返回转换后的字符串。
	 */
 /*   	@SuppressLint("DefaultLocale")*/
	public static String hex2HexString(byte[] b,boolean boo) {
		int len = b.length;
		int[] x = new int[len];
		String[] y = new String[len];
		StringBuilder str = new StringBuilder();
		// 转换成Int数组,然后转换成String数组
		for (int j = 0; j < len; j++) {
			x[j] = b[j] & 0xff;
			y[j] = Integer.toHexString(x[j]);
			while (y[j].length() < 2) {
				y[j] = "0" + y[j];
			}
			//便于分隔在每个字节中间加上空格
			if(j==(len-1)){
				str.append(y[j]);
			}else{
				str.append(y[j]);
				str.append(" ");
			}
		}

		if(boo==true){ //是否弃掉以"0"开头的字符
			while(str.indexOf("0")==0){
				str = str.delete(0, 1);
			}

		}
		return new String(str).toUpperCase();//toUpperCase()方法  转化成大写
	}
	/*关闭连接*/
	public void close(){
		if (socket != null) {
			try {
				socket.close();
				socket = null;
			} catch (IOException e) {
				System.out.println(e.getMessage());
				e.printStackTrace();
			}
		}

	}
	//属性

//	public static boolean isYsnoread() {
//		return ysnoread;
//	}

//	public static void setYsnoread(boolean ysnoread) {
//		Tcpclient.ysnoread = ysnoread;
//	}

	public static String getMsgString() {
		return MsgString;
	}

	public void setMsgString(String msgString) {
		MsgString = msgString;
	}
	public boolean isYsnonewstr() {
		return ysnonewstr;
	}
	/***新加上的***/

	private static final String SERVERIP = "192.168.0.105";
	private static final int SERVERPORT = 5000;
//	private static Socket socket;
	public static void tcpSend(String msg,Handler mHandler){
		System.out.println("tcpSend:"+msg);
		try {
			Socket	socket = new Socket(SERVERIP, SERVERPORT);
			byte buffer [] = msg.getBytes();
			socket.setSoTimeout(1000);
			OutputStream outputStream = socket.getOutputStream();
			InputStream inputStream = socket.getInputStream();
			int temp = buffer.length;
			outputStream.write(buffer ,0 ,temp);
			outputStream.flush();
			inputStream = socket.getInputStream();
			byte[] data = new byte[20];
//			StringBuilder stringBuilder = new StringBuilder();
//			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
//			boolean firstLine = true;
//			String line = null; ;
//			while((line = bufferedReader.readLine()) != null){
//				if(!firstLine){
//					stringBuilder.append(System.getProperty("line.separator"));
//				}else{
//					firstLine = false;
//				}
//				stringBuilder.append(line);
//			}
			// receive
			int totalBytesRcvd = 0;// total bytes received so far
			int bytesRcvd;// Bytes received in last read

			while (totalBytesRcvd == 0) {
				bytesRcvd = inputStream.read(data, totalBytesRcvd, data.length
						- totalBytesRcvd);
				if (bytesRcvd == -1) {
					throw new SocketException("Connection closed prematurely");
				}
				totalBytesRcvd += bytesRcvd;
			}
			String datarcvd = new String(data).trim();
			Message message = new Message();
			message.what = 101;
			message.obj = datarcvd;
			mHandler.sendMessage(message);
			System.out.println("返回的结果:"+datarcvd);
			socket.close();
			socket=null;
			System.out.println("发送之后：");
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void TcpSendData(final String sendData) {
//		new Thread(){
//			@Override
//			public void run(){
//				byte[] data = new byte[10];
//				// create socket that is connected to server on specified port
//				try {
//
//					out.write(sendData.getBytes());
//
//					// receive
//					int totalBytesRcvd = 0;// total bytes received so far
//					int bytesRcvd;// Bytes received in last read
//
//					while (totalBytesRcvd == 0) {
//						bytesRcvd = in.read(data, totalBytesRcvd, data.length
//								- totalBytesRcvd);
//						if (bytesRcvd == -1) {
//							throw new SocketException("Connection closed prematurely");
//						}
//						totalBytesRcvd += bytesRcvd;
//					}
//					String datarcvd = new String(data).trim();
//					if(datarcvd.equals("on1")){
//						Message msg = new Message();
//						msg.what = 1;
//						mHandler.sendMessage(msg);
//					}
//					else if(datarcvd.equals("off1")){
//						Message msg = new Message();
//						msg.what = 2;
//						mHandler.sendMessage(msg);
//					}
//					else if(datarcvd.equals("on2")){
//						Message msg = new Message();
//						msg.what = 3;
//						mHandler.sendMessage(msg);
//					}else if(datarcvd.equals("off2")){
//						Message msg = new Message();
//						msg.what = 4;
//						mHandler.sendMessage(msg);
//					}
//					System.out.println("Received:" + new String(data));
//
//				} catch (UnknownHostException e) {
//					e.printStackTrace();
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//			}
//		}.start();


	}

}  //CLASS类结束

