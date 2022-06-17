package comics.app;

import comics.player.*;
import snap.gfx.*;
import snap.props.PropChangeListener;
import snap.view.*;

/**
 * A class to show LineParts.
 */
public class LineView extends RowView {

    // The ScriptLine
    ScriptLine _line;

    // The selected index
    int _selIndex;

    // A listener to watch for ScriptLine Text changes
    PropChangeListener _lineLsnr = pc -> rebuildFrags();

    // Constants
    public static String SelIndex_Prop = "SelIndex";
    static Color LINEPART_FILL = new Color("#FFDDDD"), LINEPART_BORDER = Color.GRAY;
    static Color SELECT_COLOR = Color.get("#039ed3");
    static Effect SELECT_EFFECT_FOC = new ShadowEffect(8, SELECT_COLOR, 0, 0);

    /**
     * Creates a LineView.
     */
    public LineView()
    {
        setGrowWidth(true);
        setPrefHeight(40);
        setPadding(5, 5, 5, 5);
        setSpacing(8);
        setFont(new Font("Arial", 15));
        enableEvents(Action);
    }

    /**
     * Creates a LineView for given ScriptLine.
     */
    public LineView(ScriptLine aLine)
    {
        setLine(aLine);
    }

    /**
     * Sets the ScriptLine.
     */
    public void setLine(ScriptLine aLine)
    {
        // If already set, just return
        if (aLine == _line) return;

        // Set Line
        if (_line != null) _line.removePropChangeListener(_lineLsnr);
        _line = aLine;
        if (_line != null) _line.addPropChangeListener(_lineLsnr);

        // Rebuild Line FragViews and reset index
        rebuildFrags();
        setSelIndex(0);
    }

    /**
     * Rebuilds the line fragments.
     */
    void rebuildFrags()
    {
        // Remove Children
        removeChildren();

        // Add Star part
        Star star = _line != null ? _line.getStar() : null;
        String starName = star != null ? star.getStarName() : "?";
        FragView starView = new FragView(starName);
        addChild(starView);

        // Add Action part
        Action action = _line != null ? _line.getAction() : null;
        String actionName = action != null ? action.getNameUsed().toLowerCase() : "?";
        FragView actView = new FragView(actionName);
        addChild(actView);

        // Add Action predicate
        String predText = action != null ? action.getText() : "";
        if (predText.length() > 0) {
            FragView predView = new FragView(predText);
            addChild(predView);
        }

        // Make sure selection is in range
        if (getSelIndex() >= getChildCount())
            setSelIndex(getChildCount() - 1);
        else getSelView().setEffect(SELECT_EFFECT_FOC);
    }

    /**
     * Returns the selected index.
     */
    public int getSelIndex()
    {
        return _selIndex;
    }

    /**
     * Sets the selected index.
     */
    public void setSelIndex(int anIndex)
    {
        // If already set, just return
        if (anIndex == _selIndex) return;
        int oldVal = _selIndex;

        // Set new value
        View selViewOld = getSelView();
        if (selViewOld != null) selViewOld.setEffect(null);
        _selIndex = anIndex;
        View selView = getSelView();
        if (selView != null) selView.setEffect(SELECT_EFFECT_FOC);

        // FirePropChange and repaint
        firePropChange(SelIndex_Prop, oldVal, _selIndex = anIndex);
        repaint();
    }

    /**
     * Returns the selected FragView.
     */
    FragView getSelView()
    {
        return _selIndex >= 0 && _selIndex < getChildCount() ? (FragView) getChild(_selIndex) : null;
    }

    /**
     * A class to represent a subpart of a ScriptLine (star, action, predicate).
     */
    class FragView extends Label {

        /**
         * Creates FragView.
         */
        public FragView(String aStr)
        {
            setText(aStr);
            setFont(LineView.this.getFont());
            setPadding(5, 10, 5, 10);
            setRadius(10);
            setFill(LINEPART_FILL);
            setBorder(LINEPART_BORDER, 1);
            enableEvents(MousePress);
        }

        /**
         * Handle Events.
         */
        protected void processEvent(ViewEvent anEvent)
        {
            // Handle MousePress
            if (anEvent.isMousePress()) {
                int ind = getParent().indexOfChild(this);
                setSelIndex(ind);
                ViewUtils.fireActionEvent(LineView.this, anEvent);
                anEvent.consume();
            }
        }
    }

}