package wrappers;

import java.io.IOException;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

public class HttpClientWrapper {

	private CloseableHttpClient httpClient = HttpClients.createDefault();

	public String getUrlContent(String url) throws IOException {
		HttpGet httpGet = new HttpGet(url);
		CloseableHttpResponse httpResponse = httpClient.execute(httpGet);
		String respStr = EntityUtils.toString(httpResponse.getEntity());
		EntityUtils.consumeQuietly(httpResponse.getEntity());
		httpResponse.close();

		return respStr;
	}

	public void shutdown() {
		try {
			httpClient.close();
		} catch (IOException e) {
		}
	}

}
