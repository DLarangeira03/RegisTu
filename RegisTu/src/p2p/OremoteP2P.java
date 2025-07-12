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
import blockchain.Miner;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.Key;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import utils.Curriculum;
import utils.RMI;
import utils.SecurityUtils;
import utils.User;
import utils.app_params;

/**
 * This class represents the implementation of a remote object that operates as a server in the Blockchain environment.
 * Each remote object represents a node in a p2p network.
 * 
 * @author A. Benquerer @ IPT
 * @author D. Larangeira @ IPT
 */
public class OremoteP2P extends UnicastRemoteObject implements IremoteP2P {

    /**
     * Address of the remote object.
     */
    String address;
    
    /**
     * Thread-safe ArrayList of known peers (Network).
     */
    CopyOnWriteArrayList<IremoteP2P> network;
    
    /**
     * Thread-safe ArraySet of transactions
     */
    CopyOnWriteArraySet<String> transactions;
    /**
     * Listener for communication with UI.
     */
    P2Plistener p2pListener;
    
    /**
     * Miner object of the node.
     */
    Miner myMiner;
    
    /**
     * Thread-safe Blockchain.
     */
    BlockChain myBlockchain;
    
    /**
     * Node's symmetrical key.
     */
    Key simkey;
    
    /**
     * Thread-safe Array set of known users.
     */
    CopyOnWriteArraySet<String> knownUsers;
    
    /**
     * Thread-safe Array set of known trees.
     */
    CopyOnWriteArraySet<String> knownMktrees;

    /**
     * Constructor of the remote object.
     * 
     * @param address address of the remote object.
     * @param listener listener to communicate with UI.
     * @throws RemoteException
     * @throws Exception 
     */
    public OremoteP2P(String address, P2Plistener listener) throws RemoteException, Exception {
        super(RMI.getAdressPort(address));
        //define address
        this.address = address;
        //create new empty network
        this.network = new CopyOnWriteArrayList<>();
        //create new empty transactions list (buffer)
        transactions = new CopyOnWriteArraySet<>();
        //create new miner for the object
        this.myMiner = new Miner(listener);
        //create or load the Blockchain
        this.myBlockchain = new BlockChain(app_params.BCHAIN_NAME);
        //associate the listener
        this.p2pListener = listener;
        //generate the symmetrical key
        this.simkey = SecurityUtils.generateAESKey(256);
        //create new empty list of users
        this.knownUsers = new CopyOnWriteArraySet<>();
        //create new empty list of trees
        this.knownMktrees = new CopyOnWriteArraySet<>();

        //alert the UI and update the chain
        listener.onStartRemote("Internal Server Started: \t" + address + " | Listening...\n");
        listener.onBlockchainUpdate(myBlockchain);

        //thread to load all known users into the set
        new Thread(() -> {
            //specify the path to the "Users" directory
            File usersDirectory = new File("users");

            //get the list of directories in the "Users" directory
            File[] userFolders = usersDirectory.listFiles();
            //check if the directory exists and is a directory
            if (userFolders != null) {
                for (File userFolder : userFolders) {
                    //check if the current file is a directory
                    if (userFolder.isDirectory()) {
                        //add to knownUsers set
                        knownUsers.add(userFolder.getName());
                    }
                }
                //update list on UI
                p2pListener.onUserListUpdate(knownUsers.toArray());
            }
        }).start();
        
        
        //thread to load the known trees into set
        new Thread(() -> {
            //specify the path to the "Users" directory
            File treesDir = new File("mktrees");

            //get the list of directories in the "Users" directory
            File[] treeFiles = treesDir.listFiles();
            //check if the directory exists and is a directory
            if (treeFiles != null) {
                for (File treeFile : treeFiles) {
                    //check if the current file is a directory
                    if (treeFile.isFile()) {
                        //add to knownUsers map
                        knownMktrees.add(treeFile.getName().substring(0, treeFile.getName().lastIndexOf(".")));
                    }
                }
                //debug
                p2pListener.onMessage("known trees = ", knownMktrees.toString());
            }
        }).start();

    }

