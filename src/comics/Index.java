package comics;
import java.util.*;
import snap.gfx.Image;
import snap.util.JSONNode;
import snap.util.SnapUtils;
import snap.web.WebURL;

/**
 * A class to manage assetts.
 */
public class Index {

    // The actors
    List <ActorEntry>    _actors;
    
    // The anims
    List <AnimEntry>     _anims;
    
    // The settings
    List <SettingEntry>  _settings;
    
    // The root path
    static String ROOT = "/Temp/ComicScriptLib/";

    // The shared index
    static Index _shared = new Index();
    
/**
 * Creates the index.
 */
public Index()
{
    if(SnapUtils.isTeaVM) {
        ROOT = "http://localhost/files/";
        snaptea.TVWebSite.addKnownPath("/files/index.json");
    }
    
    WebURL url = WebURL.getURL(ROOT + "index.json");
    JSONNode root = JSONNode.readSource(url);
    JSONNode actors = root.getNode("Actors");
    JSONNode anims = root.getNode("Anims");
    JSONNode settings = root.getNode("Settings");
    
    // Get Actors
    _actors = new ArrayList();
    for(int i=0;i<actors.getNodeCount();i++) { Map map = actors.getNode(i).getAsMap();
        _actors.add(new ActorEntry(map)); }
    
    // Get Anims
    _anims = new ArrayList();
    for(int i=0;i<anims.getNodeCount();i++) { Map map = anims.getNode(i).getAsMap();
        _anims.add(new AnimEntry(map)); }
    
    // Get Settings
    _settings = new ArrayList();
    for(int i=0;i<settings.getNodeCount();i++) { Map map = settings.getNode(i).getAsMap();
        _settings.add(new SettingEntry(map)); }
}

/**
 * Returns the actor entry for given name.
 */
public ActorEntry getActor(String aName)
{
    String name = aName.toLowerCase();
    for(ActorEntry entry : _actors) { if(name.equals(entry.getNameLC()))
        return entry; }
    return null;
}

/**
 * Returns the actor image for given name.
 */
public Image getActorImage(String aName)
{
    ActorEntry entry = getActor(aName); if(entry==null) return null;
    return entry.getImage();
}

/**
 * Returns the anim with given actor and anim.
 */
public AnimEntry getAnim(String anActor, String anAnim)
{
    String name = anActor.toLowerCase() + '-' + anAnim.toLowerCase();
    for(AnimEntry entry : _anims) { if(name.equals(entry.getNameLC()))
        return entry; }
    return null;
}

/**
 * Returns the anims image for actor and anim.
 */
public Image getAnimImage(String anActor, String anAnim)
{
    AnimEntry entry = getAnim(anActor, anAnim); if(entry==null) return null;
    return entry.getImage();
}

/**
 * Returns the setting entry for given name.
 */
public SettingEntry getSetting(String aName)
{
    String name = aName.toLowerCase();
    for(SettingEntry entry : _settings) { if(name.equals(entry.getNameLC()))
            return entry; }
    return null;
}

/**
 * Returns the setting image for name.
 */
public Image getSettingImage(String aName)
{
    SettingEntry entry = getSetting(aName); if(entry==null) return null;
    return entry.getImage();
}

/**
 * Returns the shared index.
 */
public static Index get()  { return _shared; }

/**
 * A class to manage index entries.
 */
public static class IndexEntry {

    // The name and full name
    String          _name, _nameLC;
    
    // The path and URL string
    String          _path, _urls;
    
    // The image
    Image           _img;
    
    /** Creates a new IndexEntry for map. */
    public IndexEntry(Map aMap)
    {
        _name = (String)aMap.get("Name"); _nameLC = _name.toLowerCase();
        _path = (String)aMap.get("File");
        _urls = ROOT + "actors/" + _path;
    }
    
    /** Returns the full name. */
    public String getNameLC()  { return _nameLC; }
    
    /** Returns the image. */
    public Image getImage()
    {
        if(_img!=null) return _img;
        WebURL url = WebURL.getURL(_urls);
        if(SnapUtils.isTeaVM) snaptea.TVWebSite.addKnownPath(url.getPath());
        _img = Image.get(url);
        return _img;
    }
}

/**
 * An IndexEntry subclass to manage actor entries.
 */
public static class ActorEntry extends IndexEntry {

    /** Creates a new ActorEntry for map. */
    public ActorEntry(Map aMap)  { super(aMap); _urls = ROOT + "actors/" + _path; }
}

/**
 * An IndexEntry subclass to manage actor animation entries.
 */
public static class AnimEntry extends IndexEntry {

    /** Creates a new AnimEntry for map. */
    public AnimEntry(Map aMap)  { super(aMap); _urls = ROOT + "actors/" + _path; }
}

/**
 * An IndexEntry subclass to manage setting entries.
 */
public static class SettingEntry extends IndexEntry {

    /** Creates a new SettingEntry for map. */
    public SettingEntry(Map aMap)  { super(aMap); _urls = ROOT + "settings/" + _path; }
}

}