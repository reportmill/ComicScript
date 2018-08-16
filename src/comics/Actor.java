package comics;
import snap.gfx.*;
import snap.util.*;
import snap.view.*;
import snap.viewx.SnapScene;

/**
 * A class to model actors.
 */
public class Actor extends ImageView {
    
    // The stage
    SnapScene        _stage;
    
    // The script line
    ScriptLine       _scriptLine;
    
    // The words
    String           _words[];
    
    // The start time
    int              _startTime;
    
    // The run time
    int              _runTime;
    
    PropChangeListener _imgLoadLsnr;

/** Create new actor. */
public Actor(Image anImg)
{
    super(anImg);
    setFillHeight(true); setKeepAspect(true);
    if(!anImg.isLoaded()) anImg.addPropChangeListener(_imgLoadLsnr = pce -> imageLoaded());
    else imageLoaded();
    setEffect(new ShadowEffect(6, Color.BLACK, 0, 0));
}

void imageLoaded()
{
    setSize(getPrefWidth(-1)/2, getPrefHeight(-1)/2);
    if(_imgLoadLsnr!=null) { getImage().removePropChangeListener(_imgLoadLsnr); _imgLoadLsnr = null; }
}

/**
 * Runs the given command.
 */
public boolean run(String aCmd)
{
    switch(aCmd) {
        case "walks": runWalks(); break;
        case "drops": runDrops(); break;
        case "grows": runGrows(); break;
        case "says": runSays(); break;
        case "flips": runFlips(); break;
        case "explodes": runExplodes(); break;
        case "dances": runDance(); break;
        case "jumps": runJump(); break;
        default: return false;
    }
    return true;
}

/**
 * Runs a walk command.
 */
public void runWalks()
{
    if(ArrayUtils.contains(_words, "right")) {
        setScaleX(-1);
        setXY(_stage.getWidth(), _stage.getHeight() - getHeight() - 10);
        getAnim(_startTime).getAnim(_startTime+2000).setX(_stage.getWidth()/2-getWidth()/2+60);
    }
    
    else if(ArrayUtils.contains(_words, "out")) {
        getAnimCleared(_startTime).getAnim(_startTime+2000).setX(_stage.getWidth());
    }
    
    else {
        setXY(-getWidth(), _stage.getHeight() - getHeight() - 10);
        getAnim(_startTime).getAnim(_startTime+2000).setX(_stage.getWidth()/2-getWidth()/2-60);
    }
    
    // Look for animation
    if(isAnimImageLoading("Walk")) { _runTime = -1; return; }
    setAnimImage("Walk", 2000, 30);
    _runTime = 2000;
}

/**
 * Runs a walk command.
 */
public void runDrops()
{
    if(ArrayUtils.contains(_words, "right")) {
        setScaleX(-1);
        setXY(_stage.getWidth()/2-getWidth()/2+60, -getHeight());
        getAnim(_startTime).getAnim(_startTime+2000).setY(_stage.getHeight() - getHeight() - 10);
    }
    
    else {
        setXY(_stage.getWidth()/2-getWidth()/2-60, -getHeight());
        getAnim(_startTime).getAnim(_startTime+2000).setY(_stage.getHeight() - getHeight() - 10);
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
 * Runs a walk command.
 */
public void runSays()
{
    String text2 = _scriptLine.getText();
    int index = text2.indexOf("says,");
    if(index<0) index = text2.indexOf("says"); if(index<0) return;

    String str = text2.substring(index+4).trim();

    TextArea text = new TextArea(); text.setFont(Font.Arial10.deriveFont(24)); text.setText(str);
    text.setFill(Color.WHITE); text.setBorder(Color.BLACK,2); text.setAlign(Pos.CENTER);
    text.setBounds(_stage.getWidth()/2-150, 100, 300,60);
    text.scaleTextToFit();
    text.setOpacity(0);
    _stage.addChild(text);
    text.getAnim(_startTime).getAnim(_startTime+500).setOpacity(1).getAnim(_startTime+500+2000).setOpacity(1)
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
    if(isAnimImageLoading("Dance")) { _runTime = -1; return; }
    setAnimImage("Dance", 3000, 88);
    _runTime = 3000;
}

/**
 * Runs a jump command.
 */
public void runJump()
{
    if(isAnimImageLoading("Jump")) { _runTime = -1; return; }
    setAnimImage("Jump", 1000, 16);
    _runTime = 1000;
}

/**
 * Returns whether anim image is present, but loading.
 */
public boolean isAnimImageLoading(String aName)
{
    Image img = getAnimImage(aName);
    return img!=null && !img.isLoaded();
}

/**
 * Returns an anim image for name.
 */
public Image getAnimImage(String aName)
{
    String name = FilePathUtils.getFileNameSimple(getName());
    Image img = Index.get().getAnimImage(name, aName);
    return img;
}

/**
 * Sets the animated image over given range (if found).
 */
public boolean setAnimImage(String aName, int aTime, int aFrame)
{
    // Get image for name and cache old image
    Image img = getAnimImage(aName); if(img==null) return false;
    Image imgOld = getImage();
    
    // Set image and size
    setImage2(img); setFrame(0); //setWidth(getPrefWidth(-1)/2);
    
    // Configure anim
    getAnim(_startTime).getAnim(_startTime+aTime).setValue("Frame", aFrame);
    getAnim(_startTime).getAnim(_startTime+aTime).setOnFinish(a -> {
        setImage2(imgOld); setFrame(0); }); // setWidth(getPrefWidth(-1)/2);
    return true;
}

/**
 * Sets the image.
 */
public void setImage2(Image anImg)
{
    Rect bnds = getBounds();
    double bx = bnds.x - (anImg.getWidth()/2 - bnds.width)/2;
    double by = bnds.y - (anImg.getHeight()/2 - bnds.height);
    super.setImage(anImg);
    setBounds(bx, by, anImg.getWidth()/2, anImg.getHeight()/2);
}

}