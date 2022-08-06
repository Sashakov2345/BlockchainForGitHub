package blockchain;


public class Main {
    public static void main(String[] args) {
        MinersPool minersPool=new MinersPool(7);
        Blockchain.getBlockchain().startMessagePool();
        minersPool.createSomeBlocks(15);
    }
}
