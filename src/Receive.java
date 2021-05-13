


import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 使用多线程封装:接收端
 * 1、接收消息
 * 2、释放资源
 * 3、重写run
 *
 * @author Himit_ZH
 */
public class Receive implements Runnable {
    private DataInputStream dis;
    private Socket connect;
    private boolean isRunning;
    private chatClient client;

    public Receive(Socket connect, chatClient client) {
        this.connect = connect;
        this.isRunning = true;
        this.client = client;
        try {
            dis = new DataInputStream(connect.getInputStream());
        } catch (IOException e) {
            System.out.println("数据输入流初始化错误，请重启");
            release();
        }
    }

    //接收消息
    private String receive() {
        String msg = "";
        try {
            msg = dis.readUTF();
        } catch (IOException e) {
            release();
        }
        System.out.println("Chat" + msg);
        return msg;
    }

    public void setRunning(boolean running) {
        isRunning = running;
    }

    @Override
    public void run() {
        while (isRunning) {
            String msg = receive();
            if(!msg.equals("")&&msg!=null) {
                parseMessage(msg); //收到指令与消息的字符串，进行指令分配
            }
        }
    }


    /**
     * @MethodName parseMessage
     * @Params  * @param null
     * @Description 处理服务器发来的指令，将对应信息给予客户端线程相应的方法处理
     * @Return
     * @Since 2020/6/6
     */
    public void parseMessage(String message) {
        String code = null;
        String msg = null;
        System.out.println("afe" + message);
        /*
         * 先用正则表达式匹配响应code码和msg内容
         */
        if (message.length() > 0) {
            Pattern pattern = Pattern.compile("<cmd>(.*)</cmd>");
            Matcher matcher = pattern.matcher(message);
            if (matcher.find()) {
                code = matcher.group(1);
            }
            pattern = Pattern.compile("<msg>(.*)</msg>");
            matcher = pattern.matcher(message);
            if (matcher.find()) {
                msg = matcher.group(1);
            }
            switch (code) {
                // todo: receive msg
                case "1":   //更新世界聊天
                    client.updateTextArea(msg, "user");
                    break;
                case "2":   //更新系统消息
                    client.updateTextArea(msg, "sys");
                    break;
                case "3":   //与服务器断开消息 :可能为被踢，或者服务器关闭
                    client.showEscDialog(msg);
                    break;
                case "4":  //新增用户
                    client.addUser(msg);
                    break;
                case "5":  //删除用户 有其它用户退出 或者自己退出
                    client.delUser(msg);
                    break;

                case "7":  /*列出用户列表*/
                    client.getUserList(msg);
                    break;
                case "8": //设置用户id
                    client.setId(Integer.parseInt(msg));
                    break;
                case "9": //他人私聊于你，请求是否同意
                    client.askBuildPrivateChat(msg);
                    break;
                case "10": //你请求想与他人私聊的结果
                    client.startOrStopHisPrivateChat(msg);
                    break;
                case "11": //对方给你的私聊消息
                    client.giveMsgToPrivateChat(msg);
                    break;
                case "12": //对方结束了私聊
                    client.endPrivateChat(msg);
            }
        }
    }


    //释放资源
    private void release() {
        this.isRunning = false;
        CloseUtils.close(dis, connect);
    }

}
