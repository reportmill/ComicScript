package comics.app;
import comics.script.*;
import java.util.*;
import snap.gfx.*;
import snap.view.*;

/**
 * A TextView class to show script with line markers.
 */
public class ScriptView extends ParentView {
    
    // The ScriptEditor
    ScriptEditor       _scriptEditor;
    
    // The Script
    Script             _script;
    
    // The selected index
    int                _selIndex = -1;
    
    // The last text from script
    String             _text;
    
    // The current list of LineViews
    List <LineView>    _lineViews = new ArrayList();
    
    // The cursor lineview
    LineView           _cursorLineView;
    
    // Constants
    static int SPACING = 6;
    static Color LINEVIEW_FILL = new Color(.9d);
    static Color SELECT_COLOR = Color.get("#039ed3");
    static Effect SELECT_EFFECT = new ShadowEffect(8, SELECT_COLOR, 0, 0);

/**
 * Creates a ScriptView.
 */
public ScriptView(ScriptEditor aSE)
{
    _scriptEditor = aSE;

    setPadding(8,8,8,12);
    setGrowWidth(true); setGrowHeight(true);
    setFont(new Font("Arial", 15));
    setFill(Color.WHITE);
    enableEvents(MouseRelease);
    
    // Configure CursorLineView
    _cursorLineView = new LineView(new ScriptLine(getScript(),"")); _cursorLineView.setPrefSize(150,1);
    _cursorLineView.setMargin(0,0,0,5);
    _cursorLineView.setFill(Color.LIGHTGRAY); _cursorLineView.setEffect(SELECT_EFFECT);
}

/**
 * Returns the script.
 */
public Script getScript()  { return _script; }

/**
 * Sets the script.
 */
public void setScript(Script aScript)
{
    _script = aScript;
    _text = _script.getText();
    
    // Get current SelIndex. If out of bounds for new script, set to end.
    int selIndex = getSelIndex(); _selIndex = -1;
    if(selIndex>=0 && selIndex>=aScript.getLineCount()) selIndex = aScript.getLineCount() - 1;
    else if(selIndex<0 && -selIndex>aScript.getLineCount()) selIndex = -aScript.getLineCount();
    
    // Iterate over lines
    removeChildren(); _lineViews.clear();
    for(ScriptLine sline : aScript.getLines()) {
        LineView lview = new LineView(sline);
        addChild(lview); _lineViews.add(lview);
    }
    addChild(_cursorLineView);
    
    // Reset SelIndex
    setSelIndex(selIndex);
}

/**
 * Returns the script text.
 */
public String getText()  { return _text; }

/**
 * Returns the selected index.
 */
public int getSelIndex()  { return _selIndex; }

/**
 * Sets the selected index.
 */
public void setSelIndex(int anIndex)
{
    if(anIndex==_selIndex) return;
    if(anIndex>=getChildCount()) return;
    LineView lv0 = getSelLineView(); if(lv0!=null) lv0.setEffect(null);
    _selIndex = anIndex;
    LineView lv1 = getSelLineView(); if(lv1!=null) lv1.setEffect(SELECT_EFFECT);
    
    _scriptEditor.resetLater();
    
    View lview = getSelLineView();
    if(lview!=null)
        ViewUtils.runLater(() -> scrollToVisible(lview.getBoundsParent().getInsetRect(-5)));
        
    // Configure CursorLineView
    if(_selIndex<0) {
        int ind0 = indexOfChild(_cursorLineView), ind1 = negateIndex(_selIndex);
        if(ind1<ind0) addChild(_cursorLineView, ind1);
        else if(ind1>ind0) { removeChild(_cursorLineView); addChild(_cursorLineView, ind1); }
        ViewUtils.runLater(() -> scrollToVisible(_cursorLineView.getBoundsParent().getInsetRect(-5)));
    }
    _cursorLineView.setVisible(_selIndex<0);
}

/**
 * Returns the selected ScriptLine.
 */
public ScriptLine getSelLine()  { return _selIndex>=0? getScript().getLine(_selIndex) : null; }

/**
 * Returns the selected index.
 */
LineView getLineView(int anIndex)  { return _lineViews.get(anIndex); }

/**
 * Returns the selected index.
 */
LineView getSelLineView()  { return _selIndex>=0? getLineView(_selIndex) : null; }

/**
 * Returns the preferred width.
 */
protected double getPrefWidthImpl(double aH)  { return ColView.getPrefWidth(this, null, aH); }

/**
 * Returns the preferred height.
 */
protected double getPrefHeightImpl(double aW)  { return ColView.getPrefHeight(this, null, SPACING, aW); }

/**
 * Layout children.
 */
protected void layoutImpl()  { ColView.layout(this, null, null, false, SPACING); }

/**
 * Handle Events.
 */
protected void processEvent(ViewEvent anEvent)
{
    if(anEvent.isMouseRelease()) {
        int index = 0; for(LineView lview : _lineViews) { if(lview.getY()+5>anEvent.getY()) break; index++; }
        index = negateIndex(index);
        setSelIndex(index);
    }
}

/**
 * A class to hold a script line.
 */
private class LineView extends Label {
    
    // The ScriptLine
    ScriptLine  _line;
    
    /** Create LineView. */
    public LineView(ScriptLine aLine)
    {
        _line = aLine;
        setText(aLine.getText());
        setPadding(5,10,5,10);
        setFont(ScriptView.this.getFont());
        setFill(LINEVIEW_FILL);
        enableEvents(MouseRelease);
    }

    /** Returns bounds shape as rounded rect. */
    public Shape getBoundsShape()  { return new RoundRect(0,0,getWidth(),getHeight(),10); }
    
    /** Handle Events. */
    protected void processEvent(ViewEvent anEvent)
    {
        if(anEvent.isMouseRelease()) {
            _scriptEditor.getPlayer().stop();
            int index = _lineViews.indexOf(this);
            setSelIndex(index);
            _scriptEditor.runCurrentLine();
            anEvent.consume();
            _scriptEditor._inputText.requestFocus();
        }
    }
    
    /** Override to fix paint problem. */
    public void setEffect(Effect anEff)  { super.setEffect(anEff); repaint(-10,-10,getWidth()+20,getHeight()+20); }
}

/**
 * Negates an index.
 */
public static int negateIndex(int anIndex)
{
    if(anIndex>=0) return -anIndex - 1;
    return -(anIndex + 1);
}

}