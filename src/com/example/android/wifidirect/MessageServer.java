package com.example.android.wifidirect;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import ui.WiFiDirectActivity;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class MessageServer extends Thread
{

	public Handler mHandler;
	public Activity activity;

	public MessageServer(Activity activity, Handler handler)
	{
		this.activity = activity;
		this.mHandler = handler;
	}

	@Override
	public void run()
	{
		Message msg1 = Message.obtain();
		msg1.obj = "Test MeSSaGe!";
		mHandler.sendMessage(msg1);
		ServerSocket serverSocket = null;
		try
		{
			serverSocket = new ServerSocket(7023);
		} catch (IOException e2)
		{
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		while (true)// Keep server alive after an Exception
		{
			try
			{
				Log.v("Msg", "Waiting for clients to connect...");
				while (true)// Stay alive
				{
					Socket clientSocket = serverSocket.accept();
					Log.v("Msg", "SERVER - Message received...");
					InputStream inputstream = clientSocket.getInputStream();
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					copyFile(inputstream, baos);
					Object message = deserialize(baos.toByteArray());
					if (message instanceof PlayerInfo)
					{
						PlayerInfo player = (PlayerInfo) message;
						Message msg = Message.obtain();
						msg.obj = player;
						mHandler.sendMessage(msg);

					} else if (message instanceof String)
					{
						Message msg = Message.obtain();
						msg.obj = message;
						mHandler.sendMessage(msg);
					}

					try
					{
						clientSocket.close();
					} catch (IOException e)
					{
						Log.v("Msg", "Unable close client socket");
						e.printStackTrace();
					}

					Log.v("Msg", "processed a client");
				}
			} catch (IOException e)
			{
				Log.v("Msg", "Unable to process client request");
				e.printStackTrace();
			} catch (ClassNotFoundException e1)
			{
				Log.v("Msg", "Error while deserializing");
				e1.printStackTrace();
			}
		}
		

	}

	public static boolean copyFile(InputStream inputStream, OutputStream out)
	{
		byte buf[] = new byte[1024];
		int len;
		try
		{
			while ((len = inputStream.read(buf)) != -1)
			{
				out.write(buf, 0, len);

			}
			out.close();
			inputStream.close();
		} catch (IOException e)
		{
			Log.d(WiFiDirectActivity.TAG, e.toString());
			Log.v("Msg", "Something went wrong MS-L111");
			return false;
		}
		return true;
	}

	public static Object deserialize(byte[] bytes) throws IOException,
			ClassNotFoundException
	{
		ByteArrayInputStream b = new ByteArrayInputStream(bytes);
		ObjectInputStream o = new ObjectInputStream(b);
		return o.readObject();
	}

}