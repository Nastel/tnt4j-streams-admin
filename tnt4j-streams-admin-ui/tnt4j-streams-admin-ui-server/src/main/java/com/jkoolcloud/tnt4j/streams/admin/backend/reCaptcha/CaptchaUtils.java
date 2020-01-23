package com.jkoolcloud.tnt4j.streams.admin.backend.reCaptcha;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jkoolcloud.tnt4j.streams.admin.backend.utils.PropertyData;

public class CaptchaUtils {
	private static final Logger LOG = LoggerFactory.getLogger(CaptchaUtils.class);

	public static JsonNode verify(String reCaptchaResponse) throws Exception {
		String url = PropertyData.getProperty("verifyURL");
		String secret = PropertyData.getProperty("secretKeyCaptcha");
		if (reCaptchaResponse == null || "".equals(reCaptchaResponse)) {
			return null;
		}
		try {
			URL urlConn = new URL(url);
			HttpsURLConnection con = (HttpsURLConnection) urlConn.openConnection();
			// add request header
			con.setRequestMethod("POST");
			String postParams = "secret=" + secret + "&response=" + reCaptchaResponse;

			// Send post request
			con.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			wr.writeBytes(postParams);
			wr.flush();
			wr.close();

			int responseCode = con.getResponseCode();
			LOG.debug("\nSending 'POST' request to URL : " + url);
			LOG.debug("Post parameters : " + postParams);
			LOG.debug("Response Code : " + responseCode);

			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuilder response = new StringBuilder();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();

			ObjectMapper mapper = new ObjectMapper();
			String responseValue = response.toString();
			JsonNode actualObj = mapper.readTree(responseValue);
			LOG.debug("response value: " + responseValue);
			LOG.info("response value TRUE/FALSE: " + actualObj.get("success"));

			return actualObj;
		} catch (Exception e) {
			LOG.info("Problem on reading response for reCaptcha");
			e.printStackTrace();
			throw new Exception();
		}
	}

	// HttpClient client = new DefaultHttpClient();
	// HttpPost post = new HttpPost(url);
	// post.setHeader("User-Agent", USER_AGENT);
	//
	// List<NameValuePair> urlParameters = new ArrayList<>();
	// urlParameters.add(new BasicNameValuePair("secret", secret));
	// urlParameters.add(new BasicNameValuePair("response", reCaptchaResponse));
	//
	// post.setEntity(new UrlEncodedFormEntity(urlParameters));
	//
	// HttpResponse response = client.execute(post);
	// System.out.println("\nSending 'POST' request to URL : " + url);
	// System.out.println("Post parameters : " + post.getEntity());
	// System.out.println("Response Code : " +
	// response.getStatusLine().getStatusCode());
	//
	// BufferedReader rd = new BufferedReader(
	// new InputStreamReader(response.getEntity().getContent()));
	//
	// StringBuffer result = new StringBuffer();
	// String line = "";
	// while ((line = rd.readLine()) != null) {
	// result.append(line);
	// }

	public static void main(String[] args) {
		try {
			verify("test");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
