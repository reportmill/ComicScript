package comics.script;
import comics.app.*;
import snap.gfx.*;
import snap.util.*;
import snap.view.*;
import snap.viewx.SnapScene;
import comics.script.Asset.*;

/**
 * A class to model actors.
 */
public class Actor extends ImageView {
    
    // The stage
    SnapScene        _stage;
    
    // The Script
    Script           _script;
    
    // The Asset
    Asset            _asset;
    
    // The script line
    ScriptLine       _scriptLine;
    
    // The words
    String           _words[];
    
    // The start time
    int              _startTime;
    
    // The run time
    int              _runTime;
    
    // The offset
    double           _offsetX;
    
    // A PropChangeListener to be called when image is loaded
    PropChangeListener _imgLoadLsnr;

/**
 * Create new actor.
 */
public Actor(Script aScript, Asset anAsset)
{
    _script = aScript;
    _asset = anAsset;
    setName(anAsset.getName());
    Image img = anAsset.getImage();
    setImage(img);
    setFillHeight(true); setFillWidth(true); //setKeepAspect(true);
    if(!img.isLoaded()) img.addPropChangeListener(_imgLoadLsnr = pce -> imageLoaded());
    else imageLoaded();
    setEffect(new ShadowEffect(6, Color.BLACK, 0, 0));
    
    double heightFeet = _asset.getHeight(), widthFeet = heightFeet/2;
    double w = _script.feetToPoints(widthFeet), h = _script.feetToPoints(heightFeet);
    setSize(w,h);
}

/**
 * Called when image is loaded.
 */
void imageLoaded()
{
    setSizeForAsset(_asset);
    if(_imgLoadLsnr!=null) { getImage().removePropChangeListener(_imgLoadLsnr); _imgLoadLsnr = null; }
}

/**
 * Returns the Asset.
 */
public Asset getAsset()  { return _asset; }

/**
 * Sets the Asset.
 */
//public void setAsset(Asset anAsset)  { }

/**
 * Returns the size for an asset.
 */
protected Size getAssetSize(Asset anAsset)
{
    Image img = anAsset.getImage();
    if(!img.isLoaded()) {
        double heightFeet = anAsset.getHeight(), widthFeet = heightFeet/2;
        double w = _script.feetToPoints(widthFeet), h = _script.feetToPoints(heightFeet);
        return new Size(w,h);
    }
    
    double imageHeight = img.getHeight(), imageWidth = img.getWidth();
    double heightFeet = anAsset.getHeight(), widthFeet = heightFeet*imageWidth/imageHeight;
    double w = _script.feetToPoints(widthFeet), h = _script.feetToPoints(heightFeet);
    return new Size(w,h);
}

/**
 * Returns the size for an asset.
 */
protected void setSizeForAsset(Asset anAsset)
{
    Size size = getAssetSize(anAsset);
    setSize(size);
}

/**
 * Runs the given command.
 */
public boolean run(ScriptLine aScriptLine)
{
    _stage = aScriptLine.getScript()._stage;
    _scriptLine = aScriptLine;
    _words = aScriptLine.getWords();
    String cmd = _words[1];

    // Jump to specific command
    switch(cmd) {
        case "appears": runAppears(); break;
        case "walks": runWalks(); break;
        case "drops": runDrops(); break;
        case "grows": runGrows(); break;
        case "says": runSays(); break;
        case "flips": runFlips(); break;
        case "explodes": runExplodes(); break;
        case "dances": runDance(); break;
        case "jumps": runJump(); break;
        case "waves": runWave(); break;
        default: return false;
    }
    
    // If image not loaded yet, just return
    if(!getImage().isLoaded()) {
        aScriptLine.addUnloadedImage(getImage()); }
    aScriptLine.setRunTime(_runTime);

    // Returns whether run was executed
    return _runTime>=0;
}

/**
 * Runs an appears command.
 */
public void runAppears()
{
    // Get anim for final destination
    ViewAnim anim = getAnim(_startTime).getAnim(_startTime+2000);

    // Handle walk in
    setOpacity(0);
    setLocX(HPos.CENTER, -60, null);
    setLocY(VPos.BOTTOM, 10, null);
    anim.setOpacity(1);
    
    _runTime = 2000;
}

/**
 * Runs a walk command.
 */
public void runWalks()
{
    // Get anim for final destination
    ViewAnim anim = getAnim(_startTime).getAnim(_startTime+2000);

    // Look for animation
    _runTime = 2000;
    setAnimImage("Walk", 2000, 30);

    // Handle walk in from right
    if(ArrayUtils.contains(_words, "right")) {
        setScaleX(-1);
        setLocX(HPos.LEFT, _stage.getWidth(), null);
        setLocY(VPos.BOTTOM, 10, null);
        setLocX(HPos.CENTER, 60, getAnim(_startTime).getAnim(_startTime+2000));
        //setXY(_stage.getWidth(), _stage.getHeight() - getHeight() - 10);
        //anim.setX(_stage.getWidth()/2-getWidth()/2+60);
    }
    
    // Handle walk out
    else if(ArrayUtils.contains(_words, "out")) {
        setLocX(HPos.LEFT, _stage.getWidth(), anim);
        //anim.setX(_stage.getWidth());
    }
    
    // Handle walk in
    else {
        setLocX(HPos.LEFT, -getWidth(), null);
        setLocY(VPos.BOTTOM, 10, null);
        setLocX(HPos.CENTER, -60, anim);
        //setXY(-getWidth(), _stage.getHeight() - getHeight() - 10);
        //anim.setX(_stage.getWidth()/2-getWidth()/2-60);
    }
}

/**
 * Runs a walk command.
 */
public void runDrops()
{
    // Get anim for final destination
    ViewAnim anim = getAnim(_startTime).getAnim(_startTime+2000);

    // Handle drop right
    if(ArrayUtils.contains(_words, "right")) {
        setScaleX(-1);
        setLocX(HPos.CENTER, 60, null);
        setLocY(VPos.TOP, -getHeight(), null);
        setLocY(VPos.BOTTOM, 10, anim);
        //setXY(_stage.getWidth()/2-getWidth()/2+60, -getHeight());
        //getAnim(_startTime).getAnim(_startTime+2000).setY(_stage.getHeight() - getHeight() - 10);
    }
    
    // Handle drop
    else {
        setLocX(HPos.CENTER, -60, null);
        setLocY(VPos.TOP, -getHeight(), null);
        setLocY(VPos.BOTTOM, 10, anim);
        //setXY(_stage.getWidth()/2-getWidth()/2-60, -getHeight());
        //getAnim(_startTime).getAnim(_startTime+2000).setY(_stage.getHeight() - getHeight() - 10);
    }
    
    _runTime = 2000;
}

/**
 * Runs a grows command.
 */
public void runGrows()
{
    getAnim(_startTime).getAnim(_startTime+1000).setScale(getScale()+.1);
    _runTime = 1000;
}

/**
 * Runs a flips command.
 */
public void runFlips()
{
    getAnim(_startTime).getAnim(_startTime+1000).setRotate(getRotate()+360);
    _runTime = 1000;
}

/**
 * Runs a speak command.
 */
public void runSays()
{
    // Get text string
    String textLine = _scriptLine.getText().replace(",","").replace("\"","");
    int ind = textLine.indexOf("says"); if(ind<0) return;
    String str = textLine.substring(ind+4).trim();

    // Create, configure and add SpeakView
    SpeakView speakView = new SpeakView(); speakView.setText(str);
    speakView.setBubbleBounds(_stage.getWidth()/2-150, 50, 250,80);
    _stage.addChild(speakView);
    
    // Set speakView tail angle to point at actor head
    Rect bnds = getBoundsParent();
    speakView.setTailAngleByPoint(bnds.getMidX(),bnds.y+30);
        
    // Add SpeakView with anim to fade in/out
    speakView.setOpacity(0);
    speakView.getAnim(_startTime).getAnim(_startTime+500).setOpacity(1).getAnim(_startTime+500+2000).setOpacity(1)
        .getAnim(_startTime+500+2000+500).setOpacity(0);
    _runTime = 3000;
}

/**
 * Runs a walk command.
 */
public void runExplodes()
{
    Explode.explode(this, _startTime);
    _runTime = 2500;
}

/**
 * Runs a dance command.
 */
public void runDance()
{
    _runTime = 3000;
    setAnimImage("Dance", 3000, 88);
}

/**
 * Runs a jump command.
 */
public void runJump()
{
    _runTime = 1000;
    setAnimImage("Jump", 1000, 16);
}

/**
 * Runs a wave command.
 */
public void runWave()
{
    _runTime = 1000;
    setAnimImage("Wave", 1000, 17);
}

/**
 * Returns an anim asset for name.
 */
public AnimImage getAnimImageAsset(String aName)
{
    String name = FilePathUtils.getFileNameSimple(getName());
    return AssetIndex.get().getAnim(name, aName);
}

/**
 * Returns an anim image for name.
 */
public Image getAnimImage(String aName)
{
    String name = FilePathUtils.getFileNameSimple(getName());
    Image img = AssetIndex.get().getAnimImage(name, aName);
    return img;
}

/**
 * Sets the animated image over given range (if found).
 */
public void setAnimImage(String aName, int aTime, int aFrame)
{
    // Get image for name and cache old image
    AnimImage anim = getAnimImageAsset(aName); if(anim==null) return;
    Image img = anim.getImage(); //getAnimImage(aName); if(img==null) return false;
    
    // If image loading, just return
    if(!img.isLoaded()) {
        _scriptLine.addUnloadedImage(img); return; }
    
    // Get old image and offset
    double offsetX = _offsetX;
    
    // Set image and size
    setAssetImage(anim, anim.getOffsetX()); //setImage(img, anim.getOffsetX());
    
    // Configure anim
    getAnim(_startTime).getAnim(_startTime+aTime).setValue("Frame", aFrame);
    getAnim(0).setOnFinish(a -> { setAssetImage(_asset, offsetX); });
}

/**
 * Sets the image from given asset.
 */
public void setAssetImage(Asset anAsset, double offsetX)
{
    /// Get old/new offsets (corrected if scale is flipped)
    double offOld = _offsetX, offNew = offsetX; if(getScaleX()<0) { offOld = -offOld; offNew = -offNew; }
    
    // Get old/new width & height
    double oldW = getWidth(), oldH = getHeight();
    Size size = getAssetSize(anAsset);
    double newW = size.width, newH = size.height;
    
    // Calculate new x/y
    double bx = getX() + (oldW/2 + offOld) - (newW/2 + offNew);
    double by = getY() - (newH - oldH);
    
    // Set new image, bounds, offset and reset frame
    Image img = anAsset.getImage();
    setImage(img);
    setBounds(bx, by, newW, newH);
    setFrame(0); _offsetX = offsetX;
}

/**
 * Sets the x location.
 */
public void setLocX(HPos aPos, double aVal, ViewAnim anAnim)
{
    // Correct value for pos
    double val = aVal;
    if(aPos==HPos.CENTER) val = _stage.getWidth()/2 - getWidth()/2 + aVal;
    else if(aPos==HPos.RIGHT) val = _stage.getWidth() - getWidth() - aVal;
    
    // Set in actor or in anim
    if(anAnim==null) setX(val);
    else anAnim.setX(val);
}

/**
 * Sets the y location.
 */
public void setLocY(VPos aPos, double aVal, ViewAnim anAnim)
{
    // Correct value for pos
    double val = aVal;
    if(aPos==VPos.CENTER) val = _stage.getHeight()/2 - getHeight()/2 + aVal;
    else if(aPos==VPos.BOTTOM) val = _stage.getHeight() - getHeight() - aVal;
    
    // Set in actor or in anim
    if(anAnim==null) setY(val);
    else anAnim.setY(val);
}

/**
 * Sets the XY location.
 */
public void setLocXY(Pos aPos, double aX, double aY, ViewAnim anAnim)
{
    setLocX(aPos.getHPos(), aX, anAnim); setLocY(aPos.getVPos(), aY, anAnim);
}

}