import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SealedObject;
import java.awt.*;
import java.io.*;
import java.security.*;
import java.security.spec.*;

class cli_func {

    private Cipher cipher;
    private BufferedReader voter_input;

 /*   static void Exception_Operation(Exception exception, String message) {
        System.err.println(exception.getMessage());
        System.err.println(message);
        exception.printStackTrace();
        System.exit(1);
    }
*/
    cli_func() throws NoSuchPaddingException, NoSuchAlgorithmException {
        this.voter_input = new BufferedReader(new InputStreamReader(System.in));
        this.cipher = Cipher.getInstance("RSA");
    }

    String voter_name() throws IOException {
        String voter = new String();
        System.out.print("Input your Name: ");
        voter = this.voter_input.readLine();
        return voter;
    }

    String voter_reg() throws IOException {
        String Id = new String();
        System.out.print("Input your Id: ");
        Id = this.voter_input.readLine();
        return Id;
    }

    String welcome_system(String name) throws IOException {
        String input = new String();
        do {
            System.out.println("Welcome, " + name);
            System.out.println("    Main Menu");
            System.out.println("Please enter a number (1-4)");
            System.out.println("1. Vote");
            System.out.println("2. My vote history");
            System.out.println("3. Election result");
            System.out.println("4. Quit");
            System.out.print("make your choice: ");
            input = this.voter_input.readLine();

            if (!input.equals("1") && !input.equals("2") && !input.equals("3") && !input.equals("4")){
                System.out.println("Wrong choice!");
            }
        }while(!input.equals("1") && !input.equals("2") && !input.equals("3") && !input.equals("4"));
        return input;
    }

    String voting_system() throws IOException {
        String input = new String();
        do{
            System.out.println("Welcome voter!");
            System.out.println("1. Tim");
            System.out.println("2. Linda");
            System.out.print("make your choice: ");
            input = this.voter_input.readLine();
        }while (!input.equals("1") && !input.equals("2"));
        if(input.equals("1")){
            input = "1";
        }else
        {
            input = "2";
        }
        return input;
    }

    SealedObject encrypt(Key key,String plain_text) throws InvalidKeyException, IOException, IllegalBlockSizeException {

        this.cipher.init(Cipher.ENCRYPT_MODE,key);
        SealedObject zipbag = new SealedObject(plain_text,this.cipher);
        return  zipbag;
    }

    private KeyPair genvoterkey() throws IOException, NoSuchAlgorithmException {
        KeyPairGenerator keycreater = KeyPairGenerator.getInstance("RSA");
        keycreater.initialize(2048);
        KeyPair key = keycreater.generateKeyPair();
        FileOutputStream voterpubkey = new FileOutputStream("voter_pub.key");
        voterpubkey.write(key.getPublic().getEncoded());
        voterpubkey.close();
        FileOutputStream voterprikey = new FileOutputStream("voter_pri.key");
        voterprikey.write(key.getPrivate().getEncoded());
        voterprikey.close();
        return key;
    }

    public KeyPair receivevoterkey() throws NoSuchAlgorithmException, IOException, InvalidKeySpecException {
        try {
            KeyFactory keyscreater = KeyFactory.getInstance("RSA");
            FileInputStream voterpubkey = new FileInputStream("voter_pub.key");
            byte[] bytes_pub = new byte[voterpubkey.available()];
            voterpubkey.read(bytes_pub);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(bytes_pub);
            PublicKey key_pub = keyscreater.generatePublic(spec);
            FileInputStream voterprikey = new FileInputStream("voter_pri.key");
            byte[] bytes_pri = new byte[voterprikey.available()];
            voterprikey.read(bytes_pri);
            voterprikey.close();
            PKCS8EncodedKeySpec spec_i = new PKCS8EncodedKeySpec(bytes_pri);
            PrivateKey key_pri = keyscreater.generatePrivate(spec_i);
            return new KeyPair(key_pub, key_pri);
        } catch (FileNotFoundException error) {
            return this.genvoterkey();
        }
    }

}
