package comics;
import java.util.Random;
import snap.gfx.*;
import snap.view.*;

/**
 * A demo to show explosions.
 * 
 * Things to do:
 *    - Change rotation animation to be zero
 *    - Declare variable for PIECE_COUNT and replace occurances of constant "10"
 *    - Add Slider with bounds 220,220,220,20
 */
public class Explode extends ViewOwner {
    
    // The main view
    ChildView      _view;
    
    // The random number generator
    static Random _rand = new Random();
    
protected View createUI()
{
    _view = new ChildView(); _view.setPrefSize(800,600);
    addButton(250,250); addButton(100,100); addButton(500,100);
    addButton(100,400); addButton(500,400);
    return _view;
}

protected void respondUI(ViewEvent anEvent)
{
    if(anEvent.equals("Button"))
        explode(anEvent.getView(),0);
}

void addButton(double aX, double aY)
{
    Button btn = new Button("Hello World"); btn.setBounds(aX, aY, 200, 80); btn.setName("Button");
    btn.setFont(Font.Arial14.deriveFont(20));
    _view.addChild(btn);
}

public static void explode(View aView, int aStart)
{
    aView.setOpacity(1);
     
    // Create explode pieces
    for(int i=0;i<10;i++)
        for(int j=0;j<10;j++)
            explodePiece(aView, i, j, aStart);
    
    // Hide view and make visible after delay
    aView.setOpacity(0);
    //aView.getAnim(aStart).getAnim(aStart+2000).getAnim(aStart+2800).setOpacity(1).play();
}

public static void explodePiece(View aView, int aRow, int aCol, int aStart)
{
    // Get bounds of piece
    int w = (int)aView.getWidth()/10;
    int h = (int)aView.getHeight()/10;
    double dx = aRow*w;
    double dy = aCol*h;
    double x = aView.getX() + dx;
    double y = aView.getY() + dy;

    // Create image for piece
    Image img = Image.get(w, h, true);
    Painter pntr = img.getPainter();
    pntr.translate(-dx, -dy);
    ViewUtils.paintAll(aView, pntr);
    
    // Create ImageView for piece and add
    ImageView iview = new ImageView(img); iview.setSize(iview.getBestSize());
    iview.setXY(x, y); iview.setPickable(false);
    ChildView view = (ChildView)aView.getParent();
    view.addChild(iview);
    
    // Create random destination for piece
    double angle = Math.toRadians(_rand.nextDouble()*360);
    double dist = 100 + _rand.nextDouble()*200;
    double nx = x + dist*Math.sin(angle), ny = y + dist*Math.cos(angle);
    
    // Create random rotation and duration of piece
    double rot = _rand.nextDouble()*720 - 360;
    int time = 2000 + _rand.nextInt(1000);
    
    // Configure animation for piece and play
    ViewAnim anim = iview.getAnim(aStart).getAnim(aStart+time);
    anim.setX(nx).setY(ny).setRotate(rot).setOpacity(0).startFast();
    anim.setOnFinish(a -> view.removeChild(iview)).play();
}

public static void main(String args[])
{
    new Explode().setWindowVisible(true);
}

}