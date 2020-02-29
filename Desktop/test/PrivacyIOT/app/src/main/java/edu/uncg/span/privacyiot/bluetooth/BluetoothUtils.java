package edu.uncg.span.privacyiot.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Set;

public class BluetoothUtils {
    public static Set<BluetoothDevice> getBondedDevices() {
       BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

       return bluetoothAdapter.getBondedDevices();

    }
    public static String  getKey(String androidId, BluetoothDevice device) {

        BluetoothSocket socket = getSocketUsingReflection(device);
        String key = null;
        try {
            socket.connect();
            OutputStream outputStream = socket.getOutputStream();
            InputStream inStream = socket.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(inStream));
            String cmd = "getkey "+androidId+"\r\n";
            outputStream.write(cmd.getBytes());
            key = br.readLine();
            return key;
        } catch(IOException ex) {
            //IGNORE but log any way
            ex.printStackTrace();
        } finally {
            try {
                socket.close();
            }catch(IOException ex) {}
        }
        return key;
    }
    public static BluetoothSocket getSocketUsingReflection(BluetoothDevice device) {
        try {
            Method m = device.getClass().getMethod("createRfcommSocket", new Class[]{int.class});
            BluetoothSocket socket = (BluetoothSocket) m.invoke(device, 1);
            return socket;
        }catch(Exception ex) {

        }
        return null;
    }
}
