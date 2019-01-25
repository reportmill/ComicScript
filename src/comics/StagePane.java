package comics;
import snap.gfx.TextBoxLine;
import snap.util.*;
import snap.view.*;

/**
 * A class to manage a stage and script.
 */
public class StagePane extends ViewOwner {
    
    // The PlayerView
    PlayerView   _player;
    
    // The StageView
    StageView    _stage;
    
    // The script text view
    TextView     _textView;
    
    // The HelpPane
    HelpPane     _helpPane = new HelpPane(this);
    
    // The default script text
    static String  DEFAULT_SCRIPT = "Setting is beach\n";

/**
 * Shows the stage.
 */
public void showStage()
{
    setWindowVisible(true);
    if(SnapUtils.isTeaVM) getWindow().setMaximized(true);
    
    String lines[] = Samples.getSample("Welcome");
    setScriptLines(lines);
    
    //runLater(() -> getScript().runAll());
    runLater(() -> setShowControls(true));
}

/**
 * Returns the PlayerView.
 */
public PlayerView getPlayer()  { return getPlayer(false); }

/**
 * Returns the PlayerView with option to update.
 */
public PlayerView getPlayer(boolean doUpdate)
{
    if(doUpdate) _player.getScript().setText(_textView.getText());
    return _player;
}

/**
 * Sets the script lines.
 */
public void setScriptLines(String theLines[])
{
    String text = StringUtils.join(theLines, "\n");
    _textView.setText(text);
}

/**
 * Shows the controls.
 */
public void setShowControls(boolean aValue)
{
    Controls.PlayButton pbtn = new Controls.PlayButton(); pbtn.setOwner(this);
    _player.addChild(pbtn);
    runCurrentLine();
}

/**
 * Initialize the UI.
 */
protected void initUI()
{
    // Create PlayerView
    _player = new PlayerView();
    _stage = _player.getStage();
    enableEvents(_stage, MousePress);
    
    // Get master ColView and add StageBox
    ColView colView = getUI(ColView.class);
    colView.addChild(_player, 1);
    
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
        getPlayer().resetStage();
        
    // Handle RunButton
    if(anEvent.equals("RunButton"))
        getPlayer(true).runAll();
    
    // Handle AgainButton
    if(anEvent.equals("AgainButton"))
        runCurrentLine();
    
    // Handle Stage MousePressed
    if(anEvent.isMousePress())
        selectSettingItem(anEvent.getX(), anEvent.getY());
        
    // Handle PlayButton
    if(anEvent.equals("PlayButton"))
        runLater(() -> getPlayer().runAll());
}

/**
 * Runs the current line.
 */
public void runCurrentLine()
{
    // Get current line (or first non empty line above)
    TextBoxLine line = _textView.getSel().getStartLine();
    while(line.getString().trim().length()==0 && line.getIndex()>0)
        line = _textView.getTextArea().getLine(line.getIndex()-1);
    
    // Run line at index
    int lineIndex = line.getIndex();
    getPlayer(true).runLine(lineIndex);
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
        runCurrentLine();
    }
}

}