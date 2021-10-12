package comics.app;

import comics.player.*;
import snap.gfx.*;
import snap.view.*;
import snap.viewx.TransitionPane;

/**
 * A class to manage UI editing of a ScriptLine.
 */
public class LineEditor extends ViewOwner {

    // The EditorPane
    EditorPane _editorPane;

    // The StarPicker
    StarPicker _starPicker;

    // The ActionEditor
    ActionEditor _actionEditor;

    // The LineView shows the ScriptLine
    LineView _lineView;

    // TransitionPane
    TransitionPane _transPane;

    // Constants
    static Font MAIN_FONT = new Font("Arial", 18);

    /**
     * Creates a LineEditor.
     */
    public LineEditor(EditorPane anEP)
    {
        _editorPane = anEP;
        _starPicker = new StarPicker(this);
        _actionEditor = new ActionEditor(this);
    }

    /**
     * Returns the Player.
     */
    public PlayerView getPlayer()
    {
        return _editorPane.getPlayer();
    }

    /**
     * Returns the Script.
     */
    public Script getScript()
    {
        return _editorPane.getScript();
    }

    /**
     * Returns the current ScriptLine.
     */
    public ScriptLine getSelLine()
    {
        return _editorPane.getSelLine();
    }

    /**
     * Sets the StarPicker.
     */
    public void showStarPicker()
    {
        _transPane.setTransition(TransitionPane.MoveLeft);
        _transPane.setContent(_starPicker.getUI());
        _starPicker.resetLater();
    }

    /**
     * Sets the ActionEditor.
     */
    public void showActionEditor()
    {
        _transPane.setTransition(TransitionPane.MoveRight);
        _transPane.setContent(_actionEditor.getUI());
        _actionEditor.resetLater();
    }

    /**
     * Called when Script text changes.
     */
    protected void scriptChanged()
    {
        _starPicker.scriptChanged();
        resetLater();
    }

    /**
     * Called when LineView receives MousePress to have EditorPane go back to ScriptEditor.
     */
    void lineViewDidMousePress(ViewEvent anEvent)
    {
        _editorPane.showScriptEditor();
        anEvent.consume();
    }

    /**
     * Called when LineView.SelIndex changes to make sure correct sub-editor is showing.
     */
    void lineViewSelIndexChanged()
    {
        int ind = _lineView.getSelIndex();
        if (ind == 0 && !_starPicker.getUI().isShowing()) showStarPicker();
        else if (ind > 0 && !_actionEditor.getUI().isShowing()) showActionEditor();
    }

    /**
     * Creates UI.
     */
    protected View createUI()
    {
        // Get MainColView from UI file
        ColView mainColView = (ColView) super.createUI();
        mainColView.setFillWidth(true);

        // Get/configure ToolBar
        RowView toolBar = (RowView) mainColView.getChild(0);
        Label toolBarLabel = (Label) toolBar.getChild("ToolBarLabel");
        toolBarLabel.setFont(MAIN_FONT.getBold());

        // Create LineView
        _lineView = new LineView();
        _lineView.addPropChangeListener(pc -> lineViewSelIndexChanged(), LineView.SelIndex_Prop);
        _lineView.addEventHandler(e -> lineViewDidMousePress(e), MousePress);
        toolBar.addChild(_lineView, 1);

        // Create Divider line
        Label label = new Label();
        label.setPrefHeight(4);
        mainColView.addChild(label);
        RectView rectView = new RectView(0, 0, 100, 1);
        rectView.setFill(Color.LIGHTGRAY);
        mainColView.addChild(rectView);

        // Create TransPane
        _transPane = new TransitionPane();
        _transPane.setGrowHeight(true); //_transPane.setBorder(Color.PINK,1);
        _transPane.setContent(_starPicker.getUI());
        mainColView.addChild(_transPane);

        // Return MainColView
        return mainColView;
    }

    /**
     * Reset UI.
     */
    protected void resetUI()
    {
        // Get selected script line and star
        ScriptLine line = getSelLine();

        // Update LineView
        _lineView.setLine(line);

        // Reset StarPicker/ActionEditor
        if (_starPicker.isUISet() && _starPicker.getUI().isShowing()) _starPicker.resetLater();
        if (_actionEditor.isUISet() && _actionEditor.getUI().isShowing()) _actionEditor.resetLater();
    }

    /**
     * Respond to UI.
     */
    protected void respondUI(ViewEvent anEvent)
    {
        // Handle DoneButton
        if (anEvent.equals("DoneButton"))
            _editorPane.showScriptEditor();

        // Handle LineView
        if (anEvent.equals(_lineView)) {
            if (_lineView.getSelIndex() == 0)
                showStarPicker();
            else showActionEditor();
        }
    }

}