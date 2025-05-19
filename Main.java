/* Tiny Shooter v3 – pixel‑art sprites
 *
 *  ┌ Compile ┐   javac -classpath .;resources Main.java      (Windows)
 *              javac -classpath .:resources Main.java        (macOS / Linux)
 *
 *  ┌  Run  ┐  java  -classpath .;resources Main              (Windows)
 *           java  -classpath .:resources Main                (macOS / Linux)
 */
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;
import javax.imageio.ImageIO;

/* ────────────────────────── ENTRY ────────────────────────── */
public class Main {
    public static void main(String[] args) {
        new Game();                         // start the game
    }
}

/* ────────────────────────── GAME ─────────────────────────── */
class Game extends JFrame implements KeyListener, MouseListener {

    /* ---------- arena ---------- */
    static final int CELL = 10;            // logical step
    static final int SIZE = 500;           // window

    /* ---------- collections ---------- */
    final List<Enemy>  enemies = Collections.synchronizedList(new ArrayList<>());
    final List<Friend> friends = Collections.synchronizedList(new ArrayList<>());
    final List<Bullet> bullets = Collections.synchronizedList(new ArrayList<>());

    final Random R = new Random();
    final AirCraft aircraft = new AirCraft();

    Game() {
        setTitle("Tiny Shooter – sprites");
        setSize(SIZE, SIZE);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        addKeyListener(this);
        addMouseListener(this);
        add(new Board());
        setResizable(false);
        setVisible(true);

        /* spawn */
        for (int i = 0; i < 10; i++) new Enemy().start();
        for (int i = 0; i < 10; i++) new Friend().start();
        aircraft.start();
    }

    /* ─────────────── rendering panel ─────────────── */
    class Board extends JPanel {
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            /* → keep pixel art crisp */
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

            g.setColor(Color.WHITE);
            g.fillRect(0, 0, SIZE, SIZE);

            drawSprite(g2, aircraft);

            synchronized (enemies) { for (Enemy e : enemies)  drawSprite(g2, e); }
            synchronized (friends) { for (Friend f : friends) drawSprite(g2, f); }

            synchronized (bullets) {
                for (Bullet b : bullets) {
                    g.setColor(b.color());
                    g.fillRect(b.x, b.y, 4, 4);
                }
            }
        }

