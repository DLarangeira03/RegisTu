/*
 * A. Benquerer
 * e-mail: dev.benquerer@gmail.com
 * GitHub: https://github.com/Benquerer
 * 
 * Aluno 24633 @ IPT, Oct 2024.
 * 
 * The code in this file was developed for learning and experimentation purposes.
 * 
 */
package utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Key;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;

/**
 * This class represents a User in the application
 *
 * @author A. Benquerer @ IPT
 * @author D. Larangeira @ IPT
 */
public class User {

    /**
     * Username of the user.
     */
    private String userName;
    /**
     * The Public Key of the user.
     */
    private PublicKey pubKey;
    /**
     * The Private Key of the user.
     */
    private PrivateKey privKey;
    /**
     * The symmetrical key of the user.
     */
    private Key simKey;

    /**
     * Map for user properties.
     */
    private HashMap<String, String> properties;

    /**
     * Constructor that creates a default user, with the name "Guest". Does not
     * generate Keys.
     */
    public User() {
        this.userName = "Guest";
    }

    /**
     * Constructor that creates a user given a name. Does not generate Keys.
     *
     * @param userName Username to be assigned to the user
     */
    public User(String userName) {
        // the username are always set to lower case
        this.userName = userName.toLowerCase();
        // !! THIS DOES NOT GENERATE ANY KEYS !!
    }

    /**
     * Generates the keys for a user. Does not save the keys.
     *
     * @throws Exception problems generating keys.
     */
    public void generateKeys() throws Exception {
        //create a private/public keypair
        KeyPair kp = SecurityUtils.generateECKeyPair(256);
        //assign them to the user
        this.privKey = kp.getPrivate();
        this.pubKey = kp.getPublic();
        //create a symmetrical key
        this.simKey = SecurityUtils.generateAESKey(256);
        //user properties
        this.properties = new HashMap<String, String>();
    }
    
    /**
     * Method for setting a user registrant property as true or false.
     * The property can be accessed with the key "isRegistrant".
     * 
     * @param isReg value to set in properties.
     */
    public void setRegistrant(boolean isReg){
        if(isReg){
            properties.put("isRegistrant", "true");
        }else{
            properties.put("isRegistrant", "false");
        }
        
    }
    
    /**
     * Method for checking if a user is set as registrant.
     * 
     * @return true if the "isRegistrant" key is true, false otherwise
     */
    public boolean isRegistrant(){
        if(properties.containsKey("isRegistrant")){
            return properties.get("isRegistrant").equals("true");
        }
        return false;
    }

