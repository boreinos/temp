package com.afilon.mayor.v11.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
//import android.support.v4.app.DialogFragment;
import android.app.DialogFragment;
import android.support.v4.app.Fragment;
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
import android.widget.TextView;
import android.widget.Toast;

import com.afilon.mayor.v11.model.CustomKeyboard;
import com.afilon.mayor.v11.signaturepad.views.SignaturePad;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import com.afilon.mayor.v11.R;
import com.afilon.mayor.v11.utils.Consts;
import com.afilon.mayor.v11.utils.Utilities;
import com.afilon.mayor.v11.webservice.WebServiceActaImageTask;
import android.view.View;
/**
 * Created by Dhatch on 7/28/2017.
 */

public class drawSignature extends DialogFragment{
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private SignaturePad mSignaturePad;
    private drawSignatureListener buttonsClickedListener;
    private boolean
            isSigned = false,
            doesExists = false;
    protected Button continueBtn;
    protected Button cancelBtn;
    protected String
            message,
            electionId,
            dui,
            filename,
            title,
            name,
            jrv;
    private TextView signaturePrompt;
    private String sigText = "";
    drawSignature myContext;
    private View view;

    public drawSignature() {
        myContext = this;
        // Empty constructor required for DialogFragment
    }

//    public drawSignature(String prompt) {
//        // Empty constructor required for DialogFragment
//        sigText = prompt;
//    }

    public interface drawSignatureListener {
        public void onContinueClicked();
        public void onCancleClicked();
    }

    private View.OnClickListener pressContinue(){
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO add code to save signature image
                //  then must close out signature pad window
                //  for now just dismiss
                if (!(buttonsClickedListener == null))
                    buttonsClickedListener.onContinueClicked();
                Bitmap signatureImage = mSignaturePad.getSignatureBitmap();
                if(addJpgSignatureToGallery(signatureImage)){
                    Toast.makeText(getActivity(), "Guardando Firma..", Toast.LENGTH_LONG).show();
                }else Toast.makeText(getActivity(), "Firma No Se Guardo", Toast.LENGTH_LONG).show();

                if(addSvgSignatureToGallery(mSignaturePad.getSignatureSvg())){
                    Toast.makeText(getActivity(), "Guardando SVG Firma.. ", Toast.LENGTH_LONG).show();
                }else Toast.makeText(getActivity(), "Firma SVG No se Guardo ", Toast.LENGTH_LONG).show();

//                sendOneSig(jrv, "Pictures/Signatures/"+filename, dui, title);
//                sendOneSig(jrv, filename, dui, title);
                dismiss();
            }
        };
    }

    private View.OnClickListener pressCancel(){
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                buttonsClickedListener.onNoButtonDialogToConfirmDuiClicked();
                if(isSigned){
                    mSignaturePad.clear();
                }else {
                    if (!(buttonsClickedListener == null))
                        buttonsClickedListener.onCancleClicked();
                    dismiss();
                }
            }
        };
    }


    public void setOnClickedListener(drawSignatureListener listener) {
        this.buttonsClickedListener = listener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.AflDialogStyle);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.draw_signature_layout, container, false);

        Bundle bundle = getArguments();
        String yesBtnText = "Continuar";
        String noBtnText = "Cancelar";
//        message = bundle.getString("invisible");
        String sigText = bundle.getString("question");
        dui = bundle.getString("dui");
        title = bundle.getString("title");
        name = bundle.getString("name");
        jrv = bundle.getString("jrv");
        electionId = bundle.getString("electionID");
        final String errorMessage;
        filename = String.format("%s_%s_%s_%s.jpg",electionId, jrv,dui, title);

        mSignaturePad =(SignaturePad) view.findViewById(R.id.signature_pad);
        cancelBtn = (Button) view.findViewById(R.id.cancel_btn);

        signaturePrompt = (TextView) view.findViewById(R.id.prompt_textview);
        signaturePrompt.setText(sigText);



        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getDialog().setCanceledOnTouchOutside(false);

        continueBtn = (Button) view.findViewById(R.id.continue_btn);
        setButtonColorRed(continueBtn);
        continueBtn.setText(yesBtnText);

        continueBtn.setOnClickListener(pressContinue());
