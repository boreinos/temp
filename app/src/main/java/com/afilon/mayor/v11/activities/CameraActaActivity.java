package com.afilon.mayor.v11.activities;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afilon.mayor.v11.R;
import com.afilon.mayor.v11.data.DatabaseAdapterParlacen;
import com.afilon.mayor.v11.fragments.TwoButtonDialogFragment;
import com.afilon.mayor.v11.fragments.TwoButtonDialogFragment.OnTwoButtonDialogFragmentListener;
import com.afilon.mayor.v11.model.Escrudata;
import com.afilon.mayor.v11.model.Party;
import com.afilon.mayor.v11.model.VotingCenter;
import com.afilon.mayor.v11.utils.Consts;
import com.afilon.mayor.v11.utils.UnCaughtException;
import com.afilon.mayor.v11.utils.Utilities;

public class CameraActaActivity extends AfilonActivity implements
        SurfaceHolder.Callback, OnTwoButtonDialogFragmentListener{
//    OnTwoButtonDialogEditTextFragmentListenerX
    private static final String CLASS_TAG = "CameraActaActivity";
    private VotingCenter vc;
    private SurfaceView preview = null;
    private ImageView imageview = null;
    private SurfaceHolder previewHolder = null;
    private Camera camera = null;
    private boolean isFinalizeBtnEnabled;
    private boolean isDescartarBtnEnabled;
    private boolean isTakePictureBtnEnabled;
    private boolean isRechesarBtnEnabled;
    private boolean isAceptarBtnEnabled;
    private int iNumbOfMaxPicturesAllowed = 25;//todo
    protected String actaFileName;
    private Escrudata escrudata;
    private TextView pictureNmb_tv;
    private int currentPictureNmb;
    private int totalNumberOfImagePics;
    private int GrandTotalOfPictures = 0;
    private int totalTimesAddMorePicBtnWasPressed = 0;
    private Utilities ah;
//    private TwoButtonDialogEditTextFragmentX twoBtnDialogFragmentX;
    private TwoButtonDialogFragment twoBtnDialogFragmentX;
    private TwoButtonDialogFragment twoBtnDialogFrag;
    private char[] alphabetArray;
    private Button nextBtn;
    private Button aceptarBtn;
    private Button rejectBtn;
    private Button takePicBtn;
    private Button addMorePicBtn;
    private Button descartarPicBtn;
    private String actaSigCount;
    private int iDescartar = 0;
    private static final String TAG = "ActaActivity";
    private int iHoldLastActa;
    private List<Camera.Size> mSupportedPreviewSizes;
    private DatabaseAdapterParlacen db_adapter;
    private ArrayList<Party> partyArrayList;
    private boolean isDirectVote;

    @SuppressLint("LongLogTag")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ah = new Utilities(CameraActaActivity.this);
        ah.tabletConfiguration(Build.MODEL, this);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_camera);

        Thread.setDefaultUncaughtExceptionHandler(new UnCaughtException(CameraActaActivity.this));
        isDirectVote = getResources().getString(R.string.voteType).equals("DIRECT");

        String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

        alphabetArray = alphabet.toCharArray();

        try {
            preview = (SurfaceView) findViewById(R.id.preview);
            imageview = (ImageView) findViewById(R.id.imagePreview);
            previewHolder = preview.getHolder();
            previewHolder.addCallback(this);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)
                previewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        } catch (Exception e){ //catch (Throwable t) {
           ah.createCustomToast("Please connect Camera or Make sure Camera is allowed");
//            Log.d(CLASS_TAG, "Exception in saving picture", t);

        }

        TextView votecenter_tv = (TextView) findViewById(R.id.vote_center);
        TextView municipio_tv = (TextView) findViewById(R.id.textView13);
        TextView departamento_tv = (TextView) findViewById(R.id.textView15);
        TextView barcode_tv = (TextView) findViewById(R.id.textView23);
        TextView jvr_tv = (TextView) findViewById(R.id.textView25);

        currentPictureNmb = 0;
        totalNumberOfImagePics = 0;
        Log.i("Value of totalNumberOfImagePics INSIDE of onCreate",
                String.valueOf(totalNumberOfImagePics));

        Bundle b = getIntent().getExtras();
        if (b != null) {
            vc = b.getParcelable("com.afilon.tse.votingcenter");
            escrudata = b.getParcelable("com.afilon.tse.escrudata");
            actaSigCount = b.getString("actaSignatureCount");
            //CARLOS: 2016-10-11
            //Calculate total pictures based on
            // Total Pictures = Total Parties + 1
            db_adapter = new DatabaseAdapterParlacen(this);
            db_adapter.open();
            partyArrayList = db_adapter.getParlacenPartiesArrayList(vc.getPref_election_id());
            if (!isDirectVote) {
                totalNumberOfImagePics = partyArrayList.size() + 1;
                if (totalNumberOfImagePics > iNumbOfMaxPicturesAllowed) {
                    totalNumberOfImagePics = iNumbOfMaxPicturesAllowed;
                }
            } else {
                totalNumberOfImagePics = 1;
            }


            //Print out how many parties found
            Log.e(CLASS_TAG + " >>> Total Parties ", String.valueOf(partyArrayList.size()));
            Log.e(CLASS_TAG + " >>> Total Pictures ", String.valueOf(currentPictureNmb));
        }
        ah.saveCurrentScreen(this.getClass(),b);

        votecenter_tv.setText(vc.getVoteCenterString());
        municipio_tv.setText(vc.getMunicipioString());
        departamento_tv.setText(vc.getDepartamentoString());
        barcode_tv.setText(ah.loadPreferencesString("barcodeSaved"));
        jvr_tv.setText(vc.getJrvString());


        // file name for the acta image file
        actaFileName = vc.getJrvString() + getResources().getString(R.string.imageType) + "A";
        pictureNmb_tv = (TextView) findViewById(R.id.textboxleft);
        pictureNmb_tv.setText("");

        isFinalizeBtnEnabled = false;
        isTakePictureBtnEnabled = true;
        isRechesarBtnEnabled = false;
        isAceptarBtnEnabled = false;
        isDescartarBtnEnabled = true;

        /** FINALIZAR   */
        nextBtn = (Button) findViewById(R.id.next_btn);
        ah.setButtonColorRed(nextBtn);
        nextBtn.setOnClickListener(closePictureActivity());
        //--------------------------------------------------------
        /** IMAGEN   */
        takePicBtn = (Button) findViewById(R.id.acta_btn);
        ah.setButtonColorGreen(takePicBtn);
        takePicBtn.setOnClickListener(takePicture());
        //--------------------------------------------------------
        /** RECHAZAR */
        rejectBtn = (Button) findViewById(R.id.reject_btn);
        ah.setButtonColorRed(rejectBtn);
        rejectBtn.setOnClickListener(rejectPictureTaken());
        //---------------------------------------------------------
        /** What does descartar do? */
        descartarPicBtn = (Button) findViewById(R.id.descartar_btn);

        /** ACEPTAR */
        aceptarBtn = (Button) findViewById(R.id.aceptar_btn);
        ah.setButtonColorRed(aceptarBtn);
        aceptarBtn.setOnClickListener(acceptPictureTaken());

        /** ANADIR  */
        addMorePicBtn = (Button) findViewById(R.id.addMore_btn);
        ah.setButtonColorAmber(addMorePicBtn);
        addMorePicBtn.setOnClickListener(addMorePictures());

        /** DESCARTAR  */
        descartarPicBtn = (Button) findViewById(R.id.descartar_btn);
        ah.setButtonColorAmber(descartarPicBtn);
        descartarPicBtn.setOnClickListener(discardRemainingPictures());

        //CARLOS: 2016-10-11
        //Diaglog not longer needed, since we are getting
        // Total Pictures = Total Parties + 1
