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

import com.beust.jcommander.Parameter;

public class LsChildrenCommand implements Command {

	@Parameter(names = "-n")
	private String clusterPath;

	public LsChildrenCommand() {

	}

	public LsChildrenCommand(String clusterPath) {
		this.clusterPath = clusterPath;
	}

	@Override
	public void execute(CuratorFramework curatorFramework) throws Exception {
		List<String> clusters = curatorFramework.getChildren().forPath(clusterPath);

		for (String cluster : clusters) {
			System.out.println(cluster);
		}
	}
}
