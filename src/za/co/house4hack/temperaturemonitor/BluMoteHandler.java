package za.co.house4hack.temperaturemonitor;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

// The Handler that gets information back from the BluetoothService
public class BluMoteHandler extends Handler {
   // Debugging
   private static final String TAG = "BlueTempHandler";
   private static final boolean D = true;
   
   // Key names received from the BluetoothService Handler
   public static final String DEVICE_NAME = "device_name";
   public static final String TOAST = "toast";
   
   // Message types sent from the BluetoothService Handler
   public static final int MESSAGE_STATE_CHANGE = 1;
   public static final int MESSAGE_READ = 2;
   public static final int MESSAGE_WRITE = 3;
   public static final int MESSAGE_DEVICE_NAME = 4;
   public static final int MESSAGE_TOAST = 5;   

   protected Activity context;

   // Name of the connected device
   private String mConnectedDeviceName = null;
   
   public BluMoteHandler(Activity context) {
      this.context = context;
   }
   
   @Override
   public void handleMessage(Message msg) {
      switch (msg.what) {
         case MESSAGE_STATE_CHANGE:
            if (D) Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
            switch (msg.arg1) {
               case BluetoothService.STATE_CONNECTED:
                  Toast.makeText(context, "Connected " + msg.getData().getString(TOAST), Toast.LENGTH_SHORT).show();
                  //sendBluetooth("*");
                  break;
               case BluetoothService.STATE_CONNECTING:
                  // Toast.makeText(getApplicationContext(),
                  // msg.getData().getString(TOAST),
                  // Toast.LENGTH_SHORT).show();
                  break;
               case BluetoothService.STATE_LISTEN:
               case BluetoothService.STATE_NONE:
                  // Toast.makeText(getApplicationContext(),
                  // msg.getData().getString(TOAST),
                  // Toast.LENGTH_SHORT).show();
                  break;
            }
            break;
         case MESSAGE_WRITE:
            byte[] writeBuf = (byte[]) msg.obj;
            // construct a string from the buffer
            String writeMessage = new String(writeBuf);
            if (D) Toast.makeText(context, writeMessage, Toast.LENGTH_SHORT).show();
            break;
         case MESSAGE_READ:
            byte[] readBuf = (byte[]) msg.obj;
            // construct a string from the valid bytes in the buffer
            String readMessage = new String(readBuf, 0, msg.arg1);
            if (D) Toast.makeText(context, mConnectedDeviceName + ":  " + readMessage, Toast.LENGTH_SHORT).show();
            //processCommand(new String(readBuf));
            break;
         case MESSAGE_DEVICE_NAME:
            // save the connected device's name
            mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
            if (D && mConnectedDeviceName != null) {
               Toast.makeText(context, "Connected to " + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
            }
            break;
         case MESSAGE_TOAST:
            Toast.makeText(context, msg.getData().getString(TOAST), Toast.LENGTH_SHORT).show();
            break;
      }
   }
 

}
