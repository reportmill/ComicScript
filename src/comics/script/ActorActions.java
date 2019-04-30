package comics.script;
import comics.app.Explode;
import comics.app.SpeakView;
import comics.app.StageView;
import snap.gfx.*;
import snap.util.ArrayUtils;
import snap.view.*;

/**
 * A class to hold Actor Action subclasses.
 */
public class ActorActions {

/**
 * An Actor Action that makes actor appear.
 */
public static class AppearsAction extends Action {
    
    /** Creates the action. */
    public AppearsAction()  { setName("Appears"); setRunTime(2000); }
    
    /** Runs the action. */
    public void run()
    {
        // Get anim for final destination
        Actor actor = (Actor)getStar();
        ViewAnim anim = actor.getAnim(2000);
    
        // Handle walk in
        actor.setOpacity(0);
        actor.setLocX(HPos.CENTER, -60, null);
        actor.setLocY(VPos.BOTTOM, 10, null);
        anim.setOpacity(1);
    }
}
    
/**
 * An Actor Action that makes actor walk.
 */
public static class WalksAction extends Action {
    
    /** Creates the action. */
    public WalksAction()  { setName("Walks"); setRunTime(2000); }
    
    /** Runs the action. */
    public void run()
    {
        // Get anim for final destination
        ScriptLine line = getLine();
        Actor actor = (Actor)getStar();
        ViewAnim anim = actor.getAnim(2000);
        StageView stage = line.getScript().getStage();
    
        // Look for animation
        actor.setAnimImage("Walk", 2000, 30);
    
        // Handle walk in from right
        String words[] = getLine().getWords();
        if(ArrayUtils.contains(words, "right")) {
            actor.setScaleX(-1);
            actor.setLocX(HPos.LEFT, stage.getWidth(), null);
            actor.setLocY(VPos.BOTTOM, 10, null);
            actor.setLocX(HPos.CENTER, 60, actor.getAnim(2000));
        }
        
        // Handle walk out
        else if(ArrayUtils.contains(words, "out")) {
            actor.setLocX(HPos.LEFT, stage.getWidth(), anim);
        }
        
        // Handle walk in
        else {
            actor.setLocX(HPos.LEFT, -actor.getWidth(), null);
            actor.setLocY(VPos.BOTTOM, 10, null);
            actor.setLocX(HPos.CENTER, -60, anim);
        }
    }
}
    
/**
 * An Actor Action that makes actor drop down from top.
 */
public static class DropsAction extends Action {
    
    /** Creates the action. */
    public DropsAction()  { setName("Drops"); setRunTime(2000); }
    
    /** Runs the action. */
    public void run()
    {
        // Get anim for final destination
        Actor actor = (Actor)getStar();
        ViewAnim anim = actor.getAnim(2000);
    
        // Handle drop right
        String words[] = getLine().getWords();
        if(ArrayUtils.contains(words, "right")) {
            actor.setScaleX(-1);
            actor.setLocX(HPos.CENTER, 60, null);
            actor.setLocY(VPos.TOP, -actor.getHeight(), null);
            actor.setLocY(VPos.BOTTOM, 10, anim);
        }
        
        // Handle drop
        else {
            actor.setLocX(HPos.CENTER, -60, null);
            actor.setLocY(VPos.TOP, -actor.getHeight(), null);
            actor.setLocY(VPos.BOTTOM, 10, anim);
        }
    }
}
    
/**
 * An Actor Action that makes actor grow.
 */
public static class GrowsAction extends Action {
    
    /** Creates the action. */
    public GrowsAction()  { setName("Grows"); setRunTime(1000); }
    
    /** Runs the action. */
    public void run()
    {
        // Get anim for final destination
        Actor actor = (Actor)getStar();
        actor.getAnim(1000).setScale(actor.getScale()+.2);
    }
}
    
/**
 * An Actor Action that makes actor flip.
 */
public static class FlipsAction extends Action {
    
    /** Creates the action. */
    public FlipsAction()  { setName("Flips"); setRunTime(1000); }
    
    /** Runs the action. */
    public void run()
    {
        // Get anim for final destination
        Actor actor = (Actor)getStar();
        actor.getAnim(1000).setRotate(actor.getRotate()+360);
    }
}
    
/**
 * An Actor Action that makes actor say something with speech baloon.
 */
public static class SaysAction extends Action {
    
    /** Creates the action. */
    public SaysAction()  { setName("Says"); setRunTime(3000); }
    
    /** Runs the action. */
    public void run()
    {
        // Get anim for final destination
        ScriptLine line = getLine();
        Actor actor = (Actor)getStar();
        StageView stage = line.getScript().getStage();
        
        // Get text string
        String textLine = line.getText().replace(",","").replace("\"","");
        int ind = textLine.indexOf("says"); if(ind<0) return;
        String str = textLine.substring(ind+4).trim();
    
        // Create, configure and add SpeakView
        SpeakView speakView = new SpeakView(); speakView.setText(str);
        speakView.setBubbleBounds(stage.getWidth()/2-150, 50, 250,80);
        stage.addChild(speakView);
        
        // Set speakView tail angle to point at actor head
        Rect bnds = actor.getBoundsParent();
        speakView.setTailAngleByPoint(bnds.getMidX(),bnds.y+30);
            
        // Add SpeakView with anim to fade in/out
        speakView.setOpacity(0);
        speakView.getAnim(500).setOpacity(1).getAnim(500+2000).setOpacity(1).getAnim(500+2000+500).setOpacity(0);
    }
}
    
/**
 * An Actor Action that makes actor explode.
 */
public static class ExplodesAction extends Action {
    
    /** Creates the action. */
    public ExplodesAction()  { setName("Explodes"); setRunTime(2500); }
    
    /** Runs the action. */
    public void run()
    {
        Actor actor = (Actor)getStar();
        Explode.explode(actor, 0);
    }
}
    
/**
 * An Actor Action that makes actor dance.
 */
public static class DanceAction extends Action {
    
    /** Creates the action. */
    public DanceAction()  { setName("Dance"); setRunTime(3000); }
    
    /** Runs the action. */
    public void run()
    {
        Actor actor = (Actor)getStar();
        actor.setAnimImage("Dance", 3000, 88);
    }
}
    
/**
 * An Actor Action that makes actor jump.
 */
public static class JumpAction extends Action {
    
    /** Creates the action. */
    public JumpAction()  { setName("Jumps"); setRunTime(3000); }
    
    /** Runs the action. */
    public void run()
    {
        Actor actor = (Actor)getStar();
        actor.setAnimImage("Jump", 1000, 16);
    }
}
    
/**
 * An Actor Action that makes actor wave
 */
public static class WaveAction extends Action {
    
    /** Creates the action. */
    public WaveAction()  { setName("Waves"); setRunTime(3000); }
    
    /** Runs the action. */
    public void run()
    {
        Actor actor = (Actor)getStar();
        actor.setAnimImage("Wave", 1000, 17);
    }
}
    
}