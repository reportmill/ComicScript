package comics.app;
import snap.gfx.Color;
import snap.view.ChildView;

/**
 * A class to hold actors.
 */
public class StageView extends ChildView {

/**
 * Creates a StageView.
 */
public StageView()
{
    setPrefSize(720, 405);
    setFill(Color.BLACK); setBorder(Color.BLACK, 1);
    setFocusable(false); setFocusWhenPressed(false); // Only need this because SnapScene superclass sets
}

}