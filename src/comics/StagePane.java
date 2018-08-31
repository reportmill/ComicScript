package comics;
import snap.gfx.Color;
import snap.gfx.ShadowEffect;
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
    _stage = new SnapScene();
    _stage.setClipToBounds(true);
    _stage.setBorder(Color.BLACK, 1);
    _stage.setEffect(new ShadowEffect());
    
    _camera = new CameraView(_stage);
    
    _stageBox = new BoxView(); _stageBox.setPadding(10,10,10,10);
    _stageBox.setContent(_camera);

    ColView colView = getUI(ColView.class);
    colView.addChild(_stageBox, 1);
    
    _script = new Script();
    
    // Install HelpPane
    RowView rowView = getView("TextRowView", RowView.class);
    rowView.addChild(_helpPane.getUI());
    
    // Get TextView and configure
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
    }
    
    // Set Script Text and run to line
    String text = _textView.getText();
    _script.setText(text);
    _script.run(_stage, lineIndex);
}


}