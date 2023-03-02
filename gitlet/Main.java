package gitlet;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author TODO
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            Utils.message("Please enter a command.");
        }

        String firstArg = args[0];
        switch(firstArg) {
            case "init":
                validateNumArgs("init", args, 1);
                if (Repository.gitletInitiated()) {
                    exitWithMessage("A Gitlet version-control system already exists in the current directory.");
                }

                Repository.setupPersistence();
                break;
            case "add":
                validateNumArgs("add", args, 2);
                if (!Repository.gitletInitiated()) {
                    exitWithMessage("Not in an initialized Gitlet directory.");
                }

                String filename = args[1];
                Repository.addFiletoStaging(filename);
                break;
            case "commit":
                validateNumArgs("commit", args, 2);
                if (!Repository.gitletInitiated()) {
                    exitWithMessage("Not in an initialized Gitlet directory.");
                }

                String message = args[1];
                Repository.commitStagedChanges(message);
                break;
            case "log":
                validateNumArgs("log", args, 1);
                if (!Repository.gitletInitiated()) {
                    exitWithMessage("Not in an initialized Gitlet directory.");
                }

                Repository.logHeadHistory();
                break;
            // TODO: FILL THE REST IN
            default:
                Utils.message("No command with that name exists.");
        }
        return;
    }

    public static void validateNumArgs(String cmd, String[] args, int n) {
        if (args.length != n) {
            exitWithMessage("Incorrect operands.");
        }
    }

    public static void exitWithMessage(String msg) {
        Utils.message(msg);
        System.exit(0);
    }
}
