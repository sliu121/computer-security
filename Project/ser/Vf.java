import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;


public class Vf{
 
    public static void main(String[] args) throws IOException, NoSuchAlgorithmException {
        int portNumber = -1;
        portNumber = Integer.parseInt(args[0]);
        ServerSocket serverSocket = new ServerSocket(portNumber);

        Socket socket = null;
        while(true) {
        socket = serverSocket.accept();
        socket.setSoTimeout(4000000);
        Thread thread = new Thread(new mul_ser(socket));
        thread.start();
        }
    }
}
