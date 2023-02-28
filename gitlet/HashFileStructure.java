package gitlet;

import java.io.File;

import static gitlet.Commit.COMMIT_DIR;
import static gitlet.Repository.*;
import static gitlet.Utils.join;

public class HashFileStructure {
    private final String dirName;
    private final String fileName;
    private final File dir;
    private final File file;

    public HashFileStructure(String hash, HashType hashType) {
        this.dirName = hash.substring(0, 2);
        this.fileName = hash.substring(2);
        this.dir = hashType == HashType.BLOB ? join(BLOBS_DIR, dirName) : join(COMMIT_DIR, dirName);
        this.file = join(dir, fileName);
    }

    public String getDirName() {
        return dirName;
    }

    public String getFileName() {
        return fileName;
    }

    public File getFile() {
        return file;
    }

    public File getDir() {
        return dir;
    }
}
