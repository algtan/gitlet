package gitlet;

import java.io.File;

import static gitlet.Utils.join;

public class GitletFiles {
    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    /** The blobs directory. */
    public static final File BLOBS_DIR = join(GITLET_DIR, "blobs");
    /** The commits directory. */
    static final File COMMIT_DIR = Utils.join(GITLET_DIR, "commits");
    /** The refs directory (for branches). */
    public static final File REFS_DIR = join(GITLET_DIR, "refs");
    /** The staging area. */
    public static final File STAGING_DIR = join(GITLET_DIR, "staging");
    /** The removal area. */
    public static final File REMOVAL_DIR = join(GITLET_DIR, "removal");
    /** The HEAD reference file. */
    public static final File HEAD = join(GITLET_DIR, "HEAD");
    /** The gitletignore file. */
    public static final File GITLET_IGNORE = join(GITLET_DIR, "gitletignore");
}
