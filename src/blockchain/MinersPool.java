package blockchain;

import blockchain.messages.Message;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MinersPool {
    private List<Miner> minersList = new ArrayList<>();
    private int numberOfMiners;
    private Block lastBlock;
    private Blockchain blockchain;
    private List<Message> messagesForNewBlock;

    public MinersPool(int numberOfMiners) {
        this.blockchain = Blockchain.getBlockchain();
        this.numberOfMiners = numberOfMiners;
        blockchain.fillMinersMoneyList(numberOfMiners);
    }

    private void giveTasksToMiners(Block lastBlock) {
        minersList.clear();
        messagesForNewBlock = Collections.synchronizedList(blockchain.getMessageListForNewBlock());
//        System.out.println(messagesForNewBlock);
//        System.out.println(lastBlock);
        for (int i = 0; i < numberOfMiners; i++) {
            minersList.add(new Miner(lastBlock, messagesForNewBlock));
        }
    }

    public void createSomeBlocks(int number) {
        for (int i = 0; i < number; i++) {
            lastBlock = blockchain.getLastBlock();
            giveTasksToMiners(lastBlock);
            createNewBlock();
        }
    }


    private void createNewBlock() {
        Block newBlock=null;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfMiners);
        try {
            newBlock = executorService.invokeAny(minersList);
            executorService.shutdownNow();
            try {
                if (!executorService.awaitTermination(100, TimeUnit.MILLISECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
               if(!blockchain.submitNewBlock(newBlock)){
                   createNewBlock();
               }
        }
    }
}
