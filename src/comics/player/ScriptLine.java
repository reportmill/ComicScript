package comics.player;
import snap.gfx.Image;
import snap.util.*;

/**
 * A class to represent a line of script.
 */
public class ScriptLine implements PropChange.DoChange {
    
    // The Script
    Script             _script;
    
    // The line text
    String             _text;
    
    // The words
    String             _words[];
    
    // The star
    Star               _star;
    
    // The action
    Action             _action;
    
    // Whether this ScriptLine is loaded
    boolean            _loaded = true;
    
    // PropertyChangeSupport
    PropChangeSupport  _pcs = PropChangeSupport.EMPTY;

    // A PropChangeListener specific to ScripLine.Loaded
    PropChangeSupport  _loadLsnr;
    
    // Constants
    public static final String Text_Prop = "Text";
    
/**
 * Creates a new ScriptLine with given text.
 */
public ScriptLine(Script aScript, String aStr)
{
    _script = aScript; _text = aStr;
    addPropChangeListener(pc -> _script.scriptLineDidChange(pc));
}

/**
 * Returns the script.
 */
public Script getScript()  { return _script; }

/**
 * Returns the text.
 */
public String getText()  { return _text; }

/**
 * Sets the text.
 */
public void setText(String aStr)
{
    _words = null; _star = null; _action = null;
    firePropChange(Text_Prop, _text, _text = aStr);
}

/**
 * Returns the words.
 */
public String[] getWords()
{
    // If already set, just return
    if(_words!=null) return _words;
    
    // Get text and words
    String text = getText().toLowerCase();
    String words[] = text.split("\\s");
    for(int i=0;i<words.length;i++) words[i] = words[i].replace(",","").replace("\"","");
    return _words = words;
}

/**
 * Returns the index.
 */
public int getIndex()  { return _script.getLines().indexOf(this); }

/**
 * Returns the star name.
 */
public String getStarName()
{
    String words[] = getWords(); if(words.length==0) return null;
    String word = words[0];
    return word.length()>0? word : null;
}

/**
 * Returns the Star of this line.
 */
public Star getStar()
{
    if(_star!=null) return _star;
    return getStarImpl();
}

/**
 * Returns the Star of this line.
 */
protected Star getStarImpl()
{
    String name = getStarName(); if(name==null) return null;
    if(name.equals("setting")) return _script.getStage();
    if(name.equals("camera")) return _script._player.getCamera();
    Star star = _script.getStage().getActor(this);
    if(star==null) System.out.println("ScriptLine: Couldn't find star named: " + name);
    return star;
}

/**
 * Sets the Star.
 */
public void setStar(Star aStar)
{
    String words[] = getWords();
    String text = aStar.getStarName();
    for(int i=1;i<words.length;i++) text += ' ' + words[i];
    getScript().setLineText(text, getIndex());
}

/**
 * Returns the action.
 */
public Action getAction()
{
    // If already set, just return
    if(_action!=null) return _action;
    
    // Get Star
    Star star = getStar(); if(star==null) return null;
    if(star.getStarImage()!=null && !star.getStarImage().isLoaded())
        addUnloadedImage(star.getStarImage());
    return _action = star.getAction(this);
}

/**
 * Sets the action by name.
 */
public void setActionByName(String aName)
{
    Star star = getStar(); if(star==null) return;
    String text = star.getStarName() + ' ' + aName;
    getScript().setLineText(text, getIndex());
}

/**
 * Returns the runtime of this ScriptLine.
 */
public int getRunTime()  { Action a = getAction(); return a!=null? a.getRunTime() : 1; }

/**
 * Executes line.
 */
public void run()
{
    // Get star and make sure it's visible
    Star star = getStar(); if(star==null) return;
    if(star instanceof Actor && !((Actor)star).isVisible())
        ((Actor)star).setVisible(true);

    // Get action and run        
    Action action = getAction(); if(action==null) return;
    action.run();
}

/**
 * Returns whether this line is loaded.
 */
public boolean isLoaded()  { return _loaded; }

/**
 * Sets whether this line is loaded.
 */
protected void setLoaded(boolean aValue)
{
    if(aValue==_loaded) return;
    _loaded = aValue;
    if(aValue && _loadLsnr!=null) {
        _loadLsnr.firePropChange(new PropChange(this, "Loaded", false, true)); _loadLsnr = null; }
}

/**
 * Sets the LoadListener.
 */
public void setLoadListener(PropChangeListener aLoadLsnr)
{
    if(_loadLsnr==null) _loadLsnr = new PropChangeSupport(this);
    _loadLsnr.addPropChangeListener(aLoadLsnr);
}

/**
 * Adds an unloaded image.
 */
public void addUnloadedImage(Image anImage)
{
    _loaded = false;
    anImage.addLoadListener(pce -> setLoaded(true));
}

/**
 * Standard toString implementation.
 */
public String toString()
{
    String str = getText(); if(str.trim().length()==0) str = "(empty)";
    return "SriptLine: " + str;
}

/**
 * Add listener.
 */
public void addPropChangeListener(PropChangeListener aPCL)
{
    if(_pcs==PropChangeSupport.EMPTY) _pcs = new PropChangeSupport(this);
    _pcs.addPropChangeListener(aPCL);
}

/**
 * Remove listener.
 */
public void removePropChangeListener(PropChangeListener aPCL)  { _pcs.removePropChangeListener(aPCL); }

/**
 * Fires a property change for given property name, old value, new value and index.
 */
protected void firePropChange(String aProp, Object oldVal, Object newVal)
{
    if(!_pcs.hasListener(aProp)) return;
    _pcs.firePropChange(new PropChange(this, aProp, oldVal, newVal));
}

/**
 * PropChange.DoChange method.
 */
public void doChange(PropChange aPC, Object oldVal, Object newVal)
{
    String prop = aPC.getPropName();
    if(prop==Text_Prop) {
        setText(SnapUtils.stringValue(newVal));
        getScript().getPlayer().playLine(getIndex());
    }
}

}