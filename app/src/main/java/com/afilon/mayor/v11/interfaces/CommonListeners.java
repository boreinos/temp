package com.afilon.mayor.v11.interfaces;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

/**
 * Created by BReinosa on 4/18/2017.
 */
public class CommonListeners {
    private final String TAG = "KeyListener:";

    public View.OnKeyListener getAltKeysListener() {
        return new AltKeysListener();
    }

    public View.OnLongClickListener getMouseListener() {
        return new MouseLongClickListener();
    }

    public View.OnTouchListener getOutsideTouchListener() {
        return new DisableOutsideTouch();
    }

    public View.OnTouchListener getDismissListener(){return new DismissTouch();}

    public TextWatcher getMREFormatTextWatcher(EditText name_et, CheckBox present_cb){
        return new DuiFormatTextWatcher(name_et,present_cb);
    }

    private class AltKeysListener implements View.OnKeyListener {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            Log.e(TAG, "kecode: " + String.valueOf(keyCode));
            Log.e(TAG, "event: " + event.toString());
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                if (event.getMetaState() == (KeyEvent.META_SHIFT_ON | KeyEvent.META_SHIFT_LEFT_ON)) {
                    Log.e(TAG, "maybe now shift was consumed");
                    return true;
                }
                switch (keyCode) {
                    case KeyEvent.KEYCODE_APOSTROPHE:
                    case KeyEvent.KEYCODE_DPAD_CENTER:
                    case KeyEvent.KEYCODE_ENTER:
                        Log.e(TAG, "Enter was consumed");
                        return true;
                    case KeyEvent.META_SHIFT_LEFT_ON:
                        Log.e(TAG, "meta shift consumed");
                        return true;
                    case KeyEvent.META_SHIFT_RIGHT_ON:
                        Log.e(TAG, "meta shift consumed");
                        return true;
                    case KeyEvent.KEYCODE_SHIFT_LEFT:
                        Log.e(TAG, "shift was consumed");
                        return true;
                    case KeyEvent.KEYCODE_SHIFT_RIGHT:
                        Log.e(TAG, "Shift was consumed");
                        return true;
                    case KeyEvent.KEYCODE_SPACE:
                        Log.e(TAG, "space consumed");
                        return true;
                    case KeyEvent.KEYCODE_SLASH:
                        Log.e(TAG, "slash consumed");
                        return true;
                    case KeyEvent.KEYCODE_EQUALS:
                        Log.e(TAG, "equal sign consumed");
                        return true;
                    case KeyEvent.KEYCODE_MINUS:
                        Log.e(TAG, "minus sign consumed");
                        return true;
                    case KeyEvent.KEYCODE_PERIOD:
                        Log.e(TAG, "period consumed");
                        return true;
                    case KeyEvent.KEYCODE_COMMA:
                        Log.e(TAG, "comma was consumed");
                        return true;
                    case KeyEvent.KEYCODE_SEMICOLON:
                        Log.e(TAG, "semicolon was consumed");
                        return true;
                    default:
                        break;
                }
            } else if (keyCode == KeyEvent.KEYCODE_SHIFT_LEFT) {
                Log.e(TAG, "now shift was consumed!");
                return true;
            }
            return false;
        }
    }

    private class MouseLongClickListener implements View.OnLongClickListener {

        @Override
        public boolean onLongClick(View v) {
            Log.e(TAG, "mouse was long clicked");
            return true;
        }
    }

    private class DisableOutsideTouch implements View.OnTouchListener {

        @Override
        public boolean onTouch(View v, MotionEvent event) {

            Log.e(TAG, "MASKED ACTION:" + String.valueOf(event.getActionMasked()));
            Log.e(TAG, event.toString());
            if (event.getActionMasked() == MotionEvent.ACTION_OUTSIDE) {
                Log.e(TAG, "outside event handled!");
                return true;
            }
            return false;
        }
    }

    private class DuiFormatTextWatcher implements TextWatcher {
        private EditText mEditText;
        private Button mButton;
        private CheckBox presentCheckBox;
        private EditText nameEditText;
        int realCount = 0;
        boolean replace = false;

        public DuiFormatTextWatcher(EditText name_et, CheckBox present_cb) {
            presentCheckBox = present_cb;
            nameEditText = name_et;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (s.length() == 9 && s.charAt(8) != '-') {
                replace = true;
            }

            if (s.length() > 10) {
                Log.e("CHAR", String.valueOf(s.charAt(8)));
                nameEditText.requestFocus();
            }

        }

        @Override
        public void afterTextChanged(Editable s) {
            if (replace && realCount == 0) {
                realCount++;
                char d = s.charAt(8);
                s.replace(8, 9, "-" + d);
                replace = false;
            }
            realCount = 0;

        }
    }

    private class DuiFormatTextWatcherHon implements TextWatcher {
        private EditText mEditText;
        private Button mButton;
        private CheckBox presentCheckBox;
        private EditText nameEditText;
        int realCount = 0;
        boolean replace = false;
        boolean replace2=false;

        public DuiFormatTextWatcherHon(EditText name_et, CheckBox present_cb) {
            presentCheckBox = present_cb;
            nameEditText = name_et;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (s.length() == 9 && s.charAt(8) != '-') {
                replace = true;
            }

            if (s.length() > 10) {
                Log.e("CHAR", String.valueOf(s.charAt(8)));
                nameEditText.requestFocus();
            }

            if(s.length()==5 && s.charAt(4) !='-'){
                replace =true;
            }
            if(s.length()==10 && s.charAt(9)!='-'){
                replace2=true;
            }



            if (s.length() > 14) {
                mButton.setFocusable(true);
                mEditText.setFocusable(false);
                mEditText.setEnabled(true);
                mButton.setEnabled(true);
            }else{
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
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

    private class DismissTouch implements View.OnTouchListener{

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            return true;
        }
    }


}




