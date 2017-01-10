package proj2.editor;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.ScrollBar;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Created by Aneesh Khera on 2/26/16.
 *
 * Project Completed on 3/6/16
 * Functions as a Text Editor, similar to Notepad
 */
public class Editor extends Application {

    private static LinkedListDeque stored = new LinkedListDeque();

    private Rectangle cursor;

    private Scene scene;

    private ScrollBar scrollBar = new ScrollBar();

    private static int margin = 5;

    private int xcurr = margin;
    private int ycurr = 0;

    private int textHeight;

    private int ymin = 0;

    private double yshift;

    private int mouseX;
    private int mouseY;

    private double scrollbarwidth;

    private static String fontName = "Verdana";
    private static int fontSize = 12;

    private static String filename;

    private Group root = new Group();
    private Group textroot = new Group();

    private static int WINDOWWIDTH = 500;
    private static int WINDOWHEIGHT = 500;
    /** An EventHandler to handle keys that get pressed. */
    public class KeyEventHandler implements EventHandler<KeyEvent> {

        int wcurr;
        int hcurr;

        KeyEventHandler(int windowWidth, int windowHeight) {

            wcurr = windowWidth;
            hcurr = windowHeight;

            stored.resetSol();
            for (int c = 0; c < stored.size(); c++) {
                Text curr = stored.next();
                textroot.getChildren().add(curr);
            }
            redisplay();
        }

        /** KeyEvent Handle Method*/
        @Override
        public void handle(KeyEvent keyEvent) {
            if (keyEvent.getEventType() == KeyEvent.KEY_TYPED) {
                // Use the KEY_TYPED event rather than KEY_PRESSED for letter keys, because with
                // the KEY_TYPED event, javafx handles the "Shift" key and associated
                // capitalization.
                String characterTyped = keyEvent.getCharacter();
                if (characterTyped.equals("\r")) {
                    Text enter = new Text("\n");
                    stored.addAny(enter);
                    textroot.getChildren().add(enter);
                    redisplay();
                    keyEvent.consume();
                    stored.redoClear();
                    snapBack();
                } else if (characterTyped.length() > 0
                        && characterTyped.charAt(0) != 8 && !keyEvent.isShortcutDown()) {
                    Text curr = new Text(characterTyped);
                    stored.addAny(curr);
                    textroot.getChildren().add(curr);
                    redisplay();
                    keyEvent.consume();
                    stored.redoClear();
                    snapBack();
                }
            } else if (keyEvent.getEventType() == KeyEvent.KEY_PRESSED) {
                KeyCode code = keyEvent.getCode();
                if (code == KeyCode.BACK_SPACE) {
                    backspace();
                    keyEvent.consume();
                    stored.redoClear();
                    snapBack();
                } else if (code == KeyCode.LEFT) {
                    leftArrow();
                    keyEvent.consume();
                    snapBack();
                } else if (code == KeyCode.RIGHT) {
                    rightArrow();
                    keyEvent.consume();
                } else if (code == KeyCode.UP) {
                    upArrow();
                    keyEvent.consume();
                    snapBack();
                } else if (code == KeyCode.DOWN) {
                    downArrow();
                    keyEvent.consume();
                    snapBack();
                }
                if (keyEvent.isShortcutDown()) {
                    if (code == KeyCode.PLUS || code == KeyCode.EQUALS) {
                        fontSize += 4;
                        redisplay();
                        if (scrollBar.getValue() > scrollBar.getMax()) {
                            scrollBar.setValue(scrollBar.getMax());
                        }
                    } else if (code == KeyCode.MINUS) {
                        fontSize = Math.max(4, fontSize - 4);
                        redisplay();
                        if (scrollBar.getValue() > scrollBar.getMax()) {
                            scrollBar.setValue(scrollBar.getMax());
                        }
                    } else if (code == KeyCode.Z) {
                        undoHelper();
                        redisplay();
                        snapBack();
                    } else if (code == KeyCode.Y) {
                        redoHelper();
                        redisplay();
                        snapBack();
                    } else if (code == KeyCode.S) {
                        save();
                    } else if (code == KeyCode.P) {
                        System.out.println((int) Math.round(cursor.getX()) + ", " + (int) Math.round(cursor.getY()));
                    }
                }
                keyEvent.consume();
            }
        }

