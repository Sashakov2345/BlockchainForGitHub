package blockchain.messages;

import blockchain.Blockchain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MessagePool extends Thread {
    private List<Message> messageList;
    private long timeInterval = 1;
    private Thread minersPoolThread;
    private Blockchain blockchain;


    public MessagePool(Thread minersPoolThread) {
        this.blockchain = Blockchain.getBlockchain();
        this.minersPoolThread = minersPoolThread;
        messageList = Collections.synchronizedList(new ArrayList<>());
    }

    @Override
    public void run() {
        getMessages();
    }

    private void getMessages() {
        MessageGenerator messageGenerator = new MessageGenerator();
        while (minersPoolThread.isAlive()) {
            long somebodyHasMoney = blockchain.getMinersMoneyMap().values().stream().filter(v -> v > 0).count();
            if (somebodyHasMoney > 0) {
                if (messageList.size() <= 10) {
                    Message message = messageGenerator.getMessage(timeInterval);
                    messageList.add(message);
                }
            }
        }
    }

    public void update() {
        synchronized (messageList) {
            List<Message> messageListCopy = new ArrayList<>(messageList);
//            System.out.println("messageListCopy: "+messageListCopy);
            blockchain.setMessageListForNewBlock(messageListCopy);
            messageList.clear();
//            messageList.add(new Message(""));
        }
    }
}


