import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.QuadCurve2D;

public class AnimationPanel extends JPanel {
    private Timer timer;
    private int slimeRadius = 15; // Starting radius of the slime
    private int slimeX, slimeY; // Slime center position
    private int xVelocity = 4; // Horizontal velocity
    private int yVelocity = 4; // Vertical velocity
    private final int maxRadius = 60; // Maximum radius the slime will grow to
    private final Color slimeColor = new Color(50, 205, 50); // Color of the slime

    public AnimationPanel() {
        slimeX = 150;
        slimeY = 150;

        timer = new Timer(40, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateSlime();
                repaint();
            }
        });
        timer.start();
    }

    private void updateSlime() {
        slimeX += xVelocity;
        slimeY += yVelocity;

        if (slimeRadius < maxRadius) {
            slimeRadius++;
        }

        checkCollision();
    }

    private void checkCollision() {
        if (slimeX - slimeRadius < 0 || slimeX + slimeRadius > getWidth()) {
            xVelocity *= -1; // Reverse horizontal direction
        }
        if (slimeY - slimeRadius < 0 || slimeY + slimeRadius > getHeight()) {
            yVelocity *= -1; // Reverse vertical direction
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawSlime(g, slimeX, slimeY, slimeRadius);
    }

    private void midpointCircle(Graphics g, int centerX, int centerY, int radius) {
        g.setColor(slimeColor);

        int x = 0;
        int y = radius;
        int d = 1 - radius;
        int deltaE = 3;
        int deltaSE = -2 * radius + 5;

        while (y > x) {
            if (d < 0) {
                d += deltaE;
                deltaE += 2;
                deltaSE += 2;
            } else {
                d += deltaSE;
                deltaE += 2;
                deltaSE += 4;
                y--;
            }
            x++;
            g.fillRect(centerX + x, centerY + y, 1, 1);
            g.fillRect(centerX + x, centerY - y, 1, 1);
            g.fillRect(centerX - x, centerY + y, 1, 1);
            g.fillRect(centerX - x, centerY - y, 1, 1);
            g.fillRect(centerX + y, centerY + x, 1, 1);
            g.fillRect(centerX + y, centerY - x, 1, 1);
            g.fillRect(centerX - y, centerY + x, 1, 1);
            g.fillRect(centerX - y, centerY - x, 1, 1);
        }
    }

    private void bresenhamLine(Graphics g, int x1, int y1, int x2, int y2) {
        g.setColor(slimeColor);

        int dx = x2 - x1;
        int dy = y2 - y1;
        int d = 2 * dy - dx;
        int deltaE = 2 * dy;
        int deltaNE = 2 * (dy - dx);
        int x = x1;
        int y = y1;

        while (x < x2) {
            if (d <= 0) {
                d += deltaE;
                x++;
            } else {
                d += deltaNE;
                x++;
                y++;
            }
            g.fillRect(x, y, 1, 1);
        }
    }

    private void bezierCurve(Graphics g, int x1, int y1, int x2, int y2, int x3, int y3) {
        g.setColor(slimeColor);

        QuadCurve2D curve = new QuadCurve2D.Float();
        curve.setCurve(x1, y1, x2, y2, x3, y3);
        Graphics2D g2 = (Graphics2D) g;
        g2.draw(curve);
    }

    private void drawSlime(Graphics g, int x, int y, int radius) {
        midpointCircle(g, x, y, radius);
        bresenhamLine(g, x - radius, y, x + radius, y);
        bezierCurve(g, x - radius, y, x, y + radius, x + radius, y);
    }
}
