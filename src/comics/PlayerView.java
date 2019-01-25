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
public void runAll()  { _script.runAll(); }

/**
 * Runs the script.
 */
public void runLine(int lineIndex)  { _script.runLine(lineIndex); }

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