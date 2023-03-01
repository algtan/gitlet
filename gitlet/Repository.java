package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.TreeMap;

import static gitlet.Commit.*;
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
        Commit initialCommit = new Commit();
        String commitHash = sha1(serialize(initialCommit));
        HashFileStructure commitHashFileStruct = new HashFileStructure(commitHash, HashType.COMMIT);

        commitHashFileStruct.getDir().mkdirs();
        BLOBS_DIR.mkdirs();
        REFS_DIR.mkdirs();

        File initialCommitFile = commitHashFileStruct.getFile();
        File master = join(REFS_DIR, "master");

        try {
            commitHashFileStruct.getFile().createNewFile();
            HEAD.createNewFile();
            master.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        writeObject(initialCommitFile, initialCommit);
        writeContents(HEAD, "ref: refs/master");
        writeContents(master, commitHash);
    }

    public static void addFiletoStaging(String filename) {
        STAGING_DIR.mkdirs();

        String parentRef = getReference(HEAD);
        HashFileStructure parentCommitFileStruct = new HashFileStructure(parentRef, HashType.COMMIT);
        Commit previousCommit = readObject(parentCommitFileStruct.getFile(), Commit.class);

        String oldHash = previousCommit.getTree().get(filename);
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
        String parentRef = getReference(HEAD);
        HashFileStructure parentCommitFileStruct = new HashFileStructure(parentRef, HashType.COMMIT);
        Commit parentCommit = readObject(parentCommitFileStruct.getFile(), Commit.class);

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
            try {
                blobFile.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            writeContents(blobFile, stagedFileContents);

            stagedFile.delete();
        }

        Commit newCommit = new Commit(message, newCommitTree, parentRef);
        String commitHash = sha1(serialize(newCommit));
        HashFileStructure commitHashFileStruct = new HashFileStructure(commitHash, HashType.COMMIT);

        commitHashFileStruct.getDir().mkdirs();

        File newCommitFile = commitHashFileStruct.getFile();
        File master = join(REFS_DIR, "master");

        try {
            newCommitFile.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        writeObject(newCommitFile, newCommit);
        writeContents(master, commitHash);
    }

    private static String getReference(File file) {
        String ref = readContentsAsString(file);
        if (ref.startsWith("ref: ")) {
            String branchFile = ref.split("ref: refs/")[1];
            File branchRef = join(REFS_DIR, branchFile);
            ref = readContentsAsString(branchRef);
        }

        return ref;
    }

    public static enum HashType {
        BLOB,
        COMMIT
    }
}
