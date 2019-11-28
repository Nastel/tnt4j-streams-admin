package commands;

import java.util.Iterator;
import java.util.List;

import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.ZKUtil;
import org.apache.zookeeper.data.ACL;

import com.beust.jcommander.Parameter;

public class RemoveAllAclCommand implements Command {

	@Parameter(names = "-n")
	private String node;

	@Parameter(names = "-id")
	private String id;

	public RemoveAllAclCommand() {
	}

	public RemoveAllAclCommand(String node, String id) {
		this.node = node;
		this.id = id;
	}

	private void remove(List<ACL> aclList) {
		if (aclList.size() == 1) {
			System.out.println("Node must contain atleast 1 acl entry");
			return;
		}

		Iterator<ACL> iterator = aclList.iterator();

		while (iterator.hasNext()) {
			ACL acl = iterator.next();

			String[] idPassword = acl.getId().getId().split(":");

			if (idPassword[0].equals(id)) {
				iterator.remove();
				break;
			}
		}
	}

	@Override
	public void execute(CuratorFramework curatorFramework) throws Exception {

		List<String> subTree = ZKUtil.listSubTreeBFS(curatorFramework.getZookeeperClient().getZooKeeper(), node);

		for (String node : subTree) {

			List<ACL> aclList = curatorFramework.getACL().forPath(node);

			remove(aclList);

			curatorFramework.setACL().withACL(aclList).forPath(node);

		}
	}

}
