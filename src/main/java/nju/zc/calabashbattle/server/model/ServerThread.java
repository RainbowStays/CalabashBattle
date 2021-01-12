package nju.zc.calabashbattle.server.model;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import nju.zc.calabashbattle.game.data.AbstractBulletData;
import nju.zc.calabashbattle.game.data.Define;
import nju.zc.calabashbattle.game.message.*;
import nju.zc.calabashbattle.game.message.LoginReplyMessage.loginReplyType;



class ServerThread implements Runnable{
    Socket socket;
    ObjectInputStream in = null;
    ObjectOutputStream out = null;
    int id;
    Server server = null;
    String Clientname = null;
    Game game;
    boolean isRunning = false;

    /**
     * 与客户端通信的线程
     * @param serv    主服务器
     * @param s       服务器监听到的socket
     * @param i       服务器为客户端分配的id，用于标识客户端
     */
    ServerThread(Server serv, Socket s,int i){
        this.server = serv;
        this.socket = s;
        this.id = i;
        try{
            this.in = new ObjectInputStream(this.socket.getInputStream());
            this.out = new ObjectOutputStream(this.socket.getOutputStream());
        }catch(Exception e){
            //e.printStackTrace();
        }
    }
    
    /**
     * 标志该客户端参与的游戏对局
     * @param g
     */
    public void addGame(Game  g){
        this.game = g;
    }

    /**
     * 对客户端发来的信息进行处理
     * @param message
     */
    public void handleMsg(AbstractMessage message){
        switch(message.getType()){
            case MSG_LOGIN:handleLoginMsg((LoginMessage)message);break;
            case MSG_CREATURE_MOVE:handleCreatureMoveMsg((CreatureMoveMsg)message);break;
            case MSG_REQ_BULLET_GEN:handleBulletCreateMsg((RequestBulletGenMessage)message);break;
            case MSG_CREATURE_DIRECTION_CHANGED:handleCreatureDirectionChanged((CreatureDirectionChanged)message);break;
            case MSG_GAME_EXIT:server.disconnect(this);
            default:break;
        }
            
        
    }
    
    private void handleCreatureDirectionChanged(CreatureDirectionChanged message) {
        synchronized(game.field){
            game.field.changeCreatureDirection(message.getID(), message.getNewDirection());
            game.bad.sendMessage(message);
            game.good.sendMessage(message);
        }
    }

    /**
     * 处理客户端 创建子弹 的操作
     * 
     * @param message
     */
    private void handleBulletCreateMsg(RequestBulletGenMessage message) {
        synchronized(game.field){
            
            AbstractBulletData bData = game.field.generateAndAddCreatureBullet(message.getCreatureID(), message.getPointGoalX(), message.getPointGoalY());
            BulletGenMessage BSMsg = new BulletGenMessage(bData);
            game.bad.sendMessage(BSMsg);
            game.good.sendMessage(BSMsg);
        }
    }

    /**
     * 处理客户端 对角色进行移动 的操作
     * 
     * @param message
     */
    public void handleCreatureMoveMsg(CreatureMoveMsg message){
        int id = message.getID();
        int x = message.getNewX();
        int y = message.getNewY();
        if(x >= 0 && x < Define.FIELD_COLUMN && y >= 0 && y < Define.FIELD_COLUMN && game.field.getCreatureData(x, y) == null){
            game.field.moveCreatureTo(id, x, y);
            game.bad.sendMessage(message);
            game.good.sendMessage(message);
        }
        
         
    }

    /**
     * 处理客户端的登录
     * @param message
     */
    public void handleLoginMsg(LoginMessage message){
        String username = message.getUsername();
        if(server.users.check(username) == false){
            server.print("User: "+username+" login successfully");
            server.users.setState(username, true);
            LoginReplyMessage msg = new LoginReplyMessage(loginReplyType.TRUE, id);
            sendMessage(msg);
            Clientname = username;
            server.match(this);
        }
        else{
            if(server.users.isOnline(username) == false){
                server.print("User: "+username+" login successfully");
                server.users.setState(username, true);
                LoginReplyMessage msg = new LoginReplyMessage(loginReplyType.TRUE, id);
                sendMessage(msg);
                Clientname = username;
                server.match(this);
            }
            else {
                LoginReplyMessage msg = new LoginReplyMessage(loginReplyType.wrongUsername, -1);
                sendMessage(msg);
            }
        }
    }




    /**
     * 一直监听客户端发来的消息并对之进行处理
     */
    public void run(){
        try{
            isRunning = true;
            while(isRunning){
                AbstractMessage message = (AbstractMessage)this.in.readObject();
                this.handleMsg(message);
            }
        }catch(Exception e){
            isRunning = false;
            server.print(this.Clientname+" disconnected from the server.");
            //e.printStackTrace();
            server.disconnect(this);
        }
    }

    /**
     * 向客户端发送消息
     * @param msg
     */
    public void sendMessage(AbstractMessage msg){
        synchronized(this.out){
            try{
                if(!isRunning)return;
                this.out.writeObject(msg);
                //Define.println(this.id+"sent message:"+msg);
            }catch(Exception e){
                //e.printStackTrace();
            }
        }
    }

    /**
     * 服务器关闭，释放资源
     */
    public void release(){
        try{
            isRunning = false;
            if(this.in != null)this.in.close();
            if(this.out != null)this.out.close();
            if(this.socket != null)this.socket.close();
        }catch(Exception e){
            //e.printStackTrace();
        }
    }

}