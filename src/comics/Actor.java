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
        case "dances": runDances(); break;
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
        setXY(_stage.getWidth(), _stage.getHeight() - getHeight() - 10);
        getAnim(_startTime).getAnim(_startTime+2000).setX(_stage.getWidth()/2+60);
    }
    
    else if(ArrayUtils.contains(_words, "out")) {
        getAnimCleared(_startTime).getAnim(_startTime+2000).setX(_stage.getWidth());
    }
    
    else {
        setXY(-getWidth(), _stage.getHeight() - getHeight() - 10);
        getAnim(_startTime).getAnim(_startTime+2000).setX(_stage.getWidth()/2-120);
    }
    
    // Look for animation
    boolean canWalk = setAnimImage("Walking", 2000, 36);
    if(canWalk && !getImage().isLoaded()) { _runTime = -1; return; }
    _runTime = 2000;
}

/**
 * Runs a walk command.
 */
public void runDrops()
{
    if(ArrayUtils.contains(_words, "right")) {
        setSize(80,240);
        setXY(_stage.getWidth()/2+60,-getHeight());
        getAnim(_startTime).getAnim(_startTime+2000).setY(200);
    }
    
    else {
        setSize(80,240);
        setXY(_stage.getWidth()/2-120,-getHeight());
        getAnim(_startTime).getAnim(_startTime+2000).setY(200);
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
 * Runs a walk command.
 */
public void runDances()
{
    setAnimImage("Dancing", 5000, 170);
    if(!getImage().isLoaded()) { _runTime = -1; return; }
    _runTime = 5000;
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
    setImage(img); setWidth(getPrefWidth(-1)/2);
    
    // Configure anim
    getAnim(_startTime).getAnim(_startTime+aTime).setValue("Frame", aFrame);
    getAnim(_startTime).getAnim(_startTime+aTime).setOnFinish(a -> {
        setImage(imgOld); setFrame(0); setWidth(getPrefWidth(-1)/2); });
    return true;
}

}