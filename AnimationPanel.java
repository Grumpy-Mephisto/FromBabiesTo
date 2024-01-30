import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.Graphics;
import java.awt.Color;

public class AnimationPanel extends JPanel {
    private Timer timer;
    private int slimeSize = 30; // Starting size of the slime
    private int slimeX, slimeY; // Slime position
    private int xVelocity = 4; // Horizontal velocity
    private int yVelocity = 4; // Vertical velocity
    private final int maxSize = 120; // Maximum size the slime will grow to
    private final Color slimeColor = new Color(50, 205, 50); // Color of the slime

    public AnimationPanel() {
        // Initialize the slime at the center of the panel
        slimeX = 150;
        slimeY = 150;

        timer = new Timer(40, e -> {
            updateSlime();
            repaint();
        });
        timer.start();
    }

    private void updateSlime() {
        // Update the slime's position
        slimeX += xVelocity;
        slimeY += yVelocity;

        // Gradually change the slime's size to represent growth
        if (slimeSize < maxSize) {
            slimeSize++;
        }

        // Check for collisions with the panel's borders
        checkCollision();
    }

    private void checkCollision() {
        if (slimeX < 0 || slimeX > getWidth() - slimeSize) {
            xVelocity *= -1; // Reverse horizontal direction
        }
        if (slimeY < 0 || slimeY > getHeight() - slimeSize) {
            yVelocity *= -1; // Reverse vertical direction
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawSlime(g, slimeX, slimeY, slimeSize);
    }

    private void drawSlime(Graphics g, int x, int y, int size) {
        g.setColor(slimeColor);
        g.fillRect(x, y, size, size); // Drawing a filled square for the slime
    }
}
