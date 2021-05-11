import java.awt.*;

public class Eraser extends DrawService {
    void draw(Graphics2D g2d) {
        g2d.setColor(Color.white);
        g2d.setStroke(new BasicStroke(20.0f,
                BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL));
        g2d.drawLine(x1, y1, x2, y2);
    }
}
