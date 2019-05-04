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
    static Color INPUTTEXT_SEL_COLOR = new Color("#CDECF6");
    static String _stars[] = { "Setting", "Camera", "Lady", "Man", "Car", "Cat", "Dog", "Trump", "Obama", "Duke" };
    
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
 * Adds a ScriptLine for given string at given index.
 */
public void addLineText(String aStr, int anIndex)  { _editorPane.addLineText(aStr, anIndex); }

/**
 * Sets ScriptLine text to given string at given index.
 */
public void setLineText(String aStr, int anIndex)  { _editorPane.setLineText(aStr, anIndex); }

/**
 * Deletes the current selected line.
 */
public void delete()  { _editorPane.delete(); }

/**
 * Sets selected ScriptLine to text for given string at given index.
 */
public void setLineText(String aStr)
{
    // Get selected line index and new string
    int ind = getSelIndex(); String str = aStr.trim();
    
    // If selected line
    if(ind>=0) {
        
        // If text hasn't changed, select new
        if(getScript().getLine(ind).getText().equals(str)) {
           selectNextWithInsert(); return; }
           
        // Set line to new text
        setLineText(str, ind);
    }
    
    // If new line
    else {
        
        // If no new text, select next line
        if(str.length()==0) {
            selectNext(); return; }
            
        // Add new line
        ind = EditorPane.negateIndex(ind);
        addLineText(str, ind);
    }
    runCurrentLine();
    _scriptView.requestFocus();
}

/**
 * Returns the selected ScriptLine index.
 */
public int getSelIndex()  { return _editorPane.getSelIndex(); }

/**
 * Sets the selected ScriptLine index.
 */
public void setSelIndex(int anIndex)  { _editorPane.setSelIndex(anIndex); } //if(anIndex>=0) runCurrentLine();

/**
 * Returns the selected ScriptLine.
 */
public ScriptLine getSelLine()  { return _editorPane.getSelLine(); }

/**
 * Selects previous line.
 */
public void selectPrev()  { _editorPane.selectPrev(); }

/**
 * Selects next line.
 */
public void selectNext()  { _editorPane.selectNext(); }

/**
 * Selects next line.
 */
public void selectNextWithInsert()  { _editorPane.selectNextWithInsert(); }

/**
 * Runs the current line.
 */
public void runCurrentLine()  { _editorPane.runCurrentLine(); }

/**
 * Called when Script text changes.
 */
protected void scriptChanged()
{
    _scriptView.scriptChanged();
    resetLater();
}

/**
 * Called when InputText does KeyRelease to catch delete.
 */
void inputTextDidKeyPress(ViewEvent anEvent)
{
    if(anEvent.isTabKey() && getScript().getLineCount()>0) {
        int ind = getSelIndex(); if(ind<0) ind = EditorPane.negateIndex(ind);
        ind = (ind+1) % getScript().getLineCount();
        setSelIndex(ind);
        runCurrentLine();
    }
    
    if((anEvent.isDeleteKey() || anEvent.isBackSpaceKey()) && _inputText.length()==0) {
        //ViewUtils.fireActionEvent(_inputText, anEvent);
        _inputText.escape(anEvent);
        delete();
        anEvent.consume();
    }
}

/**
 * Creates UI.
 */
protected View createUI()
{
    // Get main ColView from UI file
    ColView mainColView = (ColView)super.createUI(); mainColView.setFillWidth(true);
    
    // Get main RowView
    RowView mainRowView = (RowView)mainColView.getChild("MainRowView"); mainRowView.setFillHeight(true);
    
    // Create/configure ScriptView add to MainRowView
    _scriptView = new ScriptView(this);
    ScrollView scriptScrollView = new ScrollView(_scriptView);
    scriptScrollView.setGrowWidth(true); scriptScrollView.setGrowHeight(true); scriptScrollView.setPrefHeight(180);
    mainRowView.addChild(scriptScrollView, 0);
    
    // Get/configure InputRow, InputText, InputButton
    RowView inputRow = (RowView)mainColView.getChild("InputRow"); inputRow.setFillHeight(true);
    _inputText = (TextField)inputRow.getChild("InputText"); _inputText.setRadius(10);
    _inputText.addEventFilter(e -> inputTextDidKeyPress(e), KeyPress);
    if(_inputText.getWidth()<0) ((ComicTextField)_inputText).paintSel(null); // Forces TeaVM to compile ComicTextField
    Button inputButton = (Button)inputRow.getChild("InputButton"); inputButton.setText("\u23CE");
    
    // Return MainColView    
    return mainColView;
}

