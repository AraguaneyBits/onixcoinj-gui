package info.onixcoin.api;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import java.io.IOException;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author jestevez
 */
public class OnixcoinInfoApi {

    private static final String ONIXCOIN_INFO_API = "https://www.onixcoin.info/api";

    private static String sendGet(String url) throws IOException {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("Cache-Control", "no-cache")
                .build();
        Response response = client.newCall(request).execute();
        String json = response.body().string();
        return json;
    }

    public static JSONObject tx(String tx) throws IOException {
        String url = ONIXCOIN_INFO_API + "/tx/" + tx;
        String json = sendGet(url);
        JSONObject req = new JSONObject(json);
        return req;
    }

    public static JSONObject addr(String address) throws IOException {
        String url = ONIXCOIN_INFO_API + "/addr/" + address;
        String json = sendGet(url);
        JSONObject req = new JSONObject(json);
        return req;
    }

    public static JSONArray addrUtxo(String address) throws IOException {
        String url = ONIXCOIN_INFO_API + "/addr/" + address + "/utxo";
        String json = sendGet(url);
        JSONArray req = new JSONArray(json);
        return req;
    }

}
