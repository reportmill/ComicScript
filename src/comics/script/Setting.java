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
 * Runs the ScriptLine.
 */
public void runScriptLine(ScriptLine aScriptLine)
{
    // Get Stage
    StageView stage = _script._stage;
    
    // Get setting Image and ImageView
    String words[] = aScriptLine.getWords();
    Image img = _script.getNextImage(words, 0); if(img==null) return;
    ImageView iview = new ImageView(img, true, true);
    iview.setSize(stage.getWidth(), stage.getHeight());
    iview.setName("Setting"); //iview.setOpacity(.5);
    
    // If old setting, remove
    View oldStg = _script.getView("Setting"); if(oldStg!=null) stage.removeChild(oldStg);
    
    // Add new setting
    stage.addChild(iview, 0);
    aScriptLine.setRunTime(1);
}

}