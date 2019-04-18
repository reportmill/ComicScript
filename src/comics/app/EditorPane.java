package comics.app;
import comics.script.*;
import snap.view.*;
import snap.viewx.TransitionPane;

/**
 * A class to manage UI for editing scripts.
 */
public class EditorPane extends ViewOwner {
    
    // The PlayerPane
    PlayerPane        _playerPane;
    
    // The PlayerView
    PlayerView        _player;
    
    // TransitionPane
    TransitionPane    _transPane;
    
    // The ScriptEditor
    ScriptEditor      _scriptEditor = new ScriptEditor(this);
    
    // The ScriptLineEditor
    ScriptLineEditor  _lineEditor = new ScriptLineEditor(this);
    
/**
 * Creates an EditorPane.
 */
public EditorPane(PlayerPane aPlayerPane)
{
    _playerPane = aPlayerPane;
    _player = aPlayerPane.getPlayer();
}

/**
 * Returns the PlayerView.
 */
public PlayerView getPlayer()  { return _player; }

/**
 * Returns the Script.
 */
public Script getScript()  { return _player.getScript(); }

/**
 * Returns the ScriptLine.
 */
public ScriptLine getScriptLine()
{
    Script script = getScript();
    int index = _player.getRunLine();
    return script.getLine(index);
}

/**
 * Sets the line editor.
 */
public void showLineEditor()
{
    _transPane.setTransition(TransitionPane.MoveUp);
    _transPane.setContent(_lineEditor.getUI());
    _lineEditor.resetLater();
}

/**
 * Sets the script editor.
 */
public void showScriptEditor()
{
    _transPane.setTransition(TransitionPane.MoveDown);
    _transPane.setContent(_scriptEditor.getUI());
    _scriptEditor.resetLater();
}

/**
 * Updates the script.
 */
protected void scriptChanged()
{
    _scriptEditor.scriptChanged();
    _lineEditor.scriptChanged();
}

/**
 * Creates UI.
 */
protected View createUI()
{
    // Create TransPane
    _transPane = new TransitionPane(); _transPane.setGrowHeight(true); //_transPane.setBorder(Color.PINK,1);
    _transPane.setContent(_scriptEditor.getUI());
    
    //<ColView Padding="8,4,4,4" GrowHeight="true" FillWidth="true" Title="Cast" />
    ColView colView = new ColView(); colView.setPadding(4,4,4,4);
    colView.setGrowHeight(true); colView.setFillWidth(true);
    colView.addChild(_transPane);
    
    return colView;
}

/**
 * Reset UI.
 */
protected void resetUI()
{
    if(_scriptEditor.getUI().isShowing()) _scriptEditor.resetLater();
    if(_lineEditor.getUI().isShowing()) _lineEditor.resetLater();
}

}