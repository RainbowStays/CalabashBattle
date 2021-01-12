package nju.zc.calabashbattle.server.model;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import nju.zc.calabashbattle.game.data.*;
import nju.zc.calabashbattle.game.message.*;
import nju.zc.calabashbattle.server.controller.LogController;
import javafx.scene.control.TextArea;

public class Server {
    ServerSocket serversocket = null;
    Users users;
    boolean isRunning = true;

    private int client_id = 1000;
    Map<Integer, ServerThread> threads = null;
    Set<ServerThread> in_match_thread = null;
    LogController logcontroller = null;
    TextArea textArea = null;

    private int game_id = 0;

    // Game game = null;
    List<Game> games = null;

    /**
     * 采用可视化日志控制器来初始化服务器
     * 
     * @param lc 图形界面控制器
     */
    public Server(LogController lc) {
        users = new Users();
        threads = new HashMap<>();
        in_match_thread = new HashSet<>();
        games = new ArrayList<Game>();
        logcontroller = lc;
    }

    /**
     * 启动服务器
     */
    public void start() {
        new Thread(new ServerStart()).start();
    }

    /**
     * 启动的服务器线程
     */
    class ServerStart implements Runnable {
        @Override
        public void run() {
            try {
                Server.this.serversocket = new ServerSocket(Define.PORT);
                Server.this.isRunning = true;
                while (Server.this.isRunning) {
                    Socket s = serversocket.accept();
                    print(s.getRemoteSocketAddress() + " connected to the server, allocate id: " + client_id);
                    ServerThread st = new ServerThread(Server.this, s, client_id);
                    threads.put(client_id++, st);
                    new Thread(st).start();
                }
            } catch (Exception e) {
            }
        }
    }

    /**
     * 每当一个客户端连接上服务器时，服务器 将其加入到匹配队列中，当匹配成功后，随机 为两方分配阵营并初始化游戏
     * 
     * @param st0 与客户端通信的服务器线程
     */
    public void match(ServerThread st) {
        synchronized (in_match_thread) {
            if (in_match_thread.size() == 0)
                in_match_thread.add(st);
            else {
                Iterator<ServerThread> itor = in_match_thread.iterator();
                ServerThread st1 = itor.next();
                if (st1.equals(st))
                    return;
                Random r = new Random();
                int camp = r.nextInt(2);
                ServerThread st0 = st;
                MatchMessage matchMsg1 = new MatchMessage(st0.Clientname, camp);
                MatchMessage matchMsg2 = new MatchMessage(st1.Clientname, 1 - camp);
                st0.sendMessage(matchMsg2);
                this.print("Sent message to client " + st0.id + ": allocate camp " + camp);
                st1.sendMessage(matchMsg1);
                this.print("Sent message to client " + st1.id + ": allocate camp " + (1 - camp));
                in_match_thread.clear();

                Game newGame = new Game(this, game_id, st0, st1);
                if (camp == 1) {
                    newGame.resetGoodAndBad(st1, st0);
                }
                newGame.init();
                games.add(newGame);
                Thread ts = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(3000);
                            GameInfoMessage infoMsg0;
                            GameInfoMessage infoMsg1;
                            if(camp == 0){
                                infoMsg0 = new GameInfoMessage(game_id, camp, st0.Clientname,
                                    st1.Clientname);
                                infoMsg1 = new GameInfoMessage(game_id++, 1 - camp, st0.Clientname,
                                    st1.Clientname);
                            }
                            else{
                                infoMsg0 = new GameInfoMessage(game_id, camp, st1.Clientname,
                                    st0.Clientname);
                                infoMsg1 = new GameInfoMessage(game_id++, 1 - camp, st1.Clientname,
                                    st0.Clientname);
                            }
                            
                            st0.sendMessage(infoMsg0);
                            st1.sendMessage(infoMsg1);
                            st0.addGame(newGame);
                            st1.addGame(newGame);
                            newGame.process();
                        } catch (Exception e) {
                            //e.printStackTrace();
                        }
                    }
                });
                ts.start();

            }
        }
    }

    public void disconnect(ServerThread t) {
        in_match_thread.remove(t);
        ServerThread t1 = null;
        if(t.game != null){
            GameOverMsg msg;
            if(t.game.good == t){
                msg = new GameOverMsg(1, -2);
                t1 = t.game.bad; 
            }
            else{
                msg = new GameOverMsg(0, -2);
                t1 = t.game.good;
            }
            if(t.isRunning)t.sendMessage(msg);
            if(t1.isRunning)t1.sendMessage(msg);
            in_match_thread.remove(t1);
            t.game.close();
            t1.release();
        }
        t.release();
    }

    /**
     * 添加日志
     * 
     * @param o
     */
    public void print(Object o) {
        this.logcontroller.print(o);
    }

    public void close() {
        for (Game g : games){
            g.close();
            g.good.release();
            g.bad.release();
        }
        for (ServerThread t : in_match_thread)
            disconnect(t);
        try {
            serversocket.close();
        } catch (IOException e) {
        }
        isRunning = false;
	}
    
}