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
