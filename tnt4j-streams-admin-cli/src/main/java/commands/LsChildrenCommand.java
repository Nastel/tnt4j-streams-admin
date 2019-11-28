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
