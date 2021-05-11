import javax.swing.*;
import java.awt.*;
import java.io.*;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import javax.swing.JButton;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.Graphics;
public class MainGUI extends JPanel {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    // 形状数组定义
    private Shape[] shapeArray = new Shape[1024000];

    public static void main(String[] args) {
        MainGUI sd = new MainGUI();
        sd.showUI();
    }

    public void showUI() {
        JFrame jf = new JFrame();
        JPanel jp1 = new JPanel(); // 创建一个面板区域来绘制图形
        JPanel jp2 = new JPanel();
        // 流式布局管理器
        java.awt.FlowLayout flow = new java.awt.FlowLayout();

        // 设置颜色大小等属性
        this.setBackground(Color.WHITE);

        jp1.setBackground(Color.GRAY);
        jp1.setPreferredSize(new Dimension(50, 600));
        jp1.setLayout(flow);

        jp2.setBackground(Color.GRAY);
        jp2.setPreferredSize(new Dimension(50, 600));
        jp2.setLayout(flow);

        jf.setTitle("画板"); // 设定标题
        jf.setSize(800, 600); // 设定界面大小
        jf.getContentPane().setBackground(Color.WHITE); // 设定界面颜色为白色
//		jf.setResizable(false); // 界面不可改变大小
        jf.setDefaultCloseOperation(3); // 关闭时退出程序
        jf.setLocationRelativeTo(null); // 设置居中显示

        jf.add(jp1, BorderLayout.WEST);
        jf.add(jp2, BorderLayout.EAST);
        jf.add(this, BorderLayout.CENTER);

        // 创建鼠标监听器对象
        DrawListener dlis = new DrawListener();

        // 给面板加上鼠标监听器
        this.addMouseListener(dlis);
        this.addMouseMotionListener(dlis);

        // 创建图形按钮，并给形状按钮加上动作监听器
        String[] name = {"直线", "椭圆", "矩形", "三角形", "多边形", "曲线", "分形", "动态", "橡皮擦", "清屏"};
        for (int i = 0; i < name.length; i++) {
            JButton jbuName = new JButton(name[i]);
            jp1.add(jbuName);
            jbuName.addActionListener(dlis);
        }

        // 创建颜色按钮，并给颜色按钮加上动作监听器
        Color[] color = {Color.BLACK, Color.RED, Color.GREEN, Color.YELLOW, Color.CYAN, Color.GRAY, Color.LIGHT_GRAY,
                Color.PINK, Color.ORANGE};
        Dimension dm = new Dimension(30, 30);
        for (int i = 0; i < color.length; i++) {
            JButton jbuColor = new JButton();
            jbuColor.setPreferredSize(dm);
            jbuColor.setBackground(color[i]);
            jp2.add(jbuColor);
            jbuColor.addActionListener(dlis);
        }

        // 方便清屏
        dlis.setthis(this);

        // 设置可见
        jf.setVisible(true);
        // 从窗体上获取画布对象
        // 即获取窗体在屏幕上占据的区域，这块区域是可以改变颜色的
        java.awt.Graphics g = this.getGraphics();
        dlis.setg(g);
        dlis.setShapeArray(shapeArray);
    }

    /*
     * 重写绘制组件的方法
     */
    public void paint(Graphics g) {
        super.paint(g);

        // 取出shapeArray数组中保存的图形对象，绘制
        for (int i = 0; i < shapeArray.length; i++) {
            Shape shape = shapeArray[i];
            if (shape != null) {
                shape.drawShape(g);
            } else {
                break;
            }
        }
    }


    public class DrawListener implements MouseListener, ActionListener, MouseMotionListener {

        private int a1, b1, a2, b2, a3, b3, e1, f1, x1, y1, x2, y2, x3, y3, x, y; // 记录鼠标两次点击的坐标
        private Graphics g; // 从界面对象上得到的画布对象
        private boolean bl = true;
        private boolean cl = true;
        private int flag = 0;
        private Color color = Color.BLACK;
        private Shape[] shapeArray;
        private int index = 0;
        private JPanel jp;

        /**
         * 构造方法，初始化画布对象
         *
         * @param
         */
        public void setg(Graphics f) {
            g = f;
        }

        public void setthis(JPanel j) {
            jp = j;
        }

        public void setShapeArray(Shape[] shapeArray) {
            this.shapeArray = shapeArray;
        }

