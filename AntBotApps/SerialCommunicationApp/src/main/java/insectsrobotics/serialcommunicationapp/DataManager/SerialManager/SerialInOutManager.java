package insectsrobotics.serialcommunicationapp.DataManager.SerialManager;


import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.util.SerialInputOutputManager;

import java.util.Arrays;

import insectsrobotics.serialcommunicationapp.DataManager.ServerManager.ServerConnection;
import insectsrobotics.serialcommunicationapp.Receiver_and_Broadcaster.Broadcast;

/**
 * This class is responsible for all communication between the Arduino Board and the Phone. It is a
 * child class of the Serial Library's SerialManager and implements a Listener Interface. This class
 * also parses all incoming and outgoing messages, repairs broken and fused data strings and
 * broadcasts the received Strings to the PathIntegrator app and the Server.
 */
public class SerialInOutManager extends SerialInputOutputManager implements SerialInputOutputManager.SerialInListener{

    String TAG = "SerialInputManager";

    String oldData;
    Broadcast pathIntegratorBroadcast;
    ServerConnection serverConnection;

    /**
     * Public Constructor, child class of the com.hoho.android.usbserial library's SerialInputOutputManager
     * @param driver UsbSerialPort passed to the superclass' constructor.
     * @param listener SerialInListener passed to the superclass' constructor
     * @param context The BackgroundService Context to enable Broadcasting.
     * @param res Resources to get the String Resources for Broadcasting from the "broadcast.xml" file
     */
    public SerialInOutManager(UsbSerialPort driver, SerialInListener listener, Context context, Resources res) {
        super(driver, listener);
        pathIntegratorBroadcast = new Broadcast(context, res);
    }

    /**
     * Sets the ServerConnection to enable sending Messages from the Arduino Board to the Server
     * @param serverConnection Object of the ServerConnection class.
     */
    public void setServerConnection(ServerConnection serverConnection) {
        this.serverConnection = serverConnection;
    }

    /**
     * Method from the implemented listener Interface. Handles new Data on the Serial Connection.
     * Strips the Message from all LineBreaks and transforms it into a String.
     * @param data Incoming Message from the Arduino Board
     */
    @Override
    public void onNewData(byte[] data) {
        Log.e("SerialInputManager", "onNewData called");
        String dataString = new String(data);
        Log.e("SerialInputManager", dataString);
        String stripped = dataString.replaceAll("\n", "").replaceAll("\r", "");
        parseNewIncomingData(stripped);
    }

    /**
     * Method from the implemented Listener Interface. Handles Upcoming Exceptions.
     * @param e Serial Connection Exception, like IOException or NullPointerException
     */
    @Override
    public void onRunError(Exception e) {
        Log.d("SerialInputManager", "Runner stopped.");
    }


    /**
     * Sends Data to the Server and the PathIntegrator.
     * @param data Incoming Message after parsing from the Arduino Board.
     */
    public void broadcastData(String data) {
        serverConnection.sendToServer(data);
        pathIntegratorBroadcast.broadcastWheelEncoderInformation(data);
        oldData = "";
    }


    /**
     * Called on every Message from the Arduino Board to ensure, that the Message is complete and not
     * fused with the next Message.
     *
     * Every Message has the same look:
     * It starts with either a "m", "t", "j" or "x".
     * "m" stands for Move
     * "t" stands for Turn
     * "j" stands for "Job completed, request new Job"
     * "x" stands for HeartBeat to ensure, that the connection is still available.
     * It then is followed by a number
     * And always ends with an "n".
     * For example: "m 100 n" means, that the Rover moved 100 meter.
     *              "j n" means, that the Rover completed the Task and requests a new Job.
     *
     * @param newData The stripped String from the onNewMessage Method of the Listener Interface.
     */
    private void parseNewIncomingData(String newData) {
        Log.e(TAG, "parseNewDataCalled");
        //Since every message ends with a "n" we know, that if a letter follows the "n", two
        //messages got fused so we have to split them up (Happens in the attemptFuseRepair Method)
        if (newData.contains("nt") || newData.contains("nm") || newData.contains("nx") || newData.contains("nj")) {
            // First check we can do is for fused messages
            attemptFuseRepair(newData);
        }
        //If there is no fused Message we have to check, if the "n" exist at all. If not the Message
        //was split so we pass it on to the attemptSplitRepair method.
        else if (newData.contains("n")) {
            // If the message contains a "n" we have to check, if it also contains one of the other
            // letters. If it does, we have a complete message. If not the message is split so we
            // pass it on to the attemptSplitRepair method.
            if (newData.contains("t") || newData.contains("m") || newData.contains("x") || newData.contains("j")) {
                //If the Message is complete we send it to the server and the PathIntegrator App.
                broadcastData(newData);
            } else {
                attemptSplitRepair(newData);
            }
        } else {
            attemptSplitRepair(newData);
        }

    }

    /**
     * Called if two messages are fused together. Splits them up and passes them on to the according
     * methods.
     * @param newData Fused Messages from the Arduino Board.
     */
    private void attemptFuseRepair(String newData) {
        Log.e(TAG,"attemptFuseRepairCalled");
        // Break the Message apart after the "n"
        int firstN = newData.indexOf('n');
        String firstHalf = newData.substring(0, firstN + 1);
        String secondHalf = newData.substring(firstN + 1, newData.length());

        //Log.i(TAG, "AntBotController attemptFuseRepair");

        // Check if the first Message is complete, if not plug it into a split repair
        if (firstHalf.contains("t") || firstHalf.contains("m") || firstHalf.contains("x") || firstHalf.contains("j")) {
            // Complete
            broadcastData(firstHalf);
        } else {
            // Incomplete, as this is the end of a message trying doing a split repair
            attemptSplitRepair(firstHalf);
        }

        // Deal with the second half. Given we know that the first half is the end of a string,
        // we can check for fuses again (might be 3 or more messages together) and recursively
        // apply fuse repairs until they are all broken up.
        // We won't need to worry about prematurely overwriting the oldData buffer as firstHalf
        // is the end of a message and if secondHalf contains any fused opening markers then the
        // message will be fully formed
        if (secondHalf.contains("nt") || secondHalf.contains("nm") || secondHalf.contains("nx") || secondHalf.contains("nj")) {
            attemptFuseRepair(secondHalf);
        } else if (secondHalf.contains(" n")) {
            broadcastData(secondHalf);
        } else {
            // Incomplete Message, this is the beginning of a message so just store it in oldData
            oldData = secondHalf;
        }
    }


