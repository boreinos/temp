package com.afilon.mayor.v11.fragments;

import android.annotation.SuppressLint;
import android.app.DialogFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.afilon.mayor.v11.R;
import com.afilon.mayor.v11.utils.Consts;

/**
 * Created by BReinosa on 8/18/2017.
 */
public class ThreeButtonFragment extends DialogFragment{
    protected Button firstBtn;
    protected Button secondBtn;
    protected Button thirdBtn;
    protected String message;
    protected TextView questionTv;

    public ThreeButtonFragment() {
        // Empty constructor required for DialogFragment
    }

    private ThreeButtonListener buttonsClickedListener;

    public interface ThreeButtonListener {
        public void onFirstButtonClicked();

        public void onSecondButtonClicked();

        public void onThirdButtonClicked();

    }

    public void setOnButtonsClickedListenerOne(
            ThreeButtonListener listener) {
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
                R.layout.three_button_fragment_layout, container, false);

        Bundle bundle = getArguments();
        String firstLabel = bundle.getString(Consts.FIRST_BTN);
        String secondLabel = bundle.getString(Consts.SECOND_BTN);
        String thirdLabel = bundle.getString(Consts.THRID_BTN);

//        message = bundle.getString("invisible");
//        final int yesIdnex = bundle.getInt("yesIndex");
//        final String questionText = bundle.getString("question");
//        questionTv = (TextView) view.findViewById(R.id.question_textview);
//        questionTv.setText(questionText);

        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getDialog().setCanceledOnTouchOutside(false);
        //------------------------------------------------------
        firstBtn = (Button) view.findViewById(R.id.first_btn);
        setButtonColorGreen(firstBtn);
        firstBtn.setText(firstLabel);
        firstBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                buttonsClickedListener.onFirstButtonClicked();
                dismiss();
            }
        });
        //-----------------------------------------------------
        secondBtn = (Button) view.findViewById(R.id.second_btn);
        setButtonColorGreen(secondBtn);
        secondBtn.setText(secondLabel);
        secondBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                buttonsClickedListener.onSecondButtonClicked();
                dismiss();
            }
        });
        //-----------------------------------------------------
        thirdBtn = (Button) view.findViewById(R.id.third_btn);
        setButtonColorGreen(thirdBtn);
        thirdBtn.setText(thirdLabel);
        thirdBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonsClickedListener.onThirdButtonClicked();
                dismiss();
            }
        });



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
}
