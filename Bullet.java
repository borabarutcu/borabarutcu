import java.awt.*;

public class Bullet extends Thread{

    private int x,y;
    private String direction;
    private boolean enemy;

    Graphics2D g2;

    Bullet (int x, int y, String direction , boolean enemy) {
        this.x = x;
        this.y = y;
        this.direction = direction;
        int height = 5;
        int width = 5;
    }

    public Color bulletColor() {
        Color color = null;
        if (enemy)
            color = Color.BLUE;
        else
            color = Color.magenta;
        return color;
    }

    public void run(){
        while (true) {
            if (g2 != null) {
                g2.setColor(bulletColor());
                g2.fillRect(x,y,5,5);

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                if (direction == "right") {
                    x += 10;
                } else {
                    x -= 10;
                }
            }
        }
    }


}
/*
Oyuncu Mouse ile bir kez tıkladığında soluna ve sağına doğru aynı anda ateş edecektir. Ateş nesnesi turuncu
renkle 5x5’lik bir kare ile gösterilecektir. Ateş karesi 0.1 saniyede 10 pixel ilerleyecektir. Ateş nesnesi düşman
karelerine çarparsa onları yok edecektir. Dost karelere karşı etkisizdir.
 */