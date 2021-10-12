package comics.player;

import snap.geom.Pos;
import snap.util.*;

/**
 * A class to represent a line of script.
 */
public class ScriptLine extends PropObject implements Loadable {

    // The Script
    Script  _script;

    // The line text
    String  _text;

    // The words
    String[]  _words;

    // The star
    Star  _star;

    // The action
    Action  _action;

    // Constants
    public static final String Text_Prop = "Text";

    /**
     * Creates a new ScriptLine with given text.
     */
    public ScriptLine(Script aScript, String aStr)
    {
        _script = aScript;
        _text = aStr;
        addPropChangeListener(pc -> _script.scriptLineDidChange(pc));
    }

    /**
     * Returns the script.
     */
    public Script getScript()
    {
        return _script;
    }

    /**
     * Returns the text.
     */
    public String getText()
    {
        return _text;
    }

    /**
     * Sets the text.
     */
    public void setText(String aStr)
    {
        _words = null;
        _star = null;
        _action = null;
        firePropChange(Text_Prop, _text, _text = aStr);
    }

    /**
     * Returns the words.
     */
    public String[] getWords()
    {
        // If already set, just return
        if (_words != null) return _words;

        // Get text and words
        String text = getText().toLowerCase().replace(",", " ").replace("\"", "");
        String words[] = text.split("\\s");
        return _words = words;
    }

    /**
     * Returns the index.
     */
    public int getIndex()
    {
        return _script.getLines().indexOf(this);
    }

    /**
     * Returns the star name.
     */
    public String getStarName()
    {
        String words[] = getWords();
        if (words.length == 0) return null;
        String word = words[0];
        return word.length() > 0 ? word : null;
    }

    /**
     * Returns the action name.
     */
    public String getActionName()
    {
        String words[] = getWords();
        if (words.length < 2) return null;
        String word = words[1];
        return word.length() > 0 ? word : null;
    }

    /**
     * Returns the Star of this line.
     */
    public Star getStar()
    {
        if (_star != null) return _star;
        return getStarImpl();
    }

    /**
     * Returns the Star of this line.
     */
    protected Star getStarImpl()
    {
        String name = getStarName();
        if (name == null) return null;
        if (name.equals("setting")) return _script.getStage();
        if (name.equals("camera")) return _script._player.getCamera();
        Star star = _script.getStage().getActor(this);
        if (star == null) System.out.println("ScriptLine: Couldn't find star named: " + name);
        return star;
    }

    /**
     * Sets the Star.
     */
    public void setStar(Star aStar)
    {
        String words[] = getWords();
        String text = aStar.getStarName();
        for (int i = 1; i < words.length; i++) text += ' ' + words[i];
        getScript().setLineText(text, getIndex());
    }

    /**
     * Returns the action.
     */
    public Action getAction()
    {
        // If already set, just return
        if (_action != null) return _action;

        // Get Star and action
        Star star = getStar();
        if (star == null) return null;
        return _action = star.getAction(this);
    }

    /**
     * Sets the action by name.
     */
    public void setActionByName(String aName)
    {
        Star star = getStar();
        if (star == null) return;
        String text = star.getStarName() + ' ' + aName;
        getScript().setLineText(text, getIndex());
    }

    /**
     * Returns the runtime of this ScriptLine.
     */
    public int getRunTime()
    {
        Action a = getAction();
        return a != null ? a.getRunTime() : 1;
    }

    /**
     * Executes line.
     */
    public void run()
    {
        // Get star and make sure it's visible
        Star star = getStar();
        if (star == null) return;
        if (star instanceof Actor && !((Actor) star).isVisible()) {
            Actor actr = (Actor) star;
            actr.setVisible(true);
            actr.setLocXY(Pos.BOTTOM_LEFT, 10, 10, null);
        }

        // Get action and run
        Action action = getAction();
        if (action == null) return;
        action.run();
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
        if (_loadable != null) return _loadable;
        Star star = getStar();
        Action action = getAction();
        if (star != null && !star.isLoaded()) System.out.println("ScriptLine.getLoadable: Star not loaded");
        if (action != null && !action.isLoaded()) System.out.println("ScriptLine.getLoadable: Action not loaded");
        _loadable = Loadable.getAsLoadable(star, action);
        return _loadable;
    }

    Loadable _loadable;

    /**
     * Standard toString implementation.
     */
    public String toString()
    {
        String str = getText();
        if (str.trim().length() == 0) str = "(empty)";
        return "SriptLine: " + str;
    }

    /**
     * PropChange.DoChange method.
     */
    public void processPropChange(PropChange aPC, Object oldVal, Object newVal)
    {
        String prop = aPC.getPropName();
        if (prop == Text_Prop) {
            setText(SnapUtils.stringValue(newVal));
            getScript().getPlayer().playLine(getIndex());
        } else super.processPropChange(aPC, oldVal, newVal);
    }
}