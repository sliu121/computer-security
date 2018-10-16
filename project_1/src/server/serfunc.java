package server;

import javax.crypto.Cipher;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SealedObject;
import java.io.*;
import java.security.InvalidKeyException;
import java.security.InvalidParameterException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;

class serfunc {
    private Cipher cipher;
    private KeyPairGenerator keyGenerator;
//    private int votenum=0;

    serfunc() {}
    /**create System Keys**/
    protected KeyPair gensyskey(){
        try {
            this.cipher = Cipher.getInstance("RSA");
            this.keyGenerator = KeyPairGenerator.getInstance("RSA");
            this.keyGenerator.initialize(2048);
            KeyPair system_keys = keyGenerator.generateKeyPair();
            FileOutputStream system_pubkey = new FileOutputStream("system_pub.key");
            system_pubkey.write(system_keys.getPublic().getEncoded());
            system_pubkey.close();

            FileOutputStream system_prikey = new FileOutputStream("system_pri.key");
            system_prikey.write(system_keys.getPrivate().getEncoded());
            system_prikey.close();

            return system_keys;
        } catch (NoSuchAlgorithmException error) {
            handleException(error, "NoSuchAlgorithm");
        } catch (NoSuchPaddingException erorr) {
            handleException(erorr, "NoSuchPadding");
        } catch (InvalidParameterException erorr) {
            handleException(erorr, "InvalidParameter");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
/**Add this function as it'e required in several method*/
    static void handleException(Exception exception, String errorMessage) {
        System.err.println(exception.getMessage());
        System.err.println(errorMessage);
        exception.printStackTrace();
        System.exit(1);
    }

/**decrypt messages sent by cli**/
    Object decrypt(Key key, SealedObject encrypted) {
        try {
            this.cipher.init(Cipher.DECRYPT_MODE, key);
            return encrypted.getObject(this.cipher);
        } catch (InvalidKeyException erorr) {
            handleException(erorr, "InvalidKey");
        } catch (IllegalBlockSizeException erorr) {
            handleException(erorr, "IllegalBlockSize");
        } catch (IOException erorr) {
            handleException(erorr, "error in I/O");
        } catch (ClassNotFoundException erorr) {
            handleException(erorr, "ClassNotFound");
        } catch (BadPaddingException erorr) {
            handleException(erorr, "BadPadding");
        }
        return null;
    }

    ArrayList<voters_opera> read_votersinfo() {
        ArrayList<voters_opera> voters = new ArrayList<voters_opera>();

        /**get info from voterinfo file**/

        String voter_info;
        try {
            BufferedReader read_voterinfo = new BufferedReader(new FileReader("src/server/voterinfo"));
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
            BufferedReader read_history = new BufferedReader(new FileReader("src/server/history"));
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
        } catch (IOException erorr) {
            handleException(erorr, "Error in I/O");
        } catch (ArrayIndexOutOfBoundsException erorr) {
            handleException(erorr, "ArrayIndexOutOfBounds");
        }
    /**save all info of voter and his vote_history in voters.**/
        return voters;
    }

    HashMap<String,Integer> read_result() {
        HashMap<String,Integer> result = new HashMap<String,Integer>();
        String read_result;
        String[] candidateVote;
        try {
            BufferedReader resultFile = new BufferedReader(new FileReader("src/server/result"));
            while ((read_result = resultFile.readLine()) != null && !read_result.trim().isEmpty()) {
                candidateVote = read_result.split(" ");
                result.put(candidateVote[0], Integer.parseInt(candidateVote[1]));
            }
            resultFile.close();
        }  catch (IOException erorr) {
            handleException(erorr, "error in I/O");
        } catch (ArrayIndexOutOfBoundsException erorr) {
            handleException(erorr, "ArrayIndexOutOfBounds");
        }
        return result;
    }

    private void new_history() {
        File read_history = new File("src/server/history");
        try {
            read_history.createNewFile();
            System.out.println("create his file");
        } catch (IOException erorr) {
            handleException(erorr, "error in I/O");
        }
    }

    boolean is_voted(voters_opera voter) {
       if(voter.has_voted())
    	   return true;
       else 
           return false;
    }
/**updated history when voters have voted.**/
    void update_history(voters_opera voter) {
        try {

            BufferedWriter read_history = new BufferedWriter(new FileWriter("src/server/history", true));
            voter.is_voted();
            read_history.write(voter.voter_id()+" "+voter.voted_time());
            read_history.newLine();
            read_history.flush();
            read_history.close();
        } catch (IOException erorr) {
            handleException(erorr, "I/O Error when using history file");
        }
    }

    /**updated result file when voters have made a vote.**/
    void update_result(HashMap<String,Integer> result) {
        try {
            BufferedWriter resultFile = new BufferedWriter(new FileWriter("src/server/result"));
            for(String candidate : result.keySet()) {
                resultFile.write(candidate + " " + Integer.toString(result.get(candidate)));
                System.out.println("update the result"+" "+Integer.toString(result.get(candidate)));
                resultFile.newLine();
                resultFile.flush();
            }
            resultFile.close();
        } catch (IOException erorr) {
            handleException(erorr, "error in I/O");
        }
    }
    /**read from history file */
    String read_history(String Id) {
    	String read_history_file;
    	String res_read_history="0";
    	boolean is_voted = true;
        try {
            BufferedReader read_history = new BufferedReader(new FileReader("src/server/history"));
            while ((read_history_file = read_history.readLine()) != null && !read_history_file.trim().isEmpty()) {
            	String[] historyentry=read_history_file.split(" ");
            	if(Id.equals(historyentry[0])) {    //check if the voter has voted before，if the voter has, we should printfout the history.
            		is_voted = true;
                    res_read_history=read_history_file;
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
        } catch (IOException erorr) {
            handleException(erorr, "error in I/O");
        }
        return res_read_history;
    }
    
}