//        continueBtn.setOnClickListener(new View.OnClickListener() {
//            @SuppressLint("LongLogTag")
//            public void onClick(View view) {
//                //TODO add code to save signature image
//                //  then must close out signature pad window
//                //  for now just dismiss
//                Bitmap signatureImage = mSignaturePad.getSignatureBitmap();
//                if(addJpgSignatureToGallery(signatureImage)){
//                    Toast.makeText(getActivity(), "Signature Save Success", Toast.LENGTH_LONG).show();
//                }else Toast.makeText(getActivity(), "Signature Save Failure", Toast.LENGTH_LONG).show();
//
//                if(addSvgSignatureToGallery(mSignaturePad.getSignatureSvg())){
//                    Toast.makeText(getActivity(), "SVG Signature saved", Toast.LENGTH_LONG).show();
//                }else Toast.makeText(getActivity(), "SVG Signature save failure", Toast.LENGTH_LONG).show();
//
////                sendOneSig(jrv, "Pictures/Signatures/"+filename, dui, title);
//                sendOneSig(jrv, filename, dui, title);
//                dismiss();
//            }
//        });

        setButtonColorGreen(cancelBtn);
        cancelBtn.setText(noBtnText);

        cancelBtn.setOnClickListener(pressCancel());
//        cancelBtn.setOnClickListener(new View.OnClickListener() {
//            @SuppressLint("LongLogTag")
//            public void onClick(View view) {
////                buttonsClickedListener.onNoButtonDialogToConfirmDuiClicked();
//                if(isSigned){
//                    mSignaturePad.clear();
//                }else dismiss();
//
//            }
//        });

        mSignaturePad.setOnSignedListener(new SignaturePad.OnSignedListener() {
            @Override
            public void onStartSigning() {
            }

            @Override
            public void onSigned() {
                isSigned = true;
                cancelBtn.setText("Borrar");
                setButtonColorGreen(continueBtn);
                setButtonColorGreen(cancelBtn);
            }

            @Override
            public void onClear() {
                isSigned = false;
                cancelBtn.setText("Cancelar");
                setButtonColorRed(continueBtn);
                setButtonColorGreen(cancelBtn);
            }
        });

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        getDialog().getWindow().addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH);
        getDialog().getWindow().setDimAmount(0.1f);
    }

    @Override
    public void onDestroyView(){
        super.onDestroyView();
    }

    public void setPromptMessage(String prompt){
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


    public File getAlbumStorageDir(String albumName) {
        // Get the directory for the user's public pictures directory.
        File file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), albumName);
        if (!file.mkdirs()) {
            Log.e("SignaturePad", "Directory not created");
        }
        return file;
    }

    public void saveBitmapToJPG(Bitmap bitmap, File photo) throws IOException {
        Bitmap newBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(newBitmap);
        canvas.drawColor(Color.WHITE);
        canvas.drawBitmap(bitmap, 0, 0, null);
        OutputStream stream = new FileOutputStream(photo);
        newBitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
        stream.close();
    }

    public boolean addJpgSignatureToGallery(Bitmap signature) {
        boolean result = false;
        try {
            //TODO change file to save file as "Signature_*DUI*"
//            File photo = new File(getAlbumStorageDir("SignaturePad"), String.format("Signature_%d.jpg", System.currentTimeMillis()));
//            File photo = new File(getAlbumStorageDir("Signatures"), filename);
            File photo = new File(Environment.getExternalStorageDirectory() + File.separator + filename);
//            if(photo.exists()){
//                doesExists = true;
//            }else {
                saveBitmapToJPG(signature, photo);
                scanMediaFile(photo);
//            }
            result = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    private void scanMediaFile(File photo) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri contentUri = Uri.fromFile(photo);
        mediaScanIntent.setData(contentUri);
        getActivity().sendBroadcast(mediaScanIntent);
    }

    public boolean addSvgSignatureToGallery(String signatureSvg) {
        boolean result = false;
        try {
            File svgFile = new File(getAlbumStorageDir("SignaturePad"), String.format("Signature_%d.svg", System.currentTimeMillis()));
            OutputStream stream = new FileOutputStream(svgFile);
            OutputStreamWriter writer = new OutputStreamWriter(stream);
            writer.write(signatureSvg);
            writer.close();
            stream.flush();
            stream.close();
            scanMediaFile(svgFile);
            result = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // TEST FUNCTION FOR SENDING SIGNATURE IMAGES //////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////
    private void sendOneSig(String jrv, String filename, String dui, String title) {
        if(!doesExists) {
            WebServiceActaImageTask uploadActaImageTaskOne;
            Utilities ah = new Utilities(getActivity());
            if (ah.isOnline(getActivity())) {
                uploadActaImageTaskOne = new WebServiceActaImageTask();
//				public void postSig(Context context, String serviceUrl, String fileName, String dui, String title)
                uploadActaImageTaskOne.postSig(getActivity(), Consts.PREF_ELECTION_SIG_URL, filename, dui, title, jrv);
            } else {
                ah.createCustomToast("No hay connecion", "accesible.");
            }
        }
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////
    // END TEST FUNCTION FOR SENDING SIGNATURE IMAGES //////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

}
