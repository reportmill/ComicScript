package comics.player;
import java.util.*;
import puppets.puppet.*;
import snap.geom.Insets;
import snap.gfx.*;
import snap.util.*;
import snap.web.*;

/**
 * A class to manage animation images and other assets.
 */
public class Asset implements Loadable {

    // The name and full name
    String _name, _nameLC;

    // The path and URL string
    String _path, _urls;

    // The image
    Image _img, _imgFlipX;

    // The height
    double _height;

    // The Index Root
    static String ROOT = AssetIndex.ROOT;

    /**
     * Creates an Asset.
     */
    protected Asset()
    {
    }

    /**
     * Creates an Asset for map.
     */
    public Asset(Map aMap)
    {
        _name = (String) aMap.get("Name");
        _nameLC = _name.toLowerCase();
        _path = '/' + (String) aMap.get("File");
        _urls = ROOT + "actors/" + _path;

        Number hnum = (Number) aMap.get("Height");
        _height = hnum != null ? hnum.doubleValue() : 0;
    }

    /**
     * Returns the full name.
     */
    public String getName()
    {
        return _name;
    }

    /**
     * Returns the full name.
     */
    public String getNameLC()
    {
        return _nameLC != null ? _nameLC : (_nameLC = _name.toLowerCase());
    }

    /**
     * Returns the path.
     */
    public String getPath()
    {
        return _path;
    }

    /**
     * Returns the image.
     */
    public Image getImage()
    {
        return _img != null ? _img : (_img = getImageImpl());
    }

    /**
     * Returns the image.
     */
    protected Image getImageImpl()
    {
        WebURL url = WebURL.getUrl(_urls);
        Image img = Image.getImageForSource(url);

        if (!SnapEnv.isTeaVM && isNewGifAvailable(url)) {
            saveSpriteSheet(url);
            img = Image.getImageForSource(url);
        }
        return img;
    }

    /**
     * Returns the image.
     */
    public Image getImageFlipX()
    {
        if (_imgFlipX != null) return _imgFlipX;
        Image img = getImage();
        if (!img.isLoaded()) return img;
        _imgFlipX = PuppetUtils.getImagesFlipped(img);
        return _imgFlipX;
    }

    /**
     * Returns the height in feet.
     */
    public double getHeight()
    {
        return _height > 0 ? _height : 5;
    }

    /**
     * Returns whether resource is loaded.
     */
    public boolean isLoaded()
    {
        Loadable loadable = getLoadable();
        return loadable == null || loadable.isLoaded();
    }

    /**
     * Adds a callback to be triggered when resources loaded (cleared automatically when loaded).
     */
    public void addLoadListener(Runnable aRun)
    {
        Loadable loadable = getLoadable();
        if (loadable == null) return;
        loadable.addLoadListener(aRun);
    }

    /**
     * Returns the loadable.
     */
    protected Loadable getLoadable()
    {
        return getImage();
    }

    /**
     * Standard toString implementation.
     */
    public String toString()
    {
        String str = getClass().getSimpleName() + "{ ";
        str += "Name:" + getName();
        str += ", Height:" + getHeight();
        return str + " }";
    }

    /**
     * An Asset subclass to manage actor entries.
     */
    public static class ActorImage extends Asset {

        /**
         * Creates a new ActorImage for map.
         */
        public ActorImage()
        {
        }

        public ActorImage(Map aMap)
        {
            super(aMap);
            _urls = ROOT + "actors" + _path;
        }
    }

    /**
     * An Asset subclass to manage actor animation entries.
     */
    public static class AnimImage extends Asset {

        int _frameCount;

        // The offset
        double _offsetX;

        /**
         * Creates a new AnimImage for map.
         */
        public AnimImage()
        {
        }

        public AnimImage(Map aMap)
        {
            super(aMap);
            _urls = ROOT + "actors" + _path;
            _frameCount = Convert.intValue(aMap.get("FrameCount"));
            _offsetX = Convert.doubleValue(aMap.get("Offset"));
        }

        /**
         * Returns the frame count.
         */
        public int getFrameCount()
        {
            return _frameCount;
        }

        /**
         * Returns the offset.
         */
        public double getOffsetX()
        {
            return _offsetX;
        }

