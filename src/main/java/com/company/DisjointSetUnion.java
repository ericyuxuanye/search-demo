package com.company;

/**
 * An implementation of a Disjoint Set Union, allowing near constant time to get the parent and unite two sets.
 */
public class DisjointSetUnion {
    /**
     * Positive represents id, if negative, it is the parent index + 1
     */
    private final int[] array;

    /**
     * Creates a new Disjoint Set Union
     * @param size number of elements this set should hold
     */
    public DisjointSetUnion(int size) {
        array = new int[size];
        for (int i = 0; i < size; i++) {
            array[i] = i;
        }
    }

    /**
     * Get the parent of the node
     * @param x node id
     * @return the parent of the node
     */
    public int get(int x) {
        return array[x] == x ? x : (array[x] = get(array[x]));
    }

    /**
     * Join two sets
     * @param a node id of any node in the first set
     * @param b node id of any node in the second set
     */
    public void unite(int a, int b) {
        a = get(a);
        b = get(b);
        if (a > b) {
            int temp = a;
            a = b;
            b = temp;
        }
        array[b] = a;
    }
}
