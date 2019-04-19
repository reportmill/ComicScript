package comics.script;
import snap.gfx.Image;
import snap.util.PropChangeListener;

/**
 * A class to represent a line of script.
 */
public class ScriptLine {
    
    // The Script
    Script      _script;
    
    // The line text
    String      _text;
    
    // The words
    String      _words[];
    
    // The runtime of this ScriptLine
    int         _runTime;
    
    // Whether this ScriptLine is loaded
    boolean     _loaded = true;
    Image       _img;
    
    // A PropChangeListener to be called when image is loaded
    PropChangeListener _imgLoadLsnr;
    
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
    // Get script words and first word
    String words[] = getWords();
    String word = words.length>1? words[0] : null;
    
    // Handle empty
    if(word==null) { setRunTime(0); return null; }
    
    // Handle Setting, Camera, Actor
    if(word.equals("setting"))
        return _script._setting;
    if(word.equals("camera"))
        return _script._player.getCamera();
    return (Actor)_script.getView(this);
}

/**
 * Returns the runtime of this ScriptLine.
 */
public int getRunTime()  { return _runTime; }

/**
 * Sets the runtime of this ScriptLine.
 */
public void setRunTime(int aValue)  { _runTime = aValue; }

/**
 * Adds an unloaded image.
 */
public void addUnloadedImage(Image anImage)
{
    if(!_loaded) return; _loaded = false; _img = anImage;
    _img.addPropChangeListener(_imgLoadLsnr = pce -> imageLoaded());
}

/**
 * Called when image is loaded.
 */
void imageLoaded()
{
    _loaded = true;
    if(_imgLoadLsnr!=null) { _img.removePropChangeListener(_imgLoadLsnr); _imgLoadLsnr = null; }
}

/**
 * Standard toString implementation.
 */
public String toString()
{
    String str = getText(); if(str.trim().length()==0) str = "(empty)";
    return "SriptLine: " + str;
}

}