        /**
         * Returns the image.
         */
        public Image getImageImpl()
        {
            Image img = super.getImageImpl(), img0 = img;
            if (img.isLoaded()) img = img.getSpriteSheetFrames(_frameCount);
            else img.addLoadListener(() -> _img = img0.getSpriteSheetFrames(_frameCount));
            return img;
        }
    }

    /**
     * An Asset subclass to manage actor entries.
     */
    public static class ActorImagePup extends ActorImage {

        // The Puppet
        Puppet _pup;

        /**
         * Creates a new ActorImage for map.
         */
        public ActorImagePup(String aName)
        {
            _name = aName;
            _path = "/people/" + _name.toLowerCase() + '/' + aName + ".png";
            _pup = PuppetUtils.getPuppetFile().getPuppetForName(aName);
            _height = _pup.getBounds().height / 100;
        }

        /**
         * Returns the image.
         */
        protected Image getImageImpl()
        {
            PuppetAction act = PuppetUtils.getActionFile().getActionForName("Resting");
            Insets ins = new Insets(0, 0, 10, 0);
            Image img = new PuppetImager(_pup, act, 1, ins).getImage();
            return img;
        }
    }

    /**
     * An Asset subclass to manage actor animation entries.
     */
    public static class AnimImagePup extends AnimImage {

        // The Puppet
        Puppet _pup;

        // The Action
        PuppetAction _act;

        /**
         * Creates a new AnimImage for map.
         */
        public AnimImagePup(String aPupName, String anActName, String animName)
        {
            _name = animName;
            _path = "/people/" + _name.toLowerCase() + '/' + animName.replace("-", "") + ".png";
            _pup = PuppetUtils.getPuppetFile().getPuppetForName(aPupName);
            _act = PuppetUtils.getActionFile().getActionForName(anActName);

            _frameCount = _act.getMaxTime() / 25 + 1;
            _offsetX = .25;
        }

        /**
         * Returns the image.
         */
        public Image getImageImpl()
        {
            Insets ins = new Insets(50, 50, 10, 50);
            Image img = new PuppetImager(_pup, _act, 1, ins).getImage();
            _height = (_pup.getBounds().height + ins.top + 17.3) / 100;
            return img;
        }
    }

    /**
     * An Asset subclass to manage setting entries.
     */
    public static class SetImage extends Asset {

        /**
         * Creates a new SetImage for map.
         */
        public SetImage(Map aMap)
        {
            super(aMap);
            _urls = ROOT + "settings" + _path;
        }
    }

    private static boolean isNewGifAvailable(WebURL aURL)
    {
        // If not looking for png file return false
        if (!aURL.getPath().endsWith(".png")) return false;

        // Get GIF URL
        WebURL url2 = WebURL.getUrl(FilePathUtils.getPeerPath(aURL.getString(), aURL.getFilenameSimple() + ".gif"));

        // If no gif file or it's older than
        long pngMod = aURL.isFound() ? aURL.getHead().getLastModTime() : 0;
        long gifMod = url2.isFound() ? url2.getHead().getLastModTime() : 0;
        return gifMod > pngMod;
    }

    /**
     * Saves an image to a sprite sheet.
     */
    private static void saveSpriteSheet(WebURL aURL)
    {
        // Get URL for GIF file
        WebURL url = WebURL.getUrl(FilePathUtils.getPeerPath(aURL.getString(), aURL.getFilenameSimple() + ".gif"));
        Image img = Image.getImageForSource(url);

        // Scale image
        if (img.getImageSet() != null) img = img.getImageSet().getImageScaled(.5);
        else img = img.cloneForScale(.5);

        // Create sprite sheet image, get PNG bytes, set in file and save
        Image sheet = img.getImageSet() != null ? img.getImageSet().getSpriteSheetImage() : img;
        byte bytes[] = sheet.getBytesPNG();
        WebFile file = aURL.createFile(false);
        file.setBytes(bytes);
        file.save();
    }

    /**
     * Returns an anim asset for name.
     */
    public static AnimImage getAnimAsset(String aStarName, String anAnimName)
    {
        String name = FilePathUtils.getFilenameSimple(aStarName);
        return AssetIndex.get().getAnimAsset(name, anAnimName);
    }

}