        /**Backspace Handle*/
        public void backspace() {
            if (stored.isEmpty() || stored.isSentinel()) {
                //if at left most position or empty page
                return;
            } else if (stored.cursorPosition().getX() == margin && stored.preCurse() == null) {
                //if you're on the first character of the doc
                textroot.getChildren().remove(stored.cursorPosition());
                stored.removeAny();
                redisplay();
            } else if (cursor.getX() == margin) {
                //if you're on the left of the first character of a line
                textroot.getChildren().remove(stored.cursorPosition());
                stored.removeAny();
                redisplay();
            } else if (stored.cursorPosition().getX() == margin) {
                //if on the right of the first character of a line
                int thisx = (int) Math.round(stored.cursorPosition().getX());
                int thisy = (int) Math.round(stored.cursorPosition().getY());
                textroot.getChildren().remove(stored.cursorPosition());
                stored.removeAny();
                redisplay();
                cursor.setX(thisx);
                cursor.setY(thisy);
            } else {  //normal backspace
                textroot.getChildren().remove(stored.cursorPosition());
                stored.removeAny();
                redisplay();
            }
        }

        /** Left Arrow Handler*/
        public void leftArrow() {
            if (cursor.getX() == margin && cursor.getY() == 0) {
                //specific case for if cursor is in top left position
                return;
            } else if (stored.preCurse() != null) {
                if (stored.cursorPosition().getText().equals("\n")) {
                    //move left if on left side on left side of first character on line
                    stored.moveBack();
                    setCursorDisplay(false);
                } else if (stored.cursorPosition().getX() == margin) {
                    //move left if right of first character on line and full line
                    stored.moveBack();
                    cursor.setX(margin);
                } else { //normal move left
                    stored.moveBack();
                    setCursorDisplay(false);
                }
            } else { //specific case for moving cursor to the first spot on the screen.
                if (!stored.isSentinel()) {
                    stored.moveBack();
                    setCursorDisplay(false);
                }
            }
        }

        /** Right Arrow Handler*/
        public void rightArrow() {
            if (stored.postCurse() != null) {
                if (stored.postCurse().getText().equals("\n")) {
                    //moving right when there's an enter
                    stored.moveFor();
                    cursor.setX(margin);
                    cursor.setY(cursor.getY() + getHeight());
                } else {
                    stored.moveFor();
                    setCursorDisplay(true);
                }
            }
        }

        /** Up Arrow Handler*/
        public void upArrow() {
            if (cursor.getY() == 0) { //if cursor is in top most position
                return;
            } else if (cursor.getX() == margin) { //if cursor is at the start of line
                int newy = (int) Math.round(cursor.getY() - getHeight());
                stored.moveUpDown(newy / getHeight());
                cursor.setX(margin);
                cursor.setY(newy);
            } else {
                int newx = (int) Math.round(cursor.getX());
                int newy = (int) Math.round(cursor.getY() - getHeight());
                stored.findClick(newy / getHeight(), newx, getHeight());
                setCursorDisplay(false);
            }
        }

        /** Down Arrow Handler*/
        public void downArrow() {
            if (cursor.getY() == ycurr) {
                // need to check if on last line, so can't move down
                return;
            } else if (cursor.getX() == margin) {
                int newy = (int) Math.round(cursor.getY() + getHeight());
                stored.moveUpDown(newy / getHeight());
                cursor.setX(margin);
                cursor.setY(newy);
            } else {
                int newx = (int) Math.round(cursor.getX());
                int newy = (int) Math.round(cursor.getY() + getHeight());
                stored.findClick(newy / getHeight(), newx, getHeight());
                setCursorDisplay(false);
            }
        }

        /** Undo Function*/
        public void undoHelper() {
            if (!stored.undoIsEmpty()) {
                String action = stored.getUndoAction();
                Text t = stored.undo();
                if (action.equals("insert")) {
                    textroot.getChildren().remove(t);
                } else {
                    textroot.getChildren().add(t);
                }
            }
        }

        /** Redo Function*/
        public void redoHelper() {
            if (!stored.redoIsEmpty()) {
                String action = stored.getRedoAction();
                Text t = stored.redo();
                if (action.equals("insert")) {
                    textroot.getChildren().add(t);
                } else {
                    textroot.getChildren().remove(t);
                }
            }
        }

