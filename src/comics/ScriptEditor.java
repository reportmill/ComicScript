package comics;
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
    
/**
 * Creates a ScriptEditor.
 */
public ScriptEditor(EditorPane anEP)
{
    _editorPane = anEP;
}

/**
 * Returns the Script.
 */
public Script getScript()  { return _editorPane.getScript(); }

/**
 * Returns the ScriptLine.
 */
public ScriptLine getScriptLine()  { return _editorPane.getScriptLine(); }

/**
 * Updates the script.
 */
public void updateScript()  { }

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
    _scriptView = new ScriptView(_editorPane._playerPane);
    _scriptView.setGrowHeight(true); _scriptView.setPrefHeight(180);
    _scriptView.setText(PlayerPane.DEFAULT_SCRIPT);
    _scriptView.setSel(_scriptView.length());
    _scriptView.addEventFilter(e -> scriptViewReturnKey(e), KeyRelease);
    setFirstFocus(_scriptView.getTextArea());
    
    //<ColView Padding="8,4,4,4" GrowHeight="true" FillWidth="true" Title="Cast" />
    ColView colView = new ColView(); colView.setPadding(8,5,5,5); colView.setSpacing(4);
    colView.setGrowHeight(true); colView.setFillWidth(true);
    colView.addChild(toolBar);
    colView.addChild(_scriptView);
    
    return colView;
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
 * Called when user hits Enter Key in ScriptView.
 */
void scriptViewReturnKey(ViewEvent anEvent)
{
    // Handle EnterKey: Run to previous line
    if(anEvent==null || anEvent.isEnterKey()) {
        //_helpPane.reset();
        _editorPane._playerPane.runCurrentLine();
    }
}

}