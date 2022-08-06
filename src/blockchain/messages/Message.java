package blockchain.messages;

import blockchain.Blockchain;
import blockchain.utility.GenerateKeys;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;

public class Message implements Serializable {
    private final static long serialVersionUID = 100L;

    private String message;
    private int identifier;
    private byte[] signature;
    private transient PrivateKey privateKey;
    private PublicKey publicKey;
    private transient Cipher cipher;


    public Message(String message) {
        this.message = message;
        try {
            this.cipher = Cipher.getInstance("RSA");
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            e.printStackTrace();
        }
        encodeMessage(message);
    }


    private void encodeMessage(String message) {
        try {
            GenerateKeys gk;
            gk = new GenerateKeys(1024);
            gk.createKeys();
            PrivateKey privateKey = gk.getPrivateKey();
            publicKey = gk.getPublicKey();
            cipher.init(Cipher.ENCRYPT_MODE, privateKey);
            identifier = Blockchain.getBlockchain().getAndIncreaseIdentifier();
            String stringForEncode = identifier + " " + message;
            signature = cipher.doFinal(stringForEncode.getBytes(StandardCharsets.UTF_8));
        } catch (
                GeneralSecurityException e) {
            e.printStackTrace();
        }
    }

    public String getMessage() {
        return message;
    }

    public int getIdentifier() {
        return identifier;
    }

    public byte[] getSignature() {
        return signature;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    @Override
    public String toString() {
        return "Message{" +
                "message='" + message + '\'' +
                ", identifier=" + identifier +
                '}';
    }
}
