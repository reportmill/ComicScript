package comics.app;
import comics.player.*;
import java.util.*;
import snap.gfx.*;
import snap.view.*;

/**
 * A TextView class to show script with line markers.
 */
public class ScriptView extends ColView {
    
    // The ScriptEditor
    ScriptEditor       _scriptEditor;
    
    // The selected index
    int                _selIndex = -1;
    
    // The current list of LineViews
    List <LineView>    _lineViews = new ArrayList();
    
    // The cursor lineview
    LineView           _cursorLineView;
    
    // Constants
    static Color LINEVIEW_FILL = new Color(.9d);
    static Color SELECT_COLOR = Color.get("#039ed3");
    static Effect SELECT_EFFECT = new ShadowEffect(8, Color.DARKGRAY, 0, 0);
    static Effect SELECT_EFFECT_FOC = new ShadowEffect(8, SELECT_COLOR, 0, 0);

/**
 * Creates a ScriptView.
 */
public ScriptView(ScriptEditor aSE)
{
    _scriptEditor = aSE;

    setPadding(8,8,8,12); setSpacing(6);
    setGrowWidth(true); setGrowHeight(true);
    setFont(new Font("Arial", 15));
    setFill(Color.WHITE);
    setFocusable(true); setFocusWhenPressed(true); setFocusKeysEnabled(false);
    enableEvents(MousePress, KeyPress, Action);
    
    // Configure CursorLineView
    _cursorLineView = new LineView(null); _cursorLineView.setPrefSize(150,1);
    _cursorLineView.setMargin(0,0,0,5);
    _cursorLineView.setFill(Color.LIGHTGRAY); _cursorLineView.setEffect(SELECT_EFFECT_FOC);
}

/**
 * Called when Script changes.
 */
protected void scriptChanged()
{
    // Set script
    Script script = _scriptEditor.getScript();
    
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

/**
 * Returns the selected index.
 */
public int getSelIndex()  { return _selIndex; }

/**
 * Sets the selected index.
 */
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

/**
 * Handle Events.
 */
protected void processEvent(ViewEvent anEvent)
{
    // Handle MousePress: Select negative index to trigger insert mode
    if(anEvent.isMousePress()) {
        int ind = 0; for(LineView lview : _lineViews) { if(lview.getY()+5>anEvent.getY()) break; ind++; }
        ind = EditorPane.negateIndex(ind);
        setSelIndex(ind);
        fireActionEvent(anEvent); anEvent.consume();
    }
    
    // Handle KeyPress
    else if(anEvent.isKeyPress()) {
        
        // Handle Delete, BackSpaceKey, Up/Down arrow
        if(anEvent.isDeleteKey() || anEvent.isBackSpaceKey()) _scriptEditor.delete();
        else if(anEvent.isUpArrow()) _scriptEditor.selectPrev();
        else if(anEvent.isDownArrow()) _scriptEditor.selectNext();
            
        // Handle tab key
        else if(anEvent.isTabKey()) {
            if(anEvent.isShiftDown()) _scriptEditor.selectPrev();
            else _scriptEditor.selectNext();
        }
            
        // Handle enter key
        else if(anEvent.isEnterKey()) {
            if(anEvent.isShiftDown()) _scriptEditor.selectPrev();
            else _scriptEditor.selectNextWithInsert();
        }
        
        // Handle letter or digit
        else {
            char c = anEvent.getKeyChar();
            if(Character.isLetterOrDigit(c)) {
                _scriptEditor._inputText.requestFocus();
                _scriptEditor._inputText.selectAll();
                ViewUtils.processEvent(_scriptEditor._inputText, anEvent);
            }
        }
        anEvent.consume();
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

/**
 * A class to hold a script line.
 */
private class LineView extends Label {
    
    /** Create LineView. */
    public LineView(ScriptLine aLine)
    {
        setPadding(5,10,5,10); setRadius(10);
        if(aLine!=null) setText(aLine.getText()); setFont(ScriptView.this.getFont());
        setFill(LINEVIEW_FILL);
        enableEvents(MousePress);
    }

    /** Handle Events. */
    protected void processEvent(ViewEvent anEvent)
    {
        // Handle MousePress
        if(anEvent.isMousePress()) {
            
            // Focus ScriptView
            ScriptView.this.requestFocus();
            
            // Handle single-click
            if(anEvent.getClickCount()==1) {
                int ind = _lineViews.indexOf(this);
                setSelIndex(ind);
                ScriptView.this.fireActionEvent(anEvent);
            }
            
            // Handle multi-click
            else _scriptEditor._editorPane.showLineEditor();
            
            // Consume event
            anEvent.consume();
        }
    }
}

}