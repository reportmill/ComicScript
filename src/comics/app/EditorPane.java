package comics.app;

import comics.player.*;
import snap.geom.Pos;
import snap.geom.Rect;
import snap.geom.Size;
import snap.gfx.*;
import snap.props.PropChangeListener;
import snap.util.SnapEnv;
import snap.view.*;
import snap.viewx.TransitionPane;

/**
 * A class to manage UI for editing scripts.
 */
public class EditorPane extends ViewOwner {

    // The PlayerPane
    PlayerPane _playerPane;

    // The PlayerView
    PlayerView _player;

    // The index of selected script line (can be negative in editor to indicate insertion point)
    int _selIndex;

    // The ScriptEditor
    ScriptEditor _scriptEditor = new ScriptEditor(this);

    // The LineEditor
    LineEditor _lineEditor = new LineEditor(this);

    // TransitionPane
    TransitionPane _transPane;

    // Indicates to EditorPane whether Player RunLine Change originated from editor
    boolean _changingSelIndex;

    // A PropChangeListener to listen for when Player.Script changes
    PropChangeListener _playerLsnr = pc -> playerScriptChanged();

    /**
     * Creates an EditorPane.
     */
    public EditorPane(PlayerPane aPlayerPane)
    {
        // Set Player, PlayerPane
        _playerPane = aPlayerPane;
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
        _playerPane.getPlayerBox().setPadding(20, 20, 20, 20);

        // Configure Player
        _player.setEffect(new ShadowEffect().copySimple());
        _player.addPropChangeListener(_playerLsnr, PlayerView.Script_Prop);

        // If desktop, set Window.MaximizedBounds to window PrefSize
        if (!SnapEnv.isTeaVM) {
            Size psize = win.isShowing() ? win.getSize() : win.getPrefSize();
            Rect screenRect = GFXEnv.getEnv().getScreenBoundsInset();
            Rect maxRect = screenRect.getRectCenteredInside(psize.width, psize.height);
            if (win.isShowing()) {
                maxRect.x = win.getX();
                maxRect.y = win.getY();
            }
            win.setMaximizedBounds(maxRect);
        }

        // Maximize window
        win.setMaximized(true);

        // Notify scriptChanged and reset UI
        playerScriptChanged();
        resetLater();

        // Start listening to player changes
        _player.addPropChangeListener(pc -> playerRunLineChanged(), PlayerView.RunLine_Prop);
    }

    /**
     * Hides the EditorPane.
     */
    public void closeEditor()
    {
        _playerPane.getWindow().setContent(_playerPane.getUI());
        _playerPane.getPlayerBox().setPadding(0, 0, 0, 0);
        ViewUtils.removeChild(_playerPane.getPlayerBox(), _playerPane.getView("CloseButton"));
        _player.setEffect(null);
        _player.removePropChangeListener(_playerLsnr, PlayerView.Script_Prop);
        _playerPane.getWindow().setMaximized(false);
    }

    /**
     * Returns the PlayerView.
     */
    public PlayerView getPlayer()
    {
        return _player;
    }

    /**
     * Returns the Script.
     */
    public Script getScript()
    {
        return _player.getScript();
    }

    /**
     * Returns the index of selected script line (can be negative in editor to indicate insertion point).
     */
    public int getSelIndex()
    {
        return _selIndex;
    }

    /**
     * Sets the index of selected script line.
     */
    public void setSelIndex(int anIndex)
    {
        // If already set, just return
        if (anIndex == _selIndex) return;

        // Set SelIndex and update player
        _selIndex = anIndex;
        int ind = _selIndex;
        if (ind < 0) ind = negateIndex(ind);
        _changingSelIndex = true;
        getPlayer().setRunLine(ind);
        _changingSelIndex = false;
        resetLater();
    }

    /**
     * Returns the selected ScriptLine.
     */
    public ScriptLine getSelLine()
    {
        int ind = getSelIndex();
        if (ind < 0) return null;
        return getScript().getLine(ind);
    }

    /**
     * Selects previous line.
     */
    public void selectPrev()
    {
        int ind = getSelIndex();
        if (ind < 0) ind = negateIndex(ind);
        if (ind == 0) {
            beep();
            return;
        }
        setSelIndex(ind - 1);
    }

    /**
     * Selects next line.
     */
    public void selectNext()
    {
        int ind = getSelIndex();
        if (ind < 0) ind = negateIndex(ind);
        if (ind + 1 >= getScript().getLineCount()) {
            beep();
            return;
        }
        setSelIndex(ind + 1);
    }

    /**
     * Selects next line.
     */
    public void selectNextWithInsert()
    {
        int ind = getSelIndex();
        if (ind < 0) {
            ind = negateIndex(ind);
            if (ind >= getScript().getLineCount()) ind = 0;
        } else ind = negateIndex(ind) - 1;
        setSelIndex(ind);
    }

    /**
     * Adds a ScriptLine for given string at given index.
     */
    public void addLineText(String aStr, int anIndex)
    {
        getScript().addLineText(aStr, anIndex);
        setSelIndex(anIndex);
    }

    /**
     * Sets ScriptLine text to given string at given index.
     */
    public void setLineText(String aStr, int anIndex)
    {
        getScript().setLineText(aStr, anIndex);
    }

