import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;

public class Game  extends JFrame implements KeyListener, MouseListener {


    ArrayList<Enemy> enemies = new  ArrayList<Enemy>();
    ArrayList<Friend> friends = new  ArrayList<Friend>();
    ArrayList<Bullet> bullets = new  ArrayList<Bullet>();
    AirCraft aircraft = new AirCraft();


    Random random = new Random();
    Graphics2D g2 = null;

    int x = 250 ,y = 250;


    Game() {
        setSize(500,500);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        Board board = new Board();
        board.setSize(500,500);
        getContentPane().add(board);

        repaint();

        setVisible(true);

        addKeyListener(this);

        addMouseListener(this);
    }

    @Override
    public void mousePressed(MouseEvent e) {


    }

    @Override
    public void mouseClicked(MouseEvent e) {
        Bullet bullet1 = new Bullet(aircraft.x+10, aircraft.y+2, "right", 1 );
        Bullet bullet2 = new Bullet(aircraft.x-5, aircraft.y+2, "left", 1 );
        bullets.add(bullet1);
        bullets.add(bullet2);
        bullet1.start();
        bullet2.start();
    }

    @Override
    public void mouseReleased(MouseEvent e) {}
    @Override
    public void mouseEntered(MouseEvent e) {}
    @Override
    public void mouseExited(MouseEvent e) {}

    class Board extends JPanel{

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.setColor(Color.white);
            g.fillRect(0,0,getWidth(),getHeight());
            if (g2 == null) {
                g2 = (Graphics2D) g.create();
            }
            g.setColor(Color.white);
            g.fillRect(0,0,500,500);
            g.setColor(Color.red);
            g.fillRect(aircraft.x, aircraft.y, 10, 10);

