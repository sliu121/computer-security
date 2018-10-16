package client;

import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SealedObject;
import java.io.*;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

class cli_func {
    private Cipher cipher;
    private BufferedReader voter_input;


    static void Exception_Operation(Exception exception, String message) {
        System.err.println(exception.getMessage());
        System.err.println(message);
        exception.printStackTrace();
        System.exit(1);
    }

    cli_func() {
        try {
            this.cipher = Cipher.getInstance("RSA");
            this.voter_input = new BufferedReader(new InputStreamReader(System.in));
        } catch (NoSuchAlgorithmException wrong_cipher) {
            Exception_Operation(wrong_cipher, "RSA not exist");
        } catch (NoSuchPaddingException bad_transmit) {
            Exception_Operation(bad_transmit, "Transformation error");
        }
    }

    String voter_name() {
        String name = new String();
        System.out.print("Pleas input your Name: ");
        try {
            name = this.voter_input.readLine();
        } catch (IOException error) {
            Exception_Operation(error, "error in I/O");
        }

        return name;
    }

    String voter_id() {
        String Id = new String();
        try {
            System.out.print("Please input your ID: ");
            Id = this.voter_input.readLine();
        } catch (IOException error) {
            Exception_Operation(error, "I/O error occurred");
        }
        return Id;
    }

    String voter_system(String name) {
        String input = new String();
        do {
            System.out.println("Welcome, " + name);
            System.out.println("    Main Menu");
            System.out.println("Please enter a number (1-4)");
            System.out.println("1. Vote");
            System.out.println("2. My vote history");
            System.out.println("3. Election result");
            System.out.println("4. Quit");
            System.out.print(": ");
            try {
                input = this.voter_input.readLine();
            } catch (IOException error) {
                Exception_Operation(error, "error in I/O");
            }
            if (input.length() != 1) {
                System.out.println("Invalid action, must be 1, 2, 3, or 4");
            }
        }
        while (!input.equals("1") || !input.equals("2") || !input.equals("3") || !input.equals("4"));
        return input;
    }

    String voting_system() {
        String voting_input = new String();
        do {
            System.out.println("Please enter a number (1-2)");
            System.out.println("1. Tim");
            System.out.println("2. Linda");
            System.out.print("Enter action here: ");
            try {
                voting_input = this.voter_input.readLine();
            } catch (IOException error) {
                Exception_Operation(error, "error in I/O");
            }
        }
        while (!voting_input.equals("1") || !voting_input.equals("2"));
        if (voting_input.equals("1")) {
            voting_input = "1";
            return voting_input;
        } else {
            voting_input = "2";
            return voting_input;
        }
    }

    SealedObject encrypt(Key key, String plain_text) {
        try {
            this.cipher.init(Cipher.ENCRYPT_MODE, key);
            return new SealedObject(plain_text, this.cipher);
        } catch (InvalidKeyException error) {
            Exception_Operation(error, "Wrong key");
        } catch (IllegalBlockSizeException error) {
            Exception_Operation(error, "I");
        } catch (IOException error) {
            Exception_Operation(error, "error in I/O");
        }
        return null;
    }

    private KeyPair createvoterkey() {
        try {
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
        } catch (NoSuchAlgorithmException error) {
            Exception_Operation(error, "NoSuchAlgorithm");
        } catch (InvalidParameterException error) {
            Exception_Operation(error, "InvalidParameter");
        } catch (FileNotFoundException error) {
            Exception_Operation(error, "FileNotFound");
        } catch (IOException error) {
            Exception_Operation(error, "error in I/O");
        }
        return null;

    }

    public KeyPair receivevoterkey() throws NoSuchAlgorithmException {
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
            return this.createvoterkey();
        } catch (IOException error) {
            Exception_Operation(error, "error in I/O");
        } catch (InvalidKeySpecException error) {
            Exception_Operation(error, "InvalidKeySpec");
        }
        return null;
    }

}