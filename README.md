# 葫芦娃大战妖精

<img src="README.assets\image-20210110010253657.png" alt="image-20210110010253657" style="zoom:67%;" />

## 简介与操作方式

​	成员：181860005 陈千昕	181860148 钟旭

​	本项目实现了葫芦娃大战妖精课程作业，即多人联网小游戏。本项目的服务器实现了多房间功能，即同一时刻可以有多组双人玩家进行各自的游戏，彼此互不干扰。并且采用了登录制登录服务器，玩家可以取喜欢的昵称进行游戏。

​	一局游戏有两位玩家参与。服务器会给玩家随机选择阵营：葫芦娃阵营或妖精阵营。倒计时3秒后开始游戏，玩家可使用鼠标选择要操控的角色（按住CTRL进行鼠标单击可以多选角色），对选中的角色使用WASD控制人物进行上下左右或者对角线方向进行移动。本方所有角色按各自的攻击速度，面向鼠标方向，向鼠标所在位置附近发射子弹。子弹击中敌方角色后，敌方角色将被扣除生命。当某一方角色全部死亡后游戏结束，另一方获胜，同时保存日志文件以供查看。

​	无论是回放还是游戏中，单击任意角色即可在上方查看到该角色的所有属性。在游戏中，已选中的角色上方会显示一个绿色的箭头：

​	<img src="README.assets\image-20210110012657958.png" alt="image-20210110012657958" style="zoom: 67%;" /> 

​	游戏中，可以单击退出战局按钮直接退出战局，对方将直接获胜。在回放中，可以单击退出回放按钮退出该局回放。在游戏或者回放结束时，单击退出游戏按钮退出程序。



## 程序入口

​	使用 `mvn clean test package` 打包后使用 `java -cp target/game-1.0.jar <入口>` 运行

​	服务器入口位于 nju.zc.calabashbattle.server.ServerMain

​	客户端入口位于 nju.zc.calabashbattle.client.ClientMain



## 总体框架

​	总体框架采用C-S结构和MVC架构结合实现：实现Client与Server两大模块的同时，在Client和Server内部使用了经典的MVC框架，其中大部分Model放在了外部（Client与Server共用），并命名为Game模块。因此工程架构是这样的结构：

> ```
> main\java\nju\zc\calabashbattle
> |- client
> |  |- controller
> |  |  |- ...
> |  |- model
> |  |  |- ...
> |  |- view
> |  |  |- ...
> |  |- ClientMain.java
> |- game
> |  |- ...
> |- server
> |  |- controller
> |  |  |- ...
> |  |- model
> |  |  |- ...
> |  |- view
> |  |  |- ...
> |  |- ServerMain.java
> ```

​	这样的结构方便了统一数据结构在Client和Server的统一与复用，同时又保留了MVC框架的特点，视图模型控制器分离，方便复用与管理。

​	此外，还使用了工厂模式去获得子弹对象，将会在下面详细解释。



## 分工

​	181860005 陈千昕 负责客户端Client部分，181860148负责服务器Server部分，两人共同分工Game模块的数据设计



## 数据模型Game

​	Game中分为5个模块，分别是bullet、creature、data、message、scene：

​	在设计这些模型的过程充分使用到了面向对象的特性，包括不限于继承、封装、多态、组合、聚合、重载、重写、抽象类、抽象方法、接口等。

### 报文message

```java
public abstract class AbstractMessage implements java.io.Serializable {
    public enum MsgType{
        MSG_UNDEFINED,
        MSG_LOGIN,
        ...
    }

    private long generateTime;

    AbstractMessage(){
        generateTime = System.currentTimeMillis();
    }
    private static final long serialVersionUID = 1L;

    public abstract MsgType getType();

    ...
}
```

