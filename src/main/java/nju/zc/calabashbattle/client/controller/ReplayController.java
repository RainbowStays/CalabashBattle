package nju.zc.calabashbattle.client.controller;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import javafx.application.Platform;
import nju.zc.calabashbattle.game.message.AbstractMessage;

public class ReplayController implements Runnable {

    BattleFieldController bController;
    List<AbstractMessage> replayMessage;
    AbstractMessage msg;

    boolean isProcessed = false;

    @Override
    public void run() {
        Iterator<AbstractMessage> itor = replayMessage.iterator();
        if (itor.hasNext()) {
            msg = itor.next();
            while (bController.isReplaying() && msg != null) {
                Platform.runLater(()->
                    bController.getMessage(msg, true)
                );
                long currentTime = msg.getTimeCreate();
                if (itor.hasNext()) {
                    AbstractMessage k = itor.next();
                    try {
                        long time = k.getTimeCreate() - currentTime;
                        if(time <= 0)time = 1;
                        Thread.sleep(time);
                        while(!isProcessed)Thread.sleep(1);
                        isProcessed = false;
                        msg = k;
                    } catch (InterruptedException e) {
                        break;
                    }
                }
                else{
                    try{
                        while(!isProcessed)Thread.sleep(1);
                    }catch(Exception e){
                    }
                    
                    break;
                }
            }
        }
    }

    public ReplayController(BattleFieldController bController, List<AbstractMessage> messages){
        this.bController = bController;
        Collections.sort(messages, new Comparator<AbstractMessage>(){

            @Override
            public int compare(AbstractMessage o1, AbstractMessage o2) {
                return (int)(o1.getTimeCreate() - o2.getTimeCreate());
            }
            
        });
        this.replayMessage = messages;
    }

    public void processFinished(){
        isProcessed = true;
    }
    
}