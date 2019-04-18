package comics.app;
import comics.script.*;
import snap.gfx.*;
import snap.view.*;

/**
 * A class to manage UI editing of Script.
 */
public class ScriptEditor extends ViewOwner {
    
    // The EditorPane
    EditorPane       _editorPane;

    // The ScriptView
    ScriptView       _scriptView;
    
    // The Script line count
    int              _scriptLineCount;
    
/**
 * Creates a ScriptEditor.
 */
public ScriptEditor(EditorPane anEP)
{
    _editorPane = anEP;
}

/**
 * Returns the PlayerView.
 */
public PlayerView getPlayer()  { return _editorPane.getPlayer(); }

/**
 * Returns the Script.
 */
public Script getScript()  { return _editorPane.getScript(); }

/**
 * Returns the ScriptLine.
 */
public ScriptLine getScriptLine()  { return _editorPane.getScriptLine(); }

/**
 * Runs the current line.
 */
public void runCurrentLine()
{
    // Get current line (or first non empty line above)
    TextBoxLine line = _scriptView.getSel().getStartLine();
    while(line.getString().trim().length()==0 && line.getIndex()>0)
        line = _scriptView.getTextArea().getLine(line.getIndex()-1);
    
    // Run line at index
    int lineIndex = line.getIndex();
    getPlayer().stop();
    getPlayer().playLine(lineIndex);
}

/**
 * Sets player to line end.
 */
public void setPlayerRunTimeToLineEnd(int aLineIndex)
{
    int time = getPlayer().getLineEndTime(aLineIndex); if(time>0) time--;
    getPlayer().stop();
    getPlayer().setRunTime(time);
    getPlayer().playLine();
}

/**
 * Called when Script text changes.
 */
protected void scriptChanged()
{
    if(_scriptView==null) return;
    _scriptView.setText(getPlayer().getScriptText());
    _scriptLineCount = _scriptView.getTextArea().getLineCount();
}

/**
 * Called when PlayerView.RunLine changes.
 */
void playerRunLineChanged()
{
    _scriptView.setRunLine(getPlayer().getRunLine());
}

/**
 * Creates UI.
 */
protected View createUI()
{
    // Create ToolBar
    RowView toolBar = new RowView();
    Label label = new Label("Script:"); label.setFont(Font.Arial16.getBold());
    Button btn = new Button("Edit Line"); btn.setName("EditLineButton");
    btn.setLeanX(HPos.RIGHT); btn.setPrefSize(95,26);
    toolBar.setChildren(label, btn);
    
    // Get/configure ScriptView
    _scriptView = new ScriptView(this);
    _scriptView.setGrowHeight(true); _scriptView.setPrefHeight(180);
    _scriptView.setText(PlayerPane.DEFAULT_SCRIPT);
    _scriptView.setSel(_scriptView.length());
    _scriptView.addEventFilter(e -> scriptViewDidKeyRelease(e), KeyRelease);
    setFirstFocus(_scriptView.getTextArea());
    
    //<ColView Padding="8,4,4,4" GrowHeight="true" FillWidth="true" Title="Cast" />
    ColView colView = new ColView(); colView.setPadding(8,5,5,5); colView.setSpacing(4);
    colView.setGrowHeight(true); colView.setFillWidth(true);
    colView.addChild(toolBar);
    colView.addChild(_scriptView);
    
    return colView;
}

/**
 * Init UI.
 */
protected void initUI()
{
    getPlayer().addPropChangeListener(pc -> playerRunLineChanged(), PlayerView.RunLine_Prop);
}

/**
 * Reset UI.
 */
protected void resetUI()
{
    // Update ScriptView text if changed externally
    String scriptText = getPlayer().getScriptText();
    if(!scriptText.equals(_scriptView.getText()))
        _scriptView.setText(scriptText);
}

/**
 * Respond to UI.
 */
protected void respondUI(ViewEvent anEvent)
{
    if(anEvent.equals("EditLineButton"))
        _editorPane.showLineEditor();
}

/**
 * Called when user hits Key in ScriptView.
 */
void scriptViewDidKeyRelease(ViewEvent anEvent)
{
    if(_scriptLineCount!=_scriptView.getTextArea().getLineCount()) { 
        _scriptLineCount = _scriptView.getTextArea().getLineCount();
        getPlayer().setScriptText(_scriptView.getText());
        runCurrentLine();
    }
}

}