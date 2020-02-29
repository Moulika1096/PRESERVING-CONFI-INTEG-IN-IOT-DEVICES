package edu.uncg.span.privacyiot.webdav;


import android.os.AsyncTask;

import edu.uncg.span.privacyiot.encryption.EncryptionUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import android.util.Base64;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class FetchWebdavTask extends AsyncTask<String, Void, WebdavResponse> {
    private WeakReference<FetchWebdavTaskCallback> fetchWebdavTaskCallbackRef;
    private String username = "";
    private String password = "";
    private String encodedCredentials = Base64.encodeToString((username+":"+password).getBytes(StandardCharsets.UTF_8),Base64.DEFAULT);  //Java 8
    private String androidId;
    private byte[] btKey;
    public FetchWebdavTask(String androidId, byte[] key, WeakReference<FetchWebdavTaskCallback> callbackRef) {
        this.androidId = androidId;
        this.btKey = key;
        this.fetchWebdavTaskCallbackRef = callbackRef;
    }

    private JSONArray getKeysFrom(String keysUrlString, WebdavResponse resp) throws IOException{
        URL keysUrl = new URL(keysUrlString);
        HttpURLConnection keysUrlConn = (HttpURLConnection)keysUrl.openConnection();

        keysUrlConn.setRequestProperty("Authorization", "Basic "+encodedCredentials);
        keysUrlConn.connect();
        InputStream in = keysUrlConn.getInputStream();
        resp.status = keysUrlConn.getResponseCode();
        resp.responseText = "";
        if(resp.status == 200) {
            InputStreamReader isw = new InputStreamReader(in);
            BufferedReader br = new BufferedReader(isw);
            String line;
            StringBuilder sb = new StringBuilder();
            while ((line = br.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
                System.out.println(line);
            }
            try {
                return new JSONArray(sb.toString());
            } catch (JSONException e) {
                resp.status = 422;
                resp.responseText = "Keys file found on server but not in proper JSON format";
            }

        }
        else
            resp.responseText = "Keys file not found on server";
        return null;

    }
    private String getEncryptedKey(JSONArray keysObj) {
        String encryptedKey = null;
        for(int i=0;i<keysObj.length();i++) {
            try {
                JSONObject keyObj = keysObj.getJSONObject(i);
                if(androidId.equals(keyObj.getString("deviceid")) ) {
                    encryptedKey = keyObj.getString("key");
                    return encryptedKey;
                }
            } catch (JSONException e) {

            }
        }
        return null;
    }

    @Override
    protected WebdavResponse doInBackground(String... params) {
        WebdavResponse resp = new WebdavResponse();
        try {
            //String btkeyEncoded = params[0];
            String urlString = params[0];
            String keysUrlString = params[1];

            //First get keys file.

            JSONArray keysObj = getKeysFrom(keysUrlString,resp);
            if(keysObj == null)
                return resp;

            //Fetch encrypted key
            String encryptedKey = getEncryptedKey(keysObj);
            if(encryptedKey == null) {
                resp.status = 422;
                resp.responseText = "Encryption key in server keys for this device is empty";
                return resp;
            }
            //Decrypt encrypted key using key obtained over bluetooth

            byte[] key = EncryptionUtils.decrypt(btKey,encryptedKey);
            if(encryptedKey == null || key == null) {
                resp.status = 422;
                resp.responseText = "Unable to decrypt Encryption key ";
                return resp;
            }
            URL url = new URL(urlString);
            HttpURLConnection urlconn = (HttpURLConnection)url.openConnection();
            //String encoded = Base64.encodeToString((username+":"+password).getBytes(StandardCharsets.UTF_8),Base64.DEFAULT);  //Java 8
            urlconn.setRequestProperty("Authorization", "Basic "+encodedCredentials);
            urlconn.connect();
            InputStream in = urlconn.getInputStream();
            resp.status = urlconn.getResponseCode();
            InputStreamReader isw = new InputStreamReader(in);
            BufferedReader br = new BufferedReader(isw);
            String line;
            StringBuilder sb = new StringBuilder();
            while ( (line=br.readLine()) != null) {
                byte[] decryptedBytes = EncryptionUtils.decrypt(key,line);
                if(decryptedBytes == null)
                    continue;
                String decryptedLine = new String(decryptedBytes);
                if(decryptedLine == null) continue;
                sb.append(decryptedLine);
                sb.append("\n");
                System.out.println(line);
            }
            resp.responseText = sb.toString();
        } catch (MalformedURLException e) {
            e.printStackTrace();
            resp.status = -1;
            resp.responseText = e.getMessage();
        }catch (IOException ex) {
            ex.printStackTrace();
            resp.status = -1;
            resp.responseText = ex.getMessage();
        }


        return resp;
    }
    @Override
    protected void onPostExecute(WebdavResponse response) {

        final FetchWebdavTaskCallback callback = fetchWebdavTaskCallbackRef.get();
        if(callback != null) {
            callback.onResponseReceived(response);
        }
    }
}
