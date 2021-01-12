package nju.zc.calabashbattle.client.controller;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;

import javafx.application.Platform;

import nju.zc.calabashbattle.game.message.*;
import nju.zc.calabashbattle.client.ClientMain;

public class ClientSocketController implements Runnable {
    private Socket socket = null;
    ObjectInputStream in = null;
    ObjectOutputStream out = null;

    boolean loggedin = false;
    boolean running = false;

    String username;
    String serverIP;
    int port;
    int my_id;

    int myTeam;

    ClientMain mainApp;

    public ClientSocketController(ClientMain mainApp, String username, String serverIP, int port) {
        this.mainApp = mainApp;
        this.username = username;
        this.serverIP = serverIP;
        this.port = port;
        loggedin = false;
        running = false;
    }

    @Override
    public void run() {
        try {
            connect();
            out = new ObjectOutputStream(socket.getOutputStream());
            sendMsg(new LoginMessage(username));
            in = new ObjectInputStream(socket.getInputStream());

            Platform.runLater(()->
                mainApp.hallController.print("Connected to Server. Waiting for logging in...")
            );

            running = true;
            

        } catch (IOException e1) {
            //e1.printStackTrace();
            Platform.runLater(()->
                mainApp.hallController.loginFailed("Unable to reach Server")
            );
        }

        while(running){
            try{
                //System.out.println("Waiting Message...");
                AbstractMessage msg = (AbstractMessage)in.readObject();
                
                //System.out.println("Get Message");
                if(handleMessage(msg) == -1){
                    close();
                    break;
                }
            }
            catch(Exception e){
                Platform.runLater(()->
                    mainApp.hallController.loginFailed("Disconnected from the Server")
                );
                break;
            }

        }
        //System.out.println("Thread Exit");
    }

    private int handleMessage(AbstractMessage msg) throws Exception {
        switch(msg.getType()){
            case MSG_LOGINREPLY:
                if(loggedin) {
                    //do something
                    break;
                }
                LoginReplyMessage msg_LoginReplyMessage = (LoginReplyMessage)msg;
                switch(msg_LoginReplyMessage.getReplyType()){
                    case TRUE:
                        loggedin = true;
                        my_id = msg_LoginReplyMessage.getRunID();
                        Platform.runLater(()->
                            mainApp.hallController.print("Logged in Successfully as ID: " + my_id + ". Waiting for match...")
                        );
                        
                        break;
                    case wrongUsername:
                        Platform.runLater(()->
                            mainApp.hallController.loginFailed("Wrong Username")
                        );
                        return -1;
                }
                break;


            case MSG_MATCH:
                MatchMessage msg_MatchMessage = (MatchMessage)msg;
                myTeam = msg_MatchMessage.getCamp();
                String side = (msg_MatchMessage.getCamp() == 0) ? ("Calabash") : ("Monster");
                Platform.runLater(()->{
                    mainApp.hallController.setButtonDisable(true);
                    mainApp.hallController.print("Matched successfully.\nYour Opponent is " + msg_MatchMessage.getUser() + ".\nYou are " + side + " side. Game will start in 3 seconds...");
                });
                break;


            case MSG_GAME_INFO:
                GameInfoMessage msgGameInfo = (GameInfoMessage)msg;
                msgGameInfo.setServerIP(serverIP);
                Platform.runLater(()->
                    mainApp.performFieldCreate(msgGameInfo)
                );
                break;

            case MSG_BULLET_DEATH:
            case MSG_BULLET_GEN:
            case MSG_BULLET_SYNC:
            case MSG_CREATURE_CREATE:
            case MSG_CREATURE_DAMAGE:
            case MSG_CREATURE_MOVE:
            case MSG_GAMEOVER:
            case MSG_GAMEREADY:
            case MSG_CREATURE_DIRECTION_CHANGED:
            
            
                Platform.runLater(()->
                    mainApp.sendFieldControllerMessage(msg)
                );
                break;

            default:
                break;
        }
        return 1;
    }

    public void connect() throws IOException {
        socket = new Socket(serverIP, port);
    }
    

    public void sendMsg(AbstractMessage message){
        synchronized(this.out){
            try{
                out.reset();
                out.writeObject(message);
                out.flush();
            }catch(Exception e){
                //e.printStackTrace();
            }
        }
    }

    public void close(){
        running = false;
        try {
            if(this.in != null)this.in.close();
            if(this.out != null)this.out.close();
            if(this.socket != null)this.socket.close();
        } catch (Exception e) {
            //e.printStackTrace();
        }
        
    }
}