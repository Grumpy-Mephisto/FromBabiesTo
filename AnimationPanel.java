import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.util.Stack;
import javax.swing.*;

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

    public AnimationPanel() {
        initializeCanvas();
        initializeTimer();
    }

    private void initializeCanvas() {
        buffer = new BufferedImage(CANVAS_WIDTH, CANVAS_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        bufferGraphics = buffer.createGraphics();
        bufferGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        characterPosition = new Point(0, 0);
        characterVelocity = new Point(5, 5);
    }

    private void initializeTimer() {
        timer = new Timer(TIMER_DELAY, e -> {
            updateCharacter();
            repaint();
        });
        timer.start();
    }

    private void updateCharacter() {
        characterAge = (characterAge + 1) % MAX_AGE;

        updatePosition();

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

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(buffer, 0, 0, null);
    }

    private void drawBackground(Graphics2D g2d) {
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, getWidth(), getHeight());
    }

    private void drawCharacter(Graphics2D g2d) {
        tRexRun(g2d);
    }

    private void tRexRun(Graphics2D g2d) {
        g2d.setColor(Color.RED);

        int size = 40 + (characterAge / 5);

        // Draw the body

    }


    private void floodFill(BufferedImage image, int x, int y, Color targetColor,
            Color replacementColor) {
        int width = image.getWidth();
        int height = image.getHeight();
        if (x < 0 || x >= width || y < 0 || y >= height)
            return;

        int targetRGB = targetColor.getRGB();
        if (image.getRGB(x, y) != targetRGB)
            return;

        Stack<Point> stack = new Stack<>();
        stack.push(new Point(x, y));

        while (!stack.isEmpty()) {
            Point p = stack.pop();
            int px = p.x;
            int py = p.y;

            if (px < 0 || px >= width || py < 0 || py >= height)
                continue;
            if (image.getRGB(px, py) != targetRGB)
                continue;

            image.setRGB(px, py, replacementColor.getRGB());

            stack.push(new Point(px + 1, py));
            stack.push(new Point(px - 1, py));
            stack.push(new Point(px, py + 1));
            stack.push(new Point(px, py - 1));
        }
    }


    private void drawBresenhamLine(Graphics2D g2d, int x1, int y1, int x2, int y2) {
        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);

        int sx = x1 < x2 ? 1 : -1;
        int sy = y1 < y2 ? 1 : -1;

        int err = dx - dy;
        int e2;

        while (true) {
            g2d.fillRect(x1, y1, 1, 1); // Draw the pixel at (x1, y1)

            if (x1 == x2 && y1 == y2) {
                break;
            }

            e2 = 2 * err;
            if (e2 > -dy) {
                err -= dy;
                x1 += sx;
            }

            if (e2 < dx) {
                err += dx;
                y1 += sy;
            }
        }
    }


    private void drawBezierCurve(Graphics2D g2d, Point p1, Point p2, Point p3) {
        Path2D path = new Path2D.Double();
        path.moveTo(p1.x, p1.y);
        path.quadTo(p2.x, p2.y, p3.x, p3.y);
        g2d.draw(path);
    }

    private void drawMidpointCircle(Graphics2D g2d, int xCenter, int yCenter, int radius) {
        int x = radius, y = 0;
        int P = 1 - radius;

        while (x > y) {
            y++;

            if (P <= 0) {
                P = P + 2 * y + 1;
            } else {
                x--;
                P = P + 2 * y - 2 * x + 1;
            }

            if (x < y) {
                break;
            }

            plotCirclePoints(g2d, xCenter, yCenter, x, y);
            if (x != y) {
                plotCirclePoints(g2d, xCenter, yCenter, y, x);
            }
        }
    }

    private void plotCirclePoints(Graphics2D g2d, int xCenter, int yCenter, int x, int y) {
        g2d.fillRect(xCenter + x, yCenter + y, 1, 1);
        g2d.fillRect(xCenter - x, yCenter + y, 1, 1);
        g2d.fillRect(xCenter + x, yCenter - y, 1, 1);
        g2d.fillRect(xCenter - x, yCenter - y, 1, 1);
        g2d.fillRect(xCenter + y, yCenter + x, 1, 1);
        g2d.fillRect(xCenter - y, yCenter + x, 1, 1);
        g2d.fillRect(xCenter + y, yCenter - x, 1, 1);
        g2d.fillRect(xCenter - y, yCenter - x, 1, 1);
    }
}