        // 重写MouseMotionListener的抽象方法
        public void mouseDragged(MouseEvent e) {
            System.out.println("鼠标拖动");
            a3 = e.getX();
            b3 = e.getY();
            // flag为6时代表曲线的绘制
            if (flag == 6) {
                if (cl) {
                    e1 = a3;
                    f1 = b3;
                    cl = false;
                } else {
                    g.drawLine(e1, f1, a3, b3);
                    shapeArray[index++] = new Shape(e1, f1, a3, b3, "曲线", color); // 重绘曲线
                    e1 = a3;
                    f1 = b3;
                }
            }
            if (flag == 8) {
                DrawThread dt = new DrawThread(g, a3, b3, color);
                dt.start();
            }
            // 橡皮擦的使用
            if (flag == 9) {
                g.fillOval(a3 - 10, b3 - 10, 20, 20);
                // 创建Shape对象，保存绘制该图形的数据
                Shape shape = new Shape(a3 - 10, b3 - 10, 20, 20, "橡皮擦", Color.WHITE);
                // 把图形对象保存到数组中
                shapeArray[index] = shape;
                index++;
            }
        }

        public void mouseMoved(MouseEvent e) {
            System.out.println("鼠标移动");
        }

        // 重写actionListener的抽象方法
        public void actionPerformed(ActionEvent e) {
            String selected = e.getActionCommand();

            // 设置当前绘制图形的颜色
            g.setColor(color);

            switch (selected) {
                case "直线":
                    flag = 1;
                    System.out.println("绘制直线");
                    break;
                case "椭圆":
                    flag = 2;
                    System.out.println("绘制椭圆");
                    break;
                case "矩形":
                    flag = 3;
                    System.out.println("绘制矩形");
                    break;
                case "三角形":
                    flag = 4;
                    System.out.println("绘制三角形");
                    break;
                case "多边形":
                    flag = 5;
                    System.out.println("绘制多边形");
                    break;
                case "曲线":
                    flag = 6;
                    System.out.println("绘制曲线");
                    break;
                case "分形":
                    flag = 7;
                    System.out.println("绘制分形图形");
                    break;
                case "动态":
                    flag = 8;
                    break;
                case "橡皮擦":
                    flag = 9;
                    g.setColor(Color.WHITE);
                    break;
                case "清屏":
                    System.out.println("清屏");
                    jp.repaint();
                    for (int i = 0; i < index; i++) {
                        shapeArray[i].reset();
                    }
                    break;
                case "":
                    // 获取当前事件原对象
                    JButton jb = (JButton) e.getSource();
                    // 获取按钮的背景色
                    color = jb.getBackground();
                    g.setColor(color);

            }
        }

        // 重写MouseListener的抽象方法
        // 鼠标按下时的点的坐标
        public void mousePressed(MouseEvent e) {
            System.out.println("按下");

            // 记录鼠标按下时的x,y:通过事件对象e得到
            a1 = e.getX();
            b1 = e.getY();
            if (flag == 7) {
                drawDream();
            }
        }

        // 鼠标释放时的点的坐标
        public void mouseReleased(MouseEvent e) {
            System.out.println("松开");

            // 记录鼠标松开点击的坐标
            a2 = e.getX();
            b2 = e.getY();

            if (a1 != a2) {
                x1 = a1;
                y1 = b1;
                x2 = a2;
                y2 = b2;
                bl = true;
            }

            // 调用画布对象的方法：
            if (flag == 1 || flag == 4 || flag == 5) {
                g.drawLine(x1, y1, x2, y2); // 绘制直线
                // 创建Shape对象，保存绘制该图形的数据
                Shape shape = new Shape(x1, y1, x2, y2, "直线", color);
                // 把图形对象保存到数组中
                shapeArray[index] = shape;
                index++;
            }
            if (flag == 2) {
                g.drawOval(Math.min(x1, x2), Math.min(y1, y2), Math.abs(x1 - x2), Math.abs(y1 - y2)); // 绘制椭圆
                Shape shape = new Shape(x1, y1, x2, y2, "椭圆", color); // 创建Shape对象，保存该图形的数据
                shapeArray[index] = shape; // 把图形对象保存到数组中
                index++;
            }
            if (flag == 3) {
                g.drawRect(Math.min(x1, x2), Math.min(y1, y2), Math.abs(x1 - x2), Math.abs(y1 - y2)); // 绘制矩形
                Shape shape = new Shape(x1, y1, x2, y2, "矩形", color); // 创建Shape对象，保存该图形的数据
                shapeArray[index] = shape; // 把图形对象保存到数组中
                index++;
            }
            if (flag == 6) {
                cl = true; // 让cl重新为true,以便下一次绘制曲线
            }
        }

