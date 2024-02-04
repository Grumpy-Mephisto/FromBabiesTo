import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import javax.swing.*;
import java.util.List;

public class AnimationPanel extends JPanel {
    private static final int CANVAS_WIDTH = 600;
    private static final int CANVAS_HEIGHT = 600;
    private static final int TIMER_DELAY = 100;
    private static final int MAX_AGE = 100;

    private Timer timer;
    private BufferedImage buffer;
    private Graphics2D bufferGraphics;
    private int characterAge = 0;
    private Point characterPosition; // Character position
    private Point characterVelocity; // Movement speed
    private List<Point> clouds; // List of cloud positions
    private double earthRotationAngle = 0; // Earth rotation angle

    private int Speed = 1; // Speed of the character

    public AnimationPanel() {
        initializeCanvas();
        initializeTimer();
        initializeClouds();
    }

    private void initializeCanvas() {
        setPreferredSize(new Dimension(CANVAS_WIDTH, CANVAS_HEIGHT));
        buffer = new BufferedImage(CANVAS_WIDTH, CANVAS_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        bufferGraphics = buffer.createGraphics();
        bufferGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        characterPosition = new Point(CANVAS_WIDTH / 2, CANVAS_HEIGHT / 2); // Start in the center
        characterVelocity = new Point(Speed, Speed); // Initial velocity
    }

    private void initializeTimer() {
        timer = new Timer(TIMER_DELAY, e -> {
            updateCharacter();
            repaint();
        });
        timer.start();
    }

    private void initializeClouds() {
        clouds = new java.util.ArrayList<>();
        for (int i = 0; i < 5; i++) { // Create 5 cloud positions
            clouds.add(new Point((int) (Math.random() * CANVAS_WIDTH),
                    (int) (Math.random() * CANVAS_HEIGHT)));
        }
    }

    private void updateCharacter() {
        characterAge = (characterAge + 1) % MAX_AGE;

        updatePosition();
        updateClouds(); // Update clouds for animation
        earthRotationAngle += 0.5; // Increment the Earth's rotation angle

        bufferGraphics.setComposite(AlphaComposite.Clear);
        bufferGraphics.fillRect(0, 0, buffer.getWidth(), buffer.getHeight());
        bufferGraphics.setComposite(AlphaComposite.SrcOver);

        drawBackground(bufferGraphics);
        drawCharacter(bufferGraphics);
    }

    private void updatePosition() {
        int size = 40 + (characterAge / 5);
        characterPosition.x += characterVelocity.x;
        characterPosition.y += characterVelocity.y;

        // Boundary checking
        if (characterPosition.x - size / 2 < 0 || characterPosition.x + size / 2 > getWidth()) {
            characterVelocity.x *= -1;
            characterPosition.x =
                    Math.max(size / 2, Math.min(characterPosition.x, getWidth() - size / 2));
        }
        if (characterPosition.y - size / 2 < 0 || characterPosition.y + size / 2 > getHeight()) {
            characterVelocity.y *= -1;
            characterPosition.y =
                    Math.max(size / 2, Math.min(characterPosition.y, getHeight() - size / 2));
        }
    }

    private void updateClouds() {
        for (Point cloud : clouds) {
            cloud.x += characterVelocity.x;
            cloud.y += characterVelocity.y;

            if (cloud.x > CANVAS_WIDTH)
                cloud.x = -50;
            if (cloud.y > CANVAS_HEIGHT)
                cloud.y = -50;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        g2d.drawImage(buffer, 0, 0, this);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    }

    private void drawBackground(Graphics2D g2d) {
        GradientPaint spaceGradient = new GradientPaint(0, 0, new Color(5, 10, 20), CANVAS_WIDTH,
                CANVAS_HEIGHT, new Color(10, 20, 40));
        g2d.setPaint(spaceGradient);
        g2d.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
    }

    private void drawCharacter(Graphics2D g2d) {
        drawEarth(g2d);
    }

    private void drawEarth(Graphics2D g2d) {
        int centerX = characterPosition.x;
        int centerY = characterPosition.y;
        int earthRadius = 40 + (characterAge / 5);

        // Draw the blue ocean
        g2d.setColor(Colors.OCEAN_BLUE);
        drawMidpointCircle(g2d, centerX, centerY, earthRadius, true);

        // Rotate the Earth
        AffineTransform oldTransform = g2d.getTransform();
        g2d.rotate(Math.toRadians(earthRotationAngle), centerX, centerY);

        // Draw Continents
        g2d.setColor(Colors.FOREST_GREEN);
        drawContinents(g2d, centerX, centerY, earthRadius);

        // Reset the transform for other drawings
        g2d.setTransform(oldTransform);

        // Draw Clouds
        g2d.setColor(Colors.CLOUD_WHITE);
        drawClouds(g2d, centerX, centerY, earthRadius);
    }

    private void drawContinents(Graphics2D g2d, int centerX, int centerY, int earthRadius) {}

    private void drawClouds(Graphics2D g2d, int centerX, int centerY, int earthRadius) {}

    /**
     * Draws a circle using the midpoint circle algorithm. This method plots eight octants
     * simultaneously to form a complete circle.
     *
     * @param g2d the Graphics2D context to draw on.
     * @param xCenter the x-coordinate of the circle's center.
     * @param yCenter the y-coordinate of the circle's center.
     * @param radius the radius of the circle. Must be non-negative.
     * @param fill true to fill the circle, false to draw the outline only.
     * @see #drawMidpointEllipse(Graphics2D, int, int, int, int, boolean)
     */
    public static void drawMidpointCircle(Graphics2D g2d, int xCenter, int yCenter, int radius,
            boolean fill) {
        if (radius < 0) {
            throw new IllegalArgumentException("Radius cannot be negative.");
        }

        int x = 0;
        int y = radius;
        int D = 1 - radius;
        int Dx = 1; // Initial value of 2 * x + 1
        int Dy = -2 * radius;
        while (x <= y) {
            if (fill) {
                g2d.drawLine(xCenter - x, yCenter + y, xCenter + x, yCenter + y);
                g2d.drawLine(xCenter - x, yCenter - y, xCenter + x, yCenter - y);
                g2d.drawLine(xCenter - y, yCenter + x, xCenter + y, yCenter + x);
                g2d.drawLine(xCenter - y, yCenter - x, xCenter + y, yCenter - x);
            } else {
                g2d.fillRect(xCenter + x, yCenter + y, 1, 1);
                g2d.fillRect(xCenter - x, yCenter + y, 1, 1);
                g2d.fillRect(xCenter + x, yCenter - y, 1, 1);
                g2d.fillRect(xCenter - x, yCenter - y, 1, 1);
                g2d.fillRect(xCenter + y, yCenter + x, 1, 1);
                g2d.fillRect(xCenter - y, yCenter + x, 1, 1);
                g2d.fillRect(xCenter + y, yCenter - x, 1, 1);
                g2d.fillRect(xCenter - y, yCenter - x, 1, 1);
            }

            x++;
            Dx += 2;
            D += Dx;
            if (D >= 0) {
                y--;
                Dy += 2;
                D += Dy;
            }
        }
    }

    /**
     * Draws an ellipse using the midpoint ellipse algorithm. This method plots four symmetrical
     * arcs simultaneously to form a complete ellipse.
     * 
     * @param g2d the Graphics2D context to draw on.
     * @param xCenter the x-coordinate of the ellipse's center.
     * @param yCenter the y-coordinate of the ellipse's center.
     * @param a the semi-major axis of the ellipse. Must be non-negative.
     * @param b the semi-minor axis of the ellipse. Must be non-negative.
     * @param fill true to fill the ellipse, false to draw the outline only.
     * @see #drawMidpointCircle(Graphics2D, int, int, int, boolean)
     */
    public static void drawMidpointEllipse(Graphics2D g2d, int xCenter, int yCenter, int a, int b,
            boolean fill) {
        int a2 = a * a;
        int b2 = b * b;
        int twoA2 = 2 * a2;
        int twoB2 = 2 * b2;
        int x = 0;
        int y = b;
        int Dx = 0;
        int Dy = twoA2 * y;
        int D = Math.round(b2 - (a2 * b) + (0.25f * a2));

        while (Dx <= Dy) {
            if (fill) {
                g2d.drawLine(xCenter - x, yCenter + y, xCenter + x, yCenter + y);
                g2d.drawLine(xCenter - x, yCenter - y, xCenter + x, yCenter - y);
            } else {
                g2d.fillRect(xCenter + x, yCenter + y, 1, 1);
            }

            x++;
            Dx += twoB2;
            D += Dx + b2;
            if (D >= 0) {
                y--;
                Dy -= twoA2;
                D -= Dy;
            }
        }

        x = a;
        y = 0;
        Dx = twoB2 * x;
        Dy = 0;
        D = Math.round(a2 - (b2 * a) + (0.25f * b2));
        while (Dx >= Dy) {
            if (fill) {
                g2d.drawLine(xCenter - x, yCenter + y, xCenter + x, yCenter + y);
                g2d.drawLine(xCenter - x, yCenter - y, xCenter + x, yCenter - y);
            } else {
                g2d.fillRect(xCenter + x, yCenter + y, 1, 1);
            }

            y++;
            Dy += twoA2;
            D += Dy + a2;
            if (D >= 0) {
                x--;
                Dx -= twoB2;
                D -= Dx;
            }
        }
    }
}
