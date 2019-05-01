package comics.script;
import snap.gfx.Image;
import snap.util.*;

/**
 * A class to represent a line of script.
 */
public class ScriptLine {
    
    // The Script
    Script             _script;
    
    // The line text
    String             _text;
    
    // The words
    String             _words[];
    
    // The action
    Action             _action;
    
    // Whether this ScriptLine is loaded
    boolean            _loaded = true;
    
    // A PropChangeListener specific to ScripLine.Loaded
    PropChangeSupport  _loadLsnr;
    
/**
 * Creates a new ScriptLine with given text.
 */
public ScriptLine(Script aScript, String aStr)
{
    _script = aScript; _text = aStr;
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
protected void setText(String aStr)
{
    _text = aStr; _words = null;
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
 * Returns the Star of this line.
 */
public Star getStar()
{
    // Get first word
    String words[] = getWords(); if(words.length==0) return null;
    String word = words[0]; if(word.length()==0) return null;
    
    // Handle Setting, Camera, Actor
    if(word.equals("setting")) return _script._setting;
    if(word.equals("camera")) return _script._player.getCamera();
    return (Actor)_script.getView(this);
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
 * Returns the runtime of this ScriptLine.
 */
public int getRunTime()  { Action a = getAction(); return a!=null? a.getRunTime() : 0; }

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
 * A class to Listen to whether ScriptLine is loaded.
 */
public static class LoadListener {
}

}