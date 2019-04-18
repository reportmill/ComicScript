package comics.app;
import comics.script.*;
import snap.gfx.*;
import snap.view.*;
import java.util.*;

/**
 * A view to hold list of stars in current script (Camera, Actors, Setting).
 */
public class StarListView extends ParentView {
    
    // The EditorPane
    ScriptLineEditor     _lineEditor;

    // The Player
    PlayerView           _player;
    
    // The list of Stars
    List <Star>          _stars = new ArrayList();
    
    // The selected index
    int                  _selIndex = -1;
    
    //
    Image                _camImg = Image.get(getClass(), "Camera.png");
    Image                _setImg = Image.get(getClass(), "Setting.png");
    
    // Constants
    int SPACING = 12;
    static Color    SELECT_COLOR = Color.get("#039ed3");
    static Effect SELECT_EFFECT = new ShadowEffect(8, SELECT_COLOR, 0, 0);

/**
 * Creates a StarListView.
 */
public StarListView(ScriptLineEditor aLE)
{
    _lineEditor = aLE;
    _player = aLE._editorPane._player;
    setGrowWidth(true);
    setBorder(Border.createLoweredBevelBorder());
    setPadding(7,5,5,12);
    setFill(Color.WHITE);
    enableEvents(MousePress);
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
    StarView sv = getSelStarView(); if(sv!=null) sv.setEffect(null);
    _selIndex = anIndex;
    StarView sv2 = getSelStarView(); if(sv2!=null) sv2.setEffect(SELECT_EFFECT);
    
    // Reset LineEditor UI
    _lineEditor.resetLater();
}

/**
 * Returns the selected StarV.
 */
public Star getSelStar()  { return getStar(_selIndex); }

/**
 * Selects the given Star.
 */
public void setSelStar(Star aStar)  { int ind = getStarIndex(aStar); setSelIndex(ind); }

/**
 * Returns the Star at given index.
 */
protected Star getStar(int anIndex)  { return anIndex>=0? _stars.get(anIndex) : null; }

/**
 * Returns the index of given Star.
 */
protected int getStarIndex(Star aStar)  { return _stars.indexOf(aStar); }

/**
 * Returns the StarView at given index.
 */
protected StarView getStarView(int anIndex)  { return anIndex>=0? (StarView)getChild(anIndex) : null; }

/**
 * Returns the selected StarView.
 */
protected StarView getSelStarView()  { return getStarView(_selIndex); }

/**
 * Updates the list of Subjects.
 */
public void updateSubjects()
{
    Script script = _player.getScript();
    removeChildren(); _stars.clear();
    
    StarView set = new StarView(script.getSetting(), _setImg);
    addChild(set); _stars.add(set.getStar());
    StarView cam = new StarView(_player.getCamera(), _camImg);
    addChild(cam); _stars.add(cam.getStar());
   
    // Iterate over lines
    for(ScriptLine sline : script.getLines()) {
        Star star = sline.getStar(); if(star==null) continue;
        if(_stars.contains(star)) continue;
        
        StarView sview = new StarView(star, star.getStarImage());
        addChild(sview); _stars.add(star);
    }
}

/**
 * Returns the preferred width.
 */
protected double getPrefWidthImpl(double aH)  { return RowView.getPrefWidth(this, null, SPACING, aH); }

/**
 * Returns the preferred height.
 */
protected double getPrefHeightImpl(double aW)  { return RowView.getPrefHeight(this, null, aW); }

/**
 * Layout children.
 */
protected void layoutImpl()  { RowView.layout(this, null, null, false, SPACING); }

/**
 * Override to handle events.
 */
protected void processEvent(ViewEvent anEvent)
{
    if(anEvent.isMousePress())
        setSelStar(null);
}
    
/**
 * A view to hold a Star.
 */
public class StarView extends ImageView {
    
    // Star
    Star   _star;
    
    /** Create an StarView. */
    public StarView(Star aStar)  { this(aStar, aStar.getStarImage()); }
    
    /** Create an StarView. */
    public StarView(Star aStar, Image anImg)
    {
        super(anImg); _star = aStar;
        setPrefSize(64,64); setKeepAspect(true); setPadding(3,3,14,3);
        enableEvents(MouseEnter, MouseExit, MousePress);
    }
    
    /** Returns the star. */
    public Star getStar()  { return _star; }
    
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
        if(anEvent.isMousePress())
            setSelStar(_star);
        if(anEvent.isMouseEnter() || anEvent.isMouseExit()) repaint();
        anEvent.consume();
    }
    
    /** Override to fix paint problem. */
    public void setEffect(Effect anEff)  { super.setEffect(anEff); repaint(-10,-10,getWidth()+20,getHeight()+20); }
}

}