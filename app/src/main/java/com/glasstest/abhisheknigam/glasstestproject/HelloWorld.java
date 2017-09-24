package com.glasstest.abhisheknigam.glasstestproject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.FileObserver;
import android.provider.MediaStore;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.ImageView;

import com.google.android.glass.content.Intents;
import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;
import com.google.android.glass.widget.CardBuilder;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;


public class HelloWorld extends Activity {

    private static final int TAKE_PICTURE_REQUEST = 1;
    private static final int TAKE_VIDEO_REQUEST = 2;
    public static final String SHARE_PICTURE = "picture";
    private GestureDetector mGestureDetector = null;
    private CameraView cameraView;

    private FileObserver observer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Initiate CameraView
        cameraView = new CameraView(this);

        // Turn on Gestures
        mGestureDetector = createGestureDetector(this);

        setContentView(cameraView);
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (cameraView != null) {
            cameraView.releaseCamera();
        }

        //setContentView(cameraView);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (cameraView != null) {
            cameraView.releaseCamera();
        }
    }

    private GestureDetector createGestureDetector(Context context) {
        GestureDetector gestureDetector = new GestureDetector(context);

        gestureDetector.setBaseListener(new GestureDetector.BaseListener() {
            @Override
            public boolean onGesture(Gesture gesture) {
                // Make sure view is initiated
                if (cameraView != null) {
                    // Tap with a single finger for photo
                    if (gesture == Gesture.TAP) {
                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        if (intent != null) {
                            startActivityForResult(intent, TAKE_PICTURE_REQUEST);
                        }

                        return true;
                    }

                    // Tap with 2 fingers for video
                    /*else if (gesture == Gesture.TWO_TAP) {
                        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                        if (intent != null) {
                            startActivityForResult(intent, TAKE_VIDEO_REQUEST);
                        }
                        return true;
                    }*/
                }

                return false;
            }
        });

        return gestureDetector;
    }

    /*
	 * Send generic motion events to the gesture detector
	 */
    @Override
    public boolean onGenericMotionEvent(MotionEvent event)
    {
        if (mGestureDetector != null)
        {
            return mGestureDetector.onMotionEvent(event);
        }

        return false;
    }

    /*
     * (non-Javadoc)
     * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == TAKE_PICTURE_REQUEST && resultCode == RESULT_OK) {
            String thumbnailPath = data.getStringExtra(Intents.EXTRA_THUMBNAIL_FILE_PATH);
            //String picturePath = data.getStringExtra(Intents.EXTRA_PICTURE_FILE_PATH);

            processPictureWhenReady(thumbnailPath);
            // TODO: Show the thumbnail to the user while the full picture is being
            // processed.
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void processPictureWhenReady(final String picturePath) {
        final File pictureFile = new File(picturePath);
        if (pictureFile.exists()) {
//                System.out.println("pddir:"+picturePath.substring(0,picturePath.lastIndexOf("/")));
//                File picturedir = new File(picturePath.substring(0,picturePath.lastIndexOf("/")));
//                picturedir.mkdirs();
//                String[] paths = picturePath.split("/");
            //picturedir, picturePath.split("/")[paths.length-1]
            //System.out.print(picturePath + "      pd:" + picturedir.getAbsolutePath());
            try {
                String output = new RetrieveFeedTask().execute(picturePath).get();
                if(output.isEmpty()){
                    output="neutral";
                }
                //CardScrollView csv = new CardScrollView(this);
                cameraView.destroyDrawingCache();
                cameraView = null;
                /*CardBuilder card = new CardBuilder(this, Card);
                card.setText(output);*/
                ImageView card = new ImageView(this);

                card.setImageURI(Uri.parse("android.resource://com.glasstest.abhisheknigam.glasstestproject/drawable/"+output));
                setContentView(card);
                final Context c = this;
                card.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // Initiate CameraView
                        cameraView = new CameraView(c);

                        // Turn on Gestures
                        mGestureDetector = createGestureDetector(c);

                        setContentView(cameraView);
                        //getNetworkData(picturePath);
                    }
                }, 2000);
                card.destroyDrawingCache();
                card = null;
            }catch(Exception e){
                System.out.println(e);
                e.printStackTrace();
            }



        } else {
            // The file does not exist yet. Before starting the file observer, you
            // can update your UI to let the user know that the application is
            // waiting for the picture (for example, by displaying the thumbnail
            // image and a progress indicator).

            final File parentDirectory = pictureFile.getParentFile();
            FileObserver observer = new FileObserver(parentDirectory.getPath(),
                    FileObserver.CLOSE_WRITE | FileObserver.MOVED_TO) {
                // Protect against additional pending events after CLOSE_WRITE
                // or MOVED_TO is handled.
                private boolean isFileWritten;

                @Override
                public void onEvent(int event, String path) {
                    if (!isFileWritten) {
                        // For safety, make sure that the file that was created in
                        // the directory is actually the one that we're expecting.
                        File affectedFile = new File(parentDirectory, path);
                        isFileWritten = affectedFile.equals(pictureFile);

                        if (isFileWritten) {
                            stopWatching();
                            //getNetworkData(path);
                            // Now that the file is ready, recursively call
                            // processPictureWhenReady again (on the UI thread).
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    processPictureWhenReady(picturePath);
                                }
                            });
                        }
                    }
                }
            };
            observer.startWatching();
        }
    }

