package commands;

import java.util.List;

import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.ZKUtil;

import com.beust.jcommander.Parameter;

public class LsAllNodesCommand implements Command {

	@Parameter(names = "-n")
	private String pathRoot;

	public LsAllNodesCommand() {
	}

	public LsAllNodesCommand(String pathRoot) {
		this.pathRoot = pathRoot;
	}

	@Override
	public void execute(CuratorFramework curatorFramework) throws Exception {
		List<String> nodes = null;

		nodes = ZKUtil.listSubTreeBFS(curatorFramework.getZookeeperClient().getZooKeeper(), pathRoot);

		for (String node : nodes) {
			System.out.println(node);
		}
	}
}
