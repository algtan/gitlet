package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.TreeMap;

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

    /* TODO: fill in the rest of this class. */
    public static boolean gitletInitiated() {
        return GITLET_DIR.exists();
    }

    public static void setupPersistence() {
        BLOBS_DIR.mkdirs();
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

        if(newHash.equals(oldHash)) {
            stagedFile.delete();
            return;
        }

        writeContents(stagedFile, fileToAddContents);
    }

    public static void commitStagedChanges(String message) {
        String currentBranch = getCurrentBranch();
        String parentRef = getBranchRef(currentBranch);
        Commit parentCommit = getCommit(parentRef);

        TreeMap<String, String> previousCommitTree = parentCommit.getTree();
        TreeMap<String, String> newCommitTree = new TreeMap<>();
        newCommitTree.putAll(previousCommitTree);

        for (String stagedFilename : plainFilenamesIn(STAGING_DIR)) {
            File stagedFile = join(STAGING_DIR, stagedFilename);
            byte[] stagedFileContents = readContents(stagedFile);
            String newHash = sha1(stagedFileContents);
            newCommitTree.put(stagedFilename, newHash);

            HashFileStructure blobFileStruct = new HashFileStructure(newHash, HashType.BLOB);
            blobFileStruct.getDir().mkdirs();
            File blobFile = blobFileStruct.getFile();
            writeContents(blobFile, stagedFileContents);

            stagedFile.delete();
        }

        long timestamp = new Date().getTime() / 1000;
        createCommit(currentBranch, message, timestamp, newCommitTree, parentRef);
    }

    private static String getCurrentBranch() {
        return readContentsAsString(HEAD);
    }

    private static String getBranchRef(String branchName) {
        File branchFile = join(REFS_DIR, branchName);
        return readContentsAsString(branchFile);
    }

    private static Commit getCommit(String hash) {
        HashFileStructure commitFileStruct = new HashFileStructure(hash, HashType.COMMIT);
        return readObject(commitFileStruct.getFile(), Commit.class);
    }

    private static void createCommit(String branchName, String message, long timestamp, TreeMap<String, String> tree, String parent1Ref) {
        Commit commit = new Commit(message, timestamp, tree, parent1Ref);
        String commitHash = sha1(serialize(commit));
        HashFileStructure commitHashFileStruct = new HashFileStructure(commitHash, HashType.COMMIT);

        commitHashFileStruct.getDir().mkdirs();

        File commitFile = commitHashFileStruct.getFile();
        File branchFile = join(REFS_DIR, branchName);

        writeObject(commitFile, commit);
        writeContents(branchFile, commitHash);
    }

    public static enum HashType {
        BLOB,
        COMMIT
    }
}
