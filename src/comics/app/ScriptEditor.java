package comics.app;
import comics.player.*;
import java.util.*;
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
    static Font SCRIPTVIEW_FONT = new Font("Arial", 15);
    static Color LINEVIEW_FILL = new Color(.9d);
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
    _scriptView.scriptChanged();
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
    _scriptView.addEventFilter(e -> scriptViewDidKeyPress(e), KeyPress);
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
 * A view subclass to show script lines.
 */
protected class ScriptView extends ColView {
    
    // The selected index
    int                _selIndex = -1;
    
    // The current list of LineViews
    List <LineView>    _lineViews = new ArrayList();
    
    // The cursor lineview
    LineView           _cursorLineView;
    
    // Constants
    Color SELECT_COLOR = Color.get("#039ed3");
    Effect SELECT_EFFECT = new ShadowEffect(8, Color.DARKGRAY, 0, 0);
    Effect SELECT_EFFECT_FOC = new ShadowEffect(8, SELECT_COLOR, 0, 0);

    /** Creates ScriptView. */
    public ScriptView()
    {
        setPadding(8,8,8,12); setSpacing(6); setGrowWidth(true); setGrowHeight(true);
        setFill(Color.WHITE); setFont(SCRIPTVIEW_FONT);
        setFocusable(true); setFocusWhenPressed(true); setFocusKeysEnabled(false);
        enableEvents(MousePress, Action);
        
        // Configure CursorLineView
        _cursorLineView = new LineView(null); _cursorLineView.setPrefSize(150,1);
        _cursorLineView.setMargin(0,0,0,5);
        _cursorLineView.setFill(Color.LIGHTGRAY); _cursorLineView.setEffect(SELECT_EFFECT_FOC);
    }
    
    /** Called when Script changes. */
    protected void scriptChanged()
    {
        // Set script
        Script script = ScriptEditor.this.getScript();
        
        // Iterate over lines and add LineView for each
        removeChildren(); _lineViews.clear();
        for(ScriptLine sline : script.getLines()) {
            LineView lview = new LineView(sline);
            addChild(lview); _lineViews.add(lview);
        }
        addChild(_cursorLineView);
        
        // Reset SelIndex
        _selIndex = -1;
    }
    
    /** Returns the selected index. */
    public int getSelIndex()  { return _selIndex; }
    
    /** Sets the selected index. */
    public void setSelIndex(int anIndex)
    {
        // If already set or too high, just return
        if(anIndex==_selIndex || anIndex>=getChildCount()) return;
        
        // Undecorate last selected LineView
        LineView oldSelLV = getSelLineView(); if(oldSelLV!=null) oldSelLV.setEffect(null);
        
        // Set new value
        _selIndex = anIndex;
        
        // Decorate new selected LineView
        LineView selLineView = getSelLineView();
        if(selLineView!=null) { 
            selLineView.setEffect(getSelEffect());
            ViewUtils.runLater(() -> scrollToVisible(selLineView.getBoundsParent().getInsetRect(-5)));
        }
        
        // Add/remove CursorLineView
        if(_selIndex<0) {
            int ind0 = indexOfChild(_cursorLineView), ind1 = EditorPane.negateIndex(_selIndex);
            if(ind1<ind0) addChild(_cursorLineView, ind1);
            else if(ind1>ind0) { removeChild(_cursorLineView); addChild(_cursorLineView, ind1); }
            ViewUtils.runLater(() -> scrollToVisible(_cursorLineView.getBoundsParent().getInsetRect(-5)));
        }
        _cursorLineView.setVisible(_selIndex<0);
        repaint();
    }
    
    /** Returns the selected index. */
    LineView getSelLineView()  { return _selIndex>=0? _lineViews.get(_selIndex) : null; }
    
    /** Handle Events. */
    protected void processEvent(ViewEvent anEvent)
    {
        // Handle MousePress: Select negative index to trigger insert mode
        if(anEvent.isMousePress()) {
            int ind = 0; for(LineView lview : _lineViews) { if(lview.getY()+5>anEvent.getY()) break; ind++; }
            ind = EditorPane.negateIndex(ind);
            setSelIndex(ind);
            fireActionEvent(anEvent); anEvent.consume();
        }
    }
    
    /** Returns the Select effect. */
    Effect getSelEffect()  { return isFocused()? SELECT_EFFECT_FOC : SELECT_EFFECT; }
    
    /** Override to reset Select effect. */
    protected void setFocused(boolean aValue)
    {
        if(aValue==isFocused()) return; super.setFocused(aValue);
        for(View child : _lineViews) if(child.getEffect()!=null) child.setEffect(getSelEffect());
    }
}

/**
 * A class to hold a script line.
 */
private class LineView extends Label {
    
    /** Create LineView. */
    public LineView(ScriptLine aLine)
    {
        setPadding(5,10,5,10); setRadius(10);
        if(aLine!=null) { setText(aLine.getText()); setFont(SCRIPTVIEW_FONT); }
        setFill(LINEVIEW_FILL);
        enableEvents(MousePress);
    }

    /** Handle Events. */
    protected void processEvent(ViewEvent anEvent)
    {
        // Handle MousePress
        if(anEvent.isMousePress()) {
            
            // Focus ScriptView
            _scriptView.requestFocus();
            
            // Handle single-click
            if(anEvent.getClickCount()==1) {
                int ind = _scriptView._lineViews.indexOf(this);
                _scriptView.setSelIndex(ind);
                ViewUtils.fireActionEvent(_scriptView, anEvent);
            }
            
            // Handle multi-click
            else _editorPane.showLineEditor();
            
            // Consume event
            anEvent.consume();
        }
    }
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