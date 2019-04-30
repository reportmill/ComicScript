package comics.script;
import comics.app.StageView;
import snap.gfx.Image;
import snap.view.*;

/**
 * A class to handle setting actions.
 */
public class Setting implements Star {
    
    // The Script
    Script        _script;

/**
 * Creates a Setting.
 */
public Setting(Script aScript)  { _script = aScript; }

/**
 * Returns the star name.
 */
public String getStarName()  { return "Setting"; }

/**
 * Returns the star image.
 */
public Image getStarImage()  { return null; }

/**
 * Returns the action names for this star.
 */
public String[] getActionNames()  { return _actions; }
private static String _actions[] = { "beach", "ovaloffice", "whitehouse" };

/**
 * Returns an Action for this star and given ScriptLine.
 */
public Action getAction(ScriptLine aScriptLine)
{
    Action action = new BackImageAction();
    action.setLine(aScriptLine);
    return action;
}

/**
 * An Setting Action that changes background image.
 */
public static class BackImageAction extends Action {
    
    /** Creates the action. */
    public BackImageAction()  { setName("is"); setRunTime(1); }
    
    /** Runs the action. */
    public void run()
    {
        // Get Stage
        ScriptLine line = getLine();
        Script script = line.getScript();
        StageView stage = line.getScript().getStage();
        
        // Get setting Image and ImageView
        String words[] = line.getWords();
        String iname = words.length>1? words[1] : null; if(iname==null) return;
        Image img = script.getNextImage(words, 0); if(img==null) return;
        ImageView iview = new ImageView(img, true, true);
        iview.setSize(stage.getWidth(), stage.getHeight());
        iview.setName("Setting");
        
        // If old setting, remove
        View oldStg = script.getView("Setting"); if(oldStg!=null) stage.removeChild(oldStg);
        
        // Add new setting
        stage.addChild(iview, 0);
    }
}
    
}