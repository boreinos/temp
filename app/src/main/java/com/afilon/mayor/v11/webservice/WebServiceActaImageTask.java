package com.afilon.mayor.v11.webservice;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.nio.charset.Charset;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.content.ByteArrayBody;

import org.apache.http.entity.mime.content.StringBody;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;


import com.afilon.mayor.v11.model.HTTPSClient;
import com.afilon.mayor.v11.webservice.CustomMultiPartEntity.ProgressListener;


public class WebServiceActaImageTask {

	private static final String TAG = "Upload Acta Image Task";
	public static final int IMAGE_MAX_SIZE = 1024;

	private Activity activityContext;
	private WeakReference<SendImageResponseCallback> mCallback;

	public interface SendImageResponseCallback {
		public void onRequestSendImageSuccess(String response);

		public void onRequestSendImageError(Exception error);
	}

	public void setResponseCallback(SendImageResponseCallback callback) {
		mCallback = new WeakReference<SendImageResponseCallback>(callback);
	}

	public void postData(Context context, String serviceUrl, String fileName, String electionId) {
		WebServiceTask wst = new WebServiceTask(WebServiceTask.POST_TASK, context, fileName, electionId);

		this.activityContext = (Activity) context;
		wst.execute(new String[] { serviceUrl });
	}

	public void postSig(Context context, String serviceUrl, String fileName, String dui, String title, String jrv) {
		WebServiceTask wst = new WebServiceTask(WebServiceTask.SIG_TASK, context, fileName, dui, title, jrv);

		this.activityContext = (Activity) context;
		wst.execute(new String[] { serviceUrl });
	}

	public void handleResponse(String response) {

		Log.e(TAG, response);

		if (mCallback != null && mCallback.get() != null) {
			if (response instanceof String) {
				mCallback.get().onRequestSendImageSuccess(response);

			} else {
				mCallback.get().onRequestSendImageError(
						new IOException("Unknown Error Contacting Host"));
			}
		}

	}

	private class WebServiceTask extends AsyncTask<String, Integer, String> {

		public static final int POST_TASK = 1;
		public static final int GET_TASK = 2;
		public static final int SIG_TASK = 3;

		private static final String TASK_TAG = "UploadActaImageAsyncTask";

		private int taskType = GET_TASK;
		private Context taskContext = null;
		private String
				imageFileName,
				electionIdString,
				dui,
				title,
				jrv;
		private ProgressDialog pDlg = null;
		private long totalSize;

		public WebServiceTask(int taskType, Context context, String actaImageFileName, String electionId) {
			this.taskType = taskType;
			this.taskContext = context;
			this.imageFileName = actaImageFileName;
			this.electionIdString = electionId;
		}

		public WebServiceTask(int taskType, Context context, String imageFileName, String dui, String title, String jrv) {
			this.taskType = taskType;
			this.taskContext = context;
			this.imageFileName = imageFileName;
			this.dui = dui;
			this.title = title;
			this.jrv = jrv;
		}

		@Override
		protected void onPreExecute() {
			// showProgressDialog(taskContext);
		}

		protected String doInBackground(String... urls) {
			String url = urls[0];
			String result = "";

			HttpResponse response = doResponse(url);
			if (response == null) {
				return result;
			} else {
				try {
					result = inputStreamToString(response.getEntity()
							.getContent());
				} catch (IllegalStateException e) {
					Log.e(TASK_TAG, e.getLocalizedMessage(), e);
				} catch (IOException e) {
					Log.e(TASK_TAG, e.getLocalizedMessage(), e);
				}
			}
			return result;
		}

		@Override
		protected void onProgressUpdate(Integer... progress) {
			// pDlg.setProgress((int) (progress[0]));
		}

		@Override
		protected void onPostExecute(String response) {
			handleResponse(response);
			// pDlg.dismiss();
		}

		private HttpResponse doResponse(String url) {

			Bitmap bitmap = null;
			try {
				bitmap = decodeFile(imageFileName);
				// bitmap = rotateBitmap(bitmap, 90);
			} catch (IOException e1) {
				Log.e(TASK_TAG, e1.getLocalizedMessage(), e1);
			}

			ByteArrayOutputStream stream = new ByteArrayOutputStream();

			// compress image before sending to server
			bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream);

			byte[] byte_arr = stream.toByteArray();

			ByteArrayBody fileBody = new ByteArrayBody(byte_arr, imageFileName);

			StringBody jrvStringBody = null;
			StringBody electionIdBody = null;
			StringBody duiBody = null;
			StringBody titleBody = null;

