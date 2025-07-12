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

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * This class represents the chain itself in the Blockchain environment. This class is already adjusted for concurrent access.
 * 
 * @author A. Benquerer @ IPT
 * @author D. Larangeira @ IPT
 */
public class BlockChain implements Serializable {

    /**
     * Thread-safe ArrayList that represents the chain.
     */
    CopyOnWriteArrayList<Block> chain;

    /**
     * Constructor for a blank chain.
     */
    public BlockChain() {
        chain = new CopyOnWriteArrayList<>();
    }
    
    /**
     * Constructor that first tries to load a chain from a given file and if it can't, creates a blank one.
     * 
     * @param fileName file name/path to check.
     */
    public BlockChain(String fileName) {
        try {
            //try to load from given file
            load(fileName);
        } catch (Exception e) {
            //if the loading fails, create blank chain.
            chain = new CopyOnWriteArrayList<>();
        }
    }

    /**
     * Getter for the hash of the last block in the chain.
     *
     * @return last block's hash.
     */
    public String getLastBlockHash() {
        //return genessis block hash
        if (chain.isEmpty()) {
            return String.format("%08d", 0);
        }
        //hash of the last block in the list
        return chain.get(chain.size() - 1).currentHash;
    }
     /**
     * Getter for the last block in the chain.
     *
     * @return last block in the chain.
     */
    public Block getLastBlock() {
        //return null (Genessis block) if the chain is empty
        if (chain.isEmpty()) {
            return null;
        }
        //return the last block in the chain
        return chain.get(chain.size() - 1);
    }

    /**
     * Method for adding a new block to the Blockchain.
     * 
     * @param newBlock block to be added.
     * @throws Exception 
     */
    public void add(Block newBlock) throws Exception {
        //
        if (chain.contains(newBlock)) {
            throw new Exception("Duplicated Block");
        }

        //verify if the block is valid
        if (!newBlock.isValid()) {
            throw new Exception("Invalid Block");
        }
        //verify if the block "connects" with the current last
        if (getLastBlockHash().compareTo(newBlock.previousHash) != 0) {
            throw new Exception("Previous hash not combine");
        }
        //add new block to the chain
        chain.add(newBlock);
    }

    /**
     * Getter for a block in a specific index.
     * 
     * @param index index to get.
     * @return block at given index.
     */
    public Block get(int index) {
        return chain.get(index);
    }
    
    /**
     * Getter for the chain's size.
     * 
     * @return chain's size.
     */
    public int getSize() {
        return chain.size();
    }

    /**
     * Method to get a string representation of the Blockchain.
     * 
     * @return String representation of the chain.
     */
    public String toString() {
        StringBuilder txt = new StringBuilder();
        txt.append("Blochain size = " + chain.size() + "\n");
        for (Block block : chain) {
            txt.append(block.toString() + "\n");
        }
        return txt.toString();
    }
    
    /**
     * Getter for the Blockchain itself.
     * 
     * @return the chain. 
     */
    public List<Block> getChain() {
        return chain;
    }
    
    /**
     * Method for saving the Blockchain in a file, using a given name/path.
     * 
     * @param fileName name of the file / path of the file.
     * @throws Exception 
     */
    public void save(String fileName) throws Exception {
        //create a outstream
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(fileName))) {
            //write chain to file
            out.writeObject(chain);
        }
    }

    /**
     * Method for loading a Blockchain from a file.
     * 
     * @param fileName file name/path.
     * @throws Exception 
     */
    public void load(String fileName) throws Exception {
        //load the chain from the file
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(fileName))) {
            this.chain = (CopyOnWriteArrayList<Block>) in.readObject();
        }
    }

    /**
     * Method for checking if a Blockchain is valid.
     * 
     * @return true if all the blocks and connections are valid, false otherwise.
     */
    public boolean isValid() {
        //check each block's validity
        for (Block block : chain) {
            if (!block.isValid()) {
                //return false if any block is not valid.
                return false;
            }
        }
        //validate each link
        for (int i = 1; i < chain.size(); i++) {
            //previous hash !=  hash of previous
            if (chain.get(i).previousHash.compareTo(chain.get(i - 1).currentHash) != 0) {
                return false;
            }
        }
        return true;
    }

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    private static final long serialVersionUID = 202208221009L;
    //:::::::::::::::::::::::::::  Copyright(c) M@nso  2022  :::::::::::::::::::
    ///////////////////////////////////////////////////////////////////////////
}
