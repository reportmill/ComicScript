package comics.app;
import comics.script.*;
import snap.gfx.*;
import snap.view.*;

/**
 * A class to manage UI editing of Script.
 */
public class ScriptEditor extends ViewOwner {
    
    // The EditorPane
    EditorPane           _editorPane;

    // The ScriptView
    ScriptView           _scriptView;
    
    // The ListView showing help suggestions
    ListView <String>    _helpListView;
    
    // The InputText
    TextField            _inputText;
    
    // The Script line count
    int                  _scriptLineCount;
    
    // Constants
    String _stars[] = { "Setting", "Camera", "Lady", "Man", "Car", "Cat", "Dog", "Trump", "Obama", "Duke" };
    String _actActions[] = { "walks", "waves", "jumps", "dances", "drops", "says", "grows", "flips", "explodes" };
    
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
    int lineIndex = getSelIndex(); if(lineIndex<0) lineIndex = ScriptView.negateIndex(lineIndex);
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
    if(!scriptText.equals(_scriptView.getText())) {
        _scriptView.setScript(getScript());
        if(getSelIndex()==-1)
            _scriptView.setSelIndex(-getScript().getLineCount() - 1);
    }
}

/**
 * Called when PlayerView.RunLine changes.
 */
void playerRunLineChanged()
{
    _scriptView.setSelIndex(getPlayer().getRunLine());
    resetLater();
}

/**
 * Called when InputText does KeyRelease to catch delete.
 */
void inputTextDidKeyPress(ViewEvent anEvent)
{
    if(anEvent.isTabKey() && getScript().getLineCount()>0) {
        int ind = getSelIndex(); if(ind<0) ind = ScriptView.negateIndex(ind);
        ind = (ind+1) % getScript().getLineCount();
        _scriptView.setSelIndex(ind);
        runCurrentLine();
    }
    
    if((anEvent.isDeleteKey() || anEvent.isBackSpaceKey()) && _inputText.length()==0) {
        ViewUtils.fireActionEvent(_inputText, anEvent);
        anEvent.consume();
    }
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
    scriptScrollView.setGrowWidth(true); scriptScrollView.setGrowHeight(true); scriptScrollView.setPrefHeight(180);
    
    // Load HelpListView UI
    ColView helpViewBox = (ColView)super.createUI();
    
    // Create a RowView to hold scriptView and helpListView
    RowView rowView = new RowView(); rowView.setSpacing(5); rowView.setGrowHeight(true); rowView.setFillHeight(true);
    rowView.setChildren(scriptScrollView, helpViewBox);
    
    // Create/configure InputText
    _inputText = new TextField(); _inputText.setName("InputText"); _inputText.setGrowWidth(true);
    _inputText.setFont(new Font("Arial", 16)); _inputText.setRadius(10);
    _inputText.addEventFilter(e -> inputTextDidKeyPress(e), KeyPress);
    setFirstFocus(_inputText);
    
    // Create/configure InputButton
    Button inputButton = new Button("\u23CE"); inputButton.setName("InputButton"); inputButton.setPrefWidth(80);
    
    // Create/configure InputText
    RowView inputRow = new RowView(); inputRow.setPadding(4,4,4,4); inputRow.setSpacing(8);
    inputRow.setFillHeight(true);
    inputRow.setChildren(_inputText, inputButton);
    
    //<ColView Padding="8,4,4,4" GrowHeight="true" FillWidth="true" Title="Cast" />
    ColView colView = new ColView(); colView.setPadding(8,5,5,5); colView.setSpacing(4);
    colView.setGrowHeight(true); colView.setFillWidth(true);
    colView.addChild(toolBar);
    colView.addChild(rowView);
    colView.addChild(inputRow);
    
    return colView;
}

/**
 * Init UI.
 */
protected void initUI()
{
    // Get HelpListView
    _helpListView = getView("HelpListView", ListView.class);
    _helpListView.setFont(Font.Arial16);
    _helpListView.setFocusWhenPressed(false); _helpListView.getListArea().setFocusWhenPressed(false);
    
    // List for Player RunLine changes
    getPlayer().addPropChangeListener(pc -> playerRunLineChanged(), PlayerView.RunLine_Prop);
}

/**
 * Reset UI.
 */
protected void resetUI()
{
    // Get seleccted ScriptLine
    ScriptLine sline = getSelLine();
    
    // Update HelpListView
    resetHelpListView();

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
        
    // Handle HelpListView
    if(anEvent.equals("HelpListView")) {
        ScriptLine line = getSelLine();
        String starName = line!=null && line.getStar()!=null? line.getStar().getStarName() : null;
        String str = _helpListView.getSelItem();
        String str2 = starName!=null? starName + ' ' + str : str;
        ViewUtils.runOnMouseUp(() -> {
            modifyScript(str2); });
    }
    
    // Handle InputButton
    if(anEvent.equals("InputButton"))
        runLater(() -> ViewUtils.fireActionEvent(_inputText, anEvent));
        
    // Handle InputText
    if(anEvent.equals("InputText"))
        modifyScript(anEvent.getStringValue().trim());
}

/**
 * Rests the HelpListView.
 */
void resetHelpListView()
{
    ScriptLine line = getSelLine();
    
    // If no line or no star, set list of stars
    if(line==null || line.getStar()==null) {
        setViewText("HelpListLabel", "Subjects");
        _helpListView.setItems(_stars);
    }
    
    // Otherwise, clear list
    else {
        setViewText("HelpListLabel", "Actions");
        _helpListView.setItems(_actActions);
    }
}

/**
 * Modify's the script by replacing current SelLine text with given string.
 */
void modifyScript(String aStr)
{
    // Get selected line index and new string
    int ind = getSelIndex(); String str = aStr.trim();
    
    // If selected line
    if(ind>=0) {
        
        // If text hasn't changed, select new
        if(getScript().getLine(ind).getText().equals(str)) { ind = ScriptView.negateIndex(ind) - 1;
           _scriptView.setSelIndex(ind); return; }
           
        // Set line to new text
        getScript().setLineText(str, ind);
        scriptChanged();
    }
    
    // If new line
    else {
        
        // If no new text, select next line
        if(str.length()==0) { ind = ScriptView.negateIndex(ind);
            if(ind>=getScript().getLineCount()) ind = 0;
            _scriptView.setSelIndex(ind);
            runCurrentLine(); return;
        }
            
        // Add new line
        ind = ScriptView.negateIndex(ind);
        getScript().addLineText(str, ind);
        scriptChanged();
        _scriptView.setSelIndex(ind);
    }
    runCurrentLine(); runCurrentLine(); // This sucks
    runLater(() -> resetLater());
}

}