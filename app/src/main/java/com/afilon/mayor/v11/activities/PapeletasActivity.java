package com.afilon.mayor.v11.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.afilon.mayor.v11.R;
import com.afilon.mayor.v11.fragments.TimeWheelFragment;
import com.afilon.mayor.v11.fragments.TimeWheelFragment.OnTimeWheelDialogFragmentListener;
import com.afilon.mayor.v11.interfaces.CommonListeners;
import com.afilon.mayor.v11.model.AppLog;
import com.afilon.mayor.v11.model.CustomKeyboard;
import com.afilon.mayor.v11.model.Escrudata;
import com.afilon.mayor.v11.model.VotingCenter;
import com.afilon.mayor.v11.utils.Consts;
import com.afilon.mayor.v11.utils.UnCaughtException;
import com.afilon.mayor.v11.utils.Utilities;

import kankan.wheel.widget.WheelView;

public class PapeletasActivity extends AfilonActivity implements
        OnTimeWheelDialogFragmentListener {

    private TextView barcode_tv;
    private TextView hora_de_conclucion_tv;
    private EditText papeletasInitialIngresar;
    private EditText papeletasFinalIngresar;
    private EditText cantidadPapeletasFinal;

    private int papInicialInt;
    private int papFinalInt;
    private String papeletaInicialString;
    private String cantidadPapeletasFinalString;
    private String papeletaFinalString;
    private Button papeletasInitialBtn, papeletasFinalBtn, resetBtn;
    protected Context context;
    private TextView min_de_conclucion_tv;

    private Button compareTimes_btn;
    private CustomKeyboard mCustomKeyboard;
    private Utilities ah;
    private VotingCenter vc;
    private Escrudata escrudata;
    private AppLog applog;

    private String submitterOne;
    private Button continuar_btn;
    private Button cantidadPapeletaBtn;
    private boolean isSameQty = false, newStart = true;
    private RelativeLayout papeletasParent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ah = new Utilities(this);
        ah.tabletConfiguration(Build.MODEL, this);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_papeletas);
        //Catch Unexpected Error:
        Thread.setDefaultUncaughtExceptionHandler(new UnCaughtException(
                PapeletasActivity.this));

        //--------- filter listeners ---------------------------------------------------
        CommonListeners listenerHandler = new CommonListeners();
        View.OnKeyListener altkeys = listenerHandler.getAltKeysListener();
        View.OnLongClickListener longClickListener = listenerHandler.getMouseListener();
        //--------- end filter listeners ------------------------------------------------

        final Animation animScale = AnimationUtils.loadAnimation(this, R.anim.scale);

        String deviceBrand = android.os.Build.BRAND;
        String deviceModel = android.os.Build.MODEL;
        Log.d("Device Brand : ", deviceBrand);
        Log.d("Device Model : ", deviceModel);

        papeletasParent = (RelativeLayout) findViewById(R.id.papeletasParent);

        hora_de_conclucion_tv = (TextView) findViewById(R.id.textViewHour);
        min_de_conclucion_tv = (TextView) findViewById(R.id.textViewMin);

        hora_de_conclucion_tv.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                startTimeWheel(1);
//                min_de_conclucion_tv.requestFocus();
            }
        });

        min_de_conclucion_tv.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                startTimeWheel(2);
