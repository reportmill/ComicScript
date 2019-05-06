package comics.player;
import snap.gfx.*;
import snap.view.*;

/**
 * A class to hold controls.
 */
public class PlayBar extends RowView {
    
    // The Player
    PlayerView   _player;
    
    // The time label
    Label        _timeLabel;
    
    // The edit button (label)
    Label        _editButton;
    
    // The Bar height
    double       _barHeight = 3;
    
    // Whether mouse is currently over bar
    boolean      _mouseOverBar;
    
    // Constants
    static final int BAR_Y = 4;
    static final int BAR_HEIGHT = 3;
    static final double BAR_MIDY = 6;
    static final double BAR_MAXY = 12;
    static final Color MARK_COLOR = new Color("#F4D64A");
    static final Color BUTTON_COLOR = new Color(1,1,1, .8);
    static final String BarHeight_Prop = "BarHeight";
    
/**
 * Creates a PlayBar.
 */
public PlayBar(PlayerView aPV)
{
    _player = aPV;
    setHeight(36); setManaged(false); setLean(Pos.BOTTOM_LEFT); setGrowWidth(true);
    setPadding(6,10,0,10); setAlign(HPos.LEFT); setSpacing(20);
    enableEvents(MouseMove, MousePress, MouseDrag, MouseExit);
    setCursor(Cursor.HAND);
    
    // Create/configure/add PlayButton
    addChild(new PlayButton());
    
    // Create/configure/add TimeLabel
    _timeLabel = new Label(); _timeLabel.setTextFill(Color.WHITE); _timeLabel.setFont(Font.Arial12);
    addChild(_timeLabel);
    
    // Create/configure/add EditButton
    _editButton = new Label("Edit"); _editButton.setPadding(0,10,0,10);
    _editButton.setLeanX(HPos.RIGHT); _editButton.setName("EditButton");
    _editButton.setTextFill(Color.WHITE); _editButton.setFont(Font.Arial14);
    _editButton.addEventHandler(e -> ViewUtils.fireActionEvent(_editButton, e), MousePress);
    _editButton.addEventHandler(e -> e.consume(), MouseDrag, MouseRelease);
    _editButton.addEventHandler(e -> _editButton.setFont(Font.Arial14.getBold()), MouseEnter);
    _editButton.addEventHandler(e -> _editButton.setFont(Font.Arial14), MouseExit);
    ViewUtils.enableEvents(_editButton, Action);
    addChild(_editButton);
}

/**
 * Returns the bar y.
 */
double getBarY()  { return BAR_MIDY - _barHeight/2; }

/**
 * Returns the bar height.
 */
double getBarHeight()  { return _barHeight; }

/**
 * Sets the bar height.
 */
void setBarHeight(double aValue)  { _barHeight = aValue; repaint(0, 0, getWidth(), BAR_MAXY); }

/**
 * Sets whether mouse is over bar.
 */
void setMouseOverBar(boolean aValue)
{
    if(aValue==_mouseOverBar) return;
    _mouseOverBar = aValue;
    getAnim(0).finish();
    if(aValue) getAnimCleared(300).setValue(BarHeight_Prop, 5d).play();
    else getAnimCleared(300).setValue(BarHeight_Prop, 3d).play();
}

/**
 * Override to paint button.
 */
protected void paintFront(Painter aPntr)
{
    double x = 10, w = getWidth() - x*2, barY = getBarY(), barH = getBarHeight(), btnH = barH*2+2;
    aPntr.setColor(new Color(1,1,1,.4)); aPntr.fillRect(x, barY, w, barH);
    
    // Draw progress bar background
    int runTime = _player.getScript().getRunTime();
    int lineCount = _player.getScript().getLineCount();
    
    // Draw progress bar
    int runTimeNow = _player.getRunTime();
    double lineW = runTimeNow*w/runTime;
    aPntr.setColor(Color.RED); aPntr.fillRect(x, barY, lineW, barH);
    
    // Draw progress bar frames
    aPntr.setColor(MARK_COLOR);
    for(int i=0;i<lineCount-1;i++) { int rt = _player.getLineEndTime(i);
        double fx = x + rt*w/runTime;
        aPntr.fillRect(fx-3, barY, 6, barH);
    }
    
    // Draw progress bar button
    aPntr.setColor(Color.RED); aPntr.fill(new Ellipse(x + lineW - btnH/2, BAR_MIDY - btnH/2, btnH, btnH));
}

/**
 * Process event.
 */
protected void processEvent(ViewEvent anEvent)
{
    if(anEvent.isMousePress() || anEvent.isMouseDrag())
        setRunTime(anEvent.getX());
        
    if(anEvent.isMouseExit()) setMouseOverBar(false);
    if(anEvent.isMouseEvent())
        setMouseOverBar(isMouseDown() || anEvent.getY()>=0 && anEvent.getY()<=BAR_MAXY);
    anEvent.consume();
}

/**
 * Sets the player runtime based on given x.
 */
protected void setRunTime(double aX)
{
    double x = 10, w = getWidth() - x*2;
    int runTime = (int)Math.round((aX-x)/w*_player.getRunTimeMax());
    _player.setRunTime(_player._lastMouseRunTime = runTime);
}

/**
 * Called when player does a frame of animation.
 */
protected void playerDidFrame()
{
    int prt = _player.getRunTime()/1000, pmax = _player.getRunTimeMax()/1000;
    int prm = prt/60, prs = prt%60, pmm = pmax/60, pms = pmax%60;
    StringBuffer sb = new StringBuffer(); sb.append(prm).append(':'); if(prs<10) sb.append('0'); sb.append(prs);
    sb.append(" / ").append(pmm); if(pms<10) sb.append("0"); sb.append(pms);
    _timeLabel.setText(sb.toString()); repaint();
}

/**
 * Override to size to stage width.
 */
protected double getPrefWidthImpl(double aH)  { return _player.getStage().getPrefWidth(aH) - 60; }
    
/**
 * Returns the value for given key.
 */
public Object getValue(String aPropName)
{
    if(aPropName.equals(BarHeight_Prop)) return getBarHeight();
    return super.getValue(aPropName);
}

/**
 * Sets the value for given key.
 */
public void setValue(String aPropName, Object aValue)
{
    if(aPropName.equals(BarHeight_Prop)) setBarHeight((Double)aValue);
    else super.setValue(aPropName, aValue);
}

/**
 * A PlayButton.
 */
public class PlayButton extends View {
    
