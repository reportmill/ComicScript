package comics.player;
import snap.gfx.*;
import snap.util.SnapUtils;
import snap.view.*;

/**
 * A view to represent camera.
 */
public class CameraView extends ScaleBox implements Star {
    
    // The Player
    PlayerView    _player;
    
    // The zoom
    double        _zoom = 1;
    
    // The blur
    double        _blur = 0;
    
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
public double getZoom()  { return _zoom; }

/**
 * Sets the zoom.
 */
public void setZoom(double aValue)
{
    _zoom = aValue;
    relayout(); //getContent().setScale(aValue);
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
 * Returns the name.
 */
public String getStarName()  { return "Camera"; }

/**
 * Returns the image.
 */
public Image getStarImage()  { return null; }

/**
 * Returns the action names for this star.
 */
public String[] getActionNames()  { return _actions; }
private static String _actions[] = { "zooms", "blurs" };

/**
 * Returns an Action for this star and given ScriptLine.
 */
public Action getAction(ScriptLine aScriptLine)
{
    String words[] = aScriptLine.getWords();
    String cmd = words.length>1? words[1] : null;
    if(cmd==null)
        return null;

    // Jump to specific command
    Action action = null;
    switch(cmd) {
        case "zoom": case "zooms": action = new CameraActions.ZoomAction(); break;
        case "blur": case "blurs": action = new CameraActions.BlurAction(); break;
        default: return null;
    }
    
    action.setLine(aScriptLine);
    return action;
}

/**
 * Copied from ScaleBox.
 */
protected double getPrefWidthImpl(double aH)
{
    if(aH>=0 && (isFillHeight() || aH<getPrefHeight(-1))) return aH*getAspect();
    return super.getPrefWidthImpl(aH);
}

/**
 * Copied from ScaleBox.
 */
protected double getPrefHeightImpl(double aW)
{
    if(aW>=0 && (isFillWidth() || aW<getPrefWidth(-1))) return aW/getAspect();
    return super.getPrefHeightImpl(aW);
}

/**
 * Copied from ScaleBox.
 */
protected void layoutImpl()
{
    // If no child, just return
    ParentView aPar = this; View aChild = getContent(); if(aChild==null) return;
    Insets theIns = null; boolean isFillWidth = false; boolean isFillHeight = false;
    
    // Get parent bounds for insets (just return if empty)
    Insets ins = theIns!=null? theIns : aPar.getInsetsAll();
    double px = ins.left, py = ins.top;
    double pw = aPar.getWidth() - px - ins.right; if(pw<0) pw = 0; if(pw<=0) return;
    double ph = aPar.getHeight() - py - ins.bottom; if(ph<0) ph = 0; if(ph<=0) return;
    
    // Get content width/height
    double cw = aChild.getBestWidth(-1);
    double ch = aChild.getBestHeight(cw);
    
    // Handle ScaleToFit: Set content bounds centered, calculate scale and set
    if(isFillWidth || isFillHeight || cw>pw || ch>ph)  {
        double cx = px + (pw-cw)/2, cy = py + (ph-ch)/2;
        aChild.setBounds(cx, cy, cw, ch);
        double sx = isFillWidth || cw>pw? pw/cw : 1;
        double sy = isFillHeight || ch>ph? ph/ch : 1;
        if(isFillWidth && isFillHeight) sx = sy = Math.min(sx,sy); // KeepAspect?
        aChild.setScaleX(sx*_zoom); aChild.setScaleY(sy*_zoom);
        return;
    }
    
    // Handle normal layout
    if(cw>pw) cw = pw; if(ch>ph) ch = ph;
    double dx = pw - cw, dy = ph - ch;
    double sx = aChild.getLeanX()!=null? ViewUtils.getLeanX(aChild) : ViewUtils.getAlignX(aPar);
    double sy = aChild.getLeanY()!=null? ViewUtils.getLeanY(aChild) : ViewUtils.getAlignY(aPar);
    aChild.setBounds(px+dx*sx, py+dy*sy, cw, ch);
    aChild.setScaleX(_zoom); aChild.setScaleY(_zoom);
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