package comics;
import snap.gfx.*;
import snap.util.*;
import snap.view.*;
import snap.viewx.SnapScene;
import snap.web.WebURL;

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

/** Create new actor. */
public Actor(Image anImg)  { super(anImg); }

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
    String name = FilePathUtils.getFileNameSimple(getName());
    WebURL url = WebURL.getURL("/Temp/ComicScriptLib/images/" + name + "Walking" + ".gif");
    if(url.isFound()) {
        Image img = Image.get(url), img0 = getImage();
        setImage(img); setWidth(getPrefWidth(-1)/2);
        getAnim(_startTime).getAnim(_startTime+2000).setValue("Frame", 36);
        getAnim(_startTime).getAnim(_startTime+2000).setOnFinish(a -> {
            setImage(img0); setWidth(getPrefWidth(-1)/2); });
    }
    
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

    TextArea text = new TextArea2(); text.setFont(Font.Arial10.deriveFont(24)); text.setText(str);
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

public static class TextArea2 extends TextArea {
    
    public void setOpacity(double aValue)
    {
        System.out.println("Opacity: " + aValue + " " + getAnim(0).getTime());
        if(getAnim(0).getTime()>2000)
            System.out.println("WTF");
        super.setOpacity(aValue);
    }
}

}