package Assignment;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

//The space ship with which player will have to land.
public class Player {

    // Generates a random number for starting x coordinate of the ship.
    private Random random;
    // X coordinate of the ship.
    public int x;
    //Y coordinate of the ship.
    public int y;
    // Has the ship landed?
    public boolean landed;
    // Has the ship crashed?
    public boolean crashed;
    //Acceleration speed of the ship.
    private int speedAccelerating;
    /**
     * Stopping/Falling speed of the ship. Falling speed because, the gravity
     * pulls the ship down to Mars.
     */
    private int speedStopping;
    // Maximum speed that ship can have without having a crash when landing.
    public int topLandingSpeed;
    //Remaining ship fuel
    public int shipFuel;
    //Amount of fuel used
    public int fuelUsed;
    //How fast and in which direction is the ship moving on x coordinate?
    private int speedX;
    //How fast and in which direction is the ship moving on y coordinate?
    public int speedY;
    //Image of the ship in air
    public BufferedImage shipImg;
    //URL of the image of the ship
    public URL shipColour;
    //Image of the ship when landed
    private BufferedImage shipLandedImg;
    //Image of the ship when crashed
    private BufferedImage shipCrashedImg;
    //Image of the rocket fire
    private BufferedImage shipFireImg;
    //Width of ship.
    public int shipImgWidth;
    //Height of ship.
    public int shipImgHeight;

    public Player() {

        Initialize();
        LoadContent();

        // Set starting x coordinate within the applicable area
        x = random.nextInt(Framework.frameWidth - shipImgWidth);
    }

    private void Initialize() {
        random = new Random();

        ResetPlayer();

        speedAccelerating = 2;
        speedStopping = 1;

        topLandingSpeed = 10;

        shipFuel = 100;
        fuelUsed = 0;
    }

