package comics;
import snap.gfx.*;
import snap.view.*;

/**
 * A class to hold controls.
 */
public class PlayBar extends RowView {
    
    // The Player
    PlayerView   _player;
    static final int BAR_Y = 4;
    static final int BAR_HEIGHT = 3;
    static final Color MARK_COLOR = new Color("#F4D64A");
    static final Color BUTTON_COLOR = new Color(1,1,1, .8);
    
/**
 * Creates a PlayBar.
 */
public PlayBar(PlayerView aPV)
{
    _player = aPV;
    setHeight(36); setManaged(false);
    setPadding(6,5,0,0); setAlign(HPos.LEFT);
    enableEvents(MousePress, MouseDrag);
    addChild(new PlayButton());
}

/**
 * Override to paint button.
 */
protected void paintFront(Painter aPntr)
{
    double w = getWidth();
    aPntr.setColor(new Color(1,1,1,.4)); aPntr.fillRect(0,BAR_Y,w,BAR_HEIGHT);
    
    // Draw progress bar background
    int runTime = _player.getScript().getRunTime();
    int lineCount = _player.getScript().getLineCount();
    
    // Draw progress bar
    int runTimeNow = _player.getRunTime();
    double lineX = runTimeNow*w/runTime;
    aPntr.setColor(Color.RED); aPntr.fillRect(0,BAR_Y,lineX,BAR_HEIGHT);
    aPntr.fill(new Ellipse(lineX-4,BAR_Y+BAR_HEIGHT/2-4,8,8));
    
    // Draw progress bar frames
    aPntr.setColor(MARK_COLOR);
    for(int i=0;i<lineCount-1;i++) { int rt = _player.getLineEndTime(i);
        double x = rt*w/runTime;
        aPntr.fillRect(x-3,BAR_Y,6,BAR_HEIGHT);
    }
    
    // Draw progress bar button
    aPntr.setColor(Color.RED); aPntr.fill(new Ellipse(lineX-4,BAR_Y+BAR_HEIGHT/2-4,8,8));
}

/**
 * Process event.
 */
protected void processEvent(ViewEvent anEvent)
{
    if(anEvent.isMousePress() && anEvent.getY()<BAR_Y+BAR_HEIGHT+BAR_Y || anEvent.isMouseDrag())
        setRunTime(anEvent.getX());
}

/**
 * Sets the player runtime based on given x.
 */
protected void setRunTime(double aX)
{
    int runTime = (int)Math.round(aX/getWidth()*_player.getRunTimeMax());
    _player.setRunTime(_player._lastMouseRunTime = runTime);
}

/**
 * Override to size to stage width.
 */
protected double getPrefWidthImpl(double aH)  { return _player.getStage().getPrefWidth(aH) - 60; }
    
/**
 * A PlayButton.
 */
public class PlayButton extends View {
    
    /** Creates a PlayButton. */
    public PlayButton()
    {
        setPrefSize(28,28); setName("PlayButton");
        enableEvents(MousePress, Action);
    }
    
    /** Override to paint button. */
    protected void paintFront(Painter aPntr)
    {
        double w = getWidth(), h = getHeight(), pw = 20, ph = 20;
        
        Path path = new Path();
        if(!_player.isPlaying()) {
            path.moveTo(w/2-pw*1/3, h/2-ph/2); path.lineBy(pw,ph/2); path.lineBy(-pw,ph/2); path.close(); }
        else {
            path.moveTo(w/2-pw*1/5, h/2-ph/2); path.lineBy(pw/5,0); path.lineBy(0,ph); path.lineBy(-pw/5,0);
            path.close();
            path.moveTo(w/2-pw*3/5, h/2-ph/2); path.lineBy(pw/5,0); path.lineBy(0,ph); path.lineBy(-pw/5,0);
            path.close();
        }
        aPntr.setColor(BUTTON_COLOR); aPntr.fill(path);
    }
    
    /** Override to watch mouse. */
    protected void processEvent(ViewEvent anEvent)
    {
        if(anEvent.isMousePress()) fireActionEvent(anEvent);
        repaint();
    }
    
    protected void fireActionEvent(ViewEvent anEvent)
    {
        if(_player.isPlaying()) _player.stop();
        else _player.play();
    }
}

/**
 * A PlayButton.
 */
public static class PlayButtonBig extends View {
    
    // Whether mouse is over
    boolean _mouseOver;
    
    /** Creates a PlayButton. */
    public PlayButtonBig()
    {
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
        
        Path path = new Path(); double pw = 30, ph = 45;
        path.moveTo(w/2-pw*1/3, h/2-ph/2); path.lineBy(pw,ph/2); path.lineBy(-pw,ph/2); path.close();
        aPntr.setColor(new Color(1,1,1,_mouseOver? .6 : .3)); aPntr.fill(path);
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