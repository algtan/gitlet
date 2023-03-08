package gitlet;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;

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
    /** The removal area. */
    public static final File REMOVAL_DIR = join(GITLET_DIR, "removal");
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

        File fileToAdd = join(CWD, filename);
        if (!fileToAdd.exists()) {
            exitWithMessage("File does not exist.");
        }
        byte[] fileToAddContents = readContents(fileToAdd);
        String newHash = sha1(fileToAddContents);
        File stagedFile = join(STAGING_DIR, filename);

        if (newHash.equals(oldHash)) {
            stagedFile.delete();
            join(REMOVAL_DIR, filename).delete();
            return;
        }

        writeContents(stagedFile, fileToAddContents);
    }

    public static void commitStagedChanges(String message) {
        if (message.isBlank()) {
            exitWithMessage("Please enter a commit message.");
        }

        String currentBranch = getCurrentBranch();
        String parentRef = getBranchRef(currentBranch);

        TreeMap<String, String> parentCommitTree = getCommit(parentRef).getTree();
        TreeMap<String, String> newCommitTree = new TreeMap<>();
        newCommitTree.putAll(parentCommitTree);

        List<String> stagedFiles = plainFilenamesIn(STAGING_DIR);
        stagedFiles = stagedFiles != null ? stagedFiles : new ArrayList<>();
        List<String> removedFiles = plainFilenamesIn(REMOVAL_DIR);
        removedFiles = removedFiles != null ? removedFiles : new ArrayList<>();

        if (stagedFiles.size() == 0 && removedFiles.size() == 0) {
            exitWithMessage("No changes added to the commit.");
        }

        for (String stagedFilename : stagedFiles) {
            File stagedFile = join(STAGING_DIR, stagedFilename);
            byte[] stagedFileContents = readContents(stagedFile);
            String newHash = sha1(stagedFileContents);
            File blobFile = join(BLOBS_DIR, newHash);

            writeContents(blobFile, stagedFileContents);

            newCommitTree.put(stagedFilename, newHash);
            stagedFile.delete();
        }

        for (String removedFile : removedFiles) {
            join(REMOVAL_DIR, removedFile).delete();
        }

        STAGING_DIR.delete();
        REMOVAL_DIR.delete();
        long currentTimestamp = new Date().getTime() / 1000;
        createCommit(currentBranch, message, currentTimestamp, newCommitTree, parentRef);
    }

    public static void logHeadHistory() {
        String parentRef = getBranchRef(getCurrentBranch());

        while (parentRef != null) {
            Commit currentCommit = getCommit(parentRef);

            printCommitInfo(currentCommit, parentRef);

            parentRef = currentCommit.getParent1Ref();
        }
    }

    public static void logAllCommits() {
        List<String> commitHashes = plainFilenamesIn(COMMIT_DIR);
        for (String commitHash : commitHashes) {
            Commit commit = getCommit(commitHash);
            printCommitInfo(commit, commitHash);
        }
    }

    public static void getStatus() {
        String currentBranch = getCurrentBranch();
        List<String> branches = plainFilenamesIn(REFS_DIR);
        List<String> stagedFiles = plainFilenamesIn(STAGING_DIR);
        List<String> removedFiles = plainFilenamesIn(REMOVAL_DIR);

        int currentBranchIndex = branches.indexOf(currentBranch);
        branches.set(currentBranchIndex, "*" + currentBranch);

        System.out.println("=== Branches ===");
        for (String branch : branches) {
            System.out.println(branch);
        }
        System.out.println();

        System.out.println("=== Staged Files ===");
        if (stagedFiles != null) {
            for (String stagedFile : stagedFiles) {
                System.out.println(stagedFile);
            }
        }
        System.out.println();

        System.out.println("=== Removed Files ===");
        if (removedFiles != null) {
            for (String removedFile : removedFiles) {
                System.out.println(removedFile);
            }
        }
        System.out.println();

        System.out.println("=== Modifications Not Staged For Commit ===");
        System.out.println();

        System.out.println("=== Untracked Files ===");
        System.out.println();
    }

    public static void checkoutFilePerHead(String filename) {
        String headCommitHash = getBranchRef(getCurrentBranch());
        checkoutFilePerCommitId(headCommitHash, filename);
    }

    public static void checkoutFilePerCommitId(String commitId, String filename) {
        if (commitId.length() != UID_LENGTH) {
            String shortenedId = commitId;
            commitId = plainFilenamesIn(COMMIT_DIR).stream()
                    .filter(commit -> commit.startsWith(shortenedId))
                    .findFirst()
                    .orElse(shortenedId);
        }

        if (!join(COMMIT_DIR, commitId).exists()) {
            exitWithMessage("No commit with that id exists.");
        }

        String blobHash = getCommit(commitId).getTree().get(filename);
        if (blobHash == null) {
            exitWithMessage("File does not exist in that commit.");
        }

        writeBlobToCwd(filename, blobHash);
    }

    public static void removeFile(String filename) {
        REMOVAL_DIR.mkdirs();

        String parentRef = getBranchRef(getCurrentBranch());
        Commit parentCommit = getCommit(parentRef);
        TreeMap<String, String> parentCommitTree = getCommit(parentRef).getTree();

        if (parentCommitTree.containsKey(filename)) {
            File removedFile = join(REMOVAL_DIR, filename);
            writeContents(removedFile);
            join(CWD, filename).delete();
            return;
        }

        List<String> stagedFiles = plainFilenamesIn(STAGING_DIR);
        if (stagedFiles != null && stagedFiles.contains(filename)) {
            File stagedFile = join(STAGING_DIR, filename);
            stagedFile.delete();
            return;
        }

        exitWithMessage("No reason to remove the file.");
    }

    public static void findCommits(String message) {
        List<String> commitHashes = plainFilenamesIn(COMMIT_DIR);
        List<String> foundCommitHashes = commitHashes.stream()
                .filter(commitHash -> getCommit(commitHash).getMessage().equals(message))
                .collect(Collectors.toList());

        if (foundCommitHashes.size() == 0) {
            exitWithMessage("Found no commit with that message.");
        }

        for (String foundCommitHash : foundCommitHashes) {
            System.out.println(foundCommitHash);
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

    private static void writeBlobToCwd(String filename, String blobHash) {
        File blobFile = join(BLOBS_DIR, blobHash);
        byte[] blobFileContents = readContents(blobFile);
        File cwdFile = join(CWD, filename);

        writeContents(cwdFile, blobFileContents);
    }

    private static void printCommitInfo(Commit commit, String commitHash) {
        Date commitDate = new Date(commit.getTimestamp() * 1000);
        String parent1Ref = commit.getParent1Ref();
        String parent2Ref = commit.getParent2Ref();

        System.out.println("===");
        System.out.println("commit " + commitHash);
        if (parent2Ref != null) {
            System.out.println("Merge: " + parent1Ref.substring(0, 7) + " " + parent2Ref.substring(0, 7));
        }
        System.out.println("Date: " + SIMPLE_DATE_FORMAT.format(commitDate));
        System.out.println(commit.getMessage());
        System.out.println();
    }
}
