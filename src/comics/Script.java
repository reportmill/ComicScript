package comics;
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
    
    // The CameraView
    CameraView         _camera;

    // The View text
    String             _text;

    // The Script lines
    List <ScriptLine>  _lines;
    
    // Whether script is loaded
    boolean            _loaded;
    
    // A callback to be called when script is loaded
    Runnable           _loadLsnr;
    
    // The runtimes
    int                _runTimes[], _runTime;
    
/**
 * Creates a script for given StagePane.
 */
public Script(PlayerView aPlayer, String aScriptStr)
{
    _player = aPlayer;
    _stage = aPlayer.getStage();
    _camera = aPlayer.getCamera();
    setText(aScriptStr);
}

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
public int getLineRunTime(int anIndex)  { return getLine(anIndex).getRunTime(); }

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
    // Get script words and first word
    String words[] = aScriptLine.getWords();
    String word = words.length>=2? words[0] : null;
    
    // Handle empty
    if(word==null) aScriptLine.setRunTime(0);
    
    // Handle commands: Setting, Camera, Actor
    else if(word.equals("setting")) runSetting(aScriptLine);
    else if(word.equals("camera")) _camera.run(aScriptLine);
    else runActor(aScriptLine);
}

/**
 * Runs a setting command.
 */
protected void runSetting(ScriptLine aScriptLine)
{
    // Get setting Image and ImageView
    String words[] = aScriptLine.getWords();
    Image img = getNextImage(words, 0); if(img==null) return;
    ImageView iview = new ImageView(img, true, true);
    iview.setSize(_stage.getWidth(), _stage.getHeight());
    iview.setName("Setting"); //iview.setOpacity(.5);
    
    // If old setting, remove
    View oldStg = getView("Setting"); if(oldStg!=null) _stage.removeChild(oldStg);
    
    // Add new setting
    _stage.addChild(iview, 0);
    aScriptLine.setRunTime(1);
}

/**
 * Runs an Actor command.
 */
protected void runActor(ScriptLine aScriptLine)
{
    Actor actor = (Actor)getView(aScriptLine); if(actor==null) return;
    actor._stage = _stage; actor._scriptLine = aScriptLine;
    actor.run(aScriptLine);
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