package comics.app;
import comics.player.*;
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
    
    // Constants
    static Color INPUTTEXT_SEL_COLOR = new Color("#CDECF6");
    static String _stars[] = { "Setting", "Camera", "Lady", "Man", "Car", "Cat", "Dog", "Trump", "Obama", "Duke" };
    
/**
 * Creates a ScriptEditor.
 */
public ScriptEditor(EditorPane anEP)  { _editorPane = anEP; }

/**
 * Returns the Player.
 */
public PlayerView getPlayer()  { return _editorPane.getPlayer(); }

/**
 * Returns the Script.
 */
public Script getScript()  { return _editorPane.getScript(); }

/** Conveniences. */
int getSelIndex()  { return _editorPane.getSelIndex(); }
void setSelIndex(int anIndex)  { _editorPane.setSelIndex(anIndex); }
ScriptLine getSelLine()  { return _editorPane.getSelLine(); }
void selectPrev()  { _editorPane.selectPrev(); }
void selectNext()  { _editorPane.selectNext(); }
void selectNextWithInsert()  { _editorPane.selectNextWithInsert(); }
void runCurrentLine()  { _editorPane.runCurrentLine(); }
void delete()  { _editorPane.delete(); }

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
        _editorPane.setLineText(str, ind);
    }
    
    // If new line
    else {
        
        // If no new text, select next line
        if(str.length()==0) {
            selectNext(); return; }
            
        // Add new line
        ind = EditorPane.negateIndex(ind);
        _editorPane.addLineText(str, ind);
    }
    runCurrentLine();
    _scriptView.requestFocus();
}

/**
 * Called when Script text changes.
 */
protected void scriptChanged()
{
    _scriptView.setScript(null);
    _scriptView.setScript(getScript());
    resetLater();
}

/**
 * Called when ScriptView gets KeyPress event.
 */
void scriptViewDidKeyPress(ViewEvent anEvent)
{
    // Handle Delete, BackSpaceKey, Up/Down arrow
    if(anEvent.isDeleteKey() || anEvent.isBackSpaceKey()) delete();
    else if(anEvent.isUpArrow()) selectPrev();
    else if(anEvent.isDownArrow()) selectNext();
        
    // Handle tab key
    else if(anEvent.isTabKey()) {
        if(anEvent.isShiftDown()) selectPrev();
        else selectNext();
    }
        
    // Handle enter key
    else if(anEvent.isEnterKey()) {
        if(anEvent.isShiftDown()) selectPrev();
        else selectNextWithInsert();
    }
    
    // Handle letter or digit
    else {
        char c = anEvent.getKeyChar();
        if(Character.isLetterOrDigit(c)) {
            _inputText.requestFocus();
            _inputText.selectAll();
            ViewUtils.processEvent(_inputText, anEvent);
        }
    }
    anEvent.consume();
}

/** Called when ScriptView gets MouseRelease event. */
void scriptViewDidMouseRelease(ViewEvent anEvent)  { if(anEvent.getClickCount()==2) _editorPane.showLineEditor(); }

/**
 * Called when InputText does KeyPress to handle tab & delete keys.
 */
void inputTextDidKeyPress(ViewEvent anEvent)
{
    // Handle Tab key
    if(anEvent.isTabKey() && getScript().getLineCount()>0) {
        int ind = getSelIndex(); if(ind<0) ind = EditorPane.negateIndex(ind);
        ind = (ind+1) % getScript().getLineCount();
        setSelIndex(ind);
        runCurrentLine();
    }
    
    // Handle Delete, BackSpace keys
    if((anEvent.isDeleteKey() || anEvent.isBackSpaceKey()) && _inputText.length()==0) {
        _inputText.escape(anEvent);
        delete();
        anEvent.consume();
    }
}

/**
 * Called when HelpListView does Action event.
 */
void helpListViewDidAction(String aStr)
{
    ScriptLine line = getSelLine();
    String starName = line!=null && line.getStar()!=null? line.getStar().getStarName() : null;
    String str = starName!=null? starName + ' ' + aStr : aStr;
    setLineText(str);
    
    // Reset UI
    _helpListView.setSelIndex(-1);
    resetLater();
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
    _scriptView = new ScriptView();
    ScrollView scriptScrollView = new ScrollView(_scriptView);
    scriptScrollView.setGrowWidth(true); scriptScrollView.setGrowHeight(true); scriptScrollView.setPrefHeight(180);
    mainRowView.addChild(scriptScrollView, 0);
    
    // Get/configure InputRow 
    RowView inputRow = (RowView)mainColView.getChild("InputRow"); inputRow.setFillHeight(true);
    
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
    
    // Configure ScriptView
    _scriptView.setScript(getScript());
    _scriptView.addEventFilter(e -> scriptViewDidKeyPress(e), KeyPress);
    _scriptView.addEventFilter(e -> scriptViewDidMouseRelease(e), MouseRelease);
    setFirstFocus(_scriptView);
    
    // Get/configure HelpListView
    _helpListView = getView("HelpListView", ListView.class);
    _helpListView.setFont(Font.Arial16);
    _helpListView.setFocusWhenPressed(false); _helpListView.getListArea().setFocusWhenPressed(false);
    
    // Get/Configure InputText
    _inputText = getView("InputText", TextField.class); _inputText.setRadius(10);
    _inputText.addEventFilter(e -> inputTextDidKeyPress(e), KeyPress);
    if(_inputText.getWidth()<0) ((ComicTextField)_inputText).paintSel(null); // Forces TeaVM to compile ComicTextField
    
    // Get/Configure InputButton
    Button inputButton = getView("InputButton", Button.class); inputButton.setText("\u23CE");
}

/**
 * Reset UI.
 */
protected void resetUI()
{
    // Get seleccted ScriptLine and star
    ScriptLine line = getSelLine();
    Star star = line!=null? line.getStar() : null;
    
    // Update UndoButton, RedoButton
    setViewEnabled("UndoButton", getScript().getUndoer().hasUndos());
    setViewEnabled("RedoButton", getScript().getUndoer().hasRedos());
    
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
    
    // Handle ClearButton, SamplesButton
    if(anEvent.equals("ClearButton")) getScript().setText("Setting is blank");
    if(anEvent.equals("SamplesButton")) new SamplesPane().showSamples(_editorPane);
        
    // Handle ScriptView
    if(anEvent.equals("ScriptView")) {
        getPlayer().stop();
        int ind = _scriptView.getSelIndex();
        setSelIndex(ind);
        if(ind>=0) runCurrentLine();
        else _inputText.requestFocus();
    }
    
    // Handle HelpListView
    if(anEvent.equals("HelpListView")) {
        _scriptView.requestFocus();
        String str = _helpListView.getSelItem();
        ViewUtils.runOnMouseUp(() -> helpListViewDidAction(str));
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