//                if(min_de_conclucion_tv.getText().length() > 1) {
//                    setButtonColorGreen(compareTimes_btn);
//                }
            }
        });

        min_de_conclucion_tv.addTextChangedListener(tw1);

        compareTimes_btn = (Button) findViewById(R.id.button414);
        ah.setButtonColorRed(compareTimes_btn);

        cantidadPapeletaBtn = (Button) findViewById(R.id.buttonLast);
        ah.setButtonColorRed(cantidadPapeletaBtn);

        resetBtn = (Button) findViewById(R.id.reset_btn);
        ah.setButtonColorAmber(resetBtn);
        resetBtn.setVisibility(View.GONE);
        ah.setButtonColorRed(resetBtn);

        barcode_tv = (TextView) findViewById(R.id.textView32);
        final TextView municipio_tv = (TextView) findViewById(R.id.textView22);
        final TextView departamento_tv = (TextView) findViewById(R.id.textView72);
        final TextView jrv_tv = (TextView) findViewById(R.id.textView35);
        final TextView votecenter_tv = (TextView) findViewById(R.id.textView62);

        final EditText papeletasInitialCantidad = (EditText) findViewById(R.id.textView112);
        papeletasInitialCantidad.setOnLongClickListener(longClickListener);
        papeletasInitialCantidad.setKeyListener(DigitsKeyListener.getInstance("0123456789"));

        papeletasInitialIngresar = (EditText) findViewById(R.id.textView113);
        papeletasInitialIngresar.setOnLongClickListener(longClickListener);
        papeletasInitialIngresar.setKeyListener(DigitsKeyListener.getInstance("0123456789"));

        final EditText papeletasFinalCantidad = (EditText) findViewById(R.id.textView122);
        papeletasFinalCantidad.setOnLongClickListener(longClickListener);
        papeletasFinalCantidad.setKeyListener(DigitsKeyListener.getInstance("0123456789"));

        papeletasFinalIngresar = (EditText) findViewById(R.id.textView123);
        papeletasFinalIngresar.setOnLongClickListener(longClickListener);
        papeletasFinalIngresar.setKeyListener(DigitsKeyListener.getInstance("0123456789"));

        final EditText cantidadPapeletasCantidad = (EditText) findViewById(R.id.textView122Last);
        cantidadPapeletasCantidad.setOnLongClickListener(longClickListener);
        cantidadPapeletasCantidad.setKeyListener(DigitsKeyListener.getInstance("0123456789"));

        cantidadPapeletasFinal = (EditText) findViewById(R.id.textView123Last);
        cantidadPapeletasFinal.setKeyListener(DigitsKeyListener.getInstance("0123456789"));
        cantidadPapeletasFinal.setOnLongClickListener(longClickListener);

        papeletasInitialIngresar.addTextChangedListener(tw2);
        papeletasFinalIngresar.addTextChangedListener(tw3);
        cantidadPapeletasFinal.addTextChangedListener(tw4);

        mCustomKeyboard = new CustomKeyboard(this, R.id.keyboardview,
                R.xml.tenhexkbd);

        mCustomKeyboard.registerEditText(R.id.textView112);
        mCustomKeyboard.registerEditText(R.id.textView113);

        mCustomKeyboard.registerEditText(R.id.textView122);
        mCustomKeyboard.registerEditText(R.id.textView123);
        //CARLOS:
        mCustomKeyboard.registerEditText(R.id.textView122Last);
        mCustomKeyboard.registerEditText(R.id.textView123Last);

        ah.enableEditText(papeletasInitialCantidad,false);
        ah.enableEditText(papeletasInitialIngresar,false);
        ah.enableEditText(papeletasFinalCantidad,false);
        ah.enableEditText(papeletasFinalIngresar,false);
        ah.enableEditText(cantidadPapeletasCantidad,false);
        ah.enableEditText(cantidadPapeletasFinal,false);

        //CARLOS: 2016-09-12
        //disable text while green button has not been pressed

        final TextView totalPapeletasCalculado = (TextView) findViewById(R.id.textView132);
        final TextView totalPapeletasIngresar = (TextView) findViewById(R.id.textView133);
        ah.enableTextView(totalPapeletasCalculado,false);
        ah.enableTextView(totalPapeletasIngresar,false);

        final TextView totalPapeletasTxt = (TextView) findViewById(R.id.textView131);
        totalPapeletasCalculado.setVisibility(View.GONE); //View.GONE
        totalPapeletasIngresar.setVisibility(View.GONE); //View.GONE
        totalPapeletasTxt.setVisibility(View.GONE);

        papeletasInitialBtn = (Button) findViewById(R.id.button114);
        ah.setButtonColorRed(papeletasInitialBtn);
        papeletasFinalBtn = (Button) findViewById(R.id.button124);
        ah.setButtonColorRed(papeletasFinalBtn);

        papeletaInicialString = "0";
        papeletaFinalString = "0";
        cantidadPapeletasFinalString = "0";

        Bundle b = getIntent().getExtras();
        vc = b.getParcelable("com.afilon.tse.votingcenter");
        submitterOne = b.getString("submitterOne");
        ah.saveCurrentScreen(this.getClass(),b);

        votecenter_tv.setText(vc.getVoteCenterString());
        municipio_tv.setText(vc.getMunicipioString());
        departamento_tv.setText(vc.getDepartamentoString());
        // barcode_tv.setText(vc.getBarcodeString());
        String barcodesaved = ah.loadPreferencesString("barcodeSaved");
        barcode_tv.setText(barcodesaved);
        jrv_tv.setText(vc.getJrvString());

        escrudata = new Escrudata(vc.getJrvString());
        escrudata.setBarcode(vc.getBarcodeString());
        escrudata.setSubmitterOne(submitterOne);
        escrudata.setSubmitterTwo(submitterOne);



        //CARLOS: 2014-09-18
        // Implementing LOG transaction for any change in data
        applog = new AppLog(vc.getJrvString());


        papeletasParent.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("LongLogTag")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {

                    papeletasInitialCantidad.requestFocus();
                    mCustomKeyboard.showCustomKeyboard(papeletasInitialCantidad);
                    Log.e(">>> DEBUG Enabling custom Keyboard", "");
                    // Do what you want
                    return true;
                }
                return false;
            }
        });

        compareTimes_btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {

                if (!hora_de_conclucion_tv.getText().toString().equals("")
                        && hora_de_conclucion_tv
                        .getText()
                        .toString()
                        .equals(min_de_conclucion_tv.getText()
                                .toString())) {

                    //CARLOS: 2016-09-12
                    ah.enableEditText(papeletasInitialCantidad,true);
                    ah.enableEditText(papeletasInitialIngresar,true,papeletasInitialCantidad.getId());
                    papeletasInitialCantidad.requestFocus();
                    //lock view:
                   ah.enableTextView(hora_de_conclucion_tv,false);
                    ah.enableTextView(min_de_conclucion_tv,false);
//                    hora_de_conclucion_tv.setEnabled(false);
//                    min_de_conclucion_tv.setEnabled(false);
                    //lock button
                    ah.setButtonColorRed(compareTimes_btn);

                } else {
//                    ah.createCustomToast("Ingresar Hora", "de Conclusion");
                    ah.createCustomToast("Las Horas No Coinciden ", "Re - Ingresa");
//                    hora_de_conclucion_tv.requestFocus();
                }

            }
        });

        papeletasInitialIngresar.setOnFocusChangeListener(new OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        if (hasFocus) {
                            if (!papeletasInitialCantidad.getText().toString()
                                    .equals("")) {

                                int x = ah.parseInt(papeletasInitialCantidad
                                        .getText().toString(), 0);

                                if (x > 0) {
                                    papeletaInicialString = papeletasInitialCantidad
                                            .getText().toString();
                                    papeletasInitialCantidad.setText("****");
                                    mCustomKeyboard.showCustomKeyboard(v);
//                                    setButtonColorGreen(papeletasInitialBtn);
                                }

                            } else {

                            }
                        }
                    }
                });


        papeletasInitialBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                if (papeletaInicialString != null
                        && papeletaInicialString
                        .equals(papeletasInitialIngresar.getText()
                                .toString())) {

                    //CARLOS: 2016-09-12
                    ah.enableEditText(papeletasFinalCantidad,true);
                    ah.enableEditText(papeletasFinalIngresar,true,papeletasFinalCantidad.getId());
                    ah.enableEditText(papeletasInitialCantidad,false);
                    ah.enableEditText(papeletasInitialIngresar,false);

                    papeletasFinalCantidad.requestFocus();
                    papeletasInitialCantidad.setText(papeletaInicialString);
                    papeletasInitialIngresar.setText(papeletaInicialString);
                    escrudata.setPapeletasInicio(papeletaInicialString);
                    papInicialInt = ah.parseInt(papeletaInicialString, 0);

                    ah.setButtonColorRed(papeletasInitialBtn);
                } else {
                    papeletasInitialCantidad.setText("");
                    papeletasInitialIngresar.setText("");
                    papeletasInitialCantidad.requestFocus();
                }
            }
        });

        papeletasFinalIngresar.setOnFocusChangeListener(new OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        if (hasFocus == true) {

                            if (!papeletasFinalCantidad.getText().toString()
                                    .equals("")) {
                                int x = ah.parseInt(papeletasFinalCantidad
                                        .getText().toString(), 0);
                                if (x > 0) {
                                    papeletaFinalString = papeletasFinalCantidad
                                            .getText().toString();
                                    papeletasFinalCantidad.setText("****");
                                    mCustomKeyboard.showCustomKeyboard(v);
//                                    setButtonColorGreen(papeletasFinalBtn);
                                }

                            } else {

                            }

                        }
                    }
                });

        papeletasFinalBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                if (papeletaFinalString != null
                        && papeletaFinalString.equals(papeletasFinalIngresar
                        .getText().toString())) {

                    //CARLOS: 2016-09-12
                    ah.enableEditText(cantidadPapeletasCantidad,true);
                    ah.enableEditText(cantidadPapeletasFinal,true,cantidadPapeletasCantidad.getId());
                    ah.enableEditText(papeletasFinalCantidad,false);
                    ah.enableEditText(papeletasFinalIngresar,false);

                    papeletasFinalCantidad.setText(papeletaFinalString);
                    papeletasFinalIngresar.setText(papeletaFinalString);

                    escrudata.setPapeletasFinal(papeletaFinalString);
                    papFinalInt = ah.parseInt(papeletaFinalString, 0);

                    cantidadPapeletasCantidad.requestFocus();
                    ah.setButtonColorRed(papeletasFinalBtn);

                    if ((1 + papFinalInt - papInicialInt) > 0) {

                        // hide the keyboard
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(
                                papeletasFinalIngresar.getWindowToken(), 0);

                        totalPapeletasCalculado.setText(""
                                + (1 + papFinalInt - papInicialInt));
                        totalPapeletasIngresar.setText(""
                                + (1 + papFinalInt - papInicialInt));

                    } else {

                        ah.createCustomLongToast("Error en Digitacion",
                                "La Papeleta Final es Menor que la Inicial",
                                4000);

                        //TextViews CLEAR THEM UP
                        papeletasInitialCantidad.setText("");
                        papeletasInitialIngresar.setText("");
                        papeletasFinalCantidad.setText("");
                        papeletasFinalIngresar.setText("");

                        //TextViews ENABLE AGAIN
                        papeletasInitialCantidad.setEnabled(true);
                        papeletasInitialIngresar.setEnabled(true);
//                        papeletasFinalCantidad.setEnabled(true);
//                        papeletasFinalIngresar.setEnabled(true);

                        //TextViews ENABLE TOUCH MODE
                        papeletasInitialCantidad.setFocusableInTouchMode(true);
                        papeletasInitialIngresar.setFocusableInTouchMode(true);
//                        papeletasFinalCantidad.setFocusableInTouchMode(true);
//                        papeletasFinalIngresar.setFocusableInTouchMode(true);

                        //TextView SET FOCUS ON FIRST TEXTVIEW
                        papeletasInitialCantidad.requestFocus();
                    }

                } else {
                    papeletasFinalCantidad.setText("");
                    papeletasFinalIngresar.setText("");
                    papeletasFinalCantidad.requestFocus();
                }
            }
        });
