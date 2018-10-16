package server;

import javax.crypto.*;
import java.io.InvalidClassException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.security.*;
import java.util.ArrayList;
import java.util.HashMap;

//@@PowerMockIgnore("javax.crypto.*")
public class mul_ser implements Runnable {
	   private ServerSocket listen;
	    private serfunc func;
	    private KeyPair keys;
	    private ArrayList<voters_opera> voters;
	    private HashMap<String,Integer> result;
		//private Socket socket;
	    
		Socket socket = null;  
	    

	    public mul_ser(Socket socket)  {

	            this.func = new serfunc();
	            this.keys = this.func.gensyskey();
	            this.voters = this.func.read_votersinfo();
	            this.result = this.func.read_result();
	            this.socket = socket;

	    }

	    public void run() {
	        while (true) {
	            try {

	                System.out.println("Server Connected");
	                
	                ObjectOutputStream ser_Out = new ObjectOutputStream(socket.getOutputStream());
	                ser_Out.writeObject(this.keys.getPublic());
	                ser_Out = new ObjectOutputStream(socket.getOutputStream());
	                ObjectInputStream ser_in = new ObjectInputStream(socket.getInputStream());

	                PublicKey voter_pub_key = (PublicKey)ser_in.readObject();

	                SealedObject encrypt_voterinfo = (SealedObject) ser_in.readObject();
                    System.out.println(encrypt_voterinfo);

                    byte[] voter_ds = new byte[1000];
	                ser_in.readFully(voter_ds);

	                String voter_decrypt = (String) this.func.decrypt(this.keys.getPrivate(), encrypt_voterinfo);
	                System.out.println(voter_decrypt);

	                String[] voter_1 = voter_decrypt.split(" ");
                    Signature voter_digital = Signature.getInstance("SHA256withRSA");

                    voter_digital.initVerify(voter_pub_key);
                    voter_digital.update(voter_1[0].getBytes());

	                voters_opera the_voter = this.voters.get(0);
	                boolean isMatched = false;
	                for (voters_opera voter : this.voters) {
	                    if (voter.voter_info().equals(voter_decrypt)) {
	                        the_voter = voter;
	                        isMatched = true;
	                        break;
	                    }
	                }

	                while (isMatched) {
	                	
	                	
	                	ser_Out = new ObjectOutputStream(socket.getOutputStream());
	                	short response=1;
	                    ser_Out.writeObject(response);
	                    System.out.println("already match");

	                    while(true) {
                            ser_in = new ObjectInputStream(socket.getInputStream());
                            System.out.println("operation is"+(short)ser_in.readObject());

                            switch ((short)ser_in.readObject()){

                                case 1:
                                    if(this.func.is_voted(the_voter)) {
                                        ser_Out = new ObjectOutputStream(socket.getOutputStream());
                                        response=0;
                                        ser_Out.writeObject(response);
                                    }else {
                                        ser_Out = new ObjectOutputStream(socket.getOutputStream());
                                        response=1;
                                        ser_Out.writeObject(response);

                                        ser_in = new ObjectInputStream(socket.getInputStream());
                                        SealedObject encrypt_votinfo = (SealedObject) ser_in.readObject();
                                        String voting = (String) this.func.decrypt(this.keys.getPrivate(), encrypt_votinfo);
                                        System.out.println("Voter voting:"+voting);
                                        if (voting.equals("11")) {
                                            String vote_to_Tim="Tim";
                                            for(String candidate : this.result.keySet()) {
                                                if(vote_to_Tim.equals(candidate)) {
                                                    this.result.put(vote_to_Tim,this.result.get(vote_to_Tim)+1);
                                                }
                                            }
                                            this.func.update_result(this.result);
                                            this.func.update_history(the_voter);
                                        }
                                        if (voting.equals("12")) {
                                            String vote_to_Linda="Linda";
                                            for(String candidate : this.result.keySet()) {
                                                if(vote_to_Linda.equals(candidate)) {
                                                    this.result.put(vote_to_Linda,this.result.get(vote_to_Linda)+1);
                                                }
                                            }
                                            this.func.update_result(this.result);
                                            this.func.update_history(the_voter);
                                        }
                                    }


                                case 2:
                                    String Id;
                                    Id = the_voter.voter_id();
                                    System.out.println("action 2"+Id);
                                    String result = this.func.read_history(Id);
                                    System.out.println("action 2 sent"+result);
                                    ser_Out = new ObjectOutputStream(socket.getOutputStream());
                                    ser_Out.writeObject(result);


                                case 3:
                                    String vote_to_Tim="Tim";
                                    String vote_to_Linda="Linda";
                                    Integer voted_to_Tim = 0;
                                    Integer voted_to_Linda = 0;
                                    Integer total_voted_number = 0;
                                    voted_to_Tim = this.result.get(vote_to_Tim);
                                    voted_to_Linda = this.result.get(vote_to_Linda);
                                    total_voted_number = voted_to_Tim+voted_to_Linda;
                                    if(total_voted_number==3) {
                                        if(voted_to_Tim>voted_to_Linda) {
                                            System.out.println("Tim Win");
                                            System.out.println("Tim: "+voted_to_Tim);
                                            System.out.println("Linda: "+voted_to_Linda);
                                        ser_Out = new ObjectOutputStream(socket.getOutputStream());
                                            ser_Out.writeObject("Tim Win");
                                        }
                                        if(voted_to_Tim<voted_to_Linda) {
                                            System.out.println("Linda Win");
                                            System.out.println("Tim: "+voted_to_Tim);
                                            System.out.println("Linda: "+voted_to_Linda);

                                            ser_Out = new ObjectOutputStream(socket.getOutputStream());
                                            ser_Out.writeObject("Linda Win");

                                        }
                                    }else {

                                        ser_Out = new ObjectOutputStream(socket.getOutputStream());
                                        ser_Out.writeObject("No result");

                                    }


                                case 4:
                                    return;

                            }
	                    }
	                } 

	                ser_Out.close();
	                ser_in.close();
	                socket.close();
	            } catch (SocketTimeoutException error) {
	                System.out.println("Voting Sysyem will close with no operation");
	                break;
	            } catch (NullPointerException error) {
	                serfunc.handleException(error, "error in input or output");
	            } catch (InvalidClassException error) {
	                serfunc.handleException(error, "class wrong");
	            } catch (ClassNotFoundException error) {
	                serfunc.handleException(error, "Invalid class");
	            } catch (IOException error) {
	                serfunc.handleException(error, "error in I/O");
	            } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (SignatureException e) {
                    e.printStackTrace();
                } catch (InvalidKeyException e) {
                    e.printStackTrace();
                }
            }
	    }

/*	    public void close() {
	        try {
	            this.listen.close();
	        } catch (IOException ex) {
	            serfunc.handleException(ex, "error in I/O");
	        }
	    }
*/
		

		

}
