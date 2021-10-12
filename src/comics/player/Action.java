package comics.player;

import snap.util.Loadable;

/**
 * A class to manage an action for a star.
 */
public class Action implements Loadable {

    // The ScriptLine
    ScriptLine _line;

    // The name of the action
    String _name;

    // The Runtime
    int _runTime;

    /**
     * Returns the ScriptLine that this action is associated with.
     */
    public ScriptLine getLine()
    {
        return _line;
    }

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
    public String getName()
    {
        return _name;
    }

    /**
     * Sets the name.
     */
    protected void setName(String aName)
    {
        _name = aName;
    }

    /**
     * Returns the name used.
     */
    public String getNameUsed()
    {
        String str = _line.getText().toLowerCase(), name = getName().toLowerCase();
        int ind = str.indexOf(name), end = ind + name.length();
        if (ind < 0) return name; // Shouldn't happen
        while (end < str.length() && !Character.isWhitespace(str.charAt(end))) end++;
        return str.substring(ind, end);
    }

    /**
     * Returns the star.
     */
    public Star getStar()
    {
        return _line.getStar();
    }

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
     * Returns the predicate strings.
     */
    public String[] getPredicateStrings()
    {
        return new String[0];
    }

    /**
     * Returns the runtime of the action.
     */
    public int getRunTime()
    {
        return _runTime;
    }

    /**
     * Sets the runtime of the action.
     */
    protected void setRunTime(int aTime)
    {
        _runTime = aTime;
    }

    /**
     * Loads the Action.
     */
    public void load()
    {
    }

    /**
     * Runs the Action.
     */
    public void run()
    {
    }

    /**
     * Returns whether resource is loaded.
     */
    public boolean isLoaded()
    {
        Loadable loadable = getLoadable();
        return loadable == null || loadable.isLoaded();
    }

    /**
     * Adds a callback to be triggered when resources loaded (cleared automatically when loaded).
     */
    public void addLoadListener(Runnable aRun)
    {
        Loadable loadable = getLoadable();
        if (loadable == null) return;
        loadable.addLoadListener(aRun);
    }

    /**
     * Returns the loadable.
     */
    protected Loadable getLoadable()
    {
        return null;
    }

}