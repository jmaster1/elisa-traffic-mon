package jmaster.etm.server.model.snapshot.http;

import org.apache.commons.io.IOUtils;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import java.util.Iterator;
import java.util.function.Function;

/**
 * executes HttpRequestData
 */
public class LocalHttpExecutor implements Function<HttpRequestData, HttpResponseData> {
    @Override
    public HttpResponseData apply(HttpRequestData requestData) {
        HttpResponseData responseData = null;
        try {
            URL url = new URL(requestData.url);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod(requestData.method);
            if (requestData.headers != null) {
                Iterator<String> keys = requestData.headers.keySet().iterator();
                while (keys.hasNext()) {
                    String key = keys.next();
                    httpURLConnection.setRequestProperty(key, requestData.headers.get(key));
                }
            }

            httpURLConnection.setUseCaches(false);
            httpURLConnection.setDoInput(true);
            httpURLConnection.setDoOutput(true);

            responseData = new HttpResponseData();
            responseData.status = httpURLConnection.getResponseCode();
            responseData.contentType = httpURLConnection.getContentType();
            responseData.contentEncoding = httpURLConnection.getContentEncoding();
            byte[] sourceBytes = IOUtils.toByteArray(httpURLConnection.getInputStream());
            responseData.contentBase64 = Base64.getEncoder().encodeToString(sourceBytes);
            httpURLConnection.disconnect();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        return responseData;
    }
}
