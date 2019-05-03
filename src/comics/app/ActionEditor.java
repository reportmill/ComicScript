package comics.app;
import comics.script.*;
import snap.gfx.Font;
import snap.view.*;

/**
 * A class to provide UI editing for Actions.
 */
public class ActionEditor extends ViewOwner {

    // The ScriptLineEditor
    ScriptLineEditor   _lineEditor;

    // The ListView
    ListView <String>  _actionListView;
    
/**
 * Creates ActionEditor.
 */
public ActionEditor(ScriptLineEditor aLE)
{
    _lineEditor = aLE;
}

/**
 * Returns the ScriptLine.
 */
public ScriptLine getSelLine()  { return _lineEditor.getSelLine(); }

/**
 * Sets the action by name.
 */
public void setActionByName(String aName)
{
    ScriptLine line = getSelLine();
    line.setActionByName(aName);
    _lineEditor._editorPane.runCurrentLine();
    resetLater();
}

/**
 * Initialize UI.
 */
protected void initUI()
{
    // Configure ActionListView
    _actionListView = getView("ListView", ListView.class);
    _actionListView.setFont(Font.Arial16);
}

/**
 * Reset UI.
 */
protected void resetUI()
{
    // Get selected script line and star
    ScriptLine line = getSelLine();
    Star star = line.getStar(); if(star==null) return;
    Action action = line.getAction(); if(action==null) return;
    
    // Update ActionListView
    String actionNames[] = star.getActionNames();
    _actionListView.setItems(actionNames);
    _actionListView.setSelItem(action.getName().toLowerCase());
}

/**
 * Responds to UI.
 */
protected void respondUI(ViewEvent anEvent)
{
    // Handle ActionListView
    if(anEvent.equals(_actionListView)) {
        String name = _actionListView.getSelItem(); if(name==null) return; name = name.toLowerCase();
        setActionByName(name);
    }
        
}

}