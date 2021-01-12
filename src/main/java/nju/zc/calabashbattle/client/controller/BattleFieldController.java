package nju.zc.calabashbattle.client.controller;

import nju.zc.calabashbattle.game.data.AbstractBulletData;
import nju.zc.calabashbattle.game.data.AbstractCreatureData;
import nju.zc.calabashbattle.game.data.Define;
import nju.zc.calabashbattle.game.message.AbstractMessage;
import nju.zc.calabashbattle.game.message.BulletDeathMsg;
import nju.zc.calabashbattle.game.message.BulletGenMessage;
import nju.zc.calabashbattle.game.message.BulletSyncMsg;
import nju.zc.calabashbattle.game.message.CreatureCreateMsg;
import nju.zc.calabashbattle.game.message.CreatureDamageMsg;
import nju.zc.calabashbattle.game.message.CreatureDirectionChanged;
import nju.zc.calabashbattle.game.message.CreatureMoveMsg;
import nju.zc.calabashbattle.game.message.GameExitMessage;
import nju.zc.calabashbattle.game.message.GameInfoMessage;
import nju.zc.calabashbattle.game.message.GameOverMsg;
import nju.zc.calabashbattle.game.message.GameReadyMsg;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.util.Pair;
import nju.zc.calabashbattle.client.ClientMain;
import nju.zc.calabashbattle.client.ClientMain.stateType;
import nju.zc.calabashbattle.game.scene.BattleField;
import nju.zc.calabashbattle.client.view.BulletDisplay;
import nju.zc.calabashbattle.client.view.CreatureDisplay;

public class BattleFieldController {
    ClientMain mainApp;
    Stage stage;

    int myTeam;

    @FXML
    private AnchorPane fieldPane;

    @FXML
    private AnchorPane stagePane;

    @FXML
    private TextArea textDisplay;

    @FXML
    private Button replayButton;

    @FXML
    private Button exitButton;

    @FXML
    private Label serverLabel;
    
    @FXML
    private Label roomLabel;

    @FXML
    private Label calabashLabel;

    @FXML
    private Label monsterLabel;

    @FXML
    private HBox infoHBox;

    @FXML
    private AnchorPane infoPane;

    @FXML
    private Label infoName; 

    @FXML
    private Label infoHp; 

    @FXML
    private ProgressBar infoHpBar; 

    @FXML
    private Label infoTeam; 

    @FXML
    private Label infoAttack;

    @FXML
    private Label infoDefend;

    @FXML
    private Label infoAttackSpeed;

    @FXML
    private ImageView infoImageView;

    private int infoDisplayID = -1;


    private BattleField battleField;

    private boolean multiChoose = false;

    private Map<Integer, CreatureDisplay> creatureDisplayMap;
    private Map<Integer, BulletDisplay> bulletDisplayMap;

    private MoveController moveController;
    private LogicFrameController logicFrameController;
    private ReplayController replayController;

    private List<AbstractMessage> messageStore;

    private Double cursorX;
    private Double cursorY;

    private long replayRunningTime;

    private enum field_state {
        GAME_INITING, 
        GAME_READYING, 
        GAME_PROCESSING, 
        GAME_FINISHED, 
        REPLAY_READYING,
        REPLAY_PLAYING, 
        REPLAY_FINISHED,
        EXIT
    }

    private field_state currentState = field_state.EXIT;

