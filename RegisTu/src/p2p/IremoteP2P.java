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
package p2p;

import blockchain.Block;
import blockchain.BlockChain;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.Key;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This interface represents a node in the p2p network.
 * 
 * @author A. Benquerer @ IPT
 * @author D. Larangeira @ IPT
 */
public interface IremoteP2P extends Remote {

    //==========================
    //=============== Networking
    //==========================
     /**
     * Getter for a nodes address.
     * 
     * @return node's address.
     * @throws RemoteException 
     */
    public String getAdress() throws RemoteException;

    /**
     * Method for adding a node to the network.
     * 
     * @param node node to be added.
     * @throws RemoteException 
     */
    public void addNode(IremoteP2P node) throws RemoteException;

    /**
     * Getter for list of known peers of a node.
     *
     * @return List of known network.
     * @throws RemoteException
     */
    public List<IremoteP2P> getNetwork() throws RemoteException;

    //============================
    //=============== Transactions
    //============================
    /**
     * Gets the amount of transactions currently in the network´s "buffer".
     * 
     * @return number of transactions that are not in a block.
     * @throws RemoteException 
     */
    public int getTransactionsSize() throws RemoteException;

    /**
     * Adds a transaction to the network.
     * 
     * @param data transaction to be added.
     * @throws RemoteException 
     */
    public void addTransaction(String data) throws RemoteException;

    /**
     * Getter for the transactions list of a node.
     *
     * @return List of transactions of the referenced node.
     * @throws RemoteException
     */
    public List<String> getTransactions() throws RemoteException;
    
    /**
     * Removes a list of transactions from the network's "buffer".
     * 
     * @param myTransactions Transactions to remove.
     * @throws RemoteException 
     */
    public void removeTransactions(List<String> transactions) throws RemoteException;

    /**
     * Synchronizes the transactions between two nodes.
     * 
     * @param node
     * @throws RemoteException 
     */
    public void synchronizeTransactions(IremoteP2P node) throws RemoteException;

    //=====================
    //=============== Miner
    //=====================
    /**
     * Starts a mining of a given data.
     * 
     * @param msg data to be used in mining.
     * @param zeros difficulty.
     * @throws RemoteException 
     */
    public void startMining(String msg, int zeros) throws RemoteException;

    /**
     * Stops the mining operation.
     * 
     * @param nonce found nonce.
     * @throws RemoteException 
     */
    public void stopMining(int nonce) throws RemoteException;

    /**
     * Checks if the miner of a node is mining.
     * 
     * @return if a mining operation is in progress.
     * @throws RemoteException 
     */
    public boolean isMining() throws RemoteException;

    /**
     * Starts the mining operation.
     * 
     * @param msg data to mine.
     * @param zeros difficulty.
     * @return nonce
     * @throws RemoteException 
     */
    public int mine(String msg, int zeros) throws RemoteException;

    //==========================
    //=============== Blockchain
    //==========================
    /**
     * Method to adding a block into the network.
     * 
     * @param b block to be added.
     * @throws RemoteException 
     */
    public void addBlock(Block b) throws RemoteException;

    /**
     * Gets the sizer of the node's chain.
     * 
     * @return size of the node's chain.
     * @throws RemoteException 
     */
    public int getBlockchainSize() throws RemoteException;

    /**
     * Gets the hash of the last block in the node's chain.
     * 
     * @return hash of last block.
     * @throws RemoteException 
     */
    public String getBlockchainLastHash() throws RemoteException;
    
    /**
     * Getter for a node's Blockchain.
     * 
     * @return the node's chain.
     * @throws RemoteException 
     */
    public BlockChain getBlockchain() throws RemoteException;

    /**
     * Synchronizes the Blockchain between peers of the network.
     * 
     * @throws RemoteException 
     */
    public void synchnonizeBlockchain() throws RemoteException;
    
    /**
     * Get all transactions in a blockchain.
     * 
     * @return all transactions
     * @throws RemoteException 
     */
    public List<String> getBlockchainTransactions() throws RemoteException;
    
    //============================
    //=============== Merkle Trees
    //============================
    /**
     * Synchronizes the known merkle trees between two peers.
     * 
     * @param node node to synchronize with
     * @throws RemoteException 
     */
    public void synchronizeMerkles(IremoteP2P node) throws RemoteException;
    
    /**
     * Gets the set of known merkles of a node.
     * 
     * @return set of known merkles.
     * @throws RemoteException 
     */
    public Set<String> getMerkleList() throws RemoteException;
    
    /**
     * Method for getting the file of a tree. 
     * 
     * @param treeRoot
     * @return the tree file data
     * @throws RemoteException 
     */
    public byte[] getMktFile(String treeRoot) throws RemoteException;
    
    //=====================
    //=============== Users
    //=====================
    /**
     * Method for getting the node's key for communication.
     * 
     * @param pubkey Public key to encrypt the key with.
     * @return encrypted symmetrical key.
     * @throws RemoteException 
     */
    public byte[] getSimKey(Key pubkey) throws RemoteException;
    
    /**
     * Checks if a user exists in the network.
     * 
     * @param username user to check.
     * @return true if the user exists.
     * @throws RemoteException 
     */
    public boolean userExists(String username) throws RemoteException;
    
    /**
     * Method for registering a user in the network.
     * 
     * @param username username to register.
     * @param password encrypted password of the user.
     * @param isRegist registrant property.
     * @return
     * @throws RemoteException 
     */
    public String registerUser(String username, byte[] password, boolean isRegist) throws RemoteException;
    
    /**
     * Method for login routine of a user.
     * 
     * @param username username to login.
     * @param password encrypted password.
     * @return map of user credentials files
     * @throws RemoteException 
     */
    public Map<String, byte[]> loginUser(String username, byte[] password) throws RemoteException;
    
    /**
     * Gets the list of known users.
     * 
     * @return list of known users.
     * @throws RemoteException 
     */
    public Set<String> getUserList() throws RemoteException;
    
    /**
     * Synchronizes users between two nodes.
     * 
     * @param node node to sync with
     * @throws RemoteException 
     */
    public void synchronizeUsers(IremoteP2P node) throws RemoteException;
    
    /**
     * Method for getting a user's files.
     * 
     * @param username user to get files of.
     * @return map of user's files.
     * @throws RemoteException 
     */
    public Map<String, byte[]> getUserFiles(String username) throws RemoteException;
    
    /**
     * Method for getting a user's public key.
     * 
     * @param username username.
     * @return user's public key data.
     * @throws RemoteException 
     */
    public byte[] getUserPub(String username) throws RemoteException;
    
    //===============================
    //=============== Wallet & Search
    //===============================
    /**
     * Method for getting a user's wallet update.
     * 
     * @param username username to update
     * @param merkleSkip merkle trees to skip in search
     * @return
     * @throws RemoteException 
     */
    public Map<String, List<String>> getUserWallet(String username, Set<String> merkleSkip) throws RemoteException;
    
     /**
     * Search all curricula linked to a user.
     * 
     * @param username user to search for.
     * @return list of user's curricula.
     * @throws RemoteException 
     */
    public List<String> searchUserCurricula(String username) throws RemoteException;

}
