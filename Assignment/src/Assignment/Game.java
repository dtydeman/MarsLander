package Assignment;

import java.applet.Applet;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JTable;

public class Game extends Applet implements MouseListener, KeyListener {

    private Player playerShip;
    private LandingArea landingArea;
    private LoginForm loginForm;
    private BufferedImage backgroundImg;
    private BufferedImage marsLanderPanelImg;
    public int colourSelectorX;
    public int timeSelectorX;
    public static boolean lightBlueClicked;
    public static boolean darkBlueClicked;
    public static boolean greenClicked;
    public static boolean purpleClicked;
    public static boolean redClicked;
    public static boolean blackClicked;
    public static boolean dayClicked;
    public static boolean nightClicked;
    int set = 1;
    Statement s = null;

    public Game() {
        Framework.gameState = Framework.GameState.GAME_CONTENT_LOADING;
       /*
        Following commented code attempts to connect to an SQL database that was available on the University of 
            Chester's server, however, since graduating I am no longer able to access said server.
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            String connectionUrl = "jdbc:sqlserver://sql2012.studentwebserver.co.uk;"
                    + "databaseName=db_1101603_MarsLander;user=user_db_1101603_MarsLander;password=P@55word;";
            Connection con = DriverManager.getConnection(connectionUrl);
            s = con.createStatement();
        } catch (Exception e) {
            System.err.println("Connection failed");
            System.err.println(e.getMessage());
        }
        */
        addMouseListener(this);
        addKeyListener(this);

        Thread threadForInitGame = new Thread() {

            @Override
            public void run() {
                // Sets variables and objects for the game.
                Initialize();
                // Load game files (images, sounds, ...)
                LoadContent();

                Framework.gameState = Framework.GameState.PLAYING;
            }
        };
        threadForInitGame.start();
    }

    /**
     * Set variables and objects for the game.
     */
    private void Initialize() {
        playerShip = new Player();
        landingArea = new LandingArea();
    }

