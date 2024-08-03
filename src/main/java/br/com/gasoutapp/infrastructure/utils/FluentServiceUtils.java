package br.com.gasoutapp.infrastructure.utils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.springframework.stereotype.Service;

@Service
public class FluentServiceUtils {

	public void post(String url, Map<String, String> headers, Object postData)
			throws IOException, URISyntaxException {
		URI normUri = new URI(url).normalize();
		Request postRequest = Request.Post(normUri).connectTimeout(100000);

		if (headers != null && !headers.isEmpty()) {
			for (Map.Entry<String, String> entry : headers.entrySet()) {
				postRequest.addHeader(entry.getKey(), entry.getValue());
			}
		}

		String postDataString = JsonUtil.ObjectToJson(postData);
		postRequest.bodyString(postDataString, ContentType.APPLICATION_JSON).execute();
	}

}
