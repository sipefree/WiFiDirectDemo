
package ui;

import com.example.android.wifidirect.R;

import android.app.DialogFragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * {@link DialogFragment} used for entering player's name.
 */
public class UserNameDialog extends DialogFragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.user_name_dialog_fragment, container);
		final TextView userNameText = (TextView) view.findViewById(R.id.user_name_text_view);
		final View okButton = view.findViewById(R.id.user_name_ok_button);

		final SharedPreferences sharedPreferences = getActivity().getSharedPreferences(MenuActivity.POKER_PREFERENCES,
				Context.MODE_PRIVATE);
		final String userName = sharedPreferences.getString(MenuActivity.USER_NAME_KEY, "");
		userNameText.append(userName);

		okButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				final String name = userNameText.getText().toString();
				if (name.length() > 0) {
					final SharedPreferences.Editor editor = sharedPreferences.edit();
					editor.putString(MenuActivity.USER_NAME_KEY, name);
					editor.apply();
					((MenuActivity) getActivity()).setNameTextView(name);
					dismiss();
				}
			}
		});

		setCancelable(false);
		getDialog().setCanceledOnTouchOutside(false);
		getDialog().setTitle(getString(R.string.username_dialog_title));
		return view;
	}
}
