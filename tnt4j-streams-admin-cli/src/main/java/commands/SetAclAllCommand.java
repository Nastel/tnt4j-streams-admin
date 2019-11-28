package commands;

import java.util.List;

import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.ZKUtil;
import org.apache.zookeeper.data.ACL;

import com.beust.jcommander.Parameter;

import converters.AclConverter;

public class SetAclAllCommand implements Command {
	@Parameter(names = "-n")
	private String node;

	@Parameter(names = "-p", converter = AclConverter.class)
	private List<ACL> acls;

	public SetAclAllCommand() {
	}

	public SetAclAllCommand(String node, List<ACL> acls) {
		this.node = node;
		this.acls = acls;
	}

	@Override
	public void execute(CuratorFramework curatorFramework) throws Exception {

		List<String> nodes = ZKUtil.listSubTreeBFS(curatorFramework.getZookeeperClient().getZooKeeper(), node);

		for (String node : nodes) {
			curatorFramework.setACL().withACL(acls).forPath(node);
		}

	}
}
