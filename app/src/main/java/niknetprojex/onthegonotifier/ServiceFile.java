package niknetprojex.onthegonotifier;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;
import android.view.Gravity;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import java.util.Arrays;
import java.util.Set;

public class ServiceFile extends Service {

        private BluetoothAdapter mBluetoothAdapter;
        private ArrayAdapter<String> itemsAdapter;
        private int REQUEST_ENABLE_BT = 2;
        private boolean connected = false;
        private boolean connectedSMS = false;
        private Handler myHandler;


        private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                CharSequence text = "";
                if (isSelectedDevice(device)==true) {
                    if (device.ACTION_ACL_CONNECTED.equals(action)) {
                        text = "Bluetooth device: " + device.getName() + "connected";
                        displayToast(text, context);
                        connected = true;
                        if (connectedSMS == false) {
                            delayDeparture();
                        }
                    } else if (device.ACTION_ACL_DISCONNECTED.equals(action)) {
                        text = "Bluetooth device: " + device.getName() + "disconnected";
                        displayToast(text, context);
                        connected = false;
                        if (connectedSMS == true) {
                           sendSMSMessageYe();
                            connectedSMS = false;

                        }

                    }
                }
            }
        };
        protected boolean isSelectedDevice(BluetoothDevice device){
            SharedPreferences prefer = PreferenceManager.getDefaultSharedPreferences(this);
            Set<String> abc = prefer.getStringSet("multi_choice_prefs", null);
            String [] selectedDevices = abc.toArray(new String[abc.size()]);
            if (Arrays.asList(selectedDevices).contains(device.getAddress())){
                return true;
            }
            else
                return false;

        }
        private void delayDeparture() {
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    sendSMSMessage();
                }
            };
            myHandler = new Handler();
            myHandler.postDelayed(r, 180000);
        }



    protected void displayToast (CharSequence text, Context context) {
            Toast toast = Toast.makeText(context, text, Toast.LENGTH_LONG);
            toast.setGravity(Gravity.BOTTOM|Gravity.CENTER, 0 , 0);
            toast.show();
        }
        protected void sendSMSMessage(){
            SharedPreferences prefer = PreferenceManager.getDefaultSharedPreferences(this);
            String phoneNo = prefer.getString("sms_number_preference", null);
            String message = prefer.getString("departure_text_preference", "I left");

            try {
                if (connected==true) {
                    SmsManager smsManager = SmsManager.getDefault();
                    smsManager.sendTextMessage(phoneNo, null, message, null, null);
                    connectedSMS = true;
                    Toast.makeText(getApplicationContext(), "Text Sent", Toast.LENGTH_LONG).show();
                }
                myHandler.removeCallbacksAndMessages(null);
            }
            catch (Exception e){
                Toast.makeText(getApplicationContext(), "Text Failed to Send", Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }

        }
        protected void sendSMSMessageYe(){
            SharedPreferences prefer = PreferenceManager.getDefaultSharedPreferences(this);
            String phoneNo = prefer.getString("sms_number_preference", null);
            String message = prefer.getString("arrival_text_preference", "I arrived");

            try {
                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(phoneNo, null, message , null, null);
                Toast.makeText(getApplicationContext(), "Text Sent", Toast.LENGTH_LONG).show();
            }
            catch (Exception e){
                Toast.makeText(getApplicationContext(), "Text Failed to Send", Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }

        }




    public void onCreate(){
        super.onCreate();
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter != null) {
            //If device supports bluetooth
            //check if bluetooth is enabled
            //listen for connection/disconnection events
            IntentFilter filter1 = new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED);
            IntentFilter filter2 = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED);
            this.registerReceiver(mReceiver, filter1);
            this.registerReceiver(mReceiver, filter2);
        }


    }
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_STICKY;

    }
    public void onDestroy() {
        this.unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
    }
}