​	为了传输报文，我们使用了序列化技术，所有报文类都实现了 `java.io.Serializable` 接口，这样客户端与服务器发送的所有报文都是 `AbstractMessage` 类或其子类。为了方便客户端以及服务器处理，我们使用了内部枚举类型`MsgType`（使用了组合思想），并定义了抽象函数 `getType()`，各message类重写该抽象函数以返回自己的类对应的类型。在接受报文时只需要调用 `getType()` 方法即可获取类型，并调用不同函数处理其内部。

​	以 `BulletDeathMsg` 为例：

```java
public class BulletDeathMsg extends AbstractMessage{
    private static final long serialVersionUID = 1L;
    
    private final int deathID;

    public BulletDeathMsg(int ID){
        this.deathID = ID;
    }

    public int getID(){
        return this.deathID;
    }

    @Override
    public MsgType getType(){
        return MsgType.MSG_BULLET_DEATH;
    }
}
```

​	`	AbstractMessage` 类的子类也均实现了序列化。在 `BulletDeathMsg` 类中，我们传输的是要消除的子弹的ID，因此该message成员有 `deathID`，其修饰符为 `private` 和 `final`，因为我们不想让外界直接访问到该ID，并不想让外界随意修改ID的值。取用ID时，调用 `getID()` 方法即可。

​	`getType()`重写了基类的抽象方法，在`BulletDeathMsg` 类中返回的则是 `MsgType.MSG_BULLET_DEATH`，即自己的类型。



### 生物creature及其数据存储data

​	与message相同，各种生物均继承了`AbstractCreature` 抽象基类：我们的两大阵营生物`Calabash`和`Monster`均继承了该抽象基类。为了不让外界修改，又能较轻松的取得所有数据，而且还能方便地进行网络传输，我们定义了`AbstractCreatureData`类。先看 `AbstractCreature` 抽象基类的定义：

```
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
        ...
    }
    
    protected AbstractCreature(int id, String name, int team, int hp, int attack, int defend, int speed, int x, int y, double direction) {
        ...
    }

    public int getID() {
        return id;
    }

    ...

    public void setDirection(double direction){
        ...
    }

    public int gotDamage(int dam){
        ...
    }

    public abstract AbstractCreatureData generateData();
    
    public abstract AbstractBullet generateBullet(double PointGoalX, double PointGoalY);
}
```

​	抽象基类中定义了一个生物所必须具备的各项属性，其中所有成员均为protected类型，这是为了子类能够取用信息。之后的函数是为了取用某一属性的值，或改变某一属性的值，最后两个函数第一个是 `generateData()`，可以以该对象生成对应的数据形式，而第二个是 `generateBullet`，在给定参数射击点的情况下返回根据该生物和射击点生成的子弹。整个项目子弹的生成均来自于此函数或类似此函数的方法生成，这两个方法的设计属于类似工厂模式的方法，不同点是我们可以根据不同的生物对象生成特定的子弹，也即函数不是static方法。这样可以在不传输生物信息的情况下直接根据两个外部参数设计点坐标来直接生成子弹，进一步提高了代码的简洁性。

​	而`AbstractCreatureData`类即是对`AbstractCreature`内的所有属性拿出，并作为public final类型，不仅方便取用，还不必担心外界的修改。`CalabashData` 和 `MonsterData` 继承了该基类。同时，我们还在`AbstractCreatureData`类实现了如下抽象方法：

```java
public abstract AbstractCreature generateCreature();
```

​	也即我们根据data数据，可以直接生成以这些数据为参数的Creature。我们不必知道生成的是具体的`Calabash` 还是 `Monster`，因为在`CalabashData`中：

```java
public class CalabashData extends AbstractCreatureData{
    private static final long serialVersionUID = 1L;

    public CalabashData(int id, String name, int hp, int hpMax, int attack, int defend, int attackSpeed, int team, int posX, int posY, double direction) {
        super(id, name, hp, hpMax, attack, defend, attackSpeed, team, posX, posY, direction);
    }

    @Override
    public AbstractCreature generateCreature(){
        return new Calabash(this);
    }
}
```

