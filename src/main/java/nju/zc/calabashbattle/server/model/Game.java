package nju.zc.calabashbattle.server.model;

import java.util.Set;

import nju.zc.calabashbattle.game.creature.*;
import nju.zc.calabashbattle.game.data.AbstractBulletData;
import nju.zc.calabashbattle.game.data.AbstractCreatureData;
import nju.zc.calabashbattle.game.data.Define;
import nju.zc.calabashbattle.game.message.BulletDeathMsg;
import nju.zc.calabashbattle.game.message.BulletSyncMsg;
import nju.zc.calabashbattle.game.message.CreatureCreateMsg;
import nju.zc.calabashbattle.game.message.CreatureDamageMsg;
import nju.zc.calabashbattle.game.message.GameOverMsg;
import nju.zc.calabashbattle.game.message.GameReadyMsg;
import nju.zc.calabashbattle.game.scene.BattleField;

public class Game {
    ServerThread good;
    ServerThread bad;
    BattleField field; 
    Server server;
    int game_id;

    boolean isRunning = false;

    public Game(Server s, int id, ServerThread st1, ServerThread st2){
        server = s;
        game_id = id;
        good = st1;
        bad = st2;
        field = new BattleField();
        isRunning = false;
    }

    public void resetGoodAndBad(ServerThread st1, ServerThread st2){
        good = st1;
        bad = st2;
    }

    /**
     * 对战场进行初始化
     */
    public void init(){
        synchronized(field){
            field.addCreature(new Calabash(0,   "大娃"  , 200,  20, 15, 60, 0, 5, 3.0));
            field.addCreature(new Calabash(1,   "二娃 " , 100,  35, 10, 50, 2, 6, 3.0));
            field.addCreature(new Calabash(2,   "三娃"  , 60,   50, 5,  50, 1, 7, 3.0));
            field.addCreature(new Calabash(3,   "爷爷"  , 40,   60, 20, 10, 0, 4, 3.0));
            field.addCreature(new Monster (100, "蝎子精" , 250,  20, 15, 25, 13, 4, 1.0));
            field.addCreature(new Monster (101, "蛇精" , 500,  50, 10, 10, 13, 5, 1.0));

        }
    }

    public void process(){
        for(AbstractCreatureData cData :field.getAllCreatureData()){
            CreatureCreateMsg msg = new CreatureCreateMsg(cData);
            good.sendMessage(msg);
            bad.sendMessage(msg);
        }
        countDown();
        isRunning = true;
        bulletMove();
        gameoverDetect();
    }

    private void countDown() {
        synchronized(field){
            int time = 3;
            try {
                while (time >= 0){
                    GameReadyMsg ReadyMsg = new GameReadyMsg(time);
                    Game.this.bad.sendMessage(ReadyMsg);
                    Game.this.good.sendMessage(ReadyMsg);
                    if (time > 0)print(String.format("Game starts in...%d", time));
                    else {
                        print("Game start!");
                        break;
                    }
                    time--;
                    Thread.sleep(1000);
                }
            }
            catch(Exception e){
                //e.printStackTrace();
            }
        }
    }

    public void gameoverDetect() {
        new Thread(new Runnable(){
            @Override
            public void run(){
                while(isRunning){
                try {
                    synchronized(field){
                        GameOverMsg GOMsg = null;
                        switch(field.getTeamState()){
                            case BATTLEING:
                                break;
                            case TEAM0_WINS:
                                GOMsg = new GameOverMsg(0, 0);
                                break;
                            case TEAM1_WINS:
                                GOMsg = new GameOverMsg(1, 0);
                                break;
                            case TIE:
                                GOMsg = new GameOverMsg(-1, -1);
                                break;
                            default:
                                break;
                        }
                        if(GOMsg != null){
                            isRunning = false;
                            Game.this.bad.sendMessage(GOMsg);
                            Game.this.good.sendMessage(GOMsg);
                        }
                    }
                    Thread.sleep(Define.LOGIC_FRAME_TIME);
                } catch (Exception e) {
                    //e.printStackTrace();
                }
            }
            }
        }).start();
    }
    public void bulletDetect(){
        synchronized(field){
            Set<AbstractBulletData> ABD = field.generateBulletUpdateSet();
            for(AbstractBulletData bData : ABD){
                int x = (int) Math.floor(bData.x / Define.BOX_X_LEN);
                int y = (int) Math.floor(bData.y / Define.BOX_Y_LEN);
                AbstractCreatureData cData = field.getCreatureData (x, y);
                if(x >= Define.FIELD_COLUMN || x < 0 || y >= Define.FIELD_ROW || y < 0){
                    BulletDeathMsg bDeathMsg = new BulletDeathMsg(bData.id);
                    field.deleteBullet(bData.id);
                    Game.this.bad.sendMessage(bDeathMsg);
                    Game.this.good.sendMessage(bDeathMsg);
                }
                else if(cData != null && bData.team != cData.team){
                    field.doDamage(cData.id, bData.damage);
                    BulletDeathMsg bDeathMsg = new BulletDeathMsg(bData.id);
                    field.deleteBullet(bData.id);
                    Game.this.bad.sendMessage(bDeathMsg);
                    Game.this.good.sendMessage(bDeathMsg);
                    CreatureDamageMsg cDMsg = new CreatureDamageMsg(cData.id, bData.attackerID, bData.damage, field.isCreatureDead(cData.id));
                    Game.this.bad.sendMessage(cDMsg);
                    Game.this.good.sendMessage(cDMsg);
                    if(field.isCreatureDead(cData.id)) field.deleteCreature(cData.id);
                }
            }
        }
    }

    public void bulletMove(){
        new Thread(new Runnable(){
            @Override
            public void run(){
                int i = 0;
                while(isRunning){
                    try {
                        synchronized(field){
                            field.updateBulletsPos();
                            bulletDetect();
                            if(i >= 50){
                                BulletSyncMsg bSyncMsg = new BulletSyncMsg(field.generateBulletUpdateSet());
                                Game.this.bad.sendMessage(bSyncMsg);
                                Game.this.good.sendMessage(bSyncMsg);
                                i = 0;
                            }
                            else i++;
                        }
                        Thread.sleep(Define.LOGIC_FRAME_TIME);
                    } catch (Exception e) {
                            //e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    
  
    public void print(String text){
        server.print("[Game " + game_id + "] "+text);
    }

    public void close(){
        isRunning = false;
    }
}