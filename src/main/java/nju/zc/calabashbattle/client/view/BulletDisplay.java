package nju.zc.calabashbattle.client.view;

import javafx.geometry.Pos;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import nju.zc.calabashbattle.game.data.AbstractBulletData;
import nju.zc.calabashbattle.game.data.Define;

public class BulletDisplay {
    private AnchorPane pane;
    private VBox vBox ;
    private ImageView imageView;


    public BulletDisplay(AbstractBulletData bData, AnchorPane pane, int myTeam){
        vBox = new VBox();
        imageView = new ImageView();
        
        int index = (myTeam == bData.team ? 1 : 0);
        String url = "/resources/Image/bullet" + index + ".png";
        imageView = new ImageView(getClass().getResource(url).toString());
        imageView.setCache(true);

        vBox.getChildren().addAll(imageView);
        vBox.setMaxSize(Define.BULLET_X_LEN, Define.BULLET_Y_LEN);
        vBox.setMinSize(Define.BULLET_X_LEN, Define.BULLET_Y_LEN);
        vBox.setAlignment(Pos.CENTER);
        
        this.pane = pane;
        pane.getChildren().add(vBox);

        updateDisplay(bData.x, bData.y);
    }

    private void updateDisplay(double displayX, double displayY) {
        AnchorPane.setLeftAnchor(vBox, displayX);
        AnchorPane.setTopAnchor(vBox, displayY);
    }

    public void update(AbstractBulletData bData){
        updateDisplay(bData.x, bData.y);
    }

    public void remove() {
        //vBox.getChildren().removeAll(imageView);
        pane.getChildren().remove(vBox);
    }
}