/**
 * 
 */
package ui;


import com.example.android.wifidirect.R;
import com.example.android.wifidirect.R.id;
import com.example.android.wifidirect.R.layout;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

/**
 * @author paul
 *
 */
public class Main extends Activity
{
	private TextView userNameText;

	/**
	 * 
	 */
	public Main()
	{
		// TODO Auto-generated constructor stub
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// TODO Auto-generated method stub
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.startscreen);
	}
	
	public void savePlayerClicked(View view)
	{
		userNameText = (TextView) this.findViewById(R.id.editText1);
		Intent intent = new Intent(this, WiFiDirectActivity.class);
		intent.putExtra("playername", userNameText.getText().toString());
		startActivity(intent);
	}

}
