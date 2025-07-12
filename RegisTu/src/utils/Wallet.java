/*
 * A. Benquerer
 * e-mail: dev.benquerer@gmail.com
 * GitHub: https://github.com/Benquerer
 * 
 * Aluno 24633 @ IPT, Jan 2025.
 * 
 * The code in this file was developed for learning and experimentation purposes.
 * 
 */
package utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.Key;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class implements a Wallet for a user´s curricular events.
 * Each wallet has map of merkle references, each associated with a list of the user's curricula in said tree.
 *
 * @author A. Benquerer @ IPT
 * @author D. Larangeira @ IPT
 */
public class Wallet implements Serializable {

    /**
     * Name of the wallet's owner
     */
    String owner;
    /**
     * Wallet's owner symmetrical key.
     */
    Key simkey;
    /**
     * Map of user curricular events. Each key represents a merkle reference, and the value associated is a list of curricula that can be find in the tree.
     */
    Map<String, List<String>> wallet;

    /**
     * Constructor for an empty wallet.
     * 
     * @param owner wallet's owner name.
     * @param simkey owner's symmetrical key.
     * @throws Exception 
     */
    public Wallet(String owner, Key simkey) throws Exception {
        this.owner = owner;
        this.simkey = simkey;
        ///create empty map
        this.wallet = new HashMap<>();
    }

    /**
     * Method for adding new merkle references to the wallet.
     * 
     * @param curricula Map of new merkle trees and their respective list of curricula.
     */
    public void updateWallet(Map<String, List<String>> curricula) {
        wallet.putAll(curricula);
    }

    /**
     * Getter for a set of known merkle trees of the wallet.
     * 
     * @return all trees that are already registered in the wallet.
     */
    public Set<String> getMerkleReferences() {
        return new HashSet<>(wallet.keySet());
    }

    /**
     * Getter for all the known curricula of a user.
     * 
     * @return all base64 encoded strings for the users curricula.
     */
    public List<String> getAllCurricula() {
        //create empty list
        List<String> allStrings = new ArrayList<>();
        //iterate each known merkle reference
        for (List<String> strings : wallet.values()) {
            //add all strings associated with the merkle
            allStrings.addAll(strings);
        }
        //return list of curricula
        return allStrings;
    }

    /**
     * Method for saving the wallet in a encrypted file. The user's symmetrical key will be used in encryption.
     * 
     * @throws Exception 
     */
    public void saveWallet() throws Exception {
        //serialize the wallet
        byte[] walletData = serializeWallet();
        //encript the serialized data with the sim key
        byte[] encryptedData = SecurityUtils.encrypt(walletData, simkey);
        //save the wallet in the file
        try (FileOutputStream fout = new FileOutputStream(new File(owner + ".wlt"))) {
            fout.write(encryptedData);
            fout.flush();
            fout.close();
        }
    }

    /**
     * Auxiliary method for serializing the wallet.
     * 
     * @return serialized wallet.
     * @throws IOException 
     */
    private byte[] serializeWallet() throws IOException {
        //open stream
        try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream(); ObjectOutputStream objout = new ObjectOutputStream(byteOut)) {
            //write wallet to stream
            objout.writeObject(this);
            //flush stream
            objout.flush();
            //reaturn serialized data
            return byteOut.toByteArray();
        }
    }

    /**
     * Method for loading a user's wallet from a file.
     * 
     * @param ownername Wallet's owner name.
     * @param simkeybytes User's symmetrical key.
     * @return User's wallet.
     * @throws Exception 
     */
    public static Wallet loadWallet(String ownername, byte[] simkeybytes) throws Exception {
        byte[] encryptedData;
        //read data from the user's .wlt file
        try (FileInputStream fin = new FileInputStream(new File(ownername + ".wlt"))) {
            encryptedData = fin.readAllBytes();
        }
        //decrypt the data with the simkey
        byte[] decryptedData = SecurityUtils.decrypt(encryptedData, SecurityUtils.getAESKey(simkeybytes));
        //return the user's wallet
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(decryptedData); ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream)) {
            return (Wallet) objectInputStream.readObject();
        }
    }

    //serial int
    private static final long serialVersionUID = 1L;

}