    @FXML
    public void initialize() {
        // 为全局Pane增加键盘按下事件
        stagePane.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                switch (event.getCode()) {
                    case CONTROL:
                        multiChoose = true;
                        break;
                    case W:
                        moveController.setDirectionPressed(0, true);
                        break;
                    case A:
                        moveController.setDirectionPressed(1, true);
                        break;
                    case S:
                        moveController.setDirectionPressed(2, true);
                        break;
                    case D:
                        moveController.setDirectionPressed(3, true);
                        break;
                    default:
                        break;
                }
            }
        });

        // 为全局Pane增加键盘松开事件
        stagePane.setOnKeyReleased(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                switch (event.getCode()) {
                    case CONTROL:
                        multiChoose = false;
                        break;
                    case W:
                        moveController.setDirectionPressed(0, false);
                        break;
                    case A:
                        moveController.setDirectionPressed(1, false);
                        break;
                    case S:
                        moveController.setDirectionPressed(2, false);
                        break;
                    case D:
                        moveController.setDirectionPressed(3, false);
                        break;
                    case L:
                        if (currentState == field_state.GAME_FINISHED || currentState == field_state.REPLAY_FINISHED || currentState == field_state.EXIT)
                            loadReplay();
                        break;
                    default:
                        break;
                }
            }
        });

        stagePane.setOnMouseMoved(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                cursorX = event.getX() - fieldPane.getLayoutX();
                cursorY = event.getY() - fieldPane.getLayoutY();
            }
        });

        hideInfoDisplay();
    }

    @FXML
    public void onExitButtonClicked(){
        if(currentState == field_state.GAME_PROCESSING){
            Alert alert = new Alert(AlertType.WARNING);
            alert.setTitle("退出战局");
            alert.setHeaderText(null);
            alert.setContentText("确定要退出战局吗？您将会被判负");

            ButtonType buttonConfirm = new ButtonType("确定");
            ButtonType buttonCancel = new ButtonType("取消", ButtonData.CANCEL_CLOSE);

            alert.getButtonTypes().setAll(buttonConfirm, buttonCancel);

            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == buttonConfirm){
                // ... user chose "确定"
                GameExitMessage msg = new GameExitMessage();
                sendMessage(msg);
                
            }else {
                // ... user chose CANCEL or closed the dialog
                //Don't do anything
            }
        }
        else if(currentState == field_state.REPLAY_PLAYING){
            print("已中止回放！");
            currentState = field_state.REPLAY_FINISHED;
            exitButton.setText("退出游戏");
            replayButton.setDisable(false);
        }
        else{
            //退出游戏
            clear();
            currentState = field_state.EXIT;
            Platform.exit();
        }
    }

    public void clear(){
        synchronized(this){
            currentState = field_state.EXIT;
            if(creatureDisplayMap != null){
                for(CreatureDisplay i: creatureDisplayMap.values()){
                    i.remove();
                }
            }
            if(bulletDisplayMap != null){
                for(BulletDisplay i: bulletDisplayMap.values()){
                    i.remove();
                }
            }
            exitButton.setDisable(true);
            hideInfoDisplay();

        }
    }

    public void init(int team) {
        clear();
        exitButton.setText("退出战局");
        currentState = field_state.GAME_INITING;
        creatureDisplayMap = new HashMap<Integer, CreatureDisplay>();
        bulletDisplayMap = new HashMap<Integer, BulletDisplay>();
        moveController = new MoveController(this);
        battleField = new BattleField();
        logicFrameController = new LogicFrameController(this, moveController);
        replayController = null;

        myTeam = team;

        messageStore = new ArrayList<AbstractMessage>();

        replayButton.setDisable(true);

        String st = "葫芦娃队";
        if (team == 1)
            st = "妖精队";
        print(String.format("你的队伍是：%s", st));
        print("游戏即将开始，请做好准备...");
    }

    public void replayInit(String fileName, List<AbstractMessage> replayMessage){
        clear();
        exitButton.setText("退出回放");
        creatureDisplayMap = new HashMap<Integer, CreatureDisplay>();
        bulletDisplayMap = new HashMap<Integer, BulletDisplay>();
        moveController = null;
        battleField = new BattleField();
        logicFrameController = new LogicFrameController(this, null);
        replayController = new ReplayController(this, replayMessage);

        myTeam = -1;

        messageStore = new ArrayList<AbstractMessage>();

        replayButton.setDisable(true);
        
        print("开始回放\"" + fileName + "\"...");

        currentState = field_state.REPLAY_READYING;
        replayRunningTime = System.currentTimeMillis();
        
        Thread t = new Thread(replayController);
        t.start();
    }

    @SuppressWarnings("unchecked")
    @FXML
    public void loadReplay() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("选择回放文件");
        String nameTemp = "replays" + File.separator;
        File dir = new File(nameTemp);
        try {
            if (!dir.exists())
                dir.mkdirs();
        } catch (Exception e) {
            //e.printStackTrace();
        }
        fileChooser.setInitialDirectory(dir);
        fileChooser.getExtensionFilters().addAll(new ExtensionFilter("Battle Replay Files", "*.cmbreplay"),
                new ExtensionFilter("All Files", "*.*"));
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            ObjectInputStream in;
            String errorText = "";
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("文件读取出错");
            alert.setHeaderText(null);
            try {
                in = new ObjectInputStream(new FileInputStream(file));
                List<AbstractMessage> temp = (List<AbstractMessage>)in.readObject();
                replayInit(file.getAbsolutePath(), temp);
                in.close();
            } catch (FileNotFoundException e) {
                errorText = "找不到文件 \"" + file.getAbsolutePath() + "\" ！";
                alert.setContentText(errorText);
                alert.showAndWait();
            } catch (IOException e) {
                errorText = "读取文件 \"" + file.getAbsolutePath() + "\" 时出错！";
                alert.setContentText(errorText);
                alert.showAndWait();
            } catch (ClassNotFoundException e) {
                errorText = "文件 \"" + file.getAbsolutePath() + "\" 不是一个战斗回放文件！";
                alert.setContentText(errorText);
                alert.showAndWait();
            } catch (ClassCastException e){
                errorText = "文件 \"" + file.getAbsolutePath() + "\" 不是一个战斗回放文件！";
                alert.setContentText(errorText);
                alert.showAndWait();
            } catch (Exception e){
                errorText = "文件 \"" + file.getAbsolutePath() + "\" 不是一个战斗回放文件，或可能已损坏！";
                alert.setContentText(errorText);
                alert.showAndWait();
            }
            
            
        }
    }

    public void countDown(int time) {
        switch (time) {
            case 2:
                print("Ready...");
                break;
            case 1:
                print("Set...");
                break;
            case 0:
                print("Go!");
                break;
            default:
                break;
        }
    }

    public void start(boolean isReplaying) {
        synchronized (this) {
            if (!isReplaying){
                currentState = field_state.GAME_PROCESSING;
                for(AbstractCreatureData cData : battleField.getAllCreatureData()){
                    if(currentState == field_state.GAME_PROCESSING && cData.team == myTeam)
                    {
                        BulletCreateController bulletController = new BulletCreateController(cData.id, this);
                        Thread t1 = new Thread(bulletController);
                        t1.start();
                    }
                }
            }
            else currentState = field_state.REPLAY_PLAYING;
            exitButton.setDisable(false);
            Thread t = new Thread(logicFrameController);
            t.start();
        }
    }

    public void addCreature(AbstractCreatureData cData) {
        synchronized (this) {
            battleField.addCreature(cData.generateCreature());
            //传入的myTeam供Display根据你的队伍将友方生物和敌方生物区分显示
            creatureDisplayMap.put(cData.id, new CreatureDisplay(this, cData, fieldPane, myTeam));
            //System.out.println("new creature");
            if(currentState == field_state.GAME_PROCESSING && cData.team == myTeam)
            {
                BulletCreateController bulletController = new BulletCreateController(cData.id, this);
                Thread t1 = new Thread(bulletController);
                t1.start();
            }
            ensureOneSelectionIfPossible();
        }
    }

    public void addBullet(AbstractBulletData bData) {
        synchronized (this) {
            battleField.addBullet(bData.generateBullet());
            //传入的myTeam供Display根据你的队伍将友方子弹和敌方子弹区分显示
            bulletDisplayMap.put(bData.id, new BulletDisplay(bData, fieldPane, myTeam));
        }
    }

    public void changeCreatureDirection(int cID, double newDirection){
        synchronized (this) {
            battleField.changeCreatureDirection(cID, newDirection);
        }
    }

    public void updateDisplay() {
        synchronized (this) {
            Platform.runLater(()->
                updateInfoDisplay(infoDisplayID)
            );
            if(currentState == field_state.GAME_PROCESSING){
                for(AbstractCreatureData cData: battleField.getAllCreatureData(myTeam)){
                    if(getCursorPos().getKey() == null)break;
                    double d = (getCursorPos().getKey() >= (cData.posX + 0.5) * Define.BOX_X_LEN) ? 3.0 : 1.0;
                    if(cData.direction != d){
                        changeCreatureDirection(cData.id, d);
                        CreatureDirectionChanged msg = new CreatureDirectionChanged(cData.id, cData.team, d);
                        sendMessage(msg);
                    }
                }
            }
            for (Entry<Integer, CreatureDisplay> i : creatureDisplayMap.entrySet()) {
                i.getValue().update(battleField.getCreatureData(i.getKey()));
            }
            for (Entry<Integer, BulletDisplay> i : bulletDisplayMap.entrySet()) {
                i.getValue().update(battleField.getBulletData(i.getKey()));
            }
        }
    }

    public void updateBulletFromServer(Set<AbstractBulletData> set) {
        synchronized (this) {
            //System.out.println(set.size());
            for (AbstractBulletData i : set) {
                battleField.syncBullet(i);
            }
        }
    }

    public void doDamage(int cID, int aID, int damage) {
        synchronized (this) {
            if (currentState != field_state.GAME_PROCESSING && currentState != field_state.REPLAY_PLAYING)
                return;
            int damageReal = battleField.doDamage(cID, damage);
            AbstractCreatureData cData = battleField.getCreatureData(cID);
            AbstractCreatureData aData = battleField.getCreatureData(aID);
            if (aData == null) aData = battleField.getDeadCreatureDate(aID);
            if (damageReal > 0) {
                print(String.format("%s 受到了来自 %s 的 %d 点伤害！", cData.name, aData.name, damageReal));
            }
        }
    }

    public void setSelection(int cID) {
        synchronized (this) {
            Platform.runLater(()->
                updateInfoDisplay(cID)
            );
            if (currentState != field_state.GAME_PROCESSING)
                return;
            if (battleField.getCreatureData(cID).team != myTeam) {
                // 选择了非本队成员
                //print(String.format("你不能操纵敌方人物 %s ！", cData.name));
                return;
            }
            if (multiChoose) {
                if(creatureDisplayMap.get(cID).isSelected()){
                    moveController.removeSelection(cID);
                    creatureDisplayMap.get(cID).setSelected(false);
                }
                else{
                    moveController.addSelected(cID);
                    creatureDisplayMap.get(cID).setSelected(true);
                }
            } else {
                Set<Integer> st = moveController.getSelectedSet();
                for(int i : st){
                    creatureDisplayMap.get(i).setSelected(false);
                }
                moveController.removeAllSelection();
                creatureDisplayMap.get(cID).setSelected(true);
                moveController.setSelected(cID);
            }
        }
    }

    private void updateInfoDisplay(int cID){
        synchronized (this) {
            if(cID == -1 || battleField == null || creatureDisplayMap == null)
            {   
                hideInfoDisplay();
                return;
            }
            CreatureDisplay a = creatureDisplayMap.get(cID);
            AbstractCreatureData cData = battleField.getCreatureData(cID);
            if(a == null || cData == null || a.getImageViewBig() == null){
                hideInfoDisplay();
                return;
            }
            infoImageView.setImage(a.getImageViewBig().getImage());
            infoName.setText(cData.name);
            infoHp.setText(cData.hp + "/" + cData.hpMax);
            infoHpBar.setProgress((double)cData.hp / cData.hpMax);
            if(cData.team == myTeam)infoHpBar.setStyle("-fx-accent: green;");
            else infoHpBar.setStyle("-fx-accent: red;");
            if (cData.team == 0)infoTeam.setText("葫芦娃阵营");
            else infoTeam.setText("妖精阵营");
            infoAttack.setText("" + cData.attack);
            infoDefend.setText("" + cData.defend);
            infoAttackSpeed.setText("" + cData.attackSpeed);
            infoDisplayID = cData.id;
            if(!infoPane.getChildren().contains(infoHBox))infoPane.getChildren().add(infoHBox);
        }
    }

    private void hideInfoDisplay(){
        synchronized(this){
            if(infoPane.getChildren().contains(infoHBox))
            Platform.runLater(()->
                infoPane.getChildren().remove(infoHBox)
            );
        }
    }

    public void removeSelection(int cID) {
        synchronized (this) {
            if (currentState != field_state.GAME_PROCESSING)
                return;
            if (battleField.getCreatureData(cID).team != myTeam) {
                return;
            }
            if(moveController.removeSelection(cID)){
                CreatureDisplay cD = creatureDisplayMap.get(cID);
                if(cD != null)cD.setSelected(false);}
            
        }
    }

    private void ensureOneSelectionIfPossible(){
        synchronized (this) {
            if (currentState != field_state.GAME_PROCESSING && currentState != field_state.GAME_READYING && currentState != field_state.GAME_INITING)
                return;
            if (moveController.getSelectedSet().size() > 0)return;
            Set<AbstractCreatureData> st = battleField.getAllCreatureData(myTeam);
            if (st.size() == 0)return;
            for(AbstractCreatureData i : st){
                creatureDisplayMap.get(i.id).setSelected(true);
                moveController.setSelected(i.id);
                Platform.runLater(()->
                    updateInfoDisplay(i.id)
                );
                break;
            }
        }

    }

    /**
     * 将指定生物ID的生物移动到格子(x, y)
     * <P/>
     * 注意坐标原点在左上角，x轴正方向向右，y正方向向下
     * 
     * @param cID 生物ID
     * @param x   要移动到的格子的坐标x
     * @param y   要移动到的格子的左边y
     */
    public void moveCreatureTo(int cID, int x, int y) {
        synchronized (this) {
            if (currentState != field_state.GAME_PROCESSING)
                return;
            if (battleField.isCreatureDead(cID))return;
            CreatureMoveMsg msg = new CreatureMoveMsg(cID, x, y);
            sendMessage(msg);
        }
        // 等待服务器确认
    }

    void sendMessage(AbstractMessage msg) {
        Platform.runLater(() -> mainApp.sendMessage(msg));
    }

    /**
     * 将指定生物ID的格子坐标增加(deltaX, deltaY)
     * <P/>
     * 注意坐标原点在左上角，x轴正方向向右，y正方向向下
     * 
     * @param cID
     * @param deltaX
     * @param deltaY
     */
    public void moveCreature(int cID, int deltaX, int deltaY) {
        synchronized (this) {
            if (currentState != field_state.GAME_PROCESSING)
                return;
            if (battleField.isCreatureDead(cID))return;
            AbstractCreatureData cData = battleField.getCreatureData(cID);
            CreatureMoveMsg msg = new CreatureMoveMsg(cID, cData.posX + deltaX, cData.posY + deltaY);
            sendMessage(msg);
        }
        // 等待服务器确认
    }

    public void syncCreatureMove(int cID, int x, int y) {
        synchronized (this) {
            battleField.moveCreatureTo(cID, x, y);
        }
    }

    /**
     * 获得指定cID生物的Data类型
     * 
     * @param cID 生物的ID
     * @return AbstractCreatureData
     */
    public AbstractCreatureData getCreatureData(int cID) {
        synchronized (this) {
            return battleField.getCreatureData(cID);
        }
    }

    /**
     * 获得指定bID子弹的Data类型
     * 
     * @param bID 子弹的ID
     * @return AbstractCreatureData
     */
    public AbstractCreatureData getBulletData(int bID) {
        synchronized (this) {
            return battleField.getCreatureData(bID);
        }
    }

    public void deleteCreature(int cID, int aID) {
        synchronized (this) {
            CreatureDisplay cD = creatureDisplayMap.remove(cID);
            if (cD != null) {
                if(cD.isSelected())removeSelection(cID);
                cD.remove();
                AbstractCreatureData cData = battleField.deleteCreature(cID);
                AbstractCreatureData aData = battleField.getCreatureData(aID);
                if(aData == null) aData = battleField.getDeadCreatureDate(aID);
                if(aData != null) print(String.format("%s 被 %s 击杀！", cData.name, aData.name));
                else print(String.format("%s 已阵亡！", cData.name));
                ensureOneSelectionIfPossible();
            }
        }
    }

    public void deleteBullet(int bID) {
        synchronized (this) {
            BulletDisplay bD = bulletDisplayMap.remove(bID);
            if (bD != null) {
                bD.remove();
                battleField.deleteBullet(bID);
            }
        }
    }

    public void gameFinish(int team, int reason) {
        if(currentState == field_state.GAME_PROCESSING){
            String st = "你输了！";
            String head = "游戏结束！";
            if (team == myTeam) st = "你赢了！";
            else if(team == -1)st = "双方平局！";
            if(reason == -2){
                if(team == myTeam){
                    head = "对方退出了战局，" + head;
                }
                else{
                    head = "你退出了战局，" + head;
                }
            }
            print(String.format("%s%s", head, st));
            currentState = field_state.GAME_FINISHED;
            String s = storeFinalReplay();
            if (s != null)print(String.format("回放文件已保存在 %s", s));
            else print(String.format("保存回放文件时出错，请检查文件占用"));
        }
        else {
            String st = "葫芦娃队胜利！";
            if (team == 1) st = "妖精队胜利！";
            else if(team == -1)st = "双方平局！";
            if(reason == -2){
                if(team == 1)print(String.format("葫芦娃队退出了游戏，回放结束！%s", st));
                else print(String.format("妖精队退出了游戏，回放结束！%s", st));
            }
            else print(String.format("回放结束！%s", st));
            currentState = field_state.REPLAY_FINISHED;

        }
        Platform.runLater(()->
                exitButton.setText("退出游戏")
            );
        replayButton.setDisable(false);
    }

    public void getMessage(AbstractMessage msg, boolean isReplaying) {
        synchronized(this){
            if(currentState == field_state.EXIT || currentState == field_state.GAME_FINISHED || currentState == field_state.REPLAY_FINISHED)return;
            if(!isReplaying)cacheReplay(msg);
            switch (msg.getType()) {
                case MSG_GAMEREADY:
                    GameReadyMsg msgGameReady = (GameReadyMsg) msg;
                    countDown(msgGameReady.getTime());
                    if (msgGameReady.getTime() == 0) {
                        start(isReplaying);
                    }
                    break;
                case MSG_BULLET_DEATH:
                    BulletDeathMsg msgBulletDeath = (BulletDeathMsg) msg;
                    deleteBullet(msgBulletDeath.getID());
                    break;
                case MSG_BULLET_GEN:
                    BulletGenMessage msgBulletGen = (BulletGenMessage) msg;
                    addBullet(msgBulletGen.getBulletData());
                    break;
                case MSG_CREATURE_DAMAGE:
                    CreatureDamageMsg msgCreatureDamage = (CreatureDamageMsg) msg;
                    doDamage(msgCreatureDamage.getInjuredID(), msgCreatureDamage.getAttackerID(), msgCreatureDamage.getDamage());
                    if(msgCreatureDamage.getIsDead())deleteCreature(msgCreatureDamage.getInjuredID(), msgCreatureDamage.getAttackerID());
                    break;
                case MSG_CREATURE_MOVE:
                    CreatureMoveMsg msgCreatureMove = (CreatureMoveMsg) msg;
                    syncCreatureMove(msgCreatureMove.getID(), msgCreatureMove.getNewX(), msgCreatureMove.getNewY());
                    break;
                case MSG_GAMEOVER:
                    GameOverMsg msgGameOver = (GameOverMsg) msg;
                    gameFinish(msgGameOver.getVictoryTeam(), msgGameOver.getVictoryReason());
                    break;
                case MSG_BULLET_SYNC:
                    BulletSyncMsg msgBulletSync = (BulletSyncMsg) msg;
                    updateBulletFromServer(msgBulletSync.getBulletSyncData());
                    break;
                case MSG_CREATURE_CREATE:
                    CreatureCreateMsg msgCreatrueCreature = (CreatureCreateMsg) msg;
                    addCreature(msgCreatrueCreature.getData());
                    break;
                case MSG_CREATURE_DIRECTION_CHANGED:
                    CreatureDirectionChanged msgDirectionChanged = (CreatureDirectionChanged)msg;
                    if(isReplaying() || msgDirectionChanged.getTeam() != myTeam)changeCreatureDirection(msgDirectionChanged.getID(), msgDirectionChanged.getNewDirection());
                    //如果是本队，那么应该是该Controller发送的消息，供回放功能留存使用
                    break;
                case MSG_GAME_INFO:
                    GameInfoMessage msgGameInfo = (GameInfoMessage)msg;
                    initInfo(isReplaying, msgGameInfo.getTeam(), msgGameInfo.getServerIP(), msgGameInfo.getGameID(), msgGameInfo.getTeam0(), msgGameInfo.getTeam1());
                default:
                    break;

            }
            if(isReplaying) {
                replayRunningTime = msg.getTimeCreate();
                //System.out.println("Replayed a msg: " + msg.getType());
                replayController.processFinished();
            }
        }
    }

    private void initInfo(boolean isReplaying, int team, String serverIP, int gameID, String team0, String team1) {
        serverLabel.setText("服务器地址：" + serverIP);
        roomLabel.setText("房间号：" + gameID);
        if(isReplaying){
            myTeam = team;
            if(team == 0){
                calabashLabel.setText("葫芦娃阵营：" + team0 + " (回放阵营)");
                monsterLabel.setText("妖精阵营：" + team1);
            }else{
                calabashLabel.setText("葫芦娃阵营：" + team0);
                monsterLabel.setText("妖精阵营：" + team1 + " (回放阵营)");
            }
        }else{
            if(team == 0){
                calabashLabel.setText("葫芦娃阵营：" + team0 + " (您)");
                monsterLabel.setText("妖精阵营：" + team1);
            }else{
                calabashLabel.setText("葫芦娃阵营：" + team0);
                monsterLabel.setText("妖精阵营：" + team1 + " (您)");
            }
        }
    }

    public void cacheReplay(AbstractMessage msg) {
        //System.out.println("Cached a msg: " + msg.getType());
        this.messageStore.add(msg);
    }

    public Pair<Double, Double> getCursorPos(){
        return new Pair<Double, Double>(cursorX, cursorY);
    }

    private String storeFinalReplay() {
        String name = "replays" + File.separator + "replays-" + getDateString() + "-team" + myTeam + ".cmbreplay";
        File file = new File(name);
        try {
            if(!file.getParentFile().exists())file.getParentFile().mkdirs();
            file.createNewFile();
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file));
            final List<AbstractMessage> l = messageStore;
            out.writeObject(l);
            out.close();
            return file.getAbsolutePath();
        } catch (IOException e1) {
            e1.printStackTrace();
            return null;
        }
    }

    public boolean isRunning() {
        return (currentState == field_state.GAME_PROCESSING) || (currentState == field_state.REPLAY_PLAYING);
    }
    
    public boolean isReplaying() {
        return (currentState == field_state.REPLAY_PLAYING) || (currentState == field_state.REPLAY_READYING);
    } 

    private String getTimeString(){
        Date d = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        return formatter.format(d);
    }

    private String getTimeString(long t){
        Date d = new Date(t);
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        return formatter.format(d);
    }

    private String getDateString(){
        Date d = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        return formatter.format(d);
    }

    public void print() {
        textDisplay.appendText("\n");
    }

    public void print(String text) {
        if(isReplaying())textDisplay.appendText("[回放][" + getTimeString(replayRunningTime) + "] " + text + "\n");
        else textDisplay.appendText("[" + getTimeString() + "] " + text + "\n");
    }

    

    public void setMainApp(ClientMain mainApp) {
        this.mainApp = mainApp;
    }

    public void setStage(Stage s) {
        this.stage = s;
    }

    public void close() {
        stage.close();
        mainApp.changeState(stateType.waiting_for_logging);
    }


}

