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

import java.util.List;

import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.ZKUtil;
import org.apache.zookeeper.data.ACL;

import com.beust.jcommander.Parameter;

import converters.AclConverter;

public class AddUserCommand implements Command {

	private static String actionTokenNode = "_actionToken";
	private static String readTokenNode = "_readToken";

	@Parameter(names = "-n")
	private String node;

	@Parameter(names = "-p", converter = AclConverter.class)
	private List<ACL> acls;

	@Parameter(names = "-actions")
	private boolean actionsPermitted;

	public AddUserCommand() {
	}

	public AddUserCommand(String node, List<ACL> acls, boolean isAdmin) {
		this.node = node;
		this.acls = acls;
		actionsPermitted = isAdmin;
	}

	@Override
	public void execute(CuratorFramework curatorFramework) throws Exception {
		List<ACL> nodeAcls = null;

		List<String> nodes = null;
		try {
			nodes = ZKUtil.listSubTreeBFS(curatorFramework.getZookeeperClient().getZooKeeper(), node);
		} catch (Exception e) {
			e.printStackTrace();
		}

		for (String node : nodes) {

			if (node.contains(actionTokenNode) && !actionsPermitted) {
				continue;
			}

			nodeAcls = curatorFramework.getACL().forPath(node);
			nodeAcls.addAll(acls);
			curatorFramework.setACL().withACL(nodeAcls).forPath(node);
		}

	}
}