//        createDialogEditText("INGRESAR CANTIDAD DE FOLIOS EN ACTA", 1);

        // Display IMAGE ID before take picture
        pictureNmb_tv.setText("" + String.valueOf(alphabetArray[currentPictureNmb]));
        Log.e("Display IMAGE ID before take picture", "currentPictureNmb " +
                String.valueOf(currentPictureNmb));
    }

    //CARLOS: 2016-10-12
    private void setCameraVariables(int numeroOfPics) {
        totalNumberOfImagePics = numeroOfPics;
        // CARLOS: 2014-09-23
        Log.d("PICTURES", String.valueOf(numeroOfPics));

        Log.d("TOTAL PICTURES", String.valueOf(totalNumberOfImagePics));
        pictureNmb_tv.setText("" + String.valueOf(alphabetArray[currentPictureNmb]));
        ah.savePreferences("totalNumberOfPictures", totalNumberOfImagePics);
        // CARLOS: Create a grand total of pictures
        GrandTotalOfPictures += totalNumberOfImagePics;
        ah.savePreferences("GrandTotalOfPictures", GrandTotalOfPictures);

        addMorePicBtn.setVisibility(View.GONE);
        pictureNmb_tv.setVisibility(View.VISIBLE);
    }

    private OnClickListener closePictureActivity() {
        return new OnClickListener() {
            @Override
            public void onClick(View v) {
                //if (isFinalizeBtnEnabled) {
                Bundle b = new Bundle();
                ah.savePreferences("GrandTotalOfPictures", currentPictureNmb);
                b.putParcelable("com.afilon.tse.votingcenter", vc);
                b.putParcelable("com.afilon.tse.escrudata", escrudata);
                b.putString("actaSignatureCount", actaSigCount);
                Intent search = new Intent(CameraActaActivity.this, Consts.LASTACT);
                search.putExtras(b);
                startActivity(search);
                finish();
                //}
            }
        };

    }

    private OnClickListener takePicture() {
        return new OnClickListener() {
            @Override
            public void onClick(View v) {
                /** take preview of image */
                findViewById(R.id.warningMessage).setVisibility(View.VISIBLE);
                camera.autoFocus(autoFocusCallback);
                ah.setButtonColorRed(takePicBtn);
            }
        };
    }

    private OnClickListener rejectPictureTaken() {
        return new OnClickListener() {
            @Override
            public void onClick(View v) {
                //if (isRechesarBtnEnabled) {
                /** release preview, start new preview */
                preview.setVisibility(SurfaceView.VISIBLE);
                imageview.setVisibility(ImageView.GONE);
                clearImageCahce(imageview);
                camera.startPreview();

                ah.setButtonColorGreen(takePicBtn);
                ah.setButtonColorRed(aceptarBtn);
                ah.setButtonColorRed(rejectBtn);
                //}
            }
        };
    }

    private OnClickListener acceptPictureTaken() {
        return new OnClickListener() {
            @SuppressLint("LongLogTag")
            @Override
            public void onClick(View v) {
                preview.setVisibility(SurfaceView.VISIBLE);
                imageview.setVisibility(ImageView.GONE);
                clearImageCahce(imageview);

                Log.e(CLASS_TAG + " currentPictureNmb", String.valueOf(currentPictureNmb));
                Log.e(CLASS_TAG + " totalNumberOfImagePics", String.valueOf(totalNumberOfImagePics));
                Log.e(CLASS_TAG + " totalTimesAddMorePicBtnWasPressed", String.valueOf(totalTimesAddMorePicBtnWasPressed));

                //increment the current picture and pull next letter albel.
                currentPictureNmb++;

                //descartar counter
                iDescartar++;
                if ((currentPictureNmb + 1) <= (totalNumberOfImagePics)) {
                    pictureNmb_tv.setText("" + String.valueOf(alphabetArray[currentPictureNmb]));
                    Log.d("TO TAKE LESS TOTAL",
                            String.valueOf(currentPictureNmb)
                                    + " ---"
                                    + String.valueOf(totalNumberOfImagePics));

                    actaFileName = vc.getJrvString() + getResources().getString(R.string.imageType) + String.valueOf(alphabetArray[currentPictureNmb]);
                    camera.startPreview();
                    if (currentPictureNmb == totalNumberOfImagePics) {
                        ah.setButtonColorRed(takePicBtn);
                        ah.setButtonColorGreen(nextBtn);
                    } else {
                        ah.setButtonColorGreen(takePicBtn);
                    }
                    ah.setButtonColorRed(aceptarBtn);
                    ah.setButtonColorRed(rejectBtn);
                } else {
                    // Log.i("IF...ELSE Statement for CurrentPictureNmb",
                    // String.valueOf(currentPictureNmb));
                    pictureNmb_tv.setText("");
                    isFinalizeBtnEnabled = true;
                    // addMorePicBtn.setVisibility(View.VISIBLE);
                    ah.setButtonColorGreen(nextBtn);
                    ah.setButtonColorRed(takePicBtn);
                    ah.setButtonColorRed(aceptarBtn);
                    ah.setButtonColorRed(rejectBtn);
                }
            }
        };
    }

    private OnClickListener addMorePictures() {
        return new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (nextBtn.isEnabled()) {
                    createDialog("¿DESEA AGREGAR MAS?", 7);
                }
            }
        };
    }

    private OnClickListener discardRemainingPictures() {
        return new OnClickListener() {
            @Override
            public void onClick(View v) {
//                if (isDescartarBtnEnabled) {
                iHoldLastActa = currentPictureNmb;
                Log.d("DESCARTAR", String.valueOf(iHoldLastActa));
                pictureNmb_tv.setVisibility(View.INVISIBLE);
                ah.setButtonColorGreen(nextBtn);
                ah.setButtonColorRed(takePicBtn);
                addMorePicBtn.setVisibility(View.VISIBLE);
                // setButtonColorRed(descartarPicBtn);
                descartarPicBtn.setVisibility(View.GONE);
                //CARLOS: Without this line, we got a runtime error regarding Null value
                if (currentPictureNmb < totalNumberOfImagePics) {
                    ah.savePreferences("GrandTotalOfPictures", currentPictureNmb);
                }
//                }
            }
        };
    }

    public void createDialog(String msg, int yesIndex) {
        FragmentManager fm = getFragmentManager();
        twoBtnDialogFrag = new TwoButtonDialogFragment();
        twoBtnDialogFrag.setOnButtonsClickedListenerOne(this);
        Bundle bndl = new Bundle();
        bndl.putString("yesButtonText", "Si");
        bndl.putInt("yesIndex", yesIndex);
        bndl.putString("noButtonText", "No");
        bndl.putString("question", msg);
        bndl.putString("invisible", "visible");
        twoBtnDialogFrag.setArguments(bndl);
        twoBtnDialogFrag.show(fm, "new triage dialog");
    }

    @Override
    public void onResume() {
        super.onResume();

        try {
            camera = Camera.open();
            mSupportedPreviewSizes = camera.getParameters().getSupportedPreviewSizes();
        } catch (Throwable t) {
            Log.d(CLASS_TAG, "Exception in saving picture", t);
        }

        SurfaceHolder surfaceHolder = preview.getHolder();
        surfaceHolder.addCallback(this);

    }

    @Override
    public void onPause() {
        if (isTakePictureBtnEnabled) {
            camera.stopPreview();
        }
        camera.release();
        camera = null;
        isTakePictureBtnEnabled = false;
        super.onPause();
    }

    //CARLOS: 2016-09-12
    //RESTING THIS FUNCTION INSTEAD THE ORIGINAL BELOW, SINCE THE CAMERA CRASH DUE AUTOFOCUS
    private Camera.Size getBestPreviewSize(List<Camera.Size> previewSizes, int width, int height) {
        double targetAspect = (double) width / (double) height;

        ArrayList<Camera.Size> matchedPreviewSizes = new ArrayList<Camera.Size>();
        final double ASPECT_TOLERANCE = 0.01;
        for (Camera.Size previewSize : previewSizes) {
            double previewAspect = (double) previewSize.width / (double) previewSize.height;

            if (Math.abs(targetAspect - previewAspect) < ASPECT_TOLERANCE && previewSize.width <= width && previewSize.height <= height) {
                matchedPreviewSizes.add(previewSize);
            }
        }

        Camera.Size bestPreviewSize;
        if (!matchedPreviewSizes.isEmpty()) {
            bestPreviewSize = Collections.max(matchedPreviewSizes, sizeComparator);
        } else {
            bestPreviewSize = Collections.max(previewSizes, sizeComparator);
        }

        return bestPreviewSize;
    }

    private static Comparator<Camera.Size> sizeComparator = new Comparator<Camera.Size>() {

        @Override
        public int compare(Camera.Size lhs, Camera.Size rhs) {
            long lhsArea = lhs.height * lhs.width;
            long rhsArea = rhs.height * rhs.width;

            if (lhsArea > rhsArea) {
                return 1;
            } else if (lhsArea < rhsArea) {
                return -1;
            } else {
                return 0;
            }
        }

    };

