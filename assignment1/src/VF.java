
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class VF {

    static String NOHISTORY = "no vote histrory";

    class User{
        String userName;
        String vNumber;
        public  User(String name, String number){
            this.userName = name;
            this.vNumber = number;
        }
    }
    private int socketCount = 0;
    public class ServerThread extends Thread {
        Socket socket = null;
        private BufferedReader reader;
        private PrintWriter writer;
        private RSAPrivateKey priKey;

        public ServerThread(Socket socket) {
            this.socket = socket;
        }
        public void resetReaderAndWriter() throws IOException{
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream(),"UTF-8"));
            writer = new PrintWriter(socket.getOutputStream(), true);
        }

        public User getTheUser(String userName_vnumber) throws Exception{
            if (userName_vnumber == null) return null;
            String user_enc = userName_vnumber.split("\\|\\|")[0];
            String name_sign = userName_vnumber.split("\\|\\|")[1];

            String name_number = RSAEncrypt.decrypt(priKey,user_enc);
            String userName = name_number.split("\\|\\|")[0];
            String userNumber = name_number.split("\\|\\|")[1];

            FileReader fileReader = new FileReader("src/voterinfo.txt");
            BufferedReader bufferedReader =
                    new BufferedReader(fileReader);
            boolean validName = false;
            String line = null;
            while((line = bufferedReader.readLine()) != null) {
                String findName = line.split(" ")[0];
                if (findName.compareTo(userName) == 0 ){
                    findName = line.split(" ")[1];
                    if (findName.compareTo(userNumber) == 0){
                        validName = true;
                    }
                    break;
                }
            }
            bufferedReader.close();
            if (validName){
                RSAPublicKey userPublicKey =  RSAEncrypt.loadPublicKeyByFile("VFCer/"+userName+"_public.pem");
                if (userPublicKey!=null){
                    String verifyName = RSAEncrypt.verifySignature(userPublicKey,name_sign);
                    if (verifyName.compareTo(userName) == 0){
                        return new User(userName,userNumber);
                    }
                }
            }
            return null;
        }
        public boolean checkVoteValid(User user) throws IOException{
            String history = lookupHistory(user);
            if (history.compareTo(NOHISTORY) == 0) return true;
            return false;
        }
        public String decryptMessage(String message) throws Exception{
            return RSAEncrypt.decrypt(priKey,message);
        }
        public void updateHistrory(User user) {
            try {
                File f=new File("src/history.txt");
                FileWriter fileWriter = new FileWriter(f,true);
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date date = new Date();

                PrintWriter out = new PrintWriter(fileWriter,true);

                out.write(user.vNumber+" "+dateFormat.format(date)+"\n");

                out.close();
            }catch (IOException e){
                System.out.println(e);
            }
        }
        public void updateTheResult(User user,String number) throws Exception{
            updateHistrory(user);
            BufferedReader in =new BufferedReader(new FileReader("src/result.txt"));

            ArrayList<String> list = new ArrayList<String>(2);
            String line;
            String user1 = "";Integer result1=0;
            String user2 = "";Integer result2=0;
            int i=0;
            while((line = in.readLine()) != null) {
                i++;
                String userName = line.split(" ")[0];
                String vNumber = line.split(" ")[1];
                if (i==1){
                    user1 = userName;
                    result1 = Integer.parseInt(vNumber);
                }else if (i == 2){
                    user2 = userName;
                    result2 = Integer.parseInt(vNumber);
                }
            }
            in.close();
            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("src/result.txt")));
            if (Integer.parseInt(number) == 1){
                result1 += 1;
            }else if (Integer.parseInt(number) == 2){
                result2 += 1;
            }
            out.println("Tim "+result1+"\nLinda "+result2);
            out.close();
        }
        public String lookupHistory(User user) throws IOException{
            FileReader fileReader = new FileReader("src/history.txt");
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line = null;
            while((line = bufferedReader.readLine()) != null) {
                String findNumber = line.split(" ")[0];
                if (findNumber.compareTo(user.vNumber) == 0){
                    bufferedReader.close();
                    return line;
                }
            }
            bufferedReader.close();
            return NOHISTORY;
        }
        public boolean isAllUsersVoted() throws IOException{
            FileReader fileReader = new FileReader("src/history.txt");
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line = null;
            int count =0;
            while((line = bufferedReader.readLine()) != null) {
                count++;
            }
            bufferedReader.close();
            return (count >= 3);
        }
        public String getTheVoteResult() throws IOException{
            String user1 = "";String result1="";
            String user2 = "";String result2="";
            FileReader fileReader = new FileReader("src/result.txt");
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line = null;
            int i=0;
            while((line = bufferedReader.readLine()) != null) {
                i++;
                String userName = line.split(" ")[0];
                String vNumber = line.split(" ")[1];
                if (i==1){
                    user1 = userName;
                    result1 = vNumber;
                }else if (i == 2){
                    user2 = userName;
                    result2 = vNumber;
                }
            }
            String winUser = Integer.parseInt(result1)>Integer.parseInt(result2)?user1:user2;
            return (winUser+" Win@@ "+user1+" "+result1+"@@ "+user2+" "+result2);
        }

        public void printTheResultIfNeeded() throws IOException{
            if (isAllUsersVoted()) System.out.println(getTheVoteResult().replace("@@","\n"));
        }

        public String getUserName_vNumber() throws  IOException{
            String message = reader.readLine();
            System.out.println("Client: "+message);
            if (message == null) shutdown();
            //in.close();
            return message;
        }

        public String sendResponseCode(int code, boolean wait) throws IOException{
            String result = "";
            writer.println(code);
            //write to client
            resetReaderAndWriter();
            if (wait){
                result = reader.readLine();
                if (result == null) shutdown();
                System.out.println("Client: "+result);
            }
            return result;
        }

        public String sendResponseString(String message) throws IOException{
            writer.println(message);
            resetReaderAndWriter();
            String result = reader.readLine();
            System.out.println("Client: "+result);
            if (result == null) socket.close();

            return result;
        }
        public void shutdown() throws IOException{
            if(writer!=null) writer.close();
            if(reader!=null) reader.close();
            if(socket!=null) socket.close();
            System.out.println("Server On:"+ --socketCount);
        }

        public void run(){

            try {
                System.out.println("Server On:"+ ++socketCount);
                priKey = RSAEncrypt.loadPrivateKeyByFile("VFCer/vf_private.pem");
                //get user
                User user = null;
                String select = "";
                resetReaderAndWriter();
                while (user == null){
                    String userName_vnumber = getUserName_vNumber();
                    if (userName_vnumber == null)break;
                    user = getTheUser(userName_vnumber);
                    select = sendResponseCode(user == null ? 0 : 1,user == null?false:true);
                }
                boolean goToMenu = true;
                while (user!=null && goToMenu){
                    switch (Integer.parseInt(select)){
                        case 1:
                            if (checkVoteValid(user)){
                                String number_encrypt = sendResponseCode(1,true);//1.Tim 2.Linda
                                String number = decryptMessage(number_encrypt);
                                updateTheResult(user,number);
                                printTheResultIfNeeded();
                                select = reader.readLine();
                            }else {
                                select = sendResponseCode(0,true);
                            }
                            break;
                        case 2:
                            String history = lookupHistory(user);
                            select = sendResponseString(history);
                            break;
                        case 3:
                            if (isAllUsersVoted()){
                                String result = getTheVoteResult();
                                select = sendResponseString(result);
                            }else {
                                select = sendResponseCode(0,true);
                            }
                            break;
                        case 4:
                            goToMenu = false;
                            break;
                        default:
                    }
                }
                shutdown();
            }catch (Exception e){
                System.out.println("run :"+e);
            }
        }
    }
    public  void buildServer(String[] args) throws IOException,Exception{
        int port = 8888;
        if (args.length == 2){
            port = Integer.parseInt(args[0]);
        }
        ServerSocket server = new ServerSocket(port);
        while (true){
            Socket socket = server.accept();
            ServerThread serverThread=new ServerThread(socket);
            serverThread.start();
        }
    }
    public static void main(String[] args){
        try {
            VF vf = new VF();
            System.out.println("Build Server");
            vf.buildServer(args);
        }catch (Exception e){
            System.out.println("main :"+e);
        }
    }
}