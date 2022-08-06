package blockchain;

import blockchain.messages.Message;
import blockchain.utility.StringUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Block implements Serializable {
    private final static long serialVersionUID = 30L;
    private static int difficulty = 0;
    private static String stringOfZeros = "";
    private long timeStamp;
    private String lastHash;
    private String createdByMiner;
    private List<Message> messages;


    private String hash;

    private int blockId;

    private int magicNumber;

    private long timeForGeneration;

    public Block(String lastHash, int id, List<Message> messages,String createdByMinerNumber) {
        this.createdByMiner =createdByMinerNumber;
        this.messages = messages;
        this.lastHash = lastHash;
        blockId = id;
        String messageString = messageString();
        createHash(createdByMinerNumber + blockId + timeStamp + messageString);
    }

    private String messageString() {
        StringBuilder sb = new StringBuilder();
        for (Message s : messages) {
            sb.append(s.getMessage());
        }
        return sb.toString();
    }

    public static void setDifficulty(int difficulty) {
        String zero = "0";
        Block.difficulty = difficulty;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < difficulty; i++) {
            sb.append(zero);
        }
        Block.stringOfZeros = sb.toString();
    }

    private void createHash(String s) {
        Random random = new Random();
        String hash = "";
        int magicNumber;
        long start = System.currentTimeMillis();
        do {
            magicNumber = random.nextInt(Integer.MAX_VALUE);
            hash = StringUtil.applySha256(s + magicNumber);
        } while (!hash.startsWith(stringOfZeros));
        timeForGeneration = (System.currentTimeMillis() - start) / 1000;
        timeStamp = System.currentTimeMillis();
        this.hash = hash;
        this.magicNumber = magicNumber;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public String getLastHash() {
        return lastHash;
    }

    public int getBlockId() {
        return blockId;
    }

    public String getHash() {
        return hash;
    }

    public int getMagicNumber() {
        return magicNumber;
    }

    public long getTimeForGeneration() {
        return timeForGeneration;
    }

    public static int getDifficulty() {
        return difficulty;
    }

    public static String getStringOfZeros() {
        return stringOfZeros;
    }

    public String getCreatedByMiner() {
        return createdByMiner;
    }

    public List<Message> getMessages() {
        return new ArrayList<>(messages);
    }

    @Override
    public String toString() {
        return "Block{" +
                "timeStamp=" + timeStamp +
                ", lastHash='" + lastHash + '\'' +
                ", createdByMinerNumber='" + createdByMiner + '\'' +
                ", messages=" + messages +
                ", hash='" + hash + '\'' +
                ", blockId=" + blockId +
                ", magicNumber=" + magicNumber +
                ", timeForGeneration=" + timeForGeneration +
                '}';
    }
}