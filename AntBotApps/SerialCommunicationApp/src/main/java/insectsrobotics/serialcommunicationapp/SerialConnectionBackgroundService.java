package insectsrobotics.serialcommunicationapp;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.util.SerialInputOutputManager;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import insectrobotics.broadcastlibrary.BroadcastService;
import insectsrobotics.serialcommunicationapp.DataManager.SerialManager.SerialInOutManager;
import insectsrobotics.serialcommunicationapp.DataManager.ServerManager.ServerConnection;
import insectsrobotics.serialcommunicationapp.Receiver_and_Broadcaster.Broadcast;
import insectsrobotics.serialcommunicationapp.Receiver_and_Broadcaster.Receive;

/**
 * Main Class, Background Service
 *
 * This class binds to the AntEye Application and implements all Listeners.
 * It also starts the Serial connection and initializes the Server connection.
 */

public class SerialConnectionBackgroundService extends BroadcastService {

    private final String TAG = SerialConnectionBackgroundService.class.getSimpleName();

    Receive receive;
    Broadcast broadcast;

    //Needed Objects for Serial-Communication
    private UsbManager mUsbManager;
    List<UsbSerialDriver> availableDrivers;
    private static UsbSerialPort sPort = null;
    UsbSerialDriver driver;
    UsbManager usbManager;
    UsbDeviceConnection connection;
    //Connection Handler to run the connection process on different Thread in 1sec intervals
    Handler loadConnectionHandler = new Handler();
    //Keeps the serial connection open in different Thread
    private final ExecutorService inExecutor = Executors.newSingleThreadExecutor();

    //Classes to Manage the Data coming and going to the Serial connection
    SerialInOutManager serialInOutManager;
    //and the Server connection
    ServerConnection serverConnection;

    //Decision broadcast from the Combiner application to send to the Arduino Board
    String decision;

    //Gets set true, if the SerialIOManager is stopped and therefore stops all attempts to connect
    // to the Arduino board (See Runnable below onUnbind Method).
    boolean stopRunnable = false;

    Context context = this;

    /**
     * Listener for the com.hoho.android.usbserial Library
     * gets the Byte array from the Arduino Board and sends it to the own SerialInOutManager.
     */
    private final SerialInputOutputManager.SerialInListener mListener =
            new SerialInputOutputManager.SerialInListener() {

                @Override
                public void onNewData(final byte[] data) {
                    try {
                        serialInOutManager.onNewData(data);
                    } catch(NullPointerException e){
                        e.printStackTrace();
                    }
                }

                @Override
                public void onRunError(Exception e) {
                    Log.d(TAG, "Runner stopped.");
                }

            };

    /**
     * Listener for the Serial connection. Gets the String data from the ServerConnection class and
     * decides what to do with it. If it contains "Serial", it immediately passes the data on to
     * the SerialInOutManager to give it to the Arduino.
     *
     * If it contains "Toast" it means, that there still is no ServerConnection and therefore it makes
     * a Toast for the User to let him know, that something is wring with the Connection.
     *
     * If it contains "VNA" it means, that the data from the Server is has a command to learn an image,
     * calculate a specific error or to reset the learned networks. Therefore it gets passed on to
     * the VisualNavigationApp
     *
     * If it contains "Combiner" it means, that the User decided to start/stop homing or
     * route-following and therefore the message gets passed on to the combiner to start/ stop
     * the process.
     *
     * onConnectionChanged sends a broadcast to the AntEye application to give the user an update,
     * that the server connection is established/ is closed.
     */
    private final ServerConnection.ServerConnectionListener serverListener =
            new ServerConnection.ServerConnectionListener() {
                @Override
                public void onNewMessage(String msg) {
                    Log.e("onNewMessage", msg + " SerialInOut: " + serialInOutManager);
                    if (msg.contains("Serial")){
                        msg = msg.replace("Serial","");
                        if (serialInOutManager != null) {
                            serialInOutManager.sendData(msg);
                        }
                    } else if (msg.contains("Toast")){
                        msg = msg.replace("Toast", "");
                        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
                    } else if (msg.contains("Combiner")){
                        msg = msg.replace("Combiner", "");
                        if (broadcast != null){
                            broadcast.broadcastHomingRoutingCommand(msg);
                        }
                    }
                }

                @Override
                public void onConnectionChanged(boolean connectionState) {
                    broadcast.broadcastServerConnectionEstablished(connectionState);
                }

                @Override
                public void onRunError(Exception e) {

                }
            };

