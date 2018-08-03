package comics;
import snap.gfx.*;
import snap.view.*;
import snap.viewx.*;
import snap.util.*;
import snap.web.*;

/**
 * A class to represent a line of script.
 */
public class ScriptLine {
    
    // The Script
    Script     _script;
    
    // The line text
    String     _text;
    
    // The words
    String     _words[];
    
    // The word index
    int        _index;
    
    // The Stage
    SnapScene  _stage;
    
    // The start time
    int        _startTime;
    
    // The run time
    int        _runTime;
    
/**
 * Creates a new ScriptLine with given text.
 */
public ScriptLine(Script aScript, String aStr)
{
    _script = aScript; _text = aStr.toLowerCase();
}

/**
 * Returns the text.
 */
public String getText()  { return _text; }

/**
 * Returns the words.
 */
public String[] getWords()
{
    if(_words!=null) return _words;
    return _words = getText().split("\\s");
}

/**
 * Executes line for Stage.
 */
public int run(SnapScene aStage)
{
    _stage = aStage;
    _startTime = 0;
    
    String words[] = getWords();
    for(_index=0;_index<words.length;_index++) { String word = words[_index].toLowerCase();
    
        if(word.equals("setting")) runSetting();
        if(word.equals("walks")) runWalks();
        if(word.equals("drops")) runDrops();
        if(word.equals("grows")) runGrows();
        if(word.equals("says")) runSays();
        if(word.equals("flips")) runFlips();
        if(word.equals("explodes")) runExplodes();
    }
    
    // Register for OnFinish callback and play
    _stage.getAnim(0).getAnim(_runTime).setOnFinish(a -> runFinished());
    _stage.playAnimDeep();
    
    return _runTime;
}

/**
 * Called when script line is finished.
 */
public void runFinished()
{
    // Clear all anims
    for(View child : _stage.getChildren()) child.getAnimCleared(0);
    
    // Trigger next script line
    ViewUtils.runLater(() -> _script.runNextLine(_stage));
}

/**
 * Runs the setting.
 */
public void runSetting()
{
    // Get setting
    ImageView iview = getNextImageView(); if(iview==null) return;
    iview.setSize(_stage.getWidth(), _stage.getHeight()); iview.setFillWidth(true); iview.setFillHeight(true);
    iview.setPrefWidth(-1); iview.setKeepAspect(false);
    iview.setName("Setting");
    
    // If old setting, remove
    View oldStg = getView("Setting"); if(oldStg!=null) _stage.removeChild(oldStg);
    
    // Add new setting
    _stage.addChild(iview,0);
}

/**
 * Runs a walk command.
 */
public void runWalks()
{
    _index = -1;
    ImageView iview = (ImageView)getView();
    if(iview==null) { iview = getNextImageView(); if(iview.getParent()==null) _stage.addChild(iview); }
    if(iview==null) { System.err.println("No Image found"); return; }
    
    if(ArrayUtils.contains(_words, "right")) {
        iview.setXY(_stage.getWidth(), _stage.getHeight() - iview.getHeight() - 10); //_stage.addChild(iview);
        iview.getAnim(_startTime).getAnim(_startTime+2000).setX(_stage.getWidth()/2+60);//.play();
    }
    
    else if(ArrayUtils.contains(_words, "out")) {
        //iview = (ImageView)getView(); if(iview==null) return;
        iview.getAnim(_startTime).getAnim(_startTime+2000).setX(_stage.getWidth());//.play();
    }
    
    else {
        iview.setXY(-iview.getWidth(), _stage.getHeight() - iview.getHeight() - 10); //_stage.addChild(iview);
        iview.getAnim(_startTime).getAnim(_startTime+2000).setX(_stage.getWidth()/2-120);//.play();
    }
    
    // Look for animation
    String name = FilePathUtils.getFileNameSimple(iview.getName());
    WebURL url = WebURL.getURL("/Temp/ComicScriptLib/images/" + name + "Walking" + ".gif");
    if(url.isFound()) {
        Image img = Image.get(url), img0 = iview.getImage(); ImageView iview2 = iview;
        iview.setImage(img); iview.setWidth(iview.getPrefWidth(-1)/2);
        iview.getAnim(_startTime).getAnim(_startTime+2000).setValue("Frame", 36);
        iview.getAnim(_startTime).getAnim(_startTime+2000).setOnFinish(a -> {
            iview2.setImage(img0); iview2.setWidth(iview2.getPrefWidth(-1)/2); });
    }
    
    _index = _words.length;
    _runTime = 2000;
}

/**
 * Runs a walk command.
 */
public void runDrops()
{
    _index = -1;
    ImageView iview = getNextImageView(); if(iview==null) return;
    
    if(ArrayUtils.contains(_words, "right")) {
        iview.setSize(80,240);
        iview.setXY(_stage.getWidth()/2+60,-iview.getHeight());
        _stage.addChild(iview);
        iview.getAnim(_startTime).getAnim(_startTime+2000).setY(200).play();
    }
    
    else {
        iview.setSize(80,240);
        iview.setXY(_stage.getWidth()/2-120,-iview.getHeight());
        _stage.addChild(iview);
        iview.getAnim(_startTime).getAnim(_startTime+2000).setY(200).play();
    }
    
    _index = _words.length;
    _runTime = 2000;
}

/**
 * Runs a grows command.
 */
public void runGrows()
{
    _index = -1;
    
    ImageView iview = (ImageView)getView(); if(iview==null) return;
    iview.getAnim(_startTime).getAnim(_startTime+1000).setScale(iview.getScale()+.1).play();
    
    _index = _words.length;
    _runTime = 1000;
}

/**
 * Runs a flips command.
 */
public void runFlips()
{
    _index = -1;
    
    ImageView iview = (ImageView)getView(); if(iview==null) return;
    iview.getAnim(_startTime).getAnim(_startTime+1000).setRotate(iview.getRotate()+360).play();
    
    _index = _words.length;
    _runTime = 1000;
}

/**
 * Runs a walk command.
 */
public void runSays()
{
    int index = _text.indexOf("says,");
    if(index<0) index = _text.indexOf("says"); if(index<0) return;

    String str = _text.substring(index+4).trim();

    TextArea text = new TextArea(); text.setFont(Font.Arial10.deriveFont(24));
    text.setFill(Color.WHITE);
    text.setAlign(Pos.CENTER);
    text.setBorder(Border.createLineBorder(Color.BLACK,2));
    text.setText(str);
    text.setBounds(_stage.getWidth()/2-150, 100, 300,60);
    text.scaleTextToFit();
    text.setOpacity(0);
    _stage.addChild(text);
    text.getAnim(_startTime).getAnim(_startTime+500).setOpacity(1).getAnim(_startTime+500+2000)
        .getAnim(_startTime+500+2000+500).setOpacity(0).play();
    _runTime = 3000;
}

/**
 * Runs a walk command.
 */
public void runExplodes()
{
    View child = getView(); if(child==null) return;

    Explode.explode(child, _startTime);
    _index = _words.length;
    _runTime = 2500;
}

/**
 * Returns the next image.
 */
public View getView()
{
    int ind = _index; _index = -1;
    Image img = getNextImage();
    String name = img!=null? img.getName() : null;
    View child = getView(name);
    _index = ind;
    return child;
}

/**
 * Returns the view with name.
 */
public View getView(String aName)
{
    View child = aName!=null? _stage.getChild(aName) : null;
    return child;
}

/**
 * Returns the next image.
 */
public Image getNextImage()
{
    for(int i=_index+1;i<_words.length;i++) { String word = _words[i].toLowerCase();
        String word2 = Character.toUpperCase(word.charAt(0)) + word.substring(1);
    
        // Look for jpg
        WebURL url = WebURL.getURL(getClass(), "/images/" + word + ".jpg");
        if(SnapUtils.isTeaVM && url!=null && !url.isFound()) url = null;
        if(url==null) url = WebURL.getURL("/Temp/ComicScriptLib/images/" + word2 + ".jpg"); if(!url.isFound()) url = null;

        // Look for png
        if(url==null) url = WebURL.getURL(getClass(), "/images/" + word + ".png");
        if(url==null) url = WebURL.getURL("/Temp/ComicScriptLib/images/" + word2 + ".png"); if(!url.isFound()) url = null;
        
        // Look for gif
        if(url==null) url = WebURL.getURL("/Temp/ComicScriptLib/images/" + word2 + ".gif"); if(!url.isFound()) url = null;
        
        // Get file from URL and load image
        WebFile file = url!=null? url.getFile() : null;
        if(file!=null)
            return Image.get(file);
    }
    _index = _words.length;
    return null;
}

/**
 * Returns the next image.
 */
public ImageView getNextImageView()
{
    Image img = getNextImage(); if(img==null) return null;
    ImageView iview = new ImageView(img); iview.setName(img.getName());
    if(!img.isLoaded())
        img.addPropChangeListener(pc -> iview.repaint());
    iview.setSize(iview.getPrefWidth(-1)/2, iview.getPrefHeight(-1)/2);
    iview.setFillHeight(true); iview.setKeepAspect(true);
    return iview;
}

}