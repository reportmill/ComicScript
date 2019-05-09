package comics.app;
import comics.player.*;
import java.util.*;
import snap.gfx.*;
import snap.util.PropChangeListener;
import snap.view.*;

/**
 * A view subclass to show script lines.
 */
public class ScriptView extends ColView {
    
    // The Script
    Script              _script;
    
    // The selected index
    int                 _selIndex = -1;
    
    // The selected char index - if LineView was clicked
    int                 _selCharIndex = -1;
    
    // The current list of LineViews
    List <LineView>     _lineViews = new ArrayList();
    
    // The cursor lineview
    LineView            _cursorLineView;
    
    // A PropListener
    PropChangeListener  _scriptLsnr = pc -> rebuildLines();
    
    // Constants
    public static String SelIndex_Prop = "SelIndex";
    
    // Constants
    static Font SCRIPTVIEW_FONT = new Font("Arial", 15);
    static Color LINEVIEW_FILL = new Color(.9d), SELECT_COLOR = Color.get("#039ed3");
    static Effect SELECT_EFFECT = new ShadowEffect(8, Color.DARKGRAY, 0, 0);
    static Effect SELECT_EFFECT_FOC = new ShadowEffect(8, SELECT_COLOR, 0, 0);

/**
 * Creates ScriptView.
 */
public ScriptView()
{
    setName("ScriptView"); setPadding(8,8,8,12); setSpacing(6); setGrowWidth(true); setGrowHeight(true);
    setFill(Color.WHITE); setFont(SCRIPTVIEW_FONT);
    setFocusable(true); setFocusWhenPressed(true); setFocusKeysEnabled(false);
    enableEvents(MousePress, Action);
    
    // Configure CursorLineView
    _cursorLineView = new LineView(null); _cursorLineView.setPrefSize(150,1);
    _cursorLineView.setMargin(0,0,0,5);
    _cursorLineView.setFill(Color.LIGHTGRAY); _cursorLineView.setEffect(SELECT_EFFECT_FOC);
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
    // If already set, just return
    if(aScript==_script) return;
    
    // Set Script
    if(_script!=null) _script.removePropChangeListener(_scriptLsnr);
    _script = aScript;
    if(_script!=null) _script.addPropChangeListener(_scriptLsnr);
    
    // Rebuild lines and reset index
    rebuildLines();
    setSelIndex(0);
}

/**
 * Called to rebuild lines.
 */
protected void rebuildLines()
{
    // Remove children and clear LineViews
    removeChildren(); _lineViews.clear(); if(_script==null) return;
    
    // Iterate over lines and add LineView for each
    for(ScriptLine sline : _script.getLines()) {
        LineView lview = new LineView(sline);
        addChild(lview); _lineViews.add(lview);
    }
    addChild(_cursorLineView);
    
    // Make sure selection is in range
    if(getSelIndex()>=_lineViews.size())
        setSelIndex(_lineViews.size() - 1);
    else if(getSelView()!=null)
        getSelView().setEffect(getSelEffect());
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
    int oldVal = getSelIndex();
    
    // Undecorate last selected LineView
    LineView oldSelLV = getSelView(); if(oldSelLV!=null) oldSelLV.setEffect(null);
    
    // Set new value
    _selIndex = anIndex;
    _selCharIndex = -1;
    
    // Decorate new selected LineView
    LineView selLineView = getSelView();
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
    
    // FirePropChange and repaint
    firePropChange(SelIndex_Prop, oldVal, _selIndex = anIndex);
    repaint();
}

/** Returns the selected index. */
LineView getSelView()  { return _selIndex>=0 && _selIndex<_lineViews.size()? _lineViews.get(_selIndex) : null; }

/**
 * Returns the selected character index (assuming Line was clicked).
 */
public int getSelCharIndex()  { return _selCharIndex; }

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

/**
 * Override to reset Select effect.
 */
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
        if(aLine!=null) { setText(aLine.getText()); setFont(SCRIPTVIEW_FONT); }
        setFill(LINEVIEW_FILL);
        enableEvents(MousePress);
    }

    /** Handle Events. */
    protected void processEvent(ViewEvent anEvent)
    {
        // Handle MousePress: Select LineView and fireActionEvent
        if(anEvent.isMousePress()) {
            anEvent.consume(); if(anEvent.getClickCount()>1) return;
            ScriptView.this.requestFocus();
            int ind = _lineViews.indexOf(this);
            setSelIndex(ind);
            int cind = getStringView().getCharIndexForX(anEvent.getX() - getStringView().getX());
            _selCharIndex = cind;
            ScriptView.this.fireActionEvent(anEvent);
        }
    }
}

}