        /** Save Function*/
        public void save() {
            try {
                FileWriter writer = new FileWriter(filename);
                stored.resetSol();
                for (int b = 0; b < stored.size(); b++) {
                    writer.write(stored.next().getText());
                }
                writer.close();
            } catch (FileNotFoundException fileNotFoundException) {
                //if file is not found, state exception and create new doc
                System.out.println("File not found! Exception was: "
                        + fileNotFoundException);
            } catch (IOException ioException) {
                System.out.println("Error when saving; exception was: " + ioException);
            }
        }
    }
    /** An EventHandler to handle changing the color of the rectangle. */
    private class RectangleBlinkEventHandler implements EventHandler<ActionEvent> {
        private int currentColorIndex = 0;
        private Color[] boxColors = {Color.WHITE, Color.BLACK};
        RectangleBlinkEventHandler() {
            // Set the color to be the first color in the list.
            changeColor();
        }

        private void changeColor() {
            cursor.setFill(boxColors[currentColorIndex]);
            currentColorIndex = 1 - currentColorIndex;
        }

        @Override
        public void handle(ActionEvent event) {
            changeColor();
        }
    }

    /** Makes the text bounding box change color periodically. */
    public void makeRectangleColorChange() {
        final Timeline timeline = new Timeline();
        timeline.setCycleCount(Timeline.INDEFINITE); //blinks forever
        RectangleBlinkEventHandler cursorChange = new RectangleBlinkEventHandler();
        KeyFrame keyFrame = new KeyFrame(Duration.seconds(0.5), cursorChange);
        //timeline calls the handle function ever 0.5 seconds
        timeline.getKeyFrames().add(keyFrame);
        timeline.play();
    }

    /** An event handler that displays the current position of the mouse whenever it is clicked. */
    private class MouseClickEventHandler implements EventHandler<MouseEvent> {

        @Override
        public void handle(MouseEvent mouseEvent) {
            mouseX = (int) Math.round(mouseEvent.getX());
            mouseY = (int) Math.round(mouseEvent.getY()) + (int) Math.round(yshift);
            if (mouseX > xcurr && mouseY > ycurr) {
                stored.lastSpot();
                setCursorDisplay(true);
                return;
            }
            if (mouseX < margin) {
                mouseX = margin;
            }
            if (mouseY > ycurr) {
                mouseY = ycurr;
            }
            int linenum = mouseY / getHeight();
            boolean notlast = stored.findClick(linenum, mouseX, getHeight());
            setCursorDisplay(notlast);
        }
    }

