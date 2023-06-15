import com.fazecast.jSerialComm.SerialPort;

import static com.fazecast.jSerialComm.SerialPort.*;

public class SerialConnHandler {

    private static SerialPort conn;

    private static int speed;

    protected SerialConnHandler(String port){
        speed = 62500;
        conn = SerialPort.getCommPort(port);
    }

    protected SerialConnHandler(){
        speed = 62500;
        conn = SerialPort.getCommPorts()[0];
    }

    protected void connect() throws Exception {
        conn.setBaudRate(speed);
        conn.openPort();
        setReadTimeout();
        if (!conn.openPort()) {
            throw new Exception("Unable to open port " + conn.getSystemPortName());
        }
        else{
            System.out.println("Port opened!");
        }
    }

    protected void setConn(String name){
        conn = SerialPort.getCommPort(name);
    }

    public void setSpeed(int speed){
        conn.setBaudRate(speed);
    }

    protected void closePort(){
        conn.closePort();
    }

    public SerialPort getConn(){
        return conn;
    }

    protected void setReadTimeout(){
        try{
            //TIMEOUT_READ_BLOCKING
            conn.setComPortTimeouts(TIMEOUT_READ_SEMI_BLOCKING,100,30);
        }catch (Exception e){
            System.out.println("Failed to set read timeout with err: " + e);
        }
    }

}
