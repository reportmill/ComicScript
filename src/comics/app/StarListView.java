package comics.app;
import comics.script.*;
import snap.gfx.*;
import snap.view.*;
import java.util.*;

/**
 * A view to hold list of stars in current script (Camera, Actors, Setting).
 */
public class StarListView extends RowView {
    
    // The list of Stars
    List <Star>          _stars = new ArrayList();
    
    // The selected index
    int                  _selIndex = -1;
    
    // Images for Setting and Camera
    Image                _setImg = Image.get(getClass(), "Setting.png");
    Image                _camImg = Image.get(getClass(), "Camera.png");
    
    // Constants
    static Color    SELECT_COLOR = Color.get("#039ed3");
    static Effect   SELECT_EFFECT = new ShadowEffect(8, SELECT_COLOR, 0, 0);

/**
 * Creates a StarListView.
 */
public StarListView()
{
    setPadding(7,5,5,12); setSpacing(12); setGrowWidth(true);
    setFill(Color.WHITE); setBorder(Border.createLoweredBevelBorder());
    enableEvents(MousePress, Action);
}

/**
 * Returns a list of stars in this StarListView.
 */
public List <Star> getStars()  { return _stars; }

/**
 * Sets the list of stars in this StarListView.
 */
public void setStars(List <Star> theStars)
{
    // Clear children, stars
    removeChildren(); _stars.clear();

    // Iterate over stars
    for(Star star : theStars) {
        Image img = star instanceof Setting? _setImg : star instanceof CameraView? _camImg : star.getStarImage();
        StarView sview = new StarView(star, img);
        addChild(sview); _stars.add(star);
    }
}

/**
 * Returns the selected Subject index.
 */
public int getSelIndex()  { return _selIndex; }

/**
 * Selects the given Subject index.
 */
public void setSelIndex(int anIndex)
{
    // If already set, just return
    if(anIndex==_selIndex) return;
    
    // Set SelIndex and StarView effect
    StarView sv = getStarView(_selIndex); if(sv!=null) sv.setEffect(null);
    _selIndex = anIndex;
    StarView sv2 = getStarView(_selIndex); if(sv2!=null) sv2.setEffect(SELECT_EFFECT);
    repaint();
}

/**
 * Returns the selected StarV.
 */
public Star getSelStar()  { return getStar(_selIndex); }

/**
 * Selects the given Star.
 */
public void setSelStar(Star aStar)  { int ind = getStarIndex(aStar); setSelIndex(ind); }

/** Returns the Star at given index. */
protected Star getStar(int anIndex)  { return anIndex>=0? _stars.get(anIndex) : null; }

/** Returns the index of given Star. */
protected int getStarIndex(Star aStar)  { return _stars.indexOf(aStar); }

/** Returns the StarView at given index. */
protected StarView getStarView(int anIndex)  { return anIndex>=0? (StarView)getChild(anIndex) : null; }

/**
 * Override to handle events.
 */
protected void processEvent(ViewEvent anEvent)
{
    if(anEvent.isMousePress()) {
        setSelStar(null);
        fireActionEvent(anEvent);
    }
}
    
/**
 * A view to hold a Star.
 */
private class StarView extends ImageView {
    
    // Star
    Star   _star;
    
    /** Create an StarView. */
    public StarView(Star aStar, Image anImg)
    {
        super(anImg); _star = aStar;
        setPrefSize(64,64); setKeepAspect(true); setPadding(3,3,14,3);
        enableEvents(MouseEnter, MouseExit, MousePress);
    }
    
    /** Override to customize paint. */
    protected void paintFront(Painter aPntr)
    {
        double w = getWidth(), h = getHeight();
        RoundRect rrect = new RoundRect(0,0,w,h,7);
        aPntr.setColor(Color.WHITE); aPntr.fill(rrect);
        
        super.paintFront(aPntr);
        
        aPntr.setColor(isMouseOver() || getEffect()!=null? Color.BLUE : Color.GRAY); aPntr.draw(rrect);
        aPntr.clipRect(0,h-12,w,12); aPntr.fill(rrect);
        aPntr.setColor(Color.WHITE);
        String name = _star.getStarName(); Font font = Font.Arial10; aPntr.setFont(font);
        double sw = font.getStringAdvance(name);
        aPntr.drawString(name, Math.round((w-sw)/2), h-3);
    }
    
    /** Override to handle events. */
    protected void processEvent(ViewEvent anEvent)
    {
        if(anEvent.isMousePress()) {
            setSelStar(_star);
            StarListView.this.fireActionEvent(anEvent);
        }
        if(anEvent.isMouseEnter() || anEvent.isMouseExit()) repaint();
        anEvent.consume();
    }
}

}