/**
 * Init UI.
 */
protected void initUI()
{
    // List for ScriptLabel MousePress
    enableEvents("ScriptLabel", MousePress);
    
    // Make ScriptView FirstFocus
    setFirstFocus(_scriptView);
    
    // Get/configure HelpListView
    _helpListView = getView("HelpListView", ListView.class);
    _helpListView.setFont(Font.Arial16);
    _helpListView.setFocusWhenPressed(false); _helpListView.getListArea().setFocusWhenPressed(false);
}

/**
 * Reset UI.
 */
protected void resetUI()
{
    // Get seleccted ScriptLine and star
    ScriptLine line = getSelLine();
    Star star = line!=null? line.getStar() : null;
    
    // Update ScriptView
    _scriptView.setSelIndex(getSelIndex());
    
    // Update HelpListView
    String helpItems[] = star!=null? star.getActionNames() : _stars;
    setViewText("HelpListLabel", star!=null? "Actions" : "Subjects");
    _helpListView.setItems(helpItems);

    // Update InputText
    _inputText.setText(line!=null? line.getText() : "");
    _inputText.selectAll();
}

/**
 * Respond to UI.
 */
protected void respondUI(ViewEvent anEvent)
{
    // Handle ScriptLabel
    if(anEvent.equals("ScriptLabel")) getPlayer().showIntroAnim();
    
    // Handle CutButton, DeleteButton
    if(anEvent.equals("CutButton")) delete();
    if(anEvent.equals("DeleteButton")) delete();
    
    // Handle UndoButton, RedoButton
    if(anEvent.equals("UndoButton")) getScript().undo();
    if(anEvent.equals("RedoButton")) getScript().redo();
    
    // Handle SamplesButton
    if(anEvent.equals("SamplesButton"))
        new SamplesPane().showSamples(_editorPane);
        
    // Handle ScriptView
    if(anEvent.equals(_scriptView)) {
        getPlayer().stop();
        int ind = _scriptView.getSelIndex();
        setSelIndex(ind);
        if(ind>=0) runCurrentLine();
        else _inputText.requestFocus();
    }
    
    // Handle HelpListView
    if(anEvent.equals("HelpListView")) {
        _scriptView.requestFocus();
        ScriptLine line = getSelLine();
        String starName = line!=null && line.getStar()!=null? line.getStar().getStarName() : null;
        String str = _helpListView.getSelItem();
        String str2 = starName!=null? starName + ' ' + str : str;
        ViewUtils.runOnMouseUp(() -> {
            setLineText(str2);
            _helpListView.setSelIndex(-1);
            resetLater();
        });
    }
    
    // Handle InputText
    if(anEvent.equals("InputText"))
        setLineText(anEvent.getStringValue().trim());

    // Handle InputButton
    if(anEvent.equals("InputButton"))
        runLater(() -> ViewUtils.fireActionEvent(_inputText, anEvent));
        
    // Handle EditLineButton
    if(anEvent.equals("EditLineButton"))
        _editorPane.showLineEditor();
}

/**
 * A TextField subclass to paint selection even when not focused.
 */
public static class ComicTextField extends TextField {
   
    /** Override to paint sel even when not focused. */ 
    protected void paintSel(Painter aPntr)
    {
        super.paintSel(aPntr);
        if(!isFocused() && !isSelEmpty()) {
            Rect sbnds = getSelBounds();
            aPntr.setPaint(INPUTTEXT_SEL_COLOR); aPntr.fill(sbnds);
        }
    }
}

}