package comics;
import snap.gfx.Font;
import snap.view.*;

/**
 * A class to manage UI for editing scripts.
 */
public class EditorPane extends ViewOwner {
    
    // The PlayerPane
    PlayerPane       _playerPane;
    
    // The PlayerView
    PlayerView       _player;
    
    // The ScriptView
    ScriptView       _scriptView;
    
    // The SubjectsView
    SubjectsView       _subjectsView;

    // The ListView
    ListView <String>  _listView;
    
    // Actions
    String _camActions[] = { "zooms", "blurs" };
    String _settingActions[] = { "beach", "ovaloffice", "whitehouse" };
    String _actorActions[] = { "walks", "waves", "jumps", "dances", "drops", "says", "grows", "flips", "explodes" };

/**
 * Creates an EditorPane.
 */
public EditorPane(PlayerPane aPlayerPane)
{
    _playerPane = aPlayerPane;
    _player = aPlayerPane.getPlayer();
}

/**
 * Updates the script.
 */
public void updateScript()
{
    _subjectsView.updateSubjects();
}

/**
 * Creates UI.
 */
protected View createUI()
{
    // Get/configure ScriptView
    _scriptView = new ScriptView(_playerPane); _scriptView.setPrefHeight(180);
    _scriptView.setText(PlayerPane.DEFAULT_SCRIPT);
    _scriptView.setSel(_scriptView.length());
    _scriptView.addEventFilter(e -> scriptViewReturnKey(e), KeyRelease);
    setFirstFocus(_scriptView.getTextArea());
    
    // Create/configure SubjectsView
    _subjectsView = new SubjectsView(this);
    
    //<ColView Padding="8,4,4,4" GrowHeight="true" FillWidth="true" Title="Cast" />
    ColView colView = new ColView(); colView.setPadding(8,5,5,5); colView.setSpacing(5);
    colView.setGrowHeight(true); colView.setFillWidth(true);
    
    colView.addChild(_scriptView);
    colView.addChild(_subjectsView);
    
    RowView rowView = (RowView)super.createUI();
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
    String selName = _subjectsView.getSelName();
    if(selName==null) _listView.setItems((String[])null);
    else if(selName.equals("Camera")) _listView.setItems(_camActions);
    else if(selName.equals("Setting")) _listView.setItems(_settingActions);
    else _listView.setItems(_actorActions);
}

/**
 * Called when user hits Enter Key in ScriptView.
 */
void scriptViewReturnKey(ViewEvent anEvent)
{
    // Handle EnterKey: Run to previous line
    if(anEvent==null || anEvent.isEnterKey()) {
        //_helpPane.reset();
        _playerPane.runCurrentLine();
    }
}

}