    /**Start Method*/
    @Override
    public void start(Stage primaryStage) {
        // Create a Node that will be the parent of all things displayed on the screen.
        root.getChildren().add(textroot);
        // The Scene represents the window: its height and width will be the height and width
        // of the window displayed.
        scene = new Scene(root, WINDOWWIDTH, WINDOWHEIGHT, Color.WHITE);

        scene.setOnMouseClicked(new MouseClickEventHandler());

        cursor = new Rectangle(1, 16);
        textroot.getChildren().add(cursor);
        makeRectangleColorChange();

        // To get information about what keys the user is pressing, create an EventHandler.
        // EventHandler subclasses must override the "handle" function, which will be called
        // by javafx.

        /** Window Width Listener */
        scene.widthProperty().addListener(new ChangeListener<Number>() {
            @Override public void changed(
                    ObservableValue<? extends Number> observableValue,
                    Number oldScreenWidth,
                    Number newScreenWidth) {
                WINDOWWIDTH = newScreenWidth.intValue();
                redisplay();
                scrollBar.setLayoutX(Math.round(WINDOWWIDTH
                        - scrollBar.getLayoutBounds().getWidth()));
            }
        });

        /** Window Height Listener */
        scene.heightProperty().addListener(new ChangeListener<Number>() {
            @Override public void changed(
                    ObservableValue<? extends Number> observableValue,
                    Number oldScreenHeight,
                    Number newScreenHeight) {
                WINDOWHEIGHT = newScreenHeight.intValue();
                redisplay();
            }
        });

        root.getChildren().add(scrollBar);
        scrollbarwidth = Math.round(scrollBar.getLayoutBounds().getWidth());
        setScrollBar();

        KeyEventHandler keyEventHandler =
                new KeyEventHandler(WINDOWWIDTH, WINDOWHEIGHT);

        scene.setOnKeyTyped(keyEventHandler);
        scene.setOnKeyPressed(keyEventHandler);

        primaryStage.setTitle("slate");

        /** Scroll Bar Listener */
        scrollBar.valueProperty().addListener(new ChangeListener<Number>() {
            public void changed(
                    ObservableValue<? extends Number> observableValue,
                    Number oldValue,
                    Number newValue) {
                double oldv = oldValue.doubleValue();
                double newv = newValue.doubleValue();
                if (oldv != newv) {
                    scrollBar.setValue(newv);
                }
                yshift = (Double) newValue;
                textroot.setLayoutY(-newv);
                setScrollBar();
            }
        });

        // This is boilerplate, necessary to setup the window where things are displayed.
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /** Render Method */
    public void redisplay() {
        xcurr = margin;
        ycurr = 0;
        if (stored.size() > 0) {
            int wordlength;
            int length = WINDOWWIDTH - (2 * margin) - (int) scrollbarwidth;
            stored.resetSol();
            Text c = stored.next();
            while (c != null) {
                c.setTextOrigin(VPos.TOP);
                c.setFont(Font.font(fontName, fontSize));
                if (c.getText().equals("\n")) { // if it is a new line
                    c.setX(xcurr);
                    c.setY(ycurr);
                    xcurr = margin;
                    ycurr += getHeight();
                    c = stored.next();
                } else if (c.getText().equals(" ")) { // if it is a space
                    c.setX(xcurr);
                    c.setY(ycurr);
                    xcurr += (int) Math.round(c.getLayoutBounds().getWidth());
                    c = stored.next();
                } else { // if it is letter, then find the word and see if it will fit on the line
                    LinkedListDeque word = new LinkedListDeque();
                    wordlength = 0;
                    while (c != null && !c.getText().equals("\n") && !c.getText().equals(" ")) {
                        c.setTextOrigin(VPos.TOP);
                        c.setFont(Font.font(fontName, fontSize));
                        wordlength += (int) Math.round(c.getLayoutBounds().getWidth());
                        word.addAny(c);
                        c = stored.next();
                    }
                    if (wordlength + xcurr > length) { //if the word is too long
                        if (xcurr != margin) { //if doesnt start at beginning of line, go to next
                            xcurr = margin;
                            ycurr += getHeight();
                        }
                        int newwordcounter = 0;
                        word.resetSol();
                        Text w = word.next();
                        while (w != null) {
                            w.setTextOrigin(VPos.TOP);
                            w.setFont(Font.font(fontName, fontSize));
                            double width = Math.round(w.getLayoutBounds().getWidth());
                            if (newwordcounter + (int) width > length) {
                                newwordcounter = 0;
                                xcurr = margin;
                                ycurr += getHeight();
                            }
                            w.setX(xcurr);
                            w.setY(ycurr);
                            newwordcounter += (int) width;
                            xcurr += (int) width;
                            w = word.next();
                        }
                    } else { //if the word actually fits on the line
                        word.resetSol();
                        Text w = word.next();
                        while (w != null) {
                            w.setTextOrigin(VPos.TOP);
                            w.setFont(Font.font(fontName, fontSize));
                            w.setX(xcurr);
                            w.setY(ycurr);
                            double width = Math.round(w.getLayoutBounds().getWidth());
                            xcurr += (int) width;
                            w = word.next();
                        }
                    }
                }
            }
        }
        setCursorDisplay(false);
        lines();
        setScrollBar();
    }

    /** Lines Method - stores the first character of each line */
    public void lines() {
        stored.resetSol();
        stored.resetlines();
        Text curr = stored.next();
        while (curr != null) {
            if (curr.getX() == margin) {
                stored.addnewline();
            }
            curr = stored.next();
        }
    }

    /** Set Scroll Bar */
    public void setScrollBar() {
        scrollBar.setOrientation(Orientation.VERTICAL);
        scrollBar.setPrefHeight(WINDOWHEIGHT);
        double usableScreenWidth = WINDOWWIDTH - scrollbarwidth;
        scrollBar.setLayoutX(usableScreenWidth);
        scrollBar.setMin(ymin);
        textHeight = 0;
        if (stored.getLastSpot() != null) {
            textHeight = (int) Math.round(stored.getLastSpot().getY()
                    + stored.getLastSpot().getLayoutBounds().getHeight());
        }
        scrollBar.setMax(Math.max(ymin, textHeight - WINDOWHEIGHT));
    }

    /** Snap Back Method to show cursor when necessary*/
    public void snapBack() {
        //formulas were created during lab after help with visualization from TA, Sherdil Niyaz
        double ctop = -textroot.getLayoutY() - cursor.getY();
        double cbot = (cursor.getY() + cursor.getHeight()) - (WINDOWHEIGHT - textroot.getLayoutY());
        if (ctop > 0) {
            scrollBar.setValue(scrollBar.getValue() - ctop);
        }
        if (cbot > 0) {
            scrollBar.setValue(scrollBar.getValue() + cbot);
        }
        if (scrollBar.getValue() > scrollBar.getMax()) {
            scrollBar.setValue(scrollBar.getMax());
        }
    }

    /** Set Cursor Display */
    public void setCursorDisplay(boolean clickedLast) {
        cursor.setHeight(getHeight());
        if (stored.cursorPosition() != null) {
            if (stored.cursorPosition().getText().equals("\n")) {
                cursor.setX(margin);
                cursor.setY(stored.cursorPosition().getY() + getHeight());
            } else if (stored.postCurse() != null) {
                if (stored.cursorPosition().getY() < stored.postCurse().getY() && clickedLast) {
                    cursor.setX(margin);
                    cursor.setY(stored.postCurse().getY());
                } else {
                    //if(cursor.getX() == margin && stored.cursorPosition() == " ")
                    double width = Math.round(stored.cursorPosition().getLayoutBounds().getWidth());
                    cursor.setX(Math.max((int) stored.cursorPosition().getX() + (int) width,
                            margin));
                    cursor.setX(Math.min(cursor.getX(),
                            WINDOWWIDTH - margin - (int) scrollbarwidth));
                    cursor.setY((int) stored.cursorPosition().getY());
                }
            } else {
                double width = Math.round(stored.cursorPosition().getLayoutBounds().getWidth());
                cursor.setX(Math.max((int) stored.cursorPosition().getX() + (int) width, margin));
                cursor.setX(Math.min(cursor.getX(), WINDOWWIDTH - margin - (int) scrollbarwidth));
                cursor.setY((int) stored.cursorPosition().getY());
            }
        } else {
            cursor.setX(margin);
            cursor.setY(0);
        }
    }

    /** GetHeight */
    public int getHeight() {
        Text c = new Text("a");
        c.setFont(Font.font(fontName, fontSize));
        int height = (int) Math.round(c.getLayoutBounds().getHeight());
        return height;
    }

    /** Main Method - File Opening and Editor Startup */
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.print("No filename provided.");
            System.exit(1);
        } else {
            filename = args[0];
            try {
                FileReader reader = new FileReader(filename);
                BufferedReader bufferedReader = new BufferedReader(reader);
                int intRead = -1;
                // Keep reading from the file input read() returns -1,
                // which means the end of the file
                // was reached.

                while ((intRead = bufferedReader.read()) != -1) {
                    // The integer read can be cast to a char, because we're assuming ASCII.

                    String charRead = Character.toString((char) intRead);
                    Text c = new Text(charRead);
                    c.setTextOrigin(VPos.TOP);
                    c.setFont(Font.font(fontName, fontSize));
                    if (!charRead.equals("\r")) {
                        stored.addLast(c);
                    }
                    c.toFront();
                }
                bufferedReader.close();
            } catch (FileNotFoundException fileNotFoundException) {
                //if file is not found, state exception and create new doc
                System.out.println("File not found! Exception was: " + fileNotFoundException);
            } catch (IOException ioException) {
                System.out.println("Error when opening; exception was: " + ioException);
            }
        }
        if (args.length == 2) {
            String optional = args[1];
            if (optional.equals("debug")) {
                System.out.print("Debug Mode Initiated");
            }
        }
        launch(args);
    }
}
