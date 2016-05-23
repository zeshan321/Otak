package contexts;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.zeshanaslam.otak.Main;
import messages.Errors;
import utils.Config;
import utils.ServerUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;

public class StreamContext implements HttpHandler
{
    private Config config = Main.config;

    public void handle(HttpExchange httpExchange) throws IOException {
        ServerUtils server = new ServerUtils();
        Map<String, String> params = server.queryToMap(httpExchange.getRequestURI().getRawQuery());

        if (!params.containsKey("pass") || !params.get("pass").equals(config.getString("pass"))) {
            server.writeResponse(httpExchange, new Errors().getError(Errors.ErrorTypes.AUTH));
            return;
        }

        if (!params.containsKey("file")) {
            server.writeResponse(httpExchange, new Errors().getError(Errors.ErrorTypes.MISSING));
            return;
        }

        String mime = params.get("type");

        File file = new File(this.config.getString("dir") + File.separator + params.get("file"));
        String etag = Integer.toHexString((file.getAbsolutePath() + file.lastModified() + "" + file.length()).hashCode());

        httpExchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");

        long startFrom = 0;
        long endAt = -1;
        String range = httpExchange.getRequestHeaders().getFirst("range");
        if (range != null) {
            if (range.startsWith("bytes=")) {
                range = range.substring("bytes=".length());
                int minus = range.indexOf('-');
                try {
                    if (minus > 0) {
                        startFrom = Long.parseLong(range.substring(0, minus));
                        endAt = Long.parseLong(range.substring(minus + 1));
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }

        long fileLen = file.length();
        if (range != null && startFrom >= 0) {
            if (startFrom >= fileLen) {
                httpExchange.getResponseHeaders().set("Content-Type", mime);
                httpExchange.getResponseHeaders().set("Content-Range", "bytes 0-0/" + fileLen);
                httpExchange.getResponseHeaders().set("ETag", etag);
                httpExchange.sendResponseHeaders(416, 0);
            } else {
                if (endAt < 0) {
                    endAt = fileLen - 1;
                }
                long newLen = endAt - startFrom + 1;
                if (newLen < 0) {
                    newLen = 0;
                }

                final long dataLen = newLen;
                FileInputStream fis = new FileInputStream(file) {
                    @Override
                    public int available() throws IOException {
                        return (int) dataLen;
                    }
                };
                fis.skip(startFrom);

                httpExchange.getResponseHeaders().set("Content-Type", mime);
                httpExchange.getResponseHeaders().set("Content-Length", "" + dataLen);
                httpExchange.getResponseHeaders().set("Content-Range", "bytes " + startFrom + "-" + endAt + "/" + fileLen);
                httpExchange.getResponseHeaders().set("ETag", etag);
                httpExchange.sendResponseHeaders(206, 0);

                server.copySteam(fis, httpExchange.getResponseBody());
            }
        } else {
            if (etag.equals(httpExchange.getRequestHeaders().getFirst("if-none-match"))) {
                httpExchange.getResponseHeaders().set("Content-Type", mime);
                httpExchange.sendResponseHeaders(304, 0);
            } else {
                httpExchange.getResponseHeaders().set("Content-Type", mime);
                httpExchange.getResponseHeaders().set("Content-Length", "" + fileLen);
                httpExchange.getResponseHeaders().set("ETag", etag);
                httpExchange.sendResponseHeaders(200, 0);

                server.copySteam(new FileInputStream(file), httpExchange.getResponseBody());
            }
        }
    }
}
