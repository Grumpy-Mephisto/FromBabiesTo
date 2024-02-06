import java.awt.*;
import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.awt.geom.AffineTransform;

public class AnimationPanel extends JPanel implements Runnable {
    private static final int CANVAS_WIDTH = 600;
    private static final int CANVAS_HEIGHT = 600;
    private static final int FRAME_DELAY = 100;
    private static final int MAX_AGE = 100;

    private Thread animator;
    private boolean running = false;
    private BufferedImage buffer;
    private Graphics2D bufferGraphics;
    private int characterAge = 0;
    private Point characterPosition; // Character position
    private Point characterVelocity; // Movement speed
    private List<Point> clouds; // List of cloud positions
    private double earthRotationAngle = 0; // Earth rotation angle

    // Earth
    boolean isExploding = false;
    private int characterSpeed = 1;

    // Sun
    private double sunPulseAngle = 0; // Sun pulsation angle

    public AnimationPanel() {
        initializeCanvas();
        initializeClouds();
        startAnimation();
    }

    private void initializeCanvas() {
        setPreferredSize(new Dimension(CANVAS_WIDTH, CANVAS_HEIGHT));
        buffer = new BufferedImage(CANVAS_WIDTH, CANVAS_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        bufferGraphics = buffer.createGraphics();
        bufferGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        characterPosition = new Point(CANVAS_WIDTH / 2, CANVAS_HEIGHT / 2); // Start in the center
        characterVelocity = new Point(characterSpeed, characterSpeed); // Initial velocity
    }

    private void initializeClouds() {
        clouds = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            clouds.add(new Point((int) (Math.random() * CANVAS_WIDTH),
                    (int) (Math.random() * CANVAS_HEIGHT)));
        }
    }

    private void startAnimation() {
        running = true;
        animator = new Thread(this);
        animator.start();
    }

