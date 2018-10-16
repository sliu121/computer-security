import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class RSAEncrypt {


    public static RSAPublicKey loadPublicKeyByFile(String file) throws Exception {
        try {
            File f = new File(file);
            FileInputStream fis = new FileInputStream(f);
            DataInputStream dis = new DataInputStream(fis);
            byte[] keyBytes = new byte[(int) f.length()];
            dis.readFully(keyBytes);
            dis.close();

            String temp = new String(keyBytes);
            String publicKeyPEM = temp.replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s", "");
            byte [] decoded = Base64.getDecoder().decode(publicKeyPEM);
            X509EncodedKeySpec spec =
                    new X509EncodedKeySpec(decoded);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return (RSAPublicKey)kf.generatePublic(spec);
        } catch (IOException e) {
            throw new Exception("input error");
        } catch (NullPointerException e) {
            throw new Exception("input is null");
        }
    }
    public static RSAPrivateKey loadPrivateKeyByFile(String file) throws Exception {
        try {
            File f = new File(file);
            FileInputStream fis = new FileInputStream(f);
            DataInputStream dis = new DataInputStream(fis);
            byte[] keyBytes = new byte[(int) f.length()];
            dis.readFully(keyBytes);
            dis.close();
            String temp = new String(keyBytes);
            String privKeyPEM = temp.replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s", "");
            byte [] decoded = Base64.getDecoder().decode(privKeyPEM);
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decoded);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return (RSAPrivateKey)kf.generatePrivate(spec);
        } catch (IOException e) {
            throw new Exception("input error");
        } catch (NullPointerException e) {
            throw new Exception("input is null");
        }
    }
    //拆分byte数组
    public static byte[][] splitBytes(byte[] bytes, int splitLength){
        int x; //商，数据拆分的组数，余数不为0时+1
        int y; //余数
        y=bytes.length%splitLength;
        if(y!=0){
            x=bytes.length/splitLength+1;
        }else{
            x=bytes.length/splitLength;
        }
        byte[][] arrays=new byte[x][];
        byte[] array;
        for(int i=0; i<x; i++){
            if(i==x-1 && bytes.length%splitLength!=0){
                array=new byte[bytes.length%splitLength];
                System.arraycopy(bytes, i*splitLength, array, 0, bytes.length%splitLength);
            }else{
                array=new byte[splitLength];
                System.arraycopy(bytes, i*splitLength, array, 0, splitLength);
            }
            arrays[i]=array;
        }
        return arrays;
    }
    //byte数组转十六进制字符串
    public static String bytesToHexString(byte[] bytes) {
        StringBuffer sb = new StringBuffer(bytes.length);
        String sTemp;
        for (int i = 0; i < bytes.length; i++) {
            sTemp = Integer.toHexString(0xFF & bytes[i]);
            if (sTemp.length() < 2)
                sb.append(0);
            sb.append(sTemp.toUpperCase());
        }
        return sb.toString();
    }

    //十六进制字符串转byte数组
    public static byte[] hexString2Bytes(String hex) {
        int len = (hex.length() / 2);
        hex=hex.toUpperCase();
        byte[] result = new byte[len];
        char[] achar = hex.toCharArray();
        for (int i = 0; i < len; i++) {
            int pos = i * 2;
            result[i] = (byte) (toByte(achar[pos]) << 4 | toByte(achar[pos + 1]));
        }
        return result;
    }

    private static byte toByte(char c) {
        byte b = (byte) "0123456789ABCDEF".indexOf(c);
        return b;
    }

    public static String encrypt(RSAPublicKey publicKey, String plainText)
            throws Exception {
        Cipher cipher=Cipher.getInstance("RSA");//java默认"RSA"="RSA/ECB/PKCS1Padding"
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);

        int splitLength=(publicKey).getModulus().bitLength()/8-11;
        byte[][] arrays=splitBytes(plainText.getBytes(), splitLength);
        StringBuffer sb=new StringBuffer();
        for(byte[] array : arrays){
            sb.append(bytesToHexString(cipher.doFinal(array)));
        }
        return sb.toString();
    }
    public static String signature(RSAPrivateKey privateKey, String plainText)
            throws Exception {
        Cipher cipher=Cipher.getInstance("RSA");//java默认"RSA"="RSA/ECB/PKCS1Padding"
        cipher.init(Cipher.ENCRYPT_MODE, privateKey);

        int splitLength=(privateKey).getModulus().bitLength()/8-11;
        byte[][] arrays=splitBytes(plainText.getBytes(), splitLength);
        StringBuffer sb=new StringBuffer();
        for(byte[] array : arrays){
            sb.append(bytesToHexString(cipher.doFinal(array)));
        }
        return sb.toString();
    }

    public static String decrypt(RSAPrivateKey privateKey, String cipherText)
            throws Exception {
        Cipher cipher=Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);

        int splitLength=(privateKey).getModulus().bitLength()/8;
        byte[] contentBytes=hexString2Bytes(cipherText);
        byte[][] arrays=splitBytes(contentBytes, splitLength);
        StringBuffer sb=new StringBuffer();
        for(byte[] array : arrays){
            sb.append(new String(cipher.doFinal(array)));
        }
        return sb.toString();
    }

    public static String verifySignature(RSAPublicKey publicKey, String cipherText)
            throws Exception {
        Cipher cipher=Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, publicKey);

        int splitLength=(publicKey).getModulus().bitLength()/8;
        byte[] contentBytes=hexString2Bytes(cipherText);
        byte[][] arrays=splitBytes(contentBytes, splitLength);
        StringBuffer sb=new StringBuffer();
        for(byte[] array : arrays){
            sb.append(new String(cipher.doFinal(array)));
        }
        return sb.toString();
    }

}
