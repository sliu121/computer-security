import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SealedObject;
import java.io.InvalidClassException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.HashMap;

public class mul_ser implements Runnable {
    private ArrayList<voters_opera> voters;
    private HashMap<String, Integer> result;
    private serfunc func;
    private KeyPair keys;
    Socket socket = null;


    public mul_ser(Socket socket) throws IOException, NoSuchAlgorithmException {
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

                ObjectOutputStream out_ser = new ObjectOutputStream(socket.getOutputStream());
                out_ser.writeObject(this.keys.getPublic());

                ObjectInputStream in_ser = new ObjectInputStream(socket.getInputStream());
                PublicKey voter_pub_key = (PublicKey) in_ser.readObject();
                PublicKey cli_key = voter_pub_key;

                SealedObject encryption_info = (SealedObject) in_ser.readObject();
                byte[] Bytes = new byte[256];
                in_ser.readFully(Bytes);


                String voterinfo = (String) this.func.decrypt(this.keys.getPrivate(), encryption_info);
                String[] name = voterinfo.split(" ");

                Signature voter_DS = Signature.getInstance("SHA256withRSA");
                voter_DS.initVerify(cli_key);
                voter_DS.update(name[0].getBytes());

                voters_opera the_voter = this.voters.get(0);
                boolean isMatched = false;
                if (voter_DS.verify(Bytes)) {
                    for (voters_opera voter : this.voters) {
                        if (voter.voter_info().equals(voterinfo)) {
                            the_voter = voter;
                            isMatched = true;
                            break;
                        } else {
                            isMatched = false;
                        }
                    }
                } else {
                    System.out.println("cant verify DS");
                    return;
                }

                while (isMatched) {

                    out_ser = new ObjectOutputStream(socket.getOutputStream());
                    short res_0 = 1;
                    out_ser.writeObject(res_0);
                    short res_2;

                    while (true) {
                        in_ser = new ObjectInputStream(socket.getInputStream());
                        res_2 = (short) in_ser.readObject();
                        switch (res_2) {

                            case 1:
                                if (this.func.is_voted(the_voter)) {
                                    out_ser = new ObjectOutputStream(socket.getOutputStream());
                                    res_0 = 0;
                                    out_ser.writeObject(res_0);
                                } else {
                                    out_ser = new ObjectOutputStream(socket.getOutputStream());
                                    res_0 = 1;
                                    out_ser.writeObject(res_0);

                                    in_ser = new ObjectInputStream(socket.getInputStream());
                                    SealedObject encryptedaction = (SealedObject) in_ser.readObject();
                                    String voteraction = (String) this.func.decrypt(this.keys.getPrivate(), encryptedaction);
                                    if (voteraction.equals("1")) {
                                        String vote_to_Tim = "Tim";
                                        for (String res_3 : this.result.keySet()) {
                                            if (vote_to_Tim.equals(res_3)) {
                                                this.result.put(vote_to_Tim, this.result.get(vote_to_Tim) + 1);
                                            }
                                        }
                                        this.func.update_result(this.result);
                                        this.func.update_history(the_voter);
                                    }
                                    if (voteraction.equals("2")) {
                                        String vote_to_Linda = "Linda";
                                        for (String res_4 : this.result.keySet()) {
                                            if (vote_to_Linda.equals(res_4)) {
                                                this.result.put(vote_to_Linda, this.result.get(vote_to_Linda) + 1);
                                            }
                                        }
                                        this.func.update_result(this.result);
                                        this.func.update_history(the_voter);
                                    }
                                }

                            case 2:
                                String voter_reg;
                                voter_reg = the_voter.voter_id();
                                String res_5 = this.func.read_history(voter_reg);
                                out_ser = new ObjectOutputStream(socket.getOutputStream());
                                out_ser.writeObject(res_5);


                            case 3:
                                String vote_to_Tim = "Tim";
                                String vote_to_Linda = "Linda";
                                Integer voted_to_Tim = 0;
                                Integer voted_to_Linda = 0;
                                Integer total_voted_number = 0;
                                voted_to_Tim = this.result.get(vote_to_Tim);
                                voted_to_Linda = this.result.get(vote_to_Linda);
                                total_voted_number = voted_to_Tim + voted_to_Linda;
                                String res_final;
                                if (total_voted_number == 3) {
                                    if (voted_to_Tim > voted_to_Linda) {
                                        res_final = "Winner Tim\nTim " + voted_to_Tim + "\nLinda " + voted_to_Linda;
                                        out_ser = new ObjectOutputStream(socket.getOutputStream());
                                        out_ser.writeObject(res_final);
                                    }
                                    if (voted_to_Tim < voted_to_Linda) {
                                        res_final = "Winner Linda\nTim " + voted_to_Tim + "\nLinda " + voted_to_Linda;
                                        out_ser = new ObjectOutputStream(socket.getOutputStream());
                                        out_ser.writeObject(res_final);
                                    }
                                } else {
                                    res_final = "No result";
                                    out_ser = new ObjectOutputStream(socket.getOutputStream());
                                    out_ser.writeObject(res_final);
                                }
                            case 4:
                                return;

                        }
                    }
                }
                out_ser.reset();
                in_ser.reset();
                out_ser.close();
                in_ser.close();
                socket.close();

            } catch (SignatureException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (BadPaddingException e) {
                e.printStackTrace();
            } catch (InvalidKeyException e) {
                e.printStackTrace();
            } catch (NoSuchPaddingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (IllegalBlockSizeException e) {
                e.printStackTrace();
            }
        }


    }


}





