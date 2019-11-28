package com.jkoolcloud.tnt4j.streams.registry.zoo.authentication;

import org.apache.directory.api.ldap.model.cursor.EntryCursor;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.message.SearchScope;
import org.apache.directory.ldap.client.api.LdapConnection;
import org.apache.directory.ldap.client.api.LdapNetworkConnection;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.server.ServerCnxn;
import org.apache.zookeeper.server.auth.AuthenticationProvider;

public class LdapAuth implements AuthenticationProvider {

	private String server = "127.0.0.1";
	private int port = 10389;

	public boolean isUserValid(String creds) throws Exception {

		LdapConnection ldapConnection = new LdapNetworkConnection(server, port);

		ldapConnection.bind();

		System.out.println("isConnected: " + ldapConnection.isConnected());

		EntryCursor cursor = null;

		cursor = ldapConnection.search("o=users", "(objectclass=*)", SearchScope.ONELEVEL, "*");

		while (cursor.next()) {
			Entry entry = cursor.get();

		}

		ldapConnection.unBind();
		ldapConnection.close();

		return false;
	}

	// Triggers on setAcl, addauth scheme auth
	@Override
	public String getScheme() {
		return "ldap";
	}

	// Triggers on addauth scheme auth
	@Override
	public KeeperException.Code handleAuthentication(ServerCnxn serverCnxn, byte[] bytes) {
		String userCreds = new String(bytes);

		// serverCnxn.addAuthInfo();
		return null;
	}

	// Triggers on getAcl, get
	@Override
	public boolean matches(String s, String s1) {
		return false;
	}

	@Override
	public boolean isAuthenticated() {
		return false;
	}

	// Triggers on getAcl node
	@Override
	public boolean isValid(String s) {
		return false;
	}
}
