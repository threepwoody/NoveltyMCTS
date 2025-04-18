package ai.nodes;

public class ListNode<T> implements Poolable<ListNode<T>> {

    /** The key stored in this node. */
    private T key;

    /** The next list node. */
    private ListNode<T> next;

    /** Returns the key stored in this list node. */
    public T getKey() {
        return key;
    }

    /** Returns the next list node. */
    @Override
    public ListNode<T> getNext() {
        return next;
    }

    /** Sets the key stored in this list node. */
    public void setKey(T key) {
        this.key = key;
    }

    /** Sets the next list node. */
    @Override
    public void setNext(ListNode<T> next) {
        this.next = next;
    }

}