package insectrobotics.broadcastlibrary;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

/**
 * This is the Superclass for all Broadcaster. Every Broadcaster should include this for standardization
 * of the broadcast information. This Class exists in every application
 */
public class Broadcaster implements BroadcastInterface{

    Context context;
    Intent broadcastIntent;

    String onNewDataKey = "MainData";

    public Broadcaster(Context context){
        this.context = context;
    }

    /**
     * InitiateBroadcast is used to give every broadcast an own action for better distinguishing of
     * sent data.
     * @param intentString the intent action goes in here
     */
    @Override
    public void initiateBroadcast(String intentString){
        broadcastIntent = new Intent(intentString);

    }

    /**
     *
     * @param data the main data to send between apps. If there is more then just one data set
     *             please use the addData function. This data is specified in the Receiver with the
     *             "mainData" keyword
     */
    @Override
    public void onNewDataToBroadcast(String data) {
        broadcastIntent.putExtra(onNewDataKey, data);
    }

    @Override
    public void onNewDataToBroadcast(int data) {
        broadcastIntent.putExtra(onNewDataKey, data);
    }

    @Override
    public void onNewDataToBroadcast(boolean data) {
        broadcastIntent.putExtra(onNewDataKey, data);
    }

    @Override
    public void onNewDataToBroadcast(float data) {
        broadcastIntent.putExtra(onNewDataKey, data);
    }

    @Override
    public void onNewDataToBroadcast(double data) {
        broadcastIntent.putExtra(onNewDataKey, data);
    }

    @Override
    public void onNewDataToBroadcast(long data) {
        broadcastIntent.putExtra(onNewDataKey, data);
    }

    @Override
    public void onNewDataToBroadcast(byte data) {
        broadcastIntent.putExtra(onNewDataKey, data);
    }

    @Override
    public void onNewDataToBroadcast(short data) {
        broadcastIntent.putExtra(onNewDataKey, data);
    }

    @Override
    public void onNewDataToBroadcast(char data) {
        broadcastIntent.putExtra(onNewDataKey, data);
    }

    @Override
    public void onNewDataToBroadcast(String[] data) {
        broadcastIntent.putExtra(onNewDataKey, data);
    }

    @Override
    public void onNewDataToBroadcast(int[] data) {
        broadcastIntent.putExtra(onNewDataKey, data);
    }

    @Override
    public void onNewDataToBroadcast(boolean[] data) {
        broadcastIntent.putExtra(onNewDataKey, data);
    }

    @Override
    public void onNewDataToBroadcast(float[] data) {
        broadcastIntent.putExtra(onNewDataKey, data);
    }

    @Override
    public void onNewDataToBroadcast(double[] data) {
        broadcastIntent.putExtra(onNewDataKey, data);
    }

    @Override
    public void onNewDataToBroadcast(long[] data) {
        broadcastIntent.putExtra(onNewDataKey, data);
    }

    @Override
    public void onNewDataToBroadcast(byte[] data) {
        broadcastIntent.putExtra(onNewDataKey, data);
    }

    @Override
    public void onNewDataToBroadcast(short[] data) {
        broadcastIntent.putExtra(onNewDataKey, data);
    }

    @Override
    public void onNewDataToBroadcast(char[] data) {
        broadcastIntent.putExtra(onNewDataKey, data);
    }

    @Override
    public void onNewDataToBroadcast(Bundle data) {
        broadcastIntent.putExtra(onNewDataKey, data);
    }

    /**
     *
     * @param data put other data to the intent with a different keyword than "mainData"
     */
    @Override
    public void addData(String key, String data) {
        broadcastIntent.putExtra(key, data);
    }

    @Override
    public void addData(String key, byte[] data) {
        broadcastIntent.putExtra(key, data);
    }

    @Override
    public void addData(String key, int data) {
        broadcastIntent.putExtra(key, data);
    }

    @Override
    public void addData(String key, double data) {
        broadcastIntent.putExtra(key, data);
    }

    /**
     * Sends out the intent to all apps with the specified intent filter.
     */
    public void executeBroadcast() {
        context.sendBroadcast(broadcastIntent);
    }

}
