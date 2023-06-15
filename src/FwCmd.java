public class FwCmd{
    private int code;
    private CmdLen cmdLen;
    private byte endpoint;

    public FwCmd(int code, String name, CmdLen cmdLen) {
        this.code = code;
        this.cmdLen = cmdLen;
        this.endpoint = 2;
    }

    public int getCode() {
        return code;
    }
    public byte getEndpoint(){
        return endpoint;
    }

    public CmdLen getCmdLen() {
        return cmdLen;
    }
}