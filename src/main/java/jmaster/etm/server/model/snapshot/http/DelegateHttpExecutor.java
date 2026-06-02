package jmaster.etm.server.model.snapshot.http;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.function.Function;

/**
 * executes HttpRequestData by transmitting it to remote
 */
public class DelegateHttpExecutor implements Function<HttpRequestData, HttpResponseData> {
    private String uri, basicAuthUsername, basicAuthPassword;

    public DelegateHttpExecutor(String delegateUri, String basicAuthUsername, String basicAuthPassword) {
        uri = delegateUri;
        this.basicAuthUsername = basicAuthUsername;
        this.basicAuthPassword = basicAuthPassword;
    }

    @Override
    public HttpResponseData apply(HttpRequestData httpRequestData) {
        if (uri == null) {
            throw new NullPointerException("uri not set");
        }

        try {
            SSLUtil.turnOffSslChecking();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        if (basicAuthUsername != null) {
            headers.setBasicAuth(basicAuthUsername, basicAuthPassword);
        }
        HttpEntity<HttpRequestData> entity = new HttpEntity<>(httpRequestData, headers);
        ResponseEntity<HttpResponseData> result = restTemplate.exchange(uri, HttpMethod.POST, entity, HttpResponseData.class);
        return result.getBody();
    }
}