​	可以看到，我们的Data与对象类是紧密结合的，利用 `Calabash` 的构造函数的重载，我们可以选择直接将data传入构造函数，就直接生成了Calabash类的生物对象。

​	通过生物对象生成data，再根据data生成生物对象，这很符合网络传输的特性，因此我们项目的对子弹以及生物的网络传输传输的均是其data，在收到后直接使用 `generateCreature()`或 `generateBullet()` 方法直接获得对应的对象即可。



### 子弹bullet及其数据存储data

​	思想与creature几乎一致，基类为`AbstractBullet`，传输数据使用`AbstractBulletData`，可以从子弹生成其数据也可以根据数据生成子弹。在此不再赘述。



### 战局存储scene

​	存储了战局所需的各结构，包括：

```java
private Map<Integer, AbstractBullet> bulletMap;//以子弹ID为索引的战局内子弹的信息

private Map<Integer, AbstractCreature> creatureMap;//以生物ID为索引的战局内生物的信息

private Map<Integer, Set<Integer>> teamCreatureMap;//以队伍ID为索引的该队伍所有生物ID的信息

private Map<Pair<Integer, Integer>, Integer> creaturePosMap;//以坐标为索引的生物ID信息

private Map<Integer, AbstractCreatureData> deadCreatureDataMap;//以生物ID为索引的已死亡生物信息
```

我们存取了所有需要的信息，以及定义了对应函数的操作，使得对于战局内数据的操作仅能通过该类完成。

## 客户端Client

### 界面设计

​	客户端主要功能是与服务器进行连接，对服务器传来的战局进行显示、处理用户交互信息并发送给服务器

​	下面简单介绍一下界面：

- 登录界面：

​	<img src="README.assets\image-20210110005051143.png" alt="image-20210110005051143" style="zoom:67%;" />

​	在此界面用户可以选择连接服务器进行登录，准备开始对局，也可以选择战斗回放来回放以前进行过的战斗。当焦点不在输入框时，按L键也可进入战斗回放模式。

​	当用户输入昵称和服务器地址后，单击Login按钮就可以进入匹配界面，等待服务器匹配到另一玩家。



- 匹配界面：

  <img src="README.assets\image-20210110005313606.png" alt="image-20210110005313606" style="zoom:67%;" /> 

  连接到服务器后，将会等待另一个玩家的加入。如果无法连接到服务器或者登陆出错，将会有以下提示：

  <img src="README.assets\image-20210110005541522.png" alt="image-20210110005541522" style="zoom:67%;" /> 

  

- 游戏界面

  当服务器有2个以上玩家处于匹配模式后，服务器会将这两个玩家加入到一个战局中，倒计时并开始游戏：

  <img src="README.assets\image-20210110012428310.png" alt="image-20210110012428310" style="zoom: 50%;" /> 

  ​	游戏上方为人物信息，展示的是最近的鼠标单击过的角色的生物信息。右上方显示的是服务器地址、该局游戏的房间号以及两方阵营的玩家昵称。下方是打开回放以及退出按钮，再下方是日志栏，以文字形式记录对局信息。左侧是战局区域，是游戏的主要操纵区域。

### 设计方法

#### 界面切换

​	界面的切换对应的是View和Controller的切换。Controller的切换自然需要使用mainApp去实现，view在javafx中其实就是.fxml文件的读取。

##### 窗口的显示

​	在javafx中，主应用类要继承Application类才能运行。在start函数中，我们可以得到程序运行的主Stage，所有的组件都要添加到Stage中才能以窗口的形式显示。我们也可以创建新的Stage对象，也即打开一个新窗口。根据程序逻辑，开始时我们显示登录窗口：

