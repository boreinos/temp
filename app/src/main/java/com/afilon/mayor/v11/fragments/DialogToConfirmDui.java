package com.afilon.mayor.v11.fragments;

import android.annotation.SuppressLint;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.os.IBinder;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afilon.mayor.v11.R;
import com.afilon.mayor.v11.model.CustomKeyboard;
import com.afilon.mayor.v11.utils.Consts;
import com.afilon.mayor.v11.utils.Utilities;

public class DialogToConfirmDui extends DialogFragment {

	protected Button yesBtn;
	protected Button noBtn;
	protected String message;
	protected TextView questionTv;
	private DialogToConfirmDuiListener buttonsClickedListener;
	private EditText duiEditText;
	CustomKeyboard customKeyboard;


	public DialogToConfirmDui() {

		// Empty constructor required for DialogFragment
	}

	public interface DialogToConfirmDuiListener {
		public void onYesButtonDialogToConfirmDuiClicked(String duiNumber);

		public void onNoButtonDialogToConfirmDuiClicked();

	}
   public void setCustomKeyboard(CustomKeyboard keyboard){
	   customKeyboard = keyboard;
	   customKeyboard.registerToFragment(this);
//	   customKeyboard.registerEditText(R.id.dui_text);
   }
	public void setOnButtonsClickedListenerOne(
			DialogToConfirmDuiListener listener) {
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
                R.layout.two_button_dialog_fragment_confirmdui_layout,
                container, false);


		Bundle bundle = getArguments();
		String yesBtnText = bundle.getString("yesButtonText");
		String noBtnText = bundle.getString("noButtonText");
		message = bundle.getString("invisible");
		int yesIndex = bundle.getInt("yesIndex");
		String questionText = bundle.getString("question");
		final String errorMessage;
		if(message.equals("MARKS")){
            view = inflater.inflate(R.layout.edittext_dialog, container, false);
			errorMessage = "El numero de marcas debe ser mayor que cero 0";
		}
		else if(Consts.LOCALE.contains("HON")){
			errorMessage = "Engrese un IDENTIDAD valido para poder continuar.";
		}else errorMessage = "Engrese un DUI valido para poder continuar.";


        duiEditText = (EditText) view.findViewById(R.id.dui_text);
        noBtn = (Button) view.findViewById(R.id.no_btn);

		questionTv = (TextView) view.findViewById(R.id.question_textview);
		questionTv.setText(questionText);



		getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		getDialog().setCanceledOnTouchOutside(false);
        registerEditTextWithCustomKeyboard(duiEditText);

//		duiEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//			@Override
//			public void onFocusChange(View v, boolean hasFocus) {
//				//closeKeyboard(getActivity(), duiEditText.getWindowToken());
//				customKeyboard.showCustomKeyboard((View)v.getParent());
//			}
//		});



		yesBtn = (Button) view.findViewById(R.id.yes_btn);
		setButtonColorGreen(yesBtn);
		yesBtn.setText(yesBtnText);
		yesBtn.setOnClickListener(new View.OnClickListener() {
			@SuppressLint("LongLogTag")
			public void onClick(View view) {

				if (duiEditText.getText().toString().trim().length() != 0) {

					String duiNumber = duiEditText.getText().toString();
					buttonsClickedListener
							.onYesButtonDialogToConfirmDuiClicked(duiNumber);
					Log.i("TwoButtonDialogToConfirmDui - yesBtn code executed",
							"true");
					dismiss();

				} else {

					Utilities ah = new Utilities(getActivity());
					ah.createCustomToast(errorMessage);

//					if(Consts.LOCALE.contains("HON")){
//						ah.createCustomToast("Engrese un IDENTIDAD valido para poder continuar.");
//					}else ah.createCustomToast("Engrese un DUI valido para poder continuar.");

				}

			}
		});
		
		

		setButtonColorRed(noBtn);
		noBtn.setText(noBtnText);
		noBtn.setOnClickListener(new View.OnClickListener() {
			@SuppressLint("LongLogTag")
			public void onClick(View view) {
				buttonsClickedListener.onNoButtonDialogToConfirmDuiClicked();
				Log.i("TwoButtonDialogToConfirmDui - noBtn code executed",
						"true");
				dismiss();
			}
		});
        if (message.equals("invisible"))
            noBtn.setVisibility(View.GONE);



		return view;
	}
	public void closeKeyboard(Context c, IBinder windowToken) {
		InputMethodManager mgr = (InputMethodManager) c.getSystemService(Context.INPUT_METHOD_SERVICE);
		mgr.hideSoftInputFromWindow(windowToken, 0);
		mgr.showInputMethodPicker();
	}
	@Override
	public void onActivityCreated(Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);

        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        getDialog().getWindow().addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                |WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH);
		getDialog().getWindow().setDimAmount(0.1f);
	}
    @Override
    public void onDestroyView(){
        super.onDestroyView();
        customKeyboard.returnKeyboardToActivity();
    }

//	@Override
//	public void onDestroy(){
//		super.onDestroy();
//		//clean keyboard
//		customKeyboard.returnKeyboardToActivity();
////		customKeyboard=null;
//
//	}



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
    private void registerEditTextWithCustomKeyboard(EditText edittext){



        // Make the custom keyboard appear
        edittext.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            // NOTE By setting the on focus listener, we can show the custom
            // keyboard when the edit box gets focus, but also hide it when
            // the
            // edit box loses focus
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus){
                    setCustomKeyboard(customKeyboard); //todo: still testing.
                    customKeyboard.showCustomKeyboard((View) v.getParent());
                }

/*                    else
                        customKeyboard.hideCustomKeyboard();*/
            }
        });
        edittext.setOnClickListener(new View.OnClickListener() {
            // NOTE By setting the on click listener, we can show the custom
            // keyboard again, by tapping on an edit box that already had
            // focus
            // (but that had the keyboard hidden).
            @Override
            public void onClick(View v) {
                customKeyboard.showCustomKeyboard((View) v.getParent());
            }
        });
        // Disable standard keyboard hard way
        // NOTE There is also an easy way:
        // 'edittext.setInputType(InputType.TYPE_NULL)' (but you will not
        // have a
        // cursor, and no 'edittext.setCursorVisible(true)' doesn't work )
        edittext.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                EditText edittext = (EditText) v;
                int inType = edittext.getInputType(); // Backup the input
                // type
                edittext.setInputType(InputType.TYPE_NULL); // Disable
                // standard
                // keyboard
                edittext.onTouchEvent(event); // Call native handler
                edittext.setInputType(inType); // Restore input type
                return true; // Consume touch event
            }
        });
        // Disable spell check (hex strings look like words to Android)
        edittext.setInputType(edittext.getInputType()
                | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);

    }

}
