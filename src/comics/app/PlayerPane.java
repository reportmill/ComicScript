package comics.app;
import comics.script.Samples;
import snap.gfx.*;
import snap.util.*;
import snap.view.*;

/**
 * A class to show a PlayerView and allow editing.
 */
public class PlayerPane extends ViewOwner {
    
    // The PlayerView
    PlayerView      _player;
    
    // The TitleView at top of editor UI
    View            _titleView;
    
    // The View that holds the PlayerView
    ColView         _playerBox;
    
    // The View that holds editor UI
    ColView         _editorBox;
    
    // The EditorPane
    EditorPane      _editorPane;
    
    // Whether PlayerPane is showing editing UI
    boolean         _editing;
    
    // The default script text
    static String  DEFAULT_SCRIPT = "Setting is beach\n";

/**
 * Shows the player.
 */
public void showPlayer()
{
    setWindowVisible(true);
    //if(SnapUtils.isTeaVM) getWindow().setMaximized(true);
    
    runLater(() -> {
        String scriptText = Samples.getSample("Welcome");
        _player.setScriptText(scriptText);
        _editorPane.scriptChanged();
    });
}

/**
 * Returns the PlayerView.
 */
public PlayerView getPlayer()  { return _player; }

/**
 * Returns the StageView.
 */
public StageView getStage()  { return getPlayer().getStage(); }

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
    
    // Fix TitleView, TabView Visible
    //_titleView.setVisible(aValue);
    _editorBox.setVisible(aValue);
    _playerBox.setGrowHeight(!aValue); // Don't grow playerbox when editing (should grow EditorView)
    getPlayer().getPlayBar()._editButton.setText(aValue? "Player" : "Edit");
    
    // Enable Editing
    if(aValue) {
        if(!SnapUtils.isTeaVM) {
            Size psize = getWindow().getPrefSize();
            Rect screenRect = ViewEnv.getEnv().getScreenBoundsInset();
            Rect maxRect = screenRect.getRectCenteredInside(psize.width, psize.height);
            if(getWindow().isShowing()) { maxRect.x = getWindow().getX(); maxRect.y = getWindow().getY(); }
            getWindow().setMaximizedBounds(maxRect);
        }
        _player.setPadding(20,20,20,20); _player.getCamera().setEffect(new ShadowEffect().copySimple());
        getWindow().setMaximized(true);
        _editorPane.resetLater();
    }
    
    // Disable ShowFull
    else {
        _player.setPadding(0,0,0,0); _player.getCamera().setEffect(null);
        getWindow().setMaximized(false);
    }
    
    getPlayer().showTitleAnim();
}

/**
 * Create UI.
 */
protected View createUI()
{
    ColView colView = (ColView)super.createUI();
    return SplitView.makeSplitView(colView);
}

/**
 * Initialize the UI.
 */
protected void initUI()
{
    // Create PlayerView
    _player = new PlayerView(); _player.setGrowHeight(true);
    
    // Watch for clicks on StageView
    StageView stage = _player.getStage();
    //enableEvents(stage, MousePress);
    
    // Get master ColView and add StageBox
    SplitView splitView = getUI(SplitView.class);
    splitView.getDivider(0).setPrefSpan(8);
    _playerBox = (ColView)splitView.getItem(0);
    _playerBox.setGrowHeight(true);
    _playerBox.addChild(_player);
    
    // Get TitleView and configure to be 800 wide so window will be good size
    _titleView = _playerBox.getChild(0);
    _titleView.setPrefWidth(800);
    _titleView.setVisible(false);
    
    // Get EditorBox
    _editorBox = (ColView)splitView.getItem(1);
    _editorBox.setVisible(false);
    _editorBox.setPrefWidth(800);
    
    // Add EditorPane
    _editorPane = new EditorPane(this);
    _editorBox.addChild(_editorPane.getUI());
    
    if(!SnapUtils.isTeaVM) setEditing(true);
}

/**
 * Respond to UI.
 */
protected void respondUI(ViewEvent anEvent)
{
    // Handle ResetButton
    //if(anEvent.equals("ResetButton")) { getPlayer().stop(); getPlayer().setRunTime(0); }
        
    // Handle RunButton
    //if(anEvent.equals("RunButton")) { resetScript(); getPlayer().play(); }
        
    // Handle EditButton
    if(anEvent.equals("EditButton")) {
        setEditing(!isEditing()); anEvent.consume(); }
    
    // Handle AgainButton
    //if(anEvent.equals("AgainButton")) runCurrentLine();
    
    // Handle Stage MousePressed
    //if(anEvent.isMousePress()) selectSettingItem(anEvent.getX(), anEvent.getY());
}

/**
 * Selects a setting item.
 */
public void selectSettingItem(double aX, double aY)
{
    // Get child at point
    /*View child = getStage().getChildAt(aX, aY);
    if(child instanceof Actor) {
        String name = child.getName(); name = FilePathUtils.getFileNameSimple(name);
        _helpPane.addToScript(name);
    }*/
}

}