package gitlet;

import java.io.File;
import java.io.IOException;

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

        // grab commit from head
        // create Commit object from .gitlet folder
        // create an original TreeMap from that commit
        // create a staging TreeMap that will be used for the new commit

        // compare staged file's filename and SHA-1 hash with the original TreeMap object
        // if either filename or SHA-1 hash don't match in original TreeMap, add file to staging folder
        // otherwise, remove file from staging (if one is there)

        // update staging TreeMap whether file was added to staging

    public static enum HashType {
        BLOB,
        COMMIT
    }
}
