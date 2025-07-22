package puppets;
import snap.util.SnapEnv;
import snap.view.*;

/**
 * The top level document UI management class for displaying and editing puppets and actions.
 */
public class PuppetsPane extends ViewOwner {

    // The view that holds the document
    private BoxView _docBox;

    // The EditorPane
    private EditorPane _editorPane;

    // The ActionPane
    private ActionPane _actionPane;

    // The SpritePane
    private SpritePane _spritePane;

    /**
     * Constructor.
     */
    private PuppetsPane()
    {
        super();
        _editorPane = new EditorPane();
        _actionPane = new ActionPane(this);
        _spritePane = new SpritePane(this);
    }

    /**
     * Returns the puppet.
     */
    public Puppet getPuppet()  { return _editorPane.getPuppet(); }

    /**
     * Shows the EditorPane.
     */
    public void showEditorPane()
    {
        _docBox.setContent(_editorPane.getUI());
        setViewValue("PuppetButton", true);
    }

    /**
     * Shows the ActionPane.
     */
    public void showActionPane()
    {
        _docBox.setContent(_actionPane.getUI());
        setViewValue("ActionButton", true);
    }

    /**
     * Shows the SpritePane.
     */
    public void showSpritePane()
    {
        _docBox.setContent(_spritePane.getUI());
        setViewValue("SpriteButton", true);
    }

    /**
     * Initialize UI.
     */
    protected void initUI()
    {
        _docBox = getView("DocBox", BoxView.class);

        // Initialize EditorPane
        Puppet puppet = PuppetUtils.getPuppetFile().getPuppet(0);
        _editorPane.setPuppet(puppet);
        showEditorPane();
    }

    /**
     * Respond to UI.
     */
    protected void respondUI(ViewEvent anEvent)
    {
        switch (anEvent.getName()) {

            // Handle PuppetButton, ActionButton, SpriteButton
            case "PuppetButton" -> showEditorPane();
            case "ActionButton" -> showActionPane();
            case "SpriteButton" -> showSpritePane();
        }
    }

    /**
     * Standard main method.
     */
    public static void main(String[] args)  { ViewUtils.runLater(PuppetsPane::showPuppetsPane);}

    /**
     * Shows puppets pane.
     */
    public static void showPuppetsPane()
    {
        PuppetsPane puppetsPane = new PuppetsPane();
        puppetsPane.getWindow().setMaximized(!SnapEnv.isDesktop);
        puppetsPane.setWindowVisible(true);
    }
}