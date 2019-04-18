package comics.app;
import snap.gfx.*;
import snap.view.*;

/**
 * A TextView class to show script with line markers.
 */
public class ScriptView extends TextView {
    
    // The ScriptEditor
    ScriptEditor   _scriptEditor;
    
    // The MarkView
    MarkView       _markView;
    
    // The current RunLine
    int            _runLine;

/**
 * Creates a ScriptView.
 */
public ScriptView(ScriptEditor aSE)
{
    _scriptEditor = aSE;

    setGrowWidth(true);
    getTextArea().setGrowWidth(true);
    setFont(new Font("Arial", 20));
    
    _markView = new MarkView();
    
    RowView box = new RowView(); box.setFillHeight(true);
    box.setChildren(_markView, getTextArea());
    getScrollView().setContent(box);
    
    getTextArea().addPropChangeListener(pc -> textSelChanged(), TextArea.Selection_Prop);
}

/**
 * Returns the current marked run line.
 */
public int getRunLine()  { return _runLine; }

/**
 * Sets the current marked run line.
 */
public void setRunLine(int anIndex)
{
    if(anIndex==_runLine) return;
    _runLine = anIndex;
    _markView.repaint();
    _scriptEditor.setPlayerRunTimeToLineEnd(anIndex); //_scriptEditor.runCurrentLine();
}

/**
 * Called when selection changes.
 */
protected void textSelChanged()
{
    int ind = getSelStartLineIndex();
    setRunLine(ind);
}

/**
 * Returns the index of the selection start.
 */
public int getSelStartLineIndex()
{
    TextBoxLine line = getSel().getStartLine();
    return line.getIndex();
}

/**
 * A custom view to draw current line marker.
 */
private class MarkView extends View {
    
    // Vars
    Color MARKER_COLOR = new Color(.2,.8,.2);

    /** Creates MarkView. */
    public MarkView()
    {
        setFill(new Color(.93));
        setPrefWidth(22);
        enableEvents(MouseRelease);
    }
    
    /** Override to draw marker. */
    protected void paintFront(Painter aPntr)
    {
        if(_runLine<0 || _runLine>=getTextArea().getLineCount()) return;
        TextBoxLine line = getTextArea().getLine(_runLine);
        double y0 = line.getY(), y1 = line.getMaxY();
        
        aPntr.setColor(MARKER_COLOR);
        aPntr.fill(new Ellipse(4,y0+4,14,y1-y0-8));
    }
    
    /** Override to update selection. */
    protected void processEvent(ViewEvent anEvent)
    {
        if(anEvent.isMouseClick()) {
            TextBoxLine line = getTextBox().getLineForY(anEvent.getY());
            int cindex = line.length()>0? line.getEnd()-1 : line.getEnd();
            getTextArea().setSel(cindex);
            _scriptEditor.runCurrentLine();
        }
    }
}

}