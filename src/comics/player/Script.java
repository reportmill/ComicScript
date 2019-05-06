package comics.player;
import java.util.*;
import snap.gfx.Image;
import snap.util.SnapUtils;
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
    String             _text = "";

    // The Script lines
    List <ScriptLine>  _lines;
    
    // The runtimes
    int                _runTime;
    
    // The Setting
    Setting            _setting = new Setting(this);
    
    // Undo/Redo texts
    List <String>      _undoText = new ArrayList(), _redoText = new ArrayList();
    
    // Whether undoing/redoing
    boolean            _undoing, _redoing;
    
/**
 * Creates a script for given PlayerView.
 */
public Script(PlayerView aPlayer)
{
    _player = aPlayer;
    _stage = aPlayer.getStage();
}

/**
 * Returns the Player.
 */
public PlayerView getPlayer()  { return _player; }

/**
 * Returns the Stage.
 */
public StageView getStage()  { return _stage; }

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
public void setText(String aStr)
{
    // If already set, just return
    if(SnapUtils.equals(aStr, _text)) return;
    
    // Add old to Undo/Redo lists
    if(_undoing) _redoText.add(_text);
    else if(_redoing) _undoText.add(_text);
    else { _undoText.add(_text); _redoText.clear(); }
    
    // Set text, reset Lines, RunTime, notify player
    _text = aStr; _lines = null; _runTime = -1;
    _player.scriptChanged();
}

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
        Action action = line.getAction();
        _runTime += line.getRunTime();
        slines.add(line);
    }

    return _lines = slines;
}

/**
 * Adds a line to script.
 */
public void addLineText(String aStr, int anIndex)
{
    List <ScriptLine> lines = getLines();
    ScriptLine sline = new ScriptLine(this, aStr);
    lines.add(anIndex, sline);
    resetTextFromLines();
}

/**
 * Sets a line in script to new string.
 */
public void setLineText(String aStr, int anIndex)
{
    ScriptLine sline = getLine(anIndex);
    sline.setText(aStr);
    resetTextFromLines();
}

/**
 * Removes a line.
 */
public ScriptLine removeLine(int anIndex)
{
    List <ScriptLine> lines = getLines();
    ScriptLine sline = lines.remove(anIndex);
    resetTextFromLines();
    return sline;
}

/**
 * Sets the text from lines.
 */
void resetTextFromLines()
{
    List <ScriptLine> lines = getLines();
    StringBuffer sb = new StringBuffer();
    for(ScriptLine sl : lines) {
        String str = sl.getText();
        if(str.trim().length()>0)
            sb.append(str).append('\n');
    }
    String text = sb.toString().trim();
    setText(text);
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
    Action action = aScriptLine.getAction(); 
    if(action!=null)
        action.run();
}

/**
 * Undo text.
 */
public void undo()
{
    // Complain and bail if no more undos
    if(_undoText.size()==0) { ViewUtils.beep(); return; }
    
    // Get last undo, add to redos and set
    String str = _undoText.remove(_undoText.size()-1);
    _undoing = true; setText(str); _undoing = false;
}

/**
 * Redo text.
 */
public void redo()
{
    // Complain and bail if no more undos
    if(_redoText.size()==0) { ViewUtils.beep(); return; }
    
    // Get last undo, add to redos and set
    String str = _redoText.remove(_redoText.size()-1);
    _redoing = true; setText(str); _redoing = false;
}

/**
 * Returns the next image.
 */
public View getView(ScriptLine aScriptLine)
{
    // Get image for word
    String words[] = aScriptLine.getWords();
    Asset asset = getNextAsset(words, -1);
    if(asset==null)
        return null;
    
    // Get actor for image
    String name = asset.getName();
    View child = getView(name);
    
    // If actor not found, create
    if(child==null) {
        
        // Create new ActorView for Asset and add to Stage
        ImageView iview = new Actor(this,asset); child = iview; iview.setX(-999);
        _stage.addChild(child);
        
        // If image not loaded, tell ScriptLine
        if(!asset.isImageLoaded())
            aScriptLine.addUnloadedImage(asset.getImage());
    }
    
    // Return child
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