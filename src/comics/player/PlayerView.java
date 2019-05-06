package comics.player;
import snap.gfx.*;
import snap.util.PropChangeListener;
import snap.view.*;
import comics.player.PlayBar.*;

/**
 * A View to play an animation. Encapsulates a CameraView, StageView and Script.
 */
public class PlayerView extends ScaleBox {

    // StageView: the parent view of actors
    StageView           _stage;

    // CameraView: the parent of StageView to control how stage is viewed
    CameraView          _camera;
    
    // The Script
    Script              _script;
    
    // The current line being run
    int                 _runLine;
    
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
    public static final String Script_Prop = "Script";
    
/**
 * Creates a PlayerView.
 */
public PlayerView()
{
    // Create/configure this view
    setPrefSize(720, 405);
    setFillWidth(true); setFillHeight(true);
    setClipToBounds(true);
    enableEvents(MouseEnter, MouseExit, MouseMove, MousePress);
    
    // Create configure stage
    _stage = new StageView();
    _stage.setClipToBounds(true);
    _stage.setBorder(Color.BLACK, 1);
    
    // Create/configure camera
    _camera = new CameraView(_stage); _camera._player = this;
    setContent(_camera);
    
    // Create Script (empty)
    _script = new Script(this);
    
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
 * Returns the Script text.
 */
public String getScriptText()  { return getScript().getText(); }

/**
 * Sets the Script text.
 */
public void setScriptText(String aStr)  { getScript().setText(aStr); }

/**
 * Called when the script changes.
 */
public void scriptChanged()
{
    // Reset RunLine
    int runLine = getRunLine(); if(runLine>=getScript().getLineCount()) runLine = getScript().getLineCount() -1;
    _runLine = -1;
    setRunLine(runLine);
    
    // Notify EditorPane
    firePropChange(Script_Prop, null, _script);
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
    if(playing) {
        _camera.getAnim(0).setOnFinish(null);
        _camera.stopAnimDeep();
    }
    
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
public void setRunLine(int anIndex)
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
 * Plays until end of current RunLine - which then calls playLineDone().
 */
public void playLine()
{
    // Stop animation
    _camera.stopAnimDeep();
    
    // If current line not loaded, come back
    ScriptLine line = getScript().getLine(getRunLine());
    if(line!=null && !line.isLoaded())  {
        line.setLoadListener(pc -> playLine(getRunLine())); return; }
    
    // Get current line run time and current camera time
    int lineRunTime = getLineRunTime(getRunLine());
    int camTime = _camera.getAnimTimeDeep();
    
    // If time remaining for current line, keep playing and end with playLineDone
    if(camTime<lineRunTime) {
        _camera.getAnim(lineRunTime).setOnFinish(a -> ViewUtils.runLater(_playLineDoneRun));
        _camera.playAnimDeep();
    }
    
    // Otherwise, just trigger playLineDone
    else ViewUtils.runLater(_playLineDoneRun);
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
    
    // If at end, reset to beginning
    if(getRunTime()>=getRunTimeMax())
        setRunTime(0);
    
    // Start anim and firePropChange
    _playing = true;
    firePropChange(Playing_Prop, !_playing, _playing);
    playLine();
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
    //if(aValue) addChild(getPlayButton()); else removeChild(getPlayButton());
    
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
    _playButton = new PlayButtonBig(isPlaying());
    _playButton.addEventHandler(e -> playButtonFired(), Action);
    return _playButton;
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
    else if(anEvent.isMousePress()) {
        PlayButtonBig pb = new PlayButtonBig(isPlaying());
        addChild(pb);
        pb.animate(); pb.getAnim(0).setOnFinish(a -> removeChild(pb));
        if(isPlaying()) stop(); else play();
    }
    else super.processEvent(anEvent);
}

/**
 * Shows the title animation.
 */
public void showIntroAnim()
{
    // If already running, just return
    if(_introView!=null) { removeChild(_introView); _introView = null; }
        
    // Get IntroImage (come back later if image or Player not loaded)
    Image img = getIntroImage(), img2 = getRealImage();
    if(!img.isLoaded()) { img.addLoadListener(pc -> showIntroAnim()); return; }
    if(!img2.isLoaded()) { img2.addLoadListener(pc -> showIntroAnim()); return; }
    if(!isShowing()) {
        addPropChangeListener(PropChangeListener.getOneShot(pc -> showIntroAnim()), Showing_Prop); return; }
    
    // Create IntroImageView and RealView
    ImageView introImgView = new ImageView(img);
    ImageView realImgView = new ImageView(img2);
    realImgView.setScale(.8); realImgView.setTransY(-20);
    
    // Create ColView to hold images and add to player
    _introView = new ColView(); _introView.setAlign(Pos.TOP_CENTER); _introView.setSpacing(0);
    _introView.setPadding(30,20,20,20);
    _introView.setEffect(new ShadowEffect(20,Color.WHITE,0,0));
    _introView.setManaged(false); _introView.setLean(Pos.TOP_CENTER);
    _introView.setChildren(introImgView, realImgView);
    _introView.setSize(_introView.getPrefSize());
    addChild(_introView);
    
    // Configure/start anim
    _introView.setTransY(getHeight()-30);
    _introView.getAnim(1500).setTransY(0).getAnim(3000).getAnim(3500).setOpacity(0).play();
    _introView.getAnim(0).setOnFrame(a -> _introView.setScale(getCamera().getScale()));
    _introView.getAnim(0).setOnFinish(a -> introFinished());
}

/** Called when IntroAnim done. */
void introFinished()  { removeChild(_introView); _introView = null; }

/** Returns the Header/Reallusion images. */
Image getIntroImage()  { if(_introImg!=null) return _introImg;
    return _introImg = Image.get(getClass(), "pkg.images/Header.png"); }
Image getRealImage()  { if(_realImg!=null) return _realImg;
    return _realImg = Image.get(getClass(), "pkg.images/RealLogo.png"); }

// For IntroAnim
ColView _introView; Image _introImg; Image _realImg;

}