        void drawSprite(Graphics g2, Sprite s) {
            BufferedImage img = s.sprite();
            /* center 16 px sprite on 10 × 10 cell */
            g2.drawImage(img, s.x - 3, s.y - 3, 16, 16, null);
        }
    }

    /* ───────────── input ───────────── */
    @Override public void keyPressed(KeyEvent e) {
        int nx = aircraft.x, ny = aircraft.y;
        switch (e.getKeyCode()) {
            case KeyEvent.VK_W -> ny -= CELL;
            case KeyEvent.VK_S -> ny += CELL;
            case KeyEvent.VK_A -> nx -= CELL;
            case KeyEvent.VK_D -> nx += CELL;
        }
        if (inBounds(nx, ny)) { aircraft.x = nx; aircraft.y = ny; repaintLater(); }
    }
    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e)    {}

    @Override public void mouseClicked(MouseEvent e) { fireBullets(aircraft, 1); }
    @Override public void mousePressed(MouseEvent e)  {}
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e)  {}
    @Override public void mouseExited(MouseEvent e)   {}

    /* ───────────── helpers ───────────── */
    static boolean inBounds(int x,int y){ return x>=0&&y>=0&&x<=SIZE-CELL&&y<=SIZE-CELL; }
    void repaintLater() { SwingUtilities.invokeLater(this::repaint); }
    static void snooze(long ms){ try{Thread.sleep(ms);}catch(InterruptedException ignored){} }

    void fireBullets(Sprite shooter,int side){
        int x = shooter.x, y = shooter.y;
        Bullet r=new Bullet(x+CELL,y+2,"right",side);
        Bullet l=new Bullet(x-5  ,y+2,"left" ,side);
        bullets.add(r); bullets.add(l); r.start(); l.start();
    }
    void win()  { JOptionPane.showMessageDialog(this,"Kazandınız!"); System.exit(0); }
    void lose() { JOptionPane.showMessageDialog(this,"Kaybettiniz."); System.exit(0); }

    /* ──────────────────────────────────── SPRITES ──────────────────────────────────── */
    enum Dir { LEFT, RIGHT }

    abstract class Sprite extends Thread {
        int x,y; Dir facing = Dir.RIGHT; int anim = 0;   // anim 0|1
        Rectangle box(){return new Rectangle(x,y,CELL,CELL);}
        boolean move(int dx,int dy){
            int nx=x+dx, ny=y+dy;
            if(inBounds(nx,ny)){
                if(dx>0) facing=Dir.RIGHT; else if(dx<0) facing=Dir.LEFT;
                if(dx!=0||dy!=0) anim ^= 1;
                x=nx; y=ny; return true;
            } return false;
        }
        abstract BufferedImage sprite();
    }

    /* ---------- Enemy ---------- */
    public class Enemy extends Sprite {
        Enemy(){ x=R.nextInt(50)*CELL; y=R.nextInt(50)*CELL; }
        @Override public void run(){
            enemies.add(this);
            int tick=0;
            while(!isInterrupted()){
                decideMove();
                if(box().intersects(aircraft.box())) lose();

                if(++tick==2){                          // every 1 s
                    boolean lined = clearShot(aircraft.box());
                    boolean shoot = lined || R.nextInt(4)==0;     // 25 % blind
                    if(shoot && enemies.contains(this)) fireBullets(this,-1);
                    tick=0;
                }
                repaintLater(); snooze(500);
            }
            enemies.remove(this);
        }
        /* AI */
        void decideMove(){
            if(incomingBullet(1)){ move(0,R.nextBoolean()?CELL:-CELL); return; }
            int dy=Integer.compare(aircraft.y,y)*CELL;
            if(!move(0,dy)) move(R.nextBoolean()?CELL:-CELL,0);
        }
        boolean incomingBullet(int hostile){
            synchronized(bullets){
                for(Bullet b:bullets)
                    if(b.side==hostile && b.y==y &&
                            ((b.dirRight()&&b.x<x)||(!b.dirRight()&&b.x>x)))
                        return true;
            }return false;
        }
        boolean clearShot(Rectangle tgt){
            if(tgt.y!=y) return false;
            int dir=tgt.x>x?1:-1, cx=x+CELL*dir;
            while(cx!=tgt.x){
                synchronized(enemies){for(Enemy e:enemies) if(e!=this&&e.x==cx&&e.y==y) return false;}
                synchronized(friends){for(Friend f:friends) if(f.x==cx&&f.y==y) return false;}
                cx+=CELL*dir;
            }
            return true;
        }
        @Override BufferedImage sprite(){
            return Assets.enemy(facing,anim);
        }
    }

    /* ---------- Friend ---------- */
    public class Friend extends Sprite{
        Friend(){ x=R.nextInt(50)*CELL; y=R.nextInt(50)*CELL; }
        @Override public void run(){
            friends.add(this);
            int tick=0;
            while(!isInterrupted()){
                decideMove();
                if(++tick==2){
                    boolean lined=clearShotNearestEnemy();
                    boolean shoot=lined||R.nextInt(4)==0;
                    if(shoot && friends.contains(this)) fireBullets(this,0);
                    tick=0;
                }
                repaintLater(); snooze(500);
            }
            friends.remove(this);
        }
        void decideMove(){
            if(incomingBullet(-1)){ move(0,R.nextBoolean()?CELL:-CELL); return; }
            Enemy ne=nearestEnemy();
            if(ne!=null && ne.y==y) move(0, (ne.y>SIZE/2?-CELL:CELL));
            else move(R.nextBoolean()?CELL:-CELL,0);
        }
        Enemy nearestEnemy(){
            Enemy best=null; int d=Integer.MAX_VALUE;
            synchronized(enemies){
                for(Enemy e:enemies){
                    int dist=Math.abs(e.x-x)+Math.abs(e.y-y);
                    if(dist<d){d=dist;best=e;}
                }
            }return best;
        }
        boolean incomingBullet(int hostile){
            synchronized(bullets){
                for(Bullet b:bullets)
                    if(b.side==hostile && b.y==y &&
                            ((b.dirRight()&&b.x<x)||(!b.dirRight()&&b.x>x)))
                        return true;
            }return false;
        }
        boolean clearShotNearestEnemy(){
            Enemy e=nearestEnemy(); return e!=null && clearShot(e.box());
        }
        boolean clearShot(Rectangle tgt){
            if(tgt.y!=y) return false;
            int dir=tgt.x>x?1:-1, cx=x+CELL*dir;
            while(cx!=tgt.x){
                synchronized(friends){for(Friend f:friends) if(f!=this&&f.x==cx&&f.y==y) return false;}
                cx+=CELL*dir;
            }
            return true;
        }
        @Override BufferedImage sprite(){
            return Assets.friend(facing,anim);
        }
    }

    /* ---------- Player ---------- */
    public class AirCraft extends Sprite{
        AirCraft(){ x=SIZE/2; y=SIZE/2; }
        @Override public void run(){
            while(!isInterrupted()){
                if(enemies.isEmpty()) win();
                if(!alive()) break;
                snooze(100);
            }lose();
        }
        boolean alive(){
            synchronized(enemies){ for(Enemy e:enemies) if(box().intersects(e.box())) return false; }
            synchronized(bullets){
                for(Bullet b:bullets)
                    if(b.side==-1 && box().intersects(b.box())) return false;
            }
            return true;
        }
        @Override BufferedImage sprite(){
            return Assets.player(facing,anim);
        }
    }

    /* ---------- Bullet ---------- */
    public class Bullet extends Thread{
        int x,y,side; String dir;
        Bullet(int x,int y,String d,int s){this.x=x;this.y=y;this.dir=d;this.side=s;}
        Rectangle box(){return new Rectangle(x,y,4,4);}
        Color color(){return switch(side){case -1->Color.BLUE;case 0->Color.MAGENTA;default->Color.ORANGE;};}
        boolean dirRight(){return "right".equals(dir);}
        @Override public void run(){
            while(!isInterrupted() && inBounds(x,y)){
                x += dirRight()?4:-4;
                checkHits(); repaintLater(); snooze(50);
            } bullets.remove(this);
        }
        void checkHits(){
            if(side>=0){ // harms enemies
                synchronized(enemies){ enemies.removeIf(e->box().intersects(e.box())); }
            }else{       // harms friends & player
                synchronized(friends){ friends.removeIf(f->box().intersects(f.box())); }
                if(side==-1 && box().intersects(aircraft.box())) lose();
            }
        }
    }
}

