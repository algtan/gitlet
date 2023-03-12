package gitlet;

import java.util.*;

import static gitlet.Reference.*;

public class GitletPaths {
    private final boolean[] currentBranchMarked;
    private final boolean[] mergingBranchMarked;
    private String splitPoint;

    public GitletPaths(GitletGraph gitletGraph, String mergingBranch) {
        int V = gitletGraph.getV();
        currentBranchMarked = new boolean[V];
        mergingBranchMarked = new boolean[V];

        String currentBranchStartingRef = getBranchRef(getCurrentBranch());
        int currentBranchStartingVertex = gitletGraph.getVertexMap().get(currentBranchStartingRef);
        traverseCurrentBranch(gitletGraph, currentBranchStartingVertex);

        String mergingBranchStartingRef = getBranchRef(mergingBranch);
        int mergingBranchStartingVertex = gitletGraph.getVertexMap().get(mergingBranchStartingRef);
        findSplitPoint(gitletGraph, mergingBranchStartingVertex);
    }

    private void traverseCurrentBranch(GitletGraph gitletGraph, int v) {
        currentBranchMarked[v] = true;
        for (int w : gitletGraph.adj(v)) {
            if (!currentBranchMarked[w]) {
                traverseCurrentBranch(gitletGraph, w);
            }
        }
    }

    private void findSplitPoint(GitletGraph gitletGraph, int v) {
        Queue<Integer> fringe = new LinkedList<>();
        fringe.add(v);
        mergingBranchMarked[v] = true;
        while (!currentBranchMarked[v]) {
            v = fringe.remove();
            for (int w : gitletGraph.adj(v)) {
                if (!mergingBranchMarked[w]) {
                    fringe.add(w);
                    mergingBranchMarked[w] = true;
                }
            }
        }
        splitPoint = gitletGraph.getCommitHashes().get(v);
    }

    public String getSplitPoint() {
        return splitPoint;
    }
}

