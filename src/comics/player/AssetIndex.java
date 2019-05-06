package comics.player;
import java.util.*;
import snap.gfx.Image;
import snap.util.*;
import snap.web.*;
import comics.player.Asset.*;

/**
 * A class to manage animation assets (images).
 */
public class AssetIndex {
    
    // All Assets
    List <Asset>         _assets;

    // The actors
    List <ActorImage>    _actors;
    
    // The anims
    List <AnimImage>     _anims;
    
    // The settings
    List <SetImage>      _sets;
    
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
    _assets = new ArrayList(); _actors = new ArrayList();
    for(int i=0;i<actors.getNodeCount();i++) { Map map = actors.getNode(i).getAsMap();
        addAsset(new ActorImage(map)); }
    
    // Get Anims
    _anims = new ArrayList();
    for(int i=0;i<anims.getNodeCount();i++) { Map map = anims.getNode(i).getAsMap();
        addAsset(new AnimImage(map)); }
    
    // Get Settings
    _sets = new ArrayList();
    for(int i=0;i<settings.getNodeCount();i++) { Map map = settings.getNode(i).getAsMap();
        addAsset(new SetImage(map)); }
}

/**
 * Adds a new Asset
 */
void addAsset(Asset aAsset)
{
    _assets.add(aAsset);
    if(aAsset instanceof ActorImage) _actors.add((ActorImage)aAsset);
    else if(aAsset instanceof AnimImage) _anims.add((AnimImage)aAsset);
    else if(aAsset instanceof SetImage) _sets.add((SetImage)aAsset);
}

/**
 * Returns the actor image asset for given name.
 */
public ActorImage getActorAsset(String aName)
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
    ActorImage asset = getActorAsset(aName); if(asset==null) return null;
    return asset.getImage();
}

/**
 * Returns the anim with given actor and anim.
 */
public AnimImage getAnimAsset(String anActor, String anAnim)
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
    AnimImage asset = getAnimAsset(anActor, anAnim); if(asset==null) return null;
    return asset.getImage();
}

/**
 * Returns the setting image asset for given name.
 */
public SetImage getSetAsset(String aName)
{
    String name = aName.toLowerCase();
    for(SetImage asset : _sets) { if(name.equals(asset.getNameLC()))
            return asset; }
    return null;
}

/**
 * Returns the setting image for name.
 */
public Image getSetImage(String aName)
{
    SetImage asset = getSetAsset(aName); if(asset==null) return null;
    return asset.getImage();
}

/**
 * Returns the paths for given directory.
 */
public String[] getDirPaths(String aPath)
{
    List <String> paths = new ArrayList();
    String path = aPath; if(!path.endsWith("/")) path += '/';
    for(Asset as : _actors) { String p = as.getPath();
        if(p.startsWith(path)) {
            int ind = p.indexOf('/', path.length());
            if(ind>0) p = p.substring(0, ind+1);
            if(!paths.contains(p)) paths.add(p);
        }
    }
    return paths.toArray(new String[paths.size()]);
}

/**
 * Returns the shared index.
 */
public static AssetIndex get()  { return _shared; }

}