    /**
     * Loads background image and panel image
     */
    private void LoadContent() {
        try {
            URL backgroundImgUrl = this.getClass().getResource("Images/Mars Terrain.png");
            URL marsLanderPanelImgUrl = this.getClass().getResource("Images/Panel.png");
            backgroundImg = ImageIO.read(backgroundImgUrl);
            marsLanderPanelImg = ImageIO.read(marsLanderPanelImgUrl);

        } catch (IOException ex) {
            Logger.getLogger(Game.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Restart game - reset certain variables.
     */
    public void RestartGame() {
        playerShip.ResetPlayer();
    }

    /**
     * Update game logic.
     *
     * @param gameTime gameTime of the game.
     * @param mousePosition current mouse position.
     */
    public void UpdateGame(long gameTime, Point mousePosition) {
        // Move the ship
        playerShip.Update();

        /* Checks where the player ship is. Moving, landed or crashed?
        *  Check bottom y coordinate of the ship if is it near the landing area.
        */
        if (playerShip.y + playerShip.shipImgHeight - 10 > landingArea.y) {
            // Check if the ship is over landing area.
            if ((playerShip.x > landingArea.x) && (playerShip.x < landingArea.x + landingArea.landingAreaImgWidth - playerShip.shipImgWidth)) {
                // Check if the ship speed isn't too high.
                if (playerShip.speedY <= playerShip.topLandingSpeed) {
                    playerShip.landed = true;
                } else {
                    playerShip.crashed = true;
                }
            } else {
                playerShip.crashed = true;
            }
            Framework.gameState = Framework.GameState.GAMEOVER;
        }
    }

    /**
     * Draw the game to the screen.
     *
     * @param g2d Graphics2D
     * @param mousePosition current mouse position.
     */
    public void Draw(Graphics2D g2d, Point mousePosition) {
        g2d.drawImage(backgroundImg, 0, 0, Framework.frameWidth, Framework.frameHeight, null);
        g2d.drawImage(marsLanderPanelImg, Framework.frameWidth, 0, 400, Framework.frameHeight, null);

        landingArea.Draw(g2d);

        playerShip.Draw(g2d);
    }

    /**
     * Draw the game over screen and calculate the score
     *
     * @param g2d Graphics2D
     * @param mousePosition Current mouse position.
     * @param gameTime Game time in nanoseconds.
     */
    public void DrawGameOver(Graphics2D g2d, Point mousePosition, long gameTime) {
        Draw(g2d, mousePosition);

        int fuelScore = playerShip.shipFuel;
        int speedScore = playerShip.speedY;
        int landedScore = 0;
        int total = 0;
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        Date date = new Date();
        String user = null;
        String landed = null;
        String saveDate = dateFormat.format(date).toString();

        
        if (playerShip.landed) {
            //Points for landing safely
            landedScore = 10;
        } else {
            landedScore = 0;
        }
        if (landedScore > 0) {
            //Calculate the full score if the ship landed safely
            if ((total = (int) (fuelScore + landedScore - speedScore - (gameTime / 1000000000))) > 0) {
                total = (int) (fuelScore + landedScore - speedScore - (gameTime / 1000000000));
            } else {
                //If the ship crashed, no score is earned
                total = 0;
            }
        } else {
            total = 0;
        }

        //Gain the user name for storing with the score
        user = loginForm.strUser;
        //Create a string for if landed to input to the Database
        if (playerShip.landed == true) {
            landed = "Yes";
        }
        else {
            landed = "No";
        }

        //SQL query for uploading data to the table in the Database
        String str = "INSERT INTO tblScore VALUES('"
                + user + "','" + total + "','" + landed + "','" + saveDate + "')";

        //Run the query if possible
        try {
            s.executeUpdate(str);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        
        //Provide the player with information on their score and how to restart
        if (playerShip.landed) {
            g2d.drawString("You have successfully landed!", Framework.frameWidth / 2 - 100, Framework.frameHeight / 3);
            g2d.drawString("You have landed in " + gameTime / Framework.secInNanosec + " seconds.", Framework.frameWidth / 2 - 100, Framework.frameHeight / 3 + 20);
            g2d.drawString("Your score is " + total, Framework.frameWidth / 2 - 100, Framework.frameHeight / 3 + 40);
            g2d.drawString("Press Enter to Relaunch", Framework.frameWidth / 2 - 100, Framework.frameHeight / 3 + 60);
        } else {
            g2d.setColor(Color.red);
            g2d.drawString("You have crashed the ship!", Framework.frameWidth / 2 - 95, Framework.frameHeight / 3);
            g2d.drawString("Your score is " + total, Framework.frameWidth / 2 - 100, Framework.frameHeight / 3 + 20);
            g2d.drawString("Press Enter to Relaunch", Framework.frameWidth / 2 - 100, Framework.frameHeight / 3 + 60);
        }
    }
    
    /*
    No longer valid as access to the University's server has been revoked.
    Commented out & not deleted to show use of SQL in project

    //Used for creating an output of the top 10 scores, orgainised by highest to lowest score
    public void Scores(Graphics2D g2d) {
        String str = "SELECT * FROM tblScore ORDER BY tblScore.Score DESC";
        String strUser1 = null;
        String strScore1 = null;
        String strLanded1 = null;
        String strDate1 = null;
        String strUser2 = null;
        String strScore2 = null;
        String strLanded2 = null;
        String strDate2 = null;
        String strUser3 = null;
        String strScore3 = null;
        String strLanded3 = null;
        String strDate3 = null;
        String strUser4 = null;
        String strScore4 = null;
        String strLanded4 = null;
        String strDate4 = null;
        String strUser5 = null;
        String strScore5 = null;
        String strLanded5 = null;
        String strDate5 = null;
        String strUser6 = null;
        String strScore6 = null;
        String strLanded6 = null;
        String strDate6 = null;
        String strUser7 = null;
        String strScore7 = null;
        String strLanded7 = null;
        String strDate7 = null;
        String strUser8 = null;
        String strScore8 = null;
        String strLanded8 = null;
        String strDate8 = null;
        String strUser9 = null;
        String strScore9 = null;
        String strLanded9 = null;
        String strDate9 = null;
        String strUser10 = null;
        String strScore10 = null;
        String strLanded10 = null;
        String strDate10 = null;
        int i = 1;

        ResultSet rs = null;

        try {
            rs = s.executeQuery(str);
            while (rs.next()) {
                if (i == 1) {
                    strUser1 = rs.getString(1);
                    strScore1 = rs.getString(2);
                    strLanded1 = rs.getString(3);
                    strDate1 = rs.getString(4);
                } else if (i == 2) {
                    strUser2 = rs.getString(1);
                    strScore2 = rs.getString(2);
                    strLanded2 = rs.getString(3);
                    strDate2 = rs.getString(4);
                } else if (i == 3) {
                    strUser3 = rs.getString(1);
                    strScore3 = rs.getString(2);
                    strLanded3 = rs.getString(3);
                    strDate3 = rs.getString(4);
                } else if (i == 4) {
                    strUser4 = rs.getString(1);
                    strScore4 = rs.getString(2);
                    strLanded4 = rs.getString(3);
                    strDate4 = rs.getString(4);
                } else if (i == 5) {
                    strUser5 = rs.getString(1);
                    strScore5 = rs.getString(2);
                    strLanded5 = rs.getString(3);
                    strDate5 = rs.getString(4);
                } else if (i == 6) {
                    strUser6 = rs.getString(1);
                    strScore6 = rs.getString(2);
                    strLanded6 = rs.getString(3);
                    strDate6 = rs.getString(4);
                } else if (i == 7) {
                    strUser7 = rs.getString(1);
                    strScore7 = rs.getString(2);
                    strLanded7 = rs.getString(3);
                    strDate7 = rs.getString(4);
                } else if (i == 8) {
                    strUser8 = rs.getString(1);
                    strScore8 = rs.getString(2);
                    strLanded8 = rs.getString(3);
                    strDate8 = rs.getString(4);
                } else if (i == 9) {
                    strUser9 = rs.getString(1);
                    strScore9 = rs.getString(2);
                    strLanded9 = rs.getString(3);
                    strDate9 = rs.getString(4);
                } else if (i == 10) {
                    strUser10 = rs.getString(1);
                    strScore10 = rs.getString(2);
                    strLanded10 = rs.getString(3);
                    strDate10 = rs.getString(4);
                }
                i++;
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }

        //Outputs the scores in the bottom right of the panel
        g2d.setColor(Color.black);
        g2d.drawString("User", Framework.frameWidth + 50, 450);
        g2d.drawString("Score", Framework.frameWidth + 150, 450);
        g2d.drawString("Landed?", Framework.frameWidth + 210, 450);
        g2d.drawString("Date", Framework.frameWidth + 300, 450);
        g2d.drawString(strUser1, Framework.frameWidth + 50, 465);
        g2d.drawString(strScore1, Framework.frameWidth + 150, 465);
        g2d.drawString(strLanded1, Framework.frameWidth + 225, 465);
        g2d.drawString(strDate1, Framework.frameWidth + 300, 465);
        g2d.drawString(strUser2, Framework.frameWidth + 50, 480);
        g2d.drawString(strScore2, Framework.frameWidth + 150, 480);
        g2d.drawString(strLanded2, Framework.frameWidth + 225, 480);
        g2d.drawString(strDate2, Framework.frameWidth + 300, 480);
        g2d.drawString(strUser3, Framework.frameWidth + 50, 495);
        g2d.drawString(strScore3, Framework.frameWidth + 150, 495);
        g2d.drawString(strLanded3, Framework.frameWidth + 225, 495);
        g2d.drawString(strDate3, Framework.frameWidth + 300, 495);
        g2d.drawString(strUser4, Framework.frameWidth + 50, 510);
        g2d.drawString(strScore4, Framework.frameWidth + 150, 510);
        g2d.drawString(strLanded4, Framework.frameWidth + 225, 510);
        g2d.drawString(strDate4, Framework.frameWidth + 300, 510);
        g2d.drawString(strUser5, Framework.frameWidth + 50, 525);
        g2d.drawString(strScore5, Framework.frameWidth + 150, 525);
        g2d.drawString(strLanded5, Framework.frameWidth + 225, 525);
        g2d.drawString(strDate5, Framework.frameWidth + 300, 525);
        g2d.drawString(strUser6, Framework.frameWidth + 50, 540);
        g2d.drawString(strScore6, Framework.frameWidth + 150, 540);
        g2d.drawString(strLanded6, Framework.frameWidth + 225, 540);
        g2d.drawString(strDate6, Framework.frameWidth + 300, 540);
        g2d.drawString(strUser7, Framework.frameWidth + 50, 555);
        g2d.drawString(strScore7, Framework.frameWidth + 150, 555);
        g2d.drawString(strLanded7, Framework.frameWidth + 225, 555);
        g2d.drawString(strDate7, Framework.frameWidth + 300, 555);
        g2d.drawString(strUser8, Framework.frameWidth + 50, 570);
        g2d.drawString(strScore8, Framework.frameWidth + 150, 570);
        g2d.drawString(strLanded8, Framework.frameWidth + 225, 570);
        g2d.drawString(strDate8, Framework.frameWidth + 300, 570);
        g2d.drawString(strUser9, Framework.frameWidth + 50, 585);
        g2d.drawString(strScore9, Framework.frameWidth + 150, 585);
        g2d.drawString(strLanded9, Framework.frameWidth + 225, 585);
        g2d.drawString(strDate9, Framework.frameWidth + 300, 585);
        g2d.drawString(strUser10, Framework.frameWidth + 50, 600);
        g2d.drawString(strScore10, Framework.frameWidth + 150, 600);
        g2d.drawString(strLanded10, Framework.frameWidth + 225, 600);
        g2d.drawString(strDate10, Framework.frameWidth + 300, 600);
    }
    */

    //Draws the controls in the control panel
    public void DrawControls(Graphics2D g2d, MouseEvent e) {
        addMouseListener(this);
        addKeyListener(this);

        lightBlueClicked = true;
        darkBlueClicked = false;
        greenClicked = false;
        purpleClicked = false;
        redClicked = false;
        blackClicked = false;
        dayClicked = true;
        nightClicked = false;

        Graphics2D lightBlueShip = (Graphics2D) g2d.create();
        Graphics2D darkBlueShip = (Graphics2D) g2d.create();
        Graphics2D greenShip = (Graphics2D) g2d.create();
        Graphics2D purpleShip = (Graphics2D) g2d.create();
        Graphics2D redShip = (Graphics2D) g2d.create();
        Graphics2D blackShip = (Graphics2D) g2d.create();
        Graphics2D selected = (Graphics2D) g2d.create();
        Graphics2D day = (Graphics2D) g2d.create();
        Graphics2D night = (Graphics2D) g2d.create();
        Graphics2D time = (Graphics2D) g2d.create();

        //Draws the buttons for selection
        g2d.setColor(Color.black);
        g2d.drawString("Ship Colour: ", (Framework.frameWidth + 20), 95);
        lightBlueShip.setColor(Color.cyan);
        lightBlueShip.fillRect((Framework.frameWidth + 120), 80, 20, 20);
        lightBlueShip.drawRect((Framework.frameWidth + 120), 80, 20, 20);
        darkBlueShip.setColor(Color.blue);
        darkBlueShip.fillRect((Framework.frameWidth + 160), 80, 20, 20);
        darkBlueShip.drawRect((Framework.frameWidth + 160), 80, 20, 20);
        greenShip.setColor(Color.green);
        greenShip.fillRect((Framework.frameWidth + 200), 80, 20, 20);
        greenShip.drawRect((Framework.frameWidth + 200), 80, 20, 20);
        purpleShip.setColor(Color.magenta);
        purpleShip.fillRect((Framework.frameWidth + 240), 80, 20, 20);
        purpleShip.drawRect((Framework.frameWidth + 240), 80, 20, 20);
        redShip.setColor(Color.red);
        redShip.fillRect((Framework.frameWidth + 280), 80, 20, 20);
        redShip.drawRect((Framework.frameWidth + 280), 80, 20, 20);
        blackShip.setColor(Color.black);
        blackShip.fillRect((Framework.frameWidth + 320), 80, 20, 20);
        blackShip.drawRect((Framework.frameWidth + 320), 80, 20, 20);
        g2d.drawString("Day: ", (Framework.frameWidth + 20), 235);
        day.setColor(Color.cyan);
        day.fillRect((Framework.frameWidth + 60), 220, 20, 20);
        day.drawRect((Framework.frameWidth + 60), 220, 20, 20);
        g2d.drawString("Night: ", (Framework.frameWidth + 100), 235);
        night.setColor(Color.black);
        night.fillRect((Framework.frameWidth + 140), 220, 20, 20);
        night.drawRect((Framework.frameWidth + 140), 220, 20, 20);
        //Draws the selectors for the buttons
        selected.setColor(Color.white);
        selected.drawRect(colourSelectorX + 5, 85, 10, 10);
        selected.fillRect(colourSelectorX + 5, 85, 10, 10);
        time.setColor(Color.white);
        time.drawRect((timeSelectorX + 5), 225, 10, 10);
        time.fillRect((timeSelectorX + 5), 225, 10, 10);

        //Sets the location of the selectors and the applicable boolean values when clicked
        if (Canvas.xPos > Framework.frameWidth + 120 && Canvas.xPos < Framework.frameWidth + 140
                && Canvas.yPos > 80 && Canvas.yPos < 100 || Canvas.keyboardKeyState(KeyEvent.VK_L)) {
            lightBlueClicked = true;
            darkBlueClicked = false;
            greenClicked = false;
            purpleClicked = false;
            redClicked = false;
            blackClicked = false;
        } else if (Canvas.xPos > Framework.frameWidth + 160 && Canvas.xPos < Framework.frameWidth + 180
                && Canvas.yPos > 80 && Canvas.yPos < 100 || Canvas.keyboardKeyState(KeyEvent.VK_U)) {
            lightBlueClicked = false;
            darkBlueClicked = true;
            greenClicked = false;
            purpleClicked = false;
            redClicked = false;
            blackClicked = false;
        } else if (Canvas.xPos > Framework.frameWidth + 200 && Canvas.xPos < Framework.frameWidth + 220
                && Canvas.yPos > 80 && Canvas.yPos < 100 || Canvas.keyboardKeyState(KeyEvent.VK_G)) {
            lightBlueClicked = false;
            darkBlueClicked = false;
            greenClicked = true;
            purpleClicked = false;
            redClicked = false;
            blackClicked = false;
        } else if (Canvas.xPos > Framework.frameWidth + 240 && Canvas.xPos < Framework.frameWidth + 260
                && Canvas.yPos > 80 && Canvas.yPos < 100 || Canvas.keyboardKeyState(KeyEvent.VK_P)) {
            lightBlueClicked = false;
            darkBlueClicked = false;
            greenClicked = false;
            purpleClicked = true;
            redClicked = false;
            blackClicked = false;
        } else if (Canvas.xPos > Framework.frameWidth + 280 && Canvas.xPos < Framework.frameWidth + 300
                && Canvas.yPos > 80 && Canvas.yPos < 100 || Canvas.keyboardKeyState(KeyEvent.VK_R)) {
            lightBlueClicked = false;
            darkBlueClicked = false;
            greenClicked = false;
            purpleClicked = false;
            redClicked = true;
            blackClicked = false;
        } else if (Canvas.xPos > Framework.frameWidth + 320 && Canvas.xPos < Framework.frameWidth + 340
                && Canvas.yPos > 80 && Canvas.yPos < 100 || Canvas.keyboardKeyState(KeyEvent.VK_B)) {
            lightBlueClicked = false;
            darkBlueClicked = false;
            greenClicked = false;
            purpleClicked = false;
            redClicked = false;
            blackClicked = true;
        }

        if (Canvas.xPos > Framework.frameWidth + 60 && Canvas.xPos < Framework.frameWidth + 80
                && Canvas.yPos > 220 && Canvas.yPos < 240 || Canvas.keyboardKeyState(KeyEvent.VK_D)) {
            dayClicked = true;
            nightClicked = false;
        }
        if (Canvas.xPos > Framework.frameWidth + 140 && Canvas.xPos < Framework.frameWidth + 160
                && Canvas.yPos > 220 && Canvas.yPos < 240 || Canvas.keyboardKeyState(KeyEvent.VK_N)) {
            dayClicked = false;
            nightClicked = true;
        }

        if (lightBlueClicked == true) {
            colourSelectorX = Framework.frameWidth + 120;
        } else if (darkBlueClicked == true) {
            colourSelectorX = Framework.frameWidth + 160;
        } else if (greenClicked == true) {
            colourSelectorX = Framework.frameWidth + 200;
        } else if (purpleClicked == true) {
            colourSelectorX = Framework.frameWidth + 240;
        } else if (redClicked == true) {
            colourSelectorX = Framework.frameWidth + 280;
        } else if (blackClicked == true) {
            colourSelectorX = Framework.frameWidth + 320;
        }

        if (dayClicked == true && nightClicked == false) {
            timeSelectorX = Framework.frameWidth + 60;
        } else if (dayClicked == false && nightClicked == true) {
            timeSelectorX = Framework.frameWidth + 140;
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void mouseExited(MouseEvent e) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void keyTyped(KeyEvent e) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void keyPressed(KeyEvent e) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void keyReleased(KeyEvent e) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
