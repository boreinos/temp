package com.afilon.mayor.v11.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.afilon.mayor.v11.R;
import com.afilon.mayor.v11.data.DatabaseAdapterParlacen;
import com.afilon.mayor.v11.utils.Utilities;

public class ExitActivity extends AfilonActivity {
    private Utilities ah = new Utilities(this);
    private DatabaseAdapterParlacen db_adapter = new DatabaseAdapterParlacen(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(com.afilon.mayor.v11.R.layout.activity_exit);

        TextView goodbye_textView = (TextView) findViewById(R.id.goodbye_textView);
        goodbye_textView.setText("Muchas Gracias!\nLos Resultados Se Han Enviado.\nAdios!");


        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                ah.createCustomToast("Cerrando Applicacion ...", "");
                ah.savePreferences("newApplication",true);
                db_adapter.open();
                db_adapter.backupDatabase();
                db_adapter.close();
                finishAffinity();
            }
        }, 5000);

    }

}
