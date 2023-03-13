package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.TreeMap;

import static gitlet.GitletFiles.*;
import static gitlet.Utils.*;

/** Represents a gitlet commit object.
 *  @author Allen Tan
 */
public class Commit implements Serializable, Dumpable {
    /**
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

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
    /** The 2nd parent Commit (as reference to a Commit object) of this Commit as the result of a
     * 'merge'. */
    private transient Commit parent2Commit;

    public Commit(String message, long timestamp, TreeMap<String, String> tree,
                  String parent1Ref, String parent2Ref) {
        this.message = message;
        this.timestamp = timestamp;
        this.tree = tree;
        this.parent1Ref = parent1Ref;
        this.parent2Ref = parent2Ref;
    }

    public String getMessage() {
        return message;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public TreeMap<String, String> getTree() {
        return tree;
    }

    public String getParent1Ref() {
        return parent1Ref;
    }

    public String getParent2Ref() {
        return parent2Ref;
    }

    public Commit getParent1Commit() {
        return parent1Commit;
    }

    public void setParent1Commit(Commit parent1Commit) {
        this.parent1Commit = parent1Commit;
    }

    public Commit getParent2Commit() {
        return parent2Commit;
    }

    public void setParent2Commit(Commit parent2Commit) {
        this.parent2Commit = parent2Commit;
    }

    @Override
    public void dump() {
        System.out.println("message: " + this.message);
        System.out.println("timestamp: " + this.timestamp);
        System.out.println("tree:" + this.tree.toString());
        System.out.println("parent1Ref " + this.parent1Ref);
        System.out.println("parent2Ref " + this.parent2Ref);
    }

    public static Commit getCommit(String hash) {
        File commitFile = join(COMMIT_DIR, hash);
        return readObject(commitFile, Commit.class);
    }
}
