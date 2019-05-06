package comics.player;
import snap.gfx.*;
import snap.view.*;
import comics.player.Asset.*;

/**
 * A class to model actors.
 */
public class Actor extends ImageView implements Star {
    
    // The Script
    Script           _script;
    
    // The Asset
    Asset            _asset;
    
    // The offset
    double           _offsetX;
    
/**
 * Create new actor.
 */
public Actor(Script aScript, Asset anAsset)
{
    // Set Script, Asset and Name
    _script = aScript; _asset = anAsset;
    setName(anAsset.getName());
    
    // Set Asset image
    Image img = anAsset.getImage();
    setImage(img);
    setFillHeight(true); setFillWidth(true);
    
    // Make sure size gets set when image loaded
    if(!img.isLoaded()) img.addLoadListener(pce -> setSizeForAsset(_asset));
    else setSizeForAsset(_asset);
    
    // Set ShadowEffect
    setEffect(new ShadowEffect(6, Color.BLACK, 0, 0));
}

/**
 * Returns the Script.
 */
public Script getScript()  { return _script; }

/**
 * Returns the StageView.
 */
public StageView getStage()  { return _script.getStage(); }

/**
 * Returns the star name.
 */
public String getStarName()  { return _asset.getName(); }

/**
 * Returns the star image.
 */
public Image getStarImage()  { return _asset.getImage(); }

/**
 * Returns the action names for this star.
 */
public String[] getActionNames()  { return _actions; }
private static String _actions[] = { "appears", "walks", "waves", "jumps", "dances", "drops", "says",
    "grows", "flips", "explodes" };

/**
 * Returns an Action for this star and given ScriptLine.
 */
public Action getAction(ScriptLine aScriptLine)
{
    // Get action name from script
    String words[] = aScriptLine.getWords();
    String cmd = words.length>1? words[1] : null;
    if(cmd==null)
        return null;

    // Jump to specific command
    Action action = null;
    switch(cmd) {
        case "appears": action = new ActorAction.AppearsAction(); break;
        case "walks": action = new ActorAction.WalksAction(); break;
        case "drops": action = new ActorAction.DropsAction(); break;
        case "grows": action = new ActorAction.GrowsAction(); break;
        case "flips": action = new ActorAction.FlipsAction(); break;
        case "says": action = new ActorAction.SaysAction(); break;
        case "explodes": action = new ActorAction.ExplodesAction(); break;
        case "dances": action = new ActorAction.DanceAction(); break;
        case "jumps": action = new ActorAction.JumpAction(); break;
        case "waves": action = new ActorAction.WaveAction(); break;
        default: return null;
    }
    
    // If image not loaded yet, just return
    action.setLine(aScriptLine);
    return action;
}

/**
 * Returns the Asset.
 */
public Asset getAsset()  { return _asset; }

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
 * Sets the animated image over given range (if found).
 */
public void setAnimImage(String aName, int aTime, int aFrame)
{
    // Get image for name and cache old image
    String starName = getStarName();
    AnimImage anim = Asset.getAnimAsset(starName, aName);
    if(anim==null || !anim.isImageLoaded()) return;
    
    // Get old offset and set image and size
    double offsetX = _offsetX;
    setAssetImage(anim, anim.getOffsetX());
    
    // Configure anim
    getAnim(aTime).setValue("Frame", aFrame);
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
    StageView stage = getStage();
    double val = aVal;
    if(aPos==HPos.CENTER) val = stage.getWidth()/2 - getWidth()/2 + aVal;
    else if(aPos==HPos.RIGHT) val = stage.getWidth() - getWidth() - aVal;
    
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
    StageView stage = getStage();
    double val = aVal;
    if(aPos==VPos.CENTER) val = stage.getHeight()/2 - getHeight()/2 + aVal;
    else if(aPos==VPos.BOTTOM) val = stage.getHeight() - getHeight() - aVal;
    
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