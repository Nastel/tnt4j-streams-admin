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
