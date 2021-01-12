package nju.zc.calabashbattle.client.controller;

import java.util.HashSet;
import java.util.Set;

class MoveController {
    /**
      *[0]: up [1]:left [2]:down [3]:right
      */
    boolean directionProcess[];
    long directionProcessedTime;
    private Set<Integer> selectedSet;

    BattleFieldController bController;

    public MoveController(BattleFieldController battleFieldController){
        selectedSet = new HashSet<Integer>();
        directionProcess = new boolean[4];
        for (int i = 0; i < 4; i++){
            directionProcess[i] = false;
        }
        directionProcessedTime = -1;
        bController = battleFieldController;
    }

    /**
      *在按下SHift时，往已选中角色的list中增加Crenture
      */
    public void addSelected(int cID){
        selectedSet.add(cID);
    }

    /**
      *在未按下SHift时，设置已选中角色为Crenture
      */
    public void setSelected(int cID){
        selectedSet.clear();
        selectedSet.add(cID);
    }

    public boolean removeSelection(int cID){
        return selectedSet.remove(cID);
    }

    public void removeAllSelection(){
        selectedSet.clear();
    }

    /**
      *[0]: up [1]:left [2]:down [3]:right
      */
    public void setDirectionPressed(int direction, boolean state){
        if((direction >= 0 && direction <= 3) &&
            state && 
            !directionProcess[0] && !directionProcess[1] && !directionProcess[2] && !directionProcess[3]){
                directionProcessedTime = System.currentTimeMillis();
                directionProcess[direction] = state;
                moveAllSelectedForced(direction);
            }
        else{
            if(directionProcess[direction] != state){
                directionProcess[direction] = state;
                if(state){
                    moveAllSelectedForced(direction);
                    directionProcessedTime = System.currentTimeMillis();
                }
            }
        }
    }

    /**
      *对按下方向的所有选择成员，移动一个单位
      */
    public void moveAllSelected(){
        if(selectedSet.isEmpty() || (!directionProcess[0] && !directionProcess[1] && !directionProcess[2] && !directionProcess[3]))return;
        for(int cID : selectedSet) {
            int deltaX = (directionProcess[1] ? -1 : 0) + (directionProcess[3] ? 1 : 0);
            int deltaY = (directionProcess[0] ? -1 : 0) + (directionProcess[2] ? 1 : 0);
            if(deltaX != 0 || deltaY != 0) bController.moveCreature(cID, deltaX, deltaY);
        }
        directionProcessedTime = System.currentTimeMillis();
    }

    /**
      *对指定方向的所有选择成员移动一个单位
      */
    public void moveAllSelectedForced(int direction){
        if(selectedSet.isEmpty())return;
        for(int cID : selectedSet) {
            int deltaX = (direction == 1 ? -1 : 0) + (direction == 3 ? 1 : 0);
            int deltaY = (direction == 0 ? -1 : 0) + (direction == 2 ? 1 : 0);
            if(deltaX != 0 || deltaY != 0) bController.moveCreature(cID, deltaX, deltaY);
        }
    }

    public long getLastUpdateTime(){
        return directionProcessedTime;
    }

    public Set<Integer> getSelectedSet(){
        return this.selectedSet;
    }


}