    private void attemptSplitRepair(String newData) {
        Log.e(TAG,"attemptSplitRepairCalled");
        //Log.i(TAG, "AntBotController attemptSplitRepair");

        // Append to what we just received. oldData is set to an empty String whenever we Broadcast
        // Data.
        oldData = oldData + newData;

        // Check if we now have something complete. If not we don't end it here (the next message
        // will likely be the next segment), we will have the last section waiting in oldData for it.
        if (oldData.contains("n")) {
            if (oldData.contains("t") || oldData.contains("m") || oldData.contains("x") || oldData.contains("j")) {
                broadcastData(oldData);
            }
        }
    }


    /**
     * Sends a message from the Server to the Arduino Board.
     * (See i.e. stopRouting). A Message from the Server always looks similar.
     * It either begins with:
     * "move":   Move Straight forwards or backwards
     * "turn":   Turn on the spot
     * "head":   exact heading where to go
     * "halt":   stop everything and wait for next command
     * "wheels": Exact PWM Signal for each wheel, used only to calibrate the wheel encoders.
     * It is then followed by how much to do the command.
     * I.e.: "move 100" means to move 100 meters forward.
     *
     * The outgoing Message to the Arduino always looks similar as well. All move and turn commands
     * follow the same pattern:
     * "t x m y n" with x and y being the amount on how much to turn/ move.
     * i.e.: "t 0 m 100 n" means that the Rover is supposed to drive 100 meter straight forward.
     *
     * The halt command is just a simple "h"
     *
     * The wheels command sets the left and right wheels direction and speed. A Message looks like
     * this:
     *
     * "l left_direction, left_speed r right_direction, right_speed n"
     * i.e. "l 1, 128 r 0, 128 n"
     *
     * @param message Message coming from the Server passed through the ServerConnectionListener in
     *                the BackgroundService and then on to this class. Only includes Messages with
     *                the Arduino board as Destination. Everything else is sorted out in the
     *                BackgroundService class.
     */
    public void parseOutMessage(String message)
    {
        //Splits the message into Tokens containing the commands.
        Log.e(TAG,"parseOutMessage: " + message);
        String[] tokens = message.split("\\s+");
        Log.d(TAG, "Tokens: " + Arrays.toString(tokens));

        try
        {
            //Depending on the command this passes the message on to the according methods.
            switch(tokens[0])
            {
                case "move":
                    Log.i(TAG, "move");
                    move( Double.valueOf(tokens[1]) );
                    break;

                case "turn":
                    Log.i(TAG, "turn");
                    turn(Double.valueOf(tokens[1]));
                    break;

                case "head":
                    Log.i(TAG, "heading (heading)");
                    turnMove(Double.valueOf(tokens[1]), Double.valueOf(tokens[2]));
                    break;

                case "halt":
                    Log.i(TAG, "halt");
                    halt();
                    break;

                case "wheels":
                    Log.i(TAG, "wheels");
                    setWheels(Integer.valueOf(tokens[1]), Integer.valueOf(tokens[2]),Integer.valueOf(tokens[3]), Integer.valueOf(tokens[4]));
                    break;

            }
        }
        catch(ArrayIndexOutOfBoundsException e)
        {
            e.getStackTrace();
            Log.e(TAG, "Malformed Message - too few tokens: " + Arrays.toString(tokens));
        }
        catch(NumberFormatException e)
        {
            e.getStackTrace();
            Log.e(TAG, "Malformed Message - argument not number");
        }
    }


    /**
     * Order AntBot to move a certain distance (in metres)
     */
    public void move(double distance)
    {
            String str = "t 0 m " + distance + " n";
            sendData(str);

            Log.i(TAG, "AntBotController MOVE '" + str + "'");

    }

    /**
     * Order AntBot to turn a certain angle (in degrees)
     */
    public void turn(double angle)
    {

            String str = "t " + angle + " m 0 n";
            sendData(str);

            Log.i(TAG, "AntBotController TURN '" + str + "'");

    }

    /**
     * Order AntBot to face and then pursue a certain heading (degrees/metres)
     */
    public void turnMove(double angle, double distance)
    {
            String str = "t " + angle + " m " + distance + " n";
            sendData(str);

            Log.i(TAG, "AntBotController TURN+MOVE '" + str + "'");

    }

    /**
     * Stop all movement AntBot is making
     */
    public void halt()
    {
            Log.i(TAG, "AntBotController HALT");
            String str = "h";
            //homing = false;
            sendData(str);

    }

    /**
     * Set the PWM and DIR values of AntBot manually - should be used for testing/calibration only.
     */
    public void setWheels(int left_speed, int left_direction, int right_speed, int right_direction)
    {
            Log.i(TAG, "AntBotController setWheelConfig");
            String str = "l " + left_speed + ", " + left_direction + " r " + right_speed + ", " + right_direction + " n";
            sendData(str);

    }

    public void sendData(String msg){
        Log.e(TAG,"Message:_" + msg + "_End");
        writeAsync(msg.getBytes());
    }
}
