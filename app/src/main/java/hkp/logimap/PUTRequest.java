package hkp.logimap;

/**
 * Created by krysztal on 23.01.16.
 */
public class PUTRequest {
    String url, newjson;
    Boolean done;

    PUTRequest(String url, String json) {
        this.url = url;
        this.newjson = json;
        this.done = false;
    }
}
