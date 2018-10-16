import javax.crypto.*;
import java.io.*;
import java.security.*;
import java.util.ArrayList;
import java.util.HashMap;


class serfunc {
    serfunc() {}

    static void handleException(Exception exception, String errorMessage) {
        System.err.println(exception.getMessage());
        System.err.println(errorMessage);
        exception.printStackTrace();
        System.exit(1);
    }

    protected KeyPair gensyskey() throws IOException, NoSuchAlgorithmException {
        KeyPairGenerator keyGenerator = KeyPairGenerator.getInstance("RSA");
        keyGenerator.initialize(2048);
        KeyPair keys = keyGenerator.generateKeyPair();

        FileOutputStream publicKeyFile = new FileOutputStream("server_public.key");
        publicKeyFile.write(keys.getPublic().getEncoded());
        publicKeyFile.close();

        FileOutputStream privateKeyFile = new FileOutputStream("server_private.key");
        privateKeyFile.write(keys.getPrivate().getEncoded());
        privateKeyFile.close();

        return keys;
    }

    
    
    Object decrypt(Key key, SealedObject encrypted) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, ClassNotFoundException, BadPaddingException, IllegalBlockSizeException, IOException {
        Cipher c = Cipher.getInstance("RSA");
        c.init(Cipher.DECRYPT_MODE, key);
        return encrypted.getObject(c);
    }


    ArrayList<voters_opera> read_votersinfo() throws IOException {
        ArrayList<voters_opera> voters = new ArrayList<voters_opera>();

        /**get info from voterinfo file**/

        String voter_info;
        try {
            BufferedReader read_voterinfo = new BufferedReader(new FileReader("./ser/voterinfo"));
            while ((voter_info = read_voterinfo.readLine()) != null && !voter_info.trim().isEmpty()) {
                voters.add(new voters_opera(voter_info));
            }
            read_voterinfo.close();
        } catch (FileNotFoundException erorr) {
            handleException(erorr, "FileNotFound");
        } catch (IOException erorr) {
            handleException(erorr, "error in I/O");
        }
        /**read info from history file **/
        try {
            String voter_history;
            BufferedReader read_history = new BufferedReader(new FileReader("./ser/history"));
            String[] history;
            while ((voter_history = read_history.readLine()) != null && !voter_history.trim().isEmpty()) {
                history = voter_history.split(" ");
                for (voters_opera voter : voters) {
                    if (voter.voter_info().contains(history[0])) {
                        voter.is_voted();
                        voter.votedres(history[1]);
                        break;
                    }
                }
            }
            read_history.close();
        } catch (FileNotFoundException erorr) { /**if there is no history file, we need to create a new history file**/
            this.new_history();
        }
        /**save all info of voter and his vote_history in voters.**/
        return voters;
    }


    HashMap<String,Integer> read_result() throws IOException {
        HashMap<String,Integer> result = new HashMap<String,Integer>();
        String read_result;
        String[] candidateVote;
        BufferedReader resultFile = new BufferedReader(new FileReader("./ser/result"));
        while ((read_result = resultFile.readLine()) != null && !read_result.trim().isEmpty()) {
            candidateVote = read_result.split(" ");
            result.put(candidateVote[0], Integer.parseInt(candidateVote[1]));
        }
        resultFile.close();
        return result;

    }

    private void new_history() throws IOException {
        File historyFile = new File("./ser/history");
        historyFile.createNewFile();
        System.out.println("create his file");

    }


boolean is_voted(voters_opera voter) {
    if(voter.has_voted())
        return true;
    else
        return false;
}

    void update_history(voters_opera voter) {
        try {

            BufferedWriter read_history = new BufferedWriter(new FileWriter("./ser/history", true));
            voter.has_voted();
            read_history.write(voter.voter_id()+" "+voter.voted_time());
            read_history.newLine();
            read_history.flush();
            read_history.close();
        } catch (IOException erorr) {
            handleException(erorr, "I/O Error when using history file");
        }
    }

    void update_result(HashMap<String,Integer> result) throws IOException {
        BufferedWriter resultFile = new BufferedWriter(new FileWriter("./ser/result"));
        for(String candidate : result.keySet()) {
            resultFile.write(candidate + " " + Integer.toString(result.get(candidate)));
            System.out.println("update the result"+" "+Integer.toString(result.get(candidate)));
            resultFile.newLine();
        }
        resultFile.close();
    }
    String read_history(String Id) throws IOException {
        String read_history_file;
        String res_read_history="0";
        boolean is_voted = true;
        BufferedReader read_history = new BufferedReader(new FileReader("./ser/history"));
        while ((read_history_file = read_history.readLine()) != null && !read_history_file.trim().isEmpty()) {
            String[] historyentry=read_history_file.split(" ");
            if(Id.equals(historyentry[0])) {    //check if the voter has voted before，if the voter has, we should printfout the history.
                is_voted = true;
                break;
            }else {
                is_voted = false;
            }
        }
        if(is_voted==true) {
            res_read_history=read_history_file;
        }else {
            res_read_history="You have no voting hitory";
        }
        read_history.close();
        return res_read_history;
    }

}