/* ──────────────────────── ASSET LOADER ─────────────────────── */
class Assets {
    private static BufferedImage ENEMY_R1, ENEMY_R2, ENEMY_L1, ENEMY_L2;
    private static BufferedImage FRIEND_R1, FRIEND_R2, FRIEND_L1, FRIEND_L2;
    private static BufferedImage PLAYER_R1, PLAYER_R2, PLAYER_L1, PLAYER_L2;

    static {
        try {
            ENEMY_R1  = load("/enemy_r1.png");  ENEMY_R2  = load("/enemy_r2.png");
            ENEMY_L1  = load("/enemy_l1.png");  ENEMY_L2  = load("/enemy_l2.png");
            FRIEND_R1 = load("/friend_r1.png"); FRIEND_R2 = load("/friend_r2.png");
            FRIEND_L1 = load("/friend_l1.png"); FRIEND_L2 = load("/friend_l2.png");
            PLAYER_R1 = load("/player_r1.png"); PLAYER_R2 = load("/player_r2.png");
            PLAYER_L1 = load("/player_l1.png"); PLAYER_L2 = load("/player_l2.png");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                    "Failed to load PNGs – put them in /resources and restart.\n" + e,
                    "Missing sprites", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }
    private static BufferedImage load(String path) throws Exception {
        return ImageIO.read(Objects.requireNonNull(Assets.class.getResource(path)));
    }
    /* helpers returned by Sprite */
    static BufferedImage enemy (Game.Dir d,int a){ return (d==Game.Dir.RIGHT)?(a==0?ENEMY_R1:ENEMY_R2):(a==0?ENEMY_L1:ENEMY_L2);}
    static BufferedImage friend(Game.Dir d,int a){ return (d==Game.Dir.RIGHT)?(a==0?FRIEND_R1:FRIEND_R2):(a==0?FRIEND_L1:FRIEND_L2);}
    static BufferedImage player(Game.Dir d,int a){ return (d==Game.Dir.RIGHT)?(a==0?PLAYER_R1:PLAYER_R2):(a==0?PLAYER_L1:PLAYER_L2);}
}
