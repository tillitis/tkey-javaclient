public class FramingHdr {


    private int ID;

    private int endpoint;

    private CmdLen cmdLen;

    private boolean ResponseNotOk;

    public FramingHdr(int ID, int endpoint, CmdLen cmdLen){
        this.cmdLen = cmdLen;
        this.ID = ID;
        this.endpoint = endpoint;
        this.ResponseNotOk = false;
    }

    public int getID() {
        return ID;
    }

    public int getEndpoint() {
        return endpoint;
    }

    public CmdLen getCmdLen() {
        return cmdLen;
    }

    public boolean getResponseNotOk() {
        return ResponseNotOk;
    }

    public void setResponseNotOk(boolean response){
        ResponseNotOk = response;
    }
}
