import java.awt.*;

public class Text extends DrawService {
    void draw(Graphics2D g2d) {
        if (s1 != null) {
            //g2d.setColor(color);
            g2d.drawString(s1, x1, y1);

        }
    }
}