```java
    FXMLLoader fx = new FXMLLoader();
    URL url = getClass().getResource("/resources/Fxml/LOGIN.fxml");
    fx.setLocation(url);
    Pane pane = (Pane) fx.load();
    loginController = (LoginController) fx.getController();
    primaryStage.setTitle("Login");
    primaryStage.setScene(new Scene(pane));
    primaryStage.show();
    primaryStage.setResizable(false);
    loginController.setMainApp(this);
    loginController.init();
```

​	FXMLLoader可以对给定的FXML进行解析，从而打开设定的窗口布局。我们先找到fxml的url地址（使用Class类的 `getResource()` 方法，地址内部以“/“开始，就可以定位在包根部的文件夹路径，再再其后输入相对路径即可）。在加载后，会返回一个Pane对象，也即整个窗口布局的总容器。需要增删元件即可通过此pane进行。然后设置 `primaryStage` 各参数，将Pane放在一个新Scecne类对象中并设置该对象为primaryStage的显示场景，然后调用 `primaryStage.show()`，就完成了窗口的加载。 

​	当我们需要打开另一个窗口时，我们先new一个新的Stage，然后按上述方法找到布局文件，打开并获得总容器pane，然后进入到一个Scene对象中绑定到Stage即可。至于原来的窗口，调用 `primaryStage.close()` （或 `primaryStage.hide()` 即可。



#### 网络数据传输

​	网络的传输由另一个控制器 `ClientSocketController` 负责统筹实现。因为等待消息需要阻塞进程，我们不能让游戏一直处于阻塞状态，因此需要一个新的线程来控制网络传输。

​	下面展示了 `ClientSocketController` 所重写的 `run()` 函数

```java
@Override
    public void run() {
        try {
            connect();
            out = new ObjectOutputStream(socket.getOutputStream());
            sendMsg(new LoginMessage(username));
            in = new ObjectInputStream(socket.getInputStream());
            Platform.runLater(()->
                mainApp.hallController.print("Connected to Server. Waiting for logging in…"
            );
            running = true;
        } catch (IOException e1) {
            Platform.runLater(()->
                mainApp.hallController.loginFailed("Unable to reach Server")
            );
        }
        while(running){
            try{
                AbstractMessage msg = (AbstractMessage)in.readObject();
                if(handleMessage(msg) == -1){
                    close();
                    break;
                }
            }
            catch(Exception e){
                Platform.runLater(()->
                    mainApp.hallController.loginFailed("Disconnected from the Server")
                );
                break;
            }
        }
    }
```

​	以及建立连接使用的 `connect()` 函数：

```java
public void connect() throws IOException {
	socket = new Socket(serverIP, port);
}
```

​	我们通过新建Socket类对象，利用之前提到的序列化数据结构，我们建立 `ObjectOutputStream` 和 `ObjectInputStream` ，连接到Socket的输入输出流上，就可以不断通过 `in.readObject()` 来获取服务器发送的对象信息。本项目中所有发送的报文均为 `AbstractMessage` 的子类，因此我们通过 `AbstractMessage msg = (AbstractMessage)in.readObject();` 即可取到服务器发送的报文。发送消息只需要其他模块调用该类对象（在main函数中的引用）`socketController` 的 `sendMsg()` 方法：

```java
public void sendMsg(AbstractMessage message){
    synchronized(this.out){
        try{
            out.reset();
            out.writeObject(message);
            out.flush();
        }catch(Exception e){
            //e.printStackTrace();
        }
    }
}
```

​	这里为了防止多个线程一同调用 `sendMsg()` 导致意料之外的错误，使用了进程同步机制锁定out输出流，这样可以保证同一时刻只有一个线程在out输出流上写数据，保证了可靠性。

​	接受到的Message报文，调用其 `getType()` 成员函数，利用switch来实现不同的处理逻辑：

