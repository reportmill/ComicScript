package comics;
import snap.util.ArrayUtils;
import snap.view.*;

/**
 * A view to represent camera.
 */
public class CameraView extends BoxView {
    
    // The words
    String     _words[];
    
    // The start time
    int        _startTime = 0;
    
    // The runtime for last command
    int        _runTime;

/**
 * Creates a CameraView for content.
 */
public CameraView(View aContent)
{
    super(aContent);
    setClipToBounds(true);
}

/**
 * Returns the zoom.
 */
public double getZoom()  { return getContent().getScale(); }

/**
 * Zooms in.
 */
public void setZoom(double aValue)
{
    getContent().setScale(aValue);
}

/**
 * Runs the words.
 */
public void run(String aCmd, String theWords[])
{
    // Set words and reset runtime
    _words = theWords; _runTime = 0;
    
    // Jump to specific command
    switch(aCmd) {
        case "zooms": runZooms();
    }
}

/**
 * Runs a zoom command.
 */
public void runZooms()
{
    // Get anim for final destination
    ViewAnim anim = getContent().getAnim(_startTime).getAnim(_startTime+2000);

    // Handle Zooms Out
    if(ArrayUtils.contains(_words, "out")) {
        anim.setScale(1);
    }
    
    // Handle Zooms (anything else)
    else {
        anim.setScale(2);
    }
    
    _runTime = 2000;
}

/**
 * Calculates the preferred width.
 */
protected double getPrefWidthImpl(double aH)  { return getContent().getPrefWidth(); }

/**
 * Calculates the preferred height.
 */
protected double getPrefHeightImpl(double aW)  { return getContent().getPrefHeight(); }

/**
 * Actual method to layout children.
 */
protected void layoutImpl()
{
    View cont = getContent();
    cont.setBounds(0, 0, getWidth(), getHeight());
}

}