    /** Creates a PlayButton. */
    public PlayButton()
    {
        setPrefSize(24,24); setName("PlayButton");
        enableEvents(MousePress, MouseDrag, Action);
    }
    
    /** Override to paint button. */
    protected void paintFront(Painter aPntr)
    {
        double w = getWidth(), h = getHeight(), pw = 18, ph = 18;
        
        Path path = new Path();
        if(!_player.isPlaying()) {
            path.moveTo(w/2-pw*1/3, h/2-ph/2); path.lineBy(pw,ph/2); path.lineBy(-pw,ph/2); path.close(); }
        else {
            path.moveTo(w/2-pw/4, h/2-ph/2); path.lineBy(pw/4,0); path.lineBy(0,ph); path.lineBy(-pw/4,0);
            path.close();
            path.moveTo(w/2+pw/4, h/2-ph/2); path.lineBy(pw/4,0); path.lineBy(0,ph); path.lineBy(-pw/4,0);
            path.close();
        }
        aPntr.setColor(BUTTON_COLOR); aPntr.fill(path);
    }
    
    /** Override to watch mouse. */
    protected void processEvent(ViewEvent anEvent)
    {
        if(anEvent.isMousePress()) fireActionEvent(anEvent);
        else if(anEvent.isMouseDrag()) anEvent.consume();
        repaint();
    }
    
    protected void fireActionEvent(ViewEvent anEvent)
    {
        if(_player.isPlaying()) _player.stop();
        else _player.play(); anEvent.consume();
    }
}

/**
 * A PlayButton.
 */
public static class PlayButtonBig extends View {
    
    // Whether mouse is over
    boolean _mouseOver, _playing;
    
    /** Creates a PlayButton. */
    public PlayButtonBig(boolean isPlaying)
    {
        _playing = isPlaying;
        setSize(100,100); setName("PlayButtonBig");
        setManaged(false);
        setLean(Pos.CENTER);
        enableEvents(MouseEnter, MouseExit, MousePress, Action);
    }
    
    /** Override to paint button. */
    protected void paintFront(Painter aPntr)
    {
        double w = getWidth(), h = getHeight();
        Ellipse c1 = new Ellipse(0, 0, w, h);
        aPntr.setColor(new Color(0,0,0,.1)); aPntr.fill(c1);
        Ellipse c2 = new Ellipse(10, 10, w-20, h-20);
        Ellipse c3 = new Ellipse(20, 20, w-40, h-40);
        Shape c4 = Shape.subtract(c2, c3);
        aPntr.setColor(new Color(1,1,1,.3)); aPntr.fill(c4);
        
        if(_playing) {
            Path path = new Path(); double pw = 40, ph = 40;
            path.moveTo(w/2-pw*1.5/4, h/2-ph/2); path.lineBy(pw/4,0); path.lineBy(0,ph); path.lineBy(-pw/4,0);
            path.close();
            path.moveTo(w/2+pw*.5/4, h/2-ph/2); path.lineBy(pw/4,0); path.lineBy(0,ph); path.lineBy(-pw/4,0);
            path.close();
            aPntr.setColor(new Color(1,1,1,_mouseOver? .6 : .3)); aPntr.fill(path);
        }
     
        else {   
            Path path = new Path(); double pw = 30, ph = 45;
            path.moveTo(w/2-pw*1/3, h/2-ph/2); path.lineBy(pw,ph/2); path.lineBy(-pw,ph/2); path.close();
            aPntr.setColor(new Color(1,1,1,_mouseOver? .6 : .3)); aPntr.fill(path);
        }
    }
    
    /** Override to watch mouse. */
    protected void processEvent(ViewEvent anEvent)
    {
        if(anEvent.isMouseEnter()) _mouseOver = true;
        else if(anEvent.isMouseExit()) _mouseOver = false;
        else if(anEvent.isMousePress()) { fireActionEvent(anEvent); animate(); }
        repaint();
    }
    
    /** Animate. */
    protected void animate()
    {
        if(getScale()>1) { setScale(1); setOpacity(1); return; }
        setPickable(false);
        getAnimCleared(500).setScale(2).setOpacity(0).play();
    }
}

}