//CARLOS: //////////////////////////////////////////////////////////////////////
        cantidadPapeletasFinal.setOnFocusChangeListener(new OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        if (hasFocus) {

                            if (!cantidadPapeletasCantidad.getText().toString()
                                    .equals("")) {
                                int x = ah.parseInt(cantidadPapeletasCantidad
                                        .getText().toString(), 0);
                                if (x > 0) {
                                    cantidadPapeletasFinalString = cantidadPapeletasCantidad
                                            .getText().toString();
                                    cantidadPapeletasCantidad.setText("****");
                                    mCustomKeyboard.showCustomKeyboard(v);
//                                    setButtonColorGreen(cantidadPapeletaBtn);
                                }

                            } else {

                            }

                        }
                    }
                });

        cantidadPapeletaBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                totalPapeletasCalculado.setVisibility(View.VISIBLE);
                totalPapeletasIngresar.setVisibility(View.VISIBLE);
                totalPapeletasTxt.setVisibility(View.VISIBLE);
//				setButtonColorGreen(continuar_btn);
                cantidadPapeletasFinal.setFocusable(false);
                cantidadPapeletasCantidad.setFocusable(false);
                cantidadPapeletasCantidad.setText(cantidadPapeletasFinalString);

                // hide the keyboard
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(
                        papeletasFinalIngresar.getWindowToken(), 0);

                //continuar_btn.startAnimation(animScale);
                //http://android-er.blogspot.com/2012/02/apply-animation-on-button.html

                Boolean entryMatch = isSameQuantityInActas(cantidadPapeletasCantidad, cantidadPapeletasFinal);
                isSameQty = isSameQuantityInActas(cantidadPapeletasFinal, totalPapeletasCalculado);
                if (isSameQty && entryMatch) {
                    continuar_btn.setVisibility(View.VISIBLE);
                    ah.setButtonColorGreen(continuar_btn);
                    resetBtn.setVisibility(View.GONE);
                    ah.setButtonColorRed(resetBtn);

                    //Grey out EditText and TextView
                    hora_de_conclucion_tv.setTextColor(Color.parseColor("#999999"));
                    min_de_conclucion_tv.setTextColor(Color.parseColor("#999999"));
                    papeletasInitialCantidad.setTextColor(Color.parseColor("#999999"));
                    papeletasInitialIngresar.setTextColor(Color.parseColor("#999999"));
                    papeletasFinalCantidad.setTextColor(Color.parseColor("#999999"));
                    papeletasFinalIngresar.setTextColor(Color.parseColor("#999999"));
                    cantidadPapeletasCantidad.setTextColor(Color.parseColor("#999999"));
                    cantidadPapeletasFinal.setTextColor(Color.parseColor("#999999"));
                    totalPapeletasIngresar.setTextColor(Color.parseColor("#999999"));
                    totalPapeletasCalculado.setTextColor(Color.parseColor("#999999"));


                    //Disable EditText and TextView and Buttons
                    ah.enableTextView(hora_de_conclucion_tv,false);
                    ah.enableTextView(min_de_conclucion_tv,false);
                    ah.enableEditText(papeletasInitialCantidad,false);
                    ah.enableEditText(papeletasInitialIngresar,false);
                    ah.enableEditText(papeletasFinalCantidad,false);
                    ah.enableEditText(papeletasFinalIngresar,false);
                    ah.enableEditText(cantidadPapeletasCantidad,false);
                    ah.enableEditText(cantidadPapeletasFinal,false);
                    ah.setButtonColorRed(compareTimes_btn);
                    ah.setButtonColorRed(papeletasInitialBtn);
                    ah.setButtonColorRed(papeletasFinalBtn);
                    ah.setButtonColorRed(cantidadPapeletaBtn);

                } else {
                    ah.createCustomToast(
                            "Cantidad de Papeletas no coincide con Papeletas Calculadas",
                            "Por favor intente de nuevo");
                    ah.setButtonColorRed(continuar_btn);
                    //continuar_btn.setVisibility(View.GONE);
                    resetBtn.setVisibility(View.VISIBLE);
                    ah.setButtonColorGreen(resetBtn);
                }
            }
        });

        resetBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                newStart = true;
                papeletasInitialCantidad.setText("");
                papeletasInitialIngresar.setText("");
                papeletasFinalCantidad.setText("");
                papeletasFinalIngresar.setText("");
                cantidadPapeletasCantidad.setText("");
                cantidadPapeletasFinal.setText("");
                ah.setButtonColorRed(papeletasFinalBtn);
                ah.setButtonColorRed(papeletasInitialBtn);
                ah.setButtonColorRed(cantidadPapeletaBtn);

                ah.enableEditText(papeletasInitialCantidad,true);
                ah.enableEditText(papeletasInitialIngresar,true,papeletasInitialCantidad.getId());
                ah.enableEditText(papeletasFinalCantidad,false);
                ah.enableEditText(papeletasFinalIngresar,false);
                ah.enableEditText(cantidadPapeletasCantidad,false);
                ah.enableEditText(cantidadPapeletasFinal,false);

                papeletasInitialCantidad.requestFocus();

                totalPapeletasCalculado.setVisibility(View.GONE); // View.GONE
                totalPapeletasIngresar.setVisibility(View.GONE); // View.GONE
                totalPapeletasTxt.setVisibility(View.GONE);
                resetBtn.setVisibility(View.GONE);
                ah.setButtonColorRed(resetBtn);

            }
        });
        /////CARLOS ////////////////////////////////////////////////

        continuar_btn = (Button) findViewById(R.id.procedo_btn);
        ah.setButtonColorRed(continuar_btn);
        continuar_btn.setVisibility(View.GONE);
        continuar_btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (!totalPapeletasIngresar.getText().toString().equals("")) {

                    escrudata.setPapeletasTotal(totalPapeletasIngresar
                            .getText().toString());
                    escrudata.setHoraCierre(hora_de_conclucion_tv.getText()
                            .toString());

                    Bundle b = new Bundle();
                    b.putParcelable("com.afilon.tse.votingcenter", vc);
                    b.putParcelable("com.afilon.tse.escrudata", escrudata);
//                    b.putParcelable("com.afilon.assembly.applog", applog); //CARLOS: 2014-09-18
                    Intent intentOne = new Intent(PapeletasActivity.this,
                            Consts.EMPTYTABLEACT);
                    intentOne.putExtras(b);

                    startActivity(intentOne);
                    finish();

                } else {
                    ah.createCustomToast("Completar todos los campos",
                            "para poder proceder!");
                }
            }
        });

        ah.enableTextView(hora_de_conclucion_tv,true);
        ah.enableTextView(min_de_conclucion_tv,true);
        ah.textViewNextFocus(min_de_conclucion_tv,hora_de_conclucion_tv.getId());

        OnFocusChangeListener timeFocus= new OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus&&!newStart){
                    v.performClick();
                }
                newStart = false;
            }
        };

        hora_de_conclucion_tv.setOnFocusChangeListener(timeFocus);
        min_de_conclucion_tv.setOnFocusChangeListener(timeFocus);



