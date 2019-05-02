package comics.app;
import comics.script.*;
import snap.gfx.*;
import snap.view.*;

/**
 * A class to manage UI editing of a line.
 */
public class ScriptLineEditor extends ViewOwner {
    
    // The EditorPane
    EditorPane         _editorPane;
    
    // The LinePartsView holds the LinePartViews
    LinePartsView      _linePartsView;

    // The view to show list of stars in script
    StarListView       _starsView;

    // The ListView
    ListView <String>  _actionListView;
    
    // Constants
    static Font     MAIN_FONT = new Font("Arial", 18);
    static Color    LINEPART_FILL = new Color("#FFDDDD"), LINEPART_BORDER = Color.GRAY;
    static Color    SELECT_COLOR = Color.get("#039ed3");
    static Effect   SELECT_EFFECT = new ShadowEffect(8, Color.DARKGRAY, 0, 0);
    static Effect   SELECT_EFFECT_FOC = new ShadowEffect(8, SELECT_COLOR, 0, 0);

/**
 * Creates a ScriptLineEditor.
 */
public ScriptLineEditor(EditorPane anEP)
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
 * Called when Script text changes.
 */
protected void scriptChanged()
{
    if(_starsView==null) return;
    _starsView.updateSubjects();
    resetLater();
}

/**
 * Creates UI.
 */
protected View createUI()
{
    // Get MainColView from UI file
    ColView mainColView =  (ColView)super.createUI(); mainColView.setFillWidth(true);

    // Get/configure ToolBar
    RowView toolBar = (RowView)mainColView.getChild(0);
    Label toolBarLabel = (Label)toolBar.getChild("ToolBarLabel"); toolBarLabel.setFont(MAIN_FONT.getBold());
    TextField text = (TextField)toolBar.getChild("LineText"); text.setFont(MAIN_FONT);
    text.setVisible(false); //setFirstFocus(text);
    
    // Create LinePartsView
    _linePartsView = new LinePartsView();
    toolBar.addChild(_linePartsView, 1);
    
    // Create/add StarListView
    _starsView = new StarListView(this);
    mainColView.addChild(_starsView,1);
    
    // Return MainColView
    return mainColView;
}

/**
 * Initialize UI.
 */
protected void initUI()
{
    _actionListView = getView("ListView", ListView.class);
    _actionListView.setFont(Font.Arial16);
}

/**
 * Reset UI.
 */
protected void resetUI()
{
    // Get selected script line and star
    ScriptLine line = getScriptLine();
    Star star = line.getStar(); if(star==null) return;
    
    // Update LineText, LinePartsView
    setViewText("LineText", line.getText());
    _linePartsView.setLine(line);
    
    // Update StarsView
    _starsView.setSelStar(star);
    
    // Update ActionListView
    String actionNames[] = star.getActionNames();
    _actionListView.setItems(actionNames);
}

/**
 * Respond to UI.
 */
protected void respondUI(ViewEvent anEvent)
{
    // Handle DoneButton
    if(anEvent.equals("DoneButton"))
        _editorPane.showScriptEditor();
}

/**
 * A class to show LineParts.
 */
class LinePartsView extends RowView {
    
    // The ScriptLine
    ScriptLine        _line;
    
    // The selected index
    int               _selIndex;
    
    /** Creates a LinePartsView. */
    public LinePartsView()
    {
        setGrowWidth(true); setPrefHeight(40);
        setPadding(5,5,5,5); setSpacing(8); //setFill(Color.WHITE); setBorder(Border.createLoweredBevelBorder());
        setFont(new Font("Arial", 15));
        enableEvents(MousePress);
    }
    
    /** Sets the ScriptLine. */
    public void setLine(ScriptLine aLine)
    {
        // If already set, just return
        if(aLine==_line) return;
        _line = aLine;
        
        // Remove Children
        removeChildren();
        
        // Add Star part
        Star star = aLine.getStar();
        LinePartView starView = new LinePartView(star.getStarName());
        starView.setEffect(SELECT_EFFECT_FOC);
        addChild(starView);
        setSelIndex(0);
        
        // Add Action part
        Action action = _line.getAction(); if(action==null) return;
        LinePartView actView = new LinePartView(action.getNameUsed().toLowerCase());
        addChild(actView);

        // Add Action predicate
        String predText = action.getText();
        if(predText.length()>0) {
            LinePartView predView = new LinePartView(predText);
            addChild(predView);
        }
    }
    
    /** Returns the selected index. */
    public void setSelIndex(int anIndex)
    {
        if(anIndex==_selIndex) return;
        View selViewOld = getSelView(); if(selViewOld!=null) selViewOld.setEffect(null);
        _selIndex = anIndex;
        View selView = getSelView(); if(selView!=null) selView.setEffect(SELECT_EFFECT_FOC);
        repaint();
    }
    
    /** Returns the selected view. */
    public LinePartView getSelView()  {
        return _selIndex>=0 && _selIndex<getChildCount()? (LinePartView)getChild(_selIndex) : null;
    }
    
    /** Handle Events. */
    protected void processEvent(ViewEvent anEvent)
    {
        // Handle MousePress
        if(anEvent.isMousePress()) {
            _editorPane.showScriptEditor();
            anEvent.consume();
        }
    }
}

/**
 * A class to represent a LineView.
 */
class LinePartView extends Label {
    
    /** Creates LinePartView. */
    public LinePartView(String aStr)
    {
        setText(aStr); setFont(_linePartsView.getFont());
        setPadding(5,10,5,10); setRadius(10);
        setFill(LINEPART_FILL); setBorder(LINEPART_BORDER, 1);
        enableEvents(MousePress);
    }
    
    /** Handle Events. */
    protected void processEvent(ViewEvent anEvent)
    {
        // Handle MousePress
        if(anEvent.isMousePress()) {
            int ind = getParent().indexOfChild(this);
            _linePartsView.setSelIndex(ind);
            anEvent.consume();
        }
    }
}

}