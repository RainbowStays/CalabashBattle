package nju.zc.calabashbattle.game.scene;

import nju.zc.calabashbattle.game.bullet.*;
import nju.zc.calabashbattle.game.creature.*;
import nju.zc.calabashbattle.game.data.AbstractBulletData;
import nju.zc.calabashbattle.game.data.AbstractCreatureData;
import nju.zc.calabashbattle.game.data.Define;

import java.util.Map;
import java.util.Set;

import javafx.util.Pair;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;

public class BattleField implements Serializable{
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private Map<Integer, AbstractBullet> bulletMap;

    private Map<Integer, AbstractCreature> creatureMap;

    private Map<Integer, Set<Integer>> teamCreatureMap;

    private Map<Pair<Integer, Integer>, Integer> creaturePosMap;

    private Map<Integer, AbstractCreatureData> deadCreatureDataMap;

    public enum TeamState{
        BATTLEING,
        TEAM0_WINS,
        TEAM1_WINS,
        TIE,
        UNDEFINED
    }

    public BattleField(){
        bulletMap = new HashMap<Integer, AbstractBullet>();

        creatureMap = new HashMap<Integer, AbstractCreature>();
        teamCreatureMap = new HashMap<Integer, Set<Integer>> ();
        creaturePosMap = new HashMap<Pair<Integer, Integer>, Integer>();

        deadCreatureDataMap = new HashMap<Integer, AbstractCreatureData>();

        for(int i = 0; i < Define.TEAM_COUNT; i++){
            teamCreatureMap.put(i, new HashSet<Integer>());
        }
    }

    public boolean addBullet(AbstractBullet bullet){
        if(bulletMap.containsKey(bullet.getID()))return false;
        bulletMap.put(bullet.getID(), bullet);
        return true;
    }

    public AbstractBulletData generateAndAddCreatureBullet(int cID, double directionX, double directionY){
        AbstractCreature c = creatureMap.get(cID);
        if(c != null){
            AbstractBullet b = c.generateBullet(directionX, directionY);
            addBullet(b);
            return b.generateData();
        }
        else return null;
    }

    /**
     * 将指定的 AbstractCreature 类型对象加入战场
     * 如果战场中有Creature的id与其相同，将替换该Creature为新传入的Creature
     * @param creature
     */
    public boolean addCreature(AbstractCreature creature){
        return addCreature(creature.generateData());
    }

    public boolean addCreature(AbstractCreatureData cData){
        if(cData.id < 0)return false;
        if(cData.hp < 0 || cData.hpMax < 0)return false;
        if(cData.name == null)return false;
        if(cData.team < 0 || cData.team >= Define.TEAM_COUNT)return false;
        if(cData.posX < 0 || cData.posY < 0 || cData.posX >= Define.FIELD_COLUMN || cData.posY >= Define.FIELD_ROW)return false;

        if(creaturePosMap.containsKey(new Pair<Integer, Integer>(cData.posX, cData.posY)))return false;
        if(creatureMap.containsKey(cData.id))return false;

        creatureMap.put(cData.id, cData.generateCreature());
        teamCreatureMap.get(cData.team).add(cData.id);
        creaturePosMap.put(new Pair<Integer, Integer>(cData.posX, cData.posY), cData.id);
        return true;
    }

    public boolean changeCreatureDirection(int cID, double newDirection){
        AbstractCreature c = creatureMap.get(cID);
        if(c != null){
            c.setDirection(newDirection);
            return true;
        }
        else return false;
    }

    public AbstractBulletData deleteBullet(int bID){
        AbstractBullet res = bulletMap.remove(bID);
        if(res != null)return res.generateData();
        else return null;
    }

    public AbstractCreatureData deleteCreature(int cID){
        AbstractCreature res = creatureMap.remove(cID);
        if(res != null){
            teamCreatureMap.get(res.getTeam()).remove(cID);
            creaturePosMap.remove(new Pair<Integer, Integer>(res.getX(), res.getY()));
            AbstractCreatureData cDataDead = res.generateData();
            deadCreatureDataMap.put(cID, cDataDead);
            return cDataDead;
        }
        else return null;
    }

    public void updateBulletsPos(){
        for(AbstractBullet b: bulletMap.values()){
            b.moveUpdate();
        }
    }

    public Set<AbstractBulletData> generateBulletUpdateSet(){
        Set<AbstractBulletData> res = new HashSet<AbstractBulletData>();
        for(AbstractBullet b: bulletMap.values()){
            res.add(b.generateData());
        }
        return res;
    }

    public Set<AbstractCreatureData> getAllCreatureData(){
        Set<AbstractCreatureData> res = new HashSet<AbstractCreatureData>();
        for(AbstractCreature c: creatureMap.values()){
            res.add(c.generateData());
        }
        return res;
    }

