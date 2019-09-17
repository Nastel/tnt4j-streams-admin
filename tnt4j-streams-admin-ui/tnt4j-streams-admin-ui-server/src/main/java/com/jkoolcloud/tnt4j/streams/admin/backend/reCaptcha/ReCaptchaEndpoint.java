package com.jkoolcloud.tnt4j.streams.admin.backend.reCaptcha;

import java.util.HashMap;

import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jkoolcloud.tnt4j.streams.admin.backend.loginAuth.LoginCache;
import com.jkoolcloud.tnt4j.streams.admin.backend.loginAuth.UsersUtils;
import com.jkoolcloud.tnt4j.streams.admin.backend.utils.ClsConstants;

@Path("/reCaptcha")
public class ReCaptchaEndpoint {
    private static Logger LOG = Logger.getLogger(ReCaptchaEndpoint.class);

    @POST
    @Produces(ClsConstants.MIME_TYPE_JSON)
    public Response getReCaptchaAdmin(@HeaderParam("Authorization") String header, String resolvedCaptcha) {
        UsersUtils usersUtils = new UsersUtils();
        JsonNode response;
//        LOG.info("Trying to do the cache auth");
        HashMap infoForUserUpdate;
        LoginCache cache = new LoginCache();
        ObjectMapper mapper = new ObjectMapper();
        if(usersUtils.checkIfUserExistAndBypassLogin(header, cache)){
            try {
                cache.setUserCount(0);
                infoForUserUpdate = mapper.readValue(resolvedCaptcha, HashMap.class);
                String captchaToken = (String) infoForUserUpdate.get("responseCaptcha");
//                LOG.info("RE-CAPTCHA TOKEN RESPONSE: "+captchaToken);
                response = CaptchaUtils.verify(captchaToken);
//                LOG.info("RESPONSE: "+response);
                if (response != null) {
                    return Response.status(200).entity("{ \"re-captcha\" : "+response+" }").build();
                }else{
                    return Response.status(200).entity("{ \"re-captcha\" : \"Problem on verifying the recapcha\" }").build();
                }
            } catch (Exception e) {
                LOG.error("Problem on verifying the re-captcha ");
                return Response.status(500).entity("{\"re-captcha\" : \"No response from server or server error encountered.\" }").build();
            }
        }
        else{
            LOG.info("Return a 401 inside tree data call");
            return Response.status(401).entity("{\"re-captcha\" : \"Tried to access protected resources. No token was found\" }").build();
        }
    }

    @POST
    @Path("/unauthorized")
    @Produces(ClsConstants.MIME_TYPE_JSON)
    public Response getReCaptchaLogin(@HeaderParam("Authorization") String header, String resolvedCaptcha) {
        UsersUtils usersUtils = new UsersUtils();
        JsonNode response;
        LOG.info("Trying to do the cache auth");
        HashMap infoForUserUpdate;
        LoginCache cache = new LoginCache();
        ObjectMapper mapper = new ObjectMapper();
        try {
            cache.setUserCount(0);
            infoForUserUpdate = mapper.readValue(resolvedCaptcha, HashMap.class);
            String captchaToken = (String) infoForUserUpdate.get("responseCaptcha");
            LOG.info("RE-CAPTCHA TOKEN RESPONSE: "+captchaToken);
            response = CaptchaUtils.verify(captchaToken);
            LOG.info("RESPONSE: "+response);
            if (response != null) {
                return Response.status(200).entity("{ \"re-captcha\" : "+response+" }").build();
            }else{
                return Response.status(200).entity("{ \"re-captcha\" : \"Problem on verifying the recapcha\" }").build();
            }
        } catch (Exception e) {
            LOG.error("Problem on verifying the re-captcha ");
            return Response.status(500).entity("{\"re-captcha\" : \"No response from server or server error encountered.\" }").build();
        }
    }
}
