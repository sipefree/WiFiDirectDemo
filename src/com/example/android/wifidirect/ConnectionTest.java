package com.example.android.wifidirect;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class ConnectionTest extends Activity
{

	private ArrayList<PlayerInfo> players = null;

	private ArrayAdapter<PlayerInfo> adapter;

	private final ConnectionTest activity = this;

	private boolean isGroupOwner;

	private PlayerInfo me;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setContentView(R.layout.connection_test);

		players = (ArrayList<PlayerInfo>) getIntent().getExtras()
				.get("players");

		final ListView listView = (ListView) findViewById(R.id.mylistview);
		adapter = new ArrayAdapter<PlayerInfo>(this,
				android.R.layout.simple_list_item_1, players) {
			@Override
			public View getView(int position, View convertView, ViewGroup parent)
			{
				// TODO Auto-generated method stub
				return super.getView(position, convertView, parent);
			}
		};
		listView.setAdapter(adapter);
		Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg)
			{
				Toast.makeText(activity, msg.obj.toString(), Toast.LENGTH_SHORT)
						.show();

			}
		};

		String ip = Utils.getLocalIP();

		for (PlayerInfo p : players)
		{
			// If i am the group owner
			if (p.getAddress().equals(ip))
			{
				me = p;
				isGroupOwner = p.isGroupowner();
			}
		}

		MessageServer server = new MessageServer(this, handler);
		server.start();

	}

	public void bcastMessage(View view)
	{
		if (isGroupOwner)
		{
			for (PlayerInfo p : players)
			{
				if (!p.equals(me))
				{
					InetAddress addr;
					try
					{
						addr = InetAddress.getByName(p.getAddress());
						new MessageAsyncTask(activity, view, addr,
								"Hello from GO!").execute();
					} catch (UnknownHostException e)
					{
						e.printStackTrace();
					}

				}
			}
		}

	}

}
