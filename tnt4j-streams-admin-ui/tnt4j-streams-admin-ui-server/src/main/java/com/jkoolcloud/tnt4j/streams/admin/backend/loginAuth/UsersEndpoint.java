/*
 * Copyright 2014-2019 JKOOL, LLC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jkoolcloud.tnt4j.streams.admin.backend.loginAuth;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.naming.AuthenticationException;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jkoolcloud.tnt4j.streams.admin.backend.utils.ClsConstants;


/**
 * The login endpoint
 */
@ApplicationScoped
@Path("/authenticate")
public class UsersEndpoint {
    private static Logger LOG = Logger.getLogger(UsersEndpoint.class);

    @Inject
    private UsersUtils usersUtils;

    /**
     * Preform the login and return the success or fail message
     * @param header login credentials
     * @return
     */
    @GET
    @Path("/login")
    @Produces(ClsConstants.MIME_TYPE_JSON)
    public Response doGetRepositoryInfo(@HeaderParam("Authorization") String header, @HeaderParam("test") String test) {
        LoginCache cache = new LoginCache();
        cache.setIsUserAdmin(false);
        ObjectMapper mapper = new ObjectMapper();
        String token = ""; String loginFailMessage="";
        try {
            HashMap Authorizations = mapper.readValue(header, HashMap.class);
            if(usersUtils.checkTheUserForExclude(Authorizations.get("username").toString())){
                return Response.status(403).build();
            }
            String loginResponse = usersUtils.loginTheUserByCredentials(Authorizations.get("username").toString(), Authorizations.get("password").toString());
            if(!loginResponse.isEmpty()){
                return Response.status(401).entity(loginResponse).build();
            }
            token = cache.getGeneratedToken();
            loginFailMessage = cache.getLoginStatus();
            loginFailMessage = "{ \"Login\" : \""+loginFailMessage+"\" }";
            if(token.isEmpty()){
                LOG.info("Failed login: "+loginFailMessage);
                return Response.status(401).entity(loginFailMessage).build();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        HashMap usersList = cache.getUserMap();
        try {
            String json = new ObjectMapper().writeValueAsString(usersList);
            if(cache.getIsUserAdmin()){
                    token = "{ \"token\" : \""+token+"\", \"admin\" : \"true\", \"userList\" : "+json+" }";
            }else{
                token = "{ \"token\" : \""+token+"\", \"admin\" : \"false\", \"userList\" : "+json+" }";
            }
        } catch (JsonProcessingException e) {
            LOG.error("Problem on converting user list to JSON");
            e.printStackTrace();
            return Response.status(200).entity("{ \"Login\" : \"Wrong JSON user list got\" }").build();
        }
        LOG.info("User token got auth success: "+ token);
        cache.setBypassSecurity(true);
        return Response.status(200).entity(token).build();
    }

    /**
     * Do the logout
     * @param header the auth token saved inside the request
     * @return
     */
    @GET
    @Path("/logout")
    @Produces(ClsConstants.MIME_TYPE_JSON)
    public Response logoutApproved(@HeaderParam("Authorization") String header) {
        LoginCache cache = new LoginCache();
        String logoutMessage=""; boolean userStillExist;
        try {
            cache.removeTheTokenFromCache(header);
            userStillExist = cache.checkIfUserExistInCache(header);
            if(!userStillExist) {
                cache.disconnectFromZooKeeeper();
                cache.setBypassSecurity(false);
                cache.setUserCount(0);
                cache.clearUserMap();
                logoutMessage = "{ \"logout\" : \"You have been successfully logged out!\" }";
            }
            else{
                return Response.status(500).entity("{ \"logout\" : \"There was a problem on logout of the user\" }").build();
            }
            if (header.isEmpty()) {
                LOG.error("Failed logout: "+ logoutMessage);
                return Response.status(401).entity("{ \"logout\" : \"No token was found\" }").build();
            }
        } catch (Exception e) {
            LOG.error("Failed logout: "+ logoutMessage);
            e.printStackTrace();
            return Response.status(500).entity("{ \"logout\" : \"No response from server or server error encountered\" }").build();
        }
        return Response.status(200).entity(logoutMessage).build();
    }

    @POST
    @Path("/addUser")
    @Produces(ClsConstants.MIME_TYPE_JSON)
    public Response addNewUser(@HeaderParam("Authorization") String header, String userObject) {
        LOG.info(userObject);
        String username, password;
        HashMap newUserInfo = new HashMap();
        LoginCache cache = new LoginCache();
        ObjectMapper mapper = new ObjectMapper();
        if(usersUtils.checkIfUserExistAndBypassLogin(header, cache)){
            try {
                newUserInfo = mapper.readValue(userObject, HashMap.class);
                username =  newUserInfo.get("username").toString();
                username = usersUtils.trimFieldLegth(username, 50);
                password = newUserInfo.get("password").toString();
                password = usersUtils.trimFieldLegth(password, 100);
                HashMap<String, List> clustersWithRights = (HashMap<String, List>) newUserInfo.get("clusters");
                usersUtils.addUser(clustersWithRights, username, password, true, true);
                return Response.status(200).entity("{ \"Register\" : \"New user "+username+" was added successfully!\" }").build();
            }
            catch (AuthenticationException e) {
                LOG.error("A user with that username already exists ");
                return Response.status(200).entity("{ \"Register\" : \"The user was not created successfully, the username is already taken!\"  }").build();
            }catch (Exception e) {
                LOG.error("Problem on adding a new user to the user ZooKeeper repository");
                return Response.status(200).entity("{ \"Register\" : \"The user was not created successfully, problem occurred!\"  }").build();
            }
        }
        else{
            LOG.info("Return a 401 inside tree data call");
            return Response.status(401).entity("{\"Register\" : \"Tried to access protected resources. No token was found\" }").build();
        }
    }

    @POST
    @Path("/removeUser")
    @Produces(ClsConstants.MIME_TYPE_JSON)
    public Response deleteUser(@HeaderParam("Authorization") String header, String userDeleteObject) {
        LOG.info(userDeleteObject);
        String username, clusterPath;
        HashMap userInfoForRemove;
        LoginCache cache = new LoginCache();
        ObjectMapper mapper = new ObjectMapper();

        if(usersUtils.checkIfUserExistAndBypassLogin(header, cache)){
            try {
                LOG.info("Trying to remove a user from ZooKeeper repo");
                userInfoForRemove = mapper.readValue(userDeleteObject, HashMap.class);
                username =  userInfoForRemove.get("username").toString();
                if(usersUtils.checkTheUserForExclude(username)){
                    return Response.status(403).build();
                }
                username = usersUtils.trimFieldLegth(username, 50);
                List<String> clusters = (List<String>) userInfoForRemove.get("clusters");
                try {
                    usersUtils.removeUser(clusters, username, true);
                    if(!UsersUtils.getUserRemoval()){
                        return Response.status(200).entity("{ \"Remove\" : \"A problem occurred, the user "+username+" was not removed successfully. Please try again later.\"  }").build();
                    }
                } catch (Exception e) {
                    LOG.info("Not registered, insufficient rights");
                    return Response.status(200).entity("{ \"Remove\" : \"The user "+username+" was not removed successfully, insufficient rights!\"  }").build();
                }
                return Response.status(200).entity("{ \"Remove\" : \"The user "+username+" was removed successfully!\" }").build();

            } catch (Exception e) {
                LOG.error("Problem on adding a new user to the user ZooKeeper repository");
                return Response.status(500).entity("{\"Remove\" : \"No response from server or server error encountered\" }").build();
            }
        }
        else{
            LOG.info("Return a 401 inside tree data call");
            return Response.status(401).entity("{\"Remove\" : \"Tried to access protected resources. No token was found\" }").build();
        }
    }

    @POST
    @Path("/editUser")
    @Produces(ClsConstants.MIME_TYPE_JSON)
    public Response updateUser(@HeaderParam("Authorization") String header, String userObject) {
        LOG.info(userObject);
        String username, password;
        HashMap newUserInfo;
        LoginCache cache = new LoginCache();
        ObjectMapper mapper = new ObjectMapper();
        LOG.info("Trying to edit user data");
        if(usersUtils.checkIfUserExistAndBypassLogin(header, cache)){
            try {
                newUserInfo = mapper.readValue(userObject, HashMap.class);
                username =  newUserInfo.get("username").toString();
                if(usersUtils.checkTheUserForExclude(username)){
                    return Response.status(403).build();
                }
                username = usersUtils.trimFieldLegth(username, 50);
                password = newUserInfo.get("password").toString();
                password = usersUtils.trimFieldLegth(password, 100);
                HashMap<String, List> clustersWithRights = (HashMap<String, List>) newUserInfo.get("clusters");
                try {
                    LOG.info("Before edit user method call");
                    usersUtils.updateUser(clustersWithRights, username, password, true);
                } catch (InterruptedException e) {
                    LOG.info("The registration was interrupted by an unexpected error");
                    return Response.status(200).entity("{ \"Edit\" : \"The user "+username+" was not updated successfully.\"  }").build();
                } catch (AuthenticationException e) {
                    LOG.info("Not registered, insufficient rights");
                    return Response.status(200).entity("{ \"Edit\" : \"The user "+username+" was not updated successfully, insufficient rights!\"  }").build();
                }
                return Response.status(200).entity("{ \"Edit\" : \"The user "+username+" updated successfully!\" }").build();

            } catch (Exception e) {
                LOG.error("Problem on adding a new user to the user ZooKeeper repository");
                return Response.status(401).entity("{\"Edit\" : \"No response from server or server error encountered\" }").build();
            }
        }
        else{
            LOG.info("Return a 401 inside tree data call");
            return Response.status(401).entity("{\"Edit\" : \"Tried to access protected resources. No token was found\" }").build();
        }
    }

    @POST
    @Path("/editUserNonAdmin")
    @Produces(ClsConstants.MIME_TYPE_JSON)
    public Response updateUserNonAdmin(@HeaderParam("Authorization") String header, String userObject) {
        LOG.info(userObject);
        String username, password;
        HashMap newUserInfo;
        LoginCache cache = new LoginCache();
        ObjectMapper mapper = new ObjectMapper();
        LOG.info("Trying to edit user data");
        if(usersUtils.checkIfUserExistAndBypassLogin(header, cache)){
            try {
                newUserInfo = mapper.readValue(userObject, HashMap.class);
                username =  newUserInfo.get("username").toString();
                if(usersUtils.checkTheUserForExclude(username)){
                    return Response.status(403).build();
                }
                username = usersUtils.trimFieldLegth(username, 50);
                password = newUserInfo.get("password").toString();
                password = usersUtils.trimFieldLegth(password, 100);
                HashMap<String, List> clustersWithRights = (HashMap<String, List>) newUserInfo.get("clusters");
                try {
                    LOG.info("Before edit user method call");
                    usersUtils.updateUser(clustersWithRights, username, password, false);
                } catch (InterruptedException e) {
                    LOG.info("The registration was interrupted by an unexpected error");
                    return Response.status(200).entity("{ \"Edit\" : \"The user "+username+" was not updated successfully.\"  }").build();
                } catch (AuthenticationException e) {
                    LOG.info("Not registered, insufficient rights");
                    return Response.status(200).entity("{ \"Edit\" : \"The user "+username+" was not updated successfully, insufficient rights!\"  }").build();
                }
                return Response.status(200).entity("{ \"Edit\" : \"The user "+username+" updated successfully!\" }").build();

            } catch (Exception e) {
                LOG.error("Problem on adding a new user to the user ZooKeeper repository");
                return Response.status(401).entity("{\"Edit\" : \"No response from server or server error encountered\" }").build();
            }
        }
        else{
            LOG.info("Return a 401 inside tree data call");
            return Response.status(401).entity("{\"Edit\" : \"Tried to access protected resources. No token was found\" }").build();
        }
    }

    @POST
    @Path("/refreshUserList")
    @Produces(ClsConstants.MIME_TYPE_JSON)
    public Response getUpdatedUsersList(@HeaderParam("Authorization") String header, String userInformationForEdit) {
        String userListResponse = "";
        LOG.info("Trying to get the refreshed users list");
        HashMap infoForUserUpdate;
        LoginCache cache = new LoginCache();
        ObjectMapper mapper = new ObjectMapper();
        if(usersUtils.checkIfUserExistAndBypassLogin(header, cache)){
            try {
                cache.setUserCount(0);
                infoForUserUpdate = mapper.readValue(userInformationForEdit, HashMap.class);
                List<String> clusters = (List<String>) infoForUserUpdate.get("clusters");
                try {
                    userListResponse = usersUtils.getUserList(clusters);
                } catch (Exception e) {
                    LOG.info("Not registered, insufficient rights");
                }
                return Response.status(200).entity("{ \"userList\" : "+userListResponse+" }").build();

            } catch (Exception e) {
                LOG.error("Problem on adding a new user to the user ZooKeeper repository");
                return Response.status(500).entity("{\"userList\" : \"No response from server or server error encountered.\" }").build();
            }
        }
        else{
            LOG.info("Return a 401 inside tree data call");
            return Response.status(401).entity("{\"userList\" : \"Tried to access protected resources. No token was found\" }").build();
        }
    }

    @POST
    @Path("/refreshUser")
    @Produces(ClsConstants.MIME_TYPE_JSON)
    public Response getUpdatedUserData(@HeaderParam("Authorization") String header, String userInformationForEdit) {
        String userListResponse = "";
        LOG.info("Trying to get the refreshed users list");
        HashMap infoForUserUpdate;
        LoginCache cache = new LoginCache();
        ObjectMapper mapper = new ObjectMapper();
        if(usersUtils.checkIfUserExistAndBypassLogin(header, cache)){
            try {
                cache.setUserCount(0);
                infoForUserUpdate = mapper.readValue(userInformationForEdit, HashMap.class);
                List<String> clusters = (List<String>) infoForUserUpdate.get("clusters");
                String username = (String) infoForUserUpdate.get("username");
                try {
                    userListResponse = usersUtils.getUser(clusters, username);
                } catch (Exception e) {
                    LOG.info("Not registered, insufficient rights");
                }
                return Response.status(200).entity("{ \"userList\" : "+userListResponse+" }").build();

            } catch (Exception e) {
                LOG.error("Problem on adding a new user to the user ZooKeeper repository");
                return Response.status(500).entity("{\"userList\" : \"No response from server or server error encountered.\" }").build();
            }
        }
        else{
            LOG.info("Return a 401 inside tree data call");
            return Response.status(401).entity("{\"userList\" : \"Tried to access protected resources. No token was found\" }").build();
        }
    }

}
