


import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



/**
 * @Author: Himit_ZH
 * @Date: 2020/6/4 17:17
 * @Description: 聊天室服务器，管理员GUI界面
 */
public class chatServer extends JFrame {
    private CopyOnWriteArrayList<Channel> allUserChannel;
    /*以下为窗口参数*/
    private JFrame frame;
    //头部参数
    private JTextField port_textfield;
    private JTextField name_textfield;
    private JButton head_connect;
    private JButton head_exit;
    private int port;
    //底部参数
    private JTextField text_field;
    private JTextField sysText_field;
    private JButton foot_send;
    private JButton foot_sysSend;
    private JButton foot_userClear;

    //右边参数
    private JLabel users_label;
    private JButton privateChat_button;
    private JButton kick_button;
    private DefaultListModel<String> users_model;

    //左边参数
    private JScrollPane sysTextScrollPane;
    private JTextPane sysMsgArea;
    private JScrollBar sysVertical;

    //中间参数
    private JScrollPane userTextScrollPane;
    private JTextPane userMsgArea;
    private JScrollBar userVertical;


    //时间格式化工具类
    static private SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");//设置时间

    //用户自增ID
    private int userId = 1000;
    //服务器管理员名字
    private String adminName;

    //服务器线程
    private ServerSocket serverSocket;
    private ServerSocket ss;

    //服务器线程
    private Server server;

    //管理员的私聊窗口队列和线程队列
    private HashMap<String, privateChatFrame> adminPrivateQueue;
    private HashMap<String, Channel> adminPrivateThread;
    private static JList<String> userlist;



    public static void main(String[] args) {
        new chatServer();
    }



