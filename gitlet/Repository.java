package gitlet;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TreeMap;

import static gitlet.Commit.COMMIT_DIR;
import static gitlet.Utils.*;

// TODO: any imports you need here

/** Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author TODO
 */
public class Repository {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    /** The blobs directory. */
    public static final File BLOBS_DIR = join(GITLET_DIR, "blobs");
    /** The refs directory (for branches). */
    public static final File REFS_DIR = join(GITLET_DIR, "refs");
    /** The staging area. */
    public static final File STAGING_DIR = join(GITLET_DIR, "staging");
    /** The HEAD reference file. */
    public static final File HEAD = join(GITLET_DIR, "HEAD");

    private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z");

    /* TODO: fill in the rest of this class. */
    public static boolean gitletInitiated() {
        return GITLET_DIR.exists();
    }

    public static void setupPersistence() {
        BLOBS_DIR.mkdirs();
        COMMIT_DIR.mkdirs();
        REFS_DIR.mkdirs();

        String branchName = "master";
        writeContents(HEAD, branchName);
        createCommit(branchName, "initial commit", 0, new TreeMap<>(), null);
    }

    public static void addFiletoStaging(String filename) {
        STAGING_DIR.mkdirs();

        String parentRef = getBranchRef(getCurrentBranch());
        Commit parentCommit = getCommit(parentRef);
        String oldHash = parentCommit.getTree().get(filename);

        byte[] fileToAddContents = readContents(join(CWD, filename));
        String newHash = sha1(fileToAddContents);
        File stagedFile = join(STAGING_DIR, filename);

        if (newHash.equals(oldHash)) {
            stagedFile.delete();
            return;
        }

        writeContents(stagedFile, fileToAddContents);
    }

    public static void commitStagedChanges(String message) {
        String currentBranch = getCurrentBranch();
        String parentRef = getBranchRef(currentBranch);

        TreeMap<String, String> parentCommitTree = getCommit(parentRef).getTree();
        TreeMap<String, String> newCommitTree = new TreeMap<>();
        newCommitTree.putAll(parentCommitTree);

        for (String stagedFilename : plainFilenamesIn(STAGING_DIR)) {
            File stagedFile = join(STAGING_DIR, stagedFilename);
            byte[] stagedFileContents = readContents(stagedFile);
            String newHash = sha1(stagedFileContents);
            File blobFile = join(BLOBS_DIR, newHash);

            writeContents(blobFile, stagedFileContents);

            newCommitTree.put(stagedFilename, newHash);
            stagedFile.delete();
        }

        long currentTimestamp = new Date().getTime() / 1000;
        createCommit(currentBranch, message, currentTimestamp, newCommitTree, parentRef);
    }

    public static void logHeadHistory() {
        String parentRef = getBranchRef(getCurrentBranch());

        while (parentRef != null) {
            Commit currentCommit = getCommit(parentRef);
            Date commitDate = new Date(currentCommit.getTimestamp() * 1000);
            String parent2Ref = currentCommit.getParent2Ref();

            System.out.println("===");
            System.out.println("commit " + parentRef);
            if (parent2Ref != null) {
                System.out.println("Merge: " + parentRef.substring(0, 7) + " " + parent2Ref.substring(0, 7));
            }
            System.out.println("Date: " + SIMPLE_DATE_FORMAT.format(commitDate));
            System.out.println(currentCommit.getMessage());
            System.out.println();

            parentRef = currentCommit.getParent1Ref();
        }
    }

    private static String getCurrentBranch() {
        return readContentsAsString(HEAD);
    }

    private static String getBranchRef(String branchName) {
        File branchFile = join(REFS_DIR, branchName);
        return readContentsAsString(branchFile);
    }

    private static Commit getCommit(String hash) {
        File commitFile = join(COMMIT_DIR, hash);
        return readObject(commitFile, Commit.class);
    }

    private static void createCommit(String branchName, String message, long timestamp, TreeMap<String, String> tree, String parent1Ref) {
        Commit commit = new Commit(message, timestamp, tree, parent1Ref);
        String commitHash = sha1(serialize(commit));

        File commitFile = join(COMMIT_DIR, commitHash);
        File branchFile = join(REFS_DIR, branchName);

        writeObject(commitFile, commit);
        writeContents(branchFile, commitHash);
    }
}
