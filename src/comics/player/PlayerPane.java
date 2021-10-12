package comics.player;

import comics.app.EditorPane;
import snap.geom.Insets;
import snap.geom.Pos;
import snap.view.*;

/**
 * A class to show a PlayerView and allow editing.
 */
public class PlayerPane extends ViewOwner {

    // The PlayerView
    PlayerView _player;

    // The View that holds the PlayerView
    PlayerBox _playerBox;

    // Whether PlayerPane is showing editing UI
    boolean _editing;

    // The EditorPane
    EditorPane _editorPane;

    /**
     * Shows the player.
     */
    public void showPlayer()
    {
        if (!_player.isShowing())
            setWindowVisible(true);

        runLater(() -> {
            String scriptText = getSampleScript();
            _player.setScriptText(scriptText);
            _player.showIntroAnim();
        });
    }

    /**
     * Returns the PlayerView.
     */
    public PlayerView getPlayer()
    {
        return _player;
    }

    /**
     * Returns the view that holds the player.
     */
    public PlayerBox getPlayerBox()
    {
        return _playerBox;
    }

    /**
     * Returns whether to show editor.
     */
    public boolean isEditing()
    {
        return _editing;
    }

    /**
     * Sets whether to show editor.
     */
    public void setEditing(boolean aValue)
    {
        // If already set, just return
        if (aValue == _editing) return;
        _editing = aValue;

        // Turn on/off
        if (_editing) _editorPane = new EditorPane(this);
        else _editorPane.closeEditor();

        _playerBox.setGrowHeight(!aValue);
        _player.getPlayBar()._editButton.setText(aValue ? "Player" : "Edit");

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
        _playerBox = new PlayerBox();
        _playerBox.setGrowHeight(true);
        _playerBox.setAlign(Pos.CENTER);
        _playerBox.addChild(_player);
        return _playerBox;
    }

    /**
     * Respond to UI.
     */
    protected void respondUI(ViewEvent anEvent)
    {
        // Handle EditButton
        if (anEvent.equals("EditButton")) {
            setEditing(!isEditing());
            anEvent.consume();
        }
    }

    /**
     * Returns a sample script.
     */
    public String getSampleScript()
    {
    /*return "Setting is beach\n" + "Lady walks in\n" + "Man walks in from right\n" +
        "Lady says, \"Welcome to ComicScript animator!\"\n" +
        "Man says, \"Build animations with natural language!\"\n" +
        "Camera zooms in on Lady\n" + "Lady waves\n" + "Man jumps\n" + "Camera zooms out\n" +
        "Lady dances\n" + "Man explodes\n";*/
        return "Setting is beach\n" + "Lady walks in\n" + "Man walks in from right\n" +
                "Lady jumps\n" +
                "Man jumps\n" +
                "Lady waves\n" +
                "Man waves\n" +
                "Lady dances\n" + "Man explodes\n";
    }

    /**
     * A class to layout PlayerView.
     */
    public class PlayerBox extends ChildView {

        /**
         * Override.
         */
        protected double getPrefWidthImpl(double aH)
        {
            return BoxView.getPrefWidth(this, _player, aH);
        }

        /**
         * Override.
         */
        protected double getPrefHeightImpl(double aW)
        {
            return BoxView.getPrefHeight(this, _player, aW);
        }

        /**
         * Override.
         */
        protected void layoutImpl()
        {
            // Get parent bounds for insets (just return if empty)
            Insets ins = getInsetsAll();
            double px = ins.left, py = ins.top;
            double pw = getWidth() - px - ins.right;
            if (pw < 0) pw = 0;
            if (pw <= 0) return;
            double ph = getHeight() - py - ins.bottom;
            if (ph < 0) ph = 0;
            if (ph <= 0) return;

            // Get player pref sizes
            double cw = _player.getPrefWidth();
            double ch = _player.getPrefHeight();

            // If player size doesn't fit in available space, shrink to fit keeping aspect
            if (cw > pw || ch > ph) {
                double scale = Math.min(pw / cw, ph / ch);
                cw = Math.round(cw * scale);
                ch = Math.round(ch * scale);
            }

            // Position player in center with given sizes
            double dx = Math.round((pw - cw) / 2), dy = Math.round((ph - ch) / 2);
            _player.setBounds(px + dx, py + dy, cw, ch);
        }
    }

}