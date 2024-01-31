import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Ellipse2D;

public class AnimationPanel extends JPanel {
    private Timer timer;
    private int slimeRadius = 15;
    private int slimeX, slimeY;
    private int xVelocity = 4;
    private int yVelocity = 4;
    private final int maxRadius = 60;
    private final Color slimeColor = new Color(50, 205, 50, 180); // Semi-transparent

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
            xVelocity *= -1;
            slimeRadius -= 5;
        }
        if (slimeY - slimeRadius < 0 || slimeY + slimeRadius > getHeight()) {
            yVelocity *= -1;
            slimeRadius -= 5;
        }
        slimeRadius = Math.max(15, slimeRadius);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        drawSlime(g2d, slimeX, slimeY, slimeRadius);
    }

    private void drawSlime(Graphics2D g2d, int x, int y, int radius) {
        int ellipseWidth = radius;
        int ellipseHeight = (int) (radius * 0.7);

        midpointEllipse(g2d, x, y, ellipseWidth, ellipseHeight);

        GradientPaint gradient = new GradientPaint(x, y - ellipseHeight, slimeColor.darker(), x,
                y + ellipseHeight, slimeColor.brighter());
        g2d.setPaint(gradient);
        g2d.fill(new Ellipse2D.Double(x - ellipseWidth, y - ellipseHeight, 2 * ellipseWidth,
                2 * ellipseHeight));
    }

    private void midpointEllipse(Graphics2D g2d, int x, int y, int width, int height) {
        int rxSq = width * width;
        int rySq = height * height;
        int xe, ye, d1, d2, dx, dy;

        xe = 0;
        ye = height;
        d1 = (int) (rySq - (rxSq * height) + (0.25 * rxSq));
        dx = 2 * rySq * xe;
        dy = 2 * rxSq * ye;

        while (dx < dy) {
            plotEllipsePoints(g2d, x, y, xe, ye);
            if (d1 < 0) {
                xe++;
                dx += 2 * rySq;
                d1 += dx + rySq;
            } else {
                xe++;
                ye--;
                dx += 2 * rySq;
                dy -= 2 * rxSq;
                d1 += dx - dy + rySq;
            }
        }

        d2 = (int) ((rySq * ((xe + 0.5) * (xe + 0.5))) + (rxSq * ((ye - 1) * (ye - 1)))
                - (rxSq * rySq));

        while (ye >= 0) {
            plotEllipsePoints(g2d, x, y, xe, ye);
            if (d2 > 0) {
                ye--;
                dy -= 2 * rxSq;
                d2 += rxSq - dy;
            } else {
                ye--;
                xe++;
                dx += 2 * rySq;
                dy -= 2 * rxSq;
                d2 += dx - dy + rxSq;
            }
        }
    }

    private void plotEllipsePoints(Graphics2D g2d, int cx, int cy, int x, int y) {
        g2d.fillRect(cx + x, cy + y, 1, 1);
        g2d.fillRect(cx - x, cy + y, 1, 1);
        g2d.fillRect(cx + x, cy - y, 1, 1);
        g2d.fillRect(cx - x, cy - y, 1, 1);
    }
}
