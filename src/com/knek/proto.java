package com.knek;

import com.fazecast.jSerialComm.SerialPort;

public class proto {
    /**
     * Pre-defined list of commands and responses used in TKey communication.
     */
    private final FwCmd cmdGetNameVersion = new FwCmd(0x01, "cmdGetNameVersion", CmdLen.CmdLen1,(byte) 0);
    private final FwCmd rspGetNameVersion = new FwCmd(0x02, "rspGetNameVersion", CmdLen.CmdLen32,(byte) 0);
    private final FwCmd cmdLoadApp = new FwCmd(0x03, "cmdLoadApp", CmdLen.CmdLen128,(byte) 0);
    private final FwCmd rspLoadApp = new FwCmd(0x04, "rspLoadApp", CmdLen.CmdLen4,(byte) 0);
    private final FwCmd cmdLoadAppData = new FwCmd(0x05, "cmdLoadAppData", CmdLen.CmdLen128,(byte) 0);
    private final FwCmd rspLoadAppData = new FwCmd(0x06, "rspLoadAppData", CmdLen.CmdLen4,(byte) 0);
    private final FwCmd rspLoadAppDataReady = new FwCmd(0x07, "rspLoadAppDataReady", CmdLen.CmdLen128,(byte) 0);
    private final FwCmd cmdGetUDI = new FwCmd(0x08, "cmdGetUDI", CmdLen.CmdLen1,(byte) 0);
    private final FwCmd rspGetUDI = new FwCmd(0x09, "rspGetUDI", CmdLen.CmdLen32,(byte) 0);

    /**
     * Parses Framing HDR from the passed in int (retrieved from reading 1 byte from TKey).
     */
    private FramingHdr parseFrame(int b) throws Exception {
        if ((b & 0b1000_0000) != 0) {
            throw new Exception("Reserved bit #7 is not zero");
        }
        boolean response = (b & 0b0000_0100) != 0;
        int id = (b & 0b0110_0000) >> 5;
        int endpoint = (b & 0b0001_1000) >> 3;
        CmdLen cmdLen = CmdLen.values()[b & 0b0000_0011];

        FramingHdr framingHdr = new FramingHdr(id,endpoint,cmdLen);
        framingHdr.setResponseNotOk(response);
        return framingHdr;
    }

    /**
     * NewFrameBuf allocates a buffer with the appropriate size for the
     * command in cmd, including the framing protocol header byte. The cmd
     * parameter is used to get the endpoint and command length, which
     * together with id parameter are encoded as the header byte. The
     * header byte is placed in the first byte in the returned buffer. The
     * command code from cmd is placed in the buffer's second byte.
     */
    protected int[] newFrameBuf(FwCmd cmd, int id) throws Exception{
        CmdLen cmdlen = cmd.cmdLen();

        validate(id, 0, 3, "Frame ID needs to be between 1..3");
        validate(cmd.endpoint(), 0, 3, "Endpoint must be 0..3");
        validate(cmdlen.getByteVal(), 0, 3, "cmdLen must be 0..3");

        int[] tx = new int[cmdlen.getBytelen()+1];
        tx[0] = ((id << 5) | (cmd.endpoint() << 3) | cmdlen.getByteVal());
        tx[1] = cmd.code();
        return tx;
    }

    /**
     * dump(string, int[]) hexdumps data in d with an explaining string s first. It
     * expects d to contain the whole frame as sent on the wire, with the
     * framing protocol header in the first byte.
     */
    protected void dump(String s, int[] d) throws Exception {
        if(d == null || d.length == 0){
            throw new Exception("No data!");
        }
        FramingHdr framingHdr = parseFrame(d[0]);
        System.out.println(s + " frame len: " + 1+framingHdr.getCmdLen().getBytelen() + " bytes)");
    }

    /**
     * Writes an array to the TKey, using the specified SerialPort.
     */
    protected void write(byte[] d, SerialPort con) throws Exception {
        try{
            con.writeBytes(d, d.length);
        }catch(Exception e){
            throw new Exception("Couldn't write" + e);
        }
    }

    /**
     * ReadFrame reads a response in the framing protocol. The header byte
     * is first parsed. If the header has response status Not OK,
     * ErrResponseStatusNotOK is returned. Header command length and
     * endpoint are then checked against the expectedResp parameter,
     * header ID is checked against expectedID. The response code (first
     * byte after header) is also checked against the code in
     * expectedResp. It returns the whole frame read, and the parsed header
     * byte if successful.
     */
    protected byte[] readFrame(FwCmd expectedResp, int expectedID, SerialPort con) throws Exception {
        byte eEndpoint = expectedResp.endpoint();
        validate(expectedID, 0, 3, "Frame ID needs to be between 1..3");
        validate(eEndpoint, 0, 3, "Endpoint must be 0..3");
        validate(expectedResp.cmdLen().getByteVal(), 0, 3, "cmdLen must be 0..3");

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
            Thread.sleep(1);
            hdr = parseFrame(rxHdr[0]);
        }catch(Exception e){
            throw new Exception("Couldn't parse framing header. Failed with error: " + e);
        }
        if(hdr.getResponseNotOk()){
            byte[] rest = new byte[hdr.getCmdLen().getBytelen()];
            con.readBytes(rest,con.bytesAvailable());
            throw new Exception("Response status not OK");
        }
        if(hdr.getCmdLen() != expectedResp.cmdLen()) throw new Exception("Expected cmdlen " + expectedResp.cmdLen() + " , got" + hdr.getCmdLen());

        validate(hdr.getEndpoint(), eEndpoint, eEndpoint, "Msg not meant for us, dest: " + hdr.getEndpoint());
        validate(hdr.getID(), expectedID, expectedID, "Expected ID: " + expectedID + " got: " + hdr.getID());

        byte[] rx = new byte[1+(expectedResp.cmdLen().getBytelen())];
        rx[0] = rxHdr[0];
        int eRespCode = expectedResp.code();
        try{
            if(expectedResp.name().equals("rspGetNameVersion") || expectedResp.name().equals("rspGetUDI") ) con.readBytes(rx,rx.length);

            else readForApp(con, rx, eRespCode);

        } catch(Exception e){
            throw new Exception("Read failed, error: " + e);
        }
        if(rx[1] != eRespCode){
            System.out.println("Expected cmd code 0x" + eRespCode + ", got 0x" + rx[1]);
            System.out.println("If this happens more than once during app loading, check device app and restart is recommended!");
        }
        return rx;
    }

    /**
     * This method just handles the TKeys response after app data is written to it.
     */
    private byte[] readForApp(SerialPort con, byte[] rx, int eRespCode){
        byte[] newData = new byte[con.bytesAvailable()];
        con.readBytes(newData, newData.length);
        for (byte newDatum : newData) {
            if (newDatum == eRespCode) {
                rx[1] = newDatum;
                break;
            }
        }
        return rx;
    }

    /**
     * Helper method for validating values.
     */
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

