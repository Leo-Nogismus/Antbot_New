package insectsrobotics.serialcommunicationapp.DataManager.ServerManager;


import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;

/**
 * This class is responsible for all communication with the Server.
 */
public class ServerConnection {



    String TAG = "ServerConnection";

    //The Bundle which came from the AntEye Application. Has Extras: ServerAddressString and port.
    Bundle serverBundle;
    //Server variables where to connect to. Sent by the AntEye Application via the serverBundle.
    String serverAddressString;
    int port;

    //Needed objects to connect to the Server.
    Socket socket;
    PrintWriter socketOutput;
    BufferedReader socketInput;
    NetworkConnectThread networkConnectThread;
    //Listener to pass new Data on to the Background Service.
    ServerConnectionListener serverConnectionListener;

    //Variable to stop the server connection by setting it true.
    boolean stopServerConnection = false;


    /**
     *
     * @param mBundle Bundle with the serverAddressString and port as Extras.
     * @param listener Listener to deal with new incoming messages from the Server.
     */
    public ServerConnection(Bundle mBundle, ServerConnectionListener listener) {
        Log.e("ServerConnection", "Server Connection starting");
        serverConnectionListener = listener;
        serverBundle = mBundle;
        //Immediately calls a Background Thread to establish a connection to the Server.
        startNetworkThread(mBundle);
    }

    /**
     * Listener Interface to pass incoming data into different Classes and mainly in the Background
     * Service
     */
    public interface ServerConnectionListener{
        void onNewMessage(String msg);
        void onRunError(Exception e);
        void onConnectionChanged(boolean connectionState);
    }

    /**
     * Initiates a new BackgroundThread (AsyncTask) to establish a connection to the in the Bundle
     * specified ServerAddress and Port. Called in the Constructor of this class and in the
     * onPostExecute method of the Async Task.
     * @param mBundle Bundle with Extras: serverAddressString and port
     */
    private void startNetworkThread(Bundle mBundle){
        networkConnectThread = new NetworkConnectThread();
        //.execute is used for AsyncTasks to start the Background Thread and calls the onPreExecute/
        // doInBackground method.
        networkConnectThread.execute(mBundle);
    }


    /**
     * Inner class extending an AsyncTask. The doInBackground method is responsible for establishing
     * and holding the server connection.
     */
    class NetworkConnectThread extends AsyncTask<Bundle, String, Boolean> {

        @Override
        protected Boolean doInBackground(Bundle... params) {

            // params[0] describes the inserted Bundle. getString gets the serverAddress from the
            // Bundle. getInt gets the port from the Bundle.
            serverAddressString = params[0].getString("ServerAddress");
            port = params[0].getInt("Port");
            //Just to show a Message in the Debugger.
            publishProgress("ServerAddress: " + serverAddressString + ":" + port);

            //Starts the connection. Written in own Method to keep the Thread looking for a possible
            //connection until it is stopped by the User.
            establishConnectionLoop();

            //Again just for the Debugger.
            publishProgress("Connection successful, Socket: " + socket);
            try {
                String inputLine;
                //While the socket is connected this Thread waits for a new Message to come in.
                while (socket.isConnected())  {
                    //Debugger
                    publishProgress("Connected");
                    //Reads Line when it arrives.
                    inputLine = socketInput.readLine();
                    if (inputLine == null){
                        break;
                    }
                    //Sometimes the Server sends empty messages, so we want to get rid of those.
                    //If the message is not empty, then we publish the Line for the onProgressUpdate
                    //method to see and pass to the according Class.
                    if (!inputLine.replace(" ","").isEmpty()) {
                        publishProgress("NewLine" + inputLine);
                    }
                }
                //If the connection gets closed after the connection was already established this
                //restarts the Loop to connect to the Server.
                if (!socket.isConnected()){
                    establishConnectionLoop();
                }
            } catch (IOException e) {
                publishProgress("Run Error");
                serverConnectionListener.onRunError(e);
                e.printStackTrace();

            }
            //Gets called once all while loops are finished, so if there is no Server to connect to.
            return null;
        }

