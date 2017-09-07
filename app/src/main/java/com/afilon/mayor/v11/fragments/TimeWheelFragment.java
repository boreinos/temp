package com.afilon.mayor.v11.fragments;

import java.util.Calendar;
import java.util.Locale;

import kankan.wheel.widget.WheelView;
import kankan.wheel.widget.adapters.ArrayWheelAdapter;
import kankan.wheel.widget.adapters.NumericWheelAdapter;
import android.app.Activity;
import android.app.DialogFragment;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;

import com.afilon.mayor.v11.R;

public class TimeWheelFragment extends DialogFragment {

	public TimeWheelFragment() {
		// Empty constructor required for DialogFragment
	}

	private OnTimeWheelDialogFragmentListener buttonsClickedListener;

	public interface OnTimeWheelDialogFragmentListener {
		public void setCurrentTime(int index, int currentItem, int currentMin,
				int amPmString);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			buttonsClickedListener = (OnTimeWheelDialogFragmentListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement listeners!");
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setStyle(DialogFragment.STYLE_NORMAL, R.style.AflDialogStyle);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.time_wheel_layout, container,
				false);

		Bundle bundle = getArguments();

		final int yesIdnex = bundle.getInt("yesIndex");

		getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

		final WheelView hours = (WheelView) view.findViewById(R.id.hour);
		hours.setViewAdapter(new NumericWheelAdapter(getActivity(), 0, 11));
		hours.setCyclic(true);
//		hours.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//			@Override
//			public void onFocusChange(View v, boolean hasFocus) {
//						hours.drawCenterRect(new Canvas());
//			}
//		});
		final WheelView mins = (WheelView) view.findViewById(R.id.mins);
		mins.setViewAdapter(new NumericWheelAdapter(getActivity(), 0, 59,
				"%02d"));
		mins.setCyclic(true);

		final WheelView ampm = (WheelView) view.findViewById(R.id.ampm);
		ArrayWheelAdapter<String> ampmAdapter = new ArrayWheelAdapter<String>(
				getActivity(), new String[] { "AM", "PM" });

		ampm.setViewAdapter(ampmAdapter);

		// set current time
		Calendar calendar = Calendar.getInstance(Locale.US);
		hours.setCurrentItem(calendar.get(Calendar.HOUR));
		mins.setCurrentItem(calendar.get(Calendar.MINUTE));
		ampm.setCurrentItem(calendar.get(Calendar.AM_PM));

		Button setButton = (Button) view.findViewById(R.id.set_button);
		setButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {

				buttonsClickedListener.setCurrentTime(yesIdnex,
						hours.getCurrentItem(), mins.getCurrentItem(),
						ampm.getCurrentItem());
				dismiss();
			}
		});



		return view;
	}

	public void onFocusWheel(){

	}
}
