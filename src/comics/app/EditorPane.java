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
    updateScript();
}

/**
 * Sets the script editor.
 */
public void showScriptEditor()
{
    _transPane.setTransition(TransitionPane.MoveDown);
    _transPane.setContent(_scriptEditor.getUI());
    updateScript();
}

/**
 * Updates the script.
 */
public void updateScript()
{
    _scriptEditor.updateScript();
    _lineEditor.updateScript();
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
 * Respond to UI.
 */
protected void respondUI(ViewEvent anEvent)
{
    /*if(anEvent.equals("EditLineButton")) {
        boolean isScript = _transPane.getContent()==_scriptEditor.getUI();
        View view = isScript? _lineEditor.getUI() : _scriptEditor.getUI();
        _transPane.setTransition(isScript? TransitionPane.MoveUp : TransitionPane.MoveDown);
        _transPane.setContent(view);
        updateScript();
    }*/
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