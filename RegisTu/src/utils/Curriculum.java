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
import java.io.Serializable;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;

/**
 * This class represents a Curriculum, indicating its owner, content and the certification entity.
 * 
 * @author A. Benquerer
 * @author Diogo Larangeira
 */
public class Curriculum implements Serializable{

    //these 4 attributes are the "usefull" ones
    /**
     * Public key of the registrant entity
     */
    private String pubKeyRegistrant;
    /**
     * Public key of the curriculum owner
     */
    private String pubKeyOwner;
    /**
     * Curriculum information (Description)
     */
    private String desc;
    /**
     * Digital signature of the curriculum
     */
    private String signature; 
    
    //these other 2 attributes are used for diplaying purposes
    /**
     * Name of the owner of the curriculum
     */
    private String name;
    /**
     * Name of the Registrant entity
     */
    private String registrant; 
 
    /**
     * Constructor for a Curriculum. 
     * Sets all the attributes of the curriculum using the 2 received users and the description.
     * 
     * @param registrant User that represents the registrant of the curriculum.
     * @param ownerName Name of the user receiving the curriculum
     * @param ownerPub Public key of the user receiving the curriculum
     * @param desc Description of the curriculum.
     * @throws Exception If the signing of the curriculum throws any exceptions.
     */
    public Curriculum(User registrant, String ownerName, byte[] ownerPub, String desc) throws Exception{
        //set the owner name
        this.name = ownerName;
        //set the registrant name
        this.registrant = registrant.getUserName();
        //set the registrat public key
        this.pubKeyRegistrant = Base64.getEncoder().encodeToString(registrant.getPubKey().getEncoded());
        //set the owners public key
        this.pubKeyOwner = Base64.getEncoder().encodeToString(ownerPub);
        //set the description of the curriculum
        this.desc = desc;
        //signs the curriculum with the registrant's private key
        signCurriculum(registrant.getPrivKey());
    }
    
    /**
     * Signs a the curriculum using the registrant's private key and the data.
     * 
     * @param privKey Registrant's private key.
     * @throws Exception if the sign method {@link SecurityUtils#sign(byte[], java.security.PrivateKey)} throws any Exception.
     */
    private void signCurriculum(PrivateKey privKey) throws Exception {
        //get a digital signature using the Curriculum data and the registrant's private key
        byte[] sign = SecurityUtils.sign((pubKeyRegistrant + pubKeyOwner + desc).getBytes(), privKey);
        //sets the generated digital signature as the curriculum's signature
        this.signature = Base64.getEncoder().encodeToString(sign);
    }
   
    /**
     * Check if the Curriculum is valid.
     * Uses the data from Curriculum and it's digital signature to assert if the curriculum is authentic. 
     * 
     * @return {@code true} if the curriculum is valid, {@code false} otherwise.
     */
    public boolean IsValid(){
        try {
            //get the publickey of the registrant entity that is set in the curriculum
            PublicKey pubKey = SecurityUtils.getPublicKey(Base64.getDecoder().decode(pubKeyRegistrant));
            //get the digital signature that is expected to be valid
            byte[] sign = Base64.getDecoder().decode(signature); 
            //get the data from the curriculum, in the same exact way it would have been used for signing
            byte[] data = (pubKeyRegistrant + pubKeyOwner + desc).getBytes();
            //return if the digital signature is valid
            return SecurityUtils.verifySign(data, sign, pubKey);            
        } catch (Exception e) {
            //if any exception occur, than the curriculum isn't valid
            return false;
        }
    }
   
    /**
     * Creates an unsafe curriculum (without the usage of users, signature, etc).
     * Incompatible with the User structure.
     *
     * @param n name of the owner
     * @param d description of the curriculum
     * @param e registrant name
     * 
     * @deprecated Not safe for the User structure. Use {@link Curriculum#Curriculum(curriculum.utils.User, curriculum.utils.User, java.lang.String) } instead.
     */
    @Deprecated
    public Curriculum(String n, String d, String e) {
        this.name = n;
        this.desc = d;
        this.registrant = e;
    }
    