    /**
     * This Listener manages all the in between app broadcast receives. The Listener is declared in
     * insectsrobotics.Receiver_and_Broadcaster._Superclasses.Receiver
     * It checks the action of the received intent and processes it accordingly.
     *
     * When it gets the Server data (IP-Address and Port) it starts the server connection.
     * When it gets a new Image from the AntEye Application, it passes it on to the server.
     *
     * If it gets a message from the combiner for the next Motor commands it passes it on to the
     * Arduino
     */
    Receive.ReceiveListener receiveListener = new Receive.ReceiveListener() {
        @Override
        public void onNewMessageFromSerialConnectionApp(Intent intent, String action) {
        }

        @Override
        public void onNewMessageFromCombinerApp(Intent intent, String action) {
            switch (action){
                case DECISION:
                    //parseDecision(intent.getStringExtra("MainData"));
                    Log.e(TAG,intent.getStringExtra("MainData"));
                    serialInOutManager.sendData(intent.getStringExtra("MainData"));
                    break;
                default:
                    //Here could go some default stuff.
                    break;
            }
        }

        private String parseDecision(String decision){
            //TODO: parse the decision or maybe not depending on the combiner
            return decision;
        }

        @Override
        public void onNewMessageFromIntegratorApp(Intent intent, String action) {
        }

        @Override
        public void onNewMessageFromVisualNavigationApp(Intent intent, String action) {
        }

        @Override
        public void onNewMessageFromAntEyeApp(Intent intent, String action) {
            switch (action){
                case IMAGE_SERVER:
                    if (serverConnection!=null){
                        byte [] image = intent.getByteArrayExtra("MainData");
                        serverConnection.sendImageToServer(image);
                    }
                    break;
                case SERVER_CONNECTION:
                    serverConnection = new ServerConnection(intent.getBundleExtra("MainData"), serverListener);
                    if (serialInOutManager != null) {
                        serialInOutManager.setServerConnection(serverConnection);
                    }
                    break;
                default:
                    //Here could go some default stuff.
                    break;
            }
        }
    };

    //AntEyeReceiver receiver = new AntEyeReceiver();


    /**
     *This method gets called, when the AntEye Application binds to this Service. it initiates
     * broadcasts and Receiver, starts the background threads to establish a communication with the
     * Arduino Board and sets the intent filter for BroadcastReceiver intents. Meaning it declares
     * the intent actions accepted by the Receiver SuperClass.
     * @param intent Intent to start the background Service
     * @return Returns an IBinder, which is not needed in this Application -> returns null
     */
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.e(TAG, "onBind called");

        broadcast = new Broadcast(this, getResources());
        receive = new Receive(receiveListener);
        IntentFilter intentFilter = receive.getIntentFilter();
        registerReceiver(receive, intentFilter);

        mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        Log.e(TAG, "starting loadDrivers Runnable");
        loadConnectionHandler.post(loadDriversRunnable);





