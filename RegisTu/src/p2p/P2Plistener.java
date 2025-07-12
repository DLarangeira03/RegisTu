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

import blockchain.BlockChain;
import java.util.List;

/**
 * This interface represents a listener for the remote Object, to facilitate the interaction with the user interface.
  *
 * @author A. Benquerer @ IPT
 * @author D. Larangeira @ IPT
 */
public interface P2Plistener {
    
    /**
     * Method for reporting exceptions.
     * 
     * @param ex Exception that occurred.
     * @param message note about event.
     */
    public void onException(Exception ex, String message);

    /**
     * Method for sending messages to the UI.
     * 
     * @param title title of the message
     * @param message message content.
     */
    public void onMessage(String title, String message);

    /**
     * Method for reporting the start of the remote object.
     * 
     * @param message note about event.
     */
    public void onStartRemote(String message);

    /**
     * Method for reporting connection to another peer.
     * 
     * @param address address of the new peer.
     */
    public void onConect(String address);

    /**
     * Method for reporting a new transaction.
     * 
     * @param transaction transaction added.
     */
    public void onTransaction(String transaction);
    
    /**
     * Method for reporting the occurrence of a repeat transaction.
     * 
     * @param message note about event.
     */
    public void onRepeatTransaction(String message);
    
    /**
     * Method for reporting the removal of transactions.
     * 
     * @param c list of removed transactions.
     */
    public void onRemoveTransactions(List c);
    
    /**
     * Method for reporting the sync of transactions between peers.
     * 
     * @param c 
     */
    public void onSyncTransactions(List c);

    /**
     * Method for reporting the start if mining operation
     * 
     * @param message message associated with event.
     * @param zeros zeros used in the operation.
     */
    public void onStartMining(String message, int zeros);

    /**
     * Method for reporting the stop of the mining operation.
     * 
     * @param message message associated with event.
     * @param nonce zeros used in the operation.
     */
    public void onStopMining(String message, int nonce);

    /**
     * Method for reporting the find of a nonce.
     * 
     * @param message message associated with event.
     * @param nonce nonce that was found.
     */
    public void onNonceFound(String message, int nonce);
    
    /**
     * Method for reporting a update in the Blockchain.
     * 
     * @param b updated Blockchain.
     */
    public void onBlockchainUpdate(BlockChain b);
    
    /**
     * Method for updating the user's list.
     * 
     * @param users list of users.
     */
    public void onUserListUpdate(Object[] users);

}