    //==========================
    //=============== Networking
    //==========================
    /**
     * Getter for a nodes address.
     * 
     * @return node's address.
     * @throws RemoteException 
     */
    @Override
    public String getAdress() throws RemoteException {
        return address;
    }

    /**
     * Method for verifying if a node is in the network, and eliminate disconnected ones.
     * 
     * @param adress node's address.
     * @return true if the node is in the network.
     */
    private boolean isInNetwork(String adress) {
        //fazer o acesso iterado pelo fim do array para remover os nos inativos
        //iterate the network
        for (int i = network.size() - 1; i >= 0; i--) {
            //try talking to the node
            try {
                //if it answers, and the addresses match, the searched node is in the network
                if (network.get(i).getAdress().equals(adress)) {
                    return true;
                }
            } catch (RemoteException ex) {
                //remove nodes that dont's respond.
                network.remove(i);
            }
        }
        return false;
    }
    
    /**
     * Method for adding a node to the network.
     * 
     * @param node node to be added.
     * @throws RemoteException 
     */
    @Override
    public void addNode(IremoteP2P node) throws RemoteException {
        //if the node is already in the network, skip.
        if (isInNetwork(node.getAdress())) {
            return;
        }
        //notify the UI
        p2pListener.onMessage("Adding new Peer ", node.getAdress() + "\n");
        //add new peer
        network.add(node);
        //notify the UI
        p2pListener.onConect(node.getAdress());
        //add this node to the new peer
        node.addNode(this);
        //relay the new node to the network
        for (IremoteP2P iremoteP2P : network) {
            iremoteP2P.addNode(node);
        }

        //sync all the necessary stuff
        synchronizeTransactions(node);
        synchnonizeBlockchain();
        this.synchronizeUsers(node);
        this.synchronizeMerkles(node);

    }

    /**
     * Getter for list of known peers of a node.
     *
     * @return List of known network.
     * @throws RemoteException
     */
    @Override
    public List<IremoteP2P> getNetwork() throws RemoteException {
        return new ArrayList<>(network);
    }

    //============================
    //=============== Transactions
    //============================
    /**
     * Gets the amount of transactions currently in the network´s "buffer".
     * 
     * @return number of transactions that are not in a block.
     * @throws RemoteException 
     */
    public int getTransactionsSize() throws RemoteException {
        return transactions.size();
    }

    /**
     * Adds a transaction to the network.
     * 
     * @param data transaction to be added.
     * @throws RemoteException 
     */
    public void addTransaction(String data) throws RemoteException {
        //if the local node already has the transaction, does not add anything
        if (transactions.contains(data)) {
            p2pListener.onRepeatTransaction(data);
            //return
            return;
        }
        //add the transaction to local node
        transactions.add(data);
        //alert listener of new transaction
        p2pListener.onTransaction(data);
        //relay the transaction to peers
        for (IremoteP2P iremoteP2P : network) {
            iremoteP2P.addTransaction(data);
        }
        //thread to add a new bloc
        new Thread(() -> {
            try {
                //create the block
                List<String> blockTransactions = this.getTransactions();
                if (blockTransactions.size() != 4) {
                    return;
                }
                Block b = new Block(myBlockchain.getLastBlockHash(), blockTransactions);
                //remove transactions used in the block
                this.removeTransactions(blockTransactions);
                //mine the nonce for the block
                int nonce = this.mine(b.getMinerData(), app_params.BCHAIN_DIFFICULTY);
                //set the found nonce
                b.setNonce(nonce, app_params.BCHAIN_DIFFICULTY);
                //add block to chain
                this.addBlock(b);
                //add block's tree as known merkle
                knownMktrees.add(b.getMerkleRoot());

            } catch (Exception ex) {
                p2pListener.onException(ex, "creating block error");
            }
        }).start();
    }

    /**
     * Getter for the transactions list of a node.
     *
     * @return List of transactions of the referenced node.
     * @throws RemoteException
     */
    @Override
    public List<String> getTransactions() throws RemoteException {
        return new ArrayList<>(transactions);
    }

