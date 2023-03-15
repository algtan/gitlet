package gitlet;

import java.util.*;

import static gitlet.Reference.*;

public class GitletPaths {
    private final boolean[] currentBranchMarked;
    private final boolean[] mergingBranchMarked;
    private final int currentBranchStartingVertex;
    private final int mergingBranchStartingVertex;
    private int splitPointVertex;
    private String splitPointHash;

    public GitletPaths(GitletGraph gitletGraph, String mergingBranch) {
        int V = gitletGraph.getV();
        currentBranchMarked = new boolean[V];
        mergingBranchMarked = new boolean[V];

        String currentBranchStartingRef = getBranchRef(getCurrentBranch());
        currentBranchStartingVertex = gitletGraph.getVertexMap().get(currentBranchStartingRef);
        traverseCurrentBranch(gitletGraph, currentBranchStartingVertex);

        String mergingBranchStartingRef = getBranchRef(mergingBranch);
        mergingBranchStartingVertex = gitletGraph.getVertexMap().get(mergingBranchStartingRef);
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
        splitPointVertex = v;
        splitPointHash = gitletGraph.getCommitHashes().get(v);
    }

    public int getCurrentBranchStartingVertex() {
        return currentBranchStartingVertex;
    }

    public int getMergingBranchStartingVertex() {
        return mergingBranchStartingVertex;
    }

    public int getSplitPointVertex() {
        return splitPointVertex;
    }

    public String getSplitPointHash() {
        return splitPointHash;
    }
}

