package comics.app;
import comics.script.*;
import snap.gfx.*;
import snap.util.SnapUtils;
import snap.view.*;
import snap.viewx.TransitionPane;

/**
 * A class to manage UI for editing scripts.
 */
public class EditorPane extends ViewOwner {
    
    // The MenuBar
    MenuBar           _menuBar;
    
    // The View that holds editor UI
    ColView           _editorBox;
    
    // The PlayerPane
    PlayerPane        _playerPane;
    
    // The View that holds PlayerView
    View              _playerBox;
    
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
    _playerBox = _playerPane._playerBox;
    _player = aPlayerPane.getPlayer();
    
    // Show editor
    showEditor();
}

/**
 * Shows the EditorPane.
 */
public void showEditor()
{
    // Install in window
    WindowView win = _playerPane.getWindow();
    win.setContent(getUI());

    // Configure PlayerBox
    _playerBox.setPadding(20,20,20,20);
    
    // Configure Player
    _player.setEffect(new ShadowEffect().copySimple());
    _player._editorPane = this;
    
    // If desktop, set Window.MaximizedBounds to window PrefSize
    if(!SnapUtils.isTeaVM) {
        Size psize = win.isShowing()? win.getSize() : win.getPrefSize();
        Rect screenRect = ViewEnv.getEnv().getScreenBoundsInset();
        Rect maxRect = screenRect.getRectCenteredInside(psize.width, psize.height);
        if(win.isShowing()) { maxRect.x = win.getX(); maxRect.y = win.getY(); }
        win.setMaximizedBounds(maxRect);
    }
    
    // Maximize window
    win.setMaximized(true);
    
    // Notify scriptChanged and reset UI
    scriptChanged();
    resetLater();
}

/**
 * Hides the EditorPane.
 */
public void closeEditor()
{
    _playerPane.getWindow().setContent(_playerPane.getUI());
    _playerBox.setPadding(0,0,0,0);
    ((ChildView)_playerBox).removeChild(_playerPane.getView("CloseButton"));
    _player.setEffect(null);
    _player._editorPane = null;
    _playerPane.getWindow().setMaximized(false);
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
    if(_scriptEditor.isUISet())
        _scriptEditor.scriptChanged();
    if(_lineEditor.isUISet())
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
    _editorBox = new ColView(); _editorBox.setPadding(4,4,4,4); _editorBox.setFont(Font.Arial14);
    _editorBox.setGrowHeight(true); _editorBox.setFillWidth(true); _editorBox.setPrefHeight(400);
    _editorBox.addChild(_transPane);
    
    // Add close button
    Image img = Image.get(getClass(), "Close.png");
    ImageView closeBtn = new ImageView(img); closeBtn.setName("CloseButton");
    closeBtn.setLean(Pos.TOP_RIGHT); closeBtn.setPadding(2,2,0,0); closeBtn.setManaged(false);
    closeBtn.setSize(20+2, 20+2);
    enableEvents(closeBtn, MouseRelease);
    ((ChildView)_playerBox).addChild(closeBtn);
    
    //<ColView PrefWidth="800" Spacing="5" FillWidth="true"> ColView colView = (ColView)super.createUI();
    ColView colView = new ColView(); colView.setPrefWidth(800); colView.setSpacing(5); colView.setFillWidth(true);
    colView.addChild(_playerBox);
    colView.addChild(_editorBox);
    
    // Return in SplitView
    SplitView splitView = SplitView.makeSplitView(colView);
    splitView.getDivider(0).setPrefSpan(10);
    
    MenuBar mbar = getMenuBar(); mbar.setFont(Font.Arial14);
    View mbarView = MenuBar.createMenuBarView(mbar, splitView);
    return mbarView;
}

/**
 * Reset UI.
 */
protected void resetUI()
{
    if(_scriptEditor.getUI().isShowing()) _scriptEditor.resetLater();
    if(_lineEditor.getUI().isShowing()) _lineEditor.resetLater();
}

/**
 * RespondUI.
 */
protected void respondUI(ViewEvent anEvent)
{
    if(anEvent.equals("CloseButton") || anEvent.equals("CloseMenu"))
        _playerPane.setEditing(false);
}

/**
 * Returns the MenuBar.
 */
protected MenuBar getMenuBar()
{
    // AppMenu
    Menu appMenu = new Menu(); appMenu.setText("ComicCreator");
    MenuItem closeMenu = new MenuItem(); closeMenu.setName("CloseMenu"); closeMenu.setText("Close Editor");
    appMenu.addItem(closeMenu);
    
    // FileMenu
    Menu fileMenu = new Menu(); fileMenu.setText("File");
    MenuItem newMenu = new MenuItem(); newMenu.setText("New");
    fileMenu.addItem(newMenu);
    
    // EditMenu
    Menu editMenu = new Menu(); editMenu.setText("Edit");
    MenuItem cutMenu = new MenuItem(); cutMenu.setText("Cut");
    MenuItem copyMenu = new MenuItem(); copyMenu.setText("Copy");
    MenuItem pasteMenu = new MenuItem(); pasteMenu.setText("Paste");
    MenuItem deleteMenu = new MenuItem(); deleteMenu.setText("Delete");
    editMenu.addItem(cutMenu); editMenu.addItem(copyMenu); editMenu.addItem(pasteMenu); editMenu.addItem(deleteMenu); 
    
    // Create MenuBar
    MenuBar mbar = new MenuBar();
    mbar.addMenu(appMenu); mbar.addMenu(fileMenu); mbar.addMenu(editMenu);
    return mbar;
}

}