    /**
     * Synchronizes the transactions between two nodes.
     * 
     * @param node
     * @throws RemoteException 
     */
    @Override
    public void synchronizeTransactions(IremoteP2P node) throws RemoteException {
        //current transactions
        int currentSize = transactions.size();
        p2pListener.onMessage("sinchronizeTransactions", node.getAdress() + "\n");
        //merge transactions
        this.transactions.addAll(node.getTransactions());
        int newSize = transactions.size();
        //if the size went up
        if (currentSize < newSize) {
            p2pListener.onMessage("sinchronizeTransactions", "tamanho diferente");
            //ask the peer to sync
            node.synchronizeTransactions(this);
            p2pListener.onMessage("sinchronizeTransactions", "node.sinchronizeTransactions(this)");
            //relay sync to network
            for (IremoteP2P iremoteP2P : network) {
                //if the size of a peer is smaller, send transactions
                if (iremoteP2P.getTransactionsSize() < newSize) {
                    p2pListener.onMessage("sinchronizeTransactions", " iremoteP2P.sinchronizeTransactions(this)");
                    //sync with this node
                    iremoteP2P.synchronizeTransactions(this);
                }
            }
        }
        p2pListener.onSyncTransactions(new ArrayList(transactions));
    }

    /**
     * Removes a list of transactions from the network's "buffer".
     * 
     * @param myTransactions Transactions to remove.
     * @throws RemoteException 
     */
    @Override
    public void removeTransactions(List<String> myTransactions) throws RemoteException {
        //remove given list from current transactions
        transactions.removeAll(myTransactions);
        //notify UI
        p2pListener.onRemoveTransactions(myTransactions);
        //relay the removal
        for (IremoteP2P iremoteP2P : network) {
            if (iremoteP2P.getTransactions().retainAll(transactions)) {
                //remove transactions
                iremoteP2P.removeTransactions(myTransactions);
            }
        }

    }

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
    @Override
    public void startMining(String msg, int zeros) throws RemoteException {
        try {
            //start mining in this node's miner
            myMiner.startMining(msg, zeros);
            //notify UI
            p2pListener.onStartMining(msg, zeros);
            //relay the startmining to network
            for (IremoteP2P iremoteP2P : network) {
                //send only to nodes not mining
                if (!iremoteP2P.isMining()) {
                    //notify UI
                    p2pListener.onStartMining(iremoteP2P.getAdress() + " mining", zeros);
                    //start mining in peer node
                    iremoteP2P.startMining(msg, zeros);
                }
            }
        } catch (Exception ex) {
            p2pListener.onException(ex, "startMining");
        }

    }

    /**
     * Stops the mining operation.
     * 
     * @param nonce found nonce.
     * @throws RemoteException 
     */
    @Override
    public void stopMining(int nonce) throws RemoteException {
        //stop this miner
        myMiner.stopMining(nonce);
        //relay the stop to the network
        for (IremoteP2P iremoteP2P : network) {
            //stop only those that are mining  
            if (iremoteP2P.isMining()) {
                //stop mining in peer node
                iremoteP2P.stopMining(nonce);
            }
        }
    }
    
    /**
     * Starts the mining operation.
     * 
     * @param msg data to mine.
     * @param zeros difficulty.
     * @return nonce
     * @throws RemoteException 
     */
    @Override
    public int mine(String msg, int zeros) throws RemoteException {
        try {
            //start mining
            startMining(msg, zeros);
            //wait for nonce and return it
            return myMiner.waitToNonce();
        } catch (InterruptedException ex) {
            p2pListener.onException(ex, "Mine");
            return -1;
        }

    }

    /**
     * Checks if the miner of a node is mining.
     * 
     * @return if a mining operation is in progress.
     * @throws RemoteException 
     */
    @Override
    public boolean isMining() throws RemoteException {
        return myMiner.isMining();
    }
    
