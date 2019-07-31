package zookeeper;

import org.apache.curator.framework.recipes.queue.SimpleDistributedQueue;

public class DistributedQueueManager {

	SimpleDistributedQueue simpleDistributedQueue;

	public DistributedQueueManager(SimpleDistributedQueue simpleDistributedQueue) {
		this.simpleDistributedQueue = simpleDistributedQueue;
	}

	public byte[] consume() throws Exception {
		byte[] bytes = simpleDistributedQueue.take();
		return bytes;
	}

	public void offer(String data) throws Exception {
		simpleDistributedQueue.offer(data.getBytes());
	}

}
