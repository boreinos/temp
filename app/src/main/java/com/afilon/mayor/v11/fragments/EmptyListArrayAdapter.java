package com.afilon.mayor.v11.fragments;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import com.afilon.mayor.v11.R;
import com.afilon.mayor.v11.model.CustomKeyboard;
import com.afilon.mayor.v11.model.Party;
import com.afilon.mayor.v11.utils.Utilities;

public class EmptyListArrayAdapter extends ArrayAdapter<Party> {

	private final Context context;
	private ArrayList<Party> concetoArray;
	private ArrayList<RadioButton> arrayRadioBtn;
	private ArrayList<EditText> arrayEditTxt;
	private Utilities ah;
	private boolean radioPressed;
	private int currentRadioChecked = -1;
	private int lastRadioChecked = -1;
	private int switchIndex = -1;
	private boolean isCellEditable = false;
	private boolean isAdapterWithData = false;
	private boolean isCustomKeyboardRegistred = false;
	private CustomKeyboard mCustomKeyboard;
	// private final Activity mActivity;
	private EditText textViewValues;

	public EmptyListArrayAdapter(Context context, ArrayList<Party> concetoArray) {
		super(context, R.layout.custom_empty_list_item, concetoArray);
		this.context = context;
		// this.mActivity = activity;
		this.concetoArray = concetoArray;
		ah = new Utilities(context);
		radioPressed = false;
		arrayRadioBtn = new ArrayList<RadioButton>();
		arrayEditTxt = new ArrayList<EditText>();
		// imm = (InputMethodManager)
		// context.getSystemService(Context.INPUT_METHOD_SERVICE);
		// Hide soft keyboard
		((Activity) context).getWindow().getAttributes().softInputMode = WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {

		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = inflater.inflate(R.layout.custom_empty_list_item,
				parent, false);

		TextView textViewConceptos = (TextView) rowView
				.findViewById(R.id.concepto_name);

		textViewValues = (EditText) rowView.findViewById(R.id.concepto_value);

		textViewValues.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {

				v = (EditText) v;

				// ((EditText) v).setInputType(0); // Hide the standard keyboard
				mCustomKeyboard.showCustomKeyboard(v);

			}
		});

		textViewValues.setEnabled(false);

		textViewConceptos.setText(concetoArray.get(position).getParty_name());
		textViewValues.setText(concetoArray.get(position).getParty_votes());
		// textViewValues.setId(Integer.parseInt(String.valueOf(PREFIX_EDITTIEXT)
		// + String.valueOf(position)));
		textViewValues.setId(position);

		final int iCellPosition = textViewValues.getId();

		// Display Radio Buttons just when Adapter reached the second stage
		// (values on votes)
		if (isAdapterWithData) {
			// CARLOS: Add RadioButton for each row
			final RadioButton rb = (RadioButton) rowView
					.findViewById(R.id.radioBtn);
			rb.setVisibility(View.VISIBLE);
			// rb.setId(Integer.parseInt(String.valueOf(PREFIX_RADIOBUTTON) +
			// String.valueOf(position)));
			rb.setId(position);
			rb.setTag(position);

			arrayRadioBtn.add(rb);
			arrayEditTxt.add(textViewValues);

			// if(isCellEditable) {
			// rb.setButtonDrawable(R.drawable.radiobuttongreen);
			// }

			// CARLOS: Keep track of EditView cells while scrolling up and down
			/*
			 * if (switchIndex == 1 && isCellEditable) {
			 * 
			 * setEditVote(arrayEditTxt, position); }
			 */

			// Keep the SELECTED RadioButton and the EditText Updated based on
			// the position
			rb.setButtonDrawable(currentRadioChecked == position ? R.drawable.checked
					: R.drawable.disabled_cb);
			rb.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {

					if (switchIndex == 1) {

						isCellEditable = true;

						if (lastRadioChecked > -1) {
							if (Integer.parseInt(v.getTag().toString()) == lastRadioChecked) {
								rb.setButtonDrawable(R.drawable.checked);
							}
						}

						// Mark RadioButton Selected
						rb.setButtonDrawable(Integer.parseInt(v.getTag()
								.toString()) == position ? R.drawable.checked
								: R.drawable.radiobuttonred);
						// textViewValues.setEnabled(textViewValues.getId() ==
						// position ? true : false);
						// textViewValues.setInputType(textViewValues.getId() ==
						// position ? 0x00000002 : 0x00000000);

						// Tint each RadioButton that is not equal to the
						// pressed
						// one
						for (RadioButton radioBtn : arrayRadioBtn) {
							if (radioBtn.getId() != position) {
								radioBtn.setButtonDrawable(R.drawable.disabled_cb);
							}
						}

						setEditVote(arrayEditTxt, position);

						currentRadioChecked = Integer.parseInt(v.getTag()
								.toString());

						// Track the last RadioButton checked
						lastRadioChecked = Integer.parseInt(v.getTag()
								.toString());

						// ah.createCustomToast(
						// "Usted ha Seleccionado CELDA # : ",
						// String.valueOf(iCellPosition));
						radioPressed = true;
					}
				}

			});

		}

		return rowView;
	}

	// CARLOS: Control the EditView inside the adapter
	private void setEditVote(ArrayList<EditText> arrayEditTxt,
			final int position) {
		// OPEN UP the EditText that was selected
		for (EditText vote : arrayEditTxt) {
			if (vote.getId() == position) {
				// Log.i("arrayEditTxt position ", String.valueOf(position));

				vote.setBackgroundResource(android.R.drawable.editbox_background_normal);
				vote.setLayoutParams(new LinearLayout.LayoutParams(200, 55));
				vote.setEnabled(true);

				if (isCustomKeyboardRegistred) {
					View vi = vote;
					if (vi instanceof EditText) {
						int textId = vi.getId();
						// Log.i("textId ", String.valueOf(textId));
						mCustomKeyboard.registerEditText(textId);
					}
				}

				vote.requestFocus();
				// vote.setRawInputType(InputType.TYPE_CLASS_NUMBER);
				vote.setTextColor(Color.BLUE);
			}
		}

		/*
		 * for (EditText vote : arrayEditTxt) { if (vote.getId() != position) {
		 * 
		 * vote.setBackgroundColor(00000000); // Set default. //
		 * vote.setTextColor(Color.BLUE); vote.setLayoutParams(new
		 * LinearLayout.LayoutParams( LinearLayout.LayoutParams.WRAP_CONTENT,
		 * LinearLayout.LayoutParams.WRAP_CONTENT)); vote.setEnabled(false); } }
		 */
	}

	public void enableEditViewCells() {
		switchIndex = 1;
	}

	public void setRadioButtonsForDisplay() {
		isAdapterWithData = true;
	}

	public void registerKeyboard(CustomKeyboard mCustomKeyboard2) {

		mCustomKeyboard = mCustomKeyboard2;
		isCustomKeyboardRegistred = true;

		// if (textViewValues != null) {
		// int id = textViewValues.getId();
		// mCustomKeyboard.registerEditText(id);
		// // mCustomKeyboard2.showCustomKeyboard(v);
		// }
	}

	public void registerEditText(EditText edittext) {
		textViewValues = edittext;
	}
}
