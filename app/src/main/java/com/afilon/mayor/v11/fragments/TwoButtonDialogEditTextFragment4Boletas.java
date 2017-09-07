package com.afilon.mayor.v11.fragments;

import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.text.method.DigitsKeyListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.afilon.mayor.v11.R;
import com.afilon.mayor.v11.utils.Utilities;

public class TwoButtonDialogEditTextFragment4Boletas extends DialogFragment {

	protected Button yesBtn;
	protected Button noBtn;
	protected String message;
	protected TextView questionTv;
	private OnTwoButtonBoletaDialogFragmentListener buttonsClickedListener;
	private EditText numberOfBoletasEditText;
	private int inputCounter = 0;
	private TwoButtonDialogFragment twoBtnDialogFragment;

	public TwoButtonDialogEditTextFragment4Boletas() {
		// Empty constructor required for DialogFragment
	}

	public interface OnTwoButtonBoletaDialogFragmentListener {
		public void onYesButtonBoletaDialogClicked(String numberOfPics);

		public void onNoButtonBoletaDialogClicked();
	}

	public void setOnButtonsClickedListenerOne(
			OnTwoButtonBoletaDialogFragmentListener listener) {
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
				R.layout.two_button_dialog_fragment_with_edittext_layout,
				container, false);

		Bundle bundle = getArguments();
		String yesBtnText = bundle.getString("yesButtonText");
		String noBtnText = bundle.getString("noButtonText");
		message = bundle.getString("invisible");
		final int yesIdnex = bundle.getInt("yesIndex");
		final String questionText = bundle.getString("question");

		questionTv = (TextView) view.findViewById(R.id.question_textview);
		questionTv.setText(questionText);

		numberOfBoletasEditText = (EditText) view
				.findViewById(R.id.number_of_pics);
		numberOfBoletasEditText.setKeyListener(DigitsKeyListener.getInstance("0123456789"));

		getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		getDialog().setCanceledOnTouchOutside(false);

		yesBtn = (Button) view.findViewById(R.id.yes_btn);
		yesBtn.setText(yesBtnText);
		yesBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {

				String numberOfBoletas = numberOfBoletasEditText.getText()
						.toString();
				Utilities ah = new Utilities(getActivity());
				int intNumberOfBoletas = ah.parseInt(numberOfBoletas, -1);

				if (intNumberOfBoletas >= 0) {
//					numberOfBoletasEditText.clearFocus();
					InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(numberOfBoletasEditText.getWindowToken(), 0);

					dismiss();
					buttonsClickedListener.onYesButtonBoletaDialogClicked(numberOfBoletas);
				} else {

					ah.createCustomToast("El numero ingresado no es valido");//de boletas debe ser mayor que cero 0.");

				}
			}
		});
		noBtn = (Button) view.findViewById(R.id.no_btn);
		noBtn.setText(noBtnText);
		noBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				buttonsClickedListener.onNoButtonBoletaDialogClicked();
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