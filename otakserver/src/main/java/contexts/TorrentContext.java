package contexts;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.turn.ttorrent.client.Client;
import com.turn.ttorrent.client.SharedTorrent;
import com.zeshanaslam.otak.Main;
import messages.Errors;
import org.json.JSONException;
import org.json.JSONObject;
import utils.Config;
import utils.ServerUtils;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

public class TorrentContext implements HttpHandler {

    private Config config = Main.config;

    @Override
    public void handle(final HttpExchange httpExchange) throws IOException {
        final ServerUtils server = new ServerUtils();

        final Map<String, String> params = server.queryToMap(httpExchange.getRequestURI().getRawQuery());

        // Auth connection
        if (!params.containsKey("pass") || !params.get("pass").equals(config.getString("pass"))) {
            server.writeResponse(httpExchange, new Errors().getError(Errors.ErrorTypes.AUTH));
            return;
        }

        // Params check
        if (!params.containsKey("file") || !params.containsKey("sender")) {
            server.writeResponse(httpExchange, new Errors().getError(Errors.ErrorTypes.MISSING));
            return;
        }

        try {
            File torrent = new File(config.getString("dir") + File.separator + params.get("file"));

            Client client = new Client(InetAddress.getLocalHost(), SharedTorrent.fromFile(
                    torrent,
                    new File(config.getString("dir"))));

            client.addObserver(new Observer() {
                @Override
                public void update(Observable observable, Object data) {
                    Client client = (Client) observable;
                    float progress = client.getTorrent().getCompletion();
                    System.out.println(progress);
                }
            });

            client.setMaxDownloadRate(0.0);
            client.setMaxUploadRate(0.0);
            client.share(0);

            client.download();
            client.waitForCompletion();

            server.writeResponse(httpExchange, returnData(true));
        } catch (NoSuchAlgorithmException e) {
            server.writeResponse(httpExchange, returnData(false));
        }
    }

    private String returnData(boolean status) {
        JSONObject jsonObject;
        String data = null;

        try {
            jsonObject = new JSONObject();
            jsonObject.put("success", status);
            data = jsonObject.toString(2);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return data;
    }

}
