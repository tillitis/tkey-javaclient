import com.fazecast.jSerialComm.SerialPort;
public class proto {
    private final FwCmd cmdGetNameVersion = new FwCmd(0x01, "cmdGetNameVersion", CmdLen.CmdLen1);
    private final FwCmd rspGetNameVersion = new FwCmd(0x02, "rspGetNameVersion", CmdLen.CmdLen32);
    private final FwCmd cmdLoadApp = new FwCmd(0x03, "cmdLoadApp", CmdLen.CmdLen128);
    private final FwCmd rspLoadApp = new FwCmd(0x04, "rspLoadApp", CmdLen.CmdLen4);
    private final FwCmd cmdLoadAppData = new FwCmd(0x05, "cmdLoadAppData", CmdLen.CmdLen128);
    private final FwCmd rspLoadAppData = new FwCmd(0x06, "rspLoadAppData", CmdLen.CmdLen4);
    private final FwCmd rspLoadAppDataReady = new FwCmd(0x07, "rspLoadAppDataReady", CmdLen.CmdLen128);
    private final FwCmd cmdGetUDI = new FwCmd(0x08, "cmdGetUDI", CmdLen.CmdLen1);
    private final FwCmd rspGetUDI = new FwCmd(0x09, "rspGetUDI", CmdLen.CmdLen32);

    protected FramingHdr parseFrame(int b) throws Exception {
        boolean response = false;
        if ((b & 0b1000_0000) != 0) {
            throw new Exception("Reserved bit #7 is not zero");
        }
        response = (b & 0b0000_0100) != 0;
        int id = (b & 0b0110_0000) >> 5;
        int endpoint = (b & 0b0001_1000) >> 3;
        CmdLen cmdLen = CmdLen.values()[b & 0b0000_0011];

        FramingHdr framingHdr = new FramingHdr(id,endpoint,cmdLen);
        framingHdr.setResponseNotOk(response);
        return framingHdr;
    }

    protected int[] newFrameBuf(FwCmd cmd, int id) throws Exception{
        validate(id, 1, 3, "Frame ID needs to be between 1..3");
        validate(cmd.getEndpoint(), 0, 3, "Endpoint must be 0..3");
        validate(cmd.getCmdLen().getByteVal(), 0, 3, "cmdLen must be 0..3");

        CmdLen cmdlen = cmd.getCmdLen();
        int[] tx = new int[cmdlen.getBytelen()+1];
        tx[0] = ((id << 5) | (cmd.getEndpoint() << 3) | cmdlen.getByteVal());
        tx[1] = cmd.getCode();
        return tx;
    }

    /**
     * TODO method doesn't really do anything, seeing as dumped data isn't returned.
     */
    protected void dump(String s, int[] d) throws Exception {
        if(d == null || d.length == 0){
            throw new Exception("No data!");
        }
        try{
            FramingHdr framingHdr = parseFrame(d[0]);
            System.out.println(s + " Frame len: " + (1+framingHdr.getCmdLen().getBytelen()));
        }catch(Exception e){
            throw new Exception(s + " parseframe error: " + e);
        }
        System.out.println("Hexdump translation to be done...");
    }

    protected void write(byte[] d, SerialPort con) throws Exception {
        try{
            con.writeBytes(d, d.length);
        }catch(Exception e){
            throw new Exception("Couldn't write" + e);
        }
    }

    protected byte[] readFrame(FwCmd expectedResp, int expectedID, SerialPort con) throws Exception {
        byte eEndpoint = expectedResp.getEndpoint();
        validate(expectedID, 1, 3, "Frame ID needs to be between 1..3");
        validate(eEndpoint, 0, 3, "Endpoint must be 0..3");
        validate(expectedResp.getCmdLen().getByteVal(), 0, 3, "cmdLen must be 0..3");

        byte[] rxHdr = new byte[1];
        int n;
        try{
            n = con.readBytes(rxHdr,1);
        }catch(Exception e){
            throw new Exception("Read failed, error: " + e);
        }
        validate(n, 1, Integer.MAX_VALUE, "Read timeout!");

        FramingHdr hdr;
        try{
            hdr = parseFrame(rxHdr[0]);
        }catch(Exception e){
            throw new Exception("Couldn't parse framing header. Failed with error: " + e);
        }
        if(hdr.getResponseNotOk()){
            throw new Exception("Response status not OK");
            //TODO: Incomplete error management as compared to golang implementation.
            // Doesn't extract rest of read, which means key must be reset (plugged out/in).
        }
        if(hdr.getCmdLen() != expectedResp.getCmdLen()) throw new Exception("Expected cmdlen " + expectedResp.getCmdLen () + " , got" + hdr.getCmdLen());

        validate(hdr.getEndpoint(), eEndpoint, eEndpoint, "Msg not meant for us, dest: " + hdr.getEndpoint());
        validate(hdr.getID(), expectedID, expectedID, "Expected ID: " + expectedID + " got: " + hdr.getID());

        byte[] rx = new byte[1+(expectedResp.getCmdLen().getBytelen())];
        rx[0] = rxHdr[0];
        int eRespCode = expectedResp.getCode();

        try{
            //This is a workaround that isn't in the golang implementation.
            byte[] newData = new byte[con.bytesAvailable()];
            con.readBytes(newData, newData.length);
            for (byte newDatum : newData) {
                if (newDatum == eRespCode) {
                    rx[1] = newDatum;
                    break;
                }
            }
        }catch(Exception e){
            throw new Exception("Read failed, error: " + e);
        }
        //this line causes issues
        if(rx[1] != eRespCode){
            System.out.println("Expected cmd code 0x" + eRespCode + " , got 0x" + rx[1]);
            System.out.println("Check app and restart is recommended!");
        }
        //validate(rx[1], eRespCode, eRespCode, "Expected cmd code 0x" + eRespCode + " , got 0x" + rx[1]);
        return rx;
    }

    private void validate(int value, int min, int max, String errorMessage) throws Exception {
        if (value < min || value > max) throw new Exception(errorMessage);
    }

    public FwCmd getCmdGetNameVersion() {
        return cmdGetNameVersion;
    }

    public FwCmd getRspGetNameVersion() {
        return rspGetNameVersion;
    }

    public FwCmd getCmdLoadApp() {
        return cmdLoadApp;
    }

    public FwCmd getRspLoadApp() {
        return rspLoadApp;
    }

    public FwCmd getCmdLoadAppData() {
        return cmdLoadAppData;
    }

    public FwCmd getRspLoadAppData() {
        return rspLoadAppData;
    }

    public FwCmd getRspLoadAppDataReady() {
        return rspLoadAppDataReady;
    }

    public FwCmd getCmdGetUDI() {
        return cmdGetUDI;
    }

    public FwCmd getRspGetUDI() {
        return rspGetUDI;
    }
}

