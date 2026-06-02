package jmaster.etm.server.model.snapshot.http;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.springframework.http.HttpStatus;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class HttpResponseData {
    public int status;

    public String contentType;

    public String contentEncoding;

    public String contentBase64;

    public void ensureStatusOk() {
        if (status != HttpStatus.OK.value()) {
            throw new RuntimeException("Bad status: " + status);
        }
    }

    public JsonObject getContentAsJsonObject() {
        String str = getContentAsString();
        return new Gson().fromJson(str, JsonObject.class);
    }

    private String getContentAsString() {
        byte[] bytes = Base64.getDecoder().decode(contentBase64);
        return new String(bytes, StandardCharsets.UTF_8);
    }
}
