package com.jkoolcloud.tnt4j.streams.registry.zoo.authentication;

public interface Auth<T> {

	T getIdentifier();

	boolean isIdentifierValid(T token);
}
