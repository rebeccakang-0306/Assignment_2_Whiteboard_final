


import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



/**
 1. @Author: Himit_ZH
 2. @Date: 2020/6/4 14:55
 3. @Description: 聊天室客户端GUI
 */
public class chatClient {
    private JFrame frame;
    //头部参数
    private JTextField host_textfield;
    private JTextField port_textfield;
    private JTextField name_textfield;
    private JButton head_connect;
    private JButton head_exit;
    //底部参数
    private JTextField text_field;
    private JButton foot_send;
    private JButton foot_sysClear;
    private JButton foot_userClear;

    //右边参数
    private JLabel users_label;
    private JButton privateChat_button;
    private JButton shield_button;
    private JButton unshield_button;
    private static  JList<String> userlist;
    private DefaultListModel<String> users_model;
    private HashMap<String, Integer> users_map;

    //左边参数
    private JScrollPane sysTextScrollPane;
    private JTextPane sysMsgArea;
    private JScrollBar sysVertical;

    //中间参数
    private JScrollPane userTextScrollPane;
    private JTextPane userMsgArea;
    private JScrollBar userVertical;

    //发送和接受参数
    private DataOutputStream dos;
    private Receive receive;
    private Socket charClient;
    private Socket clientSocket;
    private static String clientname;
    //时间格式化工具类
    static private SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");//设置时间

    //当前用户的id
    private int id;

    //私聊窗口Map
    private HashMap<String, privateChatFrame> privateChatFrameMap;

    public static void main(String[] args) {

        new chatClient();
    }

    /**
     * @MethodName init
     * @Params * @param null
     * @Description 客户端GUI界面初始化，各种监听事件绑定
     * @Return
     * @Since 2020/6/6
     */
    public void init(String username) {
        this.clientname = username;
        users_map = new HashMap<>();
        privateChatFrameMap = new HashMap<>();
        /*设置窗口的UI风格和字体*/
        setUIStyle();
        frame = new JFrame("White Board Chat Room");
        JPanel panel = new JPanel();        /*主要的panel，上层放置连接区，下层放置消息区，中间是消息面板，左边是系统消息，右边是当前room的用户列表*/
        JPanel headpanel = new JPanel();    /*上层panel，用于放置连接区域相关的组件*/
        JPanel footpanel = new JPanel();    /*下层panel，用于放置发送信息区域的组件*/
        JPanel centerpanel = new JPanel();    /*中间panel，用于放置聊天信息*/
        JPanel leftpanel = new JPanel();    /*左边panel，用于放置房间列表和加入按钮*/
        JPanel rightpanel = new JPanel();   /*右边panel，用于放置房间内人的列表*/

        /*顶层的布局，分中间，东南西北五个部分*/
        BorderLayout layout = new BorderLayout();

        /*格子布局，主要用来设置西、东、南三个部分的布局*/
        GridBagLayout gridBagLayout = new GridBagLayout();

        /*主要设置北部的布局*/
        FlowLayout flowLayout = new FlowLayout();

        /*设置初始窗口的一些性质*/
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        frame.setContentPane(panel);
        frame.setLayout(layout);

        /*设置各个部分的panel的布局和大小*/
        headpanel.setLayout(flowLayout);
        footpanel.setLayout(gridBagLayout);
        leftpanel.setLayout(gridBagLayout);
        centerpanel.setLayout(gridBagLayout);
        rightpanel.setLayout(gridBagLayout);

        //设置面板大小
        leftpanel.setPreferredSize(new Dimension(200, 0));
        rightpanel.setPreferredSize(new Dimension(155, 0));
        footpanel.setPreferredSize(new Dimension(0, 40));

        //头部布局
        host_textfield = new JTextField("127.0.0.1");
        port_textfield = new JTextField("1");
        name_textfield = new JTextField(username);
        host_textfield.setPreferredSize(new Dimension(100, 25));
        port_textfield.setPreferredSize(new Dimension(70, 25));
        name_textfield.setPreferredSize(new Dimension(150, 25));


        JLabel name_label = new JLabel("username:");

        headpanel.add(name_label);
        headpanel.add(name_textfield);

        //底部布局
        foot_send = new JButton("Send");
        foot_sysClear = new JButton("Delete System Message");
        foot_sysClear.setPreferredSize(new Dimension(193, 0));
        foot_userClear = new JButton("Delete Chat Message");
        foot_userClear.setPreferredSize(new Dimension(148, 0));

        text_field = new JTextField();
        footpanel.add(foot_sysClear, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(1, 1, 1, 1), 0, 0));
        footpanel.add(text_field, new GridBagConstraints(1, 0, 1, 1, 100, 100,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(1, 1, 1, 1), 0, 0));
        footpanel.add(foot_send, new GridBagConstraints(2, 0, 1, 1, 1.0, 1.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(1, 1, 1, 1), 0, 0));
        footpanel.add(foot_userClear, new GridBagConstraints(3, 0, 1, 1, 1.0, 1.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(1, 1, 1, 1), 0, 0));


