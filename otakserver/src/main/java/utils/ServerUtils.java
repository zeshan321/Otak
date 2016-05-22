package utils;

import com.sun.net.httpserver.HttpExchange;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

public class ServerUtils {

    public void writeResponse(HttpExchange httpExchange, String text) {
        try {
            httpExchange.getResponseHeaders().set("Content-Encoding", "gzip");
            httpExchange.sendResponseHeaders(200, 0);

            GZIPOutputStream os = new GZIPOutputStream(httpExchange.getResponseBody());
            os.write(text.getBytes());
            os.flush();
            os.close();

            httpExchange.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeFile(HttpExchange httpExchange, File response) {
        try {
            httpExchange.getResponseHeaders().set("Content-Encoding", "gzip");
            httpExchange.sendResponseHeaders(200, 0);

            GZIPOutputStream outputStream = new GZIPOutputStream(httpExchange.getResponseBody());
            FileInputStream fileInputStream = new FileInputStream(response);

            copySteam(fileInputStream, outputStream);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void copySteam(InputStream inputStream, OutputStream outputStream) throws IOException {
        try {
            IOUtils.copy(inputStream, outputStream);

            outputStream.flush();
            outputStream.close();
            inputStream.close();
        } finally {
            IOUtils.closeQuietly(inputStream);
            IOUtils.closeQuietly(outputStream);
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
