package blockchain;

import blockchain.messages.Message;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;

public class Miner implements Callable<Block> {
    private Block prevBlock;
    private List<Message> messages;

    @Override
    public Block call() throws Exception {
        return mineNewBlock();
    }

    public Miner(Block lastBlock, List<Message> messages) {
        this.prevBlock = lastBlock;
        this.messages = messages;
    }

    private Block mineNewBlock() {
        Block block;
        String threadName = Thread.currentThread().getName();
        String minersName="miner"+threadName.substring(threadName.length() - 1);
        if (Objects.equals(prevBlock, null)) {
            block = new Block("0", 1, messages
                    , minersName);
        } else {
            block = new Block(prevBlock.getHash(), prevBlock.getBlockId() + 1
                    , messages, minersName);
        }
        return block;
    }
}
