package gitlet;

import java.io.File;

import static gitlet.GitletFiles.*;
import static gitlet.Utils.*;

public class Reference {
    public static String getCurrentBranch() {
        return readContentsAsString(HEAD);
    }

    public static String getBranchRef(String branchName) {
        if (branchName == null) {
            return null;
        }
        File branchFile = join(REFS_DIR, branchName);
        return readContentsAsString(branchFile);
    }
}
