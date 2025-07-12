/*
 * A. Benquerer
 * e-mail: dev.benquerer@gmail.com
 * GitHub: https://github.com/Benquerer
 * 
 * Aluno 24633 @ IPT, Dec 2024.
 * 
 * The code in this file was developed for learning and experimentation purposes.
 * 
 */

package resources;

import javax.swing.*;
import java.awt.*;

/**
 * This class implements a custom circular progress bar.
 *
 * @author A. Benquerer @ IPT
 * @author D. Larangeira @ IPT
 */
public class circLoadingBar extends JPanel {
    private int angle = 0;

    public circLoadingBar() {
        Timer timer = new Timer(6, e -> {
            angle = (angle - 3) % 360;
            repaint();
        });
        timer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        int size = Math.min(getWidth(), getHeight()) - 10; 
        int x = (getWidth() - size) / 2;
        int y = (getHeight() - size) / 2;

        
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.setStroke(new BasicStroke(10, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.drawOval(x, y, size, size);

        // Fetch the Look and Feel accent color
        Color accentColor = UIManager.getColor("ProgressBar.foreground");
        if (accentColor == null) {
            accentColor = Color.BLUE; // Default fallback color
        }
        g2d.setColor(accentColor);
        g2d.drawArc(x, y, size, size, angle, 30);
    }

    
}



