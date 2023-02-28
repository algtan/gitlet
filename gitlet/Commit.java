package gitlet;

// TODO: any imports you need here

import java.io.File;
import java.io.Serializable;
import java.util.Date;
import java.util.TreeMap;

import static gitlet.Repository.*;

/** Represents a gitlet commit object.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author TODO
 */
public class Commit implements Serializable {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    static final File COMMIT_DIR = Utils.join(GITLET_DIR, "commits");

    /** The message of this Commit. */
    private String message;
    /** The timestamp of this Commit. */
    private long timestamp;
    /** The tree of this Commit. */
    private TreeMap<String, String> tree;
    /** The 1st parent Commit (as a reference string) of this Commit. */
    private String parent1Ref;
    /** The 2nd parent Commit (as a reference string) of this Commit as the result of a 'merge'. */
    private String parent2Ref;
    /** The 1st parent Commit (as reference to a Commit object) of this Commit. */
    private transient Commit parent1Commit;
    /** The 2nd parent Commit (as reference to a Commit object) of this Commit as the result of a 'merge'. */
    private transient Commit parent2Commit;

    /* TODO: fill in the rest of this class. */
    public Commit() {
        this.message = "initial commit";
        this.timestamp = 0;
    }
}
