package wrappers;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

public class HttpClientWrapper {

	private HttpClient httpClient = HttpClients.createDefault();

	public String getUrlContent(String url) throws IOException {
		HttpGet httpGet = new HttpGet(url);
		HttpResponse httpResponse = httpClient.execute(httpGet);
		return EntityUtils.toString(httpResponse.getEntity());
	}

}
