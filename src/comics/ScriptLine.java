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
    
    String word = words[0];
    
    // Handle setting
    if(word.equals("setting")) runSetting();
    
    // Handle view
    else {
        
        // Get actor
        Actor actor = (Actor)getView(); if(actor==null) return 0;
        actor._stage = _stage; actor._scriptLine = this; actor._words = words;
        
        word = words[1];
        if(word.equals("walks")) actor.runWalks();
        if(word.equals("drops")) actor.runDrops();
        if(word.equals("grows")) actor.runGrows();
        if(word.equals("says")) actor.runSays();
        if(word.equals("flips")) actor.runFlips();
        if(word.equals("explodes")) actor.runExplodes();
        if(word.equals("dances")) actor.runDances();
        _runTime = actor._runTime;
    }
    
    // Register for OnFinish callback and play
    _stage.getAnim(0).getAnim(_runTime).setOnFinish(a -> ViewUtils.runLater(() -> runFinished(a)));
    _stage.playAnimDeep();
    
    return _runTime;
}

/**
 * Called when script line is finished.
 */
public void runFinished(ViewAnim anAnim)
{
    // Clear all anims
    _stage.getAnimCleared(0);
    for(View child : _stage.getChildren()) child.getAnimCleared(0);
    
    // Trigger next script line
    ViewUtils.runLater(() -> _script.runNextLine(_stage));
}

/**
 * Runs the setting.
 */
public void runSetting()
{
    // Get setting Image and ImageView
    Image img = getNextImage(); if(img==null) return;
    ImageView iview = new ImageView(img, true, true); iview.setName(img.getName());
    iview.setSize(_stage.getWidth(), _stage.getHeight());
    iview.setName("Setting"); //iview.setOpacity(.5);
    
    // If old setting, remove
    View oldStg = getView("Setting"); if(oldStg!=null) _stage.removeChild(oldStg);
    
    // Add new setting
    _stage.addChild(iview, 0);
    _runTime = 1;
}

/**
 * Returns the next image.
 */
public View getView()
{
    // Get image for word
    int ind = _index; _index = -1;
    Image img = getNextImage();
    
    // Get actor for image
    String name = img!=null? img.getName() : null;
    View child = getView(name);
    
    // If actor not found, create
    if(child==null) {
        ImageView iview = new Actor(img); iview.setName(img.getName()); child = iview;
        iview.setSize(iview.getPrefWidth(-1)/2, iview.getPrefHeight(-1)/2);
        iview.setFillHeight(true); iview.setKeepAspect(true);
        _stage.addChild(child);
    }
    
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

}