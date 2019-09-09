package cmd;

import java.util.Scanner;

import com.beust.jcommander.JCommander;

import commands.*;

public class CliMain {

	private static final String LIST_ALL_NODES = "lsAll";
	private static final String LIST_CHILDREN = "lsChildren";
	private static final String UPDATE_ACL = "updateAcl";
	private static final String UPDATE_ALL_ACLS = "updateAclAll";
	private static final String GET_ACL = "getAcl";
	private static final String REMOVE_ACL = "removeAcl";
	private static final String REMOVE_ALL_ACL = "removeAclAll";
	private static final String SET_ACL_ALL = "setAclAll";
	private static final String ADD_USER = "addUser";
	private static final String GENERATE_CONFIG = "gen";
	private static final String LOGIN = "login";
	private static final String HELP = "help";
	private static Invoker invoker = new Invoker();

	private static void printBanner() {
		System.out.println(" ______                                 _         _                    ____ _     ___ \n"
				+ "|__  / | __   _ __   ___ _ __ _ __ ___ (_)___ ___(_) ___  _ __  ___   / ___| |   |_ _|\n"
				+ "  / /| |/ /  | '_ \\ / _ \\ '__| '_ ` _ \\| / __/ __| |/ _ \\| '_ \\/ __| | |   | |    | | \n"
				+ " / /_|   <   | |_) |  __/ |  | | | | | | \\__ \\__ \\ | (_) | | | \\__ \\ | |___| |___ | | \n"
				+ "/____|_|\\_\\  | .__/ \\___|_|  |_| |_| |_|_|___/___/_|\\___/|_| |_|___/  \\____|_____|___|\n"
				+ "             |_|                                                                      ");
	}

	private static void printAvailableCommands() {
		System.out.println("---------------------------------------");
		System.out.println("lsAll [-n]");
		System.out.println("list all nodes below the specified node");
		System.out.println("example: lsAll -n /streams/v1");
		System.out.println();

		System.out.println("lsChildren [-n] ");
		System.out.println("list all children below a certain node");
		System.out.println("example: lsChildren -n /streams/v1");
		System.out.println();

		System.out.println("getAcl [-n]");
		System.out.println("get acl of a specified node");
		System.out.println("example: getAcl -n /streams ");
		System.out.println();

		System.out.println("updateAcl [-n] [-p]");
		System.out.println("update acl of a specified node");
		System.out.println("example: updateAcl -n /streams -p digest:user1:user1:crdwa");
		System.out.println("example: updateAcl -n /streams -p digest:user1:user1:crdwa,digest:user2:user2:crdwa");
		System.out.println();

		System.out.println("updateAclAll [-n] [-p]");
		System.out.println("update acl of all nodes below the specified node ");
		System.out.println("example: updateAclAll -n /streams -p digest:user1:user1:crdwa");
		System.out.println("example: updateAclAll -n /streams -p digest:user1:user1:crdwa,digest:user2:user2:crdwa");
		System.out.println();

		System.out.println("removeAcl [-n] [-id]");
		System.out.println("removes acl entry of a specified node ");
		System.out.println("will fail if node contain only 1 entry");
		System.out.println("example: removeAcl -n /streams -id user1");
		System.out.println();

		System.out.println("removeAclAll [-n] [-id]");
		System.out.println("removes all acl entries below specified node");
		System.out.println("will fail if node contain only 1 entry");
		System.out.println("example: removeAllAcl -n /streams -id user1");
		System.out.println();

		System.out.println("setAclAll [-n] [-p]");
		System.out.println("overrides all acl entries");
		System.out.println("example: setAclAll -n /streams -p digest:user1:user1:cdrwa");
		System.out.println("example: setAclAll -n /streams -p digest:user1:user1:cdrwa,digest:user1:user1:cdrwa");
		System.out.println();

		System.out.println("addUser [-n] [-p] [-actions]");
		System.out.println("creates a new user");
		System.out.println("add -admin if you want to create a admin user");
		System.out.println("admin creation example: addUser -n /streams -p digest:user1:user1:cdrwa -actions");
		System.out.println("basic user creation example: addUser -n /streams -p digest:user1:user1:cdrwa");
		System.out.println();

		System.out.println("gen [-t] [-v] [-o]");
		System.out.println("-t template path");
		System.out.println("-v values path");
		System.out.println("-o output path");

		System.out.println("login -login -pass -ip");

		System.out.println("help");
		System.out.println("displays short manual for all commands");
		System.out.println();
		System.out.println("---------------------------------------");
	}

