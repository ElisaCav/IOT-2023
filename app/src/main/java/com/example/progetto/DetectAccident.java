package com.example.progetto;

import android.content.Context;
import android.content.Intent;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.List;

public class DetectAccident {

    // Metodo per rilevare un incidente
    public static void detectAccident(Context context, float averageAcceleration) {

        // Verifica se l'accelerazione media supera una certa soglia per determinare se c'Ã¨ stato un incidente
        if (averageAcceleration > 30) {
            // Se l'accelerazione supera la soglia, avvia l'activity EmergencyHandlerActivity
            Intent intent = new Intent(context, EmergencyHandlerActivity.class);
            intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }

    public static float calculateAverageAcceleration(List<Float[]> window) {
        float sumX = 0, sumY = 0, sumZ = 0;
        int count = window.size();

        for (Float[] values : window) {
            sumX += Math.abs(values[0]);
            sumY += Math.abs(values[1]);
            sumZ += Math.abs(values[2]); //- 9.8; // Sottrai la gravitÃ 
        }

        float averageX = sumX / count;
        float averageY = sumY / count;
        float averageZ = sumZ / count;
        //norma euclidea
        return (float) Math.sqrt(averageX * averageX + averageY * averageY + averageZ * averageZ);
    }

    public static float mlDetect (List<Float[]> window) {
        sendPostRequestWithArray(window);
        return 0;
    }

    public static void sendPostRequestWithArray(List<Float[]> dataArrayList) {
        try {
            String urlString = "<INSERT URL HERE>";
            URL url = new URL(urlString);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

            // Set connection properties
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Type", "application/json");

            // Create JSON array of arrays
            JSONArray jsonArray = new JSONArray();
            for (Float[] dataArray : dataArrayList) {
                JSONArray innerArray = new JSONArray();
                for (Float value : dataArray) {
                    innerArray.put(value);
                }
                jsonArray.put(innerArray);
            }

            // Create JSON object with the array
            JSONObject jsonParam = new JSONObject();
            jsonParam.put("data", jsonArray);

            // Enable writing to the connection output stream
            urlConnection.setDoOutput(true);

            // Write data to the connection output stream
            OutputStream outputStream = new BufferedOutputStream(urlConnection.getOutputStream());
            outputStream.write(jsonParam.toString().getBytes());
            outputStream.flush();
            outputStream.close();

            // Get response from the server
            int responseCode = urlConnection.getResponseCode();
            Log.d("API RESPONSE", String.valueOf(responseCode));

            // Handle response if needed
            BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            Log.d("API RESPONSE", response.toString());

            // Close the connection
            urlConnection.disconnect();
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            Log.d("API ERRORE", e.toString());
        }
    }

}



