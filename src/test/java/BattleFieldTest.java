import nju.zc.calabashbattle.game.bullet.AbstractBullet;
import nju.zc.calabashbattle.game.creature.*;
import nju.zc.calabashbattle.game.data.AbstractCreatureData;
import nju.zc.calabashbattle.game.data.CalabashData;
import nju.zc.calabashbattle.game.scene.BattleField;

import static org.junit.Assert.*;

import org.junit.*;


public class BattleFieldTest {
    private static BattleField field = new BattleField();

    @BeforeClass
    public static void init(){
        field.clear();
        field.addCreature(new Calabash(0,   "大娃"  , 200,  10, 15, 60, 0, 5, 3.0));
        field.addCreature(new Calabash(1,   "二娃 " , 100,  25, 10, 50, 2, 6, 3.0));
        field.addCreature(new Calabash(2,   "三娃"  , 50,   50, 5,  50, 1, 7, 3.0));
        field.addCreature(new Monster (100, "蝎子精" , 500,  20, 15, 25, 13, 8, 1.0));
    }

    @Test
    public void TestFieldAddCreature(){
        //初始添加的葫芦娃所在位置应该有葫芦娃
        assertNotNull(field.getCreatureData(0, 5));
        assertNotNull(field.getCreatureData(2, 6));
        assertNotNull(field.getCreatureData(1, 7));
        assertNotNull(field.getCreatureData(13, 8));

        //向已有葫芦娃的位置添加葫芦娃应失败
        assertFalse(field.addCreature(new Calabash(5,   "五娃"  , 300,  25, 40, 50, 0, 5, 3.0)));
        
        //检查战场不应该有id为5的葫芦娃
        assertNull(field.getCreatureData(5));

        //id重复添加应该失败
        assertFalse(field.addCreature(new Calabash(2,   "三娃二号"  , 300,  25, 40, 50, 4, 5, 3.0)));

        //检查战场(4, 5)处不应该有葫芦娃
        assertNull(field.getCreatureData(4, 5));

        //测试另一种添加葫芦娃的方法
        AbstractCreatureData cData = new CalabashData(6, "test", 1, 2, 3, 4, 5, 0, 7, 8, 3.0);
        field.addCreature(cData);
        assertNotNull(field.getCreatureData(7, 8));

        //这种方法添加的生物也应不能重复
        AbstractCreatureData cData2 = new CalabashData(6, "test2", 2, 3, 4, 5, 6, 0, 8, 9, 3.0);
        field.addCreature(cData2);
        assertNull(field.getCreatureData(8, 9));
        
        //对于pos在战场外的生物应添加失败
        AbstractCreatureData cData3 = new CalabashData(7, "test7", 2, 3, 4, 5, 6, 0, 20, 20, 3.0);
        assertFalse(field.addCreature(cData3));

        //根据规范，改变已经传进field的生物被在field外的变量所引用的对象的属性，值不应该被改变
        AbstractCreature c = new Calabash(81,   "八十一难娃"  , 9,   9, 80,  1, 2, 2, 3.0);
        field.addCreature(c);
        c.setAttack(100);
        assertNotNull(field.getCreatureData(81));
        assertEquals(field.getCreatureData(81).attack, 9);

        init();
    }

    @Test
    public void TestFieldGetCreatureData(){
        //战场内应该有id为2的葫芦娃
        assertEquals(2, field.getCreatureData(2).id);

        //位置(0, 5)的葫芦娃id应为0
        assertEquals(0, field.getCreatureID(0, 5));

        //检查战场不应该有id为5的葫芦娃
        assertNull(field.getCreatureData(5));

        //测试添加的Data的正确性
        AbstractCreatureData cData = new CalabashData(6, "test", 1, 2, 3, 4, 5, 0, 7, 8, 3.0);
        field.addCreature(cData);
        assertNotNull(field.getCreatureData(7, 8));
        AbstractCreatureData cDataIn = field.getCreatureData(6);
        assertEquals(cData.name, cDataIn.name);

        init();
    }

