/*
 * A. Benquerer
 * e-mail: dev.benquerer@gmail.com
 * GitHub: https://github.com/Benquerer
 * 
 * Aluno 24633 @ IPT, Dec 2024.
 * 
 * The code in this file was developed for learning and experimentation purposes.
 * 
 */
package blockchain;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * This class implements a "Block" in the Blockchain environment. 
 * 
 * @author A. Benquerer @ IPT
 * @author D. Larangeira @ IPT
 */
public class Block implements Serializable, Comparable<Block> {
    
    /**
     * Hash of the previous block in the chain, used to connect two blocks.
     */
    String previousHash;
    /**
     * Root of the MerkleTree containing the block's transactions.
     */
    String merkleRoot;   
    /**
     * List of transactions contained in the block.
     */
    List<String> transactions; 
    /**
     * Nonce mined for the block (Proof of Work).
     */
    int nonce; 
    /**
     * Hash of the Block.
     */
    String currentHash;
    
    /**
     * Constructor for the Block. This method will create the block and the MerkleTree (which will be saved as a .mkt file in the server).
     * 
     * @param previousHash Hash of the previous block in the chain.
     * @param transactions List of transactions that will compose the block.
     * @throws IOException Error in saving the mkt file.
     */
    public Block(String previousHash, List<String> transactions) throws IOException {
        //set reference to previous block
        this.previousHash = previousHash;
        //set transaction list
        this.transactions = transactions;
        //create mktree with the transactions
        MerkleTree mkt = new MerkleTree(transactions);
        //set the merkle root.
        this.merkleRoot = mkt.getRoot();
        //save merke tree in it's respective file
        mkt.saveToFile(merkleRoot + ".mkt");
    }

    /**
     * Method that will set the nonce for the block. The nonce will be checked here to see if it's valid.
     * This will also set the current hash of the block.
     * 
     * @param nonce Nonce to be set.
     * @param zeros Amount of zeros to verify the nonce with.
     * @throws Exception 
     */
    public void setNonce(int nonce, int zeros) throws Exception {
        //set the nonce
        this.nonce = nonce;
        //calculate and set the hash of the block
        this.currentHash = calculateHash();
        
        //check if the hash is valid
        String prefix = String.format("%0" + zeros + "d", 0);
        if (!currentHash.startsWith(prefix)) {
            throw new Exception(nonce + " not valid Hash=" + currentHash);
        }
        
    }

    /**
     * Getter for the data used in the mining process, to find the valid nonce.
     * 
     * @return data to mine. 
     */
    public String getMinerData() {
        return previousHash + merkleRoot;
    }

    /**
     * Getter for the Root of the block's respective MerkleTree.
     * 
     * @return Merkle root.
     */
    public String getMerkleRoot() {
        return merkleRoot;
    }

    /**
     * Getter for the transaction list of the block.
     * 
     * @return transaction list.
     */
    public List<String> transactions() {
        return transactions;
    }

    /**
     * Getter for the hash of the previous block associated.
     * 
     * @return hash of previous block.
     */
    public String getPreviousHash() {
        return previousHash;
    }

    /**
     * Getter for the block's nonce.
     * 
     * @return block's nonce.
     */
    public int getNonce() {
        return nonce;
    }
    
    /**
     * Method to calculate the block's hash.
     * 
     * @return block's hash.
     */
    public String calculateHash() {
        return Miner.getHash(getMinerData(), nonce);
    }

    /**
     * Getter for the block's hash.
     * 
     * @return block's hash. 
     */
    public String getCurrentHash() {
        return currentHash;
    }

    /**
     * Method to get a string representation of the blocks information.
     * 
     * @return String representation of the block. 
     */
    @Override
    public String toString() {
        return // (isValid() ? "OK\t" : "ERROR\t")+
                String.format("[ %8s", previousHash) + " <- "
                + String.format("%-10s", merkleRoot) + String.format(" %7d ] = ", nonce)
                + String.format("%8s", currentHash);

    }

    /**
     * Method to return a map for the block's information, without the transaction list.
     * 
     * @return Header Map of the block.
     */
    public Map<String,String> getBlockHeader() {
        //create header map
        Map<String,String> header = new HashMap<>();
        //put information in the map
        header.put("prevHash", previousHash);
        header.put("mktRoot", merkleRoot);
        header.put("nonce", String.valueOf(nonce));
        header.put("currentHash", currentHash);
        //return it
        return header;
    }
    
    /**
     * Method to get the block's transactions list.
     * 
     * @return blocks transactions.
     */
    public List getBlockTransactions() {
        return transactions;
    }

    /**
     * Method to check the block's validity, by comparing the blocks current hash with the supposed hash based on it's attributes.
     * 
     * @return true if the block is valid, false otherwise.
     */
    public boolean isValid() {
        return currentHash.equals(calculateHash());
    }

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    private static final long serialVersionUID = 202208220923L;
    //:::::::::::::::::::::::::::  Copyright(c) M@nso  2022  :::::::::::::::::::
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Block other = (Block) obj;
        if (this.nonce != other.nonce) {
            return false;
        }
        if (!Objects.equals(this.previousHash, other.previousHash)) {
            return false;
        }
        if (!Objects.equals(this.merkleRoot, other.merkleRoot)) {
            return false;
        }
        return Objects.equals(this.currentHash, other.currentHash);
    }

    
    @Override
    public int compareTo(Block o) {
        return this.currentHash.compareTo(o.currentHash);
    }

}
