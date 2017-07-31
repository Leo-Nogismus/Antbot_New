package insectrobotics.broadcastlibrary;


import android.os.Bundle;

/**
 * Implemented by the superclass
 */
public interface BroadcastInterface {

    void initiateBroadcast(String broadcastDestination);
    void onNewDataToBroadcast(String data);
    void onNewDataToBroadcast(int data);
    void onNewDataToBroadcast(boolean data);
    void onNewDataToBroadcast(float data);
    void onNewDataToBroadcast(double data);
    void onNewDataToBroadcast(long data);
    void onNewDataToBroadcast(byte data);
    void onNewDataToBroadcast(short data);
    void onNewDataToBroadcast(char data);
    void onNewDataToBroadcast(String[] data);
    void onNewDataToBroadcast(int[] data);
    void onNewDataToBroadcast(boolean[] data);
    void onNewDataToBroadcast(float[] data);
    void onNewDataToBroadcast(double[] data);
    void onNewDataToBroadcast(long[] data);
    void onNewDataToBroadcast(byte[] data);
    void onNewDataToBroadcast(short[] data);
    void onNewDataToBroadcast(char[] data);
    void onNewDataToBroadcast(Bundle data);
    void addData(String key, String data);
    void addData(String key, byte[] data);
    void addData(String key, int data);
    void addData(String key, double data);
    void executeBroadcast();
}


