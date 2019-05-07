package comics.app;
import comics.player.*;
import snap.gfx.*;
import snap.view.*;
import snap.viewx.TransitionPane;

/**
 * A class to manage UI editing of a ScriptLine.
 */
public class LineEditor extends ViewOwner {
    
    // The EditorPane
    EditorPane         _editorPane;
    
    // The StarPicker
    StarPicker         _starPicker;
    
    // The ActionEditor
    ActionEditor       _actionEditor;
    
    // The LinePartsView holds the LinePartViews
    LinePartsView      _linePartsView;

    // TransitionPane
    TransitionPane     _transPane;
    
    // Constants
    static Font     MAIN_FONT = new Font("Arial", 18);
    static Color    LINEPART_FILL = new Color("#FFDDDD"), LINEPART_BORDER = Color.GRAY;
    static Color    SELECT_COLOR = Color.get("#039ed3");
    static Effect   SELECT_EFFECT = new ShadowEffect(8, Color.DARKGRAY, 0, 0);
    static Effect   SELECT_EFFECT_FOC = new ShadowEffect(8, SELECT_COLOR, 0, 0);

/**
 * Creates a LineEditor.
 */
public LineEditor(EditorPane anEP)
{
    _editorPane = anEP;
    _starPicker = new StarPicker(this);
    _actionEditor = new ActionEditor(this);
}

/**
 * Returns the Player.
 */
public PlayerView getPlayer()  { return _editorPane.getPlayer(); }

/**
 * Returns the Script.
 */
public Script getScript()  { return _editorPane.getScript(); }

/**
 * Returns the current ScriptLine.
 */
public ScriptLine getSelLine()  { return _editorPane.getSelLine(); }

/**
 * Sets the StarPicker.
 */
public void showStarPicker()
{
    _transPane.setTransition(TransitionPane.MoveLeft);
    _transPane.setContent(_starPicker.getUI());
    _starPicker.resetLater();
}

/**
 * Sets the ActionEditor.
 */
public void showActionEditor()
{
    _transPane.setTransition(TransitionPane.MoveRight);
    _transPane.setContent(_actionEditor.getUI());
    _actionEditor.resetLater();
}

/**
 * Called when Script text changes.
 */
protected void scriptChanged()
{
    _starPicker.scriptChanged();
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
    
    // Create LinePartsView
    _linePartsView = new LinePartsView();
    toolBar.addChild(_linePartsView, 1);
    
    // Create Divider line
    Label label = new Label(); label.setPrefHeight(4);
    mainColView.addChild(label);
    RectView rectView = new RectView(0,0,100,1); rectView.setFill(Color.LIGHTGRAY);
    mainColView.addChild(rectView);
    
    // Create TransPane
    _transPane = new TransitionPane(); _transPane.setGrowHeight(true); //_transPane.setBorder(Color.PINK,1);
    _transPane.setContent(_starPicker.getUI());
    mainColView.addChild(_transPane);
    
    // Return MainColView
    return mainColView;
}

/**
 * Reset UI.
 */
protected void resetUI()
{
    // Get selected script line and star
    ScriptLine line = getSelLine();
    
    // Update LinePartsView
    _linePartsView.setLine(line);
    
    // Reset StarPicker/ActionEditor
    if(_starPicker.isUISet() && _starPicker.getUI().isShowing()) _starPicker.resetLater();
    if(_actionEditor.isUISet() && _actionEditor.getUI().isShowing()) _actionEditor.resetLater();
}

/**
 * Respond to UI.
 */
protected void respondUI(ViewEvent anEvent)
{
    // Handle DoneButton
    if(anEvent.equals("DoneButton"))
        _editorPane.showScriptEditor();
        
    // Handle LinePartsView
    if(anEvent.equals(_linePartsView)) {
        if(_linePartsView.getSelIndex()==0)
            showStarPicker();
        else showActionEditor();
    }
}

/**
 * A class to show LineParts.
 */
class LinePartsView extends RowView {
    
    // The ScriptLine
    ScriptLine        _line;
    String            _text = "";
    
    // The selected index
    int               _selIndex;
    
    /** Creates a LinePartsView. */
    public LinePartsView()
    {
        setGrowWidth(true); setPrefHeight(40);
        setPadding(5,5,5,5); setSpacing(8);
        setFont(new Font("Arial", 15));
        enableEvents(MousePress, Action);
    }
    
    /** Sets the ScriptLine. */
    public void setLine(ScriptLine aLine)
    {
        // If already set, just return
        String text = aLine!=null? aLine.getText() : "";
        if(aLine==_line && text.equals(_text)) return;

        // Get index
        int ind = _line==aLine? _selIndex : 0;
        
        // Set Line and Text
        _line = aLine; _text = text;
        
        // Remove Children
        removeChildren();
        
        // Add Star part
        Star star = aLine!=null? aLine.getStar() : null;
        String starName = star!=null? star.getStarName() : "?";
        LinePartView starView = new LinePartView(starName);
        starView.setEffect(SELECT_EFFECT_FOC);
        addChild(starView);
        setSelIndex(0);
        
        // Add Action part
        Action action = aLine!=null? aLine.getAction() : null;
        String actionName = action!=null? action.getNameUsed().toLowerCase() : "?";
        LinePartView actView = new LinePartView(actionName);
        addChild(actView);

        // Add Action predicate
        String predText = action!=null? action.getText() : "";
        if(predText.length()>0) {
            LinePartView predView = new LinePartView(predText);
            addChild(predView);
        }
        
        // Reset selection
        if(ind>=getChildCount()) ind = getChildCount() - 1;
        setSelIndex(ind);
        if(ind==0 && !_starPicker.getUI().isShowing()) showStarPicker();
        else if(ind>0 && !_actionEditor.getUI().isShowing()) showActionEditor();
    }
    
    /** Returns the selected index. */
    public int getSelIndex()  { return _selIndex; }
    
    /** Sets the selected index. */
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
            ViewUtils.fireActionEvent(_linePartsView, anEvent);
            anEvent.consume();
        }
    }
}

}