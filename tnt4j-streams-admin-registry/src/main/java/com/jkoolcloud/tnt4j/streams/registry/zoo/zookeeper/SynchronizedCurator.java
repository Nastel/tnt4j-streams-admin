package com.jkoolcloud.tnt4j.streams.registry.zoo.zookeeper;

import org.apache.curator.CuratorZookeeperClient;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.imps.CuratorFrameworkState;

public class SynchronizedCurator {

	private static CuratorFramework curatorFramework;

	public SynchronizedCurator(CuratorFramework curatorFramework) {
		this.curatorFramework = curatorFramework;
	}

	public synchronized void start() {
		if (curatorFramework.getState().equals(CuratorFrameworkState.LATENT)) {
			curatorFramework.start();
		}
	}

	public synchronized void close() {
		if (curatorFramework.getState().equals(CuratorFrameworkState.STARTED)) {
			curatorFramework.close();
		}
	}

	public CuratorZookeeperClient getZookeeperClient() {
		return curatorFramework.getZookeeperClient();
	}

	public CuratorFramework getCuratorFramework() {
		return curatorFramework;
	}

}
