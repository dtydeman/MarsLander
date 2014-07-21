package Assignment;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JPanel;

/**
 * Create a JPanel on which will draw and listen for keyboard and mouse
 * events.
 */
public abstract class Canvas extends JPanel implements KeyListener, MouseListener {

    // Keyboard states - Here are stored states for keyboard keys - is it down or not.
    private static boolean[] keyboardState = new boolean[525];
    // Mouse states - Here are stored states for mouse keys - is it down or not.
    private static boolean[] mouseState = new boolean[3];
    //the X Position of the mouse click
    public static int xPos;
    //the Y Position of the mouse click
    public static int yPos;

    public Canvas() {
        // We use double buffer to draw on the screen.
        this.setDoubleBuffered(true);
        //ensures that the content is focusable to allow keyboard controls
        this.setFocusable(true);
        //sets the default background as black
        this.setBackground(Color.black);
        // Adds the keyboard listener to JPanel to receive key events from this component.
        this.addKeyListener(this);
        // Adds the mouse listener to JPanel to receive mouse events from this component.
        this.addMouseListener(this);

    }

    // This method is overridden in Framework.java and is used for drawing to the screen.
    public abstract void Draw(Graphics2D g2d);

    @Override
    public void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        super.paintComponent(g2d);
        Draw(g2d);
    }

    // Keyboard
    /**
     * Is keyboard key "key" down?
     *
     * @param key Number of key for which you want to check the state.
     * @return true if the key is down, false if the key is not down.
     */
    public static boolean keyboardKeyState(int key) {
        return keyboardState[key];
    }

    //When a key is pressed
    @Override
    public void keyPressed(KeyEvent e) {
        keyboardState[e.getKeyCode()] = true;
    }

    //When a pressed key is released
    @Override
    public void keyReleased(KeyEvent e) {
        keyboardState[e.getKeyCode()] = false;
        keyReleasedFramework(e);
    }

    //Unused methods
    @Override
    public void keyTyped(KeyEvent e) {
    }

    public abstract void keyReleasedFramework(KeyEvent e);

    // Mouse
    /**
     * Is mouse button "button" down? Parameter "button" can be
     * "MouseEvent.BUTTON1" - Indicates mouse button #1 or "MouseEvent.BUTTON2"
     * - Indicates mouse button #2 ...
     *
     * @param button Number of mouse button for which you want to check the
     * state.
     * @return true if the button is down, false if the button is not down.
     */
    public static boolean mouseButtonState(int button) {
        return mouseState[button - 1];
    }

    // Sets mouse key status.
    private void mouseKeyStatus(MouseEvent e, boolean status) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            mouseState[0] = status;
        } else if (e.getButton() == MouseEvent.BUTTON2) {
            mouseState[1] = status;
        } else if (e.getButton() == MouseEvent.BUTTON3) {
            mouseState[2] = status;
        }
    }

    // When the mouse button is pressed
    @Override
    public void mousePressed(MouseEvent e) {
        mouseKeyStatus(e, true);
        xPos = e.getX();
        yPos = e.getY();
    }

    //When the mouse button is released
    @Override
    public void mouseReleased(MouseEvent e) {
        mouseKeyStatus(e, false);
    }

    
    //Unused methods
    @Override
    public void mouseClicked(MouseEvent e) {
        
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }
}
