package edu.uncg.span.privacyiot;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import edu.uncg.span.privacyiot.bluetooth.BluetoothUtils;

import java.lang.ref.WeakReference;
import java.security.Key;
import java.security.KeyStore;
import java.util.ArrayList;
import edu.uncg.span.privacyiot.bluetooth.FetchBTTaskCallback;
import edu.uncg.span.privacyiot.keystore.KeyStoreUtil;

import android.util.Base64;

import javax.crypto.spec.SecretKeySpec;

public class BluetoothActivity extends AppCompatActivity implements FetchBTTaskCallback {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);
        // get the reference of RecyclerView
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        // set a LinearLayoutManager with default orientation
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(linearLayoutManager); // set LayoutManager to RecyclerView
        ArrayList<BluetoothDevice> bondedDevices = new ArrayList<BluetoothDevice>(BluetoothUtils.getBondedDevices());
        WeakReference<FetchBTTaskCallback> callbackRef = new WeakReference<FetchBTTaskCallback>(this);
        recyclerView.setAdapter(new BluetoothDevicesAdapter(this, bondedDevices,callbackRef));
    }

    @Override
    public void onEncryptionKeyReceived(String key) {
        Toast.makeText(this,"Encryption Key received "+key,Toast.LENGTH_SHORT).show();
        if(key != null) {
            try {
                byte[] keyBytes = Base64.decode(key,Base64.DEFAULT);
                Key btKey = new SecretKeySpec(keyBytes, "AES");
                KeyStore.PrivateKeyEntry entry = KeyStoreUtil.getKeyEntry(this);
                byte[] wrappedKeyBytes = KeyStoreUtil.wrapKey(entry.getCertificate().getPublicKey(), btKey);
                SharedPreferences prefs = getSharedPreferences(Constants.sharedPrefsName, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString(Constants.KEY_NAME, Base64.encodeToString(wrappedKeyBytes,Base64.DEFAULT));
                editor.commit();
            }catch(Exception ex) {

            }
        }

    }
}