    /**
     * Deletes the current selected line.
     */
    public void delete()
    {
        // Get selcted index
        int ind = getSelIndex();
        if (ind < 0) {
            selectPrev();
            return;
        }

        // If first and only line, reset line and return
        if (ind == 0 && getScript().getLineCount() == 1) {
            setLineText("Setting is blank", 0);
            runCurrentLine();
            return;
        }

        // Remove selected line, select previous line and run
        getScript().removeLine(ind);
        if (ind < getScript().getLineCount()) selectPrev();
        runCurrentLine();
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
     * Runs the current line.
     */
    public void runCurrentLine()
    {
        if (getSelIndex() < 0) return;
        getPlayer().stop();
        getPlayer().playLine();
    }

    /**
     * Updates the script.
     */
    void playerScriptChanged()
    {
        if (_scriptEditor.isUISet()) _scriptEditor.scriptChanged();
        if (_lineEditor.isUISet()) _lineEditor.scriptChanged();
    }

    /**
     * Called when PlayerView.RunLine changes.
     */
    void playerRunLineChanged()
    {
        if (_changingSelIndex) return;
        setSelIndex(getPlayer().getRunLine());
    }

    /**
     * Creates UI.
     */
    protected View createUI()
    {
        // Create box to hold editor
        ColView editorBox = new ColView();
        editorBox.setPadding(4, 4, 4, 4);
        editorBox.setFont(Font.Arial14);
        editorBox.setGrowHeight(true);
        editorBox.setFillWidth(true);
        editorBox.setPrefHeight(400);

        // Create TransPane to swap ScriptEditor/LineEditor and make content of EditorBox
        _transPane = new TransitionPane();
        _transPane.setGrowHeight(true); //_transPane.setBorder(Color.PINK,1);
        _transPane.setContent(_scriptEditor.getUI());
        editorBox.addChild(_transPane);

        // Add close button
        Image img = Image.getImageForClassResource(getClass(), "Close.png");
        ImageView closeBtn = new ImageView(img);
        closeBtn.setName("CloseButton");
        closeBtn.setLean(Pos.TOP_RIGHT);
        closeBtn.setPadding(2, 2, 0, 0);
        closeBtn.setManaged(false);
        closeBtn.setPrefSize(20 + 2, 20 + 2);
        closeBtn.addEventHandler(e -> _playerPane.setEditing(false), MouseRelease);
        ViewUtils.addChild(_playerPane.getPlayerBox(), closeBtn);

        // Create MainColView to hold Player and Editor
        ColView colView = new ColView();
        colView.setPrefWidth(800);
        colView.setSpacing(5);
        colView.setFillWidth(true);
        colView.addChild(_playerPane.getPlayerBox());
        colView.addChild(editorBox);

        // Create SplitView from MainColView to separate Player/Editor
        SplitView splitView = SplitView.makeSplitView(colView);
        splitView.getDivider(0).setSpan(10);

        // Create MenuBar and wrap MenuBar and SplitView - return that view
        MenuBar mbar = getMenuBar();
        mbar.setFont(Font.Arial14);
        View mbarView = MenuBar.createMenuBarView(mbar, splitView);
        return mbarView;
    }

    /**
     * Reset UI.
     */
    protected void resetUI()
    {
        if (_scriptEditor.getUI().isShowing())
            _scriptEditor.resetLater();
        if (_lineEditor.getUI().isShowing())
            _lineEditor.resetLater();
    }

    /**
     * RespondUI.
     */
    protected void respondUI(ViewEvent anEvent)
    {
        // Handle CloseMenu
        if (anEvent.equals("CloseMenu"))
            _playerPane.setEditing(false);

        // Handle IntroMenu
        if (anEvent.equals("IntroMenu"))
            _player.showIntroAnim();
    }

    /**
     * Returns the MenuBar.
     */
    protected MenuBar getMenuBar()
    {
        // AppMenu
        Menu appMenu = new Menu();
        appMenu.setText("ComicCreator");
        MenuItem closeMenu = new MenuItem();
        closeMenu.setName("CloseMenu");
        closeMenu.setText("Close Editor");
        MenuItem introlMenu = new MenuItem();
        introlMenu.setName("IntroMenu");
        introlMenu.setText("Show Intro");
        appMenu.addItem(closeMenu);
        appMenu.addItem(introlMenu);

        // FileMenu
        Menu fileMenu = new Menu();
        fileMenu.setText("File");
        MenuItem newMenu = new MenuItem();
        newMenu.setText("New");
        fileMenu.addItem(newMenu);

        // EditMenu
        Menu editMenu = new Menu();
        editMenu.setText("Edit");
        MenuItem cutMenu = new MenuItem();
        cutMenu.setText("Cut");
        MenuItem copyMenu = new MenuItem();
        copyMenu.setText("Copy");
        MenuItem pasteMenu = new MenuItem();
        pasteMenu.setText("Paste");
        MenuItem deleteMenu = new MenuItem();
        deleteMenu.setText("Delete");
        editMenu.addItem(cutMenu);
        editMenu.addItem(copyMenu);
        editMenu.addItem(pasteMenu);
        editMenu.addItem(deleteMenu);

        // Create MenuBar
        MenuBar mbar = new MenuBar();
        mbar.addMenu(appMenu);
        mbar.addMenu(fileMenu);
        mbar.addMenu(editMenu);
        return mbar;
    }

    /**
     * Negates an index.
     */
    public static int negateIndex(int anIndex)
    {
        if (anIndex >= 0) return -anIndex - 1;
        return -(anIndex + 1);
    }
}