//    public void getNetworkData(String path, byte[] b)
//    {
//        try
//        {
//            // NOTE: You must use the same region in your REST call as you used to obtain your subscription keys.
//            //   For example, if you obtained your subscription keys from westcentralus, replace "westus" in the
//            //   URL below with "westcentralus".
//            //URIBuilder uriBuilder = new URIBuilder("https://westus.api.cognitive.microsoft.com/emotion/v1.0/recognize");
//
//            //URI uri = uriBuilder.build();
//
//            HttpPost request = new HttpPost("https://westus.api.cognitive.microsoft.com/emotion/v1.0/recognize");
//            System.out.println("URL" + request.toString());
//
//            // Request headers. Replace the example key below with your valid subscription key.
//            //request.setHeader("Content-Type", "application/json");
//            request.setHeader("Content-Type", "application/octet-stream");
//            request.setHeader("ocp-apim-subscription-key", "4a84beb4d5074a169e954466ccf3fc38");
//
//            // Request body. Replace the example URL below with the URL of the image you want to analyze.
//            //StringEntity reqEntity = new StringEntity("{ \"url\":" + path +"}");
//            //request.setEntity(reqEntity);
//            System.out.println("Request 0" + request.toString());
//            request.setEntity(new ByteArrayEntity((byte[])b));
//            System.out.println("Request 1" + request.toString());
//
//            HttpClient httpClient = new DefaultHttpClient();
//            HttpResponse response = httpClient.execute(request);
//
//            System.out.println("Response 2" + response.toString());
//
//            HttpEntity entity = response.getEntity();
//
//            System.out.println("Entity"  + entity);
//            if (entity != null)
//            {
//                System.out.println(EntityUtils.toString(entity));
//            }
//        }
//        catch (Exception e)
//        {
//            System.out.println(e);
//            e.printStackTrace();
//        }
//    }

    private String getFileURL(String path){
        try {
            URL url = new URL("https://upload.uploadcare.com/base/");
            URI uri = url.toURI();
            Map<String,String> otherparams = new HashMap<String, String>();
            otherparams.put("UPLOADCARE_PUB_KEY","fda62941114df266eab8");
            otherparams.put("UPLOADCARE_STORE","1");
            //String res = multipartRequest(url, otherparams, path, "file", getMimeType(path));
            return null;
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
        return "";
    }

//    public String getMimeType(String path) {
//        String mimeType = null;
//        if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
//            ContentResolver cr = getAppContext().getContentResolver();
//            mimeType = cr.getType(uri);
//        } else {
//            String fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri
//                    .toString());
//            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
//                    fileExtension.toLowerCase());
//        }
//        return mimeType;
//    }

//    public String multipartRequest(String urlTo, Map<String, String> parmas, String filepath, String filefield, String fileMimeType) throws CustomException {
//        HttpURLConnection connection = null;
//        DataOutputStream outputStream = null;
//        InputStream inputStream = null;
//
//        String twoHyphens = "--";
//        String boundary = "*****" + Long.toString(System.currentTimeMillis()) + "*****";
//        String lineEnd = "\r\n";
//
//        String result = "";
//
//        int bytesRead, bytesAvailable, bufferSize;
//        byte[] buffer;
//        int maxBufferSize = 1 * 1024 * 1024;
//
//        String[] q = filepath.split("/");
//        int idx = q.length - 1;
//
//        try {
//            File file = new File(filepath);
//            FileInputStream fileInputStream = new FileInputStream(file);
//
//            URL url = new URL(urlTo);
//            connection = (HttpURLConnection) url.openConnection();
//
//            connection.setDoInput(true);
//            connection.setDoOutput(true);
//            connection.setUseCaches(false);
//
//            connection.setRequestMethod("POST");
//            connection.setRequestProperty("Connection", "Keep-Alive");
//            connection.setRequestProperty("User-Agent", "Android Multipart HTTP Client 1.0");
//            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
//
//            outputStream = new DataOutputStream(connection.getOutputStream());
//            outputStream.writeBytes(twoHyphens + boundary + lineEnd);
//            outputStream.writeBytes("Content-Disposition: form-data; name=\"" + filefield + "\"; filename=\"" + q[idx] + "\"" + lineEnd);
//            outputStream.writeBytes("Content-Type: " + fileMimeType + lineEnd);
//            outputStream.writeBytes("Content-Transfer-Encoding: binary" + lineEnd);
//
//            outputStream.writeBytes(lineEnd);
//
//            bytesAvailable = fileInputStream.available();
//            bufferSize = Math.min(bytesAvailable, maxBufferSize);
//            buffer = new byte[bufferSize];
//
//            bytesRead = fileInputStream.read(buffer, 0, bufferSize);
//            while (bytesRead > 0) {
//                outputStream.write(buffer, 0, bufferSize);
//                bytesAvailable = fileInputStream.available();
//                bufferSize = Math.min(bytesAvailable, maxBufferSize);
//                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
//            }
//
//            outputStream.writeBytes(lineEnd);
//
//            // Upload POST Data
//            Iterator<String> keys = parmas.keySet().iterator();
//            while (keys.hasNext()) {
//                String key = keys.next();
//                String value = parmas.get(key);
//
//                outputStream.writeBytes(twoHyphens + boundary + lineEnd);
//                outputStream.writeBytes("Content-Disposition: form-data; name=\"" + key + "\"" + lineEnd);
//                outputStream.writeBytes("Content-Type: text/plain" + lineEnd);
//                outputStream.writeBytes(lineEnd);
//                outputStream.writeBytes(value);
//                outputStream.writeBytes(lineEnd);
//            }
//
//            outputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
//
//
//            if (200 != connection.getResponseCode()) {
//                throw new CustomException("Failed to upload code:" + connection.getResponseCode() + " " + connection.getResponseMessage());
//            }
//
//            inputStream = connection.getInputStream();
//
//            result = this.convertStreamToString(inputStream);
//
//            fileInputStream.close();
//            inputStream.close();
//            outputStream.flush();
//            outputStream.close();
//
//            return result;
//        } catch (Exception e) {
//            logger.error(e);
//            throw new CustomException(e);
//        }
//
//    }

    private String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

//    @Override
//    protected void onResume() {
//        super.onResume();
//        mCardScroller.activate();
//    }
//
//    @Override
//    protected void onPause() {
//        mCardScroller.deactivate();
//        super.onPause();
//    }

    /**
     * Builds a Glass styled "Hello World!" view using the {@link CardBuilder} class.
     */
//    private View buildView() {
//        CardBuilder card = new CardBuilder(this, CardBuilder.Layout.TEXT);
//
//        card.setText(R.string.hello_world);
//        return card.getView();
//    }

}
