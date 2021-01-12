package nju.zc.calabashbattle.client.view;

import javafx.application.Platform;
//import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import nju.zc.calabashbattle.client.controller.BattleFieldController;
import nju.zc.calabashbattle.game.data.AbstractCreatureData;
import nju.zc.calabashbattle.game.data.Define;

public class CreatureDisplay {
    private AnchorPane pane;
    private VBox vBox;
    private ProgressBar hpBar;
    private Button button;
    private ImageView imageView_l;
    private ImageView imageView_r;
    private ImageView imageView_big;
    private int cID;
    private BattleFieldController controller;
    private boolean selected = false;
    private ImageView imageSeletion;
    private VBox selectedVBox;
    private double currentDirection = 0;

    public CreatureDisplay(BattleFieldController bController, AbstractCreatureData cData, AnchorPane pane, int myTeam) {
        vBox = new VBox();
        button = new Button();
        hpBar = new ProgressBar();
        imageView_l = new ImageView();
        imageView_r = new ImageView();
        imageView_big = new ImageView();
        selectedVBox = new VBox();
        imageSeletion = new ImageView();

        this.cID = cData.id;
        controller = bController;
        currentDirection = cData.team == 0 ? 3.0 : 1.0; 

        String url_l = "/resources/Image/" + cID + "_l.png";
        String url_r = "/resources/Image/" + cID + "_r.png";
        String url_big = "/resources/Image/" + cID + "_big.png";
        imageView_l = new ImageView(getClass().getResource(url_l).toString());
        imageView_l.setCache(true);
        imageView_r = new ImageView(getClass().getResource(url_r).toString());
        imageView_r.setCache(true);
        imageView_big = new ImageView(getClass().getResource(url_big).toString());
        imageView_big.setCache(true);


        String url_arrow = "/resources/Image/arrow.png";
        imageSeletion = new ImageView(getClass().getResource(url_arrow).toString());
        imageSeletion.setCache(true);
        selectedVBox.getChildren().add(imageSeletion);
        selectedVBox.setMaxSize(Define.BOX_X_LEN, 10);
        selectedVBox.setMinSize(Define.BOX_X_LEN, 10);
        selectedVBox.setAlignment(Pos.CENTER);

        if(currentDirection == 3.0)button.setGraphic(imageView_r);
        else button.setGraphic(imageView_l);
        button.setTooltip(new Tooltip(String.format("%s%n生命：%d/%d%n攻击力：%d%n防御力：%d%n攻速：%d", cData.name, cData.hp,
                cData.hpMax, cData.attack, cData.defend, cData.attackSpeed)));
        button.setMinSize(Define.BOX_X_LEN, Define.BOX_Y_LEN - 10);
        button.setMaxSize(Define.BOX_X_LEN, Define.BOX_Y_LEN - 10);
        button.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
        if(!controller.isReplaying() && myTeam == cData.team)
        {   
            button.setCursor(Cursor.OPEN_HAND);
        }
        button.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                synchronized(controller){
                    controller.setSelection(cID);
                }

            }
        });

        hpBar.setMinSize(Define.BOX_X_LEN * 0.75, 10.0);
        hpBar.setMaxSize(Define.BOX_X_LEN * 0.75, 10.0);
        if(cData.team == myTeam)hpBar.setStyle("-fx-accent: green;");
        else hpBar.setStyle("-fx-accent: red;");

        vBox.getChildren().addAll(hpBar, button);
        vBox.setMaxSize(Define.BOX_X_LEN, Define.BOX_Y_LEN);
        vBox.setMinSize(Define.BOX_X_LEN, Define.BOX_Y_LEN);
        vBox.setAlignment(Pos.CENTER);
        
        this.pane = pane;
        pane.getChildren().add(vBox);

        updateHPBar(cData.hp, cData.hpMax);
        updateTip(cData);
        updateDisplay(cData.posX, cData.posY);
        updateSeletionArrow(cData.posX, cData.posY);
    }

    private void updateHPBar(double hp, int hpMax){
        this.hpBar.setProgress(hp / hpMax);
    }

    private void updateTip(AbstractCreatureData cData){
        Platform.runLater(()->
            button.getTooltip().setText(String.format("%s%n生命：%d/%d%n攻击力：%d%n防御力：%d%n攻速：%d", cData.name, cData.hp, cData.hpMax, cData.attack, cData.defend, cData.attackSpeed))
        );
    }

    private void updateDisplay(int displayX, int displayY) {
        AnchorPane.setLeftAnchor(vBox, displayX * Define.BOX_X_LEN);
        AnchorPane.setTopAnchor(vBox, displayY * Define.BOX_Y_LEN);
    }

    public void update(AbstractCreatureData cData){
        updateHPBar(cData.hp, cData.hpMax);
        updateTip(cData);
        updateDisplay(cData.posX, cData.posY);
        if(currentDirection != cData.direction)updateTowards(cData.direction);
        if(selected)updateSeletionArrow(cData.posX, cData.posY);
    }

    public void remove() {
        if(pane.getChildren().contains(this.vBox))pane.getChildren().remove(this.vBox);
        setSelected(false);
    }

    public void setSelected(boolean state){
        if(this.selected == state)return;
        if(state){
            selected = true;
            Platform.runLater(()->
                button.requestFocus()
            );
            pane.getChildren().add(selectedVBox);
        }
        else{
            selected = false;
            pane.getChildren().remove(selectedVBox);
        }
    }

    public boolean isSelected(){
        return this.selected;
    }

    public void updateSeletionArrow(int displayX, int displayY) {
        AnchorPane.setLeftAnchor(selectedVBox, displayX * Define.BOX_X_LEN);
        AnchorPane.setTopAnchor(selectedVBox, displayY * Define.BOX_Y_LEN - selectedVBox.getMaxHeight() - 5);
    }

    private void updateTowards(double direction){
        if(direction == currentDirection)return;
        else{
            if(direction == 3.0){
                Platform.runLater(()->
                    button.setGraphic(imageView_r)
                );
                currentDirection = 3.0;
            }
            else{
                Platform.runLater(()->
                    button.setGraphic(imageView_l)
                );
                currentDirection = 1.0;
            }
        }
    }

    public void setCursor(Cursor c){
        button.setCursor(c);
    }

    public ImageView getImageViewBig(){
        return imageView_big;
    }

}