```java
private int handleMessage(AbstractMessage msg) throws Exception {
        switch(msg.getType()){
            case MSG_LOGINREPLY:
            	...
            case MSG_MATCH:
            	...
            case MSG_GAME_INFO:
            	...
            case MSG_BULLET_DEATH:
            case MSG_BULLET_GEN:
            case MSG_BULLET_SYNC:
            case MSG_CREATURE_CREATE:
            case MSG_CREATURE_DAMAGE:
            case MSG_CREATURE_MOVE:
            case MSG_GAMEOVER:
            case MSG_GAMEREADY:
            case MSG_CREATURE_DIRECTION_CHANGED:
            	Platform.runLater(()->
                    mainApp.sendFieldControllerMessage(msg)
                );
                break;
            default:
                break;
        }
        return 1;
    }
```

​	例如对于最后的数种message，传递给战场控制器此报文，作为游戏中的报文的处理。这里使用 `Platform.runlater()` 是因为之后进行的很多操作在javafx中限制必须要使用主线程处理，使用该函数类似于指派主线程在不久后需要完成的任务，这些任务会按照加入的时间先后尽快被处理。

#### 游戏运行逻辑

​		为了让游戏正常运行，我们需要以下几个线程：首先是之前提到的网络通信线程，然后我们需要给战场刷新显示任务一个线程，还需要给每一个生物一个线程，用来控制生物发送子弹（因为每个生物的攻速不同，根据每个生物的攻速在发送子弹后sleep对应时长，这种实现方法较为便捷和优雅。

#### 核心思想

​	对于战场的所有操作需要通过 `BattleFieldController` 实现，这里我们新建一个该类对象给变量bController。为了确保进程之间的协同，我们对于每个函数内需要操作战场的部分（也即需要使用bController的成员函数时）都对bController进行锁定，下面是操作模板：

```java
public void somefunction(){
	...
	synchronized(bController){
		//do something
	}
	...
}
```

​	为了保证线程结束能被统一控制，线程的操作的循环条件是 `bController.isRunning()`，这样子在需要关闭各线程时只要改变该函数返回值即可。

#### 数据操作与视图的协同

​	我们提到所有对战场的操作都通过bController实现，也即是说bController可以直接操作战场的model（即上文介绍的BattleField类），因此 `BattleFieldController` 的成员应含有一个BattleField类对象。

​	我们将BattleField的各操作再次封装一层，除了对数据model进行操作我们还加入对相应view的操作。只是大多数情况下，我们不需要显示操作每次都立即执行。我们可以大约每10ms左右检测一次，每20ms统一刷新一次战场内的所有生物、子弹等内容。这一任务是由上文提到的显示任务线程管理的：

```java
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
            }
        }
    }
```

​	除了显示之外，这个线程还可以用来控制角色的运动。我们计算当前时间与上一次刷新显示的时间的时间差，如果大于等于我们设置的时间，就调用 `bController.updateDisplay()` 来根据当前的数据进行刷新显示。这样操作，数据更改与刷新操作分离，无需每一次更改数据都进行刷新视图特定部分的繁杂处理，只需每隔一段很小的时间根据现在战场内的数据信息刷新战场的所有视图即可。

#### 日志的保存与回放

​	前文提到mainApp会将bController需要的报文发送过来，我们仅需要在bController内部的接收消息函数的头部保存每一个收到的message，在游戏结束时统一将所有message以ObjectOutputStream形式写到文件中保存。在回放时，使用ObjectInputStream读取文件，然后新建一个线程模拟ClientSocketController，即将所有的message按接收的时间排序后（使用Collections框架），每次发送一个message，就sleep下一个message发送的时间减去这个message发送的时间的时长，就可以做到模拟了。

​	发送的函数使用如下函数，利用Platform交由主线程处理（这也是ClientSocketController的做法），因为很多操作必须由javafx线程完成：

```java
Platform.runLater(()->
	bController.getMessage(msg, true)
);
```

​	但这样会导致的问题是可能在sleep的这段时间内bController可能未处理完成，甚至开始下一次循环时主线程还没来得及来时处理该线程。这时简单使用 `synchronized(bController)` 不解决问题。为此我们加入如下判断来进行线程同步：

