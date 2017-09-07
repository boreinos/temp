package com.afilon.mayor.v11.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.os.IBinder;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
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
import android.widget.TextView;

import com.afilon.mayor.v11.R;
import com.afilon.mayor.v11.interfaces.CommonListeners;
import com.afilon.mayor.v11.model.CustomKeyboard;
import com.afilon.mayor.v11.utils.Consts;
import com.afilon.mayor.v11.utils.Utilities;

public class DialogToConfirmDuiTwoBtns extends DialogFragment {

    protected Button yesBtn;
    protected Button noBtn;
    protected String message;
    protected TextView questionTv;
    private DialogToConfirmDuiListener buttonsClickedListener;
    private DuiChallengeListener duiChallengeListener;
    private EditText duiEditText;
    int realCount = 0;
    boolean replace = false, replace2 = false;
    Utilities ah;
    CustomKeyboard customKeyboard;
    DialogToConfirmDuiTwoBtns mycontext;


    public DialogToConfirmDuiTwoBtns() {
        mycontext = this;

        // Empty constructor required for DialogFragment
    }


    public interface DialogToConfirmDuiListener {
        public void onYesButtonDialogToConfirmDuiClicked(String duiNumber);

        public void onNoButtonDialogToConfirmDuiClicked();

    }

    public interface DuiChallengeListener {
        public void onYesButtonClicked(String duiNumber, int routineID);

        public void onNoButtonClicked();
    }

    public void setCustomKeyboard(CustomKeyboard keyboard) {
        customKeyboard = keyboard;
        customKeyboard.registerToFragment(this);

//        customKeyboard.registerEditText(R.id.dui_text);
    }


    public void setOnButtonsClickedListenerOne(
            DialogToConfirmDuiListener listener) {
        this.buttonsClickedListener = listener;
    }

    public void setOnDuiChallengerListener(DuiChallengeListener listener) {
        this.duiChallengeListener = listener;
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
        final String noBtnText = bundle.getString("noButtonText");
        message = bundle.getString("invisible");
        final int yesIndex = bundle.getInt("yesIndex");
        String questionText = bundle.getString("question");


        questionTv = (TextView) view.findViewById(R.id.question_textview);
        questionTv.setText(questionText);

        //--------- filter listeners ---------------------------------------------------
        CommonListeners listenerHandler = new CommonListeners();
        View.OnLongClickListener longClickListener = listenerHandler.getMouseListener();
        //--------- end filter listeners ------------------------------------------------


        duiEditText = (EditText) view.findViewById(R.id.dui_text);
        getDialog().setCanceledOnTouchOutside(false); //true to dismiss the diaglog touching outside
        registerEditTextWithCustomKeyboard(duiEditText);

        TextWatcher tw3 = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                Log.d("tw3 beforeTextChanged", "x");
            }

            @SuppressLint("LongLogTag")
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if(Consts.LOCALE.contains("ELSAL")) {
                    if (s.length() == 9 && s.charAt(8) != '-') {
                        replace = true;
                    }

                    if (duiEditText.getText().length() > 9) {
                        Log.d("tw3 duiEditText > 9", "x");

                        ah.setButtonColorGreen(yesBtn);
                        ah.setButtonColorRed(noBtn);
                    } else {
                        Log.d("tw3 duiEditText NOT > 9", "x");
                        ah.setButtonColorRed(yesBtn);
                        ah.setButtonColorGreen(noBtn);
                    }
                } else if (Consts.LOCALE.contains("HON")){
                    if(s.length()==5 && s.charAt(4) !='-'){
                        replace =true;
                    }
                    if(s.length()==10 && s.charAt(9)!='-'){
                        replace2=true;
                    }

                    if (s.length() > 14) {
                        ah.setButtonColorGreen(yesBtn);
                        ah.setButtonColorRed(noBtn);
                    }else{
                        ah.setButtonColorRed(yesBtn);
                        ah.setButtonColorGreen(noBtn);
                    }
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

                if(Consts.LOCALE.contains("ELSAL")) {
                    if (replace && realCount == 0) {
                        realCount++;
                        char d = s.charAt(8);
                        s.replace(8, 9, "-" + d);
                        replace = false;
                    }
                    realCount = 0;
                } else if (Consts.LOCALE.contains("HON")){
                    if(replace && realCount==0){
                        realCount++;
                        char d = s.charAt(4);
                        s.replace(4,5,"-"+d);
                        replace=false;
                    }else if(replace2 && realCount==0){
                        realCount++;
                        char d = s.charAt(9);
                        s.replace(9,10,"-"+d);
                        replace2=false;
                    }
                    realCount=0;
                }
            }
        };

        duiEditText.addTextChangedListener(tw3);
        duiEditText.setKeyListener(DigitsKeyListener.getInstance("0123456789-"));
        duiEditText.setOnLongClickListener(longClickListener);
        duiEditText.setOnTouchListener(listenerHandler.getDismissListener());


        yesBtn = (Button) view.findViewById(R.id.yes_btn);
        ah = new Utilities(getActivity());
        ah.setButtonColorRed(yesBtn);
        yesBtn.setText(yesBtnText);
        yesBtn.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("LongLogTag")
            public void onClick(View view) {

                if (duiEditText.getText().toString().trim().length() != 0) {

                    String duiNumber = duiEditText.getText().toString();
                    if (!(buttonsClickedListener == null))
                        buttonsClickedListener.onYesButtonDialogToConfirmDuiClicked(duiNumber);
                    if (!(duiChallengeListener == null))
                        duiChallengeListener.onYesButtonClicked(duiNumber, yesIndex);
                    // FOR NEW METHOD:

                    Log.i("TwoButtonDialogToConfirmDui - yesBtn code executed",
                            "true");
                    dismiss();

                } else {

                    Utilities ah = new Utilities(getActivity());
                    if(Consts.LOCALE.contains("HON")){
                        ah.createCustomToast("Engrese un IDENTIDAD valido para poder continuar.");
                    }else ah.createCustomToast("Engrese un DUI valido para poder continuar.");

                }

            }
        });


        noBtn = (Button) view.findViewById(R.id.no_btn);
        ah.setButtonColorGreen(noBtn);
        noBtn.setText(noBtnText);
        noBtn.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("LongLogTag")
            public void onClick(View view) {
                if (!(buttonsClickedListener == null))
                    buttonsClickedListener.onNoButtonDialogToConfirmDuiClicked();
                if (!(duiChallengeListener == null))
                    duiChallengeListener.onNoButtonClicked();
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
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getDialog().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        getDialog().getWindow().addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                |WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH);

//        getDialog().getWindow().addFlags(WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH);
        getDialog().getWindow().setDimAmount(0.1f);

    }
    @Override
    public void onDestroyView(){
        super.onDestroyView();
        customKeyboard.returnKeyboardToActivity();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
//        customKeyboard.hideCustomKeyboard();

    }


    public String getMessage() {
        return message;
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
