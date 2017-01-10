package proj2.editor;

import javafx.scene.text.Text;
import java.util.ArrayList;

/**
 * Created by Aneesh Khera on 2/26/16.
 *
 * Class Specifically for storing Text Objects
 * Optimized for helping keep track of cursor
 *
 */

public class LinkedListDeque {

    /** Parts of this Node class is from Professor Hug's Lecture 5
     */
    public class Node {

        protected Text item;
        protected Node prev;
        protected Node next;

        /**Initialization of Node for implementing doubly linked list*/
        Node(Text i, Node p, Node n) { // credits to lecture 5 vid 2
            item = i;
            prev = p;
            next = n;
        }
    }

    ArrayList<Node> newlines = new ArrayList<>();

    Node[] u = new Node[1];
    Node[] r = new Node[1];

    Stacker<Node[]> undoHolder = new Stacker<>();
    Stacker<Node[]> redoHolder = new Stacker<>();

    private Node sentinel;
    private Node position;
    private Node sol;
    private int size;

    public LinkedListDeque() {
        size = 0;
        sentinel = new Node(null, null, null);
        insertLink(sentinel, sentinel, sentinel);
        position = sentinel;
        sol = sentinel;
    }

    /**Private helper method for connecting a new Node between left and right*/
    private void insertLink(Node left, Node mid, Node right) {
        left.next = mid;
        right.prev = mid;
    }

    private void deleteLink(Node left, Node right) {
        left.next = right;
        right.prev = left;
    }

    public void addLast(Text item) {
        size += 1;
        Node last = new Node(item, sentinel.prev, sentinel);
        insertLink(sentinel.prev, last, sentinel);
    }

    public void addAny(Text item) {
        size += 1;
        Node added = new Node(item, position, position.next);
        insertLink(position, added, position.next);
        position = added;
        addTracker(new Node(new Text("insert"), null, null));
    }

    public void addnewline() {
        newlines.add(sol.prev);
    }

    public Text removeAny() {
        addTracker(new Node(new Text("backspace"), null, null));

        size -= 1;
        Node removed = position;
        position.prev.next = position.next;
        position.next.prev = position.prev;
        position = position.prev;

        return removed.item;
    }

    /**Method to add information to Stack*/
    public void addTracker(Node action) {
        //Stores the curr, prev, and next node
        //information which is to be used later
        Node[] tracker = {action, position.prev, position, position.next};
        undoHolder.addTop(tracker);
    }

    public void moveBack() {
        if (position.prev != sentinel.prev) {
            position = position.prev;
        }
    }

    public void moveFor() {
        if (position.next != sentinel) {
            position = position.next;
        }
    }

    public Text preCurse() {
        if (position.prev != sentinel) {
            return position.prev.item;
        }
        return null;
    }

    public Boolean isSentinel() {
        return position == sentinel;
    }


    public Text postCurse() {
        if (position.next != sentinel) {
            return position.next.item;
        }
        return null;
    }

    public void resetlines() {
        newlines.clear();
    }

    public boolean isLast() {
        return position.next == sentinel;
    }

    public Text cursorPosition() {
        return position.item;
    }

    public boolean isEmpty() {
        return (size == 0);
    }

    public int size() {
        return size;
    }

    public Text removeLast() {
        if (isEmpty()) {
            return null;
        }
        size -= 1;
        Node oldBack = sentinel.prev;
        deleteLink(sentinel.prev.prev, sentinel);
        return oldBack.item;
    }

    public Text get(int index) { //must use iteration
        if (index >= size || index < 0 || isEmpty()) {
            return null;
        }
        Node curr;
        if (index < size / 2) {
            curr = sentinel.next;
            int count = 0;
            while (count != index) {
                curr = curr.next;
                count += 1;
            }
        } else {
            curr = sentinel;
            int count = size;
            while (count != index) {
                curr = curr.prev;
                count -= 1;
            }
        }
        return curr.item;
    }

    public Text next() {
        if (sol == sentinel) {
            return null;
        }
        Text x = sol.item;
        sol = sol.next;
        return x;
    }

    public void resetSol() {
        if (size != 0) {
            sol = sentinel.next;
        }
    }

    public boolean findClick(int i, int xcoor, int height) {
        if (i > arraySize() - 1) {
            lastSpot();
            return false;
        }
        Node line = newlines.get(i);
        boolean notlast;
        int widthcounter = 5;
        boolean rightH = true;
        while (widthcounter + (int) Math.round(line.item.getLayoutBounds().getWidth())
                < xcoor && rightH) {
            widthcounter += (int) Math.round(line.item.getLayoutBounds().getWidth());
            if (line.next.item != null) {
                if (line.next.item.getY() / height > i) {
                    rightH = false;
                } else {
                    line = line.next;
                }
            } else {
                rightH = false;
            }
        }
        if (Math.abs(xcoor - line.item.getX()) > Math.abs(line.item.getX()
                + line.item.getLayoutBounds().getWidth() - xcoor)) {
            position = line;
            notlast = false;
        } else {
            position = line.prev;
            notlast = true;
        }
        return notlast;
    }

    public void moveUpDown(int i) {
        if (i <= arraySize() - 1) {
            position = newlines.get(i).prev;
        } else {
            position = sentinel.prev;
        }
    }

    public void lastSpot() {
        position = sentinel.prev;
    }

    public Text getLastSpot() {
        return sentinel.prev.item;
    }

    public int arraySize() {
        return newlines.size();
    }

    /**Implementation for stack below*/

    public boolean undoIsEmpty() {
        return undoHolder.isEmpty();
    }

    public boolean redoIsEmpty() {
        return redoHolder.isEmpty();
    }

    public String getUndoAction() {
        if (!undoHolder.isEmpty()) {
            u = undoHolder.peekTop();
            String action = u[0].item.getText();
            return action;
        }
        return null;
    }

    public String getRedoAction() {
        if (!redoHolder.isEmpty()) {
            r = redoHolder.peekTop();
            String action = r[0].item.getText();
            return action;
        }
        return null;
    }

    public void redoClear() {
        redoHolder.clear();
    }

    public Text undo() {
        //credits to idea of stack from discussion 3
        //use the stored nodes of cursor position to
        //be manipulated whenever undo is called.
        u = undoHolder.popBot();
        String action = u[0].item.getText();
        Node prev = u[1];
        Node curr = u[2];
        Node next = u[3];
        redoHolder.addTop(u);
        if (action.equals("insert")) {
            size -= 1;
            prev.next = next;
            next.prev = prev;
            position = prev;
        } else {
            size += 1;
            prev.next = curr;
            next.prev = curr;
            position = curr;
        }
        return curr.item;
    }

    public Text redo() {
        r = redoHolder.popBot();
        String action = r[0].item.getText();
        Node prev = r[1];
        Node curr = r[2];
        Node next = r[3];
        undoHolder.addTop(r);
        if (action.equals("insert")) {
            size += 1;
            prev.next = curr;
            next.prev = curr;
            position = curr;
        } else {
            size -= 1;
            prev.next = next;
            next.prev = prev;
            position = prev;
        }
        return curr.item;
    }
}
