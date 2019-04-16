package comics;
import snap.gfx.*;
import snap.util.ArrayUtils;
import snap.view.*;
import java.util.*;

/**
 * A view to hold list of subjects in current script (Camera, Actors, Setting).
 */
public class SubjectsView extends ParentView {
    
    // The EditorPane
    EditorPane           _editorPane;

    // The Player
    PlayerView           _player;
    
    // The selected index
    int                  _selIndex = -1;
    
    //
    Image                _camera = Image.get(getClass(), "Camera.png");
    Image                _setting = Image.get(getClass(), "Setting.png");
    
    // Constants
    int SPACING = 12;
    static Color    SELECT_COLOR = Color.get("#039ed3");
    static Effect SELECT_EFFECT = new ShadowEffect(8, SELECT_COLOR, 0, 0);

/**
 * Creates a SubjectsView.
 */
public SubjectsView(EditorPane aEP)
{
    _editorPane = aEP;
    _player = aEP._player;
    setGrowWidth(true);
    setBorder(Border.createLoweredBevelBorder());
    setPadding(7,5,5,12);
    setFill(Color.WHITE);
    enableEvents(MousePress);
}

/**
 * Returns the selected SubjectView.
 */
public SubjectView getSelSubjectView()  { return _selIndex>=0? (SubjectView)getChild(_selIndex) : null; }

/**
 * Selects the given SubjectView.
 */
public void setSelSubjectView(SubjectView aAV)
{
    SubjectView oldView = getSelSubjectView();
    if(oldView!=null) oldView.setEffect(null);
    _selIndex = ArrayUtils.indexOf(getChildren(), aAV);
    if(aAV!=null) aAV.setEffect(SELECT_EFFECT);
    _editorPane.resetLater();
}

/**
 * Returns the selected SubjectView name.
 */
public String getSelName()  { return getSelSubjectView()!=null? getSelSubjectView()._name : null; }

/**
 * Updates the list of Subjects.
 */
public void updateSubjects()
{
    Script script = _player.getScript();
    removeChildren();
    
    SubjectView setting = new SubjectView(_setting, "Setting");
    addChild(setting);
    SubjectView cam = new SubjectView(_camera, "Camera");
    addChild(cam);
   
    // Iterate over lines
    List <Asset> assets = new ArrayList();
    for(ScriptLine sline : script.getLines()) {
        Asset asset = getActor(sline); if(asset==null) continue;
        if(assets.contains(asset)) continue;
        
        SubjectView sview = new SubjectView(asset);
        addChild(sview);
        assets.add(asset);
    }
}

/**
 * Returns the actor for ScriptLine.
 */
Asset getActor(ScriptLine aSline)
{
    // Get script words and first word
    String words[] = aSline.getWords();
    String word = words.length>=2? words[0] : null;
    
    // Handle empty
    if(word==null) return null;
    
    // Handle commands: Setting, Camera, Actor
    if(word.equals("setting")) return null;
    if(word.equals("camera")) return null;
    Actor actor = (Actor)_player.getScript().getView(aSline);
    return actor!=null? actor.getAsset() : null;
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
        setSelSubjectView(null);
}
    
/**
 * A view to hold a Subject.
 */
public class SubjectView extends ImageView {
    
    // Name
    String _name;
    
    /** Create an SubjectView. */
    public SubjectView(Image anImg, String aName)
    {
        super(anImg); _name = aName;
        setPrefSize(64,64); setKeepAspect(true); setPadding(3,3,14,3);
        enableEvents(MouseEnter, MouseExit, MousePress);
    }
    
    /** Create an SubjectView. */
    public SubjectView(Asset anAsset)  { this(anAsset.getImage(), anAsset.getName()); }
    
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
        Font font = Font.Arial10; aPntr.setFont(font);
        double sw = font.getStringAdvance(_name);
        aPntr.drawString(_name, Math.round((w-sw)/2), h-3);
    }
    
    /** Override to handle events. */
    protected void processEvent(ViewEvent anEvent)
    {
        if(anEvent.isMousePress())
            setSelSubjectView(this);
        if(anEvent.isMouseEnter() || anEvent.isMouseExit()) repaint();
        anEvent.consume();
    }
    
    /** Override to fix paint problem. */
    public void setEffect(Effect anEff)  { super.setEffect(anEff); repaint(-10,-10,getWidth()+20,getHeight()+20); }
}

}