    public Set<AbstractCreatureData> getAllCreatureData(int team){
        Set<AbstractCreatureData> res = new HashSet<AbstractCreatureData>();
        if(team < 0 || team >= Define.TEAM_COUNT) return res;
        for(int cID : teamCreatureMap.get(team)){
            res.add(creatureMap.get(cID).generateData());
        }
        return res;
    }

    public boolean moveCreatureTo(int cID, int x, int y){
        AbstractCreature c = creatureMap.get(cID);
        if(c == null) return false;
        else {
            if(x < 0 || y < 0 || x >= Define.FIELD_COLUMN || y >= Define.FIELD_ROW)return false;
            if(creaturePosMap.get(new Pair<Integer, Integer>(x, y)) != null) return false;
            creaturePosMap.remove(new Pair<Integer, Integer>(c.getX(), c.getY()));
            creaturePosMap.put(new Pair<Integer, Integer>(x, y), c.getID());
            return c.updatePos(x, y);
        }
    }

    public boolean moveCreature(int cID, int deltaX, int deltaY){
        AbstractCreature c = creatureMap.get(cID);
        if(c == null) return false;
        else {
            if(c.getX() + deltaX < 0 || c.getY() + deltaY < 0 || c.getX() + deltaX >= Define.FIELD_COLUMN || c.getY() + deltaY >= Define.FIELD_ROW)return false;
            if(creaturePosMap.get(new Pair<Integer, Integer>(c.getX() + deltaX, c.getY() + deltaY)) != null) return false;
            creaturePosMap.remove(new Pair<Integer, Integer>(c.getX(), c.getY()));
            creaturePosMap.put(new Pair<Integer, Integer>(c.getX() + deltaX, c.getY() + deltaY), c.getID());
            return c.updatePos(c.getX() + deltaX, c.getY() + deltaY);
        }
    }

    public Pair<Double, Double> getBulletPos(int bID) {
        AbstractBullet b = bulletMap.get(bID);
        if(b == null) return null;
        else return new Pair<Double, Double>(b.getX(), b.getY());
    }

    public AbstractBulletData getBulletData(int bID){
        AbstractBullet b = bulletMap.get(bID);
        if(b == null) return null;        
        return b.generateData();
    }

    public Pair<Integer, Integer> getCreaturePos(int cID){
        AbstractCreature c = creatureMap.get(cID);
        if(c == null)return null;
        else return new Pair<Integer, Integer>(c.getX(), c.getY());
    }

    public AbstractCreatureData getCreatureData(int cID){
        AbstractCreature c = creatureMap.get(cID);
        if(c == null)return null;
        else return c.generateData();
    }

    public AbstractCreatureData getCreatureData(int x, int y){
        Integer res = creaturePosMap.get(new Pair<Integer, Integer>(x, y));
        if(res != null){
            AbstractCreature c = creatureMap.get(res);
            return c.generateData();
        }
        else return null;
    }

    public AbstractCreatureData getDeadCreatureDate(int cID){
        return deadCreatureDataMap.get(cID); 
    }

    public int getCreatureID(int x, int y){
        Integer res = creaturePosMap.get(new Pair<Integer, Integer>(x, y));
        if(res != null) return res;
        else return -1;
    }

	public int doDamage(int cID, int damage) {
        AbstractCreature c = creatureMap.get(cID);
        if(c == null)return -1;
        else return c.gotDamage(damage);
    }
    
    public boolean isCreatureDead(int cID){
        AbstractCreature c = creatureMap.get(cID);
        if(c == null)return true;
        else return c.getHp() <= 0;
    }

	public boolean syncBullet(AbstractBulletData bData) {
        AbstractBullet b = bulletMap.get(bData.id);
        if(b != null){
            b.externalUpdate(bData);
            return true;
        }
        else return false;
    }
    
    public TeamState getTeamState(){
        int t = teamCreatureMap.size();
        if (t == 2){
            int team0 = teamCreatureMap.get(0).size();
            int team1 = teamCreatureMap.get(1).size();
            if(team0 == 0 && team1 == 0)return TeamState.TIE;
            else if(team0 == 0)return TeamState.TEAM1_WINS;
            else if(team1 == 0)return TeamState.TEAM0_WINS;
            else return TeamState.BATTLEING;

        }else return TeamState.BATTLEING;
        
    }

	public void clear() {
        bulletMap.clear();

        creatureMap.clear();

        teamCreatureMap.clear();

        creaturePosMap.clear();

        deadCreatureDataMap.clear();

        for(int i = 0; i < Define.TEAM_COUNT; i++){
            teamCreatureMap.put(i, new HashSet<Integer>());
        }
	}
}
