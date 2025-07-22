package puppets;
import snap.util.SnapEnv;
import snap.view.*;

/**
 * The top level document UI management class for displaying and editing puppets and actions.
 */
public class PuppetsPane extends ViewOwner {

    // The view that holds the document
    BoxView _docBox;

    // The EditorPane
    EditorPane _editorPane = new EditorPane(this);

    // The ActionPane
    ActionPane _actionPane;

    // The SpritePane
    SpritePane _spritePane;

    /**
     * Constructor.
     */
    public static void showAppPane()
    {
        PuppetsPane puppetsPane = new PuppetsPane();
        if (SnapEnv.isTeaVM)
            puppetsPane.getWindow().setMaximized(true);
        puppetsPane.setWindowVisible(true);

        // Initialize EditorPane
        //apane._editorPane.open("Man");
        Puppet puppet = PuppetUtils.getPuppetFile().getPuppet(0);
        puppetsPane._editorPane.setPuppet(puppet);
        puppetsPane.showEditorPane();
    }

    /**
     * Returns the puppet.
     */
    public Puppet getPuppet()
    {
        return _editorPane.getPuppet();
    }

    /**
     * Shows the EditorPane.
     */
    public void showEditorPane()
    {
        //_docBox.removeChildren();
        _docBox.setContent(_editorPane.getUI());
        setViewValue("PuppetButton", true);
    }

    /**
     * Shows the ActionPane.
     */
    public void showActionPane()
    {
        //_docBox.removeChildren();

        _actionPane = new ActionPane(this);
        _docBox.setContent(_actionPane.getUI());
        setViewValue("ActionButton", true);
    }

    /**
     * Shows the SpritePane.
     */
    public void showSpritePane()
    {
        //_docBox.removeChildren();

        _spritePane = new SpritePane(this);
        _docBox.setContent(_spritePane.getUI());
        setViewValue("SpriteButton", true);
    }

    /**
     * Initialize UI.
     */
    protected void initUI()
    {
        _docBox = getView("DocBox", BoxView.class);
    }

    /**
     * Respond to UI.
     */
    protected void respondUI(ViewEvent anEvent)
    {
        // Handle PuppetButton, ActionButton, SpriteButton
        if (anEvent.equals("PuppetButton")) showEditorPane();
        if (anEvent.equals("ActionButton")) showActionPane();
        if (anEvent.equals("SpriteButton")) showSpritePane();
    }

    /**
     * Standard main method.
     */
    public static void main(String[] args)  { ViewUtils.runLater(PuppetsPane::showAppPane);}
}