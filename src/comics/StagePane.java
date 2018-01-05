package comics;
import snap.view.*;
import snap.viewx.*;

/**
 * A class to manage a stage and script.
 */
public class StagePane extends ViewOwner {
    
    // The StageView
    SnapScene    _stage;
    
    // The script text view
    TextView     _textView;
    
    // The Script
    Script       _script;

/**
 * Initialize the UI.
 */
protected void initUI()
{
    _stage = new SnapScene();

    ColView colView = getUI(ColView.class);
    colView.addChild(_stage, 0);
    
    _script = new Script();
    
    _textView = getView("ScriptText", TextView.class);
    _textView.setText(_script.getText());
    _textView.addEventFilter(e -> textViewReturnKey(e), KeyRelease);
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
        runScript();
}

void textViewReturnKey(ViewEvent anEvent)
{
    if(anEvent.isEnterKey())
        getEnv().runLater(() -> runScript());
}

/**
 * Runs the script.
 */
public void runScript()
{
    _stage.removeChildren();
    String text = _textView.getText();
    _script.setText(text);
    _script.run(_stage);
}


}