package blockchain.messages;

public class MessageGenerator {
    private Message messageObj;

    public Message getMessage(long timeInterval) {
        String message = new Transaction().toString();
        try {
            Thread.sleep(timeInterval);
        } catch (InterruptedException e) {
            //e.printStackTrace();
        } finally {
            messageObj = new Message(message);
            return messageObj;
        }
    }

}
