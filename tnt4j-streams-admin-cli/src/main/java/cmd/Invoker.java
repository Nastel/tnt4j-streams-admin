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

package cmd;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryForever;

import com.beust.jcommander.Parameter;

import commands.Command;

public class Invoker {

	@Parameter(names = "-login")
	private String login;
	@Parameter(names = "-pass")
	private String password;
	@Parameter(names = "-ip")
	private String ip;

	private CuratorFramework curatorFramework;

	public Invoker() {
	}

	public Invoker(CuratorFramework curatorFramework) {
		this.curatorFramework = curatorFramework;
	}

	public Invoker(String login, String password, String ip) {
		this.login = login;
		this.password = password;
		this.ip = ip;
	}

	public void startConnection() {
		curatorFramework = CuratorFrameworkFactory.builder().connectString(ip).retryPolicy(new RetryForever(10))
				.authorization("digest", (login + ":" + password).getBytes()).build();
		curatorFramework.start();
	}

	public void closeConnection() {
		curatorFramework.close();
	}

	public void invoke(Command command) throws Exception {
		command.execute(curatorFramework);
	}

}
