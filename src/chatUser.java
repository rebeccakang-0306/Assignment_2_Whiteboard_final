

import java.net.Socket;

/**
 * @Author: Himit_ZH
 * @Date: 2020/6/4 23:25
 * @Description: 聊天室用户信息类
 */
public class chatUser {
    private String username;
    private Integer id;
    private Socket socket;

    public chatUser(String username, Integer id, Socket socket) {
        this.username = username;
        this.id = id;
        this.socket = socket;
    }


    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }
}