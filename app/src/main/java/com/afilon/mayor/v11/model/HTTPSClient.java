package com.afilon.mayor.v11.model;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;

import org.apache.http.HttpVersion;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

public class HTTPSClient extends DefaultHttpClient {

	public HTTPSClient() {
	}

	@Override
	protected ClientConnectionManager createClientConnectionManager() {

		SchemeRegistry registry = new SchemeRegistry();

		HostnameVerifier hostnameVerifier = org.apache.http.conn.ssl.SSLSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER;
		final SSLSocketFactory socketFactory = SSLSocketFactory
				.getSocketFactory();
		socketFactory
				.setHostnameVerifier((X509HostnameVerifier) hostnameVerifier);
		// socketFactory.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

		registry.register(new Scheme("http", PlainSocketFactory
				.getSocketFactory(), 80));

		registry.register(new Scheme("https", socketFactory, 8181));
		HttpsURLConnection.setDefaultHostnameVerifier(hostnameVerifier);

		HttpParams params = new BasicHttpParams();
		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
		HttpProtocolParams.setContentCharset(params, "UTF-8");

		return new SingleClientConnManager(params, registry);
	}

}
