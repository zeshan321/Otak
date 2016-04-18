package utils;

import com.sun.net.httpserver.HttpExchange;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

public class ServerUtils {

    public void writeResponse(HttpExchange httpExchange, String text) {
        try {
            httpExchange.sendResponseHeaders(200, text.length());

            OutputStream os = httpExchange.getResponseBody();
            os.write(text.getBytes());
            os.flush();
            os.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeFile(HttpExchange t, File response) {
        try {
            t.sendResponseHeaders(200, response.length());

            OutputStream outputStream = t.getResponseBody();
            FileInputStream fileInputStream = new FileInputStream(response);

            try {
                IOUtils.copy(fileInputStream, outputStream);
            } finally {
                IOUtils.closeQuietly(fileInputStream);
                IOUtils.closeQuietly(outputStream);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Map<String, String> queryToMap(String query) {
        Map<String, String> result = new HashMap<>();

        try {
            for (String param : query.split("&")) {
                String pair[] = param.split("=");
                if (pair.length > 1) {
                    result.put(pair[0], URLDecoder.decode(pair[1], "UTF-8"));
                } else {
                    result.put(pair[0], "");
                }
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return result;
    }
}