```java
while(!isProcessed)Thread.sleep(1);
isProcessed = false;
```

​	当我们完成一条信息发送后，就进入等待时间，当bController处理完一个消息后，进入 `getMessage()` 函数的尾部，这时我们将i `sProcessed`置为 `true`，这里就会从阻塞状态退出，然后我们再将 `isProcessed` 置 `false`即可，直到下一次bController处理完毕。



## 服务器端

### 简介

​	服务器的主要功能是与客户端进行连接和监听客户端发来的消息并对之进行处理。当客户端连接成功后，服务器将该客户端其加入匹配队列中，每当两个客户端匹配成功后，将为其开启一局新游戏并且初始化战场，理论上可支持多组玩家同时分别进行对战。   
​	在游戏进行过程中，服务器的职能有：  

1.  对玩家进行的操作的合法性进行判断和处理，例如一个生物要移动到的位置已有生物，则会无视该移动请求。
2.  判断战场的胜负情况。
3.  对子弹消亡和子弹造成的伤害进行处理。
4.  同步两个客户端的战场上的子弹。
5.  打印日志

### 网络通信

​	服务器开始运行后，开启一个线程用来监听新客户端的连接：

```java
Server.this.serversocket = new ServerSocket(Common.PORT);
Server.this.isRunning = true;
while(Server.this.isRunning){
    Socket s = serversocket.accept();
    //handle
}
```

​	当有客户端连接成功后，将另开启一个新线程分配资源，用于监听客户端发来的各种消息，同时原进程继续等待新客户端的连接

```java
ServerThread st = new ServerThread(Server.this, s, client_id);
threads.put(client_id++, st);
new Thread(st).start();
```

新线程的执行流程如下：
```java
public void run(){
        try{
            while(true){
                AbstractMessage message = (AbstractMessage)this.in.readObject();
                this.handleMsg(message);
            }
        }catch(Exception e){
            server.print(this.Clientname+" disconnected from the server.");
            release();
        }
    }
```

```java
public void handleMsg(AbstractMessage message){
        switch(message.getType()){
            case MSG_LOGIN:handleLoginMsg((LoginMessage)message);break;
            case MSG_CREATURE_MOVE:handleCreatureMoveMsg((CreatureMoveMsg)message);break;
            case MSG_BULLET_GEN:handleBulletCreateMsg((BulletGenMessage)message);break;
            default:break;
        }      
    }
```

​	服务器与客户端的通信通过AbstractMessage类的序列化和反序列化来完成，发送方想要发送的信息序列化为字节序列，接收方收到后，将其反序列化为对象，这样就实现了服务器与客户端的网络通信。 具体细节与Client几乎一致。



### 游戏匹配

​	服务器的游戏匹配按照“先来先服务”的原则，当服务器有客户端连接后，会判断当前服务器匹配池的人数。如果匹配池中没有人的话，则将当前客户端加入到匹配池中；如果匹配池中已经存在玩家，则将该名玩家从匹配池中删除，与最近连接的客户端进行匹配并随机为双方分配阵营。
匹配成功后，玩家进入读秒状态，服务器固定初始化战场数据。3秒后游戏开始，玩家便可开始操作，进行畅快淋漓的战斗。



### 子弹同步

​	由于子弹是游戏核心的伤害元素，因此子弹在两客户端的同步性极大影响了游戏体验。为了最大可能保持同步，我们采取了一种子弹同步机制：每过一定的时间（在本游戏中为10ms），服务器就会根据子弹的方向与速度计算出子弹在当前时间所处的位置，计算完毕后，先检测子弹是否命中敌方英雄或飞出边界，并发送相应的报文，之后便将当前子弹的位置信息分别发送给两个客户端。

```java
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
        Thread.sleep(Common.LOGIC_FRAME_TIME);
        } catch (Exception e) {
            e.printStackTrace();
            }
                }
```









​    