package blockchain.messages;

import blockchain.Blockchain;

import java.util.*;

public class Transaction {
    private Map<String, Integer> minersMoneyMap = Blockchain.getBlockchain().getMinersMoneyMap();
    private String from;
    private Integer amount;
    private String to;
    private boolean isExist = false;

    public Transaction() {
        createTransaction();
    }

    public Transaction(String from, Integer amount, String to, boolean isExist) {
        this.from = from;
        this.amount = amount;
        this.to = to;
        this.isExist = isExist;
    }

    private void createTransaction() {
        Map<String, Integer> haveMoney = new HashMap<>();
        for (Map.Entry<String, Integer> entry : minersMoneyMap.entrySet()) {
            if (entry.getValue() > 0) {
                haveMoney.put(entry.getKey(), entry.getValue());
            }
        }
        if (haveMoney.isEmpty()) {
            return;
        }
        Random random = new Random();
        ArrayList<String> keySetWithMoney = new ArrayList<>(haveMoney.keySet());
        ArrayList<String> keySetOfAllMiners = new ArrayList<>(minersMoneyMap.keySet());
        int fromWho = random.nextInt(keySetWithMoney.size());
        from = keySetWithMoney.get(fromWho);
        int toWho;
        do {
            toWho = random.nextInt(keySetOfAllMiners.size());
            to = keySetOfAllMiners.get(toWho);
        }
        while (Objects.equals(from,to));
        amount = random.nextInt(minersMoneyMap.get(from))+1;
        isExist=true;
    }

    public String getFrom() {
        return from;
    }

    public Integer getAmount() {
        return amount;
    }

    public String getTo() {
        return to;
    }

    @Override
    public String toString() {
        return isExist?String.format("%s sent %d VC to %s", from, amount, to):"Empty pockets";

    }

    public boolean isExist() {
        return isExist;
    }
}
