package comics;
import snap.gfx.*;
import snap.view.*;
import comics.PlayBar.*;

/**
 * A View to play an animation. Encapsulates a CameraView, StageView and Script.
 */
public class PlayerView extends BoxView {

    // StageView: the parent view of actors
    StageView           _stage;

    // CameraView: the parent of StageView to control how stage is viewed
    CameraView          _camera;
    
    // The Script
    Script              _script;
    
    // The current line being run
    int                 _runLine = -1;
    
    // Whether currenly running all lines
    boolean             _playing;
    
    // Wether to show controls
    boolean             _showControls;
    
    // The play controls bar
    PlayBar             _playBar;
    
    // The play button
    PlayButtonBig       _playButton;
    
    // Whether mouse is over
    boolean             _mouseOver;
    
    // The run time for last mouse
    int                 _lastMouseRunTime;
    
    // Constants for Property Changes
    public static final String RunLine_Prop = "RunLine";
    public static final String Playing_Prop = "Playing";
    public static final String Loading_Prop = "Loading";
    
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
    enableEvents(MouseEnter, MouseExit, MouseMove);
    
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
 * Sets the Script text.
 */
public void setScriptText(String aStr)
{
    if(aStr.equals(getScript().getText())) return;
    getScript().setText(aStr);
    _runLine = -1; setRunTime(0);
}

/**
 * Returns the number of lines in script.
 */
public int getLineCount()  { return _script.getLineCount(); }

/**
 * Returns the run time for line at given index.
 */
public int getLineRunTime(int aLine)  { return _script.getLineRunTime(aLine); }

/**
 * Returns the start time for line at given index.
 */
public int getLineStartTime(int aLine)  { return _script.getLineStartTime(aLine); }

/**
 * Returns the end time for line at given index.
 */
public int getLineEndTime(int aLine)  { return _script.getLineEndTime(aLine); }

/**
 * Returns the run line for given run time.
 */
public int getLineForTime(int aTime)  { return _script.getLineForTime(aTime); }

/**
 * Returns how long the animation has been running.
 */
public int getRunTime()
{
    int runLine = getRunLine();
    int runLineStartTime = getLineStartTime(runLine);
    int runLineTime = runLineStartTime + getAnim(0).getTime();
    return runLineTime;
}

/**
 * Returns how long the animation has been running.
 */
public void setRunTime(int aTime)
{
    // Get RunLine and RunLine time for player time
    int time = Math.min(aTime, getRunTimeMax());
    int runLine = getLineForTime(time);
    int lineTime = time - getLineStartTime(runLine);
    
    setRunLine(runLine);
    setAnimTimeDeep(lineTime);
    repaint();
}

/**
 * Returns how long the animation has been running.
 */
public int getRunTimeMax()  { return _script.getRunTime(); }

/**
 * Returns the currently running line.
 */
public int getRunLine()  { return _runLine; }

/**
 * Runs the script.
 */
public void setRunLine(int anIndex)
{
    // If already set just return
    if(anIndex==_runLine) return;
    
    // Get current RunLine and RunTimes
    int runLine = _runLine;
    
    // If current line undefined or beyond new line, reset stage
    if(runLine<0 || runLine>anIndex) { resetStage(); runLine = -1; }
    
    // Otherwise, reset current line to end
    else {
        setAnimTimeDeep(getLineRunTime(runLine));
        getAnimCleared(0); _camera.getAnimCleared(0); _stage.getAnimCleared(0);
        for(View child : _stage.getChildren()) child.getAnimCleared(0);
    }
    
    // Configure and run lines up to index
    for(int i=runLine+1; i<=anIndex; i++) {
        _script.runLine(i); if(i==anIndex) break;
        setAnimTimeDeep(getLineRunTime(i));
        getAnimCleared(0); _camera.getAnimCleared(0); _stage.getAnimCleared(0);
        for(View child : _stage.getChildren()) child.getAnimCleared(0);
    }
    
    // Configure PlayerView anim to call runLineDone() and playerDidAnim()
    int runTime = getLineRunTime(anIndex);
    getAnim(0).getAnim(runTime).setOnFinish(a -> ViewUtils.runLater(() -> runLineDone()));
    getAnim(0).setOnFrame(a -> playerDidAnim());
    
    // Update RunLine and call Script.runLine()
    firePropChange(RunLine_Prop, runLine, _runLine = anIndex);
}

/**
 * Called when runLine is done.
 */
protected void runLineDone()
{
    // Clear Stage, Camera and actor anims
    getAnimCleared(0); _stage.getAnimCleared(0); _camera.getAnimCleared(0);
    for(View child : _stage.getChildren()) child.getAnimCleared(0);
    
    // If not RunAll, just return
    if(!_playing) return;
    
    // If no more lines, just return
    if(_runLine+1>=getLineCount()) { stop(); return; }
    
    // Run next line
    runLine(_runLine+1);
}

/**
 * Called when player does a frame of animation.
 */
protected void playerDidAnim()
{
    if(getPlayBar().isShowing()) {
        getPlayBar().repaint();
        resetShowingControls();
    }
}

/**
 * Returns whether animation is playing.
 */
public boolean isPlaying()  { return _playing; }

/**
 * Sets whether animation is playing.
 */
protected void setPlaying(boolean aValue)
{
    if(aValue==isPlaying()) return;
    firePropChange(Playing_Prop, _playing, _playing = aValue);
}

/**
 * Runs the script.
 */
public void play()
{
    // If no lines, just return
    if(getLineCount()==0) return;
    
    // If script not loaded, come back
    if(!_script.isLoaded()) { setLoading(true, () -> play()); return; }
    
    // Set RunAll and run first line
    setPlaying(true);
    playAnimDeep();
}

/**
 * Stops the script.
 */
public void stop()
{
    stopAnimDeep();
    setPlaying(false);
    resetShowingControls();
}

/**
 * Runs the script line at given index.
 */
public void runLine(int anIndex)
{
    int runTime = getLineStartTime(anIndex);
    setRunTime(runTime);
    playAnimDeep();
}

/**
 * Returns whether the player loading.
 */
public boolean isLoading()  { return _loading; } boolean _loading;

/**
 * Sets the player loading.
 */
protected void setLoading(boolean aValue, Runnable aRun)
{
    if(aValue==_loading) return;
    firePropChange(Loading_Prop, _loading, _loading = aValue);
    
    if(aValue) {
        _script._loadLsnr = aRun;
        ProgressBar pbar = new ProgressBar(); pbar.setIndeterminate(true); pbar.setSize(pbar.getPrefSize());
        pbar.setManaged(false); pbar.setLean(Pos.CENTER);
        _stage.addChild(pbar);
    }
    
    else if(aRun!=null)
        aRun.run();
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

/**
 * Shows the controls.
 */
public boolean isShowControls()  { return _showControls; }

/**
 * Shows the controls.
 */
public void setShowControls(boolean aValue)
{
    if(aValue==isShowControls()) return;
    _showControls = aValue; resetShowingControls();
}

/**
 * Returns whether controls are showing.
 */
public boolean isShowingControls()  { return _playButton!=null && _playButton.isShowing(); }

/**
 * Sets whether controls are showing.
 */
protected void setShowingControls(boolean aValue)
{
    // If value already set, just return
    if(aValue==isShowingControls()) return;
    
    // Add or remove PlayButton
    if(aValue) addChild(getPlayButton());
    else removeChild(getPlayButton());
    
    // Add or remove PlayBar
    if(aValue) addChild(getPlayBar());
    else removeChild(getPlayBar());
}

/**
 * Updates whether controls are showing.
 */
protected void resetShowingControls()
{
    // If script not loaded, just return
    if(!_script.isLoaded()) { setLoading(true, () -> resetShowingControls()); return; }
    
    // If should never show controls, just return
    if(!isShowControls()) {
        setShowingControls(false); return; }
        
    // Get whether controls should be showing
    boolean shouldShow = false;
    if(!isPlaying()) shouldShow = true;
    else if(_mouseOver) {
        int idleTime = getRunTime() - _lastMouseRunTime;
        if(idleTime<2500) shouldShow = true;
    }
    
    // Set showing
    setShowingControls(shouldShow);
}

/**
 * Returns the play bar.
 */
protected PlayBar getPlayBar()
{
    if(_playBar!=null) return _playBar;
    _playBar = new PlayBar(this);
    return _playBar;
}

/**
 * Returns the play button.
 */
protected PlayButtonBig getPlayButton()
{
    if(_playButton!=null) return _playButton;
    _playButton = new PlayButtonBig();
    _playButton.addEventHandler(e -> playButtonFired(), Action);
    return _playButton;
}

/**
 * Override to size PlayBar.
 */
protected void layoutImpl()
{
    // Do normal version
    super.layoutImpl();
    
    // If PlayBar showing, position at bottom-center of camera rect
    PlayBar pbar = getPlayBar();
    if(pbar.isShowing()) {
        pbar.setBounds(getCamera().getX()+10, getCamera().getMaxY() - pbar.getHeight(), getCamera().getWidth()-20,
            pbar.getHeight());
    }
}

/**
 * Process event.
 */
protected void processEvent(ViewEvent anEvent)
{
    if(anEvent.isMouseEnter()) { _mouseOver = true; resetShowingControls(); }
    else if(anEvent.isMouseExit()) { _mouseOver = false; resetShowingControls(); }
    else if(anEvent.isMouseMove())  {
        _lastMouseRunTime = getRunTime(); resetShowingControls();
    }
    else super.processEvent(anEvent);
}

}