    /**
     * @MethodName init   GUI初始化，初始化各种监听事件
     * @Params  * @param null
     * @Return null
     * @Since 2020/6/6
     * @return
     */
    public void init(String username) {
        allUserChannel = new CopyOnWriteArrayList<>();
        adminPrivateQueue = new HashMap<>();
        adminPrivateThread = new HashMap<>();
        setUIStyle();
        this.adminName = username;

        frame = new JFrame("White Board Chat Room");
        JPanel panel = new JPanel();        /*主要的panel，上层放置连接区，下层放置消息区，
                                                  中间是消息面板，左边是room列表，右边是当前room的用户列表*/
        JPanel headpanel = new JPanel();    /*上层panel，用于放置连接区域相关的组件*/
        JPanel footpanel = new JPanel();    /*下层panel，用于放置发送信息区域的组件*/
        JPanel centerpanel = new JPanel();    /*中间panel，用于放置聊天信息*/
        JPanel leftpanel = new JPanel();    /*左边panel，用于放置房间列表和加入按钮*/
        JPanel rightpanel = new JPanel();   /*右边panel，用于放置房间内人的列表*/

        /*最上层的布局，分中间，东南西北五个部分*/
        BorderLayout layout = new BorderLayout();
        /*格子布局，主要用来设置西、东、南三个部分的布局*/
        GridBagLayout gridBagLayout = new GridBagLayout();
        /*主要设置北部的布局*/
        FlowLayout flowLayout = new FlowLayout();
        /*设置初始窗口的一些性质*/
        frame.setSize(900, 600);
        frame.setLocationRelativeTo(null);

       // frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.setContentPane(panel);
        frame.setLayout(layout);
        /*设置各个部分的panel的布局和大小*/
        headpanel.setLayout(flowLayout);
        footpanel.setLayout(gridBagLayout);
        leftpanel.setLayout(gridBagLayout);
        centerpanel.setLayout(gridBagLayout);
        rightpanel.setLayout(gridBagLayout);
        //设置面板大小
        leftpanel.setPreferredSize(new Dimension(350, 0));
        rightpanel.setPreferredSize(new Dimension(155, 0));
        footpanel.setPreferredSize(new Dimension(0, 40));

        //头部布局
       port_textfield = new JTextField("1");
       name_textfield = new JTextField("Username");
       port_textfield.setPreferredSize(new Dimension(70, 25));
       name_textfield.setPreferredSize(new Dimension(150, 25));

        JLabel name_label = new JLabel("Manager:");



        headpanel.add(port_textfield);
        headpanel.add(name_label);
        headpanel.add(name_textfield);


        //底部布局
        foot_send = new JButton("Send to Chat");
        Font f=new Font("Time New Romance",Font.BOLD,10);
        foot_send.setFont(f);
        foot_send.setPreferredSize(new Dimension(100, 0));
        foot_sysSend = new JButton("Send to System");
        foot_sysSend.setFont(f);
        foot_sysSend.setPreferredSize(new Dimension(120, 0));
        foot_userClear = new JButton("Delete chat history");
        foot_userClear.setFont(f);
        foot_userClear.setPreferredSize(new Dimension(150, 0));


        sysText_field = new JTextField();
        sysText_field.setPreferredSize(new Dimension(220, 0));
        text_field = new JTextField();
        sysText_field.setPreferredSize(new Dimension(220, 0));
        footpanel.add(sysText_field, new GridBagConstraints(0, 0, 1, 1, 1, 1,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(1, 1, 1, 1), 0, 0));
        footpanel.add(foot_sysSend, new GridBagConstraints(1, 0, 1, 1, 1.0, 1.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(1, 1, 1, 3), 0, 0));
        footpanel.add(text_field, new GridBagConstraints(2, 0, 1, 1, 100, 100,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(1, 1, 1, 1), 0, 0));
        footpanel.add(foot_send, new GridBagConstraints(3, 0, 1, 1, 1.0, 1.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(1, 1, 1, 1), 0, 0));
        footpanel.add(foot_userClear, new GridBagConstraints(4, 0, 1, 1, 1.0, 1.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(1, 1, 1, 1), 0, 0));


        //左边布局
        JLabel sysMsg_label = new JLabel("System Log:");
        sysMsgArea = new JTextPane();
        sysMsgArea.setEditable(false);
        sysTextScrollPane = new JScrollPane();
        sysTextScrollPane.setViewportView(sysMsgArea);
        sysVertical = new JScrollBar(JScrollBar.VERTICAL);
        sysVertical.setAutoscrolls(true);
        sysTextScrollPane.setVerticalScrollBar(sysVertical);
        leftpanel.add(sysMsg_label, new GridBagConstraints(0, 0, 1, 1, 1, 1,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
        leftpanel.add(sysTextScrollPane, new GridBagConstraints(0, 1, 1, 1, 100, 100,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

        //右边布局

        users_label = new JLabel("Connected User:0");
        privateChat_button = new JButton("Private Chat");
        kick_button = new JButton("Kick Out");

        users_model = new DefaultListModel<>();
        users_model.add(0,username);
        userlist = new JList<String>(users_model);
        JScrollPane userListPane = new JScrollPane(userlist);


        rightpanel.add(users_label, new GridBagConstraints(0, 0, 1, 1, 1, 1,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
        rightpanel.add(privateChat_button, new GridBagConstraints(0, 1, 1, 1, 1, 1,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
        rightpanel.add(kick_button, new GridBagConstraints(0, 2, 1, 1, 1, 1,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
        rightpanel.add(userListPane, new GridBagConstraints(0, 3, 1, 1, 100, 100,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

        //中间布局
        JLabel userMsg_label = new JLabel("Global Chat");
        userMsgArea = new JTextPane();
        userMsgArea.setEditable(false);
        userTextScrollPane = new JScrollPane();
        userTextScrollPane.setViewportView(userMsgArea);
        userVertical = new JScrollBar(JScrollBar.VERTICAL);
        userVertical.setAutoscrolls(true);
        userTextScrollPane.setVerticalScrollBar(userVertical);

        centerpanel.add(userMsg_label, new GridBagConstraints(0, 0, 1, 1, 1, 1,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
        centerpanel.add(userTextScrollPane, new GridBagConstraints(0, 1, 1, 1, 100, 100,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

        //设置顶层布局
        panel.add(headpanel, "North");
        panel.add(footpanel, "South");
        panel.add(leftpanel, "West");
        panel.add(rightpanel, "East");
        panel.add(centerpanel, "Center");

        //将按钮事件全部注册到监听器
        allActionListener allActionListener = new allActionListener();
        //开启服务
        head_connect.addActionListener(allActionListener);
        // 管理员发布消息
        foot_send.addActionListener(allActionListener);
        //关闭服务器
        head_exit.addActionListener(allActionListener);
        //清空消息日志
        foot_sysSend.addActionListener(allActionListener);
        //清空世界聊天消息
        foot_userClear.addActionListener(allActionListener);
        //Private Chat
        privateChat_button.addActionListener(allActionListener);
        //踢人
        kick_button.addActionListener(allActionListener);

        //服务器窗口关闭事件
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                int option = JOptionPane.showConfirmDialog(null, "Are you sure you want to quit?",
                        "Close Window Warning", JOptionPane.YES_NO_OPTION);
                if (option == JOptionPane.YES_OPTION) {
                    if (e.getWindow() == frame) {
                        if (server != null) { //如果已开启服务，就告诉各个已连接客户端:服务器已关闭
                            sendSysMsg("由于服务器关闭，已断开连接", 3);
                        }
                        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

                    } else {
                        return;
                    }
                } else {
                    return;
                }
            }
        });

        //聊天信息输入框的监听回车按钮事件
        text_field.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {

                if (e.getKeyChar() == KeyEvent.VK_ENTER) {
                    String text = text_field.getText();
                    if (text != null && !text.equals("")) {
                        sendAdminMsg(text);
                        text_field.setText("");
                        insertMessage(userTextScrollPane, userMsgArea, null, "[Manager]" + adminName +" "+df.format(new Date()) , " "+text, userVertical, false);
                    }
                }
            }
        });
        // 系统信息输入框的回车监控事件
        sysText_field.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {

                if (e.getKeyChar() == KeyEvent.VK_ENTER) {

                    String sysText = sysText_field.getText(); //获取输入框中的内容
                    if (sysText != null && !sysText.equals("")) {
                        sendSysMsg(sysText, 2);
                        sysText_field.setText("");
                        insertMessage(sysTextScrollPane, sysMsgArea, null, "[System Log] " + df.format(new Date()), "[Manager]" + adminName + ":" + sysText, sysVertical, true);
                    }
                }
            }
        });

        //窗口显示
        frame.setVisible(true);
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

        name_textfield.setText(adminName);
        String strport = port_textfield.getText();
        try {
            server = new Server(new ServerSocket(Integer.parseInt(strport)));
            insertMessage(sysTextScrollPane, sysMsgArea, null, "[System Log] " + df.format(new Date()), "Chat Room Start！", sysVertical, true);
            (new Thread(server)).start();
            adminName = name_textfield.getText();
            name_textfield.setEditable(false);
            port_textfield.setEditable(false);
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }

    }




//    //线程锁，防止多线程争夺同个id
//    public synchronized int getUserId() {
//        userId++;
//        return userId;
//    }

    /**
     * 按钮监听内部类
     * Function: 全局监听事件，监听所有按钮
     */
    private class allActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {

            String cmd = e.getActionCommand();

            {/* case "Close":
                    if (server == null) {
                        JOptionPane.showMessageDialog(frame, "不能关闭，未开启过服务器!", "Error", JOptionPane.ERROR_MESSAGE);
                        break;
                    }
                    sendSysMsg("由于服务器关闭，已断开连接", 3);
                    try {
                        serverSocket.close();
                    } catch (Exception e1) {
                        insertMessage(sysTextScrollPane, sysMsgArea, null, "[System Log] " + df.format(new Date()), "Error:服务器关闭失败!", sysVertical, true);
                    }
                    head_connect.setText("Start");
                    head_exit.setText("已关闭");
                    port_textfield.setEditable(true);
                    name_textfield.setEditable(true);
                    for (Channel channel : allUserChannel) {
                        channel.release();
                    }
                    server = null;
                    users_model.removeAllElements();
                    insertMessage(sysTextScrollPane, sysMsgArea, null, "[System Log] " + df.format(new Date()), "服务器已关闭!", sysVertical, true);
                    JOptionPane.showMessageDialog(frame, "服务器已关闭!");
                    break;*/}
            switch (cmd) {
                case "Send System Message":

                    String sysText = sysText_field.getText(); //获取输入框中的内容
                    sendSysMsg(sysText, 2);
                    sysText_field.setText("");
                    insertMessage(sysTextScrollPane, sysMsgArea, null, "[System Log] " + df.format(new Date()), "[Manager]" + adminName + ":" + sysText, sysVertical, true);
                    break;
                case "Send Chat Message":

                    String text = text_field.getText(); //获取输入框中的内容
                    sendAdminMsg(text);
                    text_field.setText("");
                    insertMessage(userTextScrollPane, userMsgArea, null, "[Manager]" + adminName +" "+df.format(new Date()) , " "+text, userVertical, false);
                    break;
                case "Kick Out":



                    try {
                        String selected = userlist.getSelectedValue();
                        kickUser(selected);
                    } catch (NullPointerException e1) {
                        JOptionPane.showMessageDialog(frame, "Please choose the user you want to kick", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                    break;
                case "Clean up the system log":
                    sysMsgArea.setText("");
                    break;
                case "Delete chat history":
                    userMsgArea.setText("");
                    break;
                case "Private Chat":

                    String privateSelected = userlist.getSelectedValue();

                    privateChat(privateSelected);
                    break;
                default:
                    break;
            }
        }
    }



    /**
     * @MethodName insertMessage
     * @Params  * @param null
     * @Description 往系统消息文本域或者聊天事件文本域插入固定格式的内容
     * @Return
     * @Since 2020/6/6
     */
    private void insertMessage(JScrollPane scrollPane, JTextPane textPane, String icon_code,
                               String title, String content, JScrollBar vertical, boolean isSys) {
        StyledDocument document = textPane.getStyledDocument();     /*获取textpane中的文本*/
        /*设置标题的属性*/
        Color content_color = null;
        if (isSys) {
            content_color = Color.RED;
        } else {
            content_color = Color.GRAY;
        }
        SimpleAttributeSet title_attr = new SimpleAttributeSet();
        StyleConstants.setBold(title_attr, true);
        StyleConstants.setForeground(title_attr, Color.BLUE);
        /*设置正文的属性*/
        SimpleAttributeSet content_attr = new SimpleAttributeSet();
        StyleConstants.setBold(content_attr, false);
        StyleConstants.setForeground(content_attr, content_color);
        Style style = null;
        if (icon_code != null) {
            Icon icon = new ImageIcon("icon/" + icon_code + ".png");
            style = document.addStyle("icon", null);
            StyleConstants.setIcon(style, icon);
        }

        try {
            document.insertString(document.getLength(), title + "\n", title_attr);
            if (style != null)
                document.insertString(document.getLength(), "\n", style);
            else
                document.insertString(document.getLength(), content + "\n", content_attr);

        } catch (BadLocationException ex) {
            System.out.println("Bad location exception");
        }
        /*设置滑动条到最后*/
        textPane.setCaretPosition(textPane.getDocument().getLength());
    }

    /**
     * @MethodName sendSysMsg
     * @Params  * @param null
     * @Description 发布系统消息
     * @Return
     * @Since 2020/6/6
     */
    private void sendSysMsg(String content, int code) {
        if (code == 2) {
            String msg = "<time>" + df.format(new Date()) + "</time><sysMsg>" + content + "</sysMsg>";
            for (Channel channel : allUserChannel) {
                channel.send(formatCodeWithMsg(msg, code));
            }
        } else if (code == 3) {
            for (Channel channel : allUserChannel) {
                channel.send(formatCodeWithMsg(content, code));
            }
        }
    }

    /**
     * @MethodName sendAdminMsg
     * @Params  * @param null
     * @Description 发送管理员聊天
     * @Return
     * @Since 2020/6/6
     */
    private void sendAdminMsg(String content) {
        String msg = "<userId>0</userId><userMsg>" + content + "</userMsg><time>" + df.format(new Date()) + "</time>";
        for (Channel channel : allUserChannel) {
            // tood: broadcast函数
            channel.send(formatCodeWithMsg(msg, 1));
        }
    }

    /**
     * @MethodName kickUser
     * @Params  * @param null
     * @Description 踢出操作,对选中用户进行踢出操作，顺便向其它用户说明
     * @Return
     * @Since 2020/6/6
     */
    private void kickUser(String selected) {

        String kickedUserName = null;
        String kickedUserId  = null;
        //管理员还在与之私聊不可踢!避免冲突!
        for (Channel channel1 : allUserChannel) {
            String tmp = "[User" + channel1.user.getId() + "]" + channel1.user.getUsername();
            if (adminPrivateThread.containsKey(channel1.user.getId().toString())) {
                JOptionPane.showMessageDialog(frame, "管理员与该用户的私聊未结束，无法踢出!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if(tmp.equals(selected)){
                kickedUserName = tmp;
                kickedUserId = channel1.user.getId().toString();
            }
        }
        String kickedUserMsg = "对不起，您已被本聊天室管理员踢出!";
        String otherUserMsg = "<time>" + df.format(new Date()) + "</time><sysMsg>" +"通知:"+kickedUserName+" 被踢出了聊天室!" + "</sysMsg>";
        for (Channel channel2 : allUserChannel) {
            String tmp = "[User" + channel2.user.getId() + "]" + channel2.user.getUsername();
            if (tmp.equals(selected)) {
                //告诉对方你被踢了
                channel2.send(formatCodeWithMsg(kickedUserMsg, 3));
                //服务器端系统记录
                insertMessage(sysTextScrollPane, sysMsgArea, null, "[System Log] " + df.format(new Date()) , tmp+" 被踢出了聊天室", sysVertical, true);
                //服务器端界面用户列表移除对应用户
                users_model.removeElement(selected);
                channel2.release();
                users_label.setText("当前连接用户:" + allUserChannel.size());
                break;
            }else{
                //通知每个用户 此人被踢出聊天室
                channel2.send(formatCodeWithMsg(otherUserMsg,2));
                channel2.send(formatCodeWithMsg(kickedUserId,5));
            }
        }
    }

    /**
     * @MethodName privateChat
     * @Params  * @param null
     * @Description 管理员私聊操作
     * @Return
     * @Since 2020/6/6
     */
    private void privateChat(String selected) {
        for (Channel channel : allUserChannel) {
            String tmp = "[User" + channel.user.getId() + "]" + channel.user.getUsername();
            if (tmp.equals(selected)) {
                if(adminPrivateQueue.containsKey(channel.user.getId().toString())){ //不能重复私聊
                    JOptionPane.showMessageDialog(frame, "与该用户私聊窗口已存在，请不要重复私聊!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                String Msg = "<from>[Manager]" + adminName+ "</from><id>0</id>";
                channel.send(formatCodeWithMsg(Msg, 9)); //将自己的个人信息发给想要私聊的用户线程
                break;
            }
        }
    }

    /**
     * @MethodName formatCodeWithMsg
     * @Params  * @param null
     * @Description 消息与命令的格式化
     * @Return
     * @Since 2020/6/6
     */
    private String formatCodeWithMsg(String msg, int code) {
        return "<cmd>" + code + "</cmd><msg>" + msg + "</msg>\n";
    }

    /**
     * @ClassName Server
     * @Params  * @param null
     * @Description 服务器开启TCP接受客户端信息的线程，内部类实现
     * @Return
     * @Since 2020/6/6
     */
    private class Server implements Runnable {

        private Server(ServerSocket socket) {
            serverSocket = socket;
        }

        @Override
        public void run() {
            while (true) {

                try {
                    Socket client = serverSocket.accept();
                    userId++;
                    chatUser user = new chatUser("user" + userId, userId,client);
                    Channel channel = new Channel(user);
                    allUserChannel.add(channel);
                    users_label.setText("Connected User Number " + allUserChannel.size());
                    (new Thread(channel)).start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ;


            }
        }
    }

    /**
     * @ClassName Channel
     * @Params  * @param null
     * @Description 内部类 启动一个线程应对一个客户端的服务
     * @Return
     * @Since 2020/6/6
     */
    protected class Channel implements Runnable {
        private DataInputStream dis;
        private DataOutputStream dos;
        private boolean isRunning;
        private chatUser user;
        private CopyOnWriteArrayList<Channel> shieldList;
        private HashMap<String, Channel> privateQueue;

        public Channel(chatUser user) {
            try {
                this.dis = new DataInputStream(user.getSocket().getInputStream());
                this.dos = new DataOutputStream(user.getSocket().getOutputStream());
                this.shieldList = new CopyOnWriteArrayList<>();
                this.privateQueue = new HashMap<>();
                this.isRunning = true;
                this.user = user;
            } catch (IOException var3) {
                System.out.println("聊天室服务器初始化失败");
                this.release();
            }
        }

        public chatUser getUser() {
            return user;
        }

        public CopyOnWriteArrayList<Channel> getShieldList() {
            return shieldList;
        }

        private void parseMsg(String msg) {
            String code = null;
            String message = null;
            if (msg.length() > 0) {
                //获取指令码
                //Pattern pattern = Pattern.compile(""<cmd>(.*)</cmd>);
                //System.out.println("pattern"+pattern);
                Pattern pattern = Pattern.compile("<cmd>(.*)</cmd>");
                Matcher matcher = pattern.matcher(msg);
                System.out.println("matcher"+matcher);

                if (matcher.find()) {
                    code = matcher.group(1);
                }
                //获取消息


               // System.out.println("pattern"+pattern);
                pattern = Pattern.compile("<msg>(.*)</msg>");
                matcher = pattern.matcher(msg);
                System.out.println("matcher"+matcher);

                if (matcher.find()) {
                    message = matcher.group(1);
                }

                switch (code) {
                    // 该服务器线程对应的客户端线程新用户刚加入
                    case "new":
                        user.setUsername(message);
                        if (!users_model.contains("[User" + user.getId() + "]" + user.getUsername())) {
                            users_model.addElement("[User" + user.getId() + "]" + user.getUsername());
                        }

                        String title = "[System Log] " + df.format(new Date());
                        String content = "[User" + user.getId() + "]" + user.getUsername() + "  join Chat Room";

                        insertMessage(sysTextScrollPane, sysMsgArea, null, title, content, sysVertical, true);
                        sendAnyone(formatCodeWithMsg("<username>" + user.getUsername() + "</username><id>" + user.getId() + "</id>", 4), false);
                        //给当前线程服务的客户端id
                        send(formatCodeWithMsg(String.valueOf(user.getId()), 8));
                        break;
                    case "exit":
                        if (users_model.contains("[User" + user.getId() + "]" + user.getUsername())) {
                            users_model.removeElement("[User" + user.getId() + "]" + user.getUsername());
                        }
                        String logTitle = "[System Log] " + df.format(new Date());
                        String logContent = "[User" + user.getId() + "]" + user.getUsername() + "  Quit Chat Room";
                        insertMessage(sysTextScrollPane, sysMsgArea, null, logTitle, logContent, sysVertical, true);
                        allUserChannel.remove(this);
                        sendAnyone(formatCodeWithMsg("" + user.getId(), 5), false);
                        this.release(); //移除用户，关闭线程。
                        break;
                    case "getList":
                        //给客户端传送
                        send(formatCodeWithMsg(getUsersList(), 7));
                        break;
                    case "msg":
                        String now = df.format(new Date());
                        //写入服务端的聊天世界中
                        insertMessage(userTextScrollPane, userMsgArea, null,  "[User" + user.getId() + "]" + user.getUsername() + " "+now, " "+message, userVertical, false);
                        // 将自己说的话发给每个人
                        sendAnyone(formatCodeWithMsg("<userId>" + user.getId() + "</userId><userMsg>" + message + "</userMsg><time>" + now + "</time>", 1), false);
                        break;
                    case "buildPrivateChat":
                        //建立私聊机制

                        //如果私聊对象是管理员
                        if (message.equals("0")){
                            int option = JOptionPane.showConfirmDialog(frame, "[" + user.getUsername() + "]want to chat with you, are you agree？", "Hint",
                                    JOptionPane.YES_NO_OPTION);
                            if (option == JOptionPane.YES_OPTION) { //同意私聊
                                String agreeMsg = "<result>1</result><from>" + adminName+ "</from><id>0</id>";
                                send(formatCodeWithMsg(agreeMsg, 10));
                                privateChatFrame privateChatFrame = new privateChatFrame("与[" + user.getUsername() + "]'s Chat Window", user.getUsername(), user.getId().toString());
                                adminPrivateQueue.put(user.getId().toString(),privateChatFrame);
                                adminPrivateThread.put(user.getId().toString(), this);
                            }else{ //拒绝私聊
                                String refuseMsg = "<result>0</result><from>" + adminName + "</from><id>0</id>";
                                send(formatCodeWithMsg(refuseMsg, 10));
                            }
                        }else { //普通用户私聊对象
                            for (Channel channel : allUserChannel) {
                                if (channel.getUser().getId() == Integer.parseInt(message)) {
                                    String Msg = "<from>" + user.getUsername() + "</from><id>" + user.getId() + "</id>";
                                    this.privateQueue.put(message, channel); //先将对方放入私聊队列
                                    channel.privateQueue.put(user.getId().toString(), this); //对方也将你放入私聊队列
                                    channel.send(formatCodeWithMsg(Msg, 9)); //将自己的个人信息发给想要私聊的用户线程
                                    break;
                                }
                            }
                        }
                        break;
                    case "agreePrivateChar": //同意与此ID的人进行私聊
                        if (message.equals("0")){ //如果对方是管理员
                            privateChatFrame privateChatFrame = new privateChatFrame("and[" + user.getUsername() + "]'s private room", user.getUsername(), user.getId().toString());
                            adminPrivateQueue.put(user.getId().toString(),privateChatFrame);
                            adminPrivateThread.put(user.getId().toString(), this);
                        }else { //普通用户
                            String agreeMsg = "<result>1</result><from>" + user.getUsername() + "</from><id>" + user.getId() + "</id>";
                            privateQueue.get(message).send(formatCodeWithMsg(agreeMsg, 10));
                        }
                        break;
                    case "refusePrivateChar"://拒绝与此ID的人进行私聊，从私聊队列移除
                        if(message.equals("0")){ //如果是管理员
                            JOptionPane.showMessageDialog(frame, "[" + user.getUsername() + "]refused your private chat request", "Failed", JOptionPane.ERROR_MESSAGE);
                        }else {
                            String refuseMsg = "<result>0</result><from>" + user.getUsername() + "</from><id>" + user.getId() + "</id>";
                            privateQueue.get(message).send(formatCodeWithMsg(refuseMsg, 10));
                            privateQueue.get(message).privateQueue.remove(user.getId()); //对方也移除
                            privateQueue.remove(message); //移除对方
                        }
                        break;
                    case "privateMsg": //转发私聊消息
                        Pattern privatePattern = Pattern.compile("<msg>(.*)</msg><id>(.*)</id>");
                        Matcher privateMatcher = privatePattern.matcher(message);
                        if (privateMatcher.find()) {
                            String toPrivateMsg = privateMatcher.group(1);
                            String toPrivateId = privateMatcher.group(2);
                            if (toPrivateId.equals("0")){ //想要发给管理员
                                //管理员主线程获取当前服务线程对应的私聊窗口
                                privateChatFrame nowPrivateChat = adminPrivateQueue.get(user.getId().toString());
                                insertMessage(nowPrivateChat.textScrollPane,nowPrivateChat.msgArea,null, df.format(new Date()) + " Other said", " "+toPrivateMsg, nowPrivateChat.vertical, false);
                            }else {
                                String resultMsg = "<msg>" + toPrivateMsg + "</msg><id>" + user.getId() + "</id>";
                                //根据信息来源ID（想要转发的用户ID）找到对应线程将自己的id和信息发给他
                                privateQueue.get(toPrivateId).send(formatCodeWithMsg(resultMsg, 11));
                            }
                        }
                        break;
                    case "privateExit":
                        if (message.equals("0")){ //如果是管理员
                            JOptionPane.showMessageDialog(frame, "As the other ends the private chat,the private window will close!", "Hint", JOptionPane.WARNING_MESSAGE);
                            adminPrivateQueue.get(user.getId().toString()).dispose();
                            insertMessage(sysTextScrollPane, sysMsgArea, null, "[System Message] " + df.format(new Date()), "Because[" + user.getUsername() + "]close the private window，private chat ends!", sysVertical, true);
                            adminPrivateQueue.remove(user.getId().toString());   //移除此私聊对话窗口
                            adminPrivateThread.remove(user.getId().toString());  //移除此私聊对话窗口线程
                        }else {//普通用户私聊
                            String endMsg = "<id>" + user.getId() + "</id>";
                            privateQueue.get(message).send(formatCodeWithMsg(endMsg, 12));
                            privateQueue.get(message).privateQueue.remove(this.user.getId().toString()); //对方也移除
                            privateQueue.remove(message); //移除对方
                        }
                        break;
                    case "shield": //将传来的id对应的服务线程加入到屏蔽列表里面
                        for (Channel channel : allUserChannel) {
                            if (channel.getUser().getId() == Integer.parseInt(message)) {
                                if (!shieldList.contains(channel)) {
                                    shieldList.add(channel);
                                }
                                break;
                            }
                        }
                        System.out.println("Blocking"+shieldList);
                        break;
                    case "unshield": //将传来的id对应的服务线程从屏蔽列表里面删除
                        for (Channel channel : allUserChannel) {
                            if (channel.getUser().getId() == Integer.parseInt(message)) {
                                if (shieldList.contains(channel)) {
                                    shieldList.remove(channel);
                                }
                                break;
                            }
                        }
                        System.out.println("Cancel blocking:"+shieldList);
                        break;
                    case "setName":
                        // 改了昵称，跟其它客户端的用户列表进行更正
                        users_model.removeElement(user.getId() + "#@" + user.getUsername());
                        user.setUsername(message);
                        users_model.addElement(user.getId() + "#@" + user.getUsername());
                        sendAnyone(formatCodeWithMsg("<id>" + user.getId() + "</id><username>" + message + "</username>", 6), false);
                        break;
                    default:
                        System.out.println("not valid message from user" + user.getId());
                        break;
                }
            }
        }


        private String getUsersList() {
            StringBuffer stringBuffer = new StringBuffer();
            /*获得房间中所有的用户的列表，然后构造成一定的格式发送回去*/
            stringBuffer.append("<user><id>0</id><username>" + adminName + "</username></user>");
            for (Channel each : allUserChannel) {
                stringBuffer.append("<user><id>" + each.getUser().getId() +
                        "</id><username>" + each.getUser().getUsername() + "</username></user>");
            }
            return stringBuffer.toString();
        }

        private String receive() {
            String msg = "";
            try {
                msg = this.dis.readUTF();
            } catch (IOException var3) {
                this.release();
            }
            System.out.println("msg Received");
            return msg;
        }

        public void send(String msg) {
            try {
                this.dos.writeUTF(msg);
                this.dos.flush();
            } catch (IOException var3) {
                insertMessage(sysTextScrollPane, sysMsgArea, null, "[System Log] "+df.format(new Date()), "User[" + user.getUsername() + "]的服务器端服务线程出错，请重启服务器!", sysVertical, true);
                this.release();
            }

        }

        private void sendAnyone(String msg, boolean isSys) {

            for (Channel userChannel : allUserChannel) {
                //获取每个用户的线程类
                if (!userChannel.getShieldList().contains(this)) {//当前服务线程不在对方线程屏蔽组内的才发送信息
                    userChannel.send(msg);
                }
            }
        }

        //释放资源
        public void release() {
            this.isRunning = false;
            CloseUtils.close(dis, dos);
            // 列表中移除用户
            users_model.removeElement(user.getId() + "#@" + user.getUsername());
            if (allUserChannel.contains(this)) {
                allUserChannel.remove(this);
            }
            users_label.setText("Current connected user number:" + allUserChannel.size());
        }

        @Override
        public void run() {
            while (isRunning) {
                String msg = receive();
                if (!msg.equals("")) {
                    parseMsg(msg);
                }
            }
        }
    }

    /**
     * @ClassName privateChatFrame
     * @Params  * @param null
     * @Description 管理员所属的私聊窗口内部类
     * @Return
     * @Since 2020/6/6
     */
    private class privateChatFrame extends JFrame {
        private String otherName;
        private String otherId;
        private JButton sendButton;
        private JTextField msgTestField;
        private JTextPane msgArea;
        private JScrollPane textScrollPane;
        private JScrollBar vertical;

        public privateChatFrame(String title, String otherName, String otherId) throws HeadlessException {
            super(title);
            this.otherName = otherName;
            this.otherId = otherId;
            //全局面板容器
            JPanel panel = new JPanel();
            //全局布局
            BorderLayout layout = new BorderLayout();

            JPanel headpanel = new JPanel();    //上层panel，
            JPanel footpanel = new JPanel();    //下层panel
            JPanel centerpanel = new JPanel(); //中间panel

            //头部布局
            FlowLayout flowLayout = new FlowLayout();
            //底部布局
            GridBagLayout gridBagLayout = new GridBagLayout();

            setSize(600, 500);
            setLocationRelativeTo(null);
            setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            //窗口关闭事件
            addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    int option = JOptionPane.showConfirmDialog(e.getOppositeWindow(), "确定结束私聊？", "提示",
                            JOptionPane.YES_NO_OPTION);
                    if (option == JOptionPane.YES_OPTION) {
                        if (server != null) {
                            //关闭当前私聊连接
                            String endMsg = "<id>0</id>";
                            adminPrivateThread.get(otherId).send(formatCodeWithMsg(endMsg, 12)); //关闭当前私聊连接
                            adminPrivateQueue.remove(otherId);   //移除此私聊对话窗口
                            adminPrivateThread.remove(otherId);
                        }
                        insertMessage(sysTextScrollPane, sysMsgArea, null, "[System Message] " + df.format(new Date()), "you and[" + otherName + "]‘s private chat", sysVertical, true);
                        dispose();
                    } else {
                        return;
                    }
                }
            });
            setContentPane(panel);
            setLayout(layout);

            headpanel.setLayout(flowLayout);
            footpanel.setLayout(gridBagLayout);
            footpanel.setPreferredSize(new Dimension(0, 40));
            centerpanel.setLayout(gridBagLayout);

            //添加头部部件
            JLabel Name = new JLabel(otherName);
            headpanel.add(Name);

            //设置底部布局
            sendButton = new JButton("Send");
            sendButton.setPreferredSize(new Dimension(40, 0));
            msgTestField = new JTextField();
            footpanel.add(msgTestField, new GridBagConstraints(0, 0, 1, 1, 100, 100,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(1, 1, 1, 1), 0, 0));
            footpanel.add(sendButton, new GridBagConstraints(1, 0, 1, 1, 10, 10,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(1, 1, 1, 1), 0, 0));

            //中间布局
            msgArea = new JTextPane();
            msgArea.setEditable(false);
            textScrollPane = new JScrollPane();
            textScrollPane.setViewportView(msgArea);
            vertical = new JScrollBar(JScrollBar.VERTICAL);
            vertical.setAutoscrolls(true);
            textScrollPane.setVerticalScrollBar(vertical);
            centerpanel.add(textScrollPane, new GridBagConstraints(0, 0, 1, 1, 100, 100,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

            //设置顶层布局
            panel.add(headpanel, "North");
            panel.add(footpanel, "South");
            panel.add(centerpanel, "Center");

            //聊天信息输入框的监听回车按钮事件
            msgTestField.addKeyListener(new KeyAdapter() {
                @Override
                public void keyTyped(KeyEvent e) {
                    if (e.getKeyChar() == KeyEvent.VK_ENTER) {
                        {/* if (server == null) {
                            JOptionPane.showMessageDialog(frame, "请先开启聊天室的服务器!", "提示", JOptionPane.WARNING_MESSAGE);
                            return;
                        }*/}
                        String text = msgTestField.getText();
                        if (text != null && !text.equals("")) {
                            String resultMsg = "<msg>"+text+"</msg><id>0</id>";
                            adminPrivateThread.get(otherId).send(formatCodeWithMsg(resultMsg,11));
                            msgTestField.setText("");
                            insertMessage(textScrollPane, msgArea, null, df.format(new Date()) + " You said", " "+text, vertical, false);
                        }
                    }
                }
            });
            sendButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String cmd = e.getActionCommand();
                    if (cmd.equals("Send")) {
                        {/*
                        if (server == null) {
                            JOptionPane.showMessageDialog(frame, "请先开启聊天室的服务器!", "提示", JOptionPane.WARNING_MESSAGE);
                            return;
                        }*/}
                        String text = msgTestField.getText();
                        if (text != null && !text.equals("")) {
                            String resultMsg = "<msg>"+text+"</msg><id>0</id>";
                            adminPrivateThread.get(otherId).send(formatCodeWithMsg(resultMsg,11));
                            msgTestField.setText("");
                            insertMessage(textScrollPane, msgArea, null, df.format(new Date()) + " 你说:", " "+text, vertical, false);
                        }
                    }
                }
            });
            //窗口显示
            setVisible(true);
        }
    }

    /**
     * @MethodName setUIStyle
     * @Params  * @param null
     * @Description 根据操作系统自动变化GUI界面风格
     * @Return
     * @Since 2020/6/6
     */
    public static void setUIStyle() {
//        String lookAndFeel = UIManager.getSystemLookAndFeelClassName(); //设置当前系统风格
        String lookAndFeel = UIManager.getCrossPlatformLookAndFeelClassName(); //可跨系统
        try {
            UIManager.setLookAndFeel(lookAndFeel);
            UIManager.put("Menu.font", new Font("宋体", Font.PLAIN, 12));
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InstantiationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (UnsupportedLookAndFeelException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
