import java.io.*;
import java.util.Arrays;
import java.util.Scanner;

public class main {

    private static proto proto = new proto();
    private static SerialConnHandler connHandler;

    // replace with your actual file name
    private static final String FILE_NAME = "blink.bin";


    public static void main(String[] args) throws Exception {
        connHandler = new SerialConnHandler(promptForPort());
        connHandler.connect();
        int id = 2;
        int[] tx = proto.newFrameBuf(proto.getCmdGetUDI(),id);
        byte[] txarr = intArrayToByteArray(tx);
        connHandler.getConn().writeBytes(txarr,txarr.length);
        proto.readFrame(proto.getRspGetUDI(),2, connHandler.getConn());
        //loadAppFromFile("blink.bin");
    }

    private static String promptForPort() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Please enter the COM port (e.g. COM7): ");
        return scanner.nextLine();
    }
    public static void loadAppFromFile(String fileName) throws Exception {
        byte[] content = readFile(fileName);
        LoadApp(content);
    }

    private static void LoadApp(byte[] bin) throws Exception {
        int binLen = bin.length;
        if (binLen > 100 * 1024) {
            throw new Exception("File too big");
        }

        System.out.println("app size: " + binLen + ", 0x" + Integer.toHexString(binLen) + ", 0b" + Integer.toBinaryString(binLen));

        byte[] uss = new byte[0];
        loadApp(binLen, uss);

        int offset = 0;
        int[] deviceDigest = new int[32];

        for(int nsent; offset < binLen; offset+= nsent){
            Tuple tup;
            try{
                if(binLen-offset <= proto.getCmdLoadAppData().getCmdLen().getBytelen()-1){
                    tup = loadAppData( (Arrays.copyOfRange(bin, offset, bin.length)),true);
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
        if(offset>binLen){
            throw new Exception("Transmitted more than expected");
        }
        /*
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] digestFromHost = digest.digest(bin);

        System.out.println("Digest from host: " + bytesToHex(digestFromHost));
        //System.out.println("Digest from device: " + bytesToHex(Arrays.stream(deviceDigest).map()));

        if (!Arrays.equals(deviceDigest, digestFromHost)) {
            throw new Exception("Different digests");
        }
        System.out.println("Same digests!");*/
    }

    private static void loadApp(int size, byte[] secretPhrase) throws Exception {

        int id = 2;
        int[] tx;
        try{
            tx = proto.newFrameBuf(proto.getCmdLoadApp(),id);
            //tx = proto.newFrameBuf(proto.getCmdGetNameVersion(),id);
        }catch(Exception e){
            throw new Exception(e);
        }
        tx[2] = size;
        tx[3] = size >> 8;
        tx[4] = size >> 16;
        tx[5] = size >> 24;
        tx[6] = 0;
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


        // Prepare your command to set the size and USS of the app, then write it to conn.getOutputStream()
        // ...
    }

    private static Tuple loadAppData(byte[] contentByte, boolean last) throws Exception {
        int id = 2;
        int[] tx = proto.newFrameBuf(proto.getCmdLoadAppData(), id);
        int[] content = byteArrayToIntArray(contentByte);

        int[] payload = new int[proto.getCmdLoadAppData().getCmdLen().getBytelen()-2];
        int copied = Math.min(content.length, payload.length);
        System.arraycopy(content, 0, payload, 0, copied);

        if (copied < payload.length) {
            int[] padding = new int[payload.length - copied];
            System.arraycopy(padding, 0, payload, copied, padding.length-1);
        }
        System.out.println(payload.length);
        System.arraycopy(payload, 0, tx, 2, payload.length);

        try{
            proto.dump("LoadAppData tx", tx);
        }catch (Exception e){
            throw new Exception(e);
        }
        int[] rx;
        FwCmd cmd;
        if(last){
            cmd = proto.getRspLoadAppDataReady();
        }else{
            cmd = proto.getRspLoadAppData();
        }
        try {
            Tuple tup = proto.readFrame(cmd, id, connHandler.getConn());
            rx = tup.getIntArray();
        }catch (Exception e){
            throw new Exception(e);
        }
        if(last){
            int[] digest = new int[32];
            System.arraycopy(rx, 3, digest,0 ,32);
            return new Tuple(digest,copied);
        }
        return new Tuple(new int[32], copied);
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
            arr[i] = (int) byteArray[i];
        }
        return arr;
    }
}
