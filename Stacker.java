package proj2.editor;

import java.util.LinkedList;

/**
 * Created by Aneesh Khera on 3/4/16.
 *
 * Created Stack like features for a
 * java Linked List to help with the
 * managing of Undo and Redo commands
 * Added for simplicity and clarity
 *
 * Stacks Arrays which hold
 * curr node, prev node, next node
 * and action on the node
 *
 * Can't use java stack because it
 * does not have a way to max the
 * at size 100.
 *
 * Discussed idea with lab assistants
 * and other students about how to
 * make the implementation more efficient
 * - which led to idea of storing
 * Arrays of nodes in a LinkedList.
 *
 */
public class Stacker<Array> {

    LinkedList<Array> stack;

    public Stacker() {
        stack = new LinkedList<>();
    }

    public boolean isEmpty() {
        return stack.size() == 0;
    }

    public Array popBot() {
        return stack.removeLast();
    }

    public Array peekTop() {
        return stack.peekLast();
    }

    public boolean isFull() {
        return stack.size() >= 100;
    }
    public void addTop(Array x) {
        if (isFull()) {
            stack.removeFirst();
        }
        stack.addLast(x);
    }

    public void clear() {
        stack.clear();
    }
}
