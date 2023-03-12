package gitlet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static gitlet.Commit.*;

public class GitletGraph {
    private final int V;
    private List<String> commitHashes;
    private HashMap<String, Integer> vertexMap;
    private List<Integer>[] adj;

    public GitletGraph(List<String> commitHashes) {
        V = commitHashes.size();
        this.commitHashes = commitHashes;
        vertexMap = new HashMap<>();
        adj = (List<Integer>[]) new ArrayList[V];

        commitHashes.forEach(hash -> vertexMap.put(hash, vertexMap.size()));

        for (int i = 0; i < V; i++) {
            String commitHash = commitHashes.get(i);
            Commit commit = getCommit(commitHash);
            String parent1Ref = commit.getParent1Ref();
            String parent2Ref = commit.getParent2Ref();

            adj[i] = new ArrayList<Integer>();
            if (parent1Ref != null) {
                addEdge(i, vertexMap.get(parent1Ref));
            }
            if (parent2Ref != null) {
                addEdge(i, vertexMap.get(parent2Ref));
            }
        }
    }

    private void addEdge(int start, int end) {
        adj[start].add(end);
    }

    public Iterable<Integer> adj(int v) {
        return adj[v];
    }

    public List<String> getCommitHashes() {
        return commitHashes;
    }

    public int getV() {
        return V;
    }

    public HashMap<String, Integer> getVertexMap() {
        return vertexMap;
    }
}
