package comics.player;
import java.util.*;
import snap.gfx.Image;
import snap.util.*;
import snap.web.*;

/**
 * A class to manage animation images and other assets.
 */
public class Asset {

    // The name and full name
    String          _name, _nameLC;
    
    // The path and URL string
    String          _path, _urls;
    
    // The image
    Image           _img;
    
    // The height
    double          _height;
    
    // The Index Root
    static String ROOT = AssetIndex.ROOT;
    
/**
 * Creates an Asset for map.
 */
public Asset(Map aMap)
{
    _name = (String)aMap.get("Name"); _nameLC = _name.toLowerCase();
    _path = '/' + (String)aMap.get("File");
    _urls = ROOT + "actors/" + _path;
    
    Number hnum = (Number)aMap.get("Height");
    _height = hnum!=null? hnum.doubleValue() : 0;
}

/**
 * Returns the full name.
 */
public String getName()  { return _name; }

/**
 * Returns the full name.
 */
public String getNameLC()  { return _nameLC; }

/**
 * Returns the path.
 */
public String getPath()  { return _path; }

/**
 * Returns the image.
 */
public Image getImage()  { return _img!=null? _img : (_img=getImageImpl()); }

/**
 * Returns the image.
 */
protected Image getImageImpl()
{
    WebURL url = WebURL.getURL(_urls);
    Image img = Image.get(url);
    
    if(!SnapUtils.isTeaVM && isNewGifAvailable(url)) {
        saveSpriteSheet(url);
        img = Image.get(url);
    }
    return img;
}

/**
 * Returns whether image is loaded.
 */
public boolean isImageLoaded()  { Image img = getImage(); return img==null || img.isLoaded(); }

/**
 * Returns the height in feet.
 */
public double getHeight()  { return _height>0? _height : 5; }

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

    /** Creates a new ActorImage for map. */
    public ActorImage(Map aMap)  { super(aMap); _urls = ROOT + "actors" + _path; }
}

/**
 * An Asset subclass to manage actor animation entries.
 */
public static class AnimImage extends Asset {
    
    int     _frameCount;

    // The offset
    double  _offsetX;
    
    /** Creates a new AnimImage for map. */
    public AnimImage(Map aMap)
    {
        super(aMap); _urls = ROOT + "actors" + _path;
        _frameCount = SnapUtils.intValue(aMap.get("FrameCount"));
        _offsetX = SnapUtils.doubleValue(aMap.get("Offset"));
    }
    
    /** Returns the offset. */
    public double getOffsetX()  { return _offsetX; }

    /** Returns the image. */
    public Image getImageImpl()
    {
        Image img = super.getImageImpl(), img0 = img;
        if(img.isLoaded()) img = img.getSpriteSheetFrames(_frameCount);
        else img.addLoadListener(pc -> _img = img0.getSpriteSheetFrames(_frameCount));
        return img;
    }
}

/**
 * An Asset subclass to manage setting entries.
 */
public static class SetImage extends Asset {

    /** Creates a new SetImage for map. */
    public SetImage(Map aMap)  { super(aMap); _urls = ROOT + "settings" + _path; }
}

private static boolean isNewGifAvailable(WebURL aURL)
{
    // If not looking for png file return false
    if(!aURL.getPath().endsWith(".png")) return false;
    
    // Get GIF URL
    WebURL url2 = WebURL.getURL(FilePathUtils.getPeer(aURL.getString(), aURL.getPathNameSimple() + ".gif"));
    
    // If no gif file or it's older than
    long pngMod = aURL.isFound()? aURL.getHead().getModTime() : 0;
    long gifMod = url2.isFound()? url2.getHead().getModTime() : 0;
    return gifMod>pngMod;
}

/**
 * Saves an image to a sprite sheet.
 */
private static void saveSpriteSheet(WebURL aURL)
{
    // Get URL for GIF file
    WebURL url = WebURL.getURL(FilePathUtils.getPeer(aURL.getString(), aURL.getPathNameSimple() + ".gif"));
    Image img = Image.get(url);
    
    // Scale image
    if(img.getImageSet()!=null) img = img.getImageSet().getImageScaled(.5);
    else img = img.getImageScaled(.5);
    
    // Create sprite sheet image, get PNG bytes, set in file and save
    Image sheet = img.getImageSet()!=null? img.getImageSet().getSpriteSheetImage() : img;
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
    String name = FilePathUtils.getFileNameSimple(aStarName);
    return AssetIndex.get().getAnimAsset(name, anAnimName);
}

}