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
    
    // The run time for last mouse
    int                 _lastMouseRunTime;
    
    // The runnable to call playLineDone()
    Runnable            _playLineDoneRun = () -> playLineDone();
    
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
    _camera = new CameraView(_stage); _camera._player = this;
    
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
 * Convenience methods for Script methods.
 */
int getLineCount()  { return _script.getLineCount(); }
int getLineRunTime(int aLine)  { return _script.getLineRunTime(aLine); }
int getLineStartTime(int aLine)  { return _script.getLineStartTime(aLine); }
int getLineEndTime(int aLine)  { return _script.getLineEndTime(aLine); }
int getLineForTime(int aTime)  { return _script.getLineForTime(aTime); }

/**
 * Returns how long the animation has been running.
 */
public int getRunTime()
{
    int runLine = getRunLine();
    int runLineStartTime = getLineStartTime(runLine);
    int runLineTime = runLineStartTime + _camera.getAnim(0).getTime();
    return runLineTime;
}

/**
 * Returns how long the animation has been running.
 */
public void setRunTime(int aTime)
{
    // If playing, stop anim
    boolean playing = isPlaying();
    if(playing) { _camera.getAnim(0).setOnFinish(null); _camera.stopAnimDeep(); }
    
    // Get RunLine and RunLine time for player time
    int time = Math.min(aTime, getRunTimeMax()); if(time<0) time = 0;
    int runLine = getLineForTime(time);
    int lineTime = time - getLineStartTime(runLine);
    
    // Set RunLine and LineTime for time
    setRunLine(runLine);
    _camera.setAnimTimeDeep(lineTime);
    
    // If was playing, restart
    if(playing)
        playLine();
}

/**
 * Returns the total run time for animation.
 */
public int getRunTimeMax()  { return _script.getRunTime(); }

/**
 * Returns the currently running line.
 */
public int getRunLine()  { return _runLine; }

/**
 * Runs the script.
 */
protected void setRunLine(int anIndex)
{
    // If already set just return
    if(anIndex==_runLine) return;
    
    // Get current RunLine and RunTimes
    int runLine = _runLine;
    
    // If empty index
    if(anIndex<0) { resetStage(); _runLine = -1; return; }
    
    // If current line undefined or beyond new line, reset stage
    if(runLine<0 || runLine>anIndex) { resetStage(); runLine = -1; }
    
    // Otherwise, reset current line to end
    else {
        _camera.setAnimTimeDeep(getLineRunTime(runLine));
        clearAnims();
    }
    
    // Configure and run lines up to index
    for(int i=runLine+1; i<=anIndex; i++) {
        _script.runLine(i); if(i==anIndex) break;
        _camera.setAnimTimeDeep(getLineRunTime(i));
        clearAnims();
    }
    
    // Configure PlayerView.Anim to call playerDidAnim() on each frame
    _camera.getAnim(0).setOnFrame(a -> playerDidFrame());
    
    // Update RunLine and call Script.runLine()
    firePropChange(RunLine_Prop, runLine, _runLine = anIndex);
}

/**
 * Plays until end of current RunLine - which then calls playLineDone().
 */
protected void playLine()
{
    _camera.stopAnimDeep(); //System.out.println("Play Line " + _runLine);
    int runTime = getLineRunTime(getRunLine());
    _camera.getAnim(runTime).setOnFinish(a -> ViewUtils.runLater(_playLineDoneRun));
    _camera.playAnimDeep();
}

/**
 * Runs the script line at given index.
 */
public void playLine(int anIndex)
{
    // If replaying line, reset RunLine
    if(anIndex==getRunLine()) setRunLine(-1);
    
    // Set RunTime to Line.StartTime and playLine()
    int runTime = getLineStartTime(anIndex);
    setRunTime(runTime);
    playLine();
}

/**
 * Called when runLine is done.
 */