    //==========================
    //=============== Blockchain
    //==========================
    /**
     * Method to adding a block into the network.
     * 
     * @param b block to be added.
     * @throws RemoteException 
     */
    @Override
    public void addBlock(Block b) throws RemoteException {
        try {
            //exit if the block is not valid
            if (!b.isValid()) {
                throw new RemoteException("invalid block");
            }
            //if the block fits add it to the chain
            if (myBlockchain.getLastBlockHash().equals(b.getPreviousHash())) {
                myBlockchain.add(b);
                //save the blockchain
                myBlockchain.save(app_params.BCHAIN_NAME);
                p2pListener.onBlockchainUpdate(myBlockchain);
            }
            //relay the block to network
            for (IremoteP2P iremoteP2P : network) {
                //if it fits in the peer's chain
                if (!iremoteP2P.getBlockchainLastHash().equals(b.getPreviousHash())
                        || //or the peer chain is smaller
                        iremoteP2P.getBlockchainSize() < myBlockchain.getSize()) {
                    //adds block to peer
                    iremoteP2P.addBlock(b);
                }
            }
            //if does not fit, sync the chains
            if (!myBlockchain.getLastBlockHash().equals(b.getCurrentHash())) {
                synchnonizeBlockchain();
            }
        } catch (Exception ex) {
            p2pListener.onException(ex, "Add bloco " + b);
        }
    }

    /**
     * Gets the sizer of the node's chain.
     * 
     * @return size of the node's chain.
     * @throws RemoteException 
     */
    @Override
    public int getBlockchainSize() throws RemoteException {
        return myBlockchain.getSize();
    }

    /**
     * Gets the hash of the last block in the node's chain.
     * 
     * @return hash of last block.
     * @throws RemoteException 
     */
    @Override
    public String getBlockchainLastHash() throws RemoteException {
        return myBlockchain.getLastBlockHash();
    }

    /**
     * Getter for a node's Blockchain.
     * 
     * @return the node's chain.
     * @throws RemoteException 
     */
    @Override
    public BlockChain getBlockchain() throws RemoteException {
        return myBlockchain;
    }

    /**
     * Synchronizes the Blockchain between peers of the network.
     * 
     * @throws RemoteException 
     */
    @Override
    public void synchnonizeBlockchain() throws RemoteException {
        //sync with the whole network
        for (IremoteP2P iremoteP2P : network) {
            //if the chain is bigger in peer
            if (iremoteP2P.getBlockchainSize() > myBlockchain.getSize()) {
                BlockChain remote = iremoteP2P.getBlockchain();
                //check if the peer chain is valid
                if (remote.isValid()) {
                    //update this node's blockchain
                    myBlockchain = remote;
                    //notify UI
                    p2pListener.onBlockchainUpdate(myBlockchain);
                }
            }
        }
    }

    /**
     * Get all transactions in a blockchain.
     * 
     * @return all transactions
     * @throws RemoteException 
     */
    @Override
    public List<String> getBlockchainTransactions() throws RemoteException {
        ArrayList<String> allTransactions = new ArrayList<>();
        for (Block b : myBlockchain.getChain()) {
            allTransactions.addAll(b.transactions());
        }
        return allTransactions;
    }

    //============================
    //=============== Merkle Trees
    //============================
    /**
     * Gets the set of known merkles of a node.
     * 
     * @return set of known merkles.
     * @throws RemoteException 
     */
    public Set<String> getMerkleList() throws RemoteException {
        return knownMktrees;
    }

