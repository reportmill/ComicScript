package comics.player;
import snap.util.ArrayUtils;
import snap.view.*;

/**
 * A class to hold Camera Action subclasses.
 */
public class CameraActions {

/**
 * An Camera Action that makes camera zoom.
 */
public static class ZoomAction extends Action {
    
    /** Creates the action. */
    public ZoomAction()  { setName("Zoom"); setRunTime(2000); }
    
    /** Runs the action. */
    public void run()
    {
        // Get anim for final destination
        CameraView camera = (CameraView)getStar();
        ViewAnim anim = camera.getAnim(2000);
    
        // Handle Zooms Out
        String words[] = getLine().getWords();
        if(ArrayUtils.contains(words, "out")) {
            anim.setValue("Zoom", 1);
        }
        
        // Handle Zooms (anything else)
        else {
            anim.setValue("Zoom", 2);
        }
    }
}
    
/**
 * An Camera Action that makes camera blur.
 */
public static class BlurAction extends Action {
    
    /** Creates the action. */
    public BlurAction()  { setName("Blur"); setRunTime(1000); }
    
    /** Runs the action. */
    public void run()
    {
        // Get anim for final destination
        CameraView camera = (CameraView)getStar();
        ViewAnim anim = camera.getAnim(1000);
    
        // Handle Zooms Out
        String words[] = getLine().getWords();
        if(ArrayUtils.contains(words, "out") || ArrayUtils.contains(words, "off")) {
            anim.setValue("Blur", 0d);
        }
        
        // Handle Zooms (anything else)
        else {
            anim.setValue("Blur", 8d);
        }
    }
}
    
}