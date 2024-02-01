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
    private LifeStage currentStage = LifeStage.BABY;

    private enum LifeStage {
        BABY, CHILD, TEENAGER, ADULT, ELDER
    }

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

        updateLifeStage();
        updatePosition();

        bufferGraphics.setComposite(AlphaComposite.Clear);
        bufferGraphics.fillRect(0, 0, buffer.getWidth(), buffer.getHeight());
        bufferGraphics.setComposite(AlphaComposite.SrcOver);

        drawBackground(bufferGraphics);
        drawCharacter(bufferGraphics);
    }

    private void updateLifeStage() {
        if (characterAge <= 20) {
            currentStage = LifeStage.BABY;
        } else if (characterAge <= 40) {
            currentStage = LifeStage.CHILD;
        } else if (characterAge <= 60) {
            currentStage = LifeStage.TEENAGER;
        } else if (characterAge <= 80) {
            currentStage = LifeStage.ADULT;
        } else {
            currentStage = LifeStage.ELDER;
        }
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
        g2d.setColor(getBackgroundColor());
        g2d.fillRect(0, 0, getWidth(), getHeight());
    }

    private Color getBackgroundColor() {
        switch (currentStage) {
            case BABY:
                return Color.PINK;
            case CHILD:
                return Color.CYAN;
            case TEENAGER:
                return Color.ORANGE;
            case ADULT:
                return Color.GREEN;
            case ELDER:
                return Color.GRAY;
            default:
                return Color.WHITE;
        }
    }

    private void drawCharacter(Graphics2D g2d) {
        int size = 40 + (characterAge / 5); // Character size changes with age
        int earSize = size / 4;

        // Drawing the body of the cat using Midpoint Circle
        Color characterColor = getStageColor();
        drawMidpointCircle(g2d, characterPosition.x, characterPosition.y, size / 2);
        floodFill(buffer, characterPosition.x, characterPosition.y, new Color(0, 0, 0, 0),
                characterColor);

        // Drawing ears with Bézier curves
        drawBezierEar(g2d, characterPosition.x - size / 2, characterPosition.y - size / 2, earSize);
        drawBezierEar(g2d, characterPosition.x + size / 2, characterPosition.y - size / 2, earSize);

        // Drawing eyes
        g2d.setColor(Color.BLACK);
        g2d.fillOval(characterPosition.x - size / 4, characterPosition.y - size / 4, size / 10,
                size / 10);
        g2d.fillOval(characterPosition.x + size / 4 - size / 10, characterPosition.y - size / 4,
                size / 10, size / 10);

        // Drawing tail with Bézier curve
        drawBezierTail(g2d, characterPosition.x + size / 2, characterPosition.y, size);
    }

    private void floodFill(BufferedImage image, int x, int y, Color targetColor,
            Color replacementColor) {
        int width = image.getWidth();
        int height = image.getHeight();
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return;
        }
        if (!new Color(image.getRGB(x, y), true).equals(targetColor)) {
            return;
        }

        Stack<Point> stack = new Stack<>();
        stack.push(new Point(x, y));

        while (!stack.empty()) {
            Point point = stack.pop();
            x = point.x;
            y = point.y;

            if (x < 0 || x >= width || y < 0 || y >= height) {
                continue;
            }

            if (new Color(image.getRGB(x, y), true).equals(targetColor)) {
                image.setRGB(x, y, replacementColor.getRGB());

                stack.push(new Point(x + 1, y));
                stack.push(new Point(x - 1, y));
                stack.push(new Point(x, y + 1));
                stack.push(new Point(x, y - 1));
            }
        }
    }

    private void drawBezierEar(Graphics2D g2d, int x, int y, int size) {
        Point p1 = new Point(x, y);
        Point p2 = new Point(x + size / 2, y - size);
        Point p3 = new Point(x + size, y);

        drawBezierCurve(g2d, p1, p2, p3);
    }

    private void drawBezierTail(Graphics2D g2d, int x, int y, int length) {
        Point p1 = new Point(x, y);
        Point p2 = new Point(x + length / 2, y + length / 2);
        Point p3 = new Point(x + length, y);

        drawBezierCurve(g2d, p1, p2, p3);
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

    private Color getStageColor() {
        switch (currentStage) {
            case BABY:
                return Color.YELLOW;
            case CHILD:
                return Color.BLUE;
            case TEENAGER:
                return Color.MAGENTA;
            case ADULT:
                return Color.RED;
            case ELDER:
                return Color.DARK_GRAY;
            default:
                return Color.BLACK;
        }
    }
}
