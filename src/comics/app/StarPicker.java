package comics.app;
import comics.player.*;
import java.util.*;
import snap.gfx.*;
import snap.util.FilePathUtils;
import snap.util.StringUtils;
import snap.view.*;

/**
 * A class to provide UI for selecting the Star of a ScriptLine.
 */
public class StarPicker extends ViewOwner {
    
    // The LineEditor
    LineEditor            _lineEditor;

    // The view to show list of stars in script
    StarListView          _starsView;
    
    // The stars browser
    BrowserView <String>  _starsBrowser;
    
    // Constants
    static Color    SELECT_COLOR = Color.get("#039ed3");
    static Effect   SELECT_EFFECT = new ShadowEffect(8, SELECT_COLOR, 0, 0);
    
/**
 * Creates StarPicker.
 */
public StarPicker(LineEditor aLE)
{
    _lineEditor = aLE;
}

/**
 * Returns the current ScriptLine.
 */
public ScriptLine getSelLine()  { return _lineEditor.getSelLine(); }

/**
 * Returns the Star for current ScriptLine.
 */
public Star getStar()
{
    ScriptLine line = getSelLine();
    return line.getStar();
}

/**
 * Called when Script text changes.
 */
protected void scriptChanged()
{
    if(!isUISet()) return;
    setStarsForPlayer(_lineEditor.getPlayer());
    resetLater();
}

/**
 * Creates UI.
 */
protected View createUI()
{
    // Get MainColView from UI file
    ColView mainColView =  (ColView)super.createUI(); mainColView.setFillWidth(true);

    // Create/add StarListView
    _starsView = new StarListView();
    mainColView.addChild(_starsView,1);
    
    // Return MainColView
    return mainColView;
}

/**
 * Initialize UI.
 */
protected void initUI()
{
    // Configure StarsView
    setStarsForPlayer(_lineEditor.getPlayer());
    
    // Get StarsBrowser
    _starsBrowser = getView("StarsBrowser", BrowserView.class); _starsBrowser.setRowHeight(24);
    _starsBrowser.setResolver(new StarTreeResolver());
    _starsBrowser.setItems(AssetIndex.get().getDirPaths("/"));
}

/**
 * Resets the UI.
 */
protected void resetUI()
{
    // Get current ScriptLine and Star
    ScriptLine line = getSelLine();
    Star star = line!=null? line.getStar() : null;
    
    // Update StarListView
    _starsView.setSelStar(star);
}

/**
 * Responds to the UI.
 */
protected void respondUI(ViewEvent anEvent)
{
    // Get current ScriptLine
    ScriptLine line = getSelLine();
    
    // Handle StarListView
    if(anEvent.equals(_starsView)) {
        Star star = _starsView.getSelStar();
        line.setStar(star);
        _lineEditor._editorPane.runCurrentLine();
    }
}

/**
 * Returns a list of stars for a PlayerView.
 */
protected List <Star> getStarsForPlayer(PlayerView aPlayer)
{
    Script script = aPlayer.getScript();
    List <Star> stars = new ArrayList();
    
    Star set = script.getStage(); stars.add(set);
    Star cam = aPlayer.getCamera(); stars.add(cam);
   
    // Iterate over lines
    for(ScriptLine sline : script.getLines()) {
        Star star = sline.getStar(); if(star==null) continue;
        if(!stars.contains(star)) stars.add(star);
    }
    
    // Returns stars
    return stars;
}

/**
 * Sets the list of stars for given Player.
 */
protected void setStarsForPlayer(PlayerView aPlayer)
{
    List <Star> stars = getStarsForPlayer(aPlayer);
    _starsView.setStars(stars);
}

/**
 * A view to hold list of stars in current script (Camera, Actors, Setting).
 */
public class StarListView extends RowView {
    
    // The list of Stars
    List <Star>   _stars = new ArrayList();
    
    // The selected index
    int           _selIndex = -1;
    
    // Images for Setting and Camera
    Image         _setImg = Image.get(getClass(), "Setting.png");
    Image         _camImg = Image.get(getClass(), "Camera.png");
    
    /** Creates a StarListView. */
    public StarListView()
    {
        setPadding(7,5,5,12); setSpacing(12); setGrowWidth(true);
        setFill(Color.WHITE); setBorder(Border.createLoweredBevelBorder());
        enableEvents(MousePress, Action);
    }
    
    /** Returns a list of stars in this StarListView. */
    public List <Star> getStars()  { return _stars; }
    
    /** Sets the list of stars in this StarListView. */
    public void setStars(List <Star> theStars)
    {
        // If already set, just return
        if(theStars.equals(_stars)) return;
        
        // Clear children, stars
        Star selStar = getSelStar();
        removeChildren(); _stars.clear();
    
        // Iterate over stars
        for(Star star : theStars) {
            Image img = star instanceof StageView? _setImg : star instanceof CameraView? _camImg : star.getStarImage();
            StarView sview = new StarView(star, img);
            addChild(sview); _stars.add(star);
        }
        _selIndex = -1; setSelStar(selStar);
    }

    /** Returns the selected Subject index. */
    public int getSelIndex()  { return _selIndex; }
    
    /** Selects the given Subject index. */
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
    
    /** Returns the selected StarV. */
    public Star getSelStar()  { return getStar(_selIndex); }
    
    /** Selects the given Star. */
    public void setSelStar(Star aStar)  { int ind = getStarIndex(aStar); setSelIndex(ind); }
    
    /** Returns the Star at given index. */
    protected Star getStar(int anIndex)  { return anIndex>=0? _stars.get(anIndex) : null; }
    
    /** Returns the index of given Star. */
    protected int getStarIndex(Star aStar)  { return _stars.indexOf(aStar); }
    
    /** Returns the StarView at given index. */
    protected StarView getStarView(int anIndex)  { return anIndex>=0? (StarView)getChild(anIndex) : null; }
    
    /** Override to handle events. */
    protected void processEvent(ViewEvent anEvent)
    {
        if(anEvent.isMousePress()) {
            setSelStar(null);
            fireActionEvent(anEvent);
        }
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
            _starsView.setSelStar(_star);
            ViewUtils.fireActionEvent(_starsView, anEvent);
        }
        if(anEvent.isMouseEnter() || anEvent.isMouseExit()) repaint();
        anEvent.consume();
    }
}

/**
 * A TreeResolver for WebFile
 */
public static class StarTreeResolver extends TreeResolver <String> {

    /** Returns the parent of given item. */
    public String getParent(String anItem)  { return anItem.length()>1? FilePathUtils.getParent(anItem) : null; }
    
    // Return whether file is directory
    public boolean isParent(String anItem)  { return anItem.endsWith("/"); }

    // Return child files
    public String[] getChildren(String aPar)  { return AssetIndex.get().getDirPaths(aPar); }

    // Return child file name
    public String getText(String anItem)
    {
        String name = FilePathUtils.getFileNameSimple(anItem);
        return StringUtils.firstCharUpperCase(name);
    }

    // Return child file icon
    public Image getImage(String anItem)  { return null; }
}

}