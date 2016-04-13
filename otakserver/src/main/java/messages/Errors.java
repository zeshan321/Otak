package messages;

import org.json.JSONException;
import org.json.JSONObject;

public class Errors {

    public String getError(ErrorTypes error) {
        JSONObject jsonObject;
        String data = null;

        switch (error) {
            case Auth:
                try {
                    jsonObject = new JSONObject();
                    jsonObject.put("success", false);
                    jsonObject.put("error", "Incorrect password");

                    data = jsonObject.toString(2);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                return data;
        }

        return null;
    }

    public enum ErrorTypes {
        Auth
    }
}
