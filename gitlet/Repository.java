package gitlet;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static gitlet.GitletFiles.*;
import static gitlet.Commit.*;
import static gitlet.Reference.*;
import static gitlet.Utils.*;

/** Represents a gitlet repository.
 *  @author Allen Tan
 */
public class Repository {
    /**
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat(
            "EEE MMM d HH:mm:ss yyyy Z");

    private static final String HEAD_CONFLICT_MARKER = "<<<<<<< HEAD" + System.lineSeparator();

    private static final String GIVEN_BRANCH_CONFLICT_MARKER = "=======" + System.lineSeparator();

    private static final String CONFLICT_MARKER_END = ">>>>>>>" + System.lineSeparator();

    public static boolean gitletInitiated() {
        return GITLET_DIR.exists();
    }

    public static void setupPersistence() {
        BLOBS_DIR.mkdirs();
        COMMIT_DIR.mkdirs();
        REFS_DIR.mkdirs();

        String branchName = "master";
        writeContents(HEAD, branchName);
        createCommit(branchName, "initial commit", 0, new TreeMap<>(), null, null);

        List<String> preexistingFiles = plainFilenamesIn(CWD);
        writeContents(GITLET_IGNORE, String.join("\n", preexistingFiles));
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
        commitStagedChanges(message, null);
    }

    public static void commitStagedChanges(String message, String mergingBranch) {
        if (message.isBlank()) {
            exitWithMessage("Please enter a commit message.");
        }

        String currentBranch = getCurrentBranch();
        String parent1Ref = getBranchRef(currentBranch);
        String parent2Ref = getBranchRef(mergingBranch);

        TreeMap<String, String> parentCommitTree = getCommit(parent1Ref).getTree();
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
            newCommitTree.remove(removedFile);
        }

        STAGING_DIR.delete();
        REMOVAL_DIR.delete();
        long currentTimestamp = new Date().getTime() / 1000;
        createCommit(currentBranch, message, currentTimestamp, newCommitTree,
                parent1Ref, parent2Ref);
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
        stagedFiles = stagedFiles != null ? stagedFiles : new ArrayList<>();
        List<String> removedFiles = plainFilenamesIn(REMOVAL_DIR);
        removedFiles = removedFiles != null ? removedFiles : new ArrayList<>();

        TreeMap<String, String> commitTree = getCommit(getBranchRef(getCurrentBranch())).getTree();
        List<String> cwdFilenames = plainFilenamesIn(CWD);
        List<String> ignoredFilenames = Arrays.asList(readContentsAsString(GITLET_IGNORE)
                .split("\n"));

        int currentBranchIndex = branches.indexOf(currentBranch);
        branches.set(currentBranchIndex, "*" + currentBranch);

        System.out.println("=== Branches ===");
        for (String branch : branches) {
            System.out.println(branch);
        }
        System.out.println();

        System.out.println("=== Staged Files ===");
        if (stagedFiles.size() > 0) {
            for (String stagedFile : stagedFiles) {
                System.out.println(stagedFile);
            }
        }
        System.out.println();

        System.out.println("=== Removed Files ===");
        if (removedFiles.size() > 0) {
            for (String removedFile : removedFiles) {
                System.out.println(removedFile);
            }
        }
        System.out.println();

        System.out.println("=== Modifications Not Staged For Commit ===");
        for (Map.Entry<String, String> fileEntry : commitTree.entrySet()) {
            String filename = fileEntry.getKey();
            String blobHash = fileEntry.getValue();

            if (!removedFiles.contains(filename) && !cwdFilenames.contains(filename)) {
                System.out.println(filename + " (deleted)");
            }

            File cwdFile = join(CWD, filename);
            if (cwdFile.exists() && !sha1(readContents(cwdFile)).equals(blobHash)) {
                System.out.println(filename + " (modified)");
            }
        }
        System.out.println();

        System.out.println("=== Untracked Files ===");
        for (String cwdFilename : cwdFilenames) {
            if (!stagedFiles.contains(cwdFilename) && !commitTree.containsKey(cwdFilename)
                    && !ignoredFilenames.contains(cwdFilename)) {
                System.out.println(cwdFilename);
            }
        }
        System.out.println();
    }

    public static void checkoutFilePerHead(String filename) {
        String headCommitHash = getBranchRef(getCurrentBranch());
        checkoutFilePerCommitId(headCommitHash, filename);
    }

    public static void checkoutFilePerCommitId(String commitId, String filename) {
        commitId = getFullCommitId(commitId);

        if (!join(COMMIT_DIR, commitId).exists()) {
            exitWithMessage("No commit with that id exists.");
        }

        String blobHash = getCommit(commitId).getTree().get(filename);
        if (blobHash == null) {
            exitWithMessage("File does not exist in that commit.");
        }

        writeBlobToCwd(filename, blobHash);
    }

    public static void checkoutBranch(String branchName) {
        checkUntrackedFilesExist();

        if (!plainFilenamesIn(REFS_DIR).contains(branchName)) {
            exitWithMessage("No such branch exists.");
        }

        if (getCurrentBranch().equals(branchName)) {
            exitWithMessage("No need to checkout the current branch.");
        }

        updateCwdPerCommitHash(getBranchRef(branchName));
        clearStagingArea();
        writeContents(HEAD, branchName);
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

    public static void createBranch(String branchName) {
        List<String> branches = plainFilenamesIn(REFS_DIR);
        String branchRef = getBranchRef(getCurrentBranch());

        if (branches.contains(branchName)) {
            exitWithMessage("A branch with that name already exists.");
        }

        File branchFile = join(REFS_DIR, branchName);
        writeContents(branchFile, branchRef);
    }

    public static void removeBranch(String branchName) {
        if (!plainFilenamesIn(REFS_DIR).contains(branchName)) {
            exitWithMessage("A branch with that name does not exist.");
        }

        if (getCurrentBranch().equals(branchName)) {
            exitWithMessage("Cannot remove the current branch.");
        }

        join(REFS_DIR, branchName).delete();
    }

    public static void resetToCommitId(String commitId) {
        checkUntrackedFilesExist();

        commitId = getFullCommitId(commitId);

        if (!join(COMMIT_DIR, commitId).exists()) {
            exitWithMessage("No commit with that id exists.");
        }

        updateCwdPerCommitHash(commitId);
        clearStagingArea();

        writeContents(join(REFS_DIR, getCurrentBranch()), commitId);
    }

    public static void mergeToCurrentBranch(String branchName) {
        checkForMergeErrors(branchName);

        boolean mergeConflictExists = false;

        String currentBranch = getCurrentBranch();
        List<String> commitHashes = plainFilenamesIn(COMMIT_DIR);
        GitletGraph commitGraph = new GitletGraph(commitHashes);
        GitletPaths paths = new GitletPaths(commitGraph, branchName);
        String splitPointHash = paths.getSplitPointHash();

        int splitPointVertex = paths.getSplitPointVertex();
        if (splitPointVertex == paths.getMergingBranchStartingVertex()) {
            exitWithMessage("Given branch is an ancestor of the current branch.");
        }

        if (splitPointVertex == paths.getCurrentBranchStartingVertex()) {
            checkoutBranch(branchName);
            System.out.println("Current branch fast-forwarded.");
            return;
        }

        TreeMap<String, String> splitPointTree = getCommit(splitPointHash).getTree();
        TreeMap<String, String> currentBranchTree = getCommit(getBranchRef(currentBranch))
                .getTree();
        TreeMap<String, String> mergingBranchTree = getCommit(getBranchRef(branchName)).getTree();

        Set<String> mergingFilenames = new HashSet<>();
        mergingFilenames.addAll(currentBranchTree.keySet());
        mergingFilenames.addAll(mergingBranchTree.keySet());

        for (String filename : mergingFilenames) {
            String splitPointBlob = splitPointTree.get(filename);
            String currentBranchBlob = currentBranchTree.get(filename);
            String mergingBranchBlob = mergingBranchTree.get(filename);

            if (Objects.equals(splitPointBlob, currentBranchBlob) && mergingBranchBlob == null) {
                removeFile(filename);
                continue;
            }

            if (Objects.equals(splitPointBlob, currentBranchBlob)
                    && !Objects.equals(splitPointBlob, mergingBranchBlob)) {
                writeBlobToCwd(filename, mergingBranchBlob);
                addFiletoStaging(filename);
                continue;
            }

            if (!Objects.equals(splitPointBlob, currentBranchBlob)
                    && !Objects.equals(splitPointBlob, mergingBranchBlob)
                    && !Objects.equals(currentBranchBlob, mergingBranchBlob)) {
                String currentBranchBlobContents = currentBranchBlob != null
                        ? readContentsAsString(join(BLOBS_DIR, currentBranchBlob)) : "";
                String mergingBranchBlobContents = mergingBranchBlob != null
                        ? readContentsAsString(join(BLOBS_DIR, mergingBranchBlob)) : "";

                String conflictingFileContents = HEAD_CONFLICT_MARKER + currentBranchBlobContents
                        + GIVEN_BRANCH_CONFLICT_MARKER + mergingBranchBlobContents
                        + CONFLICT_MARKER_END;

                writeContents(join(CWD, filename), conflictingFileContents);
                addFiletoStaging(filename);
                mergeConflictExists = true;
            }
        }

        String mergedCommitMsg = "Merged " + branchName + " into " + currentBranch + ".";
        commitStagedChanges(mergedCommitMsg, branchName);

        if (mergeConflictExists) {
            System.out.println("Encountered a merge conflict.");
        }
    }

    private static void createCommit(String branchName, String message,
                                     long timestamp, TreeMap<String, String> tree,
                                     String parent1Ref, String parent2Ref) {
        Commit commit = new Commit(message, timestamp, tree, parent1Ref, parent2Ref);
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
            System.out.println("Merge: "
                    + parent1Ref.substring(0, 7) + " " + parent2Ref.substring(0, 7));
        }
        System.out.println("Date: " + SIMPLE_DATE_FORMAT.format(commitDate));
        System.out.println(commit.getMessage());
        System.out.println();
    }

    private static void updateCwdPerCommitHash(String commitHash) {
        TreeMap<String, String> branchTree = getCommit(commitHash).getTree();
        for (Map.Entry<String, String> fileEntry : branchTree.entrySet()) {
            String filename = fileEntry.getKey();
            String blobHash = fileEntry.getValue();
            writeBlobToCwd(filename, blobHash);
        }

        List<String> ignoredFilenames = Arrays.asList(readContentsAsString(GITLET_IGNORE)
                .split("\n"));
        List<String> updatedCwdFilenames = plainFilenamesIn(CWD);
        for (String filename : updatedCwdFilenames) {
            if (!ignoredFilenames.contains(filename) && !branchTree.containsKey(filename)) {
                join(CWD, filename).delete();
            }
        }
    }

    private static void clearStagingArea() {
        List<String> stagedFiles = plainFilenamesIn(STAGING_DIR);
        stagedFiles = stagedFiles != null ? stagedFiles : new ArrayList<>();
        List<String> removedFiles = plainFilenamesIn(REMOVAL_DIR);
        removedFiles = removedFiles != null ? removedFiles : new ArrayList<>();

        stagedFiles.forEach(filename -> join(STAGING_DIR, filename).delete());
        removedFiles.forEach(filename -> join(REMOVAL_DIR, filename).delete());

        STAGING_DIR.delete();
        REMOVAL_DIR.delete();
    }

    private static void checkUntrackedFilesExist() {
        List<String> stagedFiles = plainFilenamesIn(STAGING_DIR);
        stagedFiles = stagedFiles != null ? stagedFiles : new ArrayList<>();

        TreeMap<String, String> commitTree = getCommit(getBranchRef(getCurrentBranch())).getTree();
        List<String> cwdFilenames = plainFilenamesIn(CWD);
        List<String> ignoredFilenames = Arrays.asList(readContentsAsString(GITLET_IGNORE)
                .split("\n"));

        for (String cwdFilename : cwdFilenames) {
            if (!stagedFiles.contains(cwdFilename) && !commitTree.containsKey(cwdFilename)
                    && !ignoredFilenames.contains(cwdFilename)) {
                exitWithMessage("There is an untracked file in the way;"
                        + " delete it, or add and commit it first.");
            }
        }
    }

    private static String getFullCommitId(String commitId) {
        if (commitId.length() != UID_LENGTH) {
            String shortenedId = commitId;
            commitId = plainFilenamesIn(COMMIT_DIR).stream()
                    .filter(commit -> commit.startsWith(shortenedId))
                    .findFirst()
                    .orElse(shortenedId);
        }
        return commitId;
    }

    private static void checkForMergeErrors(String branchName) {
        checkUntrackedFilesExist();

        if (!plainFilenamesIn(REFS_DIR).contains(branchName)) {
            exitWithMessage("A branch with that name does not exist.");
        }

        if (getCurrentBranch().equals(branchName)) {
            exitWithMessage("Cannot merge a branch with itself.");
        }

        List<String> stagedFiles = plainFilenamesIn(STAGING_DIR);
        stagedFiles = stagedFiles != null ? stagedFiles : new ArrayList<>();
        List<String> removedFiles = plainFilenamesIn(REMOVAL_DIR);
        removedFiles = removedFiles != null ? removedFiles : new ArrayList<>();
        if (stagedFiles.size() > 0 || removedFiles.size() > 0) {
            exitWithMessage("You have uncommitted changes.");
        }
    }
}
