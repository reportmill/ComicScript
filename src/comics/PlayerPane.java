package comics;
import snap.gfx.*;
import snap.util.*;
import snap.view.*;

/**
 * A class to show a PlayerView and allow editing.
 */
public class PlayerPane extends ViewOwner {
    
    // The PlayerView
    PlayerView   _player;
    
    // The TitleView at top of editor UI
    View         _titleView;
    
    // The View that holds the PlayerView
    ColView      _playerBox;
    
    // The View that holds editor UI
    View         _editorView;
    
    // The script text view
    ScriptView   _textView;
    
    // The HelpPane
    HelpPane     _helpPane = new HelpPane(this);
    
    // Whether script needs to be reset
    boolean      _resetScript;
    
    // Whether PlayerPane is showing editing UI
    boolean      _editing;
    
    // The default script text
    static String  DEFAULT_SCRIPT = "Setting is beach\n";

/**
 * Shows the player.
 */
public void showPlayer()
{
    setWindowVisible(true);
    //if(SnapUtils.isTeaVM) getWindow().setMaximized(true);
    
    String lines[] = Samples.getSample("Welcome");
    setScriptLines(lines);

    runLater(() -> getPlayer());
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
 * Returns whether to show editor.
 */
public boolean isEditing()  { return _editing; }

/**
 * Sets whether to show editor.
 */
public void setEditing(boolean aValue)
{
    // If already set, just return
    if(aValue==_editing) return;
    _editing = aValue;
    
    // Fix TitleView, EditorView Visible
    _titleView.setVisible(aValue);
    _editorView.setVisible(aValue);
    _playerBox.setGrowHeight(!aValue); // Don't grow playerbox when editing (should grow EditorView)
    getPlayer().getPlayBar()._editButton.setText(aValue? "Player" : "Edit");
    
    // Enable Editing
    if(aValue) {
        if(!SnapUtils.isTeaVM) {
            Size psize = getWindow().getPrefSize();
            Rect screenRect = ViewEnv.getEnv().getScreenBoundsInset();
            Rect maxRect = screenRect.getRectCenteredInside(psize.width, psize.height);
            getWindow().setMaximizedBounds(maxRect);
        }
        _player.setPadding(20,20,20,20); _player.getCamera().setEffect(new ShadowEffect());
        getWindow().setMaximized(true);
    }
    
    // Disable ShowFull
    else {
        _player.setPadding(0,0,0,0); _player.getCamera().setEffect(null);
        getWindow().setMaximized(false);
    }
}

/**
 * Create UI.
 */
protected View createUI()
{
    ColView colView = (ColView)super.createUI();
    return SplitView.makeSplitView(colView);
}

/**
 * Initialize the UI.
 */
protected void initUI()
{
    // Create PlayerView
    _player = new PlayerView(); _player.setGrowHeight(true);
    _player.addPropChangeListener(pc -> playerRunLineChanged(), PlayerView.RunLine_Prop);
    
    // Watch for clicks on StageView
    StageView stage = _player.getStage();
    //enableEvents(stage, MousePress);
    
    // Get master ColView and add StageBox
    SplitView splitView = getUI(SplitView.class);
    splitView.getDivider(0).setPrefSpan(8);
    _playerBox = (ColView)splitView.getItem(0);
    _playerBox.addChild(_player);
    
    // Get TitleView and configure to be 800 wide so window will be good size
    _titleView = _playerBox.getChild(0);
    _titleView.setPrefWidth(800);
    _titleView.setVisible(false);
    
    // Get EditorView
    _editorView = splitView.getItem(1);
    _editorView.setVisible(false);
    
    // Get TextRowView and remove stand-in TextView
    RowView rowView = getView("TextRowView", RowView.class);
    rowView.removeChild(0);
    
    // Get/configure TextView
    _textView = new ScriptView(this); _textView.setPrefHeight(300);
    _textView.setText(DEFAULT_SCRIPT);
    _textView.setSel(_textView.length());
    _textView.addEventFilter(e -> textViewReturnKey(e), KeyRelease);
    setFirstFocus(_textView.getTextArea());
    rowView.addChild(_textView);
    
    // Install HelpPane
    rowView.addChild(_helpPane.getUI());
    
    if(!SnapUtils.isTeaVM) setEditing(true);
}

/**
 * Respond to UI.
 */
protected void respondUI(ViewEvent anEvent)
{
    // Handle ResetButton
    //if(anEvent.equals("ResetButton")) { getPlayer().stop(); getPlayer().setRunTime(0); }
        
    // Handle RunButton
    if(anEvent.equals("RunButton")) { resetScript();
        getPlayer().play(); }
        
    // Handle EditButton
    if(anEvent.equals("EditButton")) {
        setEditing(!isEditing()); anEvent.consume(); }
    
    // Handle AgainButton
    //if(anEvent.equals("AgainButton")) runCurrentLine();
    
    // Handle Stage MousePressed
    //if(anEvent.isMousePress()) selectSettingItem(anEvent.getX(), anEvent.getY());
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
    getPlayer().playLine(lineIndex);
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