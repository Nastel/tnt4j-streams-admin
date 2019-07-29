package com.jkoolcloud.tnt4j.streams.registry.zoo.requestExecutors;

public interface JsonRpcRequest<T> {
	public void processRequest(T args);
}
