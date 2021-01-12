package nju.zc.calabashbattle.game.creature;

import nju.zc.calabashbattle.game.bullet.AbstractBullet;
import nju.zc.calabashbattle.game.data.AbstractCreatureData;
import nju.zc.calabashbattle.game.data.Define;

public abstract class AbstractCreature{

    protected final int id;
    protected final String name;
    protected final int team;
    protected final int hpMax;

    protected int hp;
    
    protected int attack;
    protected int defend;
    protected int attackSpeed;
    protected int x;
    protected int y;
    protected double direction;

    protected AbstractCreature(){
        this.id = -1;
        this.name = "";
        this.team = -1;
        this.hp = this.hpMax = this.attack = this.defend = this.attackSpeed = -1;
    }
    
    protected AbstractCreature(int id, String name, int team, int hp, int attack, int defend, int speed, int x, int y, double direction) {
        this.id = id;
        this.name = name;
        this.team = team;
        this.hp = hp;
        this.hpMax = hp;
        this.attack = attack;
        this.defend = defend;
        this.attackSpeed = speed;
        this.x = x;
        this.y = y;
        this.direction = direction;
    }

    public int getID() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getTeam(){
        return team;
    }

    public int getHp() {
        return hp;
    }

    public int getHpMax() {
        return hpMax;
    }

    public int getAttack(){
        return attack;
    }

    public int getDefend(){
        return defend;
    }

    public int getAttackSpeed(){
        return attackSpeed;
    }

    public int getX(){
        return x;
    }

    public int getY(){
        return y;
    }

    public double getDirection(){
        return direction;
    }

    public void setDirection(double direction){
        this.direction = direction;
    }

    public void setAttack(int attack){
        this.attack = attack;
    }

    public void setDefend(int defend){
        this.defend = defend;
    }

    public void setAttackSpeed(int speed){
        this.attackSpeed = speed;
    }

    public boolean updatePos(int x, int y){
        if(x >= 0 && x < Define.FIELD_COLUMN && y >= 0 && y < Define.FIELD_ROW){
            this.x = x;
            this.y = y;
            return true;
        }
        return false;
    }

    public int gotDamage(int dam){
        double reduceDamage = dam / (double)(defend + dam);
        int d = (int)(dam * reduceDamage);
        this.hp -= d;
        if(this.hp < 0)this.hp = 0;

        return d;
    }

    public abstract AbstractCreatureData generateData();
    
    public abstract AbstractBullet generateBullet(double PointGoalX, double PointGoalY);
}
