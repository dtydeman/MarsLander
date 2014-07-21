package Assignment;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 * Framework that controls the game (Game.java) that created it, update it and
 * draw it on the screen.
 */
public class Framework extends Canvas implements ActionListener {

    //Is the game over?
    public boolean gameOver;
    //Width of the frame.
    public static int frameWidth;
    // Height of the frame.
    public static int frameHeight;
    //Time of one second in nanoseconds. 1 second = 1 000 000 000 nanoseconds
    public static final long secInNanosec = 1000000000L;
    //Time of one millisecond in nanoseconds. 1 millisecond = 1 000 000 nanoseconds
    public static final long milisecInNanosec = 1000000L;
    //FPS - Frames per second How many times per second the game should update
    private final int GAME_FPS = 16;
    //Pause between updates. It is in nanoseconds.
    private final long GAME_UPDATE_PERIOD = secInNanosec / GAME_FPS;

    @Override
    public void actionPerformed(ActionEvent e) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    // Possible states of the game
    public static enum GameState {

        STARTING, VISUALIZING, GAME_CONTENT_LOADING, MAIN_MENU, OPTIONS, PLAYING, GAMEOVER, DESTROYED
    }
    // Current state of the game
    public static GameState gameState;
    //Elapsed game time in nanoseconds.
    private long gameTime;
    // Used for calculating elapsed time.
    private long lastTime;
    // The actual game
    private Game game;
    // Image for menu
    private BufferedImage marsLanderMenuImg;
    // Image for panel
    private BufferedImage marsLanderPanelImg;
    //Allows setting of colour
    public int colour;

    public Framework() {
        super();

        gameState = GameState.VISUALIZING;

        //Start game in new thread.
        Thread gameThread = new Thread() {

            @Override
            public void run() {
                GameLoop();
            }
        };
        gameThread.start();
    }

    //Loads the menu and panel images
    private void LoadContent() {
        try {

            URL marsLanderMenuImgUrl = this.getClass().getResource("Images/Nasa Logo.jpg");
            URL marsLanderPanelImgUrl = this.getClass().getResource("Images/Panel.png");

            marsLanderMenuImg = ImageIO.read(marsLanderMenuImgUrl);
            marsLanderPanelImg = ImageIO.read(marsLanderPanelImgUrl);
        } catch (IOException ex) {
            Logger.getLogger(Framework.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * In specific intervals of time (GAME_UPDATE_PERIOD) the game/logic is
     * updated and then the game is drawn on the screen.
     */
    private void GameLoop() {
        /* This two variables are used in VISUALIZING state of the game. 
        *  Used ensure correct frame/window resolution.
        */
        long visualizingTime = 0, lastVisualizingTime = System.nanoTime();
        /* These variables are used for calculating the time that defines for 
        *  how long the thread  should sleep to meet the GAME_FPS.
        */
        long beginTime, timeTaken, timeLeft;

        while (true) {
            beginTime = System.nanoTime();

            switch (gameState) {
                case PLAYING:
                    gameTime += System.nanoTime() - lastTime;
                    game.UpdateGame(gameTime, mousePosition());
                    lastTime = System.nanoTime();
                    break;
                case GAMEOVER:
                    break;
                case MAIN_MENU:
                    break;
                case OPTIONS:
                    break;
                case GAME_CONTENT_LOADING:
                    break;
                case STARTING:
                    LoadContent();
                    // When all things that are called above finished, change game status to main menu.
                    gameState = GameState.MAIN_MENU;
                    break;
                case VISUALIZING:
                    /* Wait one second for the window/frame to be set to its correct size. 
                    * Also insert 'this.getWidth() > 1' condition in case the window/frame 
                    *size wasn't set in time
                    */
                    if (this.getWidth() > 1 && visualizingTime > secInNanosec) {
                        frameWidth = this.getWidth() - 400;
                        frameHeight = this.getHeight();

                        // When the size of frame is obtained, change status.
                        gameState = GameState.STARTING;
                    } else {
                        visualizingTime += System.nanoTime() - lastVisualizingTime;
                        lastVisualizingTime = System.nanoTime();
                    }
                    break;
            }

            // Repaint the screen.
            repaint();

            // Calculate the time that defines for how long the thread should sleep to meet the GAME_FPS.
            timeTaken = System.nanoTime() - beginTime;
            timeLeft = (GAME_UPDATE_PERIOD - timeTaken) / milisecInNanosec; // In milliseconds
            // If the time is less than 10 milliseconds, then put thread to sleep for 
            //10 millisecond to allow other threads to function
            if (timeLeft < 10) {
                timeLeft = 10; //set a minimum
            }
            try {
                //Provides the necessary delay and also yields control so that other threads can function
                Thread.sleep(timeLeft);
            } catch (InterruptedException ex) {
            }
        }
    }
    
     /**
     * Draw the game to the screen. It is called through repaint() method in
     * GameLoop() method.
     */
    @Override
    public void Draw(Graphics2D g2d) {
        switch (gameState) {
            case PLAYING:
                game.Draw(g2d, mousePosition());
                game.DrawControls(g2d, null);
                break;
            case GAMEOVER:
                game.DrawGameOver(g2d, mousePosition(), gameTime);
                game.DrawControls(g2d, null);
                //game.Scores(g2d);
                //Drew the game scores when access was available to server
                break;
            case MAIN_MENU:
                g2d.drawImage(marsLanderMenuImg, 0, 0, frameWidth, frameHeight, null);
                g2d.drawImage(marsLanderPanelImg, frameWidth, 0, 400, frameHeight, null);
                break;
            case OPTIONS:
                break;
            case GAME_CONTENT_LOADING:
                g2d.setColor(Color.white);
                g2d.drawString("GAME is LOADING", frameWidth / 2 - 50, frameHeight / 2);
                break;
        }
    }

    /**
     * Starts new game.
     */
    private void newGame() {
        // Set gameTime to zero and lastTime to current time for later calculations.
        gameTime = 0;
        lastTime = System.nanoTime();

        game = new Game();
    }

    /**
     * Restart game - reset game time and call RestartGame() method of game
     * object so that reset some variables.
     */
    private void restartGame() {
        // Set gameTime to zero and lastTime to current time for later calculations.
        gameTime = 0;
        lastTime = System.nanoTime();

        game.RestartGame();

        // Change game status so that the game can start.
        gameState = GameState.PLAYING;
    }

    /**
     * @return Point of mouse coordinates.
     */
    private Point mousePosition() {
        try {
            Point mp = this.getMousePosition();

            if (mp != null) {
                return this.getMousePosition();
            } else {
                return new Point(0, 0);
            }
        } catch (Exception e) {
            return new Point(0, 0);
        }
    }

    /**
     * This method is called when keyboard key is released.
     *
     * @param e KeyEvent
     */
    @Override
    public void keyReleasedFramework(KeyEvent e) {
        switch (gameState) {
            case MAIN_MENU:
                newGame();
                break;
            case GAMEOVER:
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    restartGame();
                }
                break;
        }
    }

    /**
     * This method is called when mouse button is clicked.
     *
     * @param e MouseEvent
     */
    @Override
    public void mouseClicked(MouseEvent e) {
    }
}
