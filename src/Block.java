import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.Serializable;

// Implementing a Block
public class Block implements Serializable{
    private static String hash;
    private static String previousHash;
    private static String data;
    private static long timeStamp; // The timestamp of the creation of this block
    private static int nonce; // An arbitrary number used in cryptography.

    public Block(String data, String previousHash)  {
        this.data = data;
        this.previousHash = previousHash;
        this.timeStamp = new Date().getTime();
        this.hash = calculateBlockHash();
    }

    // Calculating the Hash
    public static String calculateBlockHash() {
        // Concatenate different parts of the block to generate a hash form
        String dataToHash = previousHash + Long.toString(timeStamp)
                + Integer.toString(nonce) + data;
        MessageDigest digest = null;
        byte[] bytes = null;

        try {
            // Get an instance of the SHA-256 hash function from MessageDigest
            digest = MessageDigest.getInstance("SHA-256");
            // Generate the hash value of our input data, which is a byte array
            bytes = digest.digest(dataToHash.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException ex) {
            Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
            logger.log(Level.SEVERE, ex.getMessage());
        }

        // Transform the byte array into a hex string, a hash is typically represented as a 32
        // digit hex number
        StringBuffer buffer = new StringBuffer();
        for (byte b : bytes) {
            buffer.append(String.format("%02x",b));
        }
        return buffer.toString();
    }

    // Mining a Block
    public static String mineBlock(int prefix) {
        // Define the prefix we desire to find
        String prefixString = new String(new char[prefix]).replace('\0', '0');
        // Check if we've find the solution
        while (!hash.substring(0,prefix).equals(prefixString)) {
            // If not, increment the nonce and calculate the hash in a loop
            nonce++;
            hash = calculateBlockHash();
        }
        return hash;
    }

    public static Boolean isChainValid() {
        LinkedList<Block> blockchain = new LinkedList<>();
        Block currentBlock;
        Block previousBlock;

        for (int i= 1; i < blockchain.getSize(); i++) {
            // Storing the current block and the previous block
            currentBlock = blockchain.get(i);
            previousBlock = blockchain.get(i - 1);

            // Checking if the current hash is equal to the calculated hash or not
            if (!currentBlock.hash.equals(currentBlock.calculateBlockHash())) {
                System.out.println("Hashes are not equal");
                return false;
            }

            // Checking of the previous hash is equal to the calculated previous hash or not
            if (!previousBlock.hash.equals(currentBlock.previousHash)) {
                System.out.println("Previous Hashes are not equal");
                return false;
            }
        }
        // If all hashes are equal to the calculated hashes, then the blockchain is valid
        return true;
    }

    // Create random blockchain, so no need to manually insert data
    public static void createRandomBlockchain(LinkedList<Block> blockchain, int numOfBlocks){
        int randomNum = (int) (Math.random() * numOfBlocks) + 1;
        String previousHash = "0";
        for (int i = 0; i < randomNum; i++) {
            String data = "Block " + i;
            Block newBlock = new Block(data, previousHash);
            blockchain.insertAtHead(newBlock);
            previousHash = newBlock.hash;
        }
    }

    public static void main(String[] args) {
        LinkedList<Block> blockchain = new LinkedList<>();
        createRandomBlockchain(blockchain,10);


        // Adding the data to the Linked List
//        blockchain.insertAtHead(new Block("First block", "0"));
//        blockchain.insertAtHead(new Block("Second block", blockchain.get(blockchain.getSize() - 1).hash));
//        blockchain.insertAtHead(new Block("Third block", blockchain.get(blockchain.getSize() - 1).hash));
//        blockchain.insertAtHead(new Block("Fourth block", blockchain.get(blockchain.getSize() - 1).hash));

        try {
            //input data into file
            File newFile = new File("blockchain_data.bin");
            FileOutputStream fos = new FileOutputStream("blockchain_data.bin");

            ObjectOutputStream oos = new ObjectOutputStream(fos);

            //Read File
            FileInputStream fis = new FileInputStream("blockchain_data.bin");

            ObjectInputStream ois = new ObjectInputStream(fis);

            for (int i = 0; i < blockchain.getSize(); i++) {
                oos.writeObject(mineBlock(i));

//                Object newBlock = (Object) ois.readObject();

                System.out.println("<PUBLIC KEY: MK" + i + ", PRIVATE KEY: " + ois.readObject() + ">");
            }


            oos.close();
            ois.close();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}
