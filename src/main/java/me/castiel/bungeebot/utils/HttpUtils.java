package me.castiel.bungeebot.utils;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContexts;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class HttpUtils {

    public static int sendPostRequest(String link, HashMap<String, String> headers, String body) {
        try {
            URL url = new URL(link);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                connection.addRequestProperty(entry.getKey(), entry.getValue());
            }
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Length", Integer.toString(body.length()));
            connection.getOutputStream().write(body.getBytes(StandardCharsets.UTF_8));
            System.out.println(connection.getResponseCode() + " < " + connection.getResponseMessage());
            return connection.getResponseCode();
        }
        catch (IOException ignored) {
            ignored.printStackTrace();
            return 0;
        }
    }

    public static void sendPostRequest(String link, HashMap<String, String> keys) {
        try {
            SSLContext sslContext = SSLContexts.custom().loadTrustMaterial((chain, authType) -> true).build();
            HostnameVerifier hostnameVerifier = new NoopHostnameVerifier();
            SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(sslContext, hostnameVerifier);
            CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(sslConnectionSocketFactory).build();
            HttpPost httppost = new HttpPost(link);
            List<NameValuePair> params = new ArrayList<>();
            for (Map.Entry<String, String> entry : keys.entrySet()) {
                params.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
            }
            httppost.setEntity(new UrlEncodedFormEntity(params));
            httpclient.execute(httppost);
        } catch (IOException | NoSuchAlgorithmException | KeyStoreException | KeyManagementException ignored) { ignored.printStackTrace(); }
    }
}
