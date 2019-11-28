package commands;

import org.apache.curator.framework.CuratorFramework;

public interface Command {
	public void execute(CuratorFramework curatorFramework) throws Exception;
}
