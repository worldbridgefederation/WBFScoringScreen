package org.worldbridge.development.wbfscoringscreen;

import android.util.Log;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.worldbridge.development.wbfscoringscreen.domain.StatusResponse;
import org.worldbridge.development.wbfscoringscreen.domain.StatusTaskDetails;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class StatusTaskHelper {
    public StatusResponse doStatusUpdate(StatusTaskDetails taskDetails) {
        HttpURLConnection conn = null;
        try {
            Gson gson = new GsonBuilder()
                    .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                    .create();


            if (taskDetails == null || taskDetails.getDestination() == null) {
                Log.e("StatusUpdateTask", "No destination data, unable to send update");
                return null;
            }

            URL url = new URL("http://" + taskDetails.getDestination().getHost()
                    + ":" + taskDetails.getDestination().getPort() + "/rest-api/screens/");

            conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(5000);
            conn.setConnectTimeout(5000);
            conn.setDoOutput(true);
            conn.setChunkedStreamingMode(0);

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type","application/JSON");

            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));

            String jsonContent = gson.toJson(taskDetails.getStatus());
            writer.write(jsonContent);

            writer.flush();
            writer.close();
            os.close();

            int status = conn.getResponseCode();
            if (status >= 300) {
                Log.e("StatusUpdateTask", "Not expecting a " + status + " response...");

                return null;
            } else {
                ByteArrayOutputStream sink = new ByteArrayOutputStream();
                copy(conn.getInputStream(), sink, 3000);
                byte[] downloadedFile = sink.toByteArray();

                String rawContent =  new String(downloadedFile, "UTF8");
                if (rawContent.isEmpty()) {
                    Log.i("StatusUpdateTask", "No data received from server");
                    return null;
                }
                return gson.fromJson(rawContent, StatusResponse.class);
            }

        } catch (IOException e) {
            Log.w("StatusUpdateTask", "Failed to send status update");
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        return null;
    }

    private static void copy(InputStream in, OutputStream out , int bufferSize)
            throws IOException
    {
        // Read bytes and write to destination until eof
        byte[] buf = new byte[bufferSize];
        int len = 0;
        while ((len = in.read(buf)) >= 0)
        {
            out.write(buf, 0, len);
        }
    }

}