        return null;
    }

    Handler deviceHandler = new Handler();
    private Runnable checkForDevices = new Runnable() {
        @Override
        public void run() {
            //Check if there is still a Device connected to the phone.
            availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(mUsbManager);
            Log.e(TAG,"checkForDevicesRunnable: Drivers: " + availableDrivers);
            //If not we restart the connection Runnable.
            if (availableDrivers.isEmpty() && !stopRunnable) {
                //Lets the AntEye Application know, that the device was disconnected.
                broadcast.broadcastSerialConnectionEstablished(false);
                //Re-posts the drivers loading Runnable to retry loading the drivers.
                Log.e(TAG,"Retrying to connect to device");
                loadConnectionHandler.post(loadDriversRunnable);
            }
            //If there still is a device this Runnable is re-posted after 1sec.
            else {
                deviceHandler.postDelayed(checkForDevices, 1000);
            }
        }

    };



    /**
     * This Method is called, when the AntEye Application gets Destroyed. Therefore it is to assume,
     * that neither Serial nor Server connection is needed, so it closes all Connection to prevent
     * Memory Leaks. It also unregisters Receivers and Intent Filters, also to prevent memory leaks
     * if any other Service did not stop and continues sending information.
     *
     * @param intent Same intent as at the onBind Method
     * @return Returns superClass return method.
     */
    @Override
    public boolean onUnbind(Intent intent) {
        Log.e(TAG, "onUnbind called");
        Log.e(TAG,"sPort in Unbind: " + sPort);
        if (serverConnection != null) {
            serverConnection.closeSocket();
        }
        stopIoManager();
        if (sPort != null) {
            try {
                sPort.close();
                Log.e(TAG, "Port Closed");
            } catch (IOException e) {
                Log.e(TAG, "Port closing failed");
                // Ignore.
            }
            sPort = null;
        }
        if (receive != null) {
            unregisterReceiver(receive);
        }

        deviceHandler.removeCallbacks(checkForDevices);
        loadConnectionHandler.removeCallbacks(loadDriversRunnable);
        loadConnectionHandler.removeCallbacks(openSerialConnectionRunnable);
        closeSerialConnection();

        return super.onUnbind(intent);
    }


    /**
     * Starts a background thread, handled by the loadConnectionHandler to post the runnable every
     * 500ms if there are no Drivers found (Meaning there is no Arduino Board connected or it is an
     * unknown device)
     */
    final Runnable loadDriversRunnable = new Runnable() {
        @Override
        public void run() {
            //loads the USB drivers
            availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(mUsbManager);
            //checks if we could find any drivers and checks if the user stopped the connection.
            if (availableDrivers.isEmpty() && !stopRunnable) {
                //Re-posts this Runnable to retry loading the drivers.
                loadConnectionHandler.postDelayed(loadDriversRunnable,500);
            }
            //If the drivers were found and the user did not stop the connection manually:
            else if (!stopRunnable){
                //We always take the first driver we found since most phones only have one USB port
                //the Arduino Board will most likely be the first entry.
                driver = availableDrivers.get(0);
                //Then we post the connection Runnable to establish a connection to the Board.
                loadConnectionHandler.post(openSerialConnectionRunnable);
            }
        }
    };


    /**
     * Once the USBDrivers are loaded this Runnable tries to establish a connection using the
     * com.hoho.android.usbserial library. The Handler posts the Runnable every 500ms if it can't
     * establish a connection.
     */
    public Runnable openSerialConnectionRunnable = new Runnable() {
        @Override
        public void run() {
            //Get the Connection (mostly took over from the com.hoho.arduino.usbserial example used
            //in the SerialConsoleActivity and the DeviceListActivity)
            connection = mUsbManager.openDevice(driver.getDevice());
            if (connection == null && !stopRunnable) {
                //If we could not open the connection we retry in 500ms by re-posting the runnable.
                loadConnectionHandler.postDelayed(openSerialConnectionRunnable,500);
            } else if (!stopRunnable){
                //If we have a connection and the user did not close the connection we now establish
                //a proper serial communication.
                boolean successfullyConnected = true; //Used in the end of the Runnable
                //Get the connection Port
                sPort = driver.getPorts().get(0);
                Log.e(TAG, "Resumed, port=" + sPort);
                if (sPort == null) {
                    Log.e(TAG, "No serial device.");
                } else {
                    //Connect using the Port
                    usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
                    connection = usbManager.openDevice(sPort.getDriver().getDevice());
                    if (connection == null) {
                        Log.e(TAG, "Opening device failed");
                        successfullyConnected = false;
                    }
                    try {
                        //Open the communication
                        sPort.open(connection);
                        //Set all parameters for proper communication with the Arduino Board.
                        sPort.setParameters(115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
                    } catch (IOException e) {
                        Log.e(TAG, "Error setting up device: " + e.getMessage(), e);
                        Log.e(TAG, "Error opening device: " + e.getMessage());
                        successfullyConnected = false;
                        try {
                            //If we cannot open the port we try to close it again and restart the
                            //connection process
                            sPort.close();
                        } catch (IOException e2) {
                            // Ignore.
                        }
                        sPort = null;
                    }
                    try {
                        Log.e(TAG, "Serial device: " + sPort.getClass().getSimpleName());
                    } catch(NullPointerException e){
                        e.printStackTrace();
                    }
                }
                //If everything went well we let the MainThread know, that we have established a
                //connection by calling the onDeviceChange Method.
                if (successfullyConnected){
                    onDeviceStateChange();
                }
                //If something went wrong, we restart the Runnable after 500ms.
                else {
                    loadConnectionHandler.postDelayed(openSerialConnectionRunnable, 500);
                }
            }
        }
    };

    /**
     * Closes the Serial Connection, called in the onUnbind method
     */
    private void closeSerialConnection(){
        if (connection != null) {
            connection.close();
        }
    }

    /**
     * stops the SerialIOManager of the com.hoho.android.usbserial library. Called in onUnbind method
     * and onDevice changed Method. It also stops all Runnable by setting the stopRunnable boolean
     * true.
     */
    private void stopIoManager() {
        if (serialInOutManager != null) {
            Log.e(TAG, "Stopping in manager ..");
            serialInOutManager.stop();
            serialInOutManager = null;
        } else {
            stopRunnable = true;
        }
    }

    /**
     * starts the SerialInOutManager, a child class of the com.hoho.android.usbserial library and
     * sets the serverConnection Object of the SerialInOutManager to send data from the server
     * directly to the Arduino Board.
     */
    private void startIoManager() {
        if (sPort != null) {
            Log.e(TAG, "Starting io manager ..");
            serialInOutManager = new SerialInOutManager(sPort, mListener, this, getResources());
            inExecutor.submit(serialInOutManager);
            if (serverConnection != null){
                serialInOutManager.setServerConnection(serverConnection);
            }
        }
    }

    /**
     * Called when the Arduino board gets connected to the phone.
     */
    private void onDeviceStateChange() {
        broadcast = new Broadcast(this,getResources());
        broadcast.broadcastSerialConnectionEstablished(true);
        deviceHandler.postDelayed(checkForDevices, 1000);
        stopIoManager();
        startIoManager();
    }
}