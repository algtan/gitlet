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
            Utils.exitWithMessage("Please enter a command.");
        }

        String filename;
        String message;
        String branchName;

        String firstArg = args[0];
        switch(firstArg) {
            case "init":
                validateNumArgs("init", args, 1);
                if (Repository.gitletInitiated()) {
                    Utils.exitWithMessage("A Gitlet version-control system already exists in the current directory.");
                }

                Repository.setupPersistence();
                break;
            case "add":
                validateNumArgs("add", args, 2);
                if (!Repository.gitletInitiated()) {
                    Utils.exitWithMessage("Not in an initialized Gitlet directory.");
                }

                filename = args[1];
                Repository.addFiletoStaging(filename);
                break;
            case "commit":
                validateNumArgs("commit", args, 2);
                if (!Repository.gitletInitiated()) {
                    Utils.exitWithMessage("Not in an initialized Gitlet directory.");
                }

                message = args[1];
                Repository.commitStagedChanges(message);
                break;
            case "log":
                validateNumArgs("log", args, 1);
                if (!Repository.gitletInitiated()) {
                    Utils.exitWithMessage("Not in an initialized Gitlet directory.");
                }

                Repository.logHeadHistory();
                break;
            case "checkout":
                validateNumArgs("checkout", args, 2, 4);
                if (!Repository.gitletInitiated()) {
                    Utils.exitWithMessage("Not in an initialized Gitlet directory.");
                }

                String secondArg = args[1];
                if (args.length == 2) {
                    Repository.checkoutBranch(secondArg);
                    break;
                }

                String thirdArg = args[2];
                if (args.length == 3 && secondArg.equals("--")) {
                    Repository.checkoutFilePerHead(thirdArg);
                    break;
                }

                if (args.length == 4 && thirdArg.equals("--")) {
                    String fourthArg = args[3];
                    Repository.checkoutFilePerCommitId(secondArg, fourthArg);
                    break;
                }

                Utils.exitWithMessage("Incorrect operands");
            case "status":
                validateNumArgs("status", args, 1);
                if (!Repository.gitletInitiated()) {
                    Utils.exitWithMessage("Not in an initialized Gitlet directory.");
                }

                Repository.getStatus();
                break;
            case "rm":
                validateNumArgs("status", args, 2);
                if (!Repository.gitletInitiated()) {
                    Utils.exitWithMessage("Not in an initialized Gitlet directory.");
                }

                filename = args[1];
                Repository.removeFile(filename);
                break;
            case "global-log":
                validateNumArgs("global-log", args, 1);
                if (!Repository.gitletInitiated()) {
                    Utils.exitWithMessage("Not in an initialized Gitlet directory.");
                }

                Repository.logAllCommits();
                break;
            case "find":
                validateNumArgs("status", args, 2);
                if (!Repository.gitletInitiated()) {
                    Utils.exitWithMessage("Not in an initialized Gitlet directory.");
                }

                message = args[1];
                Repository.findCommits(message);
                break;
            case "branch":
                validateNumArgs("branch", args, 2);
                if (!Repository.gitletInitiated()) {
                    Utils.exitWithMessage("Not in an initialized Gitlet directory.");
                }

                branchName = args[1];
                Repository.createBranch(branchName);
                break;
            // TODO: FILL THE REST IN
            default:
                Utils.message("No command with that name exists.");
        }
        return;
    }

    public static void validateNumArgs(String cmd, String[] args, int n) {
        if (args.length != n) {
            Utils.exitWithMessage("Incorrect operands.");
        }
    }

    public static void validateNumArgs(String cmd, String[] args, int min, int max) {
        if (args.length < min || args.length > max) {
            Utils.exitWithMessage("Incorrect operands.");
        }
    }
}
