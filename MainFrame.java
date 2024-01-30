import javax.swing.JFrame;

public class MainFrame extends JFrame {
    public MainFrame() {
        this.setSize(600, 600); // Set window size to 600x600
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.add(new AnimationPanel());
        this.setVisible(true);
    }
}
