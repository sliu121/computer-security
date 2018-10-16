import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.*;
import java.security.spec.InvalidKeySpecException;


public class voter_cli {
    private Socket socket;
    private cli_func func;
    private KeyPair voter_key;

    private voter_cli(String server, int portnum) throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeySpecException {

            this.socket = new Socket(server , portnum);
            this.func = new cli_func();
            this.voter_key = this.func.receivevoterkey();

    }

    private void run() {
        try {
            ObjectOutputStream out_cli;
            ObjectInputStream  in_cli= new ObjectInputStream(this.socket.getInputStream());

            PublicKey sys_public_key = (PublicKey) in_cli.readObject();

            out_cli = new ObjectOutputStream(this.socket.getOutputStream());
            PublicKey clientPublicKey=this.voter_key.getPublic();

            out_cli.writeObject(clientPublicKey);
            PublicKey ser_Pub_key = sys_public_key;

            String voter = this.func.voter_name();
            String voter_id = this.func.voter_reg();
            String voter_info = voter + ' ' + voter_id;
            
            Signature voter_DS = Signature.getInstance("SHA256withRSA");
            voter_DS.initSign(this.voter_key.getPrivate());
            voter_DS.update(voter.getBytes());
            
          

            out_cli.writeObject(func.encrypt(ser_Pub_key, voter_info));
            out_cli.flush();
            out_cli.write(voter_DS.sign());
            out_cli.flush();

            in_cli = new ObjectInputStream(this.socket.getInputStream());
            short res = (short)in_cli.readObject();
            if (res == 1) {
                System.out.print("Connected");
                String operation;
                short cli_operation;
                short result;

                do {
                    	operation = this.func.welcome_system(voter);
                    	if (operation.equals("1")) {
                            cli_operation=1;
                            out_cli = new ObjectOutputStream(this.socket.getOutputStream());
                            out_cli.writeObject(cli_operation);

                            in_cli = new ObjectInputStream(this.socket.getInputStream());
                            result = (short)in_cli.readObject();
                            if(result==0) {
                                System.out.println("you have already voted");
                            }
                            else {
                                operation =this.func.voting_system();
                                if (operation.equals("1")) {
                                    System.out.println("choose Tim");
                                    out_cli = new ObjectOutputStream(this.socket.getOutputStream());
                                    out_cli.writeObject(func.encrypt(ser_Pub_key, operation));
                                }else if (operation.equals("2")) {
                                    System.out.println("choose Linda");
                                    out_cli = new ObjectOutputStream(this.socket.getOutputStream());
                                    out_cli.writeObject(func.encrypt(ser_Pub_key, operation));
                                }
                            }
                    	}
                    	if (operation.equals("2")) {
                    	cli_operation=2;
                    	out_cli = new ObjectOutputStream(this.socket.getOutputStream());
                        out_cli.writeObject(cli_operation);

                        in_cli = new ObjectInputStream(this.socket.getInputStream());
                        String check_voter_history;
                        check_voter_history = (String) in_cli.readObject();
                        System.out.println("=================================");
                        System.out.println(check_voter_history);
                        System.out.println("=================================");

                    }
                    if (operation.equals("3")) {
                    	cli_operation=3;
                    	out_cli = new ObjectOutputStream(this.socket.getOutputStream());
                        out_cli.writeObject(cli_operation);

                        in_cli = new ObjectInputStream(this.socket.getInputStream());
                        String voted_result;
                        voted_result = (String) in_cli.readObject();
                        if(voted_result.equals("0")){
                            System.out.println("=================================");
                            System.out.println("No result");
                            System.out.println("=================================");

                        }else {
                            System.out.println("=================================");
                            System.out.println(voted_result);
                            System.out.println("=================================");
                        }
                        }
                    if (operation.equals("4")) {
                    	cli_operation=4;
                    	out_cli = new ObjectOutputStream(this.socket.getOutputStream());
                        out_cli.writeObject(cli_operation);
                        System.out.println("=================================");
                        System.out.println("Thank you for voting!");
                        System.out.println("=================================");
                    }
                } while (!operation.equals("4"));

            }  else {
                System.out.println("Invalid name or ID");
            }
            in_cli.close();
            out_cli.close();
        } catch (IOException e1) {
            e1.printStackTrace();
        } catch (NoSuchAlgorithmException e1) {
            e1.printStackTrace();
        } catch (InvalidKeyException e1) {
            e1.printStackTrace();
        } catch (SignatureException e1) {
            e1.printStackTrace();
        } catch (IllegalBlockSizeException e1) {
            e1.printStackTrace();
        } catch (ClassNotFoundException e1) {
            e1.printStackTrace();
        }
    }

    private void close() throws IOException {
        this.socket.close();
    }

    public static void main(String[] args) throws IOException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeySpecException {
        String serverDomain = args[0];
        int portNumber = 0;
        portNumber = Integer.parseInt(args[1]);
        voter_cli voterClient = new voter_cli(serverDomain, portNumber);
        voterClient.run();
        voterClient.close();
    }
}
