package org.worldbridge.development.wbfscoringscreen.nds;

import android.os.AsyncTask;
import android.util.Log;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.worldbridge.development.wbfscoringscreen.domain.StatusTaskDetails;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class StatusUpdateTask extends AsyncTask<StatusTaskDetails, String, String> {

    @Override
    protected String doInBackground(StatusTaskDetails... data) {
        try {
            Gson gson = new GsonBuilder()
                    .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                    .create();
            StatusTaskDetails taskDetails = data[0];

            if (taskDetails == null || taskDetails.getDestination() == null) {
                Log.e("StatusUpdateTask", "No destination data, unable to send update");
                return null;
            }

            URL url = new URL("http://" + taskDetails.getDestination().getHost()
                    + ":" + taskDetails.getDestination().getPort() + "/rest-api/screens/");

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(5000);
            conn.setConnectTimeout(5000);
            conn.setDoOutput(true);
            conn.setChunkedStreamingMode(0);

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type","application/JSON");

            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));

            writer.write(gson.toJson(taskDetails.getStatus()));

            writer.flush();
            writer.close();
            os.close();
            conn.connect();
        } catch (IOException e) {
            Log.wtf("Failed to send", e);
        }
        return null;
    }
}
