package commands;

import java.util.List;

import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.data.ACL;

import com.beust.jcommander.Parameter;

public class GetAclCommand implements Command {

	@Parameter(names = "-n")
	private String node;

	public GetAclCommand() {
	}

	public GetAclCommand(String node) {
		this.node = node;
	}

	@Override
	public void execute(CuratorFramework curatorFramework) throws Exception {
		List<ACL> aclList = curatorFramework.getACL().forPath(node);

		int index = 0;
		for (ACL acl : aclList) {
			// System.out.println(acl.toString());
			System.out.println("Index: " + index);
			System.out.println("Scheme: " + acl.getId().getScheme());
			System.out.println("Id: " + acl.getId().getId());
			System.out.println("Perms:" + acl.getPerms());
			System.out.println(" ");
			index++;
		}
	}
}
