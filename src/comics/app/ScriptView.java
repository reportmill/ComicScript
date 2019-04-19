package comics.app;
import comics.script.*;
import snap.gfx.*;
import snap.view.*;

/**
 * A TextView class to show script with line markers.
 */
public class ScriptView extends ParentView {
    
    // The ScriptEditor
    ScriptEditor    _scriptEditor;
    
    // The Script
    Script          _script;
    
    // The selected index
    int             _selIndex = -1;
    
    // Whether the ScriptView is changing selection
    boolean         _settingSel;
    
    // The last text from script
    String          _text;
    
    // Constants
    static int SPACING = 6;
    static Color LINE_FILL = new Color(.9d);
    static Color    SELECT_COLOR = Color.get("#039ed3");
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
    setFill(Color.WHITE); //setBorder(Border.createLoweredBevelBorder());
    enableEvents(MouseRelease);
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
    removeChildren();
    
    // Get current SelIndex. If out of bounds for new script, set to end.
    int selIndex = getSelIndex(); _selIndex = -1;
    if(selIndex>=0 && selIndex>=aScript.getLineCount()) selIndex = aScript.getLineCount() - 1;
    else if(selIndex<0 && -selIndex>aScript.getLineCount()) selIndex = -aScript.getLineCount();
    
    // Iterate over lines
    for(ScriptLine sline : aScript.getLines()) {
        
        LineView lview = new LineView(sline);
        addChild(lview);
    }
    
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
    
    _settingSel = true;
    _scriptEditor.runCurrentLine(); _settingSel = false;
    _scriptEditor.resetLater();
    
    View lview = getSelLineView();
    if(lview!=null)
        ViewUtils.runLater(() -> scrollToVisible(lview.getBoundsParent()));
}

/**
 * Returns the selected ScriptLine.
 */
public ScriptLine getSelLine()  { return _selIndex>=0? getScript().getLine(_selIndex) : null; }

/**
 * Returns whether this ScriptView is setting selection.
 */
public boolean isSettingSel()  { return _settingSel; }

/**
 * Returns the selected index.
 */
LineView getLineView(int anIndex)  { return (LineView)getChild(anIndex); }

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
    if(anEvent.isMouseRelease())
        setSelIndex(-1);
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
        setFill(LINE_FILL);
        enableEvents(MouseRelease);
    }

    /** Returns bounds shape as rounded rect. */
    public Shape getBoundsShape()  { return new RoundRect(0,0,getWidth(),getHeight(),10); }
    
    /** Handle Events. */
    protected void processEvent(ViewEvent anEvent)
    {
        int index = getParent().indexOfChild(this);
        if(index==getSelIndex()) _scriptEditor.runCurrentLine();
        else setSelIndex(index);
        anEvent.consume();
    }
    
    /** Override to fix paint problem. */
    public void setEffect(Effect anEff)  { super.setEffect(anEff); repaint(-10,-10,getWidth()+20,getHeight()+20); }
}

}