    public void stopAnimation() {
        running = false;
        try {
            animator.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void run() {
        while (running) {
            updateCharacter();
            repaint();

            try {
                Thread.sleep(FRAME_DELAY);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void clearBuffer() {
        bufferGraphics.setComposite(AlphaComposite.Clear);
        bufferGraphics.fillRect(0, 0, buffer.getWidth(), buffer.getHeight());
        bufferGraphics.setComposite(AlphaComposite.SrcOver);
    }

    private void drawScene() {
        drawBackground(bufferGraphics);
        if (isExploding) {
            drawExplosion(bufferGraphics, characterPosition.x, characterPosition.y);
        } else {
            drawCharacter(bufferGraphics);
        }
    }

    private void updateCharacter() {
        if (!isExploding) {
            characterAge++;
            if (characterAge >= MAX_AGE) {
                isExploding = true;
                characterAge = MAX_AGE;
            }
        } else {
            characterAge--;
            if (characterAge <= 0) {
                isExploding = false;
                characterAge = 0;
                characterPosition = new Point(CANVAS_WIDTH / 2, CANVAS_HEIGHT / 2);
            }
        }

        if (!isExploding) {
            updatePosition();
            earthRotationAngle += 0.5;
        }

        clearBuffer();
        drawScene();
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

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        g2d.drawImage(buffer, 0, 0, this);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    }

    private void drawBackground(Graphics2D g2d) {
        fillSpaceBackground(g2d);
        drawStars(g2d);
    }

    private void fillSpaceBackground(Graphics2D g2d) {
        GradientPaint spaceGradient = new GradientPaint(0, 0, new Color(5, 10, 20), CANVAS_WIDTH,
                CANVAS_HEIGHT, new Color(10, 20, 40));
        g2d.setPaint(spaceGradient);
        g2d.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
    }

    private void drawStars(Graphics2D g2d) {
        g2d.setColor(Color.WHITE);
        for (int i = 0; i < 100; i++) {
            float alpha = 0.1f + (float) Math.random() * 0.1f; // Semi-transparent
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

            int xCenter = (int) (Math.random() * CANVAS_WIDTH);
            int yCenter = (int) (Math.random() * CANVAS_HEIGHT);

            int radius = 1;

            for (int j = 0; j < 8; j++) {
                int x1 = xCenter + (int) (radius * Math.cos(j * Math.PI / 4));
                int y1 = yCenter + (int) (radius * Math.sin(j * Math.PI / 4));
                int x2 = xCenter + (int) (radius * Math.cos((j + 1) * Math.PI / 4));
                int y2 = yCenter + (int) (radius * Math.sin((j + 1) * Math.PI / 4));
                g2d.drawLine(x1, y1, x2, y2);
            }
        }

        // Reset alpha composite to fully opaque
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
    }

    private void drawCharacter(Graphics2D g2d) {
        drawSun(g2d);
        drawEarth(g2d);
    }

    private void drawSun(Graphics2D g2d) {
        int centerX = CANVAS_WIDTH / 4;
        int centerY = CANVAS_HEIGHT / 4;
        int sunRadius = 100;

        sunPulseAngle += 0.05; // Adjust the speed of the pulsation
        double sunPulse = Math.sin(sunPulseAngle) * 5; // Adjust the strength of the pulsation

        RadialGradientPaint sunGradient =
                new RadialGradientPaint(new Point2D.Double(centerX, centerY),
                        (float) (sunRadius + sunPulse), new float[] {0.0f, 0.8f, 1.0f},
                        new Color[] {Colors.SUN_YELLOW, Colors.SUN_ORANGE, Colors.SUN_RED});

        g2d.setPaint(sunGradient);

        drawMidpointEllipse(g2d, centerX, centerY, (int) (sunRadius + sunPulse),
                (int) (sunRadius + sunPulse), true);

        // Draw the sun's flare
        g2d.setColor(new Color(255, 255, 255, 32)); // Semi-transparent white
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
        g2d.fillOval(centerX - sunRadius / 2, centerY - sunRadius / 2, sunRadius, sunRadius);

        // Reset alpha composite to fully opaque
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
    }

    private void drawEarth(Graphics2D g2d) {
        int centerX = characterPosition.x;
        int centerY = characterPosition.y;
        int earthRadius = 40 + (characterAge / 5);

        // Light source direction
        double lightAngle = Math.toRadians(earthRotationAngle + 45); // Adjust the angle as needed

        // Create an off-screen image
        BufferedImage earthImage =
                new BufferedImage(earthRadius * 2, earthRadius * 2, BufferedImage.TYPE_INT_ARGB);
        Graphics2D earthG2d = earthImage.createGraphics();
        earthG2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw the base blue ocean
        earthG2d.setColor(Colors.OCEAN_BLUE);
        drawMidpointCircle(earthG2d, earthRadius, earthRadius, earthRadius, true);

        // Set clipping area for the continents and atmospheric effects on the off-screen image
        earthG2d.setClip(new Ellipse2D.Double(0, 0, earthRadius * 2, earthRadius * 2));

        // Draw Continents
        drawContinents(earthG2d, earthRadius, earthRadius, earthRadius);

        // Apply a shadow
        applyShadow(earthG2d, earthRadius, lightAngle);

        // Draw the Atmosphere
        drawAtmosphere(earthG2d, earthRadius, earthRadius, earthRadius);

        // Dispose off-screen image graphics context
        earthG2d.dispose();

        // Rotate the Earth
        AffineTransform oldTransform = g2d.getTransform();
        g2d.rotate(Math.toRadians(earthRotationAngle), centerX, centerY);

        // Draw the off-screen image of the Earth
        g2d.drawImage(earthImage, centerX - earthRadius, centerY - earthRadius, null);

        // Reset the transform for other drawings
        g2d.setTransform(oldTransform);

        // Set clipping area for the clouds
        g2d.setClip(new Ellipse2D.Double(centerX - earthRadius, centerY - earthRadius,
                earthRadius * 2, earthRadius * 2));

        // Draw Clouds
        drawClouds(g2d, centerX, centerY, earthRadius);

        // Reset the clipping area
        g2d.setClip(null);
    }

    private void drawExplosion(Graphics2D g2d, int xCenter, int yCenter) {
        int numberOfParticles = 50;
        int maxRadius = 100;

        for (int i = 0; i < numberOfParticles; i++) {
            Color particleColor = new Color((int) (Math.random() * 256),
                    (int) (Math.random() * 256), (int) (Math.random() * 256), 150); // Semi-transparent

            int particleSize = (int) (Math.random() * 15 + 5); // Random size between 5 and 20
            double angle = Math.random() * 2 * Math.PI; // Random direction
            int distance = (int) (Math.random() * maxRadius); // Random distance from the center

            int particleX = xCenter + (int) (distance * Math.cos(angle)) - particleSize / 2;
            int particleY = yCenter + (int) (distance * Math.sin(angle)) - particleSize / 2;

            g2d.setColor(particleColor);
            g2d.fillOval(particleX, particleY, particleSize, particleSize);
        }
    }


    private void applyShadow(Graphics2D g2d, int earthRadius, double lightAngle) {
        // Calculate the shadow angle
        double shadowAngle = lightAngle + Math.PI;

        // Create a gradient paint that simulates a shadow for depth
        float[] dist = {0.0f, 1.0f};
        Point2D center = new Point2D.Double(earthRadius + earthRadius * Math.cos(shadowAngle),
                earthRadius + earthRadius * Math.sin(shadowAngle));
        Color[] colors = {new Color(0, 0, 0, 100), new Color(0, 0, 0, 0)};
        RadialGradientPaint p = new RadialGradientPaint(center, earthRadius, dist, colors);

        g2d.setPaint(p);
        g2d.fillOval(0, 0, earthRadius * 2, earthRadius * 2);
    }

    private void drawContinents(Graphics2D g2d, int centerX, int centerY, int earthRadius) {
        double scale = earthRadius / 40.0;

        // North America
        g2d.setColor(Colors.FOREST_GREEN);

        Path2D.Double continent1 = new Path2D.Double();
        continent1.moveTo(centerX - 15 * (scale * 2), centerY - 5 * (scale * 2));
        continent1.curveTo(centerX - 20 * (scale * 2), centerY - 30 * (scale * 2),
                centerX - 5 * (scale * 2), centerY - 20 * (scale * 2), centerX - 10 * (scale * 2),
                centerY - 5 * (scale * 2));
        continent1.curveTo(centerX - 10 * (scale * 2), centerY - 5 * (scale * 2),
                centerX - 10 * (scale * 2), centerY - 5 * (scale * 2), centerX - 15 * (scale * 2),
                centerY - 5 * (scale * 2));
        continent1.closePath();
        g2d.fill(continent1);

        // Africa
        g2d.setColor(Colors.DESERT_BROWN);

        Path2D.Double continent2 = new Path2D.Double();
        continent2.moveTo(centerX, centerY);
        continent2.curveTo(centerX + 10 * scale, centerY - 10 * scale, centerX + 5 * scale,
                centerY - 30 * scale, centerX, centerY - 15 * scale);
        continent2.closePath();
        g2d.fill(continent2);

        // Europe
        g2d.setColor(Colors.MOUNTAIN_GRAY);

        Path2D.Double continent3 = new Path2D.Double();
        continent3.moveTo(centerX + 5 * scale, centerY - 10 * scale);
        continent3.curveTo(centerX + 20 * scale, centerY - 15 * scale, centerX + 15 * scale,
                centerY, centerX + 5 * scale, centerY - 5 * scale);
        continent3.closePath();
        g2d.fill(continent3);

        // South America
        g2d.setColor(Colors.FOREST_GREEN);

        Path2D.Double continent4 = new Path2D.Double();
        continent4.moveTo(centerX - 15 * scale, centerY + 5 * scale);
        continent4.curveTo(centerX - 20 * scale, centerY + 30 * scale, centerX - 5 * scale,
                centerY + 20 * scale, centerX - 10 * scale, centerY + 5 * scale);
        continent4.closePath();
        g2d.fill(continent4);

        // Asia
        g2d.setColor(Colors.MOUNTAIN_GRAY);

        Path2D.Double continent5 = new Path2D.Double();
        continent5.moveTo(centerX + 10 * scale, centerY - 5 * scale);
        continent5.curveTo(centerX + 30 * scale, centerY - 10 * scale, centerX + 20 * scale,
                centerY - 30 * scale, centerX + 10 * scale, centerY - 15 * scale);
        continent5.closePath();
        g2d.fill(continent5);

        // Australia
        g2d.setColor(Colors.DESERT_BROWN);

        Path2D.Double continent6 = new Path2D.Double();
        continent6.moveTo(centerX + 10 * scale, centerY + 5 * scale);
        continent6.curveTo(centerX + 30 * scale, centerY + 10 * scale, centerX + 20 * scale,
                centerY + 30 * scale, centerX + 10 * scale, centerY + 15 * scale);
        continent6.closePath();
        g2d.fill(continent6);
    }

    private void drawAtmosphere(Graphics2D g2d, int centerX, int centerY, int earthRadius) {
        float[] dist = {0.7f, 1.0f};
        Color[] colors = {new Color(255, 255, 255, 64), new Color(255, 255, 255, 0)};
        RadialGradientPaint p = new RadialGradientPaint(new Point2D.Float(centerX, centerY),
                earthRadius * 1.1f, dist, colors);
        g2d.setPaint(p);
        g2d.fillOval(centerX - earthRadius, centerY - earthRadius, earthRadius * 2,
                earthRadius * 2);
    }

    private void drawClouds(Graphics2D g2d, int centerX, int centerY, int earthRadius) {
        g2d.setColor(Colors.CLOUD_WHITE);

        int maxClouds = 6; // Adjust the number of clouds
        for (int i = 0; i < maxClouds; i++) {
            int cloudSize = earthRadius / 4 + (int) (Math.random() * (earthRadius / 4));
            int x = centerX - cloudSize + (int) (Math.random() * earthRadius) - earthRadius / 2;
            int y = centerY - cloudSize + (int) (Math.random() * earthRadius) - earthRadius / 2;

            Path2D.Double cloud = new Path2D.Double();
            cloud.moveTo(x, y);
            int numFluffs = 3 + (int) (Math.random() * 3) + 1; // 3 to 6 fluffs
            for (int j = 0; j < numFluffs; j++) {
                x += cloudSize / numFluffs;
                double angle1 = Math.random() * Math.PI / 2;
                double angle2 = Math.random() * Math.PI / 2;
                cloud.curveTo(x + cloudSize * Math.cos(angle1), y - cloudSize * Math.sin(angle1),
                        x + cloudSize * Math.cos(angle2), y + cloudSize * Math.sin(angle2), x, y);
            }
            cloud.closePath();

            float opacity = 0.5f + (float) Math.random() * 0.3f; // Semi-transparent
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
            g2d.fill(cloud);
        }

        // Reset alpha composite to fully opaque
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
    }

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
