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
    
    // The InputText
    TextField        _inputText;
    
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
 * Returns the selected ScriptLine index.
 */
public int getSelIndex()  { return _scriptView.getSelIndex(); }

/**
 * Returns the selected ScriptLine.
 */
public ScriptLine getSelLine()  { return _scriptView.getSelLine(); }

/**
 * Runs the current line.
 */
public void runCurrentLine()
{
    // Run line at index
    int lineIndex = getSelIndex();
    if(lineIndex<0) lineIndex = 1 - lineIndex;
    getPlayer().stop();
    getPlayer().playLine(lineIndex);
}

/** Sets player to line end. */
/*public void setPlayerRunTimeToLineEnd(int aLineIndex) {
    int time = getPlayer().getLineEndTime(aLineIndex); if(time>0) time--;
    getPlayer().stop(); getPlayer().setRunTime(time); getPlayer().playLine(); }*/

/**
 * Called when Script text changes.
 */
protected void scriptChanged()
{
    if(_scriptView==null) return;
    String scriptText = getScript().getText();
    if(!scriptText.equals(_scriptView.getText()))
        _scriptView.setScript(getScript());
}

/**
 * Called when PlayerView.RunLine changes.
 */
void playerRunLineChanged()
{
    resetLater();
    if(_scriptView.isSettingSel()) return;
    _scriptView.setSelIndex(getPlayer().getRunLine());
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
    
    // Create/configure ScriptView
    _scriptView = new ScriptView(this);
    ScrollView scriptScrollView = new ScrollView(_scriptView);
    scriptScrollView.setGrowHeight(true); scriptScrollView.setPrefHeight(180);
    
    // Create/configure InputText
    _inputText = new TextField(); _inputText.setName("InputText"); _inputText.setGrowWidth(true);
    _inputText.setFont(new Font("Arial", 16)); _inputText.setRadius(10);
    setFirstFocus(_inputText);
    
    // Create/configure InputText
    RowView inputRow = new RowView(); inputRow.setPadding(4,4,4,4);
    inputRow.addChild(_inputText);
    
    //<ColView Padding="8,4,4,4" GrowHeight="true" FillWidth="true" Title="Cast" />
    ColView colView = new ColView(); colView.setPadding(8,5,5,5); colView.setSpacing(4);
    colView.setGrowHeight(true); colView.setFillWidth(true);
    colView.addChild(toolBar);
    colView.addChild(scriptScrollView);
    colView.addChild(inputRow);
    
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
    // Get seleccted ScriptLine
    ScriptLine sline = getSelLine();

    // Update InputText
    _inputText.setText(sline!=null? sline.getText() : "");
    _inputText.selectAll();
}

/**
 * Respond to UI.
 */
protected void respondUI(ViewEvent anEvent)
{
    // Handle EditLineButton
    if(anEvent.equals("EditLineButton"))
        _editorPane.showLineEditor();
        
    // Handle InputText
    if(anEvent.equals("InputText")) {
        int ind = getSelIndex(); String str = anEvent.getStringValue().trim();
        if(ind>=0) {
            getScript().setLineText(str, ind);
            scriptChanged();
        }
        else {
            ind = 1 - ind; ind = getScript().getLineCount();
            getScript().addLineText(str, ind);
            scriptChanged();
            _scriptView.setSelIndex(ind);
        }
        runCurrentLine();
    }
}

}