    /**
     * Synchronizes the known merkle trees between two peers.
     * 
     * @param node node to synchronize with
     * @throws RemoteException 
     */
    @Override
    public void synchronizeMerkles(IremoteP2P node) throws RemoteException {
        //if the peer is empty, this node does not need to recieve nothing
        if (node.getMerkleList().isEmpty()) {
            //sync the peer with this
            node.synchronizeUsers(this);
            return;
        }
        //if this node has everything from peer, check if the peer has all from this node
        if (this.getMerkleList().containsAll(node.getMerkleList())) {
            //if peer contains all from this node, the lists are already equal, finish sync
            if (node.getMerkleList().containsAll(this.getMerkleList())) {
                return;
            } else {
                //if the peer does not have all from this node, sync peer with this node
                node.synchronizeMerkles(this);
                return;
            }
        }
        //list of merkles to get
        Set<String> missingTrees = new HashSet<>();
        //check all merkles from peer
        for (String tree : node.getMerkleList()) {
            //if this node does not have a tree known by the peer, add it to missing
            if (!knownMktrees.contains(tree)) {
                missingTrees.add(tree);
            }
        }
        //if there are trees missing, get all of them
        if (!missingTrees.isEmpty()) {
            //btye[] to recieve
            byte[] missingFile;
            //notify UI
            p2pListener.onMessage("Starting merkle sync", this.getAdress() + " is starting a sync process with " + node.getAdress());
            for (String missingTree : missingTrees) {
                try {
                    //get tree file
                    missingFile = node.getMktFile(missingTree);
                    Path folderPath = Paths.get("mktrees");
                    if (!Files.exists(folderPath)) {
                        Files.createDirectories(folderPath);
                    }
                    //define path for the file
                    Files.write(folderPath.resolve(missingTree + ".mkt"), missingFile);
                    //update knowntrees
                    knownMktrees.add(missingTree);
                } catch (IOException ex) {
                    p2pListener.onMessage("Writing Mktree Files ", "An error occurred while trying to write a mktree file");
                }
            }
            //notifyUI
            p2pListener.onMessage("sync trees", knownMktrees.toString());
        }
    }

    /**
     * Method for getting the file of a tree. 
     * 
     * @param treeRoot
     * @return the tree file data
     * @throws RemoteException 
     */
    @Override
    public byte[] getMktFile(String treeRoot) throws RemoteException {
        try {
            Path folderPath = Paths.get("mktrees");
            return Files.readAllBytes(folderPath.resolve(treeRoot + ".mkt"));
        } catch (IOException ex) {
            p2pListener.onMessage("Sending Mktree Files ", "An error occurred while trying to send a mktree file");
            return null;
        }
    }

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
    @Override
    public byte[] getSimKey(Key pubkey) throws RemoteException {
        try {
            byte[] key = simkey.getEncoded();
            //encrypt sim key with given public key
            return SecurityUtils.encrypt(key, pubkey);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * Checks if a user exists in the network.
     * 
     * @param username user to check.
     * @return true if the user exists.
     * @throws RemoteException 
     */
    @Override
    public boolean userExists(String username) throws RemoteException {
        return knownUsers.contains(username);
    }

    /**
     * Method for registering a user in the network.
     * 
     * @param username username to register.
     * @param password encrypted password of the user.
     * @param isRegist registrant property.
     * @return
     * @throws RemoteException 
     */
    @Override
    public String registerUser(String username, byte[] password, boolean isRegist) throws RemoteException {
        try {
            //create the user
            User newUser = new User(username);
            //generate the keys
            newUser.generateKeys();
            //set given property
            newUser.setRegistrant(isRegist);
            //decrypt user password
            byte[] secretUser = SecurityUtils.decrypt(password, simkey);
            //save user credentials
            newUser.save(new String(secretUser));
            //add user to knownusers
            knownUsers.add(username);
            //notify UI
            p2pListener.onMessage("New User Registered: ", username);
            p2pListener.onUserListUpdate(knownUsers.toArray());
            //relay user to network
            for (IremoteP2P node : network) {
                node.synchronizeUsers(this);
            }
            return "User created successfully!";
        } catch (Exception ex) {
            return "An error occurred while registering the user!\n Please try again later.";
        }
    }

    /**
     * Method for login routine of a user.
     * 
     * @param username username to login.
     * @param password encrypted password.
     * @return map of user credentials files
     * @throws RemoteException 
     */
    @Override
    public Map<String, byte[]> loginUser(String username, byte[] password) throws RemoteException {
        //see if user is registered
        if (knownUsers.contains(username)) {
            try {
                //create the user
                User loggedUser = new User(username);
                //decrypt pass
                byte[] userPass = SecurityUtils.decrypt(password, simkey);
                //try loading credentials with given pass
                loggedUser.load(new String(userPass)); //if it fails, login is not allowed, throws Ex

                //return credentials do client
                //get files
                byte[] userPrivKey = loggedUser.getPrivKey().getEncoded();
                byte[] userPubKey = loggedUser.getPubKey().getEncoded();
                byte[] userSimKey = loggedUser.getSimKey().getEncoded();
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream)) {
                    objectOutputStream.writeObject(loggedUser.getProperties());
                }
                byte[] userProps = byteArrayOutputStream.toByteArray();

                //encrypt files
                byte[] secretPrivKey = SecurityUtils.encrypt(userPrivKey, simkey);
                byte[] secretPubKey = SecurityUtils.encrypt(userPubKey, simkey);
                byte[] secretSimKey = SecurityUtils.encrypt(userSimKey, simkey);
                byte[] secretProps = SecurityUtils.encrypt(userProps, simkey);

                //add all to map
                Map<String, byte[]> encryptedKeys = new HashMap<>();
                encryptedKeys.put("privateKey", secretPrivKey);
                encryptedKeys.put("publicKey", secretPubKey);
                encryptedKeys.put("symmetricKey", secretSimKey);
                encryptedKeys.put("props", secretProps);
                //return o map an notify UI
                p2pListener.onMessage("Sending keys for login: ", username);
                return encryptedKeys;
            } catch (Exception ex) {
                ex.printStackTrace();
                return null;
            }
        }
        return null;
    }

