package comics;
import snap.gfx.Color;
import snap.util.FilePathUtils;
import snap.view.*;
import snap.viewx.*;

/**
 * A class to manage a stage and script.
 */
public class StagePane extends ViewOwner {
    
    // The StageView
    SnapScene    _stage;
    
    // The Stage box
    BoxView      _stageBox;
    
    // The camera view
    CameraView   _camera;
    
    // The script text view
    TextView     _textView;
    
    // The HelpPane
    HelpPane     _helpPane = new HelpPane(this);
    
    // The Script
    Script       _script;
    
    // The default script text
    static String  DEFAULT_SCRIPT = "Setting is beach\n";

/**
 * Shows the stage.
 */
public void showStage()
{
    getWindow().setGrowWidth(true);
    setWindowVisible(true);
    runLater(() -> getScript().runAll());
}

/**
 * Returns the script.
 */
public Script getScript()  { return getScript(false); }

/**
 * Returns the script.
 */
public Script getScript(boolean doUpdate)
{
    if(_script==null || doUpdate) _script = new Script(this);
    return _script;
}

/**
 * Initialize the UI.
 */
protected void initUI()
{
    // Create configure stage
    _stage = new SnapScene();
    _stage.setClipToBounds(true);
    _stage.setBorder(Color.BLACK, 1);
    enableEvents(_stage, MousePress);
    
    // Create/configure camera
    _camera = new CameraView(_stage);
    
    // Create/configure stage box
    _stageBox = new BoxView(); _stageBox.setPadding(10,10,10,10);
    _stageBox.setContent(_camera);
    _stageBox.setClipToBounds(true);

    // Get master ColView and add StageBox
    ColView colView = getUI(ColView.class);
    colView.addChild(_stageBox, 1);
    
    // Get TextRowView
    RowView rowView = getView("TextRowView", RowView.class);
    rowView.removeChild(0);
    
    // Get/configure TextView
    _textView = new ScriptView(this); //getView("ScriptText", TextView.class);
    _textView.setText(DEFAULT_SCRIPT);
    _textView.setSel(_textView.length());
    _textView.addEventFilter(e -> textViewReturnKey(e), KeyRelease);
    setFirstFocus(_textView.getTextArea());
    rowView.addChild(_textView);
    
    // Install HelpPane
    rowView.addChild(_helpPane.getUI());
}

/**
 * Respond to UI.
 */
protected void respondUI(ViewEvent anEvent)
{
    // Handle ResetButton
    if(anEvent.equals("ResetButton"))
        _stage.removeChildren();
        
    // Handle RunButton
    if(anEvent.equals("RunButton"))
        getScript(true).runAll();
    
    // Handle AgainButton
    if(anEvent.equals("AgainButton"))
        getScript(true).runLineCurrent();
    
    // Handle Stage MousePressed
    if(anEvent.isMousePress())
        selectSettingItem(anEvent.getX(), anEvent.getY());
}

/**
 * Selects a setting item.
 */
public void selectSettingItem(double aX, double aY)
{
    // Get child at point
    View child = _stage.getChildAt(aX, aY);
    if(child instanceof Actor) {
        String name = child.getName(); name = FilePathUtils.getFileNameSimple(name);
        _helpPane.addToScript(name);
    }
}

/**
 * Called when user hits Enter Key in TextView.
 */
void textViewReturnKey(ViewEvent anEvent)
{
    // Handle EnterKey: Run to previous line
    if(anEvent==null || anEvent.isEnterKey()) {
        _helpPane.reset();
        getScript(true).runLineCurrent();
    }
}

/**
 * Resets the stage.
 */
public void resetStage()
{
    _stage.removeChildren();
    _camera.setZoom(1);
    _camera.setBlur(0);
}

}