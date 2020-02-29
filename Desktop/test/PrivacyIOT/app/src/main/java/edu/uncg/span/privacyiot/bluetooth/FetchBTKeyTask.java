package edu.uncg.span.privacyiot.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.os.AsyncTask;

import java.io.IOException;
import java.lang.ref.WeakReference;

public class FetchBTKeyTask extends AsyncTask<BluetoothDevice, Void, String> {
    private WeakReference<FetchBTTaskCallback> fetchBTTaskCallbackRef;
    private String androidId;
    public FetchBTKeyTask(String androidId, WeakReference<FetchBTTaskCallback> callbackRef) {
        this.androidId = androidId;
        this.fetchBTTaskCallbackRef = callbackRef;
    }
    @Override
    protected String doInBackground(BluetoothDevice... bluetoothDevices) {
            return BluetoothUtils.getKey(androidId,bluetoothDevices[0]);
    }
    @Override
    protected void onPostExecute(String key) {

        final FetchBTTaskCallback callback = fetchBTTaskCallbackRef.get();
        if(callback != null) {
            callback.onEncryptionKeyReceived(key);
        }
    }
}
