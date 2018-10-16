package server;


import javax.crypto.SealedObject;
import java.io.InvalidClassException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;




public class vf{
 
    public static void main(String[] args) throws IOException {
        int port_num = -1;
        try {
            port_num = Integer.parseInt(args[0]);
        } catch (NumberFormatException error) {
            serfunc.handleException(error, "NumberFormat");
        }
        ServerSocket serverSocket = new ServerSocket(port_num);
        
        Socket socket;
        while(true) {
        socket = serverSocket.accept();
        socket.setSoTimeout(3000000);
        Thread thread = new Thread(new mul_ser(socket));
        thread.start();

        }
    }
}
