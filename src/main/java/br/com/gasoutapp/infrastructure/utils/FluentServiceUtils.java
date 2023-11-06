package br.com.gasoutapp.infrastructure.utils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.apache.http.entity.ContentType;
import org.springframework.stereotype.Service;

@Service
public class FluentServiceUtils {

	public Response post(String url, Map<String, String> headers, Object postData)
			throws IOException, URISyntaxException {
		URI normUri = new URI(url).normalize();
		Request postRequest = Request.Post(normUri).connectTimeout(100000);

		if (headers != null && headers.size() > 0) {
			for (Map.Entry<String, String> entry : headers.entrySet()) {
				postRequest.addHeader(entry.getKey(), entry.getValue());
			}
		}

		String postDataString = JsonUtil.ObjectToJson(postData);
		return postRequest.bodyString(postDataString, ContentType.APPLICATION_JSON).execute();
	}

	public Response get(String url, Map<String, String> headers)
			throws IOException, URISyntaxException {
		URI normUri = new URI(url).normalize();
		Request getRequest = Request.Get(normUri).connectTimeout(5000).socketTimeout(5000);

		if (headers != null && headers.size() > 0) {
			for (Map.Entry<String, String> entry : headers.entrySet()) {
				getRequest.addHeader(entry.getKey(), entry.getValue());
			}
		}

		return getRequest.execute();
	}

}
