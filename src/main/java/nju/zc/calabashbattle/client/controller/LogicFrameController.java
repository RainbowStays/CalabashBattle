package nju.zc.calabashbattle.client.controller;

import nju.zc.calabashbattle.game.data.Define;

class LogicFrameController implements Runnable {

    BattleFieldController bController;
    MoveController moveController;

    long currentTime;
    long lastUpdateTime;

    
    @Override
    public void run() {
        lastUpdateTime = 0;
        while (bController.isRunning()) {
            try {
                synchronized(bController){
                    currentTime = System.currentTimeMillis();
                    //按需刷新移动
                    if(!bController.isReplaying() && currentTime - moveController.getLastUpdateTime() >= Define.MOVE_BLANKING)moveController.moveAllSelected();
                    //按需刷新显示
                    if(currentTime - lastUpdateTime >= Define.DISPLAY_FRAME_TIME){
                        bController.updateDisplay();
                    }
                }
                Thread.sleep(Define.LOGIC_FRAME_TIME);
                
            } catch (InterruptedException e) {
                //e.printStackTrace();
            }
        }
    }

    public LogicFrameController(BattleFieldController bController, MoveController mController){
        moveController = mController;
        this.bController = bController;
    }


}

