package org.suai.constants;


import java.net.URL;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.net.MalformedURLException;

import java.io.IOException;


public interface ConnectionConstants {

	public static final String HEADER_ENCODING = "UTF-8";
	public static final String REQUEST_METHOD = "GET";
	public static final String[][] HEADER_CONNECTION = {
			{"Scheme", "https"},
			{"Hostname", "api.lenta.ru"},
			{"User-Agent", "Lenta/1.4.2 (iPhone; iOS 10.3.2; Scale/2.00)"},
			{"X-Lenta-Media-Type", "1"},
			{"Accept-Language", "ru-RU;q=1, en-RU;q=0.9"},
			{"Accept", "application/json"}
	};


	default HttpURLConnection getURLConnection(String link) throws IOException, MalformedURLException {
		URL url = new URL(link);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		
		for(int i = 0; i < HEADER_CONNECTION.length; i++) {
			connection.setRequestProperty(
					URLEncoder.encode(HEADER_CONNECTION[i][0], HEADER_ENCODING),
					URLEncoder.encode(HEADER_CONNECTION[i][1], HEADER_ENCODING));
		}

		connection.setRequestMethod(REQUEST_METHOD);
		connection.setDoInput(true);
		connection.setDoOutput(true);

		return connection;
	}

}