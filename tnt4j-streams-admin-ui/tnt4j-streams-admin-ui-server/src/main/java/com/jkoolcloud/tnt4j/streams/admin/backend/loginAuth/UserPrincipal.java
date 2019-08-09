package com.jkoolcloud.tnt4j.streams.admin.backend.loginAuth;

import java.security.Principal;

public class UserPrincipal implements Principal {

    private String name;

    /**
     * Initializer
     *
     * @param name
     */
    public UserPrincipal(String name) {
        super();
        this.name = name;
    }

    /**
     * Get the name of the user
     *
     * @return
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Set the name of the user
     *
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }
}
