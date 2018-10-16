package client;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.*;

public class voter_cli {
    private Socket client;
    private cli_func func;
    private KeyPair voter_Key;

    private voter_cli(String server, int portnum) {
        try {
            this.client = new Socket(server, portnum);
            this.func = new cli_func();
            this.voter_Key = this.func.receivevoterkey();
        } catch (UnknownHostException error) {
            cli_func.Exception_Operation(error, "Unknown Host");
        } catch (IOException error) {
            cli_func.Exception_Operation(error, "error in I/O");
        } catch (IllegalArgumentException error) {
            cli_func.Exception_Operation(error, "IllegalArgument");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    private void run() {
        try {
            ObjectOutputStream cli_out = new ObjectOutputStream(this.client.getOutputStream());
            ObjectInputStream cli_in = new ObjectInputStream(this.client.getInputStream());

           /**get key**/

            PublicKey sys_public_kye = (PublicKey) cli_in.readObject();
            cli_out.writeObject(this.voter_Key.getPublic());

            String voter = this.func.voter_name();
            String voter_id = this.func.voter_id();
            String voter_info = voter + ' ' + voter_id;

            Signature voter_ds = Signature.getInstance("SHA256withRSA");
            voter_ds.initSign(this.voter_Key.getPrivate());
            voter_ds.update(voter.getBytes());


            cli_out.writeObject(func.encrypt(sys_public_kye, voter_info));
            cli_out.flush();
            cli_out.write(voter_ds.sign());
            cli_out.flush();

            cli_in = new ObjectInputStream(this.client.getInputStream());

/***operation in system**/
            if ((short)cli_in.readObject() == 1) {
                System.out.println("Connected");
                String operation;
                short cli_operation;
                do {
                	
                    	operation = this.func.voter_system(voter);

                    	if (operation.equals("1")) {
                    	cli_operation=1;
                    	cli_out = new ObjectOutputStream(this.client.getOutputStream());
                        cli_out.writeObject(cli_operation);
                        
                        cli_in = new ObjectInputStream(this.client.getInputStream());
                        if((short)cli_in.readObject()==0) {
                        	System.out.println("Sorry, you have already voted");
                        }
                        else {
                        	operation =this.func.voting_system();
                        	System.out.println("sub operation"+operation);
                        	if (operation.equals("11")) {
                        		cli_out = new ObjectOutputStream(this.client.getOutputStream());
                        		cli_out.writeObject(func.encrypt(sys_public_kye, operation));
//                        		System.out.println("already sent action2");
                        	}else if (operation.equals("12")) {
                        		cli_out = new ObjectOutputStream(this.client.getOutputStream());
                        		cli_out.writeObject(func.encrypt(sys_public_kye, operation));
                        }
                        }
                    }
                    	
                    if (operation.equals("2")) {
                    	cli_operation=2;
                        cli_in = new ObjectInputStream(this.client.getInputStream());
                        cli_out = new ObjectOutputStream(this.client.getOutputStream());
                        cli_out.writeObject(cli_operation);
                        
                        String check_voter_history;
                        check_voter_history = (String) cli_in.readObject();
                        System.out.println(check_voter_history);

                    } 
                    if (operation.equals("3")) {
                    	cli_operation=3;
                    	cli_out = new ObjectOutputStream(this.client.getOutputStream());
                        cli_out.writeObject(cli_operation);
                        cli_in = new ObjectInputStream(this.client.getInputStream());
                        String voted_result;
                        voted_result = (String) cli_in.readObject();
                        if(voted_result.equals("0"))
                        	System.out.println("No result");
                        else
                        	System.out.println(voted_result);
                        } 
                    if (operation.equals("4")) {
                    	cli_operation=4;
                    	cli_out = new ObjectOutputStream(this.client.getOutputStream());
                        cli_out.writeObject(cli_operation);
                        System.out.println("Thank you for voting!");
                    }
                } while (!operation.equals("4"));

            } else {
                System.out.println("Invalid name or ID");
            }
        } catch (IOException error) {
            cli_func.Exception_Operation(error, "error in I/O");
        } catch (ClassNotFoundException e) {
            System.exit(1);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (SignatureException e) {
            e.printStackTrace();
        }
    }

    private void close() {
        try {
            this.client.close();
        } catch (IOException error) {
            cli_func.Exception_Operation(error, "error in I/O");
        }
    }

    public static void main(String[] args) {
        String serverDomain = args[0];
            int portNumber = 0;
            try {
            portNumber = Integer.parseInt(args[1]);
        } catch (NumberFormatException error) {
            cli_func.Exception_Operation(error, "NumberFormat");
        }
        voter_cli voterClient = new voter_cli(serverDomain, portNumber);
        voterClient.run();
        voterClient.close();
    }
}
