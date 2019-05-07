package comics.player;
import java.util.*;
import snap.util.*;

/**
 * A class to represent the instructions.
 */
public class Script implements PropChange.DoChange {
    
    // The PlayerView
    PlayerView          _player;
    
    // The View text
    String              _text = "";

    // The Script lines
    List <ScriptLine>   _lines;
    
    // The runtimes
    int                 _runTime;
    
    // Undo/Redo texts
    Undoer              _undoer = new Undoer();
    
    // PropertyChangeSupport
    PropChangeSupport   _pcs = PropChangeSupport.EMPTY;

    // Constants
    public static final String Line_Prop = "Line";
    public static final String Text_Prop = "Text";

/**
 * Creates a script for given PlayerView.
 */
public Script(PlayerView aPlayer)  { _player = aPlayer; }

/**
 * Returns the Player.
 */
public PlayerView getPlayer()  { return _player; }

/**
 * Returns the Stage.
 */
public StageView getStage()  { return _player.getStage(); }

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
public void addLine(ScriptLine aLine, int anIndex)
{
    getLines().add(anIndex, aLine);
    firePropChange(Line_Prop, null, aLine, anIndex);
    _runTime = -1; _player.scriptChanged();
}

/**
 * Removes a line.
 */
public ScriptLine removeLine(int anIndex)
{
    ScriptLine sline = getLines().remove(anIndex);
    firePropChange(Line_Prop, sline, null, anIndex);
    _runTime = -1; _player.scriptChanged();
    return sline;
}

/**
 * Adds a line to script.
 */
public void addLineText(String aStr, int anIndex)
{
    ScriptLine sline = new ScriptLine(this, aStr);
    addLine(sline, anIndex);
}

/**
 * Sets a line in script to new string.
 */
public void setLineText(String aStr, int anIndex)
{
    ScriptLine sline = getLine(anIndex);
    sline.setText(aStr);
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
 * Returns the Undoer.
 */
public Undoer getUndoer()  { return _undoer; }

/**
 * Undo text.
 */
public void undo()  { _undoer.undo(); }

/**
 * Redo text.
 */
public void redo()  { _undoer.redo(); }

/**
 * Called when a ScriptLine changes.
 */
void scriptLineDidChange(PropChange aPC)
{
    _undoer.addPropertyChange(aPC);
    _undoer.saveChanges();
    _runTime = -1; _player.scriptChanged();
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
protected void firePropChange(String aProp, Object oldVal, Object newVal, int anIndex)
{
    PropChange pc = new PropChange(this, aProp, oldVal, newVal, anIndex);
    _pcs.firePropChange(pc);
    _undoer.addPropertyChange(pc);
    _undoer.saveChanges();
}

/**
 * PropChange.DoChange method.
 */
public void doChange(PropChange aPC, Object oldVal, Object newVal)
{
    String prop = aPC.getPropName();
    if(prop==Line_Prop) {
        if(oldVal==null) addLine((ScriptLine)newVal, aPC.getIndex());
        else removeLine(aPC.getIndex());
    }
}

}