    /**
     * Saves user's credentials and properties in the their designated files. Private and
     * Symmetrical keys are encrypted using a password. ".pubk" is used for
     * public keys, ".privk" is used for private keys and ".simk" is used for
     * symmetrical keys. User properties are also saved, but are not encrypted.
     *
     * @param pwd password used for encryption.
     * @throws Exception Problems when writing to files.
     */
    public void save(String pwd) throws Exception {
        //define the folder name
        String folderName = "users/" + userName;
        Path folderPath = Paths.get(folderName);
        //create the folder if it doesn't exist
        if (!Files.exists(folderPath)) {
            Files.createDirectories(folderPath);
        }

        //save the public key in a .pubk file
        Files.write(folderPath.resolve(userName + ".pubk"), pubKey.getEncoded());

        //encrypt the private key using the password
        byte[] secretPriv = SecurityUtils.encrypt(privKey.getEncoded(), pwd);
        //save to a .privk file
        Files.write(folderPath.resolve(userName + ".privk"), secretPriv);

        //encrypt the symmetrical key using the password
        byte[] secretSim = SecurityUtils.encrypt(simKey.getEncoded(), pwd);
        //save to a .simk file
        Files.write(folderPath.resolve(userName + ".simk"), secretSim);

        //serialize the properties (HashMap)
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream)) {
            objectOutputStream.writeObject(properties);  // Serialize the HashMap
        }
        //save to file
        byte[] serializedProps = byteArrayOutputStream.toByteArray();
        Files.write(folderPath.resolve(userName + ".props"), serializedProps);

    }

    /**
     * Loads the user's credentials and properties saved in files. Password needed for the
     * decryption of ".privk" and ".simk" files.
     *
     * @param pwd Password for the decryption of private and symmetrical keys.
     * @throws Exception Problems when reading data from files.
     */
    public void load(String pwd) throws IOException, Exception {
        //define the folder name
        String folderName = "users/" + userName;
        Path folderPath = Paths.get(folderName);

        //read the data from the public key file
        byte[] pubData = Files.readAllBytes(folderPath.resolve(userName + ".pubk"));
        //get the key from the data and assign it to the user
        this.pubKey = SecurityUtils.getPublicKey(pubData);

        //read the data from the private key file
        byte[] privData = Files.readAllBytes(folderPath.resolve(userName + ".privk"));
        //decrypt it using the password, get a private key, and assign it to the user
        this.privKey = SecurityUtils.getPrivateKey(SecurityUtils.decrypt(privData, pwd));

        //read the data from the symmetrical key file
        byte[] simData = Files.readAllBytes(folderPath.resolve(userName + ".simk"));
        //decrypt it using the password, get a symmetrical key, and assign it to the user
        this.simKey = SecurityUtils.getAESKey(SecurityUtils.decrypt(simData, pwd));

        //read the data from the props file
        byte[] serializedProps = Files.readAllBytes(folderPath.resolve(userName + ".props"));
        //deserialize the byte array back into a HashMap
        try (ObjectInputStream objectInputStream = new ObjectInputStream(new ByteArrayInputStream(serializedProps))) {
            this.properties = (HashMap<String, String>) objectInputStream.readObject();
        }

    }

    /**
     * Loads only the public key of an User.
     *
     * @throws Exception problems reading the file
     */
    public void loadPub() throws IOException, Exception {
        //read the data from the public key file
        byte[] pubData = Files.readAllBytes(Path.of(this.userName + ".pubk"));
        //get the key form the data and assign it to the user
        this.pubKey = SecurityUtils.getPublicKey(pubData);
    }

    /**
     * Gets the name of the user.
     *
     * @return the user's name
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Sets the name of the user.
     *
     * @param name User's name.
     */
    public void setUserName(String name) {
        this.userName = name;
    }

    /**
     * Gets the public key of the user.
     *
     * @return user's public key .
     */
    public PublicKey getPubKey() {
        return pubKey;
    }

    /**
     * Sets the public key of the user.
     *
     * @param pubKey user's public key.
     */
    public void setPubKey(PublicKey pubKey) {
        this.pubKey = pubKey;
    }

    /**
     * Gets the private key of the user.
     *
     * @return user's private key.
     */
    public PrivateKey getPrivKey() {
        return privKey;
    }

    /**
     * Sets the private key of the user.
     *
     * @param privKey user's private key.
     */
    public void setPrivKey(PrivateKey privKey) {
        this.privKey = privKey;
    }

    /**
     * Gets the symmetric key of the user.
     *
     * @return user's symmetric.
     */
    public Key getSimKey() {
        return simKey;
    }

    /**
     * Sets the symmetric key of the user.
     *
     * @param simKey user's symmetric key.
     */
    public void setSimKey(Key simKey) {
        this.simKey = simKey;
    }

    /**
     * Getter for the user properties.
     * 
     * @return user properties.
     */
    public HashMap<String, String> getProperties() {
        return properties;
    }

    /**
     * Setter for the user properties.
     * 
     * @param properties properties to use
     */
    public void setProperties(HashMap<String, String> properties) {
        this.properties = properties;
    }

    /**
     * Setter for a specific property.
     * 
     * @param key key associated.
     * @param value value for property.
     */
    public void setProperty(String key, String value) {
        this.properties.put(key, value);
    }

    /**
     * Getter for a specific property.
     * 
     * @param key key to access the property.
     * @return property value.
     */
    public String getProperty(String key) {
        return this.properties.get(key);
    }

}