//        hora_de_conclucion_tv.requestFocus();
    }

    private static String pad(int c) {
        if (c >= 10)
            return String.valueOf(c);
        else
            return "0" + String.valueOf(c);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.jrv, menu);
        return true;
    }

    public void startTimeWheel(int yesIndex) {
        TimeWheelFragment newTimeWheelFragment = new TimeWheelFragment();
        Bundle bndl = new Bundle();
        bndl.putInt("yesIndex", yesIndex);
        newTimeWheelFragment.setArguments(bndl);
        newTimeWheelFragment.show(getFragmentManager(), "timeWheelFragment");

    }

    @Override
    public void setCurrentTime(int x, int currentHour, int currentMin,
                               int amPmInt) {

        String ampmString = "";
        switch (x) {
            case 1:
                if (amPmInt == 1) {
                    ampmString = "PM";
                } else if (amPmInt == 0) {
                    ampmString = "AM";
                }
                hora_de_conclucion_tv.setText(pad(currentHour) + " : "
                        + pad(currentMin) + " " + ampmString);
                hora_de_conclucion_tv.setTypeface(Typeface.DEFAULT_BOLD);
                break;
            case 2:
                if (amPmInt == 1) {
                    ampmString = "PM";
                } else if (amPmInt == 0) {
                    ampmString = "AM";
                }
                min_de_conclucion_tv.setText(pad(currentHour) + " : "
                        + pad(currentMin) + " " + ampmString);
                min_de_conclucion_tv.setTypeface(Typeface.DEFAULT_BOLD);
                break;

            default:
                break;
        }
    }

    private boolean isSameQuantityInActas(EditText edFirst, TextView edSecond) {
        int first, second;
        try {
            first = Integer.parseInt(edFirst.getText().toString());
            second = Integer.parseInt(edSecond.getText().toString());
        } catch (NumberFormatException e) {
            e.printStackTrace();
            // mismatch:
            first = 3;
            second = 4;
            ah.createCustomToast("Cantidad No Fue Numerica!");
        }

        return (first == second) ? true : false;
    }


    @Override
    public void onBackPressed() {
        Log.d("Papeletas Activity", "onBackPressed Called");
        Intent setIntent = new Intent(Intent.ACTION_MAIN);
        setIntent.addCategory(Intent.CATEGORY_HOME);
        setIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(setIntent);

/*        if (mCustomKeyboard.isCustomKeyboardVisible()) {
            mCustomKeyboard.hideCustomKeyboard();
        } else {
            Log.d("Papeletas Activity", "onBackPressed Called");
            Intent setIntent = new Intent(Intent.ACTION_MAIN);
            setIntent.addCategory(Intent.CATEGORY_HOME);
            setIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(setIntent);
        }*/
    }

    TextWatcher tw1 = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//			Log.d("tw1 beforeTextChanged", "x");
        }

        @SuppressLint("LongLogTag")
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (min_de_conclucion_tv.getText().length() > 1) {
                ah.setButtonColorGreen(compareTimes_btn);
                ah.textViewNextFocus(min_de_conclucion_tv,compareTimes_btn.getId());
            } else {
                ah.setButtonColorRed(compareTimes_btn);
                ah.textViewNextFocus(min_de_conclucion_tv,hora_de_conclucion_tv.getId());
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
            Log.d("tw1 afterTextChanged", "x");

        }
    };

    TextWatcher tw2 = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//			Log.d("tw1 beforeTextChanged", "x");
        }

        @SuppressLint("LongLogTag")
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (papeletasInitialIngresar.getText().length() > 0) {
                ah.setButtonColorGreen(papeletasInitialBtn);
                ah.editTextNextFocus(papeletasInitialIngresar, papeletasInitialBtn.getId());
            } else {
                ah.setButtonColorRed(papeletasInitialBtn);
                ah.editTextNextFocus(papeletasInitialIngresar,R.id.textView112);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
            Log.d("tw1 afterTextChanged", "x");

        }
    };

    TextWatcher tw3 = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//			Log.d("tw1 beforeTextChanged", "x");
        }

        @SuppressLint("LongLogTag")
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (papeletasFinalIngresar.getText().length() > 0) {
                ah.setButtonColorGreen(papeletasFinalBtn);
                ah.editTextNextFocus(papeletasFinalIngresar,papeletasFinalBtn.getId());
            } else {
                ah.setButtonColorRed(papeletasFinalBtn);
                ah.editTextNextFocus(papeletasFinalIngresar,R.id.textView122);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
            Log.d("tw1 afterTextChanged", "x");

        }
    };

    TextWatcher tw4 = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//			Log.d("tw1 beforeTextChanged", "x");
        }

        @SuppressLint("LongLogTag")
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (cantidadPapeletasFinal.getText().length() > 0) {
                ah.setButtonColorGreen(cantidadPapeletaBtn);
                ah.editTextNextFocus(cantidadPapeletasFinal,cantidadPapeletaBtn.getId());
            } else {
                ah.setButtonColorRed(cantidadPapeletaBtn);
                ah.editTextNextFocus(cantidadPapeletasFinal,R.id.textView122Last);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
            Log.d("tw1 afterTextChanged", "x");

        }
    };
}
