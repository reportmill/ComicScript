package comics;
import snap.gfx.*;
import snap.view.*;

/**
 * A class to hold controls.
 */
public class Controls {

/**
 * A PlayButton.
 */
public static class PlayButton extends View {
    
    // Whether mouse is over
    boolean _mouseOver;
    
    /** Creates a PlayButton. */
    public PlayButton()
    {
        setSize(100,100); setName("PlayButton");
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
        getAnimCleared(500).setScale(2).setOpacity(0).play();
    }
}

}