    /**
     * Gets the list of known users.
     * 
     * @return list of known users.
     * @throws RemoteException 
     */
    @Override
    public Set<String> getUserList() throws RemoteException {
        return knownUsers;
    }

    /**
     * Synchronizes users between two nodes.
     * 
     * @param node node to sync with
     * @throws RemoteException 
     */
    @Override
    public void synchronizeUsers(IremoteP2P node) throws RemoteException {
        //if the peer's list is empty, this node does not get nothing
        if (node.getUserList().isEmpty()) {
            //sync with this node
            node.synchronizeUsers(this);
            return;
        }
        //if this node already has all from the peer, check peer list
        if (this.getUserList().containsAll(node.getUserList())) {
            //if peer has everything from this node, lists are equal
            if (node.getUserList().containsAll(this.getUserList())) {
                return;
            } else {
                //the peer need to sync with this node
                node.synchronizeUsers(this);
                return;
            }
        }
        //Set of users missing
        Set<String> missingUsers = new HashSet<>();
        //check all users registered in peer
        for (String user : node.getUserList()) {
            //if theres a user that this node does not have, add it to missing list
            if (!knownUsers.contains(user)) {
                missingUsers.add(user);
            }
        }
        //sync all missing users
        if (!missingUsers.isEmpty()) {
            //get files of missing user
            Map<String, byte[]> missingFiles;
            p2pListener.onMessage("Starting user sync", this.getAdress() + " is starting a sync process with " + node.getAdress());
            for (String missingUser : missingUsers) {
                try {
                    //get user files
                    missingFiles = node.getUserFiles(missingUser);
                    //write files in correct path
                    String folderName = "users/" + missingUser;
                    Path folderPath = Paths.get(folderName);
                    //create the folder if it doesn't exist
                    if (!Files.exists(folderPath)) {
                        Files.createDirectories(folderPath);
                    }
                    //save file
                    byte[] missingPublic = missingFiles.get("publicKey");
                    Files.write(folderPath.resolve(missingUser + ".pubk"), missingPublic);
                    byte[] missingPrivate = missingFiles.get("privateKey");
                    Files.write(folderPath.resolve(missingUser + ".privk"), missingPrivate);
                    byte[] missingSim = missingFiles.get("symmetricKey");
                    Files.write(folderPath.resolve(missingUser + ".simk"), missingSim);
                    byte[] missingProps = missingFiles.get("props");
                    Files.write(folderPath.resolve(missingUser + ".props"), missingProps);
                    //update knownusers
                    knownUsers.add(missingUser);
                } catch (IOException ex) {
                    p2pListener.onMessage("Writing User Files ", "An error occurred while trying to write the user's files");
                }
            }
            p2pListener.onUserListUpdate(knownUsers.toArray());
        }
    }

