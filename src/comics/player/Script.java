package comics.player;
import java.util.*;
import snap.props.*;
import snap.util.ArrayUtils;

/**
 * A class to represent the instructions.
 */
public class Script extends PropObject {

    // The PlayerView
    PlayerView _player;

    // The View text
    String _text = "";

    // The Script lines
    List<ScriptLine> _lines;

    // The runtimes
    int _runTime;

    // Undo/Redo texts
    Undoer _undoer = new Undoer();

    // Constants
    public static final String Line_Prop = "Line";
    public static final String Text_Prop = "Text";

    /**
     * Constructor for given PlayerView.
     */
    public Script(PlayerView aPlayer)
    {
        _player = aPlayer;
    }

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
        if (Objects.equals(aStr, _text)) return;
        _lines = null;
        _runTime = -1;
        firePropChange(Text_Prop, _text, _text = aStr);
        _player.scriptChanged();
    }

    /**
     * Returns the number of lines.
     */
    public int getLineCount()
    {
        return getLines().size();
    }

    /**
     * Returns the line at given index.
     */
    public ScriptLine getLine(int anIndex)
    {
        return getLines().get(anIndex);
    }

    /**
     * Returns the lines.
     */
    public List<ScriptLine> getLines()
    {
        if (_lines != null) return _lines;
        String[] textLines = _text.split("\\n");
        return _lines = ArrayUtils.mapToList(textLines, textLine -> new ScriptLine(this, textLine));
    }

    /**
     * Adds a line to script.
     */
    public void addLine(ScriptLine aLine, int anIndex)
    {
        getLines().add(anIndex, aLine);
        firePropChange(Line_Prop, null, aLine, anIndex);
        _runTime = -1;
        _player.scriptChanged();
    }

    /**
     * Removes a line.
     */
    public void removeLine(int anIndex)
    {
        ScriptLine scriptLine = getLines().remove(anIndex);
        firePropChange(Line_Prop, scriptLine, null, anIndex);
        _runTime = -1;
        _player.scriptChanged();
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
        if (_runTime >= 0) return _runTime;
        _runTime = 0;
        for (ScriptLine line : getLines()) _runTime += line.getRunTime();
        return _runTime;
    }

    /**
     * Returns the run time of line at index.
     */
    public int getLineRunTime(int anIndex)
    {
        return anIndex < getLineCount() ? getLine(anIndex).getRunTime() : 0;
    }

    /**
     * Returns the runtime for line.
     */
    public int getLineStartTime(int aLine)
    {
        int rt = 0;
        for (int i = 0; i < aLine; i++) rt += getLineRunTime(i);
        return rt;
    }

    /**
     * Returns the runtime for line.
     */
    public int getLineEndTime(int aLine)
    {
        int rt = 0;
        for (int i = 0; i <= aLine; i++) rt += getLineRunTime(i);
        return rt;
    }

    /**
     * Returns the run line for given run time.
     */
    public int getLineForTime(int aTime)
    {
        int time = aTime, lineCount = getLineCount();
        for (int i = 0; i < lineCount; i++) {
            time -= getLineRunTime(i);
            if (time < 0) return i;
        }
        return lineCount - 1;
    }

    /**
     * Runs the script.
     */
    public void runLine(int anIndex)
    {
        // If invalid line index, just return
        if (anIndex >= getLineCount()) {
            System.err.println("Script.runLine: Index beyond bounds.");
            return;
        }

        // Run requested line
        ScriptLine line = getLine(anIndex);
        line.run();
    }

    /**
     * Returns the Undoer.
     */
    public Undoer getUndoer()
    {
        return _undoer;
    }

    /**
     * Undo text.
     */
    public void undo()
    {
        _undoer.undo();
    }

    /**
     * Redo text.
     */
    public void redo()
    {
        _undoer.redo();
    }

    /**
     * Called when a ScriptLine changes.
     */
    void handleScriptLinePropChange(PropChange propChange)
    {
        _undoer.addPropChange(propChange);
        _undoer.saveChanges();
        _runTime = -1;
        _player.scriptChanged();
    }

    /**
     * Returns the given feet size in points.
     */
    public double feetToPoints(double aValue)
    {
        double stageHeightPoints = _player.getStage().getHeight(), stageHeightFeet = 11.64;
        double pointHeight = aValue * stageHeightPoints / stageHeightFeet;
        pointHeight = Math.round(pointHeight);
        return pointHeight;
    }

    /**
     * Fires a property change for given property name, old value, new value and index.
     */
    protected void firePropChange(PropChange aPC)
    {
        super.firePropChange(aPC);
        _undoer.addPropChange(aPC);
        _undoer.saveChanges();
    }

    /**
     * PropChange.DoChange method.
     */
    @Override
    protected void processPropChange(PropChange aPC, Object oldVal, Object newVal)
    {
        String prop = aPC.getPropName();
        if (prop == Line_Prop) {
            int ind = aPC.getIndex();
            if (oldVal == null) {
                addLine((ScriptLine) newVal, ind);
                getPlayer().playLine(ind);
            } else {
                removeLine(ind);
                if (ind > 0) getPlayer().playLine(ind - 1);
            }
        }
        else super.processPropChange(aPC, oldVal, newVal);
    }
}