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
    
    // The script text view
    ScriptView   _textView;
    
    // The HelpPane
    HelpPane     _helpPane = new HelpPane(this);
    
    // Whether script needs to be reset
    boolean      _resetScript;
    
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
    
    runLater(() -> getPlayer().runLine(0));
}

/**
 * Returns the PlayerView.
 */
public PlayerView getPlayer()
{
    if(_resetScript) { _player.setScriptText(_textView.getText()); _resetScript = false; }
    return _player;
}

/**
 * Returns the StageView.
 */
public StageView getStage()  { return getPlayer().getStage(); }

/**
 * Sets the script lines.
 */
public void setScriptLines(String theLines[])
{
    String text = StringUtils.join(theLines, "\n");
    _textView.setText(text);
    resetScript();
}

/**
 * Resets the script.
 */
protected void resetScript()  { _resetScript = true; }

/**
 * Initialize the UI.
 */
protected void initUI()
{
    // Create PlayerView
    _player = new PlayerView();
    _player.addPropChangeListener(pc -> playerRunLineChanged(), PlayerView.RunLine_Prop);
    
    // Watch for clicks on StageView
    StageView stage = _player.getStage();
    enableEvents(stage, MousePress);
    
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
    if(anEvent.equals("ResetButton")) {
        getPlayer().stop(); getPlayer().setRunTime(0); }
        
    // Handle RunButton
    if(anEvent.equals("RunButton")) { resetScript();
        getPlayer().setRunTime(0); getPlayer().play(); }
    
    // Handle AgainButton
    if(anEvent.equals("AgainButton"))
        runCurrentLine();
    
    // Handle Stage MousePressed
    if(anEvent.isMousePress())
        selectSettingItem(anEvent.getX(), anEvent.getY());
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
    resetScript();
    getPlayer().stop();
    getPlayer().runLine(lineIndex);
}

/**
 * Selects a setting item.
 */
public void selectSettingItem(double aX, double aY)
{
    // Get child at point
    View child = getStage().getChildAt(aX, aY);
    if(child instanceof Actor) {
        String name = child.getName(); name = FilePathUtils.getFileNameSimple(name);
        _helpPane.addToScript(name);
    }
}

/**
 * Called when PlayerView.RunLine changes.
 */
void playerRunLineChanged()
{
    _textView.setRunLine(_player.getRunLine());
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