package com.afilon.mayor.v11.fragments;

import android.annotation.SuppressLint;
import android.app.DialogFragment;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.afilon.mayor.v11.R;

public class TwoButtonDialogFragment extends DialogFragment {

	protected Button yesBtn;
	protected Button noBtn;
	protected String message;
	protected TextView questionTv;
	protected TextView warnTv;

	public TwoButtonDialogFragment() {
		// Empty constructor required for DialogFragment
	}

	private OnTwoButtonDialogFragmentListener buttonsClickedListener;

	public interface OnTwoButtonDialogFragmentListener {
		public void onYesButtonForTwoButtonDialogClicked(int yesIdnex);

		public void onNoButtonForTwoButtonDialogClickedX();

	}

	public void setOnButtonsClickedListenerOne(
			OnTwoButtonDialogFragmentListener listener) {
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
		warnTv = (TextView)view.findViewById(R.id.warning_txtview);
		questionTv.setText(questionText);

		getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		getDialog().setCanceledOnTouchOutside(false);

		yesBtn = (Button) view.findViewById(R.id.yes_btn);
		setButtonColorGreen(yesBtn);
		yesBtn.setText(yesBtnText);
		yesBtn.setOnClickListener(new View.OnClickListener() {
			@SuppressLint("LongLogTag")
			public void onClick(View view) {
				buttonsClickedListener
						.onYesButtonForTwoButtonDialogClicked(yesIdnex);
				Log.i("TwoButtonDialogFragment - yesBtn code executed", "true");
				dismiss();
			}
		});
		noBtn = (Button) view.findViewById(R.id.no_btn);
		setButtonColorRed(noBtn);
		noBtn.setText(noBtnText);
		noBtn.setOnClickListener(new View.OnClickListener() {
			@SuppressLint("LongLogTag")
			public void onClick(View view) {
				buttonsClickedListener.onNoButtonForTwoButtonDialogClickedX();
				Log.i("TwoButtonDialogFragment - noBtn code executed", "true");
				dismiss();
			}
		});

		if (message.equals("invisible"))
			noBtn.setVisibility(View.GONE);
		else if(message.equals("noButtons"))
			hideButtons();
		else if(message.equals("closeApplication"))
			closeApplication();

		return view;
	}

	public String getMessage() {
		return message;
	}

	private void setButtonColorGreen(Button btn) {

		btn.setBackgroundResource(R.drawable.green_button_selector);
		// btn.setPadding(10, 10, 10, 10);
	}

	private void setButtonColorRed(Button btn) {

		btn.setBackgroundResource(R.drawable.red_button_selector);

	}

	public void hideButtons(){
		yesBtn.setVisibility(View.GONE);
		noBtn.setVisibility(View.GONE);

	}

	public void setTimer(){
		new CountDownTimer(30000, 1000) {

			public void onTick(long millisUntilFinished) {

				String timer = " " + millisUntilFinished / 1000+" segundos";
				String  message ="Comuníquese con el TSE para Resolver Esta Situación\n" +
						" E Iniciar de Nuevo Cuando Se Haya Resuelto\n" +
						" Esta Aplicación se Va Cerrar en "+
						timer;

				warnTv.setText(message);
			}

			public void onFinish() {
				getActivity().finishAffinity();
				//end parent application.
			}
		}.start();

	}

	public void closeApplication(){
		questionTv.setVisibility(View.GONE);
		warnTv.setVisibility(View.VISIBLE);
		hideButtons();
		setTimer();
	}

}
