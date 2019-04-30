package comics.script;

/**
 * A class to manage an action for a star.
 */
public class Action {
    
    // The ScriptLine
    ScriptLine     _line;
    
    // The name of the action
    String         _name;
    
    // The Runtime
    int            _runTime;
    
/**
 * Returns the ScriptLine that this action is associated with.
 */
public ScriptLine getLine()  { return _line; }

/**
 * Returns the ScriptLine that this action is associated with.
 */
public void setLine(ScriptLine aLine)  { _line = aLine; }

/**
 * Returns the name of the action.
 */
public String getName()  { return _name; }

/**
 * Sets the name.
 */
protected void setName(String aName)  { _name = aName; }

/**
 * Returns the runtime of the action.
 */
public int getRunTime()  { return _runTime; }

/**
 * Sets the runtime of the action.
 */
protected void setRunTime(int aTime)  { _runTime = aTime; }

/**
 * Returns the text that invoked this action and describes it.
 */
public String getText()  { return _line.getText(); }

/**
 * Returns the star.
 */
public Star getStar()  { return _line.getStar(); }

/**
 * Runs the Action.
 */
public void run()  { }

}