	public static void invokerWrapper(Command command) {
		try {
			invoker.invoke(command);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		Scanner in = new Scanner(System.in);

		System.out.println("Login to zookeeper -login -pass -ip");
		System.out.println("example: -login user1 -pass user1 -ip 127.0.0.1");
		printBanner();
		printAvailableCommands();
		while (true) {
			String[] tokens = in.nextLine().split(" ");

			switch (tokens[0]) {

			case LIST_ALL_NODES:
				LsAllNodesCommand listAllNodesCommand = new LsAllNodesCommand();
				JCommander.newBuilder().addCommand(LIST_ALL_NODES, listAllNodesCommand).args(tokens).build();
				invokerWrapper(listAllNodesCommand);
				break;

			case LIST_CHILDREN:
				LsChildrenCommand clusters = new LsChildrenCommand();
				JCommander.newBuilder().addCommand(LIST_CHILDREN, clusters).args(tokens).build();
				invokerWrapper(clusters);
				break;

			case GET_ACL:
				GetAclCommand getAclCommand = new GetAclCommand();
				JCommander.newBuilder().addCommand(GET_ACL, getAclCommand).args(tokens).build();
				invokerWrapper(getAclCommand);
				break;

			case SET_ACL_ALL:
				SetAclAllCommand setAclAllCommand = new SetAclAllCommand();
				JCommander.newBuilder().addCommand(SET_ACL_ALL, setAclAllCommand).args(tokens).build();
				invokerWrapper(setAclAllCommand);
				break;

			case UPDATE_ACL:
				UpdateAclCommand updateAclCommand = new UpdateAclCommand();
				JCommander.newBuilder().addCommand(UPDATE_ACL, updateAclCommand).args(tokens).build();
				invokerWrapper(updateAclCommand);
				break;

			case UPDATE_ALL_ACLS:
				UpdateAllAclCommand updateAllAclCommand = new UpdateAllAclCommand();
				JCommander.newBuilder().addCommand(UPDATE_ALL_ACLS, updateAllAclCommand).args(tokens).build();
				invokerWrapper(updateAllAclCommand);
				break;

			case REMOVE_ACL:
				RemoveAclCommand removeAclCommand = new RemoveAclCommand();
				JCommander.newBuilder().addCommand(REMOVE_ACL, removeAclCommand).args(tokens).build();
				invokerWrapper(removeAclCommand);
				break;

			case REMOVE_ALL_ACL:
				RemoveAllAclCommand removeAllAclCommand = new RemoveAllAclCommand();
				JCommander.newBuilder().addCommand(REMOVE_ALL_ACL, removeAllAclCommand).args(tokens).build();
				invokerWrapper(removeAllAclCommand);
				break;
			case ADD_USER:
				AddUserCommand addUserCommand = new AddUserCommand();
				JCommander.newBuilder().addCommand(ADD_USER, addUserCommand).args(tokens).build();
				invokerWrapper(addUserCommand);
				break;
			case GENERATE_CONFIG:
				GenerateConfigCommand generateConfigCommand = new GenerateConfigCommand();
				JCommander.newBuilder().addCommand(GENERATE_CONFIG, generateConfigCommand).args(tokens).build();
				generateConfigCommand.execute();
				break;
			case LOGIN:
				JCommander.newBuilder().addCommand(LOGIN, invoker).args(tokens).build();
				invoker.startConnection();
				break;

			case HELP:
				printAvailableCommands();
				break;

			default:
				System.out.println("Command is incorrect");
				break;
			}

		}
	}
}
