import com.fazecast.jSerialComm.SerialPort;

import static com.fazecast.jSerialComm.SerialPort.*;

public class SerialConnHandler {

    private static SerialPort conn;

    private static int speed;

    private static String PORT_NAME;


    protected SerialConnHandler(String port){
        this.speed = 62500;
        this.PORT_NAME = port;
    }

    protected void connect() throws Exception {
        conn = SerialPort.getCommPort(PORT_NAME);
        conn.setBaudRate(speed);
        conn.openPort();
        setReadTimeout();
        if (!conn.openPort()) {
            throw new Exception("Unable to open port " + PORT_NAME);
        }
        else{
            System.out.println("Port opened!");
        }
    }

    protected void closePort(){
        conn.closePort();
    }

    public SerialPort getConn(){
        return conn;
    }

    protected void setReadTimeout(){
        try{
            conn.setComPortTimeouts(TIMEOUT_READ_BLOCKING,100,30);
        }catch (Exception e){
            System.out.println("Failed to set read timeout with err: " + e);
        }
    }

}
