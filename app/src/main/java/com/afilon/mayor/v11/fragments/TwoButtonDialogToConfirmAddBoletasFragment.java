package com.afilon.mayor.v11.fragments;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.afilon.mayor.v11.R;

public class TwoButtonDialogToConfirmAddBoletasFragment extends DialogFragment {

	protected Button yesBtn;
	protected Button noBtn;
	protected String message;
	protected TextView questionTv;

	public TwoButtonDialogToConfirmAddBoletasFragment() {
		// Empty constructor required for DialogFragment
	}

	private OnTwoButtonDialogToConfirmAddBoletasFragmentListener buttonsClickedListener;

	public interface OnTwoButtonDialogToConfirmAddBoletasFragmentListener {
		public void onYesButtonForToConfirmAddBoletasDialogClicked(int yesIdnex);

		public void onNoButtonForToConfirmAddBoletasDialogClicked();

	}

	public void setOnButtonsClickedListenerOne(
			OnTwoButtonDialogToConfirmAddBoletasFragmentListener listener) {
		this.buttonsClickedListener = listener;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setStyle(DialogFragment.STYLE_NORMAL, R.style.AflDialogStyle);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View view = inflater.inflate(
				R.layout.two_button_dialog_fragment_layout, container, false);

		Bundle bundle = getArguments();
		String yesBtnText = bundle.getString("yesButtonText");
		String noBtnText = bundle.getString("noButtonText");
		message = bundle.getString("invisible");
		final int yesIdnex = bundle.getInt("yesIndex");
		final String questionText = bundle.getString("question");

		questionTv = (TextView) view.findViewById(R.id.question_textview);
		questionTv.setText(questionText);

		getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		getDialog().setCanceledOnTouchOutside(false);

		yesBtn = (Button) view.findViewById(R.id.yes_btn);
		yesBtn.setText(yesBtnText);
		yesBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				buttonsClickedListener
						.onYesButtonForToConfirmAddBoletasDialogClicked(yesIdnex);
				
				dismiss();
			}
		});
		noBtn = (Button) view.findViewById(R.id.no_btn);
		noBtn.setText(noBtnText);
		noBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				buttonsClickedListener.onNoButtonForToConfirmAddBoletasDialogClicked();
				
				dismiss();
			}
		});

		if (message.equals("invisible"))
			noBtn.setVisibility(View.GONE);

		return view;
	}

	public String getMessage() {
		return message;
	}

}
