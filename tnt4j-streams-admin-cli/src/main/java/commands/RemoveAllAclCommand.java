/*
 * Copyright 2014-2020 JKOOL, LLC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