protected void playLineDone()
{
    // Clear Anim.OnFinish so this this doesn't get called again
    _camera.getAnim(0).setOnFinish(null);
    
    // If not Playing, just return
    if(!_playing) return;
    
    // If no more lines, just return
    if(_runLine+1>=getLineCount()) { stop(); return; }
    
    // Run next line
    playLine(_runLine+1);
}

/**
 * Called when player does a frame of animation.
 */
protected void playerDidFrame()
{
    if(getPlayBar().isShowing()) {
        getPlayBar().playerDidFrame();
        resetShowingControls();
    }
}

/**
 * Returns whether animation is playing.
 */
public boolean isPlaying()  { return _playing; }

/**
 * Runs the script.
 */
public void play()
{
    // If is playing or no lines, just return
    if(isPlaying()) return; if(getLineCount()==0) return;
    
    // If script not loaded, come back
    if(!_script.isLoaded()) { setLoading(true, () -> play()); return; }
    
    // If at end, reset to beginning
    if(getRunTime()>=getRunTimeMax())
        setRunTime(0);
    
    // Start anim and firePropChange
    playLine(); _playing = true;
    firePropChange(Playing_Prop, !_playing, _playing);
}

/**
 * Stops the script.
 */
public void stop()
{
    // If already stopped, just return
    if(!isPlaying()) return;
    
    // Stop anim and firePropChange
    _camera.stopAnimDeep(); _playing = false;
    firePropChange(Playing_Prop, !_playing, _playing);
    
    // Reset controls
    resetShowingControls();
    repaint();
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
protected void resetStage()
{
    _stage.removeChildren();
    _camera.setZoom(1);
    _camera.setBlur(0);
    clearAnims();
}

/**
 * Clears anims.
 */
protected void clearAnims()
{
    _camera.getAnimCleared(0); _stage.getAnimCleared(0);
    for(View child : _stage.getChildren()) child.getAnimCleared(0);
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
public boolean isShowingControls()  { return _playBarShowing; }//_playBar!=null && _playBar.isShowing(); }

/**
 * Sets whether controls are showing.
 */
protected void setShowingControls(boolean aValue)
{
    // If value already set, just return
    if(aValue==isShowingControls()) return;
    _playBarShowing = aValue;
    
    // Add or remove PlayButton
    //if(aValue) addChild(getPlayButton());
    //else removeChild(getPlayButton());
    
    // Add or remove PlayBar
    getPlayBar().getAnim(0).finish();
    if(aValue) {
        addChild(getPlayBar());
        getPlayBar().setOpacity(0); getPlayBar().getAnimCleared(300).setOpacity(1).play();
    }
    else {
        getPlayBar().setOpacity(1); getPlayBar().getAnimCleared(300).setOpacity(0).play();
        getPlayBar().getAnim(0).setOnFinish(a -> removeChild(getPlayBar()));
    }
} boolean _playBarShowing;

/**
 * Updates whether controls are showing.
 */
protected void resetShowingControls()
{
    // If script not loaded, just return
    if(!_script.isLoaded()) { setLoading(true, () -> resetShowingControls()); return; }
    
    // If should never show controls, just return
    if(!isShowControls()) { setShowingControls(false); return; }
        
    // Get whether controls should be showing
    boolean shouldShow = false;
    if(!isPlaying()) shouldShow = true;
    else if(getPlayBar().isMouseOver()) shouldShow = true;
    else if(_camera.isMouseOver()) {
        int idleTime = getRunTime() - _lastMouseRunTime;
        if(idleTime<1200) shouldShow = true;
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
        pbar.setBounds(getCamera().getX(), getCamera().getMaxY() - pbar.getHeight(), getCamera().getWidth(),
            pbar.getHeight());
    }
}

/**
 * Process event.
 */
protected void processEvent(ViewEvent anEvent)
{
    if(anEvent.isMouseEnter()) resetShowingControls();
    else if(anEvent.isMouseExit()) resetShowingControls();
    else if(anEvent.isMouseMove())  {
        _lastMouseRunTime = getRunTime(); resetShowingControls();
    }
    else super.processEvent(anEvent);
}

}