package com.jkoolcloud.tnt4j.streams.admin.backend.loginAuth;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.DigestException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.naming.AuthenticationException;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.apache.curator.framework.CuratorFramework;
import org.apache.log4j.Logger;
import org.apache.zookeeper.cli.AclParser;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Id;
import org.apache.zookeeper.server.auth.DigestAuthenticationProvider;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jkoolcloud.tnt4j.streams.admin.backend.utils.PropertyData;
import com.jkoolcloud.tnt4j.streams.admin.backend.zookeeper.ZookeeperAccessService;
import com.jkoolcloud.tnt4j.streams.admin.backend.zookeeper.utils.CuratorUtils;

import cmd.Invoker;
import commands.AddUserCommand;
import commands.RemoveAllAclCommand;

public class UsersUtils {
    private static Logger LOG = Logger.getLogger(UsersUtils.class);
    private static String schema = "digest";
    private static Boolean userRemoval = false;
    private static String SERVICES_REGISTRY_START_NODE;
    private static String usersExcludeList;

    /**
     * Generates a key and an initialization vector with the given key and password.
     *
     * @param key the salt data (8 bytes of data or <code>null</code>)
     * @param password the password data (optional)
     * @param md the message digest algorithm to use
     * @return an two-element array with the generated key and IV
     */
    private static byte[][] GenerateKeyAndIV(byte[] key, byte[] password, MessageDigest md) {
        int digestLength = md.getDigestLength();
        int requiredLength = (32 + 16 + digestLength - 1) / digestLength * digestLength;
        byte[] generatedData = new byte[requiredLength];
        int generatedLength = 0;
        try {
            md.reset();
            // Repeat process until sufficient data has been generated
            while (generatedLength < 32 + 16) {
                // Digest data (last digest if available, password data, salt if available)
                if (generatedLength > 0) {
                    md.update(generatedData, generatedLength - digestLength, digestLength);
                }
                md.update(password);
                if (key != null) {
                    md.update(key, 0, 8);
                }
                md.digest(generatedData, generatedLength, digestLength);
                generatedLength += digestLength;
            }
            // Copy key and IV into separate byte arrays
            byte[][] result = new byte[2][];
            result[0] = Arrays.copyOfRange(generatedData, 0, 32);
            result[1] = Arrays.copyOfRange(generatedData, 32, 32 + 16);

            return result;
        } catch (DigestException e) {
            throw new RuntimeException(e);

        } finally {
            Arrays.fill(generatedData, (byte)0);
        }
    }

