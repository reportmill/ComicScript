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
    ListView <String>  _listView;
    
    // Actions
    String _camActions[] = { "zooms", "blurs" };
    String _settingActions[] = { "beach", "ovaloffice", "whitehouse" };
    String _actorActions[] = { "walks", "waves", "jumps", "dances", "drops", "says", "grows", "flips", "explodes" };
    
    // Constants
    Font     MAIN_FONT = new Font("Arial", 20);

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
 * Updates the script.
 */
public void updateScript()
{
    if(_starsView==null) return;
    _starsView.updateSubjects();
    _starsView.setSelIndex(0);
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
    text.setText("Setting is beach");
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
    _listView = getView("ListView", ListView.class);
    _listView.setFont(Font.Arial16);
}

/**
 * Reset UI.
 */
protected void resetUI()
{
    ScriptLine line = getScriptLine();
    
    setViewText("LineText", line.getText());
    String selName = _starsView.getSelName();
    if(selName==null) _listView.setItems((String[])null);
    else if(selName.equals("Camera")) _listView.setItems(_camActions);
    else if(selName.equals("Setting")) _listView.setItems(_settingActions);
    else _listView.setItems(_actorActions);
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