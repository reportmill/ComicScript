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
        setAnimTimeDeep(getRunLineLength(runLine));
        getAnimCleared(0); _camera.getAnimCleared(0); _stage.getAnimCleared(0);
        for(View child : _stage.getChildren()) child.getAnimCleared(0);
    }
    
    // Configure and run lines up to index
    for(int i=runLine+1; i<=anIndex; i++) {
        _script.runLine(i); if(i==anIndex) break;
        setAnimTimeDeep(getRunLineLength(i));
        getAnimCleared(0); _camera.getAnimCleared(0); _stage.getAnimCleared(0);
        for(View child : _stage.getChildren()) child.getAnimCleared(0);
    }
    
    // Update RunLine and call Script.runLine()
    firePropChange(RunLine_Prop, runLine, _runLine = anIndex);
}

/**
 * Returns the runtime for line.
 */
public int getRunLineLength(int aLine)  { return _script.getRunTimes()[aLine]; }

/**
 * Returns the runtime for line.
 */
public int getRunLineStart(int aLine)  { int rt = 0; for(int i=0;i<aLine;i++) rt += getRunLineLength(i); return rt; }

/**
 * Returns the runtime for line.
 */
public int getRunLineEnd(int aLine)  { int rt = 0; for(int i=0;i<=aLine;i++) rt += getRunLineLength(i); return rt; }

/**
 * Returns the run line for given run time.
 */
public int getRunLineForTime(int aTime)
{
    int runTimes[] = _script.getRunTimes(), time = aTime;
    for(int i=0;i<runTimes.length;i++) {
        time -= runTimes[i]; if(time<0) return i; }
    return runTimes.length -1;
}

/**
 * Returns how long the animation has been running.
 */
public int getRunTime()
{
    int runLine = getRunLine(), runTimes[] = _script.getRunTimes();
    int runLineTime0 = 0; for(int i=1;i<runLine;i++) runLineTime0 += runTimes[i];
    int runLineTime2 = runLineTime0 + getAnim(0).getTime();
    return runLineTime2;
}

/**
 * Returns how long the animation has been running.
 */
public void setRunTime(int aTime)
{
    // Get RunLine and RunLine time for player time
    int runLine = getRunLineForTime(aTime);
    int lineTime = aTime - getRunLineStart(runLine);
    
    setRunLine(runLine);
    setAnimTimeDeep(lineTime);
    repaint();
}

/**
 * Returns how long the animation has been running.
 */
public int getRunTimeMax()  { return _script.getRunTime(); }

/**
 * Returns whether animation is playing.
 */
public boolean isPlaying()  { return _playing; }

/**
 * Sets whether animation is playing.
 */
public void setPlaying(boolean aValue)
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
    if(_script.getLineCount()==0) return;
    
    // If script not loaded, come back
    if(!_script.isLoaded()) {
        setLoading(true, () -> play()); return; }
    
    // Set RunAll and run first line
    setPlaying(true);
    runLine(0);
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
 * Runs the script.
 */
public void runLine(int anIndex)
{
    // Set RunLine
    setRunLine(anIndex);
    
    // 
    int runTime = getRunLineLength(anIndex);
    getAnim(0).getAnim(runTime).setOnFinish(a -> ViewUtils.runLater(() -> runLineDone()));
    getAnim(0).setOnFrame(a -> playerDidAnim());
    playAnimDeep();

    // If no called from runAll(), resetStage
    //if(!_playing || anIndex==0) resetStage();
    
    // If script not loaded, come back when done
    //if(!_script.isLoaded()) { setLoading(true, () -> runLine(anIndex)); return; }
    
    // Update RunLine and call Script.runLine()
    //firePropChange(RunLine_Prop, _runLine, _runLine = anIndex);
    //_script.runLine(anIndex);
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
    if(_runLine+1>=_script.getLineCount()) { stop(); return; }
    
    // Run next line
    runLine(_runLine+1);
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
        _script.setLoadListener(aRun);
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