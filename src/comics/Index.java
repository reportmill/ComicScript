package comics;
import java.util.*;
import snap.util.FilePathUtils;
import snap.util.JSONNode;
import snap.util.SnapUtils;
import snap.web.WebURL;

/**
 * A class to manage assetts.
 */
public class Index {

    // The actors
    List <Map>           _actors;
    
    // The settings
    List <Map>           _settings;
    
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
    JSONNode settings = root.getNode("Settings");
    
    _actors = new ArrayList();
    for(int i=0;i<actors.getNodeCount();i++) {
        JSONNode actor = actors.getNode(i);
        Map map = actor.getAsMap();
        String name = (String)map.get("Name"); map.put("Name2", name.toLowerCase());
        _actors.add(map);
    }
    
    _settings = new ArrayList();
    for(int i=0;i<settings.getNodeCount();i++) {
        JSONNode setting = settings.getNode(i);
        Map map = setting.getAsMap();
        String name = (String)map.get("Name"); map.put("Name2", name.toLowerCase());
        _settings.add(map);
    }
    
    // Add actor and setting files to known files
    if(SnapUtils.isTeaVM) {
        for(Map m : _actors) snaptea.TVWebSite.addKnownPath("/files/actors/" + m.get("File"));
        for(Map m : _settings) snaptea.TVWebSite.addKnownPath("/files/settings/" + m.get("File"));
    }
}

/**
 * Returns the actor with given name.
 */
public Map getActor(String aName)
{
    String name = aName.toLowerCase();

    for(Map actor : _actors) { String name2 = (String)actor.get("Name2");
        if(name.equals(name2))
            return actor;
    }
    return null;
}

/**
 * Returns the actor file path for name.
 */
public String getActorFilePath(String aName)
{
    Map obj = getActor(aName); if(obj==null) return null;
    String path = (String)obj.get("File");
    return ROOT + "actors/" + path;
}

/**
 * Returns the actor file path for name.
 */
public String getActorFilePath(String aName, String anAnim)
{
    Map obj = getActor(aName); if(obj==null) return null;
    List <JSONNode> anims = (List)obj.get("Anims"); if(anims==null) return null;
    String anim = null;
    for(JSONNode jn : anims) if(jn.getString().equalsIgnoreCase(anAnim)) { anim = jn.getString(); break; }
    String path = (String)obj.get("File"); if(anim==null) return null;
    String path2 = FilePathUtils.getPeer(path, obj.get("Name") + anim + ".gif");
    return ROOT + "actors/" + path2;
}

/**
 * Returns the setting with given name.
 */
public Map getSetting(String aName)
{
    String name = aName.toLowerCase();

    for(Map setting : _settings) { String name2 = (String)setting.get("Name2");
        if(name.equals(name2))
            return setting;
    }
    return null;
}

/**
 * Returns the setting file path for name.
 */
public String getSettingFilePath(String aName)
{
    Map obj = getSetting(aName); if(obj==null) return null;
    String path = (String)obj.get("File");
    return ROOT + "settings/" + path;
}

/**
 * Returns the shared index.
 */
public static Index get()  { return _shared; }

}