    /**
     * Constructor for a default Curriculum.
     * 
     * @deprecated Not safe for the User structure. Use {@link Curriculum#Curriculum(curriculum.utils.User, curriculum.utils.User, java.lang.String) } instead.
     */
    @Deprecated
    public Curriculum() {
        this("Nome", "Descrição", "Entidade");
    }
    
    /**
     * Copy Constructor, used to create a curriculum from another.
     * 
     * @param c Curriculum to copy.
     * @deprecated Not safe for the User structure. Use {@link Curriculum#Curriculum(curriculum.utils.User, curriculum.utils.User, java.lang.String) } instead.
     */
    @Deprecated
    public Curriculum(Curriculum c){
        this(c.name,c.desc,c.registrant);
    }
    
    /**
     * Returns the text representation of a curriculum, without the keys and signature.
     * 
     * @return String representation of a curriculum.
     */
    @Override
    public String toString() {
        // return a txt version of the curriculum
        return "Certifier: " + registrant;
    }
    
    /**
     * Returns a byte[] that represents a Curriculum.
     * 
     * @return The byte[] of the serialized Curriculum.
     * @throws IOException IO problems with the streams.
     */
    public byte[] toByteArr() throws IOException{
        //create the streams used to serialize the Curriculum
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        ObjectOutputStream outStream = new ObjectOutputStream(byteStream);
        //writes the curriculum in the stream
        outStream.writeObject(this);
        outStream.flush();
        //returns the byte[] form of the curriculum
        return byteStream.toByteArray();
    }
    
    /**
     * Gets a curriculum from a byte[].
     * 
     * @param byteArr byte[] representation of the curriculum.
     * @return a Curriculum object that was read from the byte[].
     * @throws IOException problems with the streams.
     * @throws ClassNotFoundException Cant cast object to curriculum.
     */
    public static Curriculum fromByteArr(byte[] byteArr) throws IOException, ClassNotFoundException{
        //open streams with the byte array
        ByteArrayInputStream byteStream = new ByteArrayInputStream(byteArr);
        ObjectInputStream inStream = new ObjectInputStream(byteStream);
        //read the curriculum in the stream and return
        return (Curriculum) inStream.readObject();
    }

    /**
     * Gets the public key of the registrant of the curriculum.
     * 
     * @return registrant's public key.
     */
    public String getPubKeyEnt() {
        return pubKeyRegistrant;
    }
    
    /**
     * Sets the public key of the registrant.
     * @param pubKeyEnt registrant's public key.
     */
    public void setPubKeyEnt(String pubKeyEnt) {
        this.pubKeyRegistrant = pubKeyEnt;
    }
    
    /**
     * Gets the public key of the owner of the curriculum
     * @return owner's public key
     */
    public String getPubKeyOwner() {
        return pubKeyOwner;
    }

    /**
     * Sets the public key of the owner
     * 
     * @param pubKeyOwner owner's public key
     */
    public void setPubKeyOwner(String pubKeyOwner) {
        this.pubKeyOwner = pubKeyOwner;
    }

    /**
     * Gets the description of a curriculum.
     * 
     * @return the description.
     */
    public String getDesc() {
        return desc;
    }
    
    /**
     * Sets de description of a curriculum
     * 
     * @param desc description.
     */
    public void setDesc(String desc) {
        this.desc = desc;
    }
    
    /**
     * Gets the digital signature.
     * 
     * @return the digital signature.
     */
    public String getSignature() {
        return signature;
    }
    
    /**
     * sets the digital signature
     * 
     * @param signature digital signature.
     */
    public void setSignature(String signature) {
        this.signature = signature;
    }

    /**
     * Gets the name of the owner
     * 
     * @return name of the owner
     */    
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the owner
     * 
     * @param name name of the owner
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * Gets the name of the registrant
     * 
     * @return name of the registrant
     */
    public String getEntidade() {
        return registrant;
    }
    
    /**
     * Sets the name of the registrant
     * 
     * @param entidade name of the registrant
     */
    public void setEntidade(String entidade) {
        this.registrant = entidade;
    }
    
    private static final long serialVersionUID = 1L;
  
}
