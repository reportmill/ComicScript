package comics.app;
import snap.gfx.*;
import snap.util.ArrayUtils;
import snap.view.*;

/**
 * A class to show helper UI.
 */
public class HelpPane extends ViewOwner {

    // The ScriptEditor
    ScriptEditor         _scriptEditor;
    
    // The ListView
    ListView <String>    _listView;
    
    // The last added word
    String               _lastAdd;
    
    // Constants
    String _starters[] = { "Title is", "Setting is", "Lady", "Man", "Car", "Cat", "Dog", "Trump", "Obama", "Duke" };
    String _settings[] = { "Beach", "WhiteHouse", "OvalOffice" };
    String _verbs[] = { "walks", "waves", "jumps", "dances", "drops", "says", "grows", "flips", "explodes" };
    String _walks[] = { "in", "in from right", "out" };
 
/**
 * Creates a new HelpPane for PlayerPane.
 */   
public HelpPane(ScriptEditor aSE)  { _scriptEditor = aSE; }

/**
 * Returns the list of words to use.
 */
public String[] getWords()
{
    if(_lastAdd==null) return _starters;
    if(_lastAdd=="Setting is") return _settings;
    if(_lastAdd=="walks" || _lastAdd=="drops") return _walks;
    if(ArrayUtils.contains(_starters, _lastAdd)) return _verbs;
    return new String[] { "  \u23CE" };
}

public void reset()
{
    _lastAdd = null; resetLater();
}

/**
 * Initialize UI.
 */
protected void initUI()
{
    _listView = getView("ListView", ListView.class);
    _listView.setFont(Font.Arial16);
    
    Label label = getView("Label", Label.class);
    Label retButton = new Label("\u23CE"); retButton.setName("ReturnButton");
    retButton.setPadding(1,3,0,3); retButton.setLeanX(HPos.RIGHT);
    enableEvents(retButton, MouseRelease);
    label.setGraphicAfter(retButton);
}

/**
 * Reset UI.
 */
protected void resetUI()
{
    _listView.setItems(getWords());
}

/**
 * Respond UI.
 */
protected void respondUI(ViewEvent anEvent)
{
    // Handle ListView
    if(anEvent.equals("ListView")) {
        
        // Handle MouseRelease on empty
        if(anEvent.isMouseClick()) {
            if(_listView.getSelItem()==null)
                doReturn();
        }
        
        // Handle selection (on mouse up)
        else ViewUtils.runOnMouseUp(() -> {
            String str = _listView.getSelItem();
            addToScript(str);
        });
    }
    
    // Handle ReturnButton
    if(anEvent.equals("ReturnButton"))
        doReturn();
}

public void addToScript(String aStr)
{
    // If return text, do return
    if("  \u23CE".equals(aStr)) { doReturn(); return; }
    
    // Add string
    _scriptEditor._inputText.replaceChars(aStr + " ");
    _scriptEditor._inputText.requestFocus();
    _lastAdd = aStr;
    resetLater();
}

void doReturn()
{
    _scriptEditor._inputText.replaceChars("\n");
    ViewUtils.fireActionEvent(_scriptEditor._inputText, null);
}

}