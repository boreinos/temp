package com.afilon.mayor.v11.fragments;

import android.app.Activity;
import android.app.DialogFragment;
import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.afilon.mayor.v11.R;
import java.util.ArrayList;
import java.util.HashMap;

import kankan.wheel.widget.WheelView;
import kankan.wheel.widget.adapters.NumericWheelAdapter;

public class WheelFragment extends DialogFragment {

	public WheelFragment() {
		// Empty constructor required for DialogFragment
	}

	private OnWheelDialogListener buttonsClickedListener;

	public interface OnWheelDialogListener {
		public void setValuesSelected(int index, HashMap<String,Integer>values);

	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			buttonsClickedListener = (OnWheelDialogListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement listeners!");
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

		final View fview = inflater.inflate(R.layout.wheel_layout, container, false);
		getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

		Bundle bundle = getArguments();
		int min = bundle.getInt("min");
		int max = bundle.getInt("max");
		String message = bundle.getString("msg");
		final int index = bundle.getInt("index");
		int id = "MARCAS".hashCode() & 0xfffffff;
		WheelView wheelView = createWheelView(inflater,id);
		wheelView.setViewAdapter(new NumericWheelAdapter(getActivity(),min,max,"%02d"));
		wheelView.setCyclic(false);
		wheelView.setCurrentItem(max);
		((LinearLayout)fview.findViewById(R.id.wheel_layout)).addView(wheelView);

		((TextView)fview.findViewById(R.id.message)).setText(message);
		Button setButton = (Button) fview.findViewById(R.id.set_button);
		setButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				String key = "MARCAS";
				HashMap<String,Integer> selections = new HashMap<String, Integer>();
				int id = key.hashCode() & 0xfffffff;
				int value = ((WheelView)fview.findViewById(id)).getCurrentItem()+1;//returns index.
				selections.put(key,value);
				buttonsClickedListener.setValuesSelected(index,selections);
				dismiss();
			}
		});




//		final ArrayList<String> keys = bundle.getStringArrayList("keys");
//		HashMap<String,Pair> ranges = (HashMap<String,Pair>) bundle.getSerializable("ranges");
//
//
//		for(String request: keys){
//			Pair minMax = ranges.get(request);
//			int min = (int) minMax.first;
//			int max = (int) minMax.second;
//			int id = request.hashCode() & 0xfffffff;
//			WheelView wheelView = createWheelView(inflater,id);
//			wheelView.setViewAdapter(new NumericWheelAdapter(getActivity(),min,max,"%02d"));
//			wheelView.setCyclic(true);
//			wheelView.setCurrentItem(min);
//			((LinearLayout)fview.findViewById(R.id.wheel_layout)).addView(wheelView);
//		}

//		setButton.setOnClickListener(new View.OnClickListener() {
//			public void onClick(View view) {
//				HashMap<String,Integer> selections = new HashMap<String, Integer>();
//				for(String key: keys){
//					int id = key.hashCode() & 0xfffffff;
//					int value = ((WheelView)fview.findViewById(id)).getCurrentItem();
//					selections.put(key,value);
//				}
//				buttonsClickedListener.setValuesSelected(index,selections);
//				dismiss();
//			}
//		});
		return fview;
	}

	private WheelView createWheelView(LayoutInflater inflater, int id){
		View view = inflater.inflate(R.layout.wheel_view,null);
		view.setId(id);
		return (WheelView) view;
	}
}
