package comics.player;

import java.util.*;

import snap.gfx.*;
import snap.util.Loadable;
import snap.view.*;

/**
 * A class to hold actors.
 */
public class StageView extends ChildView implements Star {

    // The background image
    Image  _backImg;

    // The actors currently used by script
    Map<String, Actor>  _actors = new HashMap();

    // The list of setting names
    String[]  _setNames;

    /**
     * Creates a StageView.
     */
    public StageView()
    {
        setPrefSize(720, 405);
        setFill(Color.WHITE);
        setBorder(Color.BLACK, 1);
        setFocusable(false);
        setFocusWhenPressed(false); // Only need this because SnapScene superclass sets
    }

    /**
     * Returns the star name.
     */
    public String getStarName()
    {
        return "Setting";
    }

    /**
     * Returns the star image.
     */
    public Image getStarImage()
    {
        return null;
    }

    /**
     * Returns the action names for this star.
     */
    public String[] getActionNames()
    {
        return new String[]{"is"};
    }

    /**
     * Returns the action names for this star.
     */
    public String[] getSetNames()
    {
        if (_setNames != null) return _setNames;

        List<String> sets = new ArrayList();
        sets.add("Blank");
        for (Asset.SetImage set : AssetIndex.get()._sets)
            sets.add(set.getName());
        return _setNames = sets.toArray(new String[sets.size()]);
    }

    /**
     * Returns an Action for this star and given ScriptLine.
     */
    public Action getAction(ScriptLine aScriptLine)
    {
        Action action = new BackImageAction();
        action.setLine(aScriptLine);
        return action;
    }

    /**
     * Returns the actor for given name.
     */
    public Actor getActor(ScriptLine aScriptLine)
    {
        // Get image for word
        String sname = aScriptLine.getStarName();
        Asset asset = AssetIndex.get().getActorAsset(sname);
        if (asset == null)
            return null;

        // Get actor for name
        String name = asset.getName();
        Actor actor = _actors.get(name);

        // If actor not found, create
        if (actor == null) {

            // Create new ActorView for Asset and add to Stage
            actor = new Actor(aScriptLine.getScript(), asset);
            _actors.put(name, actor);
            addChild(actor);
        }

        // Return actor
        return actor;
    }

    /**
     * Returns the background image.
     */
    public Image getBackImage()
    {
        return _backImg;
    }

    /**
     * Sets the background image.
     */
    public void setBackImage(Image anImage)
    {
        if (anImage == _backImg) return;
        _backImg = anImage;
        repaint();
        if (_backImg != null && !_backImg.isLoaded()) _backImg.addLoadListener(() -> repaint());
    }

    /**
     * Override to paint image.
     */
    protected void paintBack(Painter aPntr)
    {
        super.paintBack(aPntr);
        if (_backImg != null)
            aPntr.drawImage(_backImg, 0, 0, getWidth(), getHeight());
    }

    /**
     * Override to reset stage (and actors).
     */
    public void resetStar()
    {
        setBackImage(null); //getAnimCleared(0);
        for (Actor actor : _actors.values())
            actor.resetStar();
    }

    /**
     * Returns whether resource is loaded.
     */
    public boolean isLoaded()
    {
        return true;
    }

    /**
     * Adds a callback to be triggered when resources loaded (cleared automatically when loaded).
     */
    public void addLoadListener(Runnable aRun)
    {
    }

    /**
     * An Setting Action that changes background image.
     */
    public class BackImageAction extends Action {

        /**
         * Creates the action.
         */
        public BackImageAction()
        {
            setName("is");
            setRunTime(1);
        }

        /**
         * Returns the background image.
         */
        public Image getImage()
        {
            ScriptLine line = getLine();
            String words[] = line.getWords();
            Asset asset = getAsset(words, 1);
            Image img = asset != null ? asset.getImage() : null;
            return img;
        }

        /**
         * Returns the loadable.
         */
        protected Loadable getLoadable()
        {
            return getImage();
        }

        /**
         * Runs the action.
         */
        public void run()
        {
            Image img = getImage();
            StageView.this.setBackImage(img);
        }

        /**
         * Returns the predicate strings.
         */
        public String[] getPredicateStrings()
        {
            return getSetNames();
        }
    }

    /**
     * Returns the first SetAsset with name matching any word in given string arry.
     */
    static Asset getAsset(String theWords[], int aStart)
    {
        for (int i = aStart; i < theWords.length; i++) {
            String word = theWords[i];
            Asset asset = AssetIndex.get().getSetAsset(word);
            if (asset != null)
                return asset;
        }
        return null;
    }

}