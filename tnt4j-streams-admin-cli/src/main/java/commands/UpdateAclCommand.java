package commands;

import java.util.List;

import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.data.ACL;

import com.beust.jcommander.Parameter;

import converters.AclConverter;

public class UpdateAclCommand implements Command {

	@Parameter(names = "-n")
	private String node;

	@Parameter(names = "-p", converter = AclConverter.class)
	private List<ACL> acls;

	public UpdateAclCommand() {
	}

	public UpdateAclCommand(String node, List<ACL> acls) {
		this.node = node;
		this.acls = acls;
	}

	@Override
	public void execute(CuratorFramework curatorFramework) throws Exception {

		List<ACL> nodeAcls = curatorFramework.getACL().forPath(node);

		nodeAcls.addAll(acls);

		curatorFramework.setACL().withACL(nodeAcls).forPath(node);

	}
}