        //左边布局
        JLabel sysMsg_label = new JLabel("System Message:");
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
        users_model = new DefaultListModel<>();
        userlist = new JList<String>(users_model);
        JScrollPane userListPane = new JScrollPane(userlist);
        users_label = new JLabel("Chat room user number:0");
        privateChat_button = new JButton("Private Chat");
        shield_button = new JButton("Block");
        unshield_button = new JButton("Unblock");




        rightpanel.add(users_label, new GridBagConstraints(0, 0, 1, 1, 1, 1,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
        rightpanel.add(privateChat_button, new GridBagConstraints(0, 1, 1, 1, 1, 1,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
        rightpanel.add(shield_button, new GridBagConstraints(0, 2, 1, 1, 1, 1,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
        rightpanel.add(unshield_button, new GridBagConstraints(0, 3, 1, 1, 1, 1,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
        rightpanel.add(userListPane, new GridBagConstraints(0, 4, 1, 1, 100, 100,
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

        /*设置顶层布局*/
        panel.add(headpanel, "North");
        panel.add(footpanel, "South");
        panel.add(leftpanel, "West");
        panel.add(rightpanel, "East");
        panel.add(centerpanel, "Center");


        //将按钮事件全部注册到监听器
        allActionListener allActionListener = new allActionListener();

        //往服务器发送消息
        foot_send.addActionListener(allActionListener);

        //清空系统消息
        foot_sysClear.addActionListener(allActionListener);
        //清空世界聊天消息
        foot_userClear.addActionListener(allActionListener);
        //Private Chat
        privateChat_button.addActionListener(allActionListener);
        //屏蔽
        shield_button.addActionListener(allActionListener);
        //Unblock
        unshield_button.addActionListener(allActionListener);

        //窗口关闭事件
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                sendMsg("exit", "");
                head_connect.setText("Connect");
                head_exit.setText("Already Quit");
                port_textfield.setEditable(true);
                name_textfield.setEditable(true);
                host_textfield.setEditable(true);
                try {
                    charClient.close();
                } catch (IOException ioException) {
                    JOptionPane.showMessageDialog(frame, "Close socket failed!", "Error", JOptionPane.ERROR_MESSAGE);
                }
                charClient = null;
                receive = null;
                int option = JOptionPane.showConfirmDialog(null, "Are you sure you want to quit?",
                        "Close Window Warning", JOptionPane.YES_NO_OPTION);
                if (option == JOptionPane.YES_OPTION) {
                    if (e.getWindow() == frame) {
                         if (receive != null) {
                            sendMsg("exit", ""); //如果已连接就告诉服务器本客户端已断开连接，退出聊天室
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
                        sendMsg("msg", text);
                        text_field.setText("");
                    }
                }
            }
        });


        //窗口显示
        frame.setVisible(true);

        name_textfield.setText(username);

        System.out.println("Connected with the server");

        String host = host_textfield.getText();
        String port = port_textfield.getText();
        //连接服务器，开启线程
        try {
            charClient = new Socket(host,  Integer. parseInt(port));
            dos = new DataOutputStream(charClient.getOutputStream());
            receive = new Receive(charClient, this);
            (new Thread(receive)).start();//接受服务器的消息的线程(系统消息和其他网友的信息）

            port_textfield.setEditable(false);
            name_textfield.setEditable(false);
            host_textfield.setEditable(false);
            sendMsg("new", name_textfield.getText()); //后续写在登录窗口
            sendMsg("getList", "");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "Connect Failed！", "Error", JOptionPane.ERROR_MESSAGE);

        }




        name_textfield.setText(clientname);
    }


    /**
     * @ClassName allActionListener
     * @Params * @param null
     * @Description 全局监听事件，监听所有按钮，输入框，内部类
     * @Return
     * @Since 2020/6/6
     */
    private class allActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {

            String cmd = e.getActionCommand();

            switch (cmd) {

                case "Send":

                    String text = text_field.getText();
                    if (text != null && !text.equals("")) {
                        sendMsg("msg", text);
                        text_field.setText("");
                         }
                    break;
                case "Private Chat":

                    String selected = userlist.getSelectedValue();
                    System.out.println(users_map.get(selected).toString());
                    //私聊自己
                    if (selected.equals(getUserName(String.valueOf(id)))) {
                        JOptionPane.showMessageDialog(frame, "You can not chat with yourself!", "Warning", JOptionPane.WARNING_MESSAGE);
                        break;
                    }
                    //已有私聊窗口
                    if (privateChatFrameMap.containsKey(users_map.get(selected).toString())) {
                        JOptionPane.showMessageDialog(frame, "Chat window already exits.Do not talk to the same person!", "Error", JOptionPane.ERROR_MESSAGE);
                        break;
                    }
                    if (users_map.containsKey(selected)) {
                        sendMsg("buildPrivateChat", String.valueOf(users_map.get(selected)));
                        //建立沟通弹窗
                    }
                    break;
                case "Block":

                    String selectedShield = userlist.getSelectedValue();
                    //如果是自己
                    if (selectedShield.equals(getUserName(String.valueOf(id)))) {
                        JOptionPane.showMessageDialog(frame, "You can not block yourself!", "Warning", JOptionPane.WARNING_MESSAGE);
                        break;
                    }
                    //不准屏蔽管理员!
                    if (selectedShield.equals(getUserName(String.valueOf(0)))) {
                        JOptionPane.showMessageDialog(frame, "Sorry!Can not block the manager!", "Warning", JOptionPane.WARNING_MESSAGE);
                        break;
                    }
                    //不能重复屏蔽!
                    if (selectedShield.contains("(Blocked)")) {
                        JOptionPane.showMessageDialog(frame, "You already blocked the user!", "Error", JOptionPane.ERROR_MESSAGE);
                        break;
                    }
                    if (users_map.containsKey(selectedShield)) { //发送需要屏蔽用户的id
                        sendMsg("shield", String.valueOf(users_map.get(selectedShield)));
                        users_map.put(selectedShield + "(Blocked)", users_map.get(selectedShield));
                        users_map.remove(selectedShield);
                    }
                    int index1 = users_model.indexOf(selectedShield);
                    users_model.set(index1, selectedShield + "(Blocked)");
                    break;
                case "Unblock":

                    String unShield = userlist.getSelectedValue();
                    if (unShield.indexOf("(Blocked)") == -1) {
                        JOptionPane.showMessageDialog(frame, "The user are not blocked!", "Warning", JOptionPane.WARNING_MESSAGE);
                        break;
                    }
                    String name = unShield.substring(0, unShield.indexOf("(Blocked)"));
                    sendMsg("unshield", String.valueOf(users_map.get(unShield)));
                    int index2 = users_model.indexOf(unShield);
                    users_model.set(index2, name);
                    users_map.put(name, users_map.get(unShield));
                    users_map.remove(unShield);
                    break;

                case "Delete System Message":
                    sysMsgArea.setText("");
                    break;
                case "Delete Chat Message":
                    userMsgArea.setText("");
                    break;
                default:
                    break;
            }

        }
    }


    /**
     * @MethodName connectServer
     * @Params * @param null
     * @Description 开启与服务器的连接，开启接受服务器的指令与信息的Receive线程类
     * @Return
     * @Since 2020/6/6
     */


    private boolean connectServer(String host, int port) {

        try {
            charClient = new Socket(host, port);
            dos = new DataOutputStream(charClient.getOutputStream());
            receive = new Receive(charClient, this);
            (new Thread(receive)).start();//接受服务器的消息的线程(系统消息和其他网友的信息）
            head_connect.setText("Connected");
            head_exit.setText("Quit");
            port_textfield.setEditable(false);
            name_textfield.setEditable(false);
            host_textfield.setEditable(false);
            sendMsg("new", name_textfield.getText()); //后续写在登录窗口
            sendMsg("getList", "");
            return true;
        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "连接服务器失败！", "错误", JOptionPane.ERROR_MESSAGE);
            return false;
        }

    }

    /**
     * @MethodName sendMsg
     * @Params * @param null
     * @Description 发送指令与信息给服务器端对应的线程服务
     * @Return
     * @Since 2020/6/6
     */
    private void sendMsg(String cmd, String msg) {
        try {
            System.out.println("char send" + cmd + "msg:" + msg);
            dos.writeUTF("<cmd>" + cmd + "</cmd><msg>" + msg + "</msg>");
            //dos.writeUTF(cmd + "::" + msg);
            dos.flush();
        } catch (IOException e) {
            CloseUtils.close(dos, charClient);
        }
    }

    public void setId(int id) {
        this.id = id;
    }

    /**
     * @MethodName updateTextArea
     * @Params * @param null
     * @Description 更新系统文本域或聊天事件文本域
     * @Return
     * @Since 2020/6/6
     */
    public void updateTextArea(String content, String where) {
        // todo: 聊天消息处理
        if (content.length() > 0) {
            Matcher matcher = null;
            if (where.equals("user")) {
                Pattern pattern = Pattern.compile("<userId>(.*)</userId><userMsg>(.*)</userMsg><time>(.*)</time>");
                matcher = pattern.matcher(content);
                if (matcher.find()) {
                    String userId = matcher.group(1);
                    String userMsg = matcher.group(2);
                    String time = matcher.group(3);

                    if (userId.equals("0")) {
                        insertMessage(userTextScrollPane, userMsgArea, null, getUserName(userId) + " " + time, " " + userMsg, userVertical, false);
                    } else {
                        String fromName = getUserName(userId);
                        if (fromName.equals("[User" + id + "]" + name_textfield.getText())) //如果是自己说的话
                            fromName = "you";
                        insertMessage(userTextScrollPane, userMsgArea, null, fromName + " " + time, " " + userMsg, userVertical, false);
                    }
                }
            } else {
                Pattern pattern = Pattern.compile("<time>(.*)</time><sysMsg>(.*)</sysMsg>");
                matcher = pattern.matcher(content);
                if (matcher.find()) {
                    String sysTime = matcher.group(1);
                    String sysMsg = matcher.group(2);
                    insertMessage(sysTextScrollPane, sysMsgArea, null, "[System Message] " + sysTime, sysMsg, sysVertical, true);
                }
            }
        }
    }

    /**
     * @MethodName insertMessage
     * @Params * @param null
     * @Description 更新文本域信息格式化工具
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
        vertical.setValue(vertical.getMaximum());
    }

    /**
     * @MethodName getUserName
     * @Params * @param null
     * @Description 在users_map中根据value值用户ID获取key值的用户名字
     * @Return
     * @Since 2020/6/6
     */
    private String getUserName(String strId) {
        int uid = Integer.parseInt(strId);
        Set<String> set = users_map.keySet();
        Iterator<String> iterator = set.iterator();
        String cur = null;
        while (iterator.hasNext()) {
            cur = iterator.next();
            if (users_map.get(cur) == uid) {
                return cur;
            }
        }
        return "";
    }


    /**
     * @MethodName showEscDialog
     * @Params * @param null
     * @Description 处理当前客户端用户断开与服务器连接的一切事务
     * @Return
     * @Since 2020/6/6
     */
    public void showEscDialog(String content) {

        //清除所有私聊
        if (privateChatFrameMap.size() != 0) {
            Set<Map.Entry<String, privateChatFrame>> entrySet = privateChatFrameMap.entrySet();
            Iterator<Map.Entry<String, privateChatFrame>> iter = entrySet.iterator();
            while (iter.hasNext()) {
                Map.Entry<String, privateChatFrame> entry = iter.next();
                entry.getValue().dispose(); //关闭对应窗口
                sendMsg("privateExit", entry.getKey()); //想对方说明私聊结束
            }
        }
        //关闭输出流
        CloseUtils.close(dos, charClient);
        receive.setRunning(false);
        //输入框可编辑
        port_textfield.setEditable(true);
        name_textfield.setEditable(true);
        host_textfield.setEditable(true);
        //head_connect.setText("Connect");
        head_exit.setText("Quit");
        insertMessage(sysTextScrollPane, sysMsgArea, null, "[System Message] " + df.format(new Date()), content, sysVertical, true);
        JOptionPane.showMessageDialog(frame, content, "Hint", JOptionPane.WARNING_MESSAGE);
        /*清除消息区内容，清除用户数据模型内容和用户map内容，更新房间内人数*/
        userMsgArea.setText("");
//      sysMsgArea.setText("");
        users_map.clear();
        users_model.removeAllElements();
        users_label.setText("User number:0");

    }

    /**
     * @MethodName addUser
     * @Params * @param null
     * @Description 当有新的用户加入聊天室，系统文本域的更新和用户列表的更新
     * @Return
     * @Since 2020/6/6
     */
    public void addUser(String content) {
        if (content.length() > 0) {
            Pattern pattern = Pattern.compile("<username>(.*)</username><id>(.*)</id>");
            Matcher matcher = pattern.matcher(content);
            if (matcher.find()) {
                String name = matcher.group(1);
                String id = matcher.group(2);
                if (!users_map.containsKey(name)) {
                    users_map.put("[User" + id + "]" + name, Integer.parseInt(id));
                    users_model.addElement("[User" + id + "]" + name);
                } else {
                    users_map.remove("[User" + id + "]" + name);
                    users_model.removeElement(name);
                    users_map.put("[User" + id + "]" + name, Integer.parseInt(id));
                    users_model.addElement("[User" + id + "]" + name);
                }
                insertMessage(sysTextScrollPane, sysMsgArea, null, "[System Message] " + df.format(new Date()), "[User" + id + "]" + name + " join the chat room", sysVertical, true);
            }
        }
        users_label.setText("Chat Room Number:" + users_map.size()); //更新房间内的人数
    }


    /**
     * @MethodName delUser
     * @Params content(为退出用户的ID)
     * @Description 当有用户退出时，系统文本域的通知和用户列表的更新
     * @Return
     * @Since 2020/6/6
     */
    public void delUser(String content) {
        if (content.length() > 0) {
            Set<String> set = users_map.keySet();
            String delName = getUserName(content);
            insertMessage(sysTextScrollPane, sysMsgArea, null, "[System Message] " + df.format(new Date()), delName + " 退出了聊天室", sysVertical, true);
            users_map.remove(delName);
            users_model.removeElement(delName);
        }
        users_label.setText("Chat Room Number:" + users_map.size());//更新房间内的人数
    }


    /**
     * @MethodName updateUsername
     * @Params content(为指定用户的ID)
     * @Description 修改指定ID用户的昵称（暂时用不到）
     * @Return
     * @Since 2020/6/6
     */

    /**
     * @MethodName getUserList
     * @Params * @param null
     * @Description 从服务器获取全部用户信息的列表，解析信息格式，列出所有用户
     * @Return
     * @Since 2020/6/6
     */
    public void getUserList(String content) {
        String name = null;
        String id = null;
        Pattern numPattern = null;
        Matcher numMatcher = null;
        Pattern userListPattern = null;

        if (content.length() > 0) {
            numPattern = Pattern.compile("<user>(.*?)</user>");
            numMatcher = numPattern.matcher(content);
            //遍历字符串，进行正则匹配，获取所有用户信息
            while (numMatcher.find()) {
                String detail = numMatcher.group(1);
                userListPattern = Pattern.compile("<id>(.*)</id><username>(.*)</username>");
                Matcher userListmatcher = userListPattern.matcher(detail);
                if (userListmatcher.find()) {
                    name = userListmatcher.group(2);
                    id = userListmatcher.group(1);
                    if (id.equals("0")) {
                        name = "[Manager]" + name;
                        users_map.put(name, Integer.parseInt(id));
                    } else {
                        name = "[User" + id + "]" + name;
                        users_map.put(name, Integer.parseInt(id));
                    }
                    users_model.addElement(name);
                }
            }
            users_model.removeElementAt(0);
        }
        users_label.setText("Chat Room Number:" + users_map.size());
    }


    /**
     * @MethodName askBuildPrivateChat
     * @Params * @param null
     * @Description 处理某用户对当前客户端的用户的私聊请求
     * @Return
     * @Since 2020/6/6
     */
    public void askBuildPrivateChat(String msg) {
        Pattern pattern = Pattern.compile("<from>(.*)</from><id>(.*)</id>");
        Matcher matcher = pattern.matcher(msg);
        if (matcher.find()) {
            String toPrivateChatName = matcher.group(1);
            String toPrivateChatId = matcher.group(2);
            int option = JOptionPane.showConfirmDialog(frame, "[" + toPrivateChatName + "]wants to chat with you，Are you agree？", "Hint",
                    JOptionPane.YES_NO_OPTION);
            if (option == JOptionPane.YES_OPTION) {
                sendMsg("agreePrivateChar", toPrivateChatId);
                privateChatFrame chatFrame = new privateChatFrame("and[" + toPrivateChatName + "]'s private chat window", toPrivateChatName, toPrivateChatId);
                privateChatFrameMap.put(toPrivateChatId, chatFrame);
            } else {
                sendMsg("refusePrivateChar", toPrivateChatId);
            }
        }
    }


    /**
     * @MethodName startOrStopHisPrivateChat
     * @Params * @param null
     * @Description 获取请求指定用户的私聊请求的结果，同意就开启私聊窗口，拒绝就提示。
     * @Return
     * @Since 2020/6/6
     */
    public void startOrStopHisPrivateChat(String msg) {
        Pattern pattern = Pattern.compile("<result>(.*)</result><from>(.*)</from><id>(.*)</id>");
        Matcher matcher = pattern.matcher(msg);
        if (matcher.find()) {
            String result = matcher.group(1);
            String toPrivateChatName = matcher.group(2);
            String toPrivateChatId = matcher.group(3);
            if (result.equals("1")) {  //对方同意的话
                if (toPrivateChatId.equals("0")) {
                    toPrivateChatName = "[Manager]" + toPrivateChatName;
                }
                privateChatFrame chatFrame = new privateChatFrame("与[" + toPrivateChatName + "]'s private chat window", toPrivateChatName, toPrivateChatId);
                privateChatFrameMap.put(toPrivateChatId, chatFrame);
            } else if (result.equals("0")) {
                JOptionPane.showMessageDialog(frame, "[" + toPrivateChatName + "]refused your private char", "Failed", JOptionPane.ERROR_MESSAGE);
            }
        }
    }


    /**
     * @MethodName giveMsgToPrivateChat
     * @Params * @param null
     * @Description 根据服务器端发来的用户ID和内容，搜寻当前客户端的用户中对应传来的用户ID的私聊窗口，将内容写进私聊窗口的文本域
     * @Return
     * @Since 2020/6/6
     */
    public void giveMsgToPrivateChat(String msg) {
        Pattern privatePattern = Pattern.compile("<msg>(.*)</msg><id>(.*)</id>");
        Matcher privateMatcher = privatePattern.matcher(msg);
        if (privateMatcher.find()) {
            String toPrivateMsg = privateMatcher.group(1);
            String toPrivateId = privateMatcher.group(2);
            privateChatFrame chatFrame = privateChatFrameMap.get(toPrivateId);
            insertMessage(chatFrame.textScrollPane, chatFrame.msgArea, null, df.format(new Date()) + " 对方说:", " " + toPrivateMsg, chatFrame.vertical, false);
        }
    }


    /**
     * @MethodName endPrivateChat
     * @Params * @param null
     * @Description 结束指定id用户的私聊窗口
     * @Return
     * @Since 2020/6/6
     */
    public void endPrivateChat(String msg) {
        Pattern privatePattern = Pattern.compile("<id>(.*)</id>");
        Matcher privateMatcher = privatePattern.matcher(msg);
        if (privateMatcher.find()) {
            String endPrivateId = privateMatcher.group(1);
            privateChatFrame chatFrame = privateChatFrameMap.get(endPrivateId);
            JOptionPane.showMessageDialog(frame, "As the user ends the private chat，this window will close!", "Hint", JOptionPane.WARNING_MESSAGE);
            chatFrame.dispose();
            insertMessage(sysTextScrollPane, sysMsgArea, null, "[System Message] " + df.format(new Date()), "Beause[" + chatFrame.otherName + "]close the private chat window,private chat ends!", sysVertical, true);
            privateChatFrameMap.remove(endPrivateId);
        }
    }


    /**
     * @ClassName privateChatFrame
     * @Params * @param null
     * @Description 私聊窗口GUI的内部类
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

            //窗口关闭事件
            addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    int option = JOptionPane.showConfirmDialog(e.getOppositeWindow(), "Are you sure you want to end the private chat？", "Hint",
                            JOptionPane.YES_NO_OPTION);
                    if (option == JOptionPane.YES_OPTION) {
                        if (receive != null) {
                            sendMsg("privateExit", otherId); //关闭当前私聊连接
                        }
                        insertMessage(sysTextScrollPane, sysMsgArea, null, "[System Message] " + df.format(new Date()), "you and[" + otherName + "]'s private chat ends", sysVertical, true);
                        dispose();
                        privateChatFrameMap.remove(otherId);
                    } else {
                        return;
                    }
                }
            });

            //聊天信息输入框的监听回车按钮事件
            msgTestField.addKeyListener(new KeyAdapter() {
                @Override
                public void keyTyped(KeyEvent e) {
                    if (e.getKeyChar() == KeyEvent.VK_ENTER) {
                        {/*
                        if (receive == null) {
                            JOptionPane.showMessageDialog(frame, "请先连接聊天室的服务器!", "提示", JOptionPane.WARNING_MESSAGE);
                            return;
                        }*/}
                        String text = msgTestField.getText();
                        if (text != null && !text.equals("")) {
                            sendMsg("privateMsg", "<msg>" + text + "</msg><id>" + otherId + "</id>");
                            msgTestField.setText("");
                            insertMessage(textScrollPane, msgArea, null, df.format(new Date()) + " You said:", " " + text, vertical, false);
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
                        if (receive == null) {
                            JOptionPane.showMessageDialog(frame, "请先连接聊天室的服务器!", "提示", JOptionPane.WARNING_MESSAGE);
                            return;
                        }*/}
                        String text = msgTestField.getText();
                        if (text != null && !text.equals("")) {
                            sendMsg("privateMsg", "<msg>" + text + "</msg><id>" + otherId + "</id>");
                            msgTestField.setText("");
                            insertMessage(textScrollPane, msgArea, null, df.format(new Date()) + " You said:", " " + text, vertical, false);
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
     * @Params * @param null
     * @Description 根据操作系统自动变化GUI界面风格
     * @Return
     * @Since 2020/6/6
     */
    public static void setUIStyle() {
//       String lookAndFeel = UIManager.getSystemLookAndFeelClassName(); //设置当前系统风格
        String lookAndFeel = UIManager.getCrossPlatformLookAndFeelClassName(); //可跨系统
        try {
            UIManager.setLookAndFeel(lookAndFeel);
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