    @Test
    public void TestGenerateCreatureData(){
        //验证已生成的cData不会因本体的改变而改变
        AbstractCreature c = new Calabash(0,   "大娃"  , 200,  10, 15, 60, 0, 5, 3.0);
        AbstractCreatureData cData1 = c.generateData();
        c.setDirection(2.0);
        assertEquals(3.0, cData1.direction, 0.00001);

        //验证更改后生物生成的cData已经正确改变
        assertEquals(2.0, c.generateData().direction, 0.001);
    }



    @Test
    public void TestFieldMoveCreature(){
        //测试移动
        assertTrue(field.moveCreatureTo(0, 1, 2));
        assertNotNull(field.getCreatureData(1, 2));
        assertNull(field.getCreatureData(0, 5));

        //测试移动到的位置有生物时的正确性
        assertFalse(field.moveCreatureTo(1, 13, 8));
        assertNotNull(field.getCreatureData(2, 6));
        assertEquals(1, field.getCreatureData(2, 6).id);
        assertEquals(100, field.getCreatureData(13, 8).id);

        //测试移动到一个非法位置时的正确性
        assertFalse(field.moveCreatureTo(1, 99, 99));
        assertNotNull(field.getCreatureData(2, 6));
        assertNull(field.getCreatureData(99, 99));
        assertEquals(1, field.getCreatureData(2, 6).id);
        init();

        //增量移动的正确性
        assertTrue(field.moveCreature(1, 1, 1));
        assertNotNull(field.getCreatureData(3, 7));
        assertNull(field.getCreatureData(2, 6));

        //增量移动的边界情况
        assertFalse(field.moveCreature(1, 121, 114514));
        assertNotNull(field.getCreatureData(3, 7));
    } 


    @Test
    public void TestFieldDeleteCreature(){
        //基本情况
        AbstractCreatureData cData = new CalabashData(6, "test", 1, 2, 3, 4, 5, 0, 7, 8, 3.0);
        field.addCreature(cData);
        assertNotNull(field.getCreatureData(7, 8));
        assertNotNull(field.deleteCreature(6));
        assertNull(field.getCreatureData(7, 8));

        //删除不存在的生物
        assertNull(field.deleteCreature(1111));

        init();
    }

    @Test
    public void TestBullet() throws InterruptedException {
        //基本情况：从生物生成的Bullet是该生物的
        AbstractCreatureData cData = new CalabashData(6, "test", 1, 2, 3, 4, 5, 0, 7, 8, 3.0);
        AbstractCreature c = cData.generateCreature();
        assertEquals(6, c.generateBullet(1, 1).getAttackerID());


        //生成的子弹位置是正确的
        assertEquals(7*60, c.generateBullet(1 * 60, 1 * 60).getX(), 0.00001);
        assertEquals(8.5*60, c.generateBullet(1 * 60, 1 * 60).getY(), 0.00001);
        assertEquals(8*60, c.generateBullet(8 * 60, 8 * 60).getX(), 0.00001);
        assertEquals(8.5*60, c.generateBullet(8 * 60, 8 * 60).getY(), 0.00001);

        //生成的子弹方向是正确的
        assertEquals(1, c.generateBullet(9 * 60, 8.5 * 60).getDIrectionX(), 0.00001);
        assertEquals(0, c.generateBullet(9 * 60, 8.5 * 60).getDIrectionY(), 0.00001);
        assertEquals(Math.sqrt(2) / 2.0, c.generateBullet(9 * 60, 9.5 * 60).getDIrectionX(), 0.00001);
        assertEquals(Math.sqrt(2) / 2.0, c.generateBullet(9 * 60, 9.5 * 60).getDIrectionY(), 0.00001);

        //子弹坐标是可以更新的
        AbstractBullet bullet = c.generateBullet(9 * 60, 8.5 * 60);
        assertEquals(1, bullet.getDIrectionX(), 0.00001);
        assertEquals(0, bullet.getDIrectionY(), 0.00001);
        assertTrue(bullet.getX() == 8 * 60);
        assertTrue(bullet.getY() == 8.5 * 60);
        Thread.sleep(100);
        bullet.moveUpdate();
        assertTrue(bullet.getX() > 8 * 60);
        assertTrue(bullet.getY() == 8.5 * 60);
    }

}