package ui;

import com.example.android.wifidirect.R;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.ChannelListener;
import android.os.Bundle;
import android.widget.LinearLayout;

import utils.BitmapCache;
import wifidirect.WiFiDirectBroadcastReceiver;

public class ServerActivity extends Activity implements ChannelListener, ChooseClientsDialog.OnClientAddedListener {
	
	private PokerTableView mGameboard;
	private BitmapCache mMemoryCache;
	private LinearLayout mServerLayout;
	
	public static final String TAG = "wifidirectdemo";
	private WifiP2pManager manager;
	private boolean isWifiP2pEnabled = false;
	private boolean retryChannel = false;

	private final IntentFilter intentFilter = new IntentFilter();
	private Channel channel;
	private BroadcastReceiver receiver = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		final int memClass = ((ActivityManager) getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass();
		final int cacheSize = 1024 * 1024 * memClass / 8;
		mMemoryCache = new BitmapCache(cacheSize, getAssets());
		
		setContentView(R.layout.server_activity);
		
		mServerLayout = (LinearLayout) findViewById(R.id.server_layout);
		
		final String userName = getSharedPreferences(MenuActivity.POKER_PREFERENCES, MODE_PRIVATE).getString(
				MenuActivity.USER_NAME_KEY, "");
		
		mGameboard = new PokerTableView(ServerActivity.this, mMemoryCache, userName);
		mServerLayout.addView(mGameboard, 0);
		mGameboard.startBus();
		
		intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
		intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
		intentFilter
				.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
		intentFilter
				.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
		manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
		channel = manager.initialize(this, getMainLooper(), null);
	}
	
	/** register the BroadcastReceiver with the intent values to be matched */
	@Override
	public void onResume()
	{
		super.onResume();
		receiver = new WiFiDirectBroadcastReceiver(manager, channel, this);
		registerReceiver(receiver, intentFilter);
	}

	@Override
	public void onPause()
	{
		super.onPause();
		unregisterReceiver(receiver);
	}
	
	
}
