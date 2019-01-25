package comics;
import snap.gfx.Color;
import snap.view.*;
import comics.PlayerViewControls.PlayButton;

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
    
    // The play button
    PlayButton   _playButton;
    
    // Constants for Property Changes
    public static final String RunLine_Prop = "RunLine";
    
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
    
    // Show Controls
    setShowControls(true);
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
 * Shows the controls.
 */
public boolean isShowControls()  { return _playButton!=null && _playButton.isShowing(); }

/**
 * Shows the controls.
 */
public void setShowControls(boolean aValue)
{
    if(aValue==isShowControls()) return;
    if(_playButton==null) {
        _playButton = new PlayButton();
        _playButton.addEventHandler(e -> playButtonFired(), Action);
    }
    if(aValue) addChild(_playButton);
    else removeChild(_playButton);
}

/**
 * Returns the currently running line.
 */
public int getRunLine()  { return _runLine; }

/**
 * Runs the script.
 */
public void play()
{
    // If no lines, just return
    if(_script.getLineCount()==0) return;
    
    // Set RunAll and run first line
    _runAll = true;
    runLine(0);
}

/**
 * Stops the script.
 */
public void stop()
{
    _runAll = false;
}

/**
 * Runs the script.
 */
public void runLine(int anIndex)
{
    // If no called from runAll(), resetStage
    if(!_runAll || anIndex==0) resetStage();
    
    // Update RunLine and call Script.runLine()
    firePropChange(RunLine_Prop, _runLine, _runLine = anIndex);
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
    if(_runLine+1>=_script.getLineCount()) { stop(); return; }
    
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

/**
 * Called when the PlayButton is pressed.
 */
void playButtonFired()
{
    play();
}

}