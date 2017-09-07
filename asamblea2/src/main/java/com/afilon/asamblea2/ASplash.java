package com.afilon.asamblea2;


import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.afilon.mayor.v11.activities.SplashActivity;
import com.afilon.mayor.v11.utils.ContextHandler;

/**
 * Created by BReinosa on 4/12/2017.
 */
public class ASplash extends SplashActivity {

    @Override
    protected void goToNextActivity() {
        super.goToNextActivity();
        startActivity(new Intent(ASplash.this, ALogin.class));
        ASplash.this.finish();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ContextHandler.setElectionContext(this.getApplicationContext());
        // add other methods oncreate
//                ((TextView)findViewById(R.id.election_type)).setText("ok, this method works!");
    }



}
