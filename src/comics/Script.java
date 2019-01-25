package comics;
import java.util.*;
import snap.gfx.Image;
import snap.view.*;

/**
 * A class to represent the instructions.
 */
public class Script {
    
    // The PlayerView
    PlayerView         _player;
    
    // The StageView
    StageView          _stage;
    
    // The CameraView
    CameraView         _camera;

    // The View text
    String             _text;

    // The Script lines
    List <ScriptLine>  _lines;
    
/**
 * Creates a script for given StagePane.
 */
public Script(PlayerView aPlayer, String aScriptStr)
{
    _player = aPlayer;
    _stage = aPlayer.getStage();
    _camera = aPlayer.getCamera();
    setText(aScriptStr);
}

/**
 * Returns the Script text.
 */
public String getText()  { return _text; }

/**
 * Sets the text.
 */
public void setText(String aStr)  { _text = aStr; _lines = null; }

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
    if(_lines!=null) return _lines;
    
    List slines = new ArrayList();
    String tlines[] = _text.split("\\n");
    
    for(String tline : tlines)
        slines.add(new ScriptLine(this, tline));

    return _lines = slines;
}

/**
 * Runs the script.
 */
public void runLine(int anIndex)
{
    // If invalid line index, just return
    if(anIndex>=getLineCount()) { System.err.println("Script.runLine: Index beyond bounds."); return; }
    
    // Run previous lines instantly
    if(!_player._runAll)
        for(int i=0;i<anIndex;i++) { ScriptLine line = getLine(i);
            runLine(line, true); }

    // Run requested line with anim
    ScriptLine line = getLine(anIndex);
    runLine(line, false);
}

/**
 * Executes line for Stage.
 */
protected void runLine(ScriptLine aScriptLine, boolean doInstantly)
{
    // Print which line
    System.out.println("Run Line " + getLines().indexOf(aScriptLine));
    
    // Get script words and first word
    String words[] = aScriptLine.getWords();
    String word = words.length>=2? words[0] : null;
    int runTime = 0;
    
    // Handle empty
    if(word==null)
        runTime = 0;
    
    // Handle Setting command
    else if(word.equals("setting"))
        runTime = runSetting(aScriptLine);
    
    // Handle Camera command
    else if(word.equals("camera")) {
        _camera.run(words[1], words);
        runTime = _camera._runTime;
    }
    
    // Handle Actor
    else runTime = runActor(aScriptLine);
    
    // If negative RunTime, come back when something is ready
    if(runTime<0) {
        ViewUtils.runDelayed(() -> runLine(aScriptLine, doInstantly), 50, true); return; }
            
    // If run instantly, just set time to end
    if(doInstantly) {
        _camera.setAnimTimeDeep(runTime);
        _camera.getAnimCleared(0);
        _stage.getAnimCleared(0);
        for(View child : _stage.getChildren()) child.getAnimCleared(0);
    }
    
    // Register for OnFinish callback and play
    else {
        _camera.getAnim(0).getAnim(runTime).setOnFinish(a -> runLineDone());
        _camera.playAnimDeep();
    }
}

/**
 * Called when a run ScriptLine is finished.
 */
void runLineDone()  { ViewUtils.runLater(() -> _player.runLineDone()); }

/**
 * Runs a setting command.
 */
protected int runSetting(ScriptLine aScriptLine)
{
    // Get setting Image and ImageView
    String words[] = aScriptLine.getWords();
    Image img = getNextImage(words, 0); if(img==null) return 0;
    ImageView iview = new ImageView(img, true, true); iview.setName(img.getName());
    iview.setSize(_stage.getWidth(), _stage.getHeight());
    iview.setName("Setting"); //iview.setOpacity(.5);
    
    // If old setting, remove
    View oldStg = getView("Setting"); if(oldStg!=null) _stage.removeChild(oldStg);
    
    // Add new setting
    _stage.addChild(iview, 0);
    return 1;
}

/**
 * Runs an Actor command.
 */
protected int runActor(ScriptLine aScriptLine)
{
    // Get actor
    String words[] = aScriptLine.getWords(), word = words[0];
    Actor actor = (Actor)getView(aScriptLine); if(actor==null) return 0;
    actor._stage = _stage; actor._scriptLine = aScriptLine;
    
    // Run command
    boolean didRun = actor.run(words[1], words);
    //if(!didRun) { ViewUtils.runDelayed(() -> runLine(aScriptLine, doInstantly), 50, true); return; }
    return didRun? actor._runTime : -1;
}

/**
 * Runs a Camera command.
 */
protected int runCamera(ScriptLine aScriptLine)
{
    String words[] = aScriptLine.getWords(), word = words[0];
    _camera.run(words[1], words);
    return _camera._runTime;    
}

/**
 * Returns the next image.
 */
public View getView(ScriptLine aScriptLine)
{
    // Get image for word
    String words[] = aScriptLine.getWords();
    Image img = getNextImage(words, -1);
    
    // Get actor for image
    String name = img!=null? img.getName() : null;
    View child = getView(name);
    
    // If actor not found, create
    if(child==null) {
        ImageView iview = new Actor(img); iview.setName(img.getName()); child = iview; iview.setX(-999);
        _stage.addChild(child);
    }
    
    return child;
}

/**
 * Returns the view with name.
 */
public View getView(String aName)
{
    View child = aName!=null? _stage.getChild(aName) : null;
    return child;
}

/**
 * Returns the next image.
 */
public Image getNextImage(String theWords[], int aStart)
{
    for(int i=aStart+1;i<theWords.length;i++) { String word = theWords[i];
        
        // Look for actor/setting
        Image img = Index.get().getActorImage(word);
        if(img==null) img = Index.get().getSettingImage(word);
        
        // Get file from URL and load image
        if(img!=null)
            return img;
    }
    return null;
}

}