package blockchain;

import blockchain.messages.Message;
import blockchain.messages.MessagePool;
import blockchain.messages.Transaction;
import blockchain.utility.SerializationUtils;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class Blockchain implements Serializable {
    private static final Blockchain blockchain = new Blockchain();
    private static int tryToReadFile=0;
    private final static long serialVersionUID = 4L;
//    private static final String filename = "Blockchain/task/blockchain.data";
    private static final String filename = "blockchain.data";
    private transient MessagePool messagePool;
    private transient List<Message> messageListForNewBlock = new ArrayList<>();
    private LinkedBlockingDeque<Block> chain = new LinkedBlockingDeque<>();
    private transient Map<String, Integer> minersMoneyMap = new ConcurrentHashMap<>();

    public static Blockchain getBlockchain() {
        try {
            if(tryToReadFile==0) {
                Blockchain blockchainFromFile = (Blockchain) SerializationUtils.deserialize(filename);
                blockchain.chain = blockchainFromFile.chain;
                counter.set(blockchain.chain.getLast().getMessages()
                        .stream()
                        .mapToInt(m -> m.getIdentifier())
                        .reduce(1, Integer::max));
            }
        } catch (IOException | ClassNotFoundException e) {
//            System.out.println("No file");
        } finally {
            tryToReadFile++;
            return blockchain;
        }
    }

    public void fillMinersMoneyList(int NumberOfMiners) {
        for (int i = 0; i < NumberOfMiners; i++) {
            minersMoneyMap.put("miner" + i, 0);
        }
    }


    private Blockchain() {
        super();
    }

    private void saveBlockchainToFile() {
        try {
            SerializationUtils.serialize(this, filename);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void printBlockInfo(Block block) {
        System.out.print(String.format("Block:\n" +
                        "Created by: %s\n" +
                        "%s gets 100 VC\n" +
                        "Id: %d\n" +
                        "Timestamp: %d\n" +
                        "Magic number: %d\n" +
                        "Hash of the previous block: \n" +
                        "%s\n" +
                        "Hash of the block: \n" +
                        "%s\n", block.getCreatedByMiner(), block.getCreatedByMiner(),
                block.getBlockId(), block.getTimeStamp(), block.getMagicNumber(),
                block.getLastHash(), block.getHash()));
        System.out.println("Block data: ");
        for (Message s : block.getMessages()) {
            System.out.println(s.getMessage());
        }
        System.out.println(String.format("Block was generating for %d seconds", block.getTimeForGeneration()));
    }

    private boolean checkForZeros(Block block) {
        if (block.getHash().startsWith(Block.getStringOfZeros())) {
            return true;
        }
        System.out.println("invalid zeros");
        return false;
    }

    private Transaction parseTransaction(String s) {
        String[] array = s.split(" ");
        if (array.length < 6) {
            return new Transaction("", 0, "", false);
        }
        String from = array[0];
        String to = array[array.length - 1];
        Integer amount = 0;
        for (int i = 1; i < array.length - 1; i++) {
            try {
                amount = Integer.parseInt(array[i]);
                break;
            } catch (NumberFormatException e) {
                continue;
            }
        }
        return new Transaction(from, amount, to, true);
    }

    private boolean checkTransaction(String message) {
        Transaction transaction = parseTransaction(message);
        if (transaction.isExist()) {
            String from = transaction.getFrom();
            String to = transaction.getTo();
            Integer amount = transaction.getAmount();
            if (minersMoneyMap.containsKey(from) && minersMoneyMap.containsKey(to)) {
                Integer currentAmountFrom = minersMoneyMap.get(from);
                Integer currentAmountTo = minersMoneyMap.get(to);
//                System.out.println(String.format("From:%s, to:%s", from, to));
//                System.out.println(String.format("Current:%d, amount:%d", currentAmountFrom, amount));
                if (currentAmountFrom >= amount) {
                    minersMoneyMap.replace(from, currentAmountFrom - amount);
                    minersMoneyMap.replace(to, currentAmountTo + amount);
                    return true;
                }
            }
        }
        return false;
    }

    private boolean checkMessages(Block prevBlock, Block newBlock) {
        int maxIdentifier = prevBlock.getMessages()
                .stream().mapToInt(m -> m.getIdentifier())
                .reduce(1, Integer::max);
        long count = newBlock.getMessages()
                .stream()
                .mapToInt(m -> m.getIdentifier())
                .filter(i -> i < maxIdentifier)
                .count();
        return count > 0 ? false : true;
    }


    public boolean validateChain() {
        if (chain.isEmpty()) {
            return false;
        }
        if (chain.size() == 1) {
            if (chain.getLast().getLastHash() == "0") {
//                System.out.println("Blockchain is valid");
                return true;
            }
        } else {
            ArrayList<Block> blocks = new ArrayList<>(chain);
            Block prevBlock = chain.getFirst();
            for (int i = 1; i < chain.size(); i++) {
                Block block = blocks.get(i);
                if (!checkMessages(prevBlock, block)) {
                    System.out.println("invalid check message");
                    return false;
                }
                prevBlock = block;
            }
            LinkedList<Block> chainCopy = new LinkedList<>(chain);
            Iterator<Block> iterator = chainCopy.descendingIterator();
            String prevHash = chainCopy.getLast().getLastHash();
            iterator.next();
            while (iterator.hasNext()) {
                Block block = iterator.next();
                if (!Objects.equals(prevHash, block.getHash())) {
                    System.out.println("invalid hash from previous");
                    return false;
                }
                prevHash = block.getLastHash();
            }
        }
//        System.out.println("Blockchain is valid");
        return true;

    }


    public void startMessagePool() {
        messagePool = new MessagePool(Thread.currentThread());
        messagePool.setName("messagePoolThread");
        messagePool.start();
    }

    public List<Message> getMessageListForNewBlock() {
        Message noMessage = new Message("No transactions");
        getMessagesFromPool();
        if (messageListForNewBlock.isEmpty()) {
            messageListForNewBlock.add(noMessage);
        }
        return messageListForNewBlock;
    }

    public void setMessageListForNewBlock(List<Message> messageListForNewBlock) {
        if (!chain.isEmpty()) {
            Block lastBlock = chain.getLast();
            int maxIdentifier = lastBlock.getMessages()
                    .stream().mapToInt(m -> m.getIdentifier())
                    .reduce(1, Integer::max);
//            System.out.println("maxIdentifier:" + maxIdentifier);
//            System.out.println("messageListForNewBlock Before: " + messageListForNewBlock);
            messageListForNewBlock = messageListForNewBlock.stream()
                    .filter(m -> m.getIdentifier() > maxIdentifier)
                    .filter(m -> checkTransaction(m.getMessage()))
                    .collect(Collectors.toList());
//            System.out.println("messageListForNewBlock After: " + messageListForNewBlock);
            this.messageListForNewBlock = messageListForNewBlock;
        }
    }


    public Block getLastBlock() {
//        System.out.println(chain);
        return chain.peekLast();
    }

    public boolean submitNewBlock(Block newBlock) {
        //  chain.addLast(newBlock);
        chain.addLast(newBlock);
        //chain.stream().forEach(System.out::println);
        if (!checkForZeros(newBlock) || !validateChain()) {
            chain.removeLast();
            System.out.println("invalid block");
            return false;
        } else {
            payToMiner(newBlock);
            printBlockInfo(newBlock);
            saveBlockchainToFile();
            evaluateDifficulty();
            return true;
        }
    }

    private void payToMiner(Block block) {
        String miner = block.getCreatedByMiner();
        if (minersMoneyMap.containsKey(miner)) {
            Integer oldAmount = minersMoneyMap.get(miner);
            minersMoneyMap.replace(miner, oldAmount + 100);
        }
    }

    private void getMessagesFromPool() {
        messagePool.update();
    }

    private void evaluateDifficulty() {
        int currentDifficulty = Block.getDifficulty();
        double timeForGeneration = chain.getLast().getTimeForGeneration();
        if (timeForGeneration > 1 && timeForGeneration < 3 || currentDifficulty >= 3) {
            System.out.println("N stays the same\n");
            return;
        }
        if (timeForGeneration <= 1) {
            currentDifficulty += 1;
            Block.setDifficulty(currentDifficulty);
            System.out.println(String.format("N was increased to %d\n", currentDifficulty));
            return;
        }
        if (timeForGeneration >= 3) {
            currentDifficulty -= 1;
            Block.setDifficulty(currentDifficulty);
            System.out.println("N was decreased by 1\n");
            return;
        }
    }

    public Map<String, Integer> getMinersMoneyMap() {
        return minersMoneyMap;
    }

    private static final AtomicInteger counter = new AtomicInteger(0);
    public int getAndIncreaseIdentifier() {
        return counter.incrementAndGet();
    }

}
