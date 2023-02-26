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

    /* TODO: fill in the rest of this class. */
    public static boolean gitletInitiated() {
        return GITLET_DIR.exists();
    }

    public static void setupPersistence() {
        Commit initialCommit = new Commit();
        byte[] serializedCommit = Utils.serialize(initialCommit);
        String commitHash = sha1(serializedCommit);
        String commitHashDir = commitHash.substring(0, 2);
        String commitHashFile = commitHash.substring(2);
        File initalCommitDir = join(COMMIT_DIR, commitHashDir);

        initalCommitDir.mkdirs();
        BLOBS_DIR.mkdirs();
        REFS_DIR.mkdirs();

        File initialCommitFile = Utils.join(initalCommitDir, commitHashFile);
        File head = Utils.join(GITLET_DIR, "HEAD");
        File master = Utils.join(REFS_DIR, "master");

        try {
            initialCommitFile.createNewFile();
            head.createNewFile();
            master.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Utils.writeObject(initialCommitFile, serializedCommit);
        Utils.writeContents(head, "ref: refs/master");
        Utils.writeContents(master, commitHash);
    }
}
