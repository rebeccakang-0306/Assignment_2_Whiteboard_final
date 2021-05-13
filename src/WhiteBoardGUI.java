/**
 * The WhiteBoardGUI program creates an interface for the manager and the users
 * of the shared whiteboard to be able to draw on the board with several default
 * tools: free-hand pencil, rectangle, line, circle and text
 * All users will be able to see a user list which contains all the users in the
 * whiteboard as well.
 *
 * @author  Zhonglin Shi
 * @studentNumber 774355
 * @version 0.1
 * @since   2020-05-30
 */

import org.json.simple.JSONObject;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicOptionPaneUI;
import javax.swing.text.*;
import javax.swing.JDesktopPane;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.channels.Channel;
import java.text.SimpleDateFormat;
import java.util.*;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.awt.Container;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;




public class WhiteBoardGUI<flowLayout, frame> extends JFrame {

    private ObjectInputStream input;
    private ObjectOutputStream output;
    private static String username;
    private static String role;
    private static String portNum;
    private JTextArea jta1;
    private JTextArea jta2;
    private JButton tools[];

    private String toolNames[] = {
            "pencil",
            "line",
            "rectangle",
            "circle",
            "text",
            "eraser"
    };
    private JLabel statusPanel;
    private Icon icons[];
    private String tooltips[] = {
            "Free hand drawing",
            "Draw a line",
            "Draw a rectangle",
            "Draw a circle",
            "Enter a text",
            "clean up your drawing"
    };
    private String SavedFileName;
    private File filePath;
    private Boolean saveAs = false;
    private Boolean saved = false;

    private int width = 800, height = 700;
    private DrawPanel drawPanel;
    private Panel panel = new Panel();
    private JPanel mainpanel = new JPanel();        /*主要的panel，上层放置连接区，下层放置消息区，中间是消息面板，左边是系统消息，右边是当前room的用户列表*/

    DataStream drawRecord = new DataStream();
    DrawService[] draws = new DrawService[drawRecord.maxStorage];
    private String drawingTool = "pencil";
    private String drawingColor = "#0000ff";
    DrawService newDraw = null;

    DataOutputStream os;
    DataInputStream is;

    Socket client;
    ServerSocket ss;

    DefaultListModel model = new DefaultListModel();
    private JPanel userListGUI = new JPanel();
    private JList userListOnBoard = new JList();
    private static JButton kickOut;


    private static  JButton chat;
    String[] color={"#00FFFF","#808080","#000080","#C0C0C0","#000000","#008000","#808000","#008080","#0000FF","#00FF00","#800080","#A4A832","#FF00FF","#800000","#FF0000","#FFFF00"};
    JButton[] colorButton = new JButton[color.length];

    private JPanel colorGUI = new JPanel();

    private ArrayList<String> curUserList = new ArrayList<>();



    public WhiteBoardGUI(String username, String role, ArrayList<String> userList, ServerSocket serverSocket) throws IOException, ClassNotFoundException {
        super("Shared Whiteboard for " + username);
        this.username = username;
        this.role = role;
        curUserList = userList;
        initialise();
        this.ss = serverSocket;

    }


    public WhiteBoardGUI(String username, String role, Socket client, ArrayList<String> userList) throws IOException, ClassNotFoundException {
        super("Shared Whiteboard for " + username);
        System.out.println("OS created\n");
        os = new DataOutputStream(new BufferedOutputStream(client.getOutputStream()));
        is = new DataInputStream(new BufferedInputStream(client.getInputStream()));
        this.username = username;
        this.client = client;
        this.role = role;
        curUserList = userList;

        initialise();

    }

