package com.afilon.mermanager;

import android.content.Intent;
import android.os.Bundle;

import com.afilon.mayor.v11.activities.MerLoginActivity;
import com.afilon.mayor.v11.activities.MerManagerActivity;
import com.afilon.mayor.v11.activities.SplashActivity;
import com.afilon.mayor.v11.utils.ContextHandler;

/**
 * Created by BReinosa on 4/25/2017.
 */
public class MSplash extends SplashActivity {

    @Override
    protected void goToNextActivity() {
        super.goToNextActivity();
        startActivity(new Intent(MSplash.this, MerLoginActivity.class));
        MSplash.this.finish();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ContextHandler.setElectionContext(this.getApplicationContext());
        // add other methods oncreate
//                ((TextView)findViewById(R.id.election_type)).setText("ok, this method works!");
    }
}
