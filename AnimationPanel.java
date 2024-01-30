import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.Graphics;
import java.awt.Color;

public class AnimationPanel extends JPanel {
    private Timer timer;
    private int ballRadius = 50; // Radius of the ball
    private int ballX, ballY; // Ball position
    private int xVelocity = 5; // Horizontal velocity
    private int yVelocity = 5; // Vertical velocity
    private Color ballColor = Color.PINK; // Ball color

    public AnimationPanel() {
        // Set initial position of the ball to be at the center
        ballX = (getWidth() - ballRadius) / 2;
        ballY = (getHeight() - ballRadius) / 2;

        timer = new Timer(40, e -> {
            updateBall();
            repaint();
        });
        timer.start();
    }

    private void updateBall() {
        // Update the ball's position
        ballX += xVelocity;
        ballY += yVelocity;

        // Check for collisions with the panel's borders
        if (ballX < 0 || ballX > getWidth() - ballRadius * 2) {
            xVelocity *= -1; // Reverse horizontal direction
        }
        if (ballY < 0 || ballY > getHeight() - ballRadius * 2) {
            yVelocity *= -1; // Reverse vertical direction
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(ballColor);
        // Draw the ball using the Midpoint Circle algorithm
        MidpointCircle(g, ballX + ballRadius, ballY + ballRadius, ballRadius);
    }

    private void MidpointCircle(Graphics g, int centerX, int centerY, int radius) {
        int x = radius;
        int y = 0;
        int p = 1 - radius;

        while (x >= y) {
            plotPoints(g, centerX, centerY, x, y);
            y++;
            if (p <= 0) {
                p = p + 2 * y + 1;
            } else {
                x--;
                p = p + 2 * y - 2 * x + 1;
            }
        }
    }

    private void plotPoints(Graphics g, int cx, int cy, int x, int y) {
        g.drawLine(cx + x, cy + y, cx + x, cy + y);
        g.drawLine(cx - x, cy + y, cx - x, cy + y);
        g.drawLine(cx + x, cy - y, cx + x, cy - y);
        g.drawLine(cx - x, cy - y, cx - x, cy - y);
        g.drawLine(cx + y, cy + x, cx + y, cy + x);
        g.drawLine(cx - y, cy + x, cx - y, cy + x);
        g.drawLine(cx + y, cy - x, cx + y, cy - x);
        g.drawLine(cx - y, cy - x, cx - y, cy - x);
    }
}
