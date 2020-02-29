package edu.uncg.span.privacyiot;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.provider.Settings;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import edu.uncg.span.privacyiot.keystore.KeyStoreUtil;
import edu.uncg.span.privacyiot.webdav.FetchWebdavTask;
import edu.uncg.span.privacyiot.webdav.FetchWebdavTaskCallback;
import edu.uncg.span.privacyiot.webdav.WebdavResponse;

import java.lang.ref.WeakReference;
import java.security.Key;
import java.security.KeyStore;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements FetchWebdavTaskCallback{
    private Button editText;
    private Button button;
    private TextView textView;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
    private View.OnClickListener onClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
                Calendar c = Calendar.getInstance();
                String dateStr = editText.getText().toString();
                try {
                    Date date = sdf.parse(dateStr);
                    c.setTime(date);// all done
                }catch(ParseException pex) {

                }
                int year = c.get(Calendar.YEAR);
                int month = c.get(Calendar.MONTH);
                int dayOfMonth = c.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog dpDlg = new DatePickerDialog(v.getContext(),new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        int month_mod = month +1;

                        editText.setText(year + "-" + String.format("%02d", month_mod) + "-" + String.format("%02d", dayOfMonth));
                    }
                }, year,month,
                 dayOfMonth);
                dpDlg.show();
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        editText = (Button)findViewById(R.id.dateText);
        setCurrentDate();
        button = (Button) findViewById(R.id.button);
        textView = (TextView) findViewById(R.id.textView2);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WeakReference<FetchWebdavTaskCallback> callbackRef = new WeakReference<FetchWebdavTaskCallback>(MainActivity.this);
                byte[] key = getKey(MainActivity.this);
                String dataurl = ""+editText.getText().toString()+".txt";
                String keysurl = "";
                String[] params = {dataurl,keysurl};
                String androidId = Settings.Secure.getString(v.getContext().getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
                new FetchWebdavTask(androidId, key,callbackRef).execute(params);

            }
        });


        editText.setOnClickListener(onClickListener);
    }

    private void setCurrentDate() {

        Date currentTime = Calendar.getInstance().getTime();
        String datestr = sdf.format(currentTime);
        editText.setText(datestr);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_getkey) {

            Intent intent = new Intent(this,BluetoothActivity.class);
            this.startActivity(intent);

            return true;
        } else if (id == R.id.action_settings) {

            Intent btSettingsIntent = new Intent();
            btSettingsIntent.setAction(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
            startActivity(btSettingsIntent);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onResponseReceived(WebdavResponse response) {
        if(response.status == 200) {
            textView.setText(response.responseText);
        } else {
            Toast.makeText(this,"Error occurred "+response.responseText,Toast.LENGTH_SHORT).show();
        }
    }

    private byte[] getKey(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(Constants.sharedPrefsName, Context.MODE_PRIVATE);
        String encodedKey = prefs.getString(Constants.KEY_NAME,null);
        if(encodedKey == null) return null;

        byte[] wrappedKeyBytes = Base64.decode(encodedKey,Base64.DEFAULT);
        try {
            KeyStore.PrivateKeyEntry entry = KeyStoreUtil.getKeyEntry(MainActivity.this);
            Key unWrappedKey = KeyStoreUtil.unWrapKey(entry.getPrivateKey(),wrappedKeyBytes);
            return unWrappedKey.getEncoded();
        }catch (Exception e) {

        }
        return null;
    }
}
