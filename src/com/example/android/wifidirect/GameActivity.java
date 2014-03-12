package com.example.android.wifidirect;

import android.app.Activity;
import android.os.Bundle;

public class GameActivity extends Activity
{
	private String playerName;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.game_activity);
		
		playerName = (String) getIntent().getExtras()
				.get("playername");
	}

}