    //Load the images used for movement, crashed and landed
    private void LoadContent() {
        try {

            //Default ship colour
            URL lbShipImgUrl = this.getClass().getResource("Images/ShipColours/Light Blue Ship.png");
            //Checks the current colour selected
            CheckColour();
            //Sets the selected colour of the ship
            if (shipColour != null) {
                shipImg = ImageIO.read(shipColour);
                shipImgWidth = shipImg.getWidth();
                shipImgHeight = shipImg.getHeight();
            } else {
                shipImg = ImageIO.read(lbShipImgUrl);
                shipImgWidth = shipImg.getWidth();
                shipImgHeight = shipImg.getHeight();
            }

            URL shipLandedImgUrl = this.getClass().getResource("Images/Green Flag.png");
            shipLandedImg = ImageIO.read(shipLandedImgUrl);

            URL shipCrashedImgUrl = this.getClass().getResource("Images/Explosion.png");
            shipCrashedImg = ImageIO.read(shipCrashedImgUrl);

            URL shipFireImgUrl = this.getClass().getResource("Images/Fire.png");
            shipFireImg = ImageIO.read(shipFireImgUrl);
        } catch (IOException ex) {
            Logger.getLogger(Player.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    //Set the ship when starting a new game.
    public void ResetPlayer() {
        landed = false;
        crashed = false;

        x = random.nextInt(Framework.frameWidth - shipImgWidth);
        y = 10;

        speedX = 0;
        speedY = 0;

        shipFuel = 100;
        fuelUsed = 0;
    }

    //Used to move the ship.
    public void Update() {
        // Calculating speed for moving up or down and fuel consumption
        if (shipFuel > 0) {
            if (Canvas.keyboardKeyState(KeyEvent.VK_UP)) {
                speedY -= speedAccelerating;
                shipFuel--;
                fuelUsed++;
            } else {
                speedY += speedStopping;
            }
        } else {
            speedY += speedStopping;
        }

        // Calculating speed for moving or stopping to the left.
        if (Canvas.keyboardKeyState(KeyEvent.VK_LEFT)) {
            speedX -= speedAccelerating;
        } else if (speedX < 0) {
            speedX += speedStopping;
        }

        // Calculating speed for moving or stopping to the right.
        if (Canvas.keyboardKeyState(KeyEvent.VK_RIGHT)) {
            speedX += speedAccelerating;
        } else if (speedX > 0) {
            speedX -= speedStopping;
        }

        // Moves the ship.
        x += speedX;
        y += speedY;
        URL lbShipImgUrl = this.getClass().getResource("Images/ShipColours/Light Blue Ship.png");
        // Ensures that the currently select colour of the ship will still allow movement
            CheckColour();
            try {
            if (shipColour != null) {
                shipImg = ImageIO.read(shipColour);
                shipImgWidth = shipImg.getWidth();
                shipImgHeight = shipImg.getHeight();
            } else {
                shipImg = ImageIO.read(lbShipImgUrl);
                shipImgWidth = shipImg.getWidth();
                shipImgHeight = shipImg.getHeight();
            }
            }
            catch (IOException ex) {
                Logger.getLogger(Player.class.getName()).log(Level.SEVERE, null, ex);
            }
    }
    
    //Checks the selected colour of the ship and provides the URL to said colour
    public void CheckColour() {
        URL lbShipImgUrl = this.getClass().getResource("Images/ShipColours/Light Blue Ship.png");
        URL dbShipImgUrl = this.getClass().getResource("Images/ShipColours/Dark Blue Ship.png");
        URL gShipImgUrl = this.getClass().getResource("Images/ShipColours/Green Ship.png");
        URL pShipImgUrl = this.getClass().getResource("Images/ShipColours/Purple Ship.png");
        URL rShipImgUrl = this.getClass().getResource("Images/ShipColours/Red Ship.png");
        URL bShipImgUrl = this.getClass().getResource("Images/ShipColours/Black Ship.png");

        if (Game.lightBlueClicked == true) {
            shipColour = lbShipImgUrl;
        } else if (Game.darkBlueClicked == true) {
            shipColour = dbShipImgUrl;
        } else if (Game.greenClicked == true) {
            shipColour = gShipImgUrl;
        } else if (Game.purpleClicked == true) {
            shipColour = pShipImgUrl;
        } else if (Game.redClicked == true) {
            shipColour = rShipImgUrl;
        } else if (Game.blackClicked == true) {
            shipColour = bShipImgUrl;
        }

    }

        //Draws additional ship states and current ship information
    public void Draw(Graphics2D g2d) {
        Color nightColor = new Color(0f, 0f, 0f, 0.85f);
        Graphics2D fuel = (Graphics2D) g2d.create();
        Graphics2D usedFuel = (Graphics2D) g2d.create();
        Graphics2D speed = (Graphics2D) g2d.create();
        Graphics2D timeOverlay = (Graphics2D) g2d.create();



        // If the ship is landed.
        if (landed) {
            g2d.drawImage(shipImg, x, y, null);
            g2d.drawImage(shipLandedImg, x + 33, y - 52, null);
        } // If the ship is crashed.
        else if (crashed) {
            g2d.drawImage(shipCrashedImg, x, y + shipImgHeight - shipCrashedImg.getHeight(), null);
        } // If the ship is still in the space.
        else {
            // If player is holding down the UP key, draw ship fire.
            if (shipFuel > 0) {
                if (Canvas.keyboardKeyState(KeyEvent.VK_UP)) {
                    g2d.drawImage(shipFireImg, x + 18, y + 66, null);
                }
            } else {
            }

            //Draws the speed, fuel and coordinate information
            g2d.drawImage(shipImg, x, y, null);
            g2d.setColor(Color.black);
            g2d.drawString("Ship coordinates", (Framework.frameWidth + 295), 610);
            g2d.drawString("X: " + x, (Framework.frameWidth + 315), 630);
            g2d.drawString("Y: " + y, (Framework.frameWidth + 315), 650);
            g2d.setColor(Color.black);
            g2d.drawString("Ship coordinates", (Framework.frameWidth + 295), 610);
            g2d.drawString("X: " + x, (Framework.frameWidth + 315), 630);
            g2d.drawString("Y: " + y, (Framework.frameWidth + 315), 650);
            g2d.drawString("Ship speed", (Framework.frameWidth + 200), 650);
            g2d.drawString(speedY + "m/s", (Framework.frameWidth + 210), 630);
            g2d.drawString("Fuel remaining", (Framework.frameWidth + 20), 650);
            g2d.drawString(shipFuel + "%", (Framework.frameWidth + 50), 667);
            fuel.setColor(Color.lightGray);
            usedFuel.setColor(Color.lightGray);
            fuel.drawRect(Framework.frameWidth + 45, 530, 25, 100);
            fuel.fillRect(Framework.frameWidth + 45, 530, 25, 100);
            if (shipFuel >= 70) {
                fuel.setColor(Color.green);
                fuel.fillRect(Framework.frameWidth + 45, 530, 25, 100);
                usedFuel.drawRect(Framework.frameWidth + 45, 530, 25, fuelUsed);
                usedFuel.fillRect(Framework.frameWidth + 45, 530, 25, fuelUsed);
            } else if (shipFuel < 70 && shipFuel >= 40) {
                fuel.setColor(Color.yellow);
                fuel.fillRect(Framework.frameWidth + 45, 530, 25, 100);
                usedFuel.drawRect(Framework.frameWidth + 45, 530, 25, fuelUsed);
                usedFuel.fillRect(Framework.frameWidth + 45, 530, 25, fuelUsed);
            } else if (shipFuel < 40 && shipFuel > 0) {
                fuel.setColor(Color.red);
                fuel.fillRect(Framework.frameWidth + 45, 530, 25, 100);
                usedFuel.drawRect(Framework.frameWidth + 45, 530, 25, fuelUsed);
                usedFuel.fillRect(Framework.frameWidth + 45, 530, 25, fuelUsed);
            } else {
                fuel.setColor(Color.lightGray);
                fuel.drawRect(Framework.frameWidth + 45, 530, 25, 100);
                fuel.fillRect(Framework.frameWidth + 45, 530, 25, 100);
            }
            speed.setColor(Color.lightGray);
            speed.drawRect(Framework.frameWidth + 210, 510, 25, 100);
            if (speedY < 0) {
                speed.setColor(Color.lightGray);
                speed.fillRect(Framework.frameWidth + 210, 510, 25, 100);
            } else if (speedY < 7) {
                speed.setColor(Color.green);
                speed.fillRect(Framework.frameWidth + 210, 510, 25, 100);
                speed.setColor(Color.lightGray);
                speed.fillRect(Framework.frameWidth + 210, 510, 25, 100 - (speedY * 3));
            } else if (speedY > 7 && speedY < 10) {
                speed.setColor(Color.yellow);
                speed.fillRect(Framework.frameWidth + 210, 510, 25, 100);
                speed.setColor(Color.lightGray);
                speed.fillRect(Framework.frameWidth + 210, 510, 25, 100 - (speedY * 3));
            } else {
                speed.setColor(Color.red);
                speed.fillRect(Framework.frameWidth + 210, 510, 25, 100);
                speed.setColor(Color.lightGray);
                speed.fillRect(Framework.frameWidth + 210, 510, 25, 100 - (speedY * 3));
            }
            
            //Draws an tranlucent overlay if it is night
            if (Game.dayClicked == true) {

            }
            else if (Game.nightClicked == true) {
                timeOverlay.setColor(nightColor);
                timeOverlay.drawRect(0, 0, Framework.frameWidth, Framework.frameHeight);
                timeOverlay.fillRect(0, 0, Framework.frameWidth, Framework.frameHeight);
            }

        }
    }
}
