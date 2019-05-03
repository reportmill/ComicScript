package comics.script;
import java.util.*;
import snap.gfx.Image;
import snap.util.*;
import snap.web.*;
import comics.script.Asset.*;

/**
 * A class to manage animation assets (images).
 */
public class AssetIndex {

    // The actors
    List <ActorImage>    _actors;
    
    // The anims
    List <AnimImage>     _anims;
    
    // The settings
    List <SettingImage>  _settings;
    
    // The root path
    static String ROOT = "/Temp/ComicLib/";

    // The shared index
    static AssetIndex  _shared = new AssetIndex();
    
/**
 * Creates the index.
 */
public AssetIndex()
{
    if(SnapUtils.isTeaVM)
        ROOT = "http://reportmill.com/ComicLib/";
    
    WebURL url = WebURL.getURL(ROOT + "index.json");
    JSONNode root = JSONNode.readSource(url);
    JSONNode actors = root.getNode("Actors");
    JSONNode anims = root.getNode("Anims");
    JSONNode settings = root.getNode("Settings");
    
    // Get Actors
    _actors = new ArrayList();
    for(int i=0;i<actors.getNodeCount();i++) { Map map = actors.getNode(i).getAsMap();
        _actors.add(new ActorImage(map)); }
    
    // Get Anims
    _anims = new ArrayList();
    for(int i=0;i<anims.getNodeCount();i++) { Map map = anims.getNode(i).getAsMap();
        _anims.add(new AnimImage(map)); }
    
    // Get Settings
    _settings = new ArrayList();
    for(int i=0;i<settings.getNodeCount();i++) { Map map = settings.getNode(i).getAsMap();
        _settings.add(new SettingImage(map)); }
}

/**
 * Returns the actor image asset for given name.
 */
public ActorImage getActor(String aName)
{
    String name = aName.toLowerCase();
    for(ActorImage asset : _actors) { if(name.equals(asset.getNameLC()))
        return asset; }
    return null;
}

/**
 * Returns the actor image for given name.
 */
public Image getActorImage(String aName)
{
    ActorImage asset = getActor(aName); if(asset==null) return null;
    return asset.getImage();
}

/**
 * Returns the anim with given actor and anim.
 */
public AnimImage getAnim(String anActor, String anAnim)
{
    String name = anActor.toLowerCase() + '-' + anAnim.toLowerCase();
    for(AnimImage asset : _anims) { if(name.equals(asset.getNameLC()))
        return asset; }
    return null;
}

/**
 * Returns the anims image for actor and anim.
 */
public Image getAnimImage(String anActor, String anAnim)
{
    AnimImage asset = getAnim(anActor, anAnim); if(asset==null) return null;
    return asset.getImage();
}

/**
 * Returns the setting image asset for given name.
 */
public SettingImage getSetting(String aName)
{
    String name = aName.toLowerCase();
    for(SettingImage asset : _settings) { if(name.equals(asset.getNameLC()))
            return asset; }
    return null;
}

/**
 * Returns the setting image for name.
 */
public Image getSettingImage(String aName)
{
    SettingImage asset = getSetting(aName); if(asset==null) return null;
    return asset.getImage();
}

/**
 * Returns the shared index.
 */
public static AssetIndex get()  { return _shared; }

}