        public void mouseClicked(MouseEvent e) {
            // 记录鼠标点击的坐标
            x3 = e.getX();
            y3 = e.getY();

            if (flag == 4) {
                // 绘制三角形
                g.drawLine(x1, y1, x3, y3);
                Shape shape = new Shape(x1, y1, x3, y3, "直线", color); // 创建Shape对象，保存绘制该图形的数据
                shapeArray[index] = shape; // 把图形对象保存到数组中
                index++;

                g.drawLine(x2, y2, x3, y3);
                Shape shape1 = new Shape(x2, y2, x3, y3, "直线", color); // 创建Shape对象，保存绘制该图形的数据
                shapeArray[index] = shape1; // 把图形对象保存到数组中
                index++;
            }
            if (flag == 5) {
                // 绘制任意多边形
                if (bl) {
                    g.drawLine(x2, y2, x3, y3);
                    Shape shape = new Shape(x2, y2, x3, y3, "直线", color); // 创建Shape对象，保存绘制该图形的数据
                    shapeArray[index] = shape; // 把图形对象保存到数组中
                    index++;
                    bl = false;
                    x = x3;
                    y = y3;
                } else {
                    g.drawLine(x, y, x3, y3);
                    Shape shape = new Shape(x, y, x3, y3, "直线", color); // 创建Shape对象，保存绘制该图形的数据
                    shapeArray[index] = shape; // 把图形对象保存到数组中
                    index++;
                    x = x3;
                    y = y3;
                }
                if (e.getClickCount() == 2) {
                    g.drawLine(x1, y1, x3, y3);
                    Shape shape = new Shape(x1, y1, x3, y3, "直线", color); // 创建Shape对象，保存绘制该图形的数据
                    shapeArray[index] = shape; // 把图形对象保存到数组中
                    index++;
                }
            }
        }

        public void drawDream() {
            double x = 0;
            double y = 0;
            // a,b,c,d等4个常量的值预设
            double a = -1.8, b = -2.0, c = -0.88, d = 1;
            for (int i = 0; i < 25500; i++) {
                // 公式：
                double temx = Math.sin(a * y) - Math.cos(b * x);
                double temy = Math.sin(c * x) - Math.cos(d * y);
                // 对x1,y1转型，放大，移动到屏幕坐标系：
                int x1 = (int) (temx * 100 + 300);
                int y1 = (int) (temy * 100 + 300);
                System.out.println("x1: " + x1 + " y1: " + y1);
                // 颜色根据迭代次数加深
                g.setColor(new Color(0, i / 100, 0));
                g.drawLine(x1, y1, x1, y1);
                shapeArray[index++] = new Shape(x1, y1, x1, y1, "直线", new Color(0, i / 100, 0));
                x = temx;
                y = temy;
            }
        }

        public void mouseEntered(MouseEvent e) {
            System.out.println("进入");
        }

        public void mouseExited(MouseEvent e) {
            System.out.println("退出");
        }

    }


    public class Shape {
        private int x1, y1, x2, y2;
        private String name;
        private Color color;

        /**
         * 创建图形对象时初始化该图形的数据
         *
         * @param x1
         * @param y1
         * @param x2
         * @param y2
         * @param name
         */
        public Shape(int x1, int y1, int x2, int y2, String name, Color color) {
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
            this.name = name;
            this.color = color;
        }

        public void reset() {
            x1 = 0;
            y1 = 0;
            x2 = 0;
            y2 = 0;
            name = "";
            color = null;
        }

        /**
         * 根据图形名字绘制对应的图形
         *
         * @param g 画笔对象
         */
        public void drawShape(Graphics g) {
            // 设定线条的颜色
            g.setColor(color);

            switch (name) {
                case "直线":
                    g.drawLine(x1, y1, x2, y2);
                    break;
                case "椭圆":
                    g.drawOval(Math.min(x1, x2), Math.min(y1, y2), Math.abs(x1 - x2), Math.abs(y1 - y2)); // 绘制椭圆
                    break;
                case "矩形":
                    g.drawRect(Math.min(x1, x2), Math.min(y1, y2), Math.abs(x1 - x2), Math.abs(y1 - y2)); // 绘制矩形
                    break;
                case "曲线":
                    g.drawLine(x1, y1, x2, y2);
                    break;
                case "橡皮擦":
                    g.fillOval(x1, y1, x2, y2);
                    break;
            }
        }
    }


    public class DrawThread extends Thread{
        private Graphics g;
        private int x,y;
        private Color c;
        private Color[] color={ Color.BLACK, Color.RED, Color.GREEN, Color.YELLOW, Color.CYAN, Color.GRAY, Color.LIGHT_GRAY,
                Color.PINK, Color.ORANGE };

        //构造方法传画布对象
        public DrawThread(Graphics g,int x,int y,Color c){
            this.g=g;
            this.x=x;
            this.y=y;
            this.c=c;
        }

        public void run(){
            for(int i=0;i<300;i++){
                g.setColor(c);
                g.fillOval(x+i, y+i, 30, 30);
                try{
                    Thread.sleep(30);
                }catch(Exception ef){}
                g.setColor(Color.WHITE);
                g.fillOval(x+i, y+i, 30, 30);
            }
        }
    }
}

