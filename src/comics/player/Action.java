package comics.player;

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
public void setLine(ScriptLine aLine)
{
    _line = aLine;
    load();
}

/**
 * Returns the name of the action.
 */
public String getName()  { return _name; }

/**
 * Sets the name.
 */
protected void setName(String aName)  { _name = aName; }

/**
 * Returns the name used.
 */
public String getNameUsed()
{
    String str = _line.getText().toLowerCase(), name = getName().toLowerCase();
    int ind = str.indexOf(name), end = ind + name.length();
    while(end<str.length() && !Character.isWhitespace(str.charAt(end))) end++;
    return str.substring(ind, end);
}

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
public String getText()
{
    String str = _line.getText().toLowerCase(), name = getNameUsed().toLowerCase();
    int ind = str.indexOf(name) + name.length();
    str = str.substring(ind).trim();
    return str;
}

/**
 * Returns the star.
 */
public Star getStar()  { return _line.getStar(); }

/**
 * Loads the Action.
 */
public void load()  { }

/**
 * Runs the Action.
 */
public void run()  { }

}