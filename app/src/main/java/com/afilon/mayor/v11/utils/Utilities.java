package com.afilon.mayor.v11.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.afilon.mayor.v11.R;
import com.afilon.mayor.v11.activities.LoginActivity;
import com.afilon.mayor.v11.model.Escrudata;
import com.afilon.mayor.v11.model.VotingCenter;
import com.google.gson.Gson;


public class Utilities   {

	protected Context context;

	public Utilities(Context context) {
		this.context = context;
	}

	public void createCustomToast(String msg, String status) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View toastRoot = inflater
				.inflate(R.layout.custom_toast_two_lines, null);

		TextView text = (TextView) toastRoot.findViewById(R.id.toastText);
		text.setTextColor(Color.BLACK);
		text.setText(msg);
		TextView textTriageStatus = (TextView) toastRoot
				.findViewById(R.id.triageStatus);
		textTriageStatus.setTextColor(Color.BLACK);
		textTriageStatus.setText(status);
		Toast toast = new Toast(context);
		toast.setView(toastRoot);
		toast.setGravity(Gravity.CENTER | Gravity.CENTER, 0, 0);
		toast.show();
	}

	public void createCustomToast(String msg) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View toastRoot = inflater.inflate(R.layout.custom_toast_single_line,
				null);
		TextView text = (TextView) toastRoot.findViewById(R.id.toastText);
		text.setTextColor(Color.BLUE);
		text.setText(msg);
		Toast toast = new Toast(context);
		toast.setView(toastRoot);
		toast.setGravity(Gravity.TOP | Gravity.RIGHT, 30, 20);
		toast.show();
	}

	public Toast createCustomToast(int i,String msg) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View toastRoot = inflater.inflate(R.layout.custom_toast_single_line,
				null);
		TextView text = (TextView) toastRoot.findViewById(R.id.toastText);
		text.setTextColor(Color.BLUE);
		text.setText(msg);
		Toast toast = new Toast(context);
		toast.setView(toastRoot);
		toast.setGravity(Gravity.TOP | Gravity.CENTER, 30, 20);
		toast.show();

		return toast;
	}

	public void createCustomLongToast(String msg, String status, int duration) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View toastRoot = inflater
				.inflate(R.layout.custom_toast_two_lines, null);

		TextView text = (TextView) toastRoot.findViewById(R.id.toastText);
		text.setTextColor(Color.BLACK);
		text.setText(msg);
		TextView textTriageStatus = (TextView) toastRoot
				.findViewById(R.id.triageStatus);
		textTriageStatus.setTextColor(Color.BLACK);
		textTriageStatus.setText(status);
		Toast toast = new Toast(context);
		toast.setView(toastRoot);
		toast.setGravity(Gravity.CENTER | Gravity.CENTER, 0, 0);
		ToastExpander.showFor(toast, duration);
	}

	public Integer parseInt(String data, int defaultInt) {
		Integer val = defaultInt;
		try {
			val = Integer.parseInt(data);
		} catch (NumberFormatException nfe) {
		}
		return val;
	}
	public float parseFloat(String data, int defaultFloat){
		float val = defaultFloat;
		try{
			val = Float.parseFloat(data);
		}catch (NumberFormatException nfe){

		}
		return val;
	}

	public boolean isOnline(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		if (netInfo != null && netInfo.isConnected()) {
			return true;
		}
		return false;
	}

	public static boolean isInternetAvailable(Context context) {
		boolean haveConnectedWifi = false;
		boolean haveConnectedMobile = false;
		boolean connectionavailable = false;

		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo[] netInfo = cm.getAllNetworkInfo();

		NetworkInfo informationabtnet = cm.getActiveNetworkInfo();
		for (NetworkInfo ni : netInfo) {
			try {

				if (ni.getTypeName().equalsIgnoreCase("WIFI"))
					if (ni.isConnected())
						haveConnectedWifi = true;
				if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
					if (ni.isConnected())
						haveConnectedMobile = true;
				if (informationabtnet.isAvailable()
						&& informationabtnet.isConnected())
					connectionavailable = true;
				Log.i("ConnectionAvailable", "" + connectionavailable);

			} catch (Exception e) {
				// TODO: handle exception
				Log.e("WiFi Status for TSE ", e.toString());
				e.printStackTrace();

			}
		}
		return haveConnectedWifi || haveConnectedMobile;
	}

	public void savePreferences(String key, String value) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(
				"TSE_PREFERENCES", Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putString(key, value);
		editor.commit();

	}
	public void removePreferences(String key) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(
				"TSE_PREFERENCES", Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.remove(key);
		editor.commit();
	}

	public void savePreferences(String key, int value) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(
				"TSE_PREFERENCES", Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putInt(key, value);
		editor.commit();
	}
	public void savePreferences(String key, boolean value){
		SharedPreferences sharedPreferences = context.getSharedPreferences("TSE_PREFERENCES", Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putBoolean(key, value);
		editor.commit();

	}

	public int loadPreferences(String key) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(
				"TSE_PREFERENCES", Context.MODE_PRIVATE);
		int strSavedMem1 = sharedPreferences.getInt(key, 0);
		return strSavedMem1;
	}


	public String loadPreferencesString(String key) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(
				"TSE_PREFERENCES", Context.MODE_PRIVATE);
		String strSavedMem1 = sharedPreferences.getString(key, "");
		return strSavedMem1;
	}
	public Boolean loadPreferencesBool(String key){
		SharedPreferences sharedPreferences = context.getSharedPreferences(
				"TSE_PREFERENCES", Context.MODE_PRIVATE);
		Boolean bool = sharedPreferences.getBoolean(key,true);
		return bool;
	}
	public Boolean loadPreferencesUpdate(String key){
		SharedPreferences sharedPreferences = context.getSharedPreferences(
				"TSE_PREFERENCES", Context.MODE_PRIVATE);
		Boolean bool = sharedPreferences.getBoolean(key,false);
		return bool;
	}

	public void runProgressDialog(Context context) {
		new ProgressDialogStart().execute();
	}

	class ProgressDialogStart extends AsyncTask<String, Context, String> {
		private static final long PROGRESS_DIALOG_TIME_OUT = 2000;
		private ProgressDialog progressDialog;
		private Context context;

		protected void onPreExecute() {
			super.onPreExecute();
			progressDialog = ProgressDialog.show(context, "", "Guardando...");
			TextView tv1 = (TextView) progressDialog
					.findViewById(android.R.id.message);
			tv1.setTextSize(TypedValue.COMPLEX_UNIT_SP, 25);
			tv1.setTextColor(context.getResources().getColor(R.color.black));
			// progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		}

		protected String doInBackground(String... params) {
			try {
				Thread.sleep(PROGRESS_DIALOG_TIME_OUT); // wait
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

		protected void onPostExecute(String feed) {
			if (progressDialog.isShowing()) {
				try {
					progressDialog.dismiss();
					progressDialog = null;

				} catch (Exception e) {

				}
			}
		}
	}

	public static String[] addStringArrays(String[]... parms) {

		int size = 0;
		for (String[] array : parms) {
			size += array.length;
		}

		String[] result = new String[size];

		int j = 0;
		for (String[] array : parms) {
			for (String s : array) {
				result[j++] = s;
			}
		}
		return result;
	}

	public String getCurrentDateTime() {

		// take current time and date
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		return dateFormat.format(date);
	}

	// this method will check if input string has only numbers
	public int matchInt(String input) {
		if (!input.matches("[0-9]+")) {
			return -1;
		} else {
			int st = Integer.parseInt(input);
			return st;
		}
	}

	public String getCurrentSsid(Context context) {

		String ssid = null;
		ConnectivityManager connManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connManager
				.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		if (networkInfo.isConnected()) {
			final WifiManager wifiManager = (WifiManager) context
					.getSystemService(Context.WIFI_SERVICE);
			final WifiInfo connectionInfo = wifiManager.getConnectionInfo();
			if (connectionInfo != null
					&& !(connectionInfo.getSSID().equals(""))) {
				// if (connectionInfo != null &&
				// !StringUtil.isBlank(connectionInfo.getSSID())) {
				ssid = connectionInfo.getSSID();
			}
			// Get WiFi status MARAKANA
			WifiInfo info = wifiManager.getConnectionInfo();
			String textStatus = "";
			textStatus += "\n\nWiFi Status: " + info.toString();
			String BSSID = info.getBSSID();
			String MAC = info.getMacAddress();

			List<ScanResult> results = wifiManager.getScanResults();
			ScanResult bestSignal = null;
			int count = 1;
			String etWifiList = "";
			for (ScanResult result : results) {
				etWifiList += count++ + ". " + result.SSID + " : "
						+ result.level + "\n" + result.BSSID + "\n"
						+ result.capabilities + "\n"
						+ "\n=======================\n";
			}
			Log.v("Utilities", "from SO: \n" + etWifiList);

			// List stored networks
			List<WifiConfiguration> configs = wifiManager
					.getConfiguredNetworks();
			for (WifiConfiguration config : configs) {
				textStatus += "\n\n" + config.toString();
			}
			Log.v("Utilities", "from marakana: \n" + textStatus);
		}
		return ssid;
	}
    public void tabletConfiguration(String tabletModel, Context context){
        //------------------------------------------------------------------------------------------
        /*Configuration for the generic tablets **/
        /* cannot be used for API < 17 */
		//69.93
        Log.e("DEVICE NAME: ", Build.MODEL+"");
        if(Build.VERSION.SDK_INT >= 17){
            if ( tabletModel.equals("SM-T817V")) {
                DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
                displayMetrics.densityDpi = 260;//DisplayMetrics.DENSITY_LOW;
                android.content.res.Configuration config = context.getResources().getConfiguration();
                config.densityDpi = 260;//DisplayMetrics.DENSITY_LOW;
                Log.e("@@CONFIG@@",String.valueOf(config.screenWidthDp));
                context.getResources().updateConfiguration(config, displayMetrics);
                Log.i("DISPLAY METRICS", displayMetrics.toString());
            }else if(tabletModel.equals("MR1011H1CW1")){
                DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
                displayMetrics.densityDpi = 135;//DisplayMetrics.DENSITY_LOW;
                android.content.res.Configuration config = context.getResources().getConfiguration();
                config.densityDpi = 135;//DisplayMetrics.DENSITY_LOW;
                Log.e("@@CONFIG@@",String.valueOf(config.screenWidthDp));
                context.getResources().updateConfiguration(config, displayMetrics);
                Log.i("DISPLAY METRICS", displayMetrics.toString());
            } else if (tabletModel.equals("SM-T813")) {
				DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
				displayMetrics.densityDpi = 264;//DisplayMetrics.DENSITY_LOW;
				android.content.res.Configuration config = context.getResources().getConfiguration();
				config.densityDpi = 264;//DisplayMetrics.DENSITY_LOW;
				Log.e("@@CONFIG@@",String.valueOf(config.screenWidthDp));
				context.getResources().updateConfiguration(config, displayMetrics);
				Log.i("DISPLAY METRICS", displayMetrics.toString());
			}
        }
        //------------------------ end of chinese tablet config-------------------------------------
    }

	public void createAlertDialog(final Context context) {

		ContextThemeWrapper ctw = new ContextThemeWrapper(context,
				R.style.AflDialogStyle);

		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ctw);
		// set title
		alertDialogBuilder.setTitle("\u00BFDesea cerrar el programa?");

		// set dialog messa
		alertDialogBuilder

				.setCancelable(false)
				.setPositiveButton("Si", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						// if this button is clicked, close
						// current activity
						((Activity) context).finish();
					}
				})
				.setNegativeButton("No", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						// if this button is clicked, just close
						// the dialog box and do nothing
						dialog.cancel();
					}
				});

		// create alert dialog
		AlertDialog alertDialog = alertDialogBuilder.create();

		// show it
		alertDialog.show();
	}

	public boolean isInteger(String value){
		try{
			Integer.parseInt(value);
		}catch (NumberFormatException e){
			return false;
		}
		return true;
	}



	////////////////////////////////////////////////////////////////////////////////////////////////
	// BUTTON AND VIEW CONTROL FUNCTIONS ///////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////

	public void setButtonColorGreen(Button btn) {
		btn.setBackgroundResource(R.drawable.green_button_selector);
		btn.setEnabled(true);
		btn.setFocusableInTouchMode(false);
		btn.setFocusable(true);
	}

	public void setButtonColorRed(Button btn) {
		btn.setBackgroundResource(R.drawable.red_button_selector);
		btn.setEnabled(false);
		btn.setFocusable(false);
		btn.setFocusableInTouchMode(false);

	}
	public void setButtonColorAmber(Button btn) {

		btn.setBackgroundResource(R.drawable.amber_button_selector);
		btn.setFocusable(true);
		btn.setEnabled(true);
		// btn.setPadding(10, 10, 10, 10);
	}
	public void enableView(View v, boolean enable){
		v.setEnabled(enable);
		v.setFocusable(enable);
//		v.setFocusableInTouchMode(enable);
	}

	public void enableEditText(EditText et, boolean enable){
		et.setEnabled(enable);
		et.setFocusable(enable);
		et.setFocusableInTouchMode(enable);
	}

	public void enableEditText(EditText et, boolean enable, int id){
		et.setEnabled(enable);
		et.setFocusable(enable);
		et.setFocusableInTouchMode(enable);
		et.setNextFocusForwardId(id);
		et.setNextFocusRightId(id);
		et.setNextFocusDownId(id);
		et.setNextFocusLeftId(id);
		et.setNextFocusUpId(id);
	}

	public void enableTextView(TextView tv, boolean enable){
		tv.setEnabled(enable);
		tv.setFocusable(enable);
		tv.setFocusableInTouchMode(enable);
	}

	public void enableTextView(EditText tv, boolean enable, int id){
		tv.setEnabled(enable);
		tv.setFocusable(enable);
		tv.setFocusableInTouchMode(enable);
		tv.setNextFocusForwardId(id);
		tv.setNextFocusRightId(id);
		tv.setNextFocusDownId(id);
		tv.setNextFocusLeftId(id);
		tv.setNextFocusUpId(id);
	}

	public void buttonNextFocus(Button btn, int id){
		btn.setNextFocusForwardId(id);
		btn.setNextFocusRightId(id);
		btn.setNextFocusDownId(id);
		btn.setNextFocusLeftId(id);
		btn.setNextFocusUpId(id);
	}

	public void textViewNextFocus(TextView tv, int id){
		tv.setNextFocusForwardId(id);
		tv.setNextFocusRightId(id);
		tv.setNextFocusDownId(id);
		tv.setNextFocusLeftId(id);
		tv.setNextFocusUpId(id);
	}

	public void editTextNextFocus(EditText et, int id){
		et.setNextFocusForwardId(id);
		et.setNextFocusRightId(id);
		et.setNextFocusDownId(id);
		et.setNextFocusLeftId(id);
		et.setNextFocusUpId(id);
	}
	public void viewNextFocus(View v, int id){
		v.setNextFocusForwardId(id);
		v.setNextFocusRightId(id);
		v.setNextFocusDownId(id);
		v.setNextFocusLeftId(id);
		v.setNextFocusUpId(id);
	}
	////////////////////////////////////////////////////////////////////////////////////////////////
	// END BUTTON AND VIEW CONTROL FUNCTIONS ///////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////

	//---------------------------- SAVE CURRENT SCREEN -------------------------------------
	public void saveCurrentScreen(Class activity, Bundle bundle){
		SharedPreferences sharedPreferences = context.getSharedPreferences(
				"TSE_PREFERENCES", Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		Gson gson = new Gson();
//		String valueMap="";

		try{
			Object vco = bundle.get("com.afilon.tse.votingcenter");
			Object sdo = bundle.get("com.afilon.tse.escrudata");
//			valueMap = bundle.getString("escrudataMap");
			VotingCenter vc = (VotingCenter) vco;
			Escrudata sd = (Escrudata) sdo;
			String vcs = gson.toJson(vc);
			String sds = gson.toJson(sd);
			saveBundle(vcs,"votingcenter");
			saveBundle(sds,"escrudata");
		}catch (NullPointerException npe){
			npe.printStackTrace();
		}

//		String bundle_string = gson.toJson(bundle);
//		saveBundle(bundle_string);
//		Log.e("AcitivityName",activity.getName());
//		editor.putString("escrudataMap",valueMap);
		editor.putString("CurrentActivity",activity.getName());
		editor.putBoolean("newApplication",false);
		editor.commit();

	}

	private void saveBundle(String bundle_string,String object){
		try{
			Writer output = null;
			String outFileName = Environment.getExternalStorageDirectory() + File.separator + object+"bundle.json";
			File file  = new File(outFileName);
			output = new BufferedWriter(new FileWriter(file));
			output.write(bundle_string);
			output.close();
		}catch (Exception e){
			Log.e("SAVEFAIL",e.getMessage());
		}
	}

	public Class loadLastScreen(){
		SharedPreferences sharedPreferences = context.getSharedPreferences(
				"TSE_PREFERENCES", Context.MODE_PRIVATE);
		String className = sharedPreferences.getString("CurrentActivity","COMPLETE");
		Log.e("CLASS SAVED",className);
		if(className.equals("COMPLETE")){
			Log.e("WFT","LOG IN CLASS?");
			return LoginActivity.class;
		}
		Class currentClass= null;
		try{
			currentClass = Class.forName(className);
		}catch (ClassNotFoundException cnfe){
			Log.e("WTF","no class found dude");
			cnfe.printStackTrace();
			currentClass = LoginActivity.class;
		}
		return currentClass;
	}

	public Bundle getLastBundle(){
		String vcj = null;
		String sdj = null;
		String votingCenterFile = Environment.getExternalStorageDirectory() + File.separator + "votingcenterbundle.json";
		String escrudataFile = Environment.getExternalStorageDirectory() + File.separator + "escrudatabundle.json";
		File vcf  = new File(votingCenterFile);
		File sdf = new File(escrudataFile);
		try{
			InputStream is = new FileInputStream(vcf);
			int size = is.available();
			byte[] buffer = new byte[size];
			is.read(buffer);
			is.close();
			vcj = new String(buffer,"UTF-8");

			is = new FileInputStream(sdf);
			size = is.available();
			buffer = new byte[size];
			is.read(buffer);
			is.close();
			sdj = new String(buffer,"UTF-8");

		}catch (IOException ioe){
			ioe.printStackTrace();
			return null;
		}
		Gson gson = new Gson();
		VotingCenter vc = gson.fromJson(vcj,VotingCenter.class);
		Escrudata sd = gson.fromJson(sdj,Escrudata.class);
//		SharedPreferences sharedPreferences = context.getSharedPreferences("TSE_PREFERENCES", Context.MODE_PRIVATE);
//		String valueMap = sharedPreferences.getString("escrudataMap","error");


		Bundle bundle = new Bundle();
		bundle.putParcelable("com.afilon.tse.votingcenter",vc);
		bundle.putParcelable("com.afilon.tse.escrudata",sd);
//		bundle.putString("escrudataMap",valueMap);
		return bundle;
	}


}