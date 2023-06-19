package com.knek;

import java.io.*;
import java.nio.*;
import java.util.*;

public class TkeyClient {
    private static final proto proto = new proto();
    private static SerialConnHandler connHandler;
    private static final int ID = 2;
    public static void main(String[] args) throws Exception {
        connect();
        getNameVersion();
        loadAppFromFile("app.bin");
    }
    public static void loadAppFromFile(String fileName) throws Exception {
        byte[] content = readFile(fileName);
        LoadApp(content);
    }

    public static boolean getHasCon(){
        return connHandler.getHasCon();
    }


    private static void LoadApp(byte[] bin) throws Exception {
        LoadApp(bin,new byte[0]);
    }

    // Perhaps should be an accessible variable instead.
    private static void LoadApp(byte[] bin, byte[] uss) throws Exception {
        int binLen = bin.length;
        if (binLen > 102400) throw new Exception("File too big");

        //System.out.println("app size: " + binLen + ", 0x" + Integer.toHexString(binLen) + ", 0b" + Integer.toBinaryString(binLen));

        loadApp(binLen, uss);

        int offset = 0;
        int[] deviceDigest = new int[32];

        for(int nsent; offset < binLen; offset+= nsent){
            Tuple tup;
            try{
                if(binLen-offset <= proto.getCmdLoadAppData().getCmdLen().getBytelen()-1){
                    tup = loadAppData((Arrays.copyOfRange(bin, offset, bin.length)),true);
                    deviceDigest = tup.getIntArray();
                    nsent = tup.getIntValue();
                } else {
                    tup = loadAppData(Arrays.copyOfRange(bin, offset, bin.length),false);
                    nsent = tup.getIntValue();
                }
            }catch(Exception e){
                throw new Exception("loadAppData error: " + e);
            }
        }
        if(offset > binLen) throw new Exception("Transmitted more than expected");

        //byte[] digest = new byte[32];
        //Blake2s256 blake2s = new Blake2s256();
        //digest = blake2s.digest(bin);
        /*Blake2s blake2s = new Blake2s(digest.length);
        digest = blake2s.digest(bin);
        if (!Arrays.equals(intArrayToByteArray(deviceDigest), digest)) {
            System.out.println(Arrays.toString(deviceDigest));
            System.out.println(Arrays.toString(digest));
            throw new Exception("Different digests");
        }
        System.out.println("Same digests!");*/
    }

    private static void loadApp(int size, byte[] secretPhrase) throws Exception {
        int[] tx = proto.newFrameBuf(proto.getCmdLoadApp(),ID);
        tx[2] = size;
        tx[3] = size >> 8;
        tx[4] = size >> 16;
        tx[5] = size >> 24;
        tx[6] = 0;
        /*
        if(secretPhrase.length == 0){
        }else{
            byte[] uss = null;
        }*/
        // TODO: Implement blake2s and USS
        try{
            proto.dump("LoadApp tx", tx);
        }catch(Exception e){
            throw new Exception(e);
        }
        byte[] tx_arr = intArrayToByteArray(tx);
        try{
            connHandler.getConn().writeBytes(tx_arr,tx_arr.length);
        }catch(Exception e){
            throw new Exception(e);
        }
        /*byte[] rx = com.knek.proto.readFrame(com.knek.proto.getRspLoadApp(),ID,connHandler.getConn());
        if(rx[2] != 0x00){
            System.out.printf("dat new err");
        }*/ // Should be here, but causes issues.
        // Prep are your command to set the size and USS of the app, then write it to conn.getOutputStream()
    }

    private static Tuple loadAppData(byte[] contentByte, boolean last) throws Exception {
        int[] tx = proto.newFrameBuf(proto.getCmdLoadAppData(), ID);

        int[] payload = new int[proto.getCmdLoadAppData().getCmdLen().getBytelen()-1];
        int copied = Math.min(contentByte.length, payload.length);
        System.arraycopy(byteArrayToIntArray(contentByte), 0, payload, 0, copied);

        if (copied < payload.length) {
            int[] padding = new int[payload.length - copied];
            System.arraycopy(padding, 0, payload, copied, padding.length-1); //this line does nothing.
        }
        System.arraycopy(payload, 0, tx, 2, payload.length);
        try{
            proto.dump("LoadAppData tx", tx);
        }catch (Exception e){
            throw new Exception(e);
        }

        proto.write(intArrayToByteArray(tx), connHandler.getConn());

        FwCmd cmd;
        if(last){
            cmd = proto.getRspLoadAppDataReady();
        } else cmd = proto.getRspLoadAppData();

        byte[] rx;
        try {
            rx = proto.readFrame(cmd, ID, connHandler.getConn());
        }catch (Exception e){
            throw new Exception(e);
        }
        if(last){
            int[] digest = new int[32];
            System.arraycopy(byteArrayToIntArray(rx), 3, digest,0 ,32);
            return new Tuple(digest,copied);
        }
        return new Tuple(new int[32], copied);
    }

    public static String getNameVersion() throws Exception {
        byte[] data = getData(proto.getCmdGetNameVersion(), proto.getRspGetNameVersion());
        return unpackName(data);
    }

    private static String unpackName(byte[] raw) {
        String name0 = new String(raw, 1, 4);
        String name1 = new String(raw, 5, 4);
        long version = ByteBuffer.wrap(raw, 9, 4).order(ByteOrder.LITTLE_ENDIAN).getInt() & 0xffffffffL;
        String concated = name0 + name1 + " " + version;
        System.out.println("TKey Device name and version: " + concated);
        return concated;
    }

    public static void getUDI() throws Exception {
        byte[] data = getData(proto.getCmdGetUDI(), proto.getRspGetUDI());
        unpackUDI(data);
    }

    public static void unpackUDI(byte[] byteArray){
        System.out.println(Arrays.toString(byteArray));
        //TODO*
    }

    public static byte[] getData(FwCmd command, FwCmd response) throws Exception {
        byte[] tx_byte = intArrayToByteArray(proto.newFrameBuf(command, ID));
        connHandler.getConn().writeBytes(tx_byte,tx_byte.length);
        return proto.readFrame(response, 2, connHandler.getConn());
    }

    private static byte[] readFile(String fileName) throws IOException {
        return java.nio.file.Files.readAllBytes(new File(fileName).toPath());
    }

    public static byte[] intArrayToByteArray(int[] intArray) {
        byte[] arr = new byte[intArray.length];
        for(int i = 0; i<intArray.length; i++){
            arr[i] = (byte) intArray[i];
        }
        return arr;
    }

    public static int[] byteArrayToIntArray(byte[] byteArray) {
        int[] arr = new int[byteArray.length];
        for(int i = 0; i<byteArray.length; i++){
            arr[i] = byteArray[i];
        }
        return arr;
    }

    public static void connect() throws Exception {
        connHandler = new SerialConnHandler();
        connHandler.connect();
    }

    public static void reconnect() throws Exception {
        connHandler.reconnect();
    }

    public static void close(){
        connHandler.closePort();
    }

    public static void withSpeed(int speed){
        connHandler.setSpeed(speed);
    }

    protected static void setCOMPort(String port) {
        connHandler.setConn(port);
    }


}
