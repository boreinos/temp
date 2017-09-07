package com.afilon.mayor.v11.activities;

import android.app.Activity;
import android.content.Context;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.WindowManager;

/**
 * Created by BReinosa on 4/13/2017.
 */
public class AfilonActivity extends Activity{
    private Context applicationCall;
    protected void goToNextActivity(){


    }
    protected Context getElectionContext(){
        return applicationCall;
    }
    protected void setElectionContext(Context context){
        this.applicationCall = context;
    }
    @Override
    public void onBackPressed() {

    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){
        if((keyCode == KeyEvent.KEYCODE_HOME)){
            return true;
        }
        return super.onKeyDown(keyCode,event);
    }
    @Override
    public void onAttachedToWindow()
    {
//        this.getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG);
        super.onAttachedToWindow();
    }

}
