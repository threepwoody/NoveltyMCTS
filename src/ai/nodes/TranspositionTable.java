package ai.nodes;

import static java.lang.Math.abs;

/** A hash table of nodes representing board configurations. */
public class TranspositionTable {

    private SearchNodeBuilder builder;
    /** ListNodes used to build child lists for SearchNodes. */
    private Pool<ListNode<SearchNode>> listNodes;
    /** Search nodes. */
    private Pool<SearchNode> searchNodes;
    /** The hash table itself. */
    private ListNode[] table;

    public TranspositionTable(int size, SearchNodeBuilder builder) {
        table = new ListNode[size];
        searchNodes = new Pool<SearchNode>();
        for (int i = 0; i < size; i++) {
            searchNodes.free(builder.buildSearchNode());
        }
        listNodes = new Pool<ListNode<SearchNode>>();
        for (int i = 0; i < 5 * size; i++) {
            listNodes.free(new ListNode<SearchNode>());
        }
        this.builder = builder;
    }

    /** Slow -- for testing only. */
    public int dagSize(SearchNode root) {
        java.util.Set<SearchNode> visited = new java.util.HashSet<SearchNode>();
        return dagSize(root, visited);
    }

    protected int dagSize(SearchNode node, java.util.Set<SearchNode> visited) {
        if (visited.contains(node)) {
            return 0;
        } else {
            visited.add(node);
            int sum = 1;
            for(SearchNode childNode : node.getChildNodes()) {
                sum += dagSize(childNode, visited);
            }
            return sum;
        }
    }

    /** Returns the node associated with hash, or null if there is no such node. */
    public synchronized SearchNode findIfPresent(long hash) {
        int slot = (int)(abs(hash % table.length));
        if (slot < 0) {
            slot = table.length - 1;
        }
        ListNode<SearchNode> listNode = table[slot];
        while (listNode != null) {
            if (listNode.getKey().getHash() == hash) {
                return listNode.getKey();
            }
            listNode = listNode.getNext();
        }
        return null;
    }

    /**
     * Returns the node associated with hash in the table, if any. If not,
     * allocates and returns a new node from the pool. If no nodes are available
     * in the pool, returns null.
     */
    public synchronized SearchNode findOrAllocate(long hash, int numberOfColors, int numberOfLegalMoves) {
        int slot = (int)(abs(hash % table.length));
        if (slot < 0) {
            slot = table.length - 1;
        }
        ListNode<SearchNode> listNode = table[slot];
        while (listNode != null) {
            if (listNode.getKey().getHash() == hash) {
                return listNode.getKey();
            }
            listNode = listNode.getNext();
        }
        // Didn't find it; allocate a node if possible
        if (searchNodes.isEmpty() | listNodes.isEmpty()) {
            return null;
        }
        ListNode<SearchNode> newListNode = listNodes.allocate();
        SearchNode newNode = searchNodes.allocate();
        newNode.reset(hash, builder, numberOfColors, numberOfLegalMoves);
        newListNode.setKey(newNode);
        newListNode.setNext(table[slot]);
        table[slot] = newListNode;
        return newNode;
    }

    /** Marks all nodes reachable from root, so they will survive sweep(). */
    public void markNodesReachableFrom(SearchNode root) {
        if (!root.isMarked()) {
            root.setMarked(true);
            for(SearchNode node : root.getChildNodes()) {
                markNodesReachableFrom(node);
            }
        }
    }

    /** Slow -- for testing only. */
    public int numberOfNodesAtDepth(SearchNode root, int depth) {
        java.util.Set<SearchNode> visited = new java.util.HashSet<SearchNode>();
        return numberOfNodesAtDepth(root, visited, depth, 0);
    }

    protected int numberOfNodesAtDepth(SearchNode root, java.util.Set<SearchNode> visited, int targetdepth, int currentdepth) {
        if (visited.contains(root) || currentdepth > targetdepth) {
            return 0;
        } else if(currentdepth==targetdepth) {
            visited.add(root);
            return 1;
        } else {
            visited.add(root);
            int sum = 0;
            for(SearchNode node : root.getChildNodes()) {
                sum += numberOfNodesAtDepth(node, visited, targetdepth, currentdepth+1);
            }
            return sum;
        }
    }

    /**
     * After markNodesUnreachableFrom(), returns all unmarked SearchNodes (and
     * associated ListNodes) to their pools.
     */
    public void sweep() {
        for (int i = 0; i < table.length; i++) {
            if (table[i] != null) {
                ListNode<SearchNode> prev = null;
                ListNode<SearchNode> node = table[i];
                while (node != null) {
                    SearchNode searchNode = node.getKey();
                    if (searchNode.isMarked()) {
                        // Clear the mark for the next pass
                        searchNode.setMarked(false);
                        prev = node;
                        node = node.getNext();
                    } else {
                        // Reclaim the ai.nodes.SearchNode itself
                        searchNodes.free(searchNode);
                        // Reclaim this ai.nodes.ListNode
                        node = listNodes.free(node);
                        if (prev == null) {
                            table[i] = node;
                        } else {
                            prev.setNext(node);
                        }
                    }
                }
            }
        }
    }

}
