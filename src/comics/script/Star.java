package comics.script;
import snap.gfx.Image;

/**
 * A class to represent elements of a script that can perform actions (Actors, Camera, Setting).
 */
public interface Star {

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

}