    public void initialise() {

        JMenuBar bar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic('F');
        JMenuItem newItem = new JMenuItem("New");
        newItem.setMnemonic('N');
        newItem.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        try {
                            newFile();
                        } catch (IOException ioException) {
                            ioException.printStackTrace();
                        }
                    }
                });
        fileMenu.add(newItem);
        JMenuItem saveItem = new JMenuItem("Save");
        saveItem.setMnemonic('S');
        saveItem.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        saveAs = false;
                        saveFile();
                        saved = true;
                    }
                });
        fileMenu.add(saveItem);
        JMenuItem saveItemAs = new JMenuItem("SaveAs");
        saveItemAs.setMnemonic('A');
        saveItemAs.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        saveAs = true;
                        saved = false;
                        saveFile();
                        saved = true;

                    }
                });
        fileMenu.add(saveItemAs);
        JMenuItem loadItem = new JMenuItem("Load");
        loadItem.setMnemonic('L');
        loadItem.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        loadFile();
                        saveAs = false;
                        saved = true;
                    }
                });
        fileMenu.add(loadItem);
        fileMenu.addSeparator();
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.setMnemonic('X');
        exitItem.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        exit();
                    }
                });
        fileMenu.add(exitItem);
        bar.add(fileMenu);

        JToolBar buttonPanel = new JToolBar(JToolBar.HORIZONTAL);
        tools = new JButton[toolNames.length];

        for (int i = 0; i < toolNames.length; i++) {
            ImageIcon icon = new ImageIcon(WhiteBoardGUI.class.getResource("img/" + toolNames[i] + ".gif"));
            tools[i] = new JButton("", icon);
            tools[i].setToolTipText(tooltips[i]);
            buttonPanel.add(tools[i]);
        }

        ButtonEventHandler eventHandler = new ButtonEventHandler();

        for (int i = 0; i < tools.length; i++) {
            tools[i].addActionListener(eventHandler);
        }

        Container c = getContentPane();
        if (role.equals("manager")) {
            super.setJMenuBar(bar);
        }

        statusPanel = new JLabel();
        drawPanel = new DrawPanel();


        // create user list GUI
        String[] userArr = new String[curUserList.size()];
        userArr = curUserList.toArray(userArr);

        for(int i = 0; i < userArr.length; i++) {
            model.addElement(userArr[i]);
        }
        userListOnBoard = new JList(model);







        // chat GUI
        kickOut= new JButton("kick");
        chat  = new JButton("Chat");

        GridLayout grid=new GridLayout(2,1);
        panel.setLayout(grid);
        kickOut.setPreferredSize(new Dimension(100,30));
        panel.add(kickOut);
        chat.setPreferredSize(new Dimension(100,30));
        panel.add(chat);
        {/*
        mainpanel.setLayout(grid1);

        JPanel headpanel = new JPanel();    /*上层panel，用于放置连接区域相关的组件
        JPanel footpanel = new JPanel();    /*下层panel，用于放置发送信息区域的组件
        JPanel centerpanel = new JPanel();    /*中间panel，用于放置聊天信息
        JPanel leftpanel = new JPanel();    /*左边panel，用于放置房间列表和加入按钮
        JPanel rightpanel = new JPanel();   /*右边panel，用于放置房间内人的列表


        /*最上层的布局，分中间，东南西北五个部分

        BorderLayout layout = new BorderLayout();
        /*格子布局，主要用来设置西、东、南三个部分的布局
        GridBagLayout gridBagLayout = new GridBagLayout();
        /*主要设置北部的布局
        FlowLayout flowLayout = new FlowLayout();

        /*设置各个部分的panel的布局和大小
        headpanel.setLayout(flowLayout);
        //footpanel.setLayout(flowLayout);
        leftpanel.setLayout(gridBagLayout);
        centerpanel.setLayout(gridBagLayout);
        rightpanel.setLayout(gridBagLayout);

        //设置面板大小
        leftpanel.setPreferredSize(new Dimension(350, 0));
        rightpanel.setPreferredSize(new Dimension(155, 0));
       // footpanel.setPreferredSize(new Dimension(0, 40));


        //头部布局
       JTextField port_textfield = new JTextField("8888");
       JTextField name_textfield = new JTextField("匿名");
        port_textfield.setPreferredSize(new Dimension(70, 25));
        name_textfield.setPreferredSize(new Dimension(150, 25));

        JLabel port_label = new JLabel("端口号:");
        JLabel name_label = new JLabel("Manager:");

       JButton head_connect = new JButton("Start");
       JButton head_exit = new JButton("Close");

        headpanel.add(port_label);
        headpanel.add(port_textfield);
        headpanel.add(name_label);
        headpanel.add(name_textfield);
        headpanel.add(head_connect);
        headpanel.add(head_exit);


        //底部布局




        // foot_userClear.setPreferredSize(new Dimension(148, 0));





        //左边布局
        JLabel sysMsg_label = new JLabel("System Log:");
        leftpanel.add(sysMsg_label);
       JTextPane sysMsgArea = new JTextPane();
        sysMsgArea.setEditable(false);
       JScrollPane sysTextScrollPane = new JScrollPane();
       sysTextScrollPane.setPreferredSize(new Dimension(200,100));

        sysTextScrollPane.setViewportView(sysMsgArea);
       JScrollBar sysVertical = new JScrollBar(JScrollBar.VERTICAL);
        sysVertical.setAutoscrolls(true);
        sysTextScrollPane.setVerticalScrollBar(sysVertical);
        leftpanel.add(sysTextScrollPane);

        JTextArea sysText_field = new JTextArea();
        sysText_field.setPreferredSize(new Dimension(100, 30));
        footpanel.add(sysText_field);
        JButton foot_sysSend = new JButton("发送系统消息");
        footpanel.add(foot_sysSend);
        //foot_sysSend.setPreferredSize(new Dimension(110, 0));
        leftpanel.add(footpanel);





        //中间布局
        JLabel userMsg_label = new JLabel("世界聊天:");
        centerpanel.add(userMsg_label);
        JTextPane userMsgArea = new JTextPane();
        userMsgArea.setEditable(false);
        userMsgArea.setPreferredSize(new Dimension(100,100));
        JScrollPane userTextScrollPane = new JScrollPane();
        userTextScrollPane.setViewportView(userMsgArea);
        JScrollBar userVertical = new JScrollBar(JScrollBar.VERTICAL);
        userVertical.setAutoscrolls(true);
        userTextScrollPane.setVerticalScrollBar(userVertical);
        centerpanel.add(userTextScrollPane);

        JPanel footpanelcenter = new JPanel();
        JTextArea text_field = new JTextArea();
        text_field.setPreferredSize(new Dimension(100,30));
        footpanelcenter.add(text_field);
        JButton foot_send = new JButton("发送聊天信息");
        footpanelcenter.add(foot_send);
        centerpanel.add(footpanelcenter);

        //右边布局

        JLabel users_label = new JLabel("当前连接用户:0");
        rightpanel.add(users_label);
        JButton privateChat_button = new JButton("Private Chat");
        rightpanel.add(privateChat_button );
        JButton kick_button = new JButton("Kick Out");
        rightpanel.add(kick_button);
        JButton foot_userClear = new JButton("清空聊天消息");
        rightpanel.add(foot_userClear);


        mainpanel.add(leftpanel);
        mainpanel.add(centerpanel);
        mainpanel.add(rightpanel);
        */}








        {/*

        JPanel jpLeft=new JPanel();

        JLabel jlb1=new JLabel("Message Window");
        jpLeft.add(jlb1);
        //接收消息框
        jta1=new JTextArea(10,30);
        jta1.setLineWrap(true);//设置文本框内容自动换行
        jta1.setWrapStyleWord(true);//设置文本框内容在单词结束处换行
        jta1.append("Start Chat:");//向消息框内添加文本
        jta1.setEditable(false);//聊天框内容不可修改

        //添加滚动条
        JScrollPane jsp1=new JScrollPane(jta1,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        jpLeft.add(jsp1);



        JLabel jlb2=new JLabel("Input Window");
        jpLeft.add(jlb2);


        //发送消息框
        jta2=new JTextArea(10,30);
        jta2.setLineWrap(true);//设置消息框内的文本每满一行就自动换行
        jta2.setWrapStyleWord(true);//设置消息框内文本按单词分隔换行

        //添加滚动条
        JScrollPane jsp2=new JScrollPane(jta2,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        jpLeft.add(jsp2);

        //发送和取消按钮

        JButton jb1=new JButton("Send");
        jpLeft.add(jb1);
        JButton jb2=new JButton("Cancel");
        jpLeft.add(jb2);
        */}

        // draw GUI
        //绘画界面







        //create color list GUI

        for(int j = 0; j < colorButton.length; j++) {
            Color colorType =  Color.decode(color[j]);
            colorButton[j] = new JButton();
            colorButton[j].setBackground(colorType);
            colorButton[j].setBorderPainted(false);
            colorButton[j].setOpaque(true);
            colorButton[j].setPreferredSize(new Dimension(30,30));
            colorGUI.add(colorButton[j]);
            String[] colorArray={"#00FFFF","#808080","#000080","#C0C0C0","#000000","#008000","#808000","#008080","#0000FF","#00FF00","#800080","#A4A832","#FF00FF","#800000","#FF0000","#FFFF00"};
            colorButton[j].addActionListener(
                    new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            JButton button=(JButton) e.getSource();
                            for (int j=0;j<colorArray.length;j++){

                                String hexCode = "#"+Integer.toHexString(button.getBackground().getRGB()).substring(2).toUpperCase(Locale.ROOT);
                                if(hexCode.equals(colorArray[j])){
                                    drawingColor = colorArray[j];

                                }
                            }
                        }
                    });
        }



        String title = "Participants";
        Border border = BorderFactory.createTitledBorder(title);
        userListGUI.setLayout(new BorderLayout());
        userListGUI.setBorder(border);
        userListGUI.add(userListOnBoard, BorderLayout.CENTER);


        if(role.equals("manager")) {
            userListGUI.add(panel,BorderLayout.SOUTH);
            kickOut.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if(userListOnBoard.getSelectedValue() != null && !userListOnBoard.getSelectedValue().equals(username)) {
                        try {
                            CreateWhiteBoard.kick(userListOnBoard.getSelectedValue());
                        } catch (IOException ioException) {
                            ioException.printStackTrace();
                        }

                    }
                }
            });
            chat.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    System.out.println("Server Clicked\n");
                    new chatServer().init(username);
                }
            });

        }
        else {
            userListGUI.add(chat,BorderLayout.SOUTH);
            chat.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    new chatClient().init(username);
                }
            });

        }
        userListGUI.setPreferredSize(new Dimension(100, 700));
        colorGUI.setPreferredSize(new Dimension(100,700));



        c.add(buttonPanel, BorderLayout.NORTH);
        c.add(colorGUI,BorderLayout.WEST);
        c.add(drawPanel, BorderLayout.CENTER);
        c.add(statusPanel, BorderLayout.SOUTH);
        c.add(userListGUI, BorderLayout.EAST);

        createDraw();
        setSize(width, height);

        setVisible(true);


    }



    class DrawPanel extends JPanel {

        public DrawPanel() {
            setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
            setBackground(Color.white);
            addMouseListener(new MouseMoveOrClick());
            addMouseMotionListener(new MouseDragOrMove());

}

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            int j = 0;
            while (j <= drawRecord.curIndex) {
                draw(g2d, draws[j]);
                j++;
            }
        }

        void draw(Graphics2D g2d, DrawService ds) {
            if (ds != null) {
                ds.draw(g2d);
            }
        }
    }

    class MouseMoveOrClick extends MouseAdapter {

        public void mousePressed(MouseEvent e) {
            statusPanel.setText("     Mouse Pressed :(" + e.getX() +
                    ", " + e.getY() + ")");
            draws[drawRecord.curIndex].x1 = draws[drawRecord.curIndex].x2 = e.getX();
            draws[drawRecord.curIndex].y1 = draws[drawRecord.curIndex].y2 = e.getY();
            draws[drawRecord.curIndex].color = Color.decode(drawingColor);

            if(drawingTool.equals("pencil")) {
                draws[drawRecord.curIndex].x1 = e.getX();
                draws[drawRecord.curIndex].y1 = e.getY();
            } else if (drawingTool.equals("text")) {
                draws[drawRecord.curIndex].x1 = e.getX();
                draws[drawRecord.curIndex].y1 = e.getY();
                String input;
                input = JOptionPane.showInputDialog(
                        "Please input the text you want!");
                draws[drawRecord.curIndex].s1 = input;

                newDraw = draws[drawRecord.curIndex];
                drawRecord.nextIndex();
                createDraw();
                repaint();
                sendToServer();
            }
            if(drawingTool.equals("eraser")) {
                draws[drawRecord.curIndex].x1 = e.getX();
                draws[drawRecord.curIndex].y1 = e.getY();
            }

        }

        public void mouseReleased(MouseEvent e) {
            statusPanel.setText("     Mouse Released :(" + e.getX() +
                    ", " + e.getY() + ")");

            draws[drawRecord.curIndex].x2 = e.getX();
            draws[drawRecord.curIndex].y2 = e.getY();
            newDraw = draws[drawRecord.curIndex];
            repaint();
            drawRecord.nextIndex();
            createDraw();
            sendToServer();
        }

        public void mouseEntered(MouseEvent e) {
            statusPanel.setText("     Mouse Entered :(" + e.getX() +
                    ", " + e.getY() + ")");
        }

        public void mouseExited(MouseEvent e) {
            statusPanel.setText("     Mouse Exited :(" + e.getX() +
                    ", " + e.getY() + ")");
        }


    }

    class MouseDragOrMove implements MouseMotionListener {
        public void mouseDragged(MouseEvent e) {
            statusPanel.setText("     Mouse Dragged :(" + e.getX() +
                    ", " + e.getY() + ")");
            if (drawingTool.equals("pencil")) {
                draws[drawRecord.curIndex].x2 = e.getX();
                draws[drawRecord.curIndex].y2 = e.getY();
                newDraw = draws[drawRecord.curIndex];
                repaint();
                drawRecord.nextIndex();
                createDraw();
                sendToServer();

                draws[drawRecord.curIndex].y2 = draws[drawRecord.curIndex].y1 = e.getY();
                draws[drawRecord.curIndex].x2 = draws[drawRecord.curIndex].x1 = e.getX();
            } else if (drawingTool.equals("eraser")) {
                draws[drawRecord.curIndex].x2 = e.getX();
                draws[drawRecord.curIndex].y2 = e.getY();
                newDraw = draws[drawRecord.curIndex];
                repaint();
                drawRecord.nextIndex();
                createDraw();
                sendToServer();

                draws[drawRecord.curIndex].y2 = draws[drawRecord.curIndex].y1 = e.getY();
                draws[drawRecord.curIndex].x2 = draws[drawRecord.curIndex].x1 = e.getX();
            }
            else {
                draws[drawRecord.curIndex].x2 = e.getX();
                draws[drawRecord.curIndex].y2 = e.getY();
                repaint();
            }


        }
        public void mouseMoved(MouseEvent e) {
            statusPanel.setText("     Mouse Moved :(" + e.getX() +
                    ", " + e.getY() + ")");
        }

    }

    void sendToServer() {
        try {
            if (!role.equals("manager")){
                sendData(newDraw);
            }
            else {
                CreateWhiteBoard.Broadcast(newDraw, this.client, "draw", null);
            }
        } catch (IOException e) {
            ExceptionHandler.main(e);
        }
    }

    public void sendData(DrawService newDraw) throws IOException {
        if(newDraw != null && newDraw.x1 != 0 && newDraw.y1 != 0) {
            System.out.println(newDraw.color);
            String hex = "#"+Integer.toHexString(newDraw.color.getRGB()).substring(2).toUpperCase(Locale.ROOT);
            String data = newDraw.x1 + "," + newDraw.y1 + "," + newDraw.x2 + "," + newDraw.y2 + "," + newDraw.type + "," + newDraw.s1 + "," + hex;
            System.out.println("lllllll" + data);
            os.writeUTF(data);
            os.flush();
        }
    }

    public void requestForGraphics() throws IOException {
        os.writeUTF("graphics,");
        os.flush();
    }

    public void receiveData() throws IOException, ClassNotFoundException {
        while (true) {
            try {
                String received = is.readUTF();
                System.out.println("!!"+received);
                JSONParser parser = new JSONParser();
                JSONObject command = (JSONObject)parser.parse(input.readUTF());
                String commandMsg = command.get("command").toString();
                System.out.println("COMMAND RECEIVED: " + command.toJSONString());
                Integer result = parseCommand(command);
                JSONObject results = new JSONObject();
                results.put("result", result);
                output.writeUTF(results.toJSONString());


                JSONObject object = JSON.parseObject;
                String[] data = received.split(",");
                if (commandMsg.equals("newuser")) {
                    addNewUser(data[1]);
                } else if (commandMsg.equals("userleave")) {
                    if (data[1].equals(username)) {
                        JOptionPane.showMessageDialog(null, "Oops! You have been kicked out by the white board owner. Please contact the owner or restart the white board.");
                        System.exit(0);
                    } else {
                        removeUser(data[1]);
                    }
                } else if (commandMsg.equals("graphics")) {
                    // redraw
                    for (int i = 1; i < data.length - 1; i++) {
                        String[] drawData = data[i].split("\\|");
                        System.out.println("jjjjjjj"+ Arrays.toString(drawData));
                        DrawService newDraw = new DrawService();
                        newDraw.x1 = Integer.parseInt(drawData[0]);
                        newDraw.y1 = Integer.parseInt(drawData[1]);
                        newDraw.x2 = Integer.parseInt(drawData[2]);
                        newDraw.y2 = Integer.parseInt(drawData[3]);
                        newDraw.type = drawData[4];
                        newDraw.s1 = drawData[5];
                        System.out.println("........."+drawData[6]);
                        newDraw.color = Color.decode(drawData[6]);
                        createDrawForClient(newDraw);
                    }

                } else if (commandMsg.equals("cleanup")) {
                    cleanBoard();
                } else if (commandMsg.equals("load")) {
                    cleanBoard();
                    requestForGraphics();
                } else {
                    if(data.length == 7) {
                        DrawService newDraw = new DrawService();
                        newDraw.x1 = Integer.parseInt(data[0]);
                        newDraw.y1 = Integer.parseInt(data[1]);
                        newDraw.x2 = Integer.parseInt(data[2]);
                        newDraw.y2 = Integer.parseInt(data[3]);
                        newDraw.type = data[4];
                        newDraw.s1 = data[5];

                        newDraw.color = Color.decode(data[6]);
                        System.out.println("Log!!!!!!!" + newDraw.color.toString());

                        createDrawForClient(newDraw);
                    }
                }

            } catch (EOFException | SocketException | ParseException e) {
                JOptionPane.showMessageDialog(null, "Server is closed by the manager. You have been removed from this whiteboard");
                System.exit(0);
            }

        }
    }

    public class ButtonEventHandler implements ActionListener {

        public void actionPerformed(ActionEvent e) {

            for (int i = 0; i < tools.length; i++) {
                if (e.getSource() == tools[i]) {
                    drawingTool = toolNames[i];
                    createDraw();
                    repaint();
                }
            }
        }
    }



    void createDraw() {
        if (drawingTool.equals("text")) {
            drawPanel.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
        } else {
            drawPanel.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        }

        ModeSwitcher();
        draws[drawRecord.curIndex].type = drawingTool;
        draws[drawRecord.curIndex].color = Color.decode(drawingColor);
        System.out.println(drawRecord.curIndex + "=" + drawingTool);

    }

    void ModeSwitcher() {
        switch (drawingTool) {
            case "pencil":
                draws[drawRecord.curIndex] = new Pencil();
                break;
            case "line":
                draws[drawRecord.curIndex] = new Line();
                break;
            case "rectangle":
                draws[drawRecord.curIndex] = new Rectangle();
                break;
            case "circle":
                draws[drawRecord.curIndex] = new Circle();
                break;
            case "text":
                draws[drawRecord.curIndex] = new Text();
                break;
            case "eraser":
                draws[drawRecord.curIndex] = new Eraser();
                break;
        }

    }

    public void createDrawForClient(DrawService clientDraw) {
        drawingTool = clientDraw.type;
        ModeSwitcher();

        draws[drawRecord.curIndex].x1 = clientDraw.x1;
        draws[drawRecord.curIndex].y1 = clientDraw.y1;
        draws[drawRecord.curIndex].x2 = clientDraw.x2;
        draws[drawRecord.curIndex].y2 = clientDraw.y2;
        draws[drawRecord.curIndex].type = clientDraw.type;
        draws[drawRecord.curIndex].color = clientDraw.color;

        if(clientDraw.type.equals("text")) {
            draws[drawRecord.curIndex].s1 = clientDraw.s1;
        }

        drawRecord.nextIndex();
        repaint();
        createDraw();

    }

    public void saveFile() {
        if (saveAs) {
            String[] typeList = new String[] {"jpg", "png", "gif"};
            JList list = new JList(typeList);
            JOptionPane.showMessageDialog(
                    null, list, "Save As?", JOptionPane.PLAIN_MESSAGE);
            String selectedFileType = typeList[list.getSelectedIndex()];
            save(selectedFileType);
        } else {
            save(null);

        }

    }

    public void save(String type) {
        if (saved) {
            File fileName = filePath;
            fileName.canWrite();
            try {
                fileName.delete();
                FileOutputStream fos;
                if (type != null) {
                    fos = new FileOutputStream(fileName + "." + type);
                    SavedFileName = fileName.getName() + "." + type;
                } else {
                    fos = new FileOutputStream(fileName);
                    SavedFileName = fileName.getName();
                }
                output = new ObjectOutputStream(fos);
                output.writeInt(drawRecord.curIndex);
                for (int i = 0; i < drawRecord.curIndex; i++) {
                    DrawService p = draws[i];
                    output.writeObject(p);
                    output.flush();
                }
                output.close();
                fos.close();
            } catch (IOException e) {
                ExceptionHandler.main(e);
            }
        } else {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            int result = fileChooser.showSaveDialog(this);
            if (result == JFileChooser.CANCEL_OPTION) {
                return;
            }
            File fileName = fileChooser.getSelectedFile();
            filePath = fileName;
            fileName.canWrite();
            if (fileName == null || fileName.getName().equals("")) {
                JOptionPane.showMessageDialog(fileChooser, "Invalid File Name",
                        "Invalid File Name", JOptionPane.ERROR_MESSAGE);
            } else {
                try {
                    fileName.delete();
                    FileOutputStream fos;
                    if (type != null) {
                        fos = new FileOutputStream(fileName + "." + type);
                        SavedFileName = fileName.getName() + "." + type;
                    } else {
                        fos = new FileOutputStream(fileName);
                        SavedFileName = fileName.getName();
                    }
                    output = new ObjectOutputStream(fos);
                    output.writeInt(drawRecord.curIndex);
                    for (int i = 0; i < drawRecord.curIndex; i++) {
                        DrawService p = draws[i];
                        output.writeObject(p);
                        output.flush();
                    }
                    output.close();
                    fos.close();
                } catch (IOException e) {
                    ExceptionHandler.main(e);
                }
            }
        }

    }

    public void loadFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.CANCEL_OPTION) {
            // do nothing
            return;
        }
        File fileName = fileChooser.getSelectedFile();
        fileName.canRead();
        if (fileName == null || fileName.getName().equals("")) {
            JOptionPane.showMessageDialog(fileChooser, "Can not load without a file name",
                    "Load fails", JOptionPane.ERROR_MESSAGE);
        } else {
            try {
                FileInputStream fis = new FileInputStream(fileName);
                input = new ObjectInputStream(fis);
                DrawService inputRecord;
                int countNumber = 0;
                countNumber = input.readInt();
                for (drawRecord.curIndex = 0; drawRecord.curIndex < countNumber; drawRecord.curIndex++) {
                    inputRecord = (DrawService) input.readObject();
                    draws[drawRecord.curIndex] = inputRecord;
                }
                createDraw();
                input.close();
                repaint();
                CreateWhiteBoard.Broadcast(null, null, "load", null);
            } catch (IOException | ClassNotFoundException e) {
                ExceptionHandler.main(e);
            }
        }
    }

    public void newFile() throws IOException {
        cleanBoard();
        CreateWhiteBoard.Broadcast(null, null, "cleanUp", null);

    }

    public void cleanBoard() {
        saveAs = false;
        saved = false;
        drawRecord.initialiseIndex();
        drawingTool = "circle";
        createDraw();
        repaint();
    }

    public void exit() {
        System.exit(0);
    }

    public void addNewUser(String user) {
        curUserList.add(user);
        model.addElement(user);
    }

    public void removeUser(String user) {
        curUserList.remove(user);
        model.removeElement(user);
    }
}