    /**
     * Used to hash the user credentials for correct acl
     * @param acls
     */
    private static void hashIds(List<ACL> acls) {
        for (ACL acl : acls) {
            try {
                System.out.println();
                String digest = DigestAuthenticationProvider.generateDigest(acl.getId().getId());
                acl.setId(new Id(schema, digest));
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Used to decrypt the password from login
     * @param encryptedPass
     * @return
     */
    private static String passDecrypt(String encryptedPass){
        String decryptedText = "";
        try {
            String secretKey = PropertyData.getProperty("secretKey");
            byte[] cipherData = Base64.getDecoder().decode(encryptedPass);
            byte[] saltData = Arrays.copyOfRange(cipherData, 8, 16);

            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[][] keyAndIV = GenerateKeyAndIV(saltData, secretKey.getBytes(StandardCharsets.UTF_8), md5);
            SecretKeySpec key = new SecretKeySpec(keyAndIV[0], "AES");
            IvParameterSpec iv = new IvParameterSpec(keyAndIV[1]);

            byte[] encrypted = Arrays.copyOfRange(cipherData, 16, cipherData.length);
            Cipher aesCBC = Cipher.getInstance("AES/CBC/PKCS5Padding");
            aesCBC.init(Cipher.DECRYPT_MODE, key, iv);
            byte[] decryptedData = aesCBC.doFinal(encrypted);
            decryptedText = new String(decryptedData, StandardCharsets.UTF_8);
        } catch (IOException e) {
            LOG.info("Problem on trying to get secret key for password");
            e.printStackTrace();
        }catch (Exception e) {
            LOG.info("Problem on trying to decrypt password");
            e.printStackTrace();
        }
        return decryptedText;
    }

    /**
     * Used to confirm that user was removed
     * @return
     */
    public static Boolean getUserRemoval() {
        return userRemoval;
    }

    /**
     * Check if the user is not in the list to be excluded from all actions and the user list
     * @param username
     * @return
     */
    public Boolean checkTheUserForExclude(String username){
        List<String> userExclude;
        try {
            usersExcludeList = PropertyData.getProperty("usersExcludeList");
            userExclude = Arrays.asList(usersExcludeList.split(";"));

            return checkIfListHasValue(userExclude, username);
        } catch (IOException e) {
            LOG.error("Problem on getting the property to exclude users "+ e);
            return false;
        }
    }

    /**
     * A method that is used to check if a user with the provided username does not yet exist and if it does return true
     * @param username username
     * @param curatorFramework connection to ZooKeeper
     * @return
     */
    private Boolean checkForUserExistence(String username,CuratorFramework curatorFramework){
        try {
            SERVICES_REGISTRY_START_NODE = PropertyData.getProperty("serviceRegistryStartNode");
        } catch (IOException e) {
            LOG.error("Service registry start node not found");
        }
        Collection<String> clusterNodes = CuratorUtils.nodeChildrenList(SERVICES_REGISTRY_START_NODE, curatorFramework);
        for(String node : clusterNodes) {
            try {
                List<ACL> aclList = curatorFramework.getACL().forPath(SERVICES_REGISTRY_START_NODE+"/"+node);
                for (ACL value : aclList) {
                    String userId = value.getId().getId().split(":")[0];
                    if(username.equals(userId)){
                        return true;
                    }
                }
            }catch (Exception e){
                LOG.error("Problem on checking if such username does not exist");
                LOG.error("Stack trace: "+ e);
            }
        }
        return false;
    }

    /**
     * A method to add a new user to ZooKeeper
     * @param clustersAndRights from front end form clusters and rights map
     * @param username the new username
     * @param password the new encrypted password
     * @throws AuthenticationException
     */
    public void addUser(HashMap<String, List> clustersAndRights, String username,
                                String password, boolean decryptPass, boolean admin) throws AuthenticationException {
            String rights = "";
            boolean adminRights = false;
            CuratorFramework curatorFramework;
            LOG.info("password : "+password);
            if(decryptPass) {
                password = passDecrypt(password);
            }
            password = password.substring(1, password.length()-1);
            if(admin) {
                curatorFramework = ZookeeperAccessService.getConnection();
            }else{
                curatorFramework = ZookeeperAccessService.getConnectionAdmin();
            }
            LOG.info("Trying to add a new user: "+ username);
            if(checkForUserExistence(username, curatorFramework)){
                throw new AuthenticationException("A user with that username already exists");
            }
            Invoker invoker = new Invoker(curatorFramework);
            try {
                for(Map.Entry<String, List> entry  : clustersAndRights.entrySet()) {
                    String key = entry.getKey();
                    List value = entry.getValue();
                    if(value.contains("action")){
                        adminRights = true;
                        value.remove("action");
                    }
                    rights = returnFirstWordCharString(value);
                    String aclDigest = schema+":"+username+":"+password+":"+rights;
                    List<ACL> ACLConstructed = AclParser.parse(aclDigest);
                    hashIds(ACLConstructed);
                    LOG.info("Add a new user to: "+key+". With the ACl set to : "+ACLConstructed+"and the action rights for user set to: " +adminRights);
                    int aclRights = getPermFromString(rights);
                    if(aclRights>0) {
                        invoker.invoke(new AddUserCommand(key, ACLConstructed, adminRights));
                    }
                }
            } catch (Exception e) {
                ZookeeperAccessService.stopConnectionAdmin(curatorFramework);
                e.printStackTrace();
            }
        if(!admin) {
            ZookeeperAccessService.stopConnectionAdmin(curatorFramework);
        }
    }

    /**
     * Preform the login activity using JAAS methods and callback handler
     * @param username
     * @param password
     * @return
     */
    public String loginTheUserByCredentials(String username, String password){
        if(!password.isEmpty()){
            password = passDecrypt(password);
            password = password.substring(1, password.length()-1);
            LOG.info("trying to log in the user: "+ username);
        }
        String loginResponse = "";
        try{
            JaasCalllbackHandler callBack = new JaasCalllbackHandler(username, password);
            LoginContext loginCont = new LoginContext("StreamsAdminLogin", callBack);
            loginCont.login();
        } catch (LoginException e) {
            LOG.info("Problem on log weld-pre loader in one of the users: "+ username);
            e.printStackTrace();
            return "Problem on login of the user: "+ username+". Authentication failed.";
        }catch(Exception e){
            LOG.error("Unexpected error occurred while trying to login user: "+ username);
            e.printStackTrace();
            return "Unexpected error occurred while trying to login user: "+ username+". Authentication failed.";
        }
        return loginResponse;
    }

    /**
     * A method used to remove the existing users from chosen clusters
     * @param pathsToNode path to clusters from which users should be removed
     * @param username the user to remove
     */
    public void removeUser(List<String> pathsToNode, String username, boolean admin){
        LOG.info("Trying to remove a user: "+ username);
        CuratorFramework curatorFramework;
        if(admin) {
             curatorFramework = ZookeeperAccessService.getConnection();
            LOG.info("curator connection admin admin");
        }else{
             curatorFramework = ZookeeperAccessService.getConnectionAdmin();
             LOG.info("curator connection simple user admin "+curatorFramework);
        }
        Invoker invoker = new Invoker(curatorFramework);
        LOG.info("Fail on invoker??");
        try {
            for (String nodePath: pathsToNode) {
                LOG.info("Removing user: "+ username+". From path: "+ nodePath);
                invoker.invoke(new RemoveAllAclCommand(nodePath, username));
            }
            userRemoval = true;
        } catch (Exception e) {
            userRemoval = false;
            LOG.error("Problem on removing user: "+username+" from cluster: "+pathsToNode);
            e.printStackTrace();
            ZookeeperAccessService.stopConnectionAdmin(curatorFramework);
        }
        if(!admin) {
            ZookeeperAccessService.stopConnectionAdmin(curatorFramework);
        }
    }

    /**
     * A method that checks the data got from front-end login form and depending on int preforms one of the two actions.
     * 1 removes the user and created a new one if the username or password was changed using the remove and add methods
     * 2 calls the acl update method created when only the user rights information has been updated.
     * @param clustersAndRights a map of clusters with their rights
     * @param username the username to edit
     * @param password the password to change
     */
    public void updateUser(HashMap<String, List> clustersAndRights, String username, String password, boolean admin) throws AuthenticationException, InterruptedException {
        LOG.info("Inside edit user method call");
        boolean passwordWasChanged = false;
        List<String> userClusters = new ArrayList<>();
        password = passDecrypt(password);
//        password = password.substring(1, password.length()-1);
        for (Map.Entry<String, List> entry : clustersAndRights.entrySet()) {
            String cluster = entry.getKey();
            passwordWasChanged = checkForPasswordChange(password);
            userClusters.add(cluster);
        }
        try {
            if(admin) {
                if (passwordWasChanged) {
                    removeUser(userClusters, username, true);
                    addUser(clustersAndRights, username, password, false, true);
                } else {
                    updateUserAclCall(clustersAndRights, username);
                }
            }else{
                LOG.info("Correct choice non admin user");
                removeUser(userClusters, username, false);
                addUser(clustersAndRights, username, password, false, false);
            }

        } catch (AuthenticationException e) {
            LOG.error("Problem on editing user information");
            throw new AuthenticationException("A user with that username already exists");
        }catch (Exception e) {
            LOG.error("Problem on editing user information");
            throw new InterruptedException();
        }
    }

    private void updateUserAclCall(HashMap<String, List> clustersAndRights, String username){
        String rights = "";
        boolean adminRights = false;
        List<ACL> aclList = new ArrayList<>();
        CuratorFramework curatorFramework;
        try {
            for(Map.Entry<String, List> entry  : clustersAndRights.entrySet()) {
                ACL UserAcl = new ACL();
                String cluster = entry.getKey();
                curatorFramework = ZookeeperAccessService.getConnection();
                Invoker invoker = new Invoker(curatorFramework);
                List value = entry.getValue();
                if(value.contains("action")){
                    adminRights = true;
                    value.remove("action");
                }else{
                    adminRights = false;
                }
                rights = returnFirstWordCharString(value);
                int aclRights = getPermFromString(rights);
                if(aclList.isEmpty()) {
                    UserAcl= getAclListForUser(cluster, username);
                }
                UserAcl.setPerms(aclRights);
                String tempUserId ="";
                if(!aclList.isEmpty()) {
                    aclList.get(0).setPerms(aclRights);
                }else{
                    aclList.add(UserAcl);
                }
                LOG.info("ACL list: "+aclList);
                removeUser(Collections.singletonList(cluster), username, true);
                LOG.info("Add a new user to: "+cluster+". With the ACl set to : "+aclList+"and the action rights for user set to: " +adminRights);
                if(aclRights>0) {
                    invoker.invoke(new AddUserCommand(cluster, aclList, adminRights));
                }
            }
        } catch (Exception e) {
            LOG.error("A problem occurred while trying to update user rights data");
            e.printStackTrace();
        }
    }

    private Boolean checkForPasswordChange(String password){
        LOG.info(password);
        if(password!=null && !password.isEmpty()) {
            return !password.equals("\"************\"");
        }
        return false;
    }

    private int getPermFromString(String permString) {
        int perm = 0;

        for(int i = 0; i < permString.length(); ++i) {
            switch(permString.charAt(i)) {
                case 'a':
                    perm |= 16;
                    break;
                case 'c':
                    perm |= 4;
                    break;
                case 'd':
                    perm |= 8;
                    break;
                case 'r':
                    perm |= 1;
                    break;
                case 'w':
                    perm |= 2;
                    break;
                default:
                    System.err.println("Unknown perm type: " + permString.charAt(i));
            }
        }

        return perm;
    }

    public ACL  getAclListForUser(String clusterNode, String username){
        CuratorFramework curatorFramework = ZookeeperAccessService.getConnection();
        List<ACL> aclList;
        ACL returnValue = new ACL();
        String tempUsername;
        try {
            aclList = curatorFramework.getACL().forPath(clusterNode);
            for (ACL value : aclList) {
                tempUsername = value.getId().getId().split(":")[0];
                if (username.equals(tempUsername)) {
                    return value;
                }
            }
        }catch (Exception e) {
            LOG.error("Problem on getting users ACL list "+e);
            e.printStackTrace();
        }
        return returnValue;
    }

    /**
     * A method that returns the list of users with their information for user table update
     * @param availableClusters list of clusters to get users from
     * @return
     */
    public String getUserList(List<String> availableClusters){
        HashMap userList = new HashMap();
        String json = "";
        LOG.info("Trying to get the updated users list");
        CuratorFramework curatorFramework = ZookeeperAccessService.getConnection();
        try {
            LoginCache cache = new LoginCache();
            cache.clearUserMap();
            for(String cluster : availableClusters) {
                if(cluster != null && !cluster.isEmpty()){

                    CuratorUtils.userMapForAdmin(curatorFramework, cluster);
                }
            }
            HashMap usersList = cache.getUserMap();
            json = new ObjectMapper().writeValueAsString(usersList);
        }catch (JsonProcessingException e) {
            LOG.error("Problem on parsing json for users list update");
            e.printStackTrace();
        }catch (Exception e){
            LOG.error("Problem on users list update");
        }
        return json;
    }

    /**
     * A method that returns the list of user with his information for user info page
     * @param availableClusters list of clusters to get user info from
     * @return
     */
    public String getUser(List<String> availableClusters, String username){
        HashMap userList = new HashMap();
        String json = "";
        LOG.info("Trying to get the updated users list");
        CuratorFramework curatorFramework = ZookeeperAccessService.getConnection();
        try {
            LoginCache cache = new LoginCache();
            cache.clearUserMap();
            for(String cluster : availableClusters) {
                if(cluster != null && !cluster.isEmpty()){

                    CuratorUtils.userMapForUser(curatorFramework, cluster, username);
                }
            }
            HashMap usersList = cache.getUserMap();
            json = new ObjectMapper().writeValueAsString(usersList);
        }catch (JsonProcessingException e) {
            LOG.error("Problem on parsing json for users list update");
            e.printStackTrace();
        }catch (Exception e){
            LOG.error("Problem on users list update");
        }
        return json;
    }

    /**
     * Mehot to check if list contians the wanted value
     * @param input the input list
     * @param valueNeeded the value to find inside the list
     * @return
     */
    public Boolean checkIfListHasValue(List<String> input, String valueNeeded){
        if (input.contains(valueNeeded)) {
//            LOG.info("DEBUG ONLY : exclude : "+valueNeeded);
            return true;
        } else {
            return false;
        }
    }

    /**
     * A method that return a string from first letters in a list of strings ( Used to set the rights when creating a
     * new user or updating an existing one)
     * @param input
     * @return
     */
    public String returnFirstWordCharString(List<String> input){
        String userRightString ="";
        for (String userRight: input) {
            userRightString =userRightString+ userRight.substring(0,1);
        }
        LOG.info("User rights separated: "+ userRightString);
        return userRightString;
    }

    /**
     * A method used to check if user exists and is already logged in and if true to set so that user would not be
     * checked for credentials.
     * @param header auth token from header for user authentication
     * @param loginCache reference to the loginCache object/class
     * @return
     */
    public boolean checkIfUserExistAndBypassLogin(String header, LoginCache loginCache){
        if (header != null && !header.isEmpty()) {
            if (loginCache.checkIfUserExistInCache(header)) {
                loginCache.setBypassSecurity(true);
                loginTheUserByCredentials("", "");
                return true;
            } else {
                LOG.info("No user with the token provided was found");
                return false;
            }
        }else{
            return false;
        }
    }

    public String trimFieldLegth(String input){
        if(input.length()>50){
            return input.substring(0,50);
        }else{
            return input;
        }
    }

}
