package br.com.gasoutapp.service.web;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.apache.http.entity.ContentType;
import org.springframework.stereotype.Service;

import br.com.gasoutapp.utils.JsonUtil;

@Service
public class FluentService {

	public Response post(String url, Map<String, String> headers, Object postData)
			throws ClientProtocolException, IOException, URISyntaxException {
		URI normUri = new URI(url).normalize();
		Request postRequest = Request.Post(normUri).connectTimeout(100000);

		if (headers != null && headers.size() > 0) {
			for (Map.Entry<String, String> entry : headers.entrySet()) {
				postRequest.addHeader(entry.getKey(), entry.getValue());
			}
		}

		String postDataString = JsonUtil.ObjectToJson(postData);
		Response response = postRequest.bodyString(postDataString, ContentType.APPLICATION_JSON).execute();

		return response;
	}

	public Response get(String url, Map<String, String> headers)
			throws ClientProtocolException, IOException, URISyntaxException {
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