//    private Camera.Size getBestPreviewSize(int width, int height, Camera.Parameters parameters) {
//        Camera.Size result = null;
//
//        for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
//            if (size.width <= width && size.height <= height) {
//                if (result == null) {
//                    result = size;
//                } else {
//                    int resultDelta = width - result.width + height - result.height;
//                    int newDelta = width - size.width + height - size.height;
//
//                    if (newDelta < resultDelta) {
//                        result = size;
//                    }
//                }
//            }
//        }
//        return (result);
//    }

    private AutoFocusCallback autoFocusCallback = new AutoFocusCallback() {
        public void onAutoFocus(boolean autoFocusSuccess, Camera arg1) {
            camera.takePicture(shutterCallBack, null, photoCallback);
            // mAutoFocus = true;
        }
    };

    private Camera.ShutterCallback shutterCallBack = new Camera.ShutterCallback() {
        @Override
        public void onShutter() {

        }
    };

    private Camera.PictureCallback photoCallback = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
            camera.stopPreview();
            final Bitmap image = BitmapFactory.decodeByteArray(data,0,data.length);
            preview.setVisibility(SurfaceView.GONE);
            imageview.setVisibility(ImageView.VISIBLE);
            imageview.setImageBitmap(image);
            findViewById(R.id.warningMessage).setVisibility(View.INVISIBLE);
            Boolean isSDPresent = android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);

            if (isSDPresent) {
                new SavePhotoSdCardTaskOld().execute(data);
                Log.d("Camera Activity", "saved to SD CARD " + actaFileName);
            } else {
                new SavePhotoInternalTask().execute(data);
                Log.d("Camera Activity", "saved internally " + actaFileName);
            }
        }
    };

    class SavePhotoInternalTask extends AsyncTask<byte[], String, String> {

        @Override
        protected String doInBackground(byte[]... data) {

            try {
                FileOutputStream fOut = openFileOutput(actaFileName, MODE_PRIVATE);
                fOut.write(data[0]);
                fOut.close();
            } catch (final Exception ex) {
                Log.d("CameraActivity", "Exception while saving file!");
                ex.printStackTrace();
            }

            return (null);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            ah.setButtonColorGreen(aceptarBtn);
            isAceptarBtnEnabled = true;
            ah.setButtonColorGreen(rejectBtn);
            isRechesarBtnEnabled = true;
            ah.setButtonColorRed(takePicBtn);
            isTakePictureBtnEnabled = false;
//            camera.stopPreview();

        }
    }

    class SavePhotoSdCardTaskOld extends AsyncTask<byte[], String, String> {
        @SuppressLint("LongLogTag")
        @Override
        protected String doInBackground(byte[]... jpeg) {
            File photo = new File(Environment.getExternalStorageDirectory() + File.separator + actaFileName);

            String imageName = actaFileName;
            Log.i("imageName in SavePhotoSdCardTaskOld ", imageName);


            if (photo.exists()) {
                photo.delete();
            }
            try {
                FileOutputStream fos = new FileOutputStream(photo.getPath());
                fos.write(jpeg[0]);
                fos.close();
            } catch (java.io.IOException e) {
                Log.d("CameraActivity", "Exception in photoCallback", e);
            }
            return (null);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ah.setButtonColorGreen(aceptarBtn);
            isAceptarBtnEnabled = true;
            ah.setButtonColorGreen(rejectBtn);
            isRechesarBtnEnabled = true;
            ah.setButtonColorRed(takePicBtn);
            isTakePictureBtnEnabled = false;
//            camera.stopPreview();

        }
    }

