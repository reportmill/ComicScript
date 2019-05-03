package comics.app;
import comics.script.*;
import java.util.*;
import snap.view.*;

/**
 * A class to provide UI for selecting the Star of a ScriptLine.
 */
public class StarPicker extends ViewOwner {
    
    // The ScriptLineEditor
    ScriptLineEditor   _lineEditor;

    // The view to show list of stars in script
    StarListView       _starsView;
    
/**
 * Creates StarPicker.
 */
public StarPicker(ScriptLineEditor aLE)
{
    _lineEditor = aLE;
}

/**
 * Returns the current ScriptLine.
 */
public ScriptLine getSelLine()  { return _lineEditor.getSelLine(); }

/**
 * Returns the Star for current ScriptLine.
 */
public Star getStar()
{
    ScriptLine line = getSelLine();
    return line.getStar();
}

/**
 * Sets the Star for current ScriptLine.
 */
public void setStar(Star aStar)
{
    ScriptLine line = getSelLine();
    line.setStar(aStar);
    _lineEditor._editorPane.runCurrentLine();
    resetLater();
}

/**
 * Called when Script text changes.
 */
protected void scriptChanged()
{
    if(!isUISet()) return;
    setStarsForPlayer(_lineEditor.getPlayer());
    resetLater();
}

/**
 * Creates UI.
 */
protected View createUI()
{
    // Get MainColView from UI file
    ColView mainColView =  (ColView)super.createUI(); mainColView.setFillWidth(true);

    // Create/add StarListView
    _starsView = new StarListView();
    mainColView.addChild(_starsView,1);
    
    // Return MainColView
    return mainColView;
}

/**
 * Initialize UI.
 */
protected void initUI()
{
    // Configure StarsView
    setStarsForPlayer(_lineEditor.getPlayer());
}

/**
 * Resets the UI.
 */
protected void resetUI()
{
    // Get current ScriptLine and Star
    ScriptLine line = getSelLine();
    Star star = line.getStar();
    
    // Update StarListView
    _starsView.setSelStar(star);
}

/**
 * Responds to the UI.
 */
protected void respondUI(ViewEvent anEvent)
{
    // Get current ScriptLine
    ScriptLine line = getSelLine();
    
    // Handle StarListView
    if(anEvent.equals(_starsView)) {
        Star star = _starsView.getSelStar();
        setStar(star);
    }
        
}

/**
 * Returns a list of stars for a PlayerView.
 */
protected List <Star> getStarsForPlayer(PlayerView aPlayer)
{
    Script script = aPlayer.getScript();
    List <Star> stars = new ArrayList();
    
    Star set = script.getSetting(); stars.add(set);
    Star cam = aPlayer.getCamera(); stars.add(cam);
   
    // Iterate over lines
    for(ScriptLine sline : script.getLines()) {
        Star star = sline.getStar(); if(star==null) continue;
        if(!stars.contains(star)) stars.add(star);
    }
    
    // Returns stars
    return stars;
}

/**
 * Sets the list of stars for given Player.
 */
protected void setStarsForPlayer(PlayerView aPlayer)
{
    List <Star> stars = getStarsForPlayer(aPlayer);
    _starsView.setStars(stars);
}

}