
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

public class VoterCli {
    class User{
        String userName;
        String vNumber;
        public  User(String name, String number){
            this.userName = name;
            this.vNumber = number;
        }
    }
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private User user;
    private RSAPublicKey vfPublicKey;
    private RSAPrivateKey priKey;

    public String sendMessage(String message) throws Exception {
        return sendMessage(message,true);
    }
    public String sendMessage(String message ,Boolean wait) throws Exception{
        String result="";
        writer.println(message);
        String line;
        if (wait){
            result = reader.readLine();
            result = result.replace("@@","\n");
        }
        if (result == null) socket.close();
        return result;
    }

    public void voteMethod() throws IOException,Exception{
        String welcome = "======================\n"+
                "Welcome, "+user.userName+"\n Main Menu\n" +
                "Please enter a number (1-4) \n1. Vote\n" +
                "2. My vote history\n" +
                "3. Election result\n" +
                "4. Quit";
        System.out.println(welcome);
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String menu = br.readLine();
        switch (Integer.parseInt(menu)){
            case 1:
                String code = sendMessage(menu);
                if (Integer.parseInt(code) == 0){//already voted
                    System.out.println("you have already voted");
                    voteMethod();
                }else {
                    System.out.println("Please enter a number (1-2)\n 1. Tim\n 2. Linda");
                    br = new BufferedReader(new InputStreamReader(System.in));
                    String vote = br.readLine();
                    String encryVote = RSAEncrypt.encrypt(vfPublicKey,vote);
                    sendMessage(encryVote,false);
                    voteMethod();
                }
                break;
            case 2:
                String result = sendMessage(menu);
                System.out.println(result);
                voteMethod();
                break;
            case 3:
                String returncode = sendMessage(menu);
                if (returncode.length()==1&&Integer.parseInt(returncode) == 0){
                    System.out.println("the result is not available");
                }else {
                    System.out.println(returncode);
                }
                voteMethod();
                break;
            case 4:
                sendMessage("4");
                break;
            default:
        }
    }

    public  void buildClient(String[] args) throws IOException, Exception{
        String ip = "127.0.0.1";
        Integer port = 8888;
        if (args.length == 3){
            ip = args[0];
            port = Integer.parseInt(args[1]);
        }
        socket = new Socket(ip, port);
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream(),"UTF-8"));
        writer = new PrintWriter(socket.getOutputStream(), true);
        vfPublicKey =  RSAEncrypt.loadPublicKeyByFile("VoterCliCer/vf_public.pem");

        boolean verifyUser = false;
        while (!verifyUser){
            System.out.println("please input your user name:");
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            String userName = br.readLine();

            if (userName.compareTo("Alice") == 0||userName.compareTo("Bob")==0||userName.compareTo("John")==0){

            }else {
                System.out.println("wrong user name!");
                continue;
            }
            System.out.println("please input your registration number:");
            br = new BufferedReader(new InputStreamReader(System.in));
            String vNumber = br.readLine();
            String encryptInfo = RSAEncrypt.encrypt(vfPublicKey,(userName+"||"+vNumber));
            priKey = RSAEncrypt.loadPrivateKeyByFile("VoterCliCer/"+userName+"_private.pem");
            String sign = RSAEncrypt.signature(priKey,userName);

            String sending = encryptInfo + "||"+sign;

            String code = sendMessage(sending);
            if (Integer.parseInt(code) == 0){
                System.out.println("Invalid name or registration number");
            }else {
                this.user = new User(userName,vNumber);
                verifyUser = true;
            }
        }
        voteMethod();
        if (socket!=null) socket.close();
    }

    public static void main(String[] args) throws IOException {
        try {
            VoterCli voter = new VoterCli();
            voter.buildClient(args);
        } catch (Exception e) {
            System.out.println("can not listen to:" + e);// 出错，打印出错信息
        }
    }
}