//    public void createDialogEditText(String msg, int yesIndex) {
//        FragmentManager fm = getFragmentManager();
//        twoBtnDialogFragment.setOnButtonsClickedListenerOne(this);
//        Bundle bndl = new Bundle();
//        bndl.putString("yesButtonText", "Continuar");
//        bndl.putInt("yesIndex", yesIndex);
//        bndl.putString("noButtonText", "No");
//        bndl.putString("question", msg);
//        bndl.putString("invisible", "invisible");
//        twoBtnDialogFragment.setArguments(bndl);
//        twoBtnDialogFragment.show(fm, "new triage dialog");
//    }

    // Updated to support INPUT >= 0
    public void createDialogEditTextX(String msg, int yesIndex) {
        FragmentManager fm = getFragmentManager();
        twoBtnDialogFragmentX = new TwoButtonDialogFragment();
//        twoBtnDialogFragmentX.setOnButtonsClickedListenerOneX(this);
        twoBtnDialogFragmentX.setOnButtonsClickedListenerOne(this);
        Bundle bndl = new Bundle();
        bndl.putString("yesButtonText", "Continuar");
        bndl.putInt("yesIndex", yesIndex);
        bndl.putString("noButtonText", "No");
        bndl.putString("question", msg);
        bndl.putString("invisible", "invisible");
        twoBtnDialogFragmentX.setArguments(bndl);
        twoBtnDialogFragmentX.show(fm, "new triage dialog");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.fouth, menu);
        return true;
    }

    public void onBackPressed() {
        Log.d(CLASS_TAG, "onBackPressed Called");
        Intent setIntent = new Intent(Intent.ACTION_MAIN);
        setIntent.addCategory(Intent.CATEGORY_HOME);
        setIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(setIntent);
    }

    @SuppressLint("LongLogTag")
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Camera.Parameters parameters = camera.getParameters();

        //CARLOS: Fix the "DARK PICTURE" issue for Samsung Tablets
        // http://stackoverflow.com/questions/26318300/samsung-galaxy-s5-camera-flash-problems
        if (android.os.Build.BRAND.equals("samsung")) {
//			parameters.set("zsl", "on");
            setHiddenParameter(parameters, "zsl-values", "zsl", "on");
            Log.d("SAMSUNG HARDWARE DETECTED!", "true");
//			camera.setParameters(parameters);
        }

