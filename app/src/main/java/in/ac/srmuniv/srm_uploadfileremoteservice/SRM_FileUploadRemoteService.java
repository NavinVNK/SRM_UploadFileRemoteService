package in.ac.srmuniv.srm_uploadfileremoteservice;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.StrictMode;
import android.util.Log;

public class SRM_FileUploadRemoteService extends Service {
    private static final String SRMUNIV_INTENT_ACTION_BIND_MESSAGE_SERVICE = "srmuniv.intent.action.bindMessageService";
    private final static String LOG_TAG = SRM_FileUploadRemoteService.class
            .getCanonicalName();

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(LOG_TAG, "The SRM_FileUploadRemoteService was created.");
    }

    @Override
    public void onDestroy() {
        Log.d(LOG_TAG, "The SRM_FileUploadRemoteService was destroyed.");
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        if (SRMUNIV_INTENT_ACTION_BIND_MESSAGE_SERVICE.equals(intent
                .getAction())) {
            Log.d(LOG_TAG, "The SRM_FileUploadRemoteService was binded.");
            return new UploadFileService();
        }
        return null;
    }

    String response = null;

    String uploadForRemoteService(String sourceFileUri) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        String upLoadServerUri = "http://navinsandroidtutorial.comlu.com/upload/upload.php";
        String fileName = sourceFileUri;
        Log.e("uploadFile", sourceFileUri);
        HttpURLConnection conn = null;
        DataOutputStream dos = null;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;
        File sourceFile = new File(sourceFileUri);
        if (!sourceFile.isFile()) {
            Log.e("uploadFile", "Source File Does not exist");
            response = "Source File Does not exist";
            return response;
        }
        try { // open a URL connection to the PHP
            FileInputStream fileInputStream = new FileInputStream(sourceFile);
            URL url = new URL(upLoadServerUri);
            conn = (HttpURLConnection) url.openConnection(); // Open a HTTP
            // connection to the URL
            conn.setDoInput(true); // Allow Inputs
            conn.setDoOutput(true); // Allow Outputs
            conn.setUseCaches(false); // Don't use a Cached Copy
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("ENCTYPE", "multipart/form-data");
            conn.setRequestProperty("Content-Type",
                    "multipart/form-data;boundary=" + boundary);
            conn.setRequestProperty("uploaded_file", fileName);
            dos = new DataOutputStream(conn.getOutputStream());
            dos.writeBytes(twoHyphens + boundary + lineEnd);
            dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""
                    + fileName + "\"" + lineEnd);
            dos.writeBytes(lineEnd);
            bytesAvailable = fileInputStream.available(); // create a buffer ofmaximum size
            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            buffer = new byte[bufferSize];
            // read file and write it into form...
            bytesRead = fileInputStream.read(buffer, 0, bufferSize);
            while (bytesRead > 0) {
                dos.write(buffer, 0, bufferSize);
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
            }

            // send multipart form data necesssary after file data...
            dos.writeBytes(lineEnd);
            dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

            // Responses from the server (code and message)
            int serverResponseCode = conn.getResponseCode();
            String serverResponseMessage = conn.getResponseMessage();

            Log.i("uploadFile", "HTTP Response is : " + serverResponseMessage
                    + ": " + serverResponseCode);

            if (serverResponseCode == 200)
                response = "File uploaded Sucessfully";
            else
                response = "File not uploaded Sucessfully";

            // close the streams //
            fileInputStream.close();
            dos.flush();
            dos.close();

        } catch (MalformedURLException ex) {
            response = "Url not found";
            ex.printStackTrace();
            Log.e("Upload file to server", "error: " + ex.getMessage(), ex);

        } catch (IOException e) {
            e.printStackTrace();
            response = "error in upload";
            Log.e("Server Exception","Exception : " + e.getMessage(), e);
        }
        return response;
    }

    public class UploadFileService extends IRemoteFileUploadService.Stub {
        @Override
        public String uploadFile(String url) throws RemoteException {
            return uploadForRemoteService(url);
        }
    }

}