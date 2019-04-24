package comics.app;
import comics.script.Samples;
import snap.gfx.Pos;
import snap.view.*;

/**
 * A class to show a PlayerView and allow editing.
 */
public class PlayerPane extends ViewOwner {
    
    // The PlayerView
    PlayerView      _player;
    
    // The View that holds the PlayerView
    BoxView         _playerBox;
    
    // Whether PlayerPane is showing editing UI
    boolean         _editing;
    
    // The EditorPane
    EditorPane      _editorPane;
    
/**
 * Shows the player.
 */
public void showPlayer()
{
    if(!_player.isShowing())
        setWindowVisible(true);
    
    runLater(() -> {
        String scriptText = Samples.getSample("Welcome");
        _player.setScriptText(scriptText);
        if(_editorPane!=null)
            _editorPane.scriptChanged();
    });
}

/**
 * Returns the PlayerView.
 */
public PlayerView getPlayer()  { return _player; }

/**
 * Returns whether to show editor.
 */
public boolean isEditing()  { return _editing; }

/**
 * Sets whether to show editor.
 */
public void setEditing(boolean aValue)
{
    // If already set, just return
    if(aValue==_editing) return;
    _editing = aValue;
    
    // Turn on/off
    if(_editing) _editorPane = new EditorPane(this);
    else _editorPane.closeEditor();
    
    _playerBox.setGrowHeight(!aValue);
    _player.getPlayBar()._editButton.setText(aValue? "Player" : "Edit");
    
    getPlayer().showIntroAnim();
}

/**
 * Create UI.
 */
protected View createUI()
{
    // Create PlayerView
    _player = new PlayerView();
    
    // Create PlayerBox to hold Player
    _playerBox = new BoxView(); _playerBox.setPadding(8,4,4,4); _playerBox.setPrefHeight(400);
    _playerBox.setGrowHeight(true); _playerBox.setAlign(Pos.CENTER);
    _playerBox.addChild(_player);
    return _playerBox;
}

/**
 * Respond to UI.
 */
protected void respondUI(ViewEvent anEvent)
{
    // Handle EditButton
    if(anEvent.equals("EditButton")) {
        setEditing(!isEditing()); anEvent.consume(); }
}

}