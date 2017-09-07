package com.afilon.mayor.v11.webservice;

import java.io.IOException;
import java.lang.ref.WeakReference;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import android.os.AsyncTask;
import android.util.Log;

public class WebServiceRestTask extends AsyncTask<HttpUriRequest, Void, Object> {

	private static final String TAG = "WebServiceRestTask";
	private AbstractHttpClient mClient;
	private WeakReference<DataResponseCallback> mCallback;
	private int ws_task;

	public WebServiceRestTask(int ws_task) {
		this(new DefaultHttpClient(), ws_task);

	}

	public WebServiceRestTask(AbstractHttpClient client, int task_number) {
		mClient = client;
		this.ws_task = task_number;
	}


	public interface DataResponseCallback {
		public void onRequestDataSuccess(String response);

		public void onRequestDataError(Exception error);
	}

	public void setResponseDataCallback(DataResponseCallback callback) {
		mCallback = new WeakReference<DataResponseCallback>(callback);
	}

	@Override
	protected Object doInBackground(HttpUriRequest... params) {
		try {
			HttpUriRequest request = params[0];
			HttpResponse serverResponse = mClient.execute(request);

			BasicResponseHandler handler = new BasicResponseHandler();
			String response = handler.handleResponse(serverResponse);
			return response + ws_task; //This line is sending 14 for Concepts

		} catch (Exception e) {
			Log.w(TAG, e);
			return e;
		}
	}



	@Override
	protected void onPostExecute(Object result) {
		if (mCallback != null && mCallback.get() != null) {
			if (result instanceof String) {
				mCallback.get().onRequestDataSuccess((String) result);
			} else if (result instanceof Exception) {
				mCallback.get().onRequestDataError((Exception) result);
			} else {
				mCallback.get().onRequestDataError(
						new IOException("Unknown Error Contacting Host"));
			}
		}
	}



}
