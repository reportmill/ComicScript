package comics.player;
import snap.gfx.*;
import snap.view.*;

/**
 * A class to hold actors.
 */
public class StageView extends ChildView implements Star {

/**
 * Creates a StageView.
 */
public StageView()
{
    setPrefSize(720, 405);
    setFill(Color.BLACK); setBorder(Color.BLACK, 1);
    setFocusable(false); setFocusWhenPressed(false); // Only need this because SnapScene superclass sets
}

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
public class BackImageAction extends Action {
    
    /** Creates the action. */
    public BackImageAction()  { setName("is"); setRunTime(1); }
    
    /** Runs the action. */
    public void run()
    {
        // Get Stage
        ScriptLine line = getLine();
        Script script = line.getScript();
        StageView stage = StageView.this;
        
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