package comics.gfx;
import java.util.*;
import snap.gfx.*;
import snap.util.PropChangeListener;
import snap.view.*;

/**
 * A demo to show explosions.
 * 
 * Things to do:
 *    - Change rotation animation to be zero
 *    - Declare variable for PIECE_COUNT and replace occurances of constant "10"
 *    - Add Slider with bounds 220,220,220,20
 */
public class Explode {
    
    // The random number generator
    static Random _rand = new Random();
    
/**
 * Configures the given view to explode.
 */
public static void explode(View aView, int aStart)
{
    if(aView instanceof ImageView && !((ImageView)aView).getImage().isLoaded()) {
        System.out.println("Image not loaded"); return; }
    if(aView.getWidth()==0) { System.out.println("Image width zero"); return; }
     
    // Create explode pieces
    List <View> pieces = new ArrayList(100);
    for(int i=0;i<10;i++) for(int j=0;j<10;j++)
        pieces.add(explodePiece(aView, i, j, aStart));
    
    // Hide view and make visible after delay
    aView.setOpacity(0);
    aView.addPropChangeListener(PropChangeListener.getOneShot(pc -> removePieces(pieces)));
    aView.getAnim(aStart+3000).setOnFinish(a -> removePieces(pieces));
}

public static View explodePiece(View aView, int aRow, int aCol, int aStart)
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
    //anim.setOnFinish(a -> view.removeChild(iview));
    return iview;
}

static void removePieces(List <View> thePieces)
{
    for(View v : thePieces)
        ((ChildView)v.getParent()).removeChild(v);
    thePieces.clear();
}

}