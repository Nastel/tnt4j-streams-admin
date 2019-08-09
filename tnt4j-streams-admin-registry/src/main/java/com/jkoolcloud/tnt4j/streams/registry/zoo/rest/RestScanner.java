package com.jkoolcloud.tnt4j.streams.registry.zoo.rest;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

@ApplicationPath("/")
public class RestScanner extends Application {
	final Set<Class<?>> classes = new HashSet<>();

	@Override
	public Set<Class<?>> getClasses() {
		classes.add(RestEndpoint.class);
		return Collections.unmodifiableSet(classes);
	}

}
