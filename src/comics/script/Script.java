package comics.script;
import comics.app.*;
import java.util.*;
import snap.gfx.Image;
import snap.view.*;

/**
 * A class to represent the instructions.
 */
public class Script {
    
    // The PlayerView
    PlayerView         _player;
    
    // The StageView
    StageView          _stage;
    
    // The View text
    String             _text;

    // The Script lines
    List <ScriptLine>  _lines;
    
    // Whether script is loaded
    boolean            _loaded;
    
    // A callback to be called when script is loaded
    public Runnable    _loadLsnr;
    
    // The runtimes
    int                _runTime;
    
    // The Setting
    Setting            _setting = new Setting(this);
    
/**
 * Creates a script for given PlayerView.
 */
public Script(PlayerView aPlayer, String aScript)
{
    _player = aPlayer;
    _stage = aPlayer.getStage();
    setText(aScript);
}

/**
 * Returns the setting.
 */
public Setting getSetting()  { return _setting; }

/**
 * Returns the Script text.
 */
public String getText()  { return _text; }

/**
 * Sets the text.
 */
public void setText(String aStr)  { _text = aStr; _lines = null; _runTime = -1; }

/**
 * Returns the number of lines.
 */
public int getLineCount()  { return getLines().size(); }

/**
 * Returns the line at given index.
 */
public ScriptLine getLine(int anIndex)  { return getLines().get(anIndex); }

/**
 * Returns the lines.
 */
public List <ScriptLine> getLines()
{
    if(_lines!=null) return _lines;
    
    List slines = new ArrayList();
    String tlines[] = _text.split("\\n");
    
    // Iterate over lines
    _runTime = 0;
    for(int i=0;i<tlines.length;i++) { String tline = tlines[i];
        ScriptLine line = new ScriptLine(this, tline);
        runLine(line);
        _runTime += line.getRunTime();
        slines.add(line);
    }

    return _lines = slines;
}

/**
 * Returns whether script is loaded.
 */
public boolean isLoaded()
{
    if(_loaded) return true;
    if(_text==null) return false;
    for(ScriptLine line : getLines()) if(!line._loaded) return false;
    _loaded = true;
    if(_loadLsnr!=null) _loadLsnr.run(); _loadLsnr = null;
    return _loaded;     //ViewUtils.runDelayed(() -> getRunTime(), 100, true);
}

/**
 * Returns the run time.
 */
public int getRunTime()  { return _runTime; }

/**
 * Returns the run time of line at index.
 */
public int getLineRunTime(int anIndex)  { return anIndex<getLineCount()? getLine(anIndex).getRunTime() : 0; }

/**
 * Returns the runtime for line.
 */
public int getLineStartTime(int aLine)  { int rt = 0; for(int i=0;i<aLine;i++) rt += getLineRunTime(i); return rt; }

/**
 * Returns the runtime for line.
 */
public int getLineEndTime(int aLine)  { int rt = 0; for(int i=0;i<=aLine;i++) rt += getLineRunTime(i); return rt; }

/**
 * Returns the run line for given run time.
 */
public int getLineForTime(int aTime)
{
    int time = aTime, lineCount = getLineCount();
    for(int i=0;i<lineCount;i++) { time -= getLineRunTime(i); if(time<0) return i; }
    return lineCount - 1;
}

/**
 * Runs the script.
 */
public void runLine(int anIndex)
{
    // If invalid line index, just return
    if(anIndex>=getLineCount()) { System.err.println("Script.runLine: Index beyond bounds."); return; }

    // Run requested line
    ScriptLine line = getLine(anIndex);
    runLine(line);
}

/**
 * Executes line for Stage.
 */
protected void runLine(ScriptLine aScriptLine)
{
    Star star = aScriptLine.getStar();
    if(star!=null)
        star.runScriptLine(aScriptLine);
}

/**
 * Returns the next image.
 */
public View getView(ScriptLine aScriptLine)
{
    // Get image for word
    String words[] = aScriptLine.getWords();
    Asset asset = getNextAsset(words, -1);
    
    // Get actor for image
    String name = asset!=null? asset.getName() : null;
    View child = getView(name);
    
    // If actor not found, create
    if(child==null) {
        ImageView iview = new Actor(this,asset); child = iview; iview.setX(-999);
        _stage.addChild(child);
    }
    
    return child;
}

/**
 * Returns the view with name.
 */
public View getView(String aName)
{
    View child = aName!=null? _stage.getChild(aName) : null;
    return child;
}

/**
 * Returns the next image.
 */
public Image getNextImage(String theWords[], int aStart)
{
    Asset asset = getNextAsset(theWords, aStart);
    Image img = asset!=null? asset.getImage() : null;
    return img;
}

/**
 * Returns the next image.
 */
public Asset getNextAsset(String theWords[], int aStart)
{
    for(int i=aStart+1;i<theWords.length;i++) { String word = theWords[i];
        
        // Look for actor/setting
        Asset asset = AssetIndex.get().getActor(word);
        if(asset==null) asset = AssetIndex.get().getSetting(word);
        
        // Get file from URL and load image
        if(asset!=null)
            return asset;
    }
    return null;
}

/**
 * Returns the given feet size in points.
 */
public double feetToPoints(double aValue)
{
    double stageHeightPoints = _player.getStage().getHeight(), stageHeightFeet = 11.64;
    double pointHeight = aValue*stageHeightPoints/stageHeightFeet;
    pointHeight = Math.round(pointHeight);
    return pointHeight;
}

}