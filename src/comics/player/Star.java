package comics.player;
import snap.gfx.Image;
import snap.util.Loadable;

/**
 * A class to represent elements of a script that can perform actions (Actors, Camera, Setting).
 */
public interface Star extends Loadable {

/**
 * Returns the name.
 */
public String getStarName();

/**
 * Returns the image.
 */
public Image getStarImage();

/**
 * Returns the action names for this star.
 */
public String[] getActionNames();

/**
 * Returns an action for given ScriptLine.
 */
public Action getAction(ScriptLine aScriptLine);

/**
 * Resets the star to initial settings.
 */
void resetStar();

}