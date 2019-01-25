package comics;
import snap.gfx.Color;
import snap.view.*;

/**
 * A View to play an animation. Encapsulates a CameraView, StageView and Script.
 */
public class PlayerView extends BoxView {

    // StageView: the parent view of actors
    StageView    _stage;

    // CameraView: the parent of StageView to control how stage is viewed
    CameraView   _camera;
    
    // The Script
    Script       _script;
    
    // The current line being run
    int          _runLine;
    
    // Whether currenly running all lines
    boolean      _runAll;
    
/**
 * Creates a PlayerView.
 */
public PlayerView()
{
    // Create configure stage
    _stage = new StageView();
    _stage.setClipToBounds(true);
    _stage.setBorder(Color.BLACK, 1);
    
    // Create/configure camera
    _camera = new CameraView(_stage);
    
    // Create/configure this view
    setPadding(10,10,10,10);
    setContent(_camera);
    setClipToBounds(true);
    
    // Create Script (empty)
    _script = new Script(this, "");
}

/**
 * Returns the StageView.
 */
public StageView getStage()  { return _stage; }

/**
 * Returns the CameraView.
 */
public CameraView getCamera()  { return _camera; }

/**
 * Returns the script.
 */
public Script getScript()  { return _script; }

/**
 * Runs the script.
 */
public void runAll()
{
    // If no lines, just return
    if(_script.getLineCount()==0) return;
    
    // Set RunAll and run first line
    _runAll = true;
    runLine(0);
}

/**
 * Runs the script.
 */
public void runLine(int anIndex)
{
    // If no called from runAll(), resetStage
    if(!_runAll || anIndex==0) resetStage();
    
    // Update RunLine and call Script.runLine()
    _runLine = anIndex;
    _script.runLine(anIndex);
}

/**
 * Called when runLine is done.
 */
protected void runLineDone()
{
    // Clear Stage, Camera and actor anims
    _stage.getAnimCleared(0); _camera.getAnimCleared(0);
    for(View child : _stage.getChildren()) child.getAnimCleared(0);
    
    // If not RunAll, just return
    if(!_runAll) return;
    
    // If no more lines, just return
    if(_runLine+1>=_script.getLineCount()) { _runAll = false; return; }
    
    // Run next line
    runLine(_runLine+1);
}

/**
 * Resets the stage.
 */
public void resetStage()
{
    _stage.removeChildren();
    _camera.setZoom(1);
    _camera.setBlur(0);
}

}