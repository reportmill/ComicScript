package comics;
import snap.gfx.*;
import snap.view.*;
import snap.viewx.*;

/**
 * A class to represent a line of script.
 */
public class ScriptLine {
    
    // The Script
    Script      _script;
    
    // The line text
    String      _text;
    
    // The words
    String      _words[];
    
    // The word index
    int         _index;
    
    // The camera
    CameraView  _camera;
    
    // The Stage
    SnapScene   _stage;
    
    // The start time
    int         _startTime;
    
    // The run time
    int         _runTime;
    
/**
 * Creates a new ScriptLine with given text.
 */
public ScriptLine(Script aScript, String aStr)
{
    _script = aScript; _text = aStr;
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
    // If already set, just return
    if(_words!=null) return _words;
    
    // Get text and words
    String text = getText().toLowerCase();
    String words[] = text.split("\\s");
    for(int i=0;i<words.length;i++) words[i] = words[i].replace(",","").replace("\"","");
    return _words = words;
}

/**
 * Executes line for Stage.
 */
public int run(SnapScene aStage)
{
    _stage = aStage;
    _camera = _stage.getParent(CameraView.class);
    _startTime = 0;
    
    String words[] = getWords();
    
    String word = words[0];
    
    // Handle setting
    if(word.equals("setting")) runSetting();
    
    // Handle camera
    else if(word.equals("camera")) {
        CameraView camera = (CameraView)_stage.getParent();
        camera.run(words[1], words);
        _runTime = camera._runTime;
    }
    
    // Handle view
    else {
        
        // Get actor
        Actor actor = (Actor)getView(); if(actor==null) return 0;
        actor._stage = _stage; actor._scriptLine = this;
        if(!actor.getImage().isLoaded()) {
            ViewUtils.runDelayed(() -> run(aStage), 50, true); return 0; }
        
        // Run command
        actor.run(words[1], words);
        if(actor._runTime<0) {
            ViewUtils.runDelayed(() -> run(aStage), 50, true); return 0; }
        _runTime = actor._runTime;
    }
    
    // Register for OnFinish callback and play
    _camera.getAnim(0).getAnim(_runTime).setOnFinish(a -> ViewUtils.runLater(() -> runFinished(a)));
    _camera.playAnimDeep();
    
    return _runTime;
}

/**
 * Called when script line is finished.
 */
public void runFinished(ViewAnim anAnim)
{
    // Clear all anims
    _camera.getAnimCleared(0);
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
        ImageView iview = new Actor(img); iview.setName(img.getName()); child = iview; iview.setX(-999);
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
    for(int i=_index+1;i<_words.length;i++) { String word = _words[i];
        
        // Look for actor/setting
        Image img = Index.get().getActorImage(word);
        if(img==null) img = Index.get().getSettingImage(word);
        
        // Get file from URL and load image
        if(img!=null)
            return img;
    }
    _index = _words.length;
    return null;
}

}