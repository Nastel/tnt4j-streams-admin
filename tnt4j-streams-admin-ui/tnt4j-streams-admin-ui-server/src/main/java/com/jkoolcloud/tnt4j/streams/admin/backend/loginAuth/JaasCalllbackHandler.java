package com.jkoolcloud.tnt4j.streams.admin.backend.loginAuth;

import java.io.IOException;

import javax.security.auth.callback.*;


public class JaasCalllbackHandler implements CallbackHandler {

    private String name;
    private String password;

    public JaasCalllbackHandler(String name, String password) {
        this.name = name;
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    @Override
    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
        for (int i = 0; i < callbacks.length; i++) {
            if (callbacks[i] instanceof NameCallback) {
                NameCallback nameCallback = (NameCallback) callbacks[i];
                nameCallback.setName(name);
            } else if (callbacks[i] instanceof PasswordCallback) {
                PasswordCallback passwordCallback = (PasswordCallback) callbacks[i];
                passwordCallback.setPassword(password.toCharArray());
            } else {
                throw new UnsupportedCallbackException(callbacks[i], "The submitted Callback is unsupported");
            }
        }
    }
}
