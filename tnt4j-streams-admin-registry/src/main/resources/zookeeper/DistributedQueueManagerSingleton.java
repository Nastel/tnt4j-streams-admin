package zookeeper;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.queue.SimpleDistributedQueue;

public class DistributedQueueManagerSingleton {

	private static DistributedQueueManager distributedQueueManager;

	private DistributedQueueManagerSingleton() {
	}

	public static void Init(CuratorFramework curatorFramework, String nodePath) {
		if (distributedQueueManager == null) {
			distributedQueueManager = new DistributedQueueManager(
					new SimpleDistributedQueue(curatorFramework, nodePath));
		}
	}

	public static DistributedQueueManager getDistributedQueueManager() {
		if (distributedQueueManager == null) {
			throw new AssertionError("You have to call init first");
		}
		return distributedQueueManager;
	}
}
