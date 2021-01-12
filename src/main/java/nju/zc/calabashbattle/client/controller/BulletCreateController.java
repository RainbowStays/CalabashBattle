package nju.zc.calabashbattle.client.controller;

import javafx.util.Pair;
import nju.zc.calabashbattle.game.data.AbstractCreatureData;
import nju.zc.calabashbattle.game.data.Define;
import nju.zc.calabashbattle.game.message.RequestBulletGenMessage;

public class BulletCreateController implements Runnable {
    private int cID;

    private BattleFieldController bController;

    BulletCreateController(int cID, BattleFieldController bController){
        this.cID = cID;
        this.bController = bController;
    }
       
    @Override
    public void run(){
        AbstractCreatureData cData = null;
        while(bController.isRunning()){
            try {
                synchronized(bController){
                    cData = bController.getCreatureData(cID);
                    if(cData == null)break;
                    Pair<Double, Double> cursorPos = bController.getCursorPos();
                    double pointGoalX;
                    double pointGoalY;
                    if(cursorPos.getKey() != null && cursorPos.getValue() != null){
                        pointGoalX = cursorPos.getKey();
                        pointGoalY = cursorPos.getValue();
                        
                    }
                    else{
                        pointGoalX = (cData.posX + (cData.team == 0 ? 1 : 0)) * Define.BOX_X_LEN + (cData.team == 0 ? 1 : -1);
                        pointGoalY = (cData.posY + 0.5) * Define.BOX_Y_LEN;
                    }
                    RequestBulletGenMessage RBSMsg = new RequestBulletGenMessage(cData.id, pointGoalX, pointGoalY);
                    bController.sendMessage(RBSMsg);
                }
                Thread.sleep(Math.round(80 + Define.SLOWEST_ATTACK_WAIT * (100.0 - cData.attackSpeed) / 100.0));
                
            } catch (Exception e) {
                //e.printStackTrace();
            }
        }
    }
}