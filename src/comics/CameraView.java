package comics;
import snap.gfx.*;
import snap.util.ArrayUtils;
import snap.util.SnapUtils;
import snap.view.*;

/**
 * A view to represent camera.
 */
public class CameraView extends BoxView {
    
    // The Player
    PlayerView    _player;
    
    // The zoom
    double        _zoom = 1;
    
    // The blur
    double        _blur = 0;
    
    // The words
    String        _words[];
    
    // The start time
    int           _startTime = 0;
    
    // The runtime for last command
    int           _runTime;
    
/**
 * Creates a CameraView for content.
 */
public CameraView(View aContent)
{
    super(aContent);
    setClipToBounds(true);
    setEffect(new ShadowEffect());
}

/**
 * Returns the zoom.
 */
public double getZoom()  { return _zoom; }

/**
 * Sets the zoom.
 */
public void setZoom(double aValue)
{
    _zoom = aValue;
    getContent().setScale(aValue);
}

/**
 * Returns the blur.
 */
public double getBlur()  { return _blur; }

/**
 * Sets the blur.
 */
public void setBlur(double aValue)
{
    _blur = aValue;
    getContent().setEffect(aValue>0? new BlurEffect(aValue) : null);
}

/**
 * Runs the words.
 */
public void run(ScriptLine aScriptLine)
{
    // Set words and reset runtime
    _words = aScriptLine.getWords();
    String cmd = _words[1];
    
    // Jump to specific command
    switch(cmd) {
        case "zoom": runZooms(); break;
        case "zooms": runZooms(); break;
        case "blur": runBlurs(); break;
        case "blurs": runBlurs(); break;
    }
    
    aScriptLine.setRunTime(_runTime);
}

/**
 * Runs a zoom command.
 */
public void runZooms()
{
    // Get anim for final destination
    ViewAnim anim = getAnim(_startTime).getAnim(_startTime+2000);

    // Handle Zooms Out
    if(ArrayUtils.contains(_words, "out")) {
        anim.setValue("Zoom", 1);
    }
    
    // Handle Zooms (anything else)
    else {
        anim.setValue("Zoom", 2);
    }
    
    _runTime = 2000;
}

/**
 * Runs a blur command.
 */
public void runBlurs()
{
    // Get anim for final destination
    ViewAnim anim = getAnim(_startTime).getAnim(_startTime+1000);

    // Handle Zooms Out
    if(ArrayUtils.contains(_words, "out") || ArrayUtils.contains(_words, "off")) {
        anim.setValue("Blur", 0d);
    }
    
    // Handle Zooms (anything else)
    else {
        anim.setValue("Blur", 8d);
    }
    
    _runTime = 1000;
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

/**
 * Returns the value for given key.
 */
public Object getValue(String aPropName)
{
    if(aPropName.equals("Zoom")) return getZoom();
    if(aPropName.equals("Blur")) return getBlur();
    return super.getValue(aPropName);
}

/**
 * Sets the value for given key.
 */
public void setValue(String aPropName, Object aValue)
{
    if(aPropName.equals("Value")) setZoom(SnapUtils.doubleValue(aValue));
    else if(aPropName.equals("Blur")) setBlur(SnapUtils.doubleValue(aValue));
    else if(aPropName.equals("Zoom")) setZoom(SnapUtils.doubleValue(aValue));
    else super.setValue(aPropName, aValue);
}

/**
 * Override to paint PlayBar background shadow.
 */
protected void paintAbove(Painter aPntr)
{
    if(_player.getPlayBar().isShowing()) {
        double h = _player.getPlayBar().getHeight() + 10, y = getHeight() - h;
        aPntr.setOpacity(_player.getPlayBar().getOpacity());
        aPntr.setPaint(_grad); aPntr.fillRect(0, y, getWidth(), h);
    }
}

/** Gradient for  Bottom. */
Color _c0 = Color.CLEAR, _c1 = new Color(0,0,0,.1), _c2 = new Color(0,0,0,.2), _c3 = new Color(0,0,0,.3);
GradientPaint.Stop _stops[] = GradientPaint.getStops(0, _c0, .2, _c1, .35, _c2, 1, _c3);
GradientPaint _grad = new GradientPaint(90, _stops);

//aPntr.clipRect(0,0,getWidth(),getHeight());
//aPntr.drawImage(getPlayBarShadowImage(), -_rad3, getHeight() - _player.getPlayBar().getHeight() - _rad2);
/*Image getPlayBarShadowImage() {
    if(_pbImg!=null && _pbImg.getWidth()==getWidth()+_rad6) return _pbImg;
    Rect rect = new Rect(0,0,getWidth() + _rad2, _player.getPlayBar().getHeight() + _rad2);
    return _pbImg = ShadowEffect.getShadowImage(rect, _rad, _c2); }
Image _pbImg; int _rad = 15, _rad2 = _rad*2, _rad3 = _rad*3, _rad4 = _rad*4, _rad6 = _rad*6; */

}