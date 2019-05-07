package comics.player;
import java.util.*;
import snap.util.SnapUtils;
import snap.view.*;

/**
 * A class to represent the instructions.
 */
public class Script {
    
    // The PlayerView
    PlayerView          _player;
    
    // The StageView
    StageView           _stage;
    
    // The View text
    String              _text = "";

    // The Script lines
    List <ScriptLine>   _lines;
    
    // The runtimes
    int                 _runTime;
    
    // Undo/Redo texts
    List <String>       _undoText = new ArrayList(), _redoText = new ArrayList();
    
    // Whether undoing/redoing
    boolean             _undoing, _redoing;
    
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
    // If already set, just return
    if(_lines!=null) return _lines;
    
    // Get text lines from text
    List slines = new ArrayList();
    String tlines[] = _text.split("\\n");
    
    // Iterate over text lines and create ScriptLines
    for(String tline : tlines)
        slines.add(new ScriptLine(this, tline));
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
    scriptLineDidChange(sline);
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
void resetTextFromLines()  { setText(getTextFromLines()); }

/**
 * Returns the text from lines.
 */
String getTextFromLines()
{
    StringBuffer sb = new StringBuffer();
    for(ScriptLine sl : getLines()) { String str = sl.getText();
        if(str.trim().length()>0)
            sb.append(str).append('\n');
    }
    return sb.toString().trim();
}

/**
 * Returns the run time.
 */
public int getRunTime()
{
    if(_runTime>=0) return _runTime;
    _runTime = 0; for(ScriptLine line : getLines()) _runTime += line.getRunTime();
    return _runTime;
}

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
    line.run();
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
 * Called when a ScriptLine changes.
 */
void scriptLineDidChange(ScriptLine aLine)
{
    _runTime = -1;
    _player.scriptChanged();
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