        /**
         * Tries to establish a connection to the specified Server. Also sends out a Toast every 5
         * failed trials.
         */
        private void establishConnectionLoop(){
            int tryToConnectCounter = 0;
            boolean established = false;
            //Retries to connect until the establishConnection() method returns true or the User
            //Ends the connection.
            while (!established && !stopServerConnection) {
                established =  establishConnection();
                tryToConnectCounter++;
                //Sends out a Toast. This has to run on the UIThread, so we publish it as a progress.
                if (tryToConnectCounter % 5 == 0){
                    publishProgress("ToastCant Connect to Server, sure Server Connection Thread is running?");
                }
            }
        }

        /**
         * Connects the Phone with the Server.
         *
         * @return True, if the connection was successfully established, Flase if not.
         */
        private boolean establishConnection() {
            publishProgress("Establishing Connection");
            //This Try Block is responsible for the actual connection. If it fails it automatically
            //returns false, so the establishConnectionLoop method calls the establishConnection
            //method again.
            try {
                socket = new Socket();
                socket.connect(new InetSocketAddress(serverAddressString, port), 2000);
                //Just for the Debugger.
                publishProgress("Socket: " + socket);
                socketOutput = new PrintWriter(socket.getOutputStream(), true);
                //Just for the Debugger.
                publishProgress("socketOutput");
                socketInput = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                //Just for the Debugger.
                publishProgress("socketInput");
            } catch (UnknownHostException e) {
                e.printStackTrace();
                publishProgress("Unknown Host Exception");
                return false;
            } catch (IOException e) {
                e.printStackTrace();
                publishProgress("IOException");
                return false;
            }
            try {
                //Rechecks, if everything is fine and if so return true, meaning, that the
                //establishConnectionLoop() method is finished and the Thread goes into a waiting
                //state to receive messages from the Server.
                if (socket != null && socketOutput != null && socketInput != null) {
                    publishProgress("Socket connected: " + socket.isConnected());
                    return socket.isConnected();
                } else {
                    publishProgress("NullPointer: Socket: " + socket + " SocketIn: " + socketInput + " SocketOut: " + socketOutput);
                    return false;
                }
            } catch (NullPointerException e) {
                publishProgress("NullPointerException");
                return false;
            }
        }

        //Manages all Debugger Information, Toasts and inputLines and sends them to the according
        //destination.
        @Override
        protected void onProgressUpdate(String... values) {
            Log.e("onProgressUpdate", values[0]);
            if (values[0].contains("NewLine") || values[0].contains("Toast")){
                String message = values[0].replace("NewLine", "");
                serverConnectionListener.onNewMessage(message);
            } else if (values[0].equals("Connected")){
                serverConnectionListener.onConnectionChanged(true);
            }
        }

        //If the Thread ever reaches its end, we restart the Thread immediately so we can reconnect
        //to a lost connection.
        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if (!stopServerConnection) {
                startNetworkThread(serverBundle);
                serverConnectionListener.onConnectionChanged(false);
            }
        }
    }

    /**
     * Sends a message to the Server using the socketOutput of the ServerConnection
     * @param message Message to send to the Server.
     */
    public void sendToServer(String message){
        if (socketOutput!=null) {
            //gives the Message to the PrintWriter
            socketOutput.write(message + "\n");
            //Flushes the message to the Server (not necessary, but safer)
            socketOutput.flush();
        }
    }

    /**
     * Sends an Image to the Server. The Image itself comes from the AntEye application. This has
     * to use this Application as a middle man since it is not possible to connect two Applications
     * to the same socket.
     * @param image byte array from the AntEye Application.
     */
    public void sendImageToServer(byte[] image){
        //Makes a String out of the Array
        String imageString = Arrays.toString(image);
        if (socketOutput != null){
            //gives the Image to the PrintWriter
            socketOutput.write("Image " + imageString + "\n");
            //Flushes the message to the Server (not necessary, but safer)
            socketOutput.flush();
        }
    }

    /**
     * This is called in the onUnbind Method of the Background service. It closes the connection
     * to the Server and sends out a "Close" Message, so the Server actually knows, that the App
     * was terminated. Also sets the stopServerConnection to true to stop the Background Threads of
     * the Async Task.
     */
    public void closeSocket(){
        Log.e(TAG, "Closing Socket");
        try {
            socketOutput.write("Close");
            socketOutput.flush();
        } catch (NullPointerException e){
            e.printStackTrace();
        }

        try {
            if (socket != null) {
                socket.close();
            }
            if (socketInput != null) {
                socketInput.close();
            }
            if (socketOutput != null) {
                socketOutput.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        stopServerConnection = true;
    }

}
