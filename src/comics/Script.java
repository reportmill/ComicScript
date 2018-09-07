package comics;
import java.util.*;
import snap.gfx.Image;
import snap.gfx.TextBoxLine;
import snap.view.*;
import snap.viewx.SnapScene;

/**
 * A class to represent the instructions.
 */
public class Script {
    
    // The StagePane
    StagePane          _stagePane;
    
    // The StageView
    SnapScene          _stageView;
    
    // The CameraView
    CameraView         _camera;

    // The TextView
    TextView           _textView;
    
    // The View text
    String             _text;

    // The Script lines
    List <ScriptLine>  _lines;
    
    // The current line index
    int                _lineIndex, _lineIndexMax;
    
    // The current runtime
    int                _runTime;

/**
 * Creates a script for given StagePane.
 */
public Script(StagePane aSP)
{
    _stagePane = aSP;
    _stageView = aSP._stage;
    _camera = aSP._camera;
    _textView = aSP._textView;
    setText(_stagePane._textView.getText());
}

/**
 * Returns the Script text.
 */
public String getText()  { return _text; }

/**
 * Sets the text.
 */
public void setText(String aStr)  { _text = aStr; _lines = null; }

/**
 * Returns the number of lines.
 */
public int getLineCount()  { return getLines().size(); }

/**
 * Returns the line at given index.
 */
public ScriptLine getLine(int anIndex)  { return getLines().get(anIndex); }

/**
 * Returns the lines.
 */
public List <ScriptLine> getLines()
{
    if(_lines!=null) return _lines;
    
    List slines = new ArrayList();
    String tlines[] = _text.split("\\n");
    
    for(String tline : tlines)
        slines.add(new ScriptLine(this, tline));

    return _lines = slines;
}

/**
 * Runs the current line.
 */
public void runLineCurrent()
{
    // Get current line (or first non empty line above)
    TextBoxLine line = _textView.getSel().getStartLine();
    while(line.getString().trim().length()==0 && line.getIndex()>0)
        line = _textView.getTextArea().getLine(line.getIndex()-1);
    
    // Run line at index
    int lineIndex = line.getIndex();
    runLine(lineIndex);
}

/**
 * Runs the script.
 */
public void runAll()
{
    // Reset stage
    _stagePane.resetStage();

    // If no lines, just return
    if(getLineCount()==0) return;
    
    // Run first line with anim
    ScriptLine line0 = getLine(0); _lineIndexMax = getLineCount();
    runLine(line0, false);
}

/**
 * Runs the script.
 */
public void runLine(int lineIndex)
{
    // Reset stage
    _stagePane.resetStage();

    // If invalid line index, just return
    if(lineIndex>=getLineCount()) return;
    
    // Run previous lines instantly
    for(int i=0;i<lineIndex;i++) { ScriptLine line = getLine(i);
        runLine(line, true); }

    // Run requested line with anim
    _lineIndexMax = Math.min(lineIndex+1, getLineCount());
    ScriptLine line = getLine(lineIndex);
    runLine(line, false);
}

/**
 * Executes line for Stage.
 */
protected int runLine(ScriptLine aScriptLine, boolean doInstantly)
{
    // Print which line
    System.out.println("Run Line " + getLines().indexOf(aScriptLine));
    
    // Get script words and first word
    String words[] = aScriptLine.getWords();
    String word = words[0];
    
    // Handle setting
    if(word.equals("setting")) runSetting(aScriptLine);
    
    // Handle camera
    else if(word.equals("camera")) {
        _camera.run(words[1], words);
        _runTime = _camera._runTime;
    }
    
    // Handle view
    else {
        
        // Get actor
        Actor actor = (Actor)getView(aScriptLine); if(actor==null) return 0;
        actor._stage = _stageView; actor._scriptLine = aScriptLine;
        if(!actor.getImage().isLoaded()) {
            ViewUtils.runDelayed(() -> runLine(aScriptLine, doInstantly), 50, true); return 0; }
        
        // Run command
        actor.run(words[1], words);
        if(actor._runTime<0) {
            ViewUtils.runDelayed(() -> runLine(aScriptLine, doInstantly), 50, true); return 0; }
        _runTime = actor._runTime;
    }
    
    // If run instantly, just set time to end
    if(doInstantly) {
        _camera.setAnimTimeDeep(_runTime);
        _camera.getAnimCleared(0);
        _stageView.getAnimCleared(0);
        for(View child : _stageView.getChildren()) child.getAnimCleared(0);
    }
    
    // Register for OnFinish callback and play
    else {
        _camera.getAnim(0).getAnim(_runTime).setOnFinish(a -> ViewUtils.runLater(() -> runFinished(a)));
        _camera.playAnimDeep();
    }
    
    // Update line index
    _lineIndex = aScriptLine.getIndex() + 1;
    
    // Return runtime
    return _runTime;
}

/**
 * Called when script line is finished.
 */
public void runFinished(ViewAnim anAnim)
{
    // Clear all anims
    _camera.getAnimCleared(0);
    _stageView.getAnimCleared(0);
    for(View child : _stageView.getChildren()) child.getAnimCleared(0);
    
    // If at LineIndexMax, return
    if(_lineIndex>=_lineIndexMax) return;

    // Run next line with anim
    ScriptLine sline = getLine(_lineIndex);
    runLine(sline, false);
}

/**
 * Runs the setting.
 */
public void runSetting(ScriptLine aScriptLine)
{
    // Get setting Image and ImageView
    String words[] = aScriptLine.getWords();
    Image img = getNextImage(words, 0); if(img==null) return;
    ImageView iview = new ImageView(img, true, true); iview.setName(img.getName());
    iview.setSize(_stageView.getWidth(), _stageView.getHeight());
    iview.setName("Setting"); //iview.setOpacity(.5);
    
    // If old setting, remove
    View oldStg = getView("Setting"); if(oldStg!=null) _stageView.removeChild(oldStg);
    
    // Add new setting
    _stageView.addChild(iview, 0);
    _runTime = 1;
}

/**
 * Returns the next image.
 */
public View getView(ScriptLine aScriptLine)
{
    // Get image for word
    String words[] = aScriptLine.getWords();
    Image img = getNextImage(words, -1);
    
    // Get actor for image
    String name = img!=null? img.getName() : null;
    View child = getView(name);
    
    // If actor not found, create
    if(child==null) {
        ImageView iview = new Actor(img); iview.setName(img.getName()); child = iview; iview.setX(-999);
        _stageView.addChild(child);
    }
    
    return child;
}

/**
 * Returns the view with name.
 */
public View getView(String aName)
{
    View child = aName!=null? _stageView.getChild(aName) : null;
    return child;
}

/**
 * Returns the next image.
 */
public Image getNextImage(String theWords[], int aStart)
{
    for(int i=aStart+1;i<theWords.length;i++) { String word = theWords[i];
        
        // Look for actor/setting
        Image img = Index.get().getActorImage(word);
        if(img==null) img = Index.get().getSettingImage(word);
        
        // Get file from URL and load image
        if(img!=null)
            return img;
    }
    return null;
}

}