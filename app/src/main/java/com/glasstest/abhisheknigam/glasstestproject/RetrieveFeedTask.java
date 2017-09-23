package com.glasstest.abhisheknigam.glasstestproject;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Scanner;

/**
 * Created by abhisheknigam on 9/23/17.
 */

class RetrieveFeedTask extends AsyncTask<String, String, String> {

    private Exception exception;
        @Override
        protected String doInBackground(String... resource) {
            try {
                // NOTE: You must use the same region in your REST call as you used to obtain your subscription keys.
                //   For example, if you obtained your subscription keys from westcentralus, replace "westus" in the
                //   URL below with "westcentralus".
                //URIBuilder uriBuilder = new URIBuilder("https://westus.api.cognitive.microsoft.com/emotion/v1.0/recognize");

                //URI uri = uriBuilder.build();
                FileInputStream fis = null;
                try {
                    fis = new FileInputStream(resource[0]);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                Bitmap bm = BitmapFactory.decodeStream(fis);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bm.compress(Bitmap.CompressFormat.JPEG, 100 , baos);
                byte[] b = baos.toByteArray();
                System.out.print(b);

                HttpPost request = new HttpPost("https://westus.api.cognitive.microsoft.com/emotion/v1.0/recognize");
                System.out.println("URL" + request.toString());

                // Request headers. Replace the example key below with your valid subscription key.
                //request.setHeader("Content-Type", "application/json");
                request.setHeader("Content-Type", "application/octet-stream");
                request.setHeader("ocp-apim-subscription-key", "4a84beb4d5074a169e954466ccf3fc38");

                // Request body. Replace the example URL below with the URL of the image you want to analyze.
                //StringEntity reqEntity = new StringEntity("{ \"url\":" + path +"}");
                //request.setEntity(reqEntity);
                System.out.println("Request 0" + request.toString());
                request.setEntity(new ByteArrayEntity((byte[]) b));
                System.out.println("Request 1" + request.toString());

                HttpClient httpClient = new DefaultHttpClient();
                HttpResponse response = httpClient.execute(request);

                System.out.println("Response 2" + response.toString());

                HttpEntity entity = response.getEntity();

                System.out.println("Entity" + entity);
                if (entity != null) {
                    System.out.println(EntityUtils.toString(entity));
                }
            } catch (Exception e) {
                System.out.println(e);
                e.printStackTrace();
            }
            return "";
        }

        private String convertStreamToString(InputStream in) {
            Scanner s = new Scanner(in);
            return s.useDelimiter("\\A").hasNext() ? s.next() : "";
        }
    }