//        Camera.Size size = getBestPreviewSize(mSupportedPreviewSizes, width, height);
        Camera.Size size = getBestPreviewSize(mSupportedPreviewSizes, parameters.getPictureSize().width, parameters.getPictureSize().height);
//        Camera.Size size = getBestPreviewSize(width, height, parameters);

        if (size != null) {
//            if(Build.MODEL.contains("T817V")) {
//                parameters.setPreviewSize(size.width, size.height);
//            }else parameters.setPreviewSize(parameters.getPictureSize().width, parameters.getPictureSize().height);
            parameters.setPreviewSize(size.width, size.height);
            parameters.setPictureFormat(ImageFormat.JPEG);

            // parameters.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);

            // CARLOS: Testing this because Julio's Tablet crashes with
            // FLASH_MODE_AUTO ********
            List<String> flashModes = parameters.getSupportedFlashModes();
            if (flashModes != null && flashModes.contains(android.hardware.Camera.Parameters.FLASH_MODE_AUTO)) {
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
            }

            List<String> focusModes = parameters.getSupportedFocusModes();
            if (focusModes != null && focusModes.contains(android.hardware.Camera.Parameters.FOCUS_MODE_AUTO)) {

                for (String seeThis : focusModes) {
                    Log.d(">>> DEBUG Camera focusModes ", seeThis);
                }

                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            }
            // ****************** FIX AUTOFOCUS AND FLASH
            // *************************************

            camera.setParameters(parameters);
            camera.startPreview();
            isTakePictureBtnEnabled = true;
        }

    }

    @Override
    public void surfaceCreated(SurfaceHolder arg0) {
        try {
            camera.setPreviewDisplay(arg0);
            isTakePictureBtnEnabled = true;
        } catch (Throwable t) {
            camera.release();
            Log.d(CLASS_TAG, "Exception in setPreviewDisplay()", t);
            Toast.makeText(CameraActaActivity.this, t.getMessage(),
                    Toast.LENGTH_LONG).show();
        }

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onNoButtonForTwoButtonDialogClickedX() {
        // TODO Auto-generated method stub
        isFinalizeBtnEnabled = true;

        ah.setButtonColorGreen(nextBtn);
        ah.setButtonColorRed(takePicBtn);
        isTakePictureBtnEnabled = false;
        ah.setButtonColorRed(aceptarBtn);
        isAceptarBtnEnabled = false;
        ah.setButtonColorRed(rejectBtn);
        isRechesarBtnEnabled = false;
    }

    // *********

    // *********

    @Override
    public void onYesButtonForTwoButtonDialogClicked(int yesIdnex) {
        // TODO Auto-generated method stub
        switch (yesIdnex) {
            case 7:
                totalTimesAddMorePicBtnWasPressed++;
                createDialogEditTextX("INGRESAR CANTIDAD DE IMAGENES A AÑADIR.", 1);

                takePicBtn.requestFocus();
                camera.startPreview();

                isFinalizeBtnEnabled = false;
                ah.setButtonColorRed(nextBtn);
                ah.setButtonColorGreen(takePicBtn);
                isTakePictureBtnEnabled = true;
                ah.setButtonColorRed(aceptarBtn);
                isAceptarBtnEnabled = false;
                ah.setButtonColorRed(rejectBtn);
                isRechesarBtnEnabled = false;
                break;

            default:
                finish();
                break;
        }
    }

    public static void setHiddenParameter(Camera.Parameters params, String values_key, String key, String value) {
        if (params.get(key) == null) {
            return;
        }

        String possible_values_str = params.get(values_key);
        if (possible_values_str == null) {
            return;
        }

        String[] possible_values = possible_values_str.split(",");
        for (String possible : possible_values) {
            if (possible.equals(value)) {
                params.set(key, value);
                return;
            }
        }
    }

    private void clearImageCahce(ImageView view){
        Drawable drawable = view.getDrawable();
        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            Bitmap bitmap = bitmapDrawable.getBitmap();
            bitmap.recycle();
        }
    }
}