            for (Enemy enemy: enemies) {
                g.setColor(Color.BLACK);
                g.fillRect(enemy.x, enemy.y, 10, 10);
            }
            for (Friend friend: friends) {
                g.setColor(Color.GREEN);
                g.fillRect(friend.x, friend.y, 10, 10);
            }
            try {
                for (Bullet bullet : bullets) {
                    if (bullet != null && bullet.bulletColor() != null) {
                        g.setColor(bullet.bulletColor());
                        g.fillRect(bullet.x, bullet.y, 5, 5);
                    }
                }
            } catch (Exception ignored ){
            }
        }

    }


    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        switch (key) {
            case KeyEvent.VK_W: {
                if (aircraft.y>=10)
                    aircraft.y -= 10;
                break;
            }
            case KeyEvent.VK_A: {
                if (aircraft.x>=10)
                    aircraft.x -= 10;
                break;
            }
            case KeyEvent.VK_S: {
                if (aircraft.y<490)
                    aircraft.y += 10;
                break;
            }
            case KeyEvent.VK_D: {
                if (aircraft.x<490)
                    aircraft.x += 10;
                break;
            }
        }
        repaint();
    }

    @Override
    public void keyTyped(KeyEvent e) {}
    @Override
    public void keyReleased(KeyEvent e) {}

    public class Enemy extends Thread{
        int x , y;
        Enemy() {
            x = random.nextInt(50)*10;
            y = random.nextInt(50)*10;
            if (g2 != null) {
                g2.setColor(Color.BLACK);
                g2.fillRect(x,y,10,10);
            }
            enemies.add(this);
        }
        @Override
        public synchronized void run() {
            int i = 0;
            while (alive()) {

                int move = random.nextInt(2)*2-1;
                int direction = random.nextInt(2);

                if (x == 0 && y == 0) {
                    if (direction == 0) {
                        x += 10;
                    } else {
                        y += 10;
                    }
                } else if (x == 490 && y == 490) {
                    if (direction == 0) {
                        x -= 10;
                    } else {
                        y -= 10;
                    }
                } else if (x == 0 || y == 0 || x == 490 || y == 490 ) {
                    if (x == 0) {
                        if (direction == 0) {
                            x += 10 ;
                        } else {
                            y += 10*move ;
                        }
                    } else if (y == 0) {
                        if (direction == 0) {
                            x += 10*move ;
                        } else {
                            y += 10 ;
                        }
                    } else if (x == 490) {
                        if (direction == 0) {
                            x -= 10 ;
                        } else {
                            y += 10*move ;
                        }
                    } else if (y == 490) {
                        if (direction == 0) {
                            x += 10*move ;
                        } else {
                            y -= 10 ;
                        }
                    }
                } else {
                    if (direction == 0) {
                        x += 10*move ;
                    } else {
                        y += 10*move ;
                    }
                }

                if (aircraft.x == x && aircraft.y == y) {
                    stopAll();
                    JOptionPane.showMessageDialog(null, "Oyunu kaybettiniz.");
                    break;
                }

                int n = 0;
                for (Friend friend : friends) {
                    if (friend.x == x && friend.y == y) {
                        friend.interrupt();
                        friends.remove(friend);
                        enemies.remove(this);
                        n++;
                    }
                }
                if (n>0) {
                    break;
                }
                /*for (Bullet bullet : bullets) {
                    if (bullet.color.equals("purple") || bullet.color.equals("orange")) {
                        if (bullet.hitSomeone(Game.this)) {
                            bullets.remove(bullet);
                            enemies.remove(this);
                            n++;
                        }
                    }
                }*/

                if (this.gotShot()){
                    this.interrupt();
                    enemies.remove(this);
                    break;
                }
                repaint();
                i++;
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ignored) {

                }
                if (i == 2) {
                    System.out.println("shoot bullet by enemy");
                    Bullet bullet1 = new Bullet(x+10 ,y+2, "right", -1);//sağa
                    Bullet bullet2 = new Bullet(x-5 ,y+2, "left" , -1);//sola
                    bullets.add(bullet1);
                    bullets.add(bullet2);
                    bullet1.start();
                    bullet2.start();
                    i -= 2;
                }
            }
        }
        public boolean alive() {
            for (Friend friend : friends) {
                if (friend.x == x && friend.y == y) {
                    return false;
                }
            }
            for (Bullet bullet : bullets ) {
                if (Objects.equals(bullet.color,"purple") ||Objects.equals(bullet.color,"orange")){
                    if (bullet.hitSomeone(Game.this)) {
                        return false;
                    }
                }
            }
            return true;
        }
        public boolean gotShot(){
            for (Bullet bullet : bullets){
                if (bullet != null) {
                    if (Objects.equals(bullet.color,"purple") || Objects.equals(bullet.color,"orange")) {
                        if (bullet.y + 2 == y && bullet.x < x && bullet.x > x + 10) {
                            bullets.remove(bullet);
                            return true;
                        }
                    }
                }
            }
            return false;
        }
    }

    public class Friend extends Thread{
        int x, y ;
        String type = "Friend";
        Friend() {
            x = random.nextInt(50)*10;
            y = random.nextInt(50)*10;
            if (g2 != null) {
                g2.setColor(Color.GREEN);
                g2.fillRect(x,y,10,10);
            }
            friends.add(this);
        }
        @Override
        public synchronized void run() {
            int i = 0;
            while (alive()) {
                int move = random.nextInt(2)*2-1;
                int direction = random.nextInt(2);

                if (x == 0 && y == 0) {
                    if (direction == 0) {
                        x += 10;
                    } else {
                        y += 10;
                    }
                } else if (x == 490 && y == 490) {
                    if (direction == 0) {
                        x -= 10;
                    } else {
                        y -= 10;
                    }
                } else if (x == 0 || y == 0 || x == 490 || y == 490 ) {
                    if (x == 0) {
                        if (direction == 0) {
                            x += 10 ;
                        } else {
                            y += 10*move ;
                        }
                    } else if (y == 0) {
                        if (direction == 0) {
                            x += 10*move ;
                        } else {
                            y += 10 ;
                        }
                    } else if (x == 490) {
                        if (direction == 0) {
                            x -= 10 ;
                        } else {
                            y += 10*move ;
                        }
                    } else if (y == 490) {
                        if (direction == 0) {
                            x += 10*move ;
                        } else {
                            y -= 10 ;
                        }
                    }
                } else {
                    if (direction == 0) {
                        x += 10*move ;
                    } else {
                        y += 10*move ;
                    }
                }
                int n = 0;
                for (Enemy enemy : enemies) {
                    if (enemy.x == x && enemy.y == y) {
                        enemy.interrupt();
                        enemies.remove(enemy);
                        friends.remove(this);
                        n++;
                    }
                }

                if (i == 2) {
                    System.out.println("shoot bullet by friend");
                    Bullet bullet1 = new Bullet(x+10 ,y+2, "right", 0);//sağa
                    Bullet bullet2 = new Bullet(x-5 ,y+2, "left" , 0);//sola
                    bullets.add(bullet1);
                    bullets.add(bullet2);
                    bullet1.start();
                    bullet2.start();
                    i -= 2;
                }
                for (Bullet bullet : bullets) {
                    if (Objects.equals(bullet.color,"blue")) {
                        if (bullet.hitSomeone(Game.this)) {
                            bullets.remove(bullet);
                            n++;
                            g2.setColor(Color.white);
                            g2.fillRect(x, y, 10, 10);
                            friends.remove(this);
                        }
                    }
                }
                if (n>0)
                    break;
                repaint();
                i++;
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ignored) {
                }
            }
        }
        public boolean alive() {
            for (Enemy enem : enemies) {
                if (enem.x == x && enem.y == y) {
                    enemies.remove(enem);
                    return false;
                }
            }
            for (Bullet bullet : bullets ) {
                if (Objects.equals(bullet.color,"blue")){
                    if (bullet.hitSomeone(Game.this)) {
                        return false;
                    }
                }
            }
            return true;
        }
        public boolean gotShot(){
            for (Bullet bullet : bullets){
                if (bullet != null) {
                    if (bullet.color.equals("blue")) {
                        if (bullet.y == y - 2 && bullet.x < x && bullet.x > x + 10) {
                            return true;
                        }
                    }
                }
            }
            return false;
        }
    }
    public class AirCraft extends Thread{

        int x, y;

        AirCraft(){
            x = 250;
            y = 250;
            if (g2 != null) {
                g2.setColor(Color.RED);
                g2.fillRect(x,y,10,10);
            }
        }
        @Override
        public void run() {
            while (alive()) {
                alive();
                if (enemies.size() == 0){
                    JOptionPane.showMessageDialog(null, "Oyunu kazandınız.");

                }
            }
            stopAll();
        }
        public boolean alive() {
            for (Enemy enem : enemies) {
                if (enem.x == x && enem.y == y) {
                    stopAll();
                    return false;
                }
            }
            for (Bullet bullet : bullets ) {
                if (Objects.equals(bullet.color,"blue")){
                    if (bullet.hitSomeone(Game.this)) {
                        stopAll();
                        return false;
                    }
                }
            }
            return true;
        }
    }
    public class Bullet extends Thread{
        private int x,y;
        private String direction, color;
        private int enemy;
        Graphics2D g2;
        Bullet (int x, int y, String direction , int enemy) {
            this.x = x;
            this.y = y;
            this.direction = direction;
            this.enemy = enemy;
            if (g2 != null) {
                if (enemy == -1) {
                    g2.setColor(Color.blue);
                    color = "blue";
                } else if (enemy == 0) {
                    g2.setColor(Color.magenta);
                    color = "purple";
                } else {
                    g2.setColor(Color.orange);
                    color = "orange";
                }
                g2.fillRect(x, y, 5, 5);
            }
            if (enemy == -1) {
                color = "blue";
            } else if (enemy == 0) {
                color = "purple";
            } else {
                color = "orange";
            }
        }

        public Color bulletColor() {
            Color color = null;
            if (enemy == -1)
                color = Color.BLUE;
            else if (enemy == 0)
                color = Color.magenta;
            else
                color = Color.orange;
            return color;
        }

        public synchronized void run(){
            while ( x>0 && x<495 && y>0 && y<495) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ignored) {

                }
                if (Objects.equals(direction, "right")) {
                    x += 10;
                } else {
                    x -= 10;
                }
                if (Objects.equals(this.color,"purple") || Objects.equals(this.color,"orange")) {
                    for (Enemy enemy : enemies) {
                        if (this.x >= enemy.x && this.x <= enemy.x + 10 && this.y >= enemy.y && this.y <= enemy.y + 10) {
                            enemy.interrupt();
                            enemy = null;
                            enemies.remove(enemy);
                            return;
                        }
                    }
                }
                if (Objects.equals(this.color, "blue")) {
                    for (Friend friend : friends) {
                        if (this.x >= friend.x && this.x <= friend.x + 10 && this.y >= friend.y && this.y <= friend.y + 10) {
                            friend.interrupt();
                            friend = null;
                            friends.remove(friend);
                            return;
                        }
                    }

                }
                repaint();
            }
            this.interrupt();
            bullets.remove(this);
        }

        public boolean hitSomeone(Game object){
            return object != null && (object.x == x && object.y+2 == y  || object.x + 5 == x && object.y+2 == y );
        }

    }

    public void stopAll(){
        for (Enemy enemy : enemies) {
            try{
            enemy.interrupt();}catch(Exception ignored){}
        }
        for (Friend friend : friends) {
            try{
            friend.interrupt();}catch(Exception ignored){}
        }
        for (Bullet bullet : bullets) {
            try{
            bullet.interrupt();}catch(Exception ignored){}
        }
        JOptionPane.showMessageDialog(null, "Oyunu kaybettiniz.");
    }
}
