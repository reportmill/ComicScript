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

/**
 * Shows the stage.
 */
public void showStage()
{
    getWindow().setGrowWidth(true);
    setWindowVisible(true);
    runLater(() -> runScript(1));
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

    // Get master ColView and add StageBox
    ColView colView = getUI(ColView.class);
    colView.addChild(_stageBox, 1);
    
    // Create script
    _script = new Script();
    
    // Install HelpPane
    RowView rowView = getView("TextRowView", RowView.class);
    rowView.addChild(_helpPane.getUI());
    
    // Get/configure TextView
    _textView = getView("ScriptText", TextView.class);
    _textView.setText(_script.getText());
    _textView.setSel(_textView.length());
    _textView.addEventFilter(e -> textViewReturnKey(e), KeyRelease);
    setFirstFocus(_textView.getTextArea());
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
    if(anEvent.equals("RunButton")) {
        _script._lineIndex = 9999;
        runScript(999);
    }
    
    // Handle AgainButton
    if(anEvent.equals("AgainButton")) {
        runScript(_script.getLineCount()-1);
    }
    
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
        int lineIndex = _textView.getSel().getStartLine().getIndex() - 1;
        getEnv().runLater(() -> runScript(lineIndex));
    }
}

/**
 * Runs the script.
 */
public void runScript(int lineIndex)
{
    // If running to line we've already hit, reset scene
    if(lineIndex<_script._lineIndex) {
        _script._lineIndex = 0;
        _stage.removeChildren();
        _camera.setZoom(1);
        _camera.setBlur(0);
    }
    
    // Set Script Text and run to line
    String text = _textView.getText();
    _script.setText(text);
    _script.run(_stage, lineIndex);
}


}