			try {
				if(taskType == SIG_TASK) {
					duiBody = new StringBody(dui, "text/plain", Charset.forName("UTF-8"));
					titleBody = new StringBody(title, "text/plain", Charset.forName("UTF-8"));
					jrvStringBody = new StringBody(jrv, "text/plain", Charset.forName("UTF-8"));
				} else {
					electionIdBody = new StringBody(electionIdString, "text/plain", Charset.forName("UTF-8"));
					jrvStringBody = new StringBody(imageFileName, "text/plain", Charset.forName("UTF-8"));
				}
			} catch (UnsupportedEncodingException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			CustomMultiPartEntity multipartEntity = new CustomMultiPartEntity(
					new ProgressListener() {
						@Override
						public void transferred(long num) {
							publishProgress((int) ((num / (float) totalSize) * 100));
						}
					});

			HTTPSClient httpclient = new HTTPSClient();
			HttpResponse response = null;
			try {

				if(taskType == SIG_TASK){
					multipartEntity.addPart("file", fileBody);
					multipartEntity.addPart("jrv", jrvStringBody);
					multipartEntity.addPart("dui", duiBody);
					multipartEntity.addPart("title", titleBody);
				} else {
					multipartEntity.addPart("file", fileBody);
					multipartEntity.addPart("jrv", jrvStringBody);
					multipartEntity.addPart("electionId", electionIdBody);
				}

				totalSize = multipartEntity.getContentLength();
				HttpPost httppost = new HttpPost(url);
				switch (taskType) {

					case POST_TASK:
						httppost.setEntity(multipartEntity);
						response = httpclient.execute(httppost);
						break;
					case SIG_TASK:
						httppost.setEntity(multipartEntity);
						response = httpclient.execute(httppost);
						break;
					case GET_TASK:
						HttpGet httpget = new HttpGet(url);
						response = httpclient.execute(httpget);
						break;
				}
			} catch (Exception e) {
				Log.e(TASK_TAG, e.getLocalizedMessage(), e);
			}

			return response;
		}

		private Bitmap decodeFile(String fname) throws IOException {
			Bitmap b = null;

			// Check if SD card is present
			Boolean isSDPresent = android.os.Environment
					.getExternalStorageState().equals(
							android.os.Environment.MEDIA_MOUNTED);

			if (isSDPresent) {
				fname = Environment.getExternalStorageDirectory()
						+ File.separator + fname;

				BitmapFactory.Options o = new BitmapFactory.Options();
				o.inJustDecodeBounds = true;

				FileInputStream fis = new FileInputStream(fname);
				BitmapFactory.decodeStream(fis, null, o);
				fis.close();

				int scale = 1;
				if (o.outHeight > IMAGE_MAX_SIZE || o.outWidth > IMAGE_MAX_SIZE) {
					scale = (int) Math.pow(
							2,
							(int) Math.round(Math.log(IMAGE_MAX_SIZE
									/ (double) Math
											.max(o.outHeight, o.outWidth))
									/ Math.log(0.5)));
				}

				// Decode with inSampleSize
				BitmapFactory.Options o2 = new BitmapFactory.Options();
				o2.inSampleSize = scale;
				fis = new FileInputStream(fname);
				b = BitmapFactory.decodeStream(fis, null, o2);
				fis.close();

				Log.e("Upload image task", "read from SD CARD " + fname);
			} else {
				FileInputStream fis = taskContext.openFileInput(fname);
				b = BitmapFactory.decodeStream(fis);
				fis.close();
				Log.e("Upload Task", "read internally");
			}

			return b;
		}

		private String inputStreamToString(InputStream is) {
			String line = "";
			StringBuilder total = new StringBuilder();
			// Wrap a BufferedReader around the InputStream
			BufferedReader rd = new BufferedReader(new InputStreamReader(is));
			try {
				// Read response until the end
				while ((line = rd.readLine()) != null) {
					total.append(line);
				}
			} catch (IOException e) {
				Log.e(TASK_TAG, e.getLocalizedMessage(), e);
			}
			// Return full string
			return total.toString();
		}
	}

	public static Bitmap rotateBitmap(Bitmap b, int degrees) {
		if (degrees != 0 && b != null) {
			Matrix m = new Matrix();

			m.setRotate(degrees, (float) b.getWidth() / 2,
					(float) b.getHeight() / 2);
			try {
				Bitmap b2 = Bitmap.createBitmap(b, 0, 0, b.getWidth(),
						b.getHeight(), m, true);
				if (b != b2) {
					b.recycle();
					b = b2;
				}
			} catch (OutOfMemoryError ex) {
				Log.e(TAG, ex.getLocalizedMessage(), ex);
				throw ex;
			}
		}
		return b;
	}

}

