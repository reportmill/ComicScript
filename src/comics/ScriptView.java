package comics;
import snap.gfx.*;
import snap.view.*;

/**
 * A custom class.
 */
public class ScriptView extends TextView {
    
    // The StagePane
    StagePane      _stagePane;
    
    // The MarkView
    MarkView       _markView;

/**
 * Creates a ScriptView.
 */
public ScriptView(StagePane aSP)
{
    _stagePane = aSP;

    setPrefHeight(300); setPadding(5,5,5,5); setGrowWidth(true);
    getTextArea().setGrowWidth(true); //getTextArea().setPadding(5,5,5,5);
    setFont(new Font("Arial", 20));
    
    _markView = new MarkView();
    
    RowView box = new RowView(); box.setFillHeight(true);
    box.setChildren(_markView, getTextArea());
    getScrollView().setContent(box);
    
    getTextArea().addPropChangeListener(pc -> textSelChanged(), TextArea.Selection_Prop);
}

/**
 * Called when selection changes.
 */
protected void textSelChanged()
{
    _markView.repaint();
}

/**
 * A custom view to draw current line marker.
 */
public class MarkView extends View {

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
        TextBoxLine line = getTextArea().getSel().getStartLine();
        double y0 = line.getY(), y1 = line.getMaxY();
        
        aPntr.setColor(new Color(.2,.8,.2));
        aPntr.fill(new Ellipse(4,y0+4,14,y1-y0-8));
    }
    
    /** Override to update selection. */
    protected void processEvent(ViewEvent anEvent)
    {
        if(anEvent.isMouseClick()) {
            TextBoxLine line = getTextBox().getLineForY(anEvent.getY());
            int cindex = line.length()>0? line.getEnd()-1 : line.getEnd();
            getTextArea().setSel(cindex);
            _stagePane.getScript(true).runLineCurrent();
        }
    }
}

}