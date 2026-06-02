package jmaster.etm.server.model.snapshot.http;

import org.springframework.http.HttpMethod;

import java.util.HashMap;
import java.util.Map;

public class HttpRequestData {
    public String url;

    public final Map<String, String> headers = new HashMap<>();

    public String method = HttpMethod.GET.name();
}
