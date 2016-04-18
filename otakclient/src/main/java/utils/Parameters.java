package utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Parameters {

    private HashMap<String, String> params = new HashMap<>();

    public void add(String key, String value) {
        params.put(key, value);
    }

    @Override
    public String toString() {
        String result = "?";
        Iterator iter = params.entrySet().iterator();

        try {
            while (iter.hasNext()) {
                if (!result.endsWith("?")) {
                    result += "&";
                }

                Map.Entry entry = (Map.Entry)iter.next();

                result += entry.getKey() + "=" + URLEncoder.encode(entry.getValue().toString(), "UTF-8");
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return result;
    }
}