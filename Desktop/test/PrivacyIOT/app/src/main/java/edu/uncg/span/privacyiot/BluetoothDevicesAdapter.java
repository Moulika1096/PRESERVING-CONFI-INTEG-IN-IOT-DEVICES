package edu.uncg.span.privacyiot;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.Adapter;

import edu.uncg.span.privacyiot.bluetooth.FetchBTKeyTask;
import edu.uncg.span.privacyiot.bluetooth.FetchBTTaskCallback;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class BluetoothDevicesAdapter extends Adapter {
    private Context context;
    private ArrayList<BluetoothDevice> bondedDevices;
    private WeakReference<FetchBTTaskCallback> callbackRef;
    public BluetoothDevicesAdapter(Context context, ArrayList<BluetoothDevice> bondedDevices, WeakReference<FetchBTTaskCallback> callbackRef) {
        this.context = context;
        this.bondedDevices = bondedDevices;
        this.callbackRef = callbackRef;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.rowlayout, parent, false);
        // set the view's size, margins, paddings and layout parameters
        MyViewHolder vh = new MyViewHolder(v); // pass the view to View Holder
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        // set the data in items
        final BluetoothDevice device = bondedDevices.get(position);
        final String txt = TextUtils.isEmpty(device.getName())?device.getAddress(): device.getName();
        ((MyViewHolder)holder).name.setText(txt);
        // implement setOnClickListener event on item view.
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // display a toast with person name on item click
                Toast.makeText(context, txt, Toast.LENGTH_SHORT).show();
                String androidId = Settings.Secure.getString(context.getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
                new FetchBTKeyTask(androidId,callbackRef).execute(device);
            }
        });
    }

    @Override
    public int getItemCount() {
        return bondedDevices.size();
    }
    class MyViewHolder extends RecyclerView.ViewHolder {
        // init the item view's
        TextView name;

        public MyViewHolder(View itemView) {
            super(itemView);
            // get the reference of item view's
            name = (TextView) itemView.findViewById(R.id.name);
        }
    }
}
