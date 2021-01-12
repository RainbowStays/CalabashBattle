package nju.zc.calabashbattle.server.model;

import java.util.HashMap;
import java.util.Map;

enum State{Online, Offline};

public class Users {
    private Map<String, State> users;
    public Users(){
        users = new HashMap<>();
    }

    /**
     * 检查该用户是否为已经登录过的用户
     * @param username  用户名
     * @return          
     */
    public boolean check(String username){
        if(this.users.containsKey(username))return false;
        else {
            this.users.put(username, State.Offline);
            return true;
        }
    }

    /**
     * 设置用户的状态：线上或离线
     * @param user
     * @param s
     */
    public void setState(String user, boolean s){
        if(s==true)this.users.put(user, State.Online);
        else this.users.put(user, State.Offline);
    }

    /**
     * 判断用户是否在线上
     * @param user
     * @return
     */
    public boolean isOnline(String user){
        if(this.users.get(user)==State.Online)return true;
        else return false;
    }
    
}