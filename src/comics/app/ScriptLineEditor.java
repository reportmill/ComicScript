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

    // The view to show list of stars in script
    StarListView       _starsView;

    // The ListView
    ListView <String>  _actionListView;
    
    // Actions
    String _camActions[] = { "zooms", "blurs" };
    String _setActions[] = { "beach", "ovaloffice", "whitehouse" };
    String _actActions[] = { "walks", "waves", "jumps", "dances", "drops", "says", "grows", "flips", "explodes" };
    
    // Constants
    Font     MAIN_FONT = new Font("Arial", 18);

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
    // Create ToolBar
    RowView toolBar = new RowView(); toolBar.setSpacing(4);
    Label label = new Label("Line:"); label.setFont(MAIN_FONT.getBold());
    TextField text = new TextField(); text.setName("LineText"); text.setGrowWidth(true); text.setFont(MAIN_FONT);
    text.setText("Setting is beach"); setFirstFocus(text);
    Button btn = new Button("Edit Script"); btn.setName("EditScriptButton");
    btn.setLeanX(HPos.RIGHT); btn.setPrefSize(95,26);
    toolBar.setChildren(label, text, btn);
    
    // Create/configure SubjectsView
    _starsView = new StarListView(this);
    
    // Create ActionEditor
    RowView rowView = (RowView)super.createUI();
    
    //<ColView Padding="8,4,4,4" GrowHeight="true" FillWidth="true" Title="Cast" />
    ColView colView = new ColView(); colView.setPadding(8,5,5,5); colView.setSpacing(5);
    colView.setGrowHeight(true); colView.setFillWidth(true);
    colView.addChild(toolBar);
    colView.addChild(_starsView);
    colView.addChild(rowView);
    
    return colView;
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
    
    // Update LineText
    setViewText("LineText", line.getText());
    
    // Update StarsView
    _starsView.setSelStar(star);
    
    // Update ListView
    String starName = star.getStarName();
    if(starName==null) _actionListView.setItems((String[])null);
    else if(starName.equals("Camera")) _actionListView.setItems(_camActions);
    else if(starName.equals("Setting")) _actionListView.setItems(_setActions);
    else _actionListView.setItems(_actActions);
}

/**
 * Respond to UI.
 */
protected void respondUI(ViewEvent anEvent)
{
    if(anEvent.equals("EditScriptButton"))
        _editorPane.showScriptEditor();
}

}