    /**
     * Method for getting a user's files.
     * 
     * @param username user to get files of.
     * @return map of user's files.
     * @throws RemoteException 
     */
    @Override
    public Map<String, byte[]> getUserFiles(String username) throws RemoteException {
        //user's folder
        String folderName = "users/" + username;
        Path folderPath = Paths.get(folderName);
        //map of file data
        Map<String, byte[]> userFiles = new HashMap<String, byte[]>();
        try {
            //get all data and add to map
            byte[] pubKey = Files.readAllBytes(folderPath.resolve(username + ".pubk"));
            byte[] privKey = Files.readAllBytes(folderPath.resolve(username + ".privk"));
            byte[] simKey = Files.readAllBytes(folderPath.resolve(username + ".simk"));
            byte[] props = Files.readAllBytes(folderPath.resolve(username + ".props"));
            userFiles.put("publicKey", pubKey);
            userFiles.put("privateKey", privKey);
            userFiles.put("symmetricKey", simKey);
            userFiles.put("props", props);
        } catch (IOException ex) {
            p2pListener.onMessage("Getting User Files:", "An error ocurred while loading users files from sender node");
        }
        //return map
        return userFiles;
    }

    /**
     * Method for getting a user's public key.
     * 
     * @param username username.
     * @return user's public key data.
     * @throws RemoteException 
     */
    @Override
    public byte[] getUserPub(String username) throws RemoteException {
        //user's folder
        String folderName = "users/" + username;
        Path folderPath = Paths.get(folderName);
        try {
            //return user public key data
            return Files.readAllBytes(folderPath.resolve(username + ".pubk"));
        } catch (IOException ex) {
            p2pListener.onMessage("Error getUserPub", "An error occurred while sending user public key");
            return null;
        }
    }

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
    @Override
    public Map<String, List<String>> getUserWallet(String username, Set<String> merkleSkip) throws RemoteException {
        //map for the update
        Map<String, List<String>> update = new HashMap<>();
        //iterate the chain
        for (Block b : myBlockchain.getChain()) {
            //skip already known merkles
            if (!merkleSkip.contains(b.getMerkleRoot())) { //does not known a merkle:
                //get merkle root
                String root = b.getMerkleRoot();
                //list of curricula that belongs to the user
                List<String> curricula = new ArrayList<>();
                for (Object transaction : b.getBlockTransactions()) {
                    try {
                        byte[] currBytes = Base64.getDecoder().decode((String) transaction);
                        Curriculum c = Curriculum.fromByteArr(currBytes);
                        if (c.getName().matches(username)) {
                            curricula.add((String) transaction);
                            p2pListener.onMessage("Added transaction to update", username + " ----- " + transaction + "\n");
                        }
                    } catch (Exception ex) {
                        p2pListener.onMessage("Error loading curriculum from transaction = ", transaction + "\n");
                    }
                }
                //add to wallet update
                update.put(root, curricula);
            }
        }
        p2pListener.onMessage("Sending user wallet update: ", username + "\n");
        //return update
        return update;
    }
    
    /**
     * Search all curricula linked to a user.
     * 
     * @param username user to search for.
     * @return list of user's curricula.
     * @throws RemoteException 
     */
    public List<String> searchUserCurricula(String username) throws RemoteException {
        List<String> curricula = new ArrayList<>();
        //iterate the chain
        for (Block b : myBlockchain.getChain()) {
            for (Object transaction : b.getBlockTransactions()) {
                try {
                    byte[] currBytes = Base64.getDecoder().decode((String) transaction);
                    Curriculum c = Curriculum.fromByteArr(currBytes);
                    //check if curriculum belongs to user
                    if (c.getName().matches(username)) {
                        curricula.add((String) transaction);
                        p2pListener.onMessage("Added transaction to list", username + " ----- " + transaction);
                    }
                } catch (Exception ex) {
                    p2pListener.onMessage("Error loading curriculum from transaction = ", transaction + "\n");
                }
            }
        }
        //return results
        return curricula;
    }

}
