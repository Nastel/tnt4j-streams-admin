package com.jkoolcloud.tnt4j.streams.admin.backend.loginAuth;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.*;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import org.apache.log4j.Logger;



public class StreamsAdminLogin implements LoginModule {



    // Logger used to output debug information
    private static final Logger LOG = Logger.getLogger(StreamsAdminLogin.class);
    LoginCache loginCache;
    // Initializing parameters
    private CallbackHandler handler;
    private Subject subject;
    private Map options;
    private Map sharedState;
    // Parameters to store user principals, users and their groups.
    private UserPrincipal userPrincipal;
    private RolePrincipal rolePrincipal;
    private List<String> userGroups;

    // configurable option
    private boolean debug = false;

    // the authentication status
    private boolean commitSucceeded = false;
    private boolean isAuthenticated = false;

    // User credentials
    private String username = null;
    private String password = null;

    /**
     * Load all the needed options
     * @param subject
     * @param callbackHandler
     * @param sharedState
     * @param options
     */
    @Override
    public void initialize(Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState,
                           Map<String, ?> options) {
        loginCache =  new LoginCache();
        handler = callbackHandler;
        this.subject = subject;

        // Second option.
        this.options = options;
        this.sharedState = sharedState;

        // initialize any configured options
        debug = "true".equalsIgnoreCase((String)options.get("debug"));
    }

    /**
     * Authenticate the user against chosen method
     * @return
     * @throws LoginException
     */
    @Override
    public boolean login() throws LoginException {
        boolean authenticateNow = loginCache.getBypassSecurity();
//        LOG.info("Is the user authenticated already and do not need any more AUTH on login method: "+ authenticateNow);
        if(authenticateNow){
            userGroups = getRoles();
            isAuthenticated = true;
            return true;
        }
        // If no handler is specified throw a error
        if (handler == null) {
            throw new LoginException("Error: no CallbackHandler available to receive authentication information from the user");
        }
        Callback[] callbacks = new Callback[2];
        callbacks[0] = new NameCallback("login");
        callbacks[1] = new PasswordCallback("password", true);
        try {
            handler.handle(callbacks);
            username = ((NameCallback) callbacks[0]).getName();
            password = String.valueOf(((PasswordCallback) callbacks[1]).getPassword());
//            if (debug) {
//                LOG.info("Username : "+ username);
//                LOG.info("Password : "+ password);
//            }

            // If no username or password is given throw LoginException
            if (username == null || username.isEmpty() || password.isEmpty()) {
                LOG.error("Authentication failed empty credentials");
                loginCache.setLoginStatus("Authentication failed");
                throw new LoginException("Data specified had null values");
            }
            else if (loginCache.checkForSuccessfulLogin(username+":"+password)) {
//            if ( (username.equals("test") && password.equals("test")) || (username.equals("admin") && password.equals("admin"))) {
                userGroups = getRoles();
                isAuthenticated = true;
                //LOG.info("Login successful");
                return true;
            }
            LOG.error("Authentication failed  No access to clusters");
            loginCache.setLoginStatus("Authentication failed");
            throw new LoginException("Authentication failed");
        } catch (IOException e) {
            LOG.info("Problem in login module");
            isAuthenticated = false;
            throw new LoginException(e.getMessage());
        } catch (UnsupportedCallbackException e) {
            LOG.info("Problem in login module UnsupportedCallbackException");
            isAuthenticated = false;
            throw new LoginException(e.getMessage());
        }
    }


    @Override
    public boolean commit() throws LoginException {
        boolean authenticateNow = loginCache.getBypassSecurity();
        if(authenticateNow){
            commitSucceeded = true;
            return true;
        }
        else if (!isAuthenticated) {
            return false;
        } else {
            userPrincipal = new UserPrincipal(username);
            subject.getPrincipals().add(userPrincipal);

            if (userGroups != null && userGroups.size() > 0) {
                for (String groupName : userGroups) {
                    rolePrincipal = new RolePrincipal(groupName);
                    subject.getPrincipals().add(rolePrincipal);
                }
                commitSucceeded = true;
            }else{
                commitSucceeded = false;
                loginCache.setLoginStatus("Authorization failed");
            }
            return true;
        }
    }

    /**
     * Terminates the logged in session on error
     *
     * @return @throws LoginException
     */
    @Override
    public boolean abort() throws LoginException {
        if (!isAuthenticated) {
            return false;
        } else if (!commitSucceeded) {
            isAuthenticated = false;
            username = null;
            password = null;
            userPrincipal = null;
        } else {
            logout();
        }
        return true;
    }

    /**
     * Logs the user out
     *
     * @return @throws LoginException
     */
    @Override
    public boolean logout() throws LoginException {
        isAuthenticated = false;
        isAuthenticated = commitSucceeded;
        subject.getPrincipals().clear();
        return true;
    }

    /**
     * Returns list of roles assigned to authenticated user.
     *
     * @return
     */
    private List<String> getRoles() {

        List<String> roleList = new ArrayList<>();
        roleList.add("admin");

        return roleList;
    }

}