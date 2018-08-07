package comics;
import java.util.*;
import snap.util.JSONNode;

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
    JSONNode root = JSONNode.readSource(ROOT + "index.json");
    JSONNode actors = root.getNode("Actors");
    JSONNode settings = root.getNode("Settings");
    
    _actors = new ArrayList();
    for(int i=0;i<actors.getNodeCount();i++) {
        JSONNode actor = actors.getNode(i);
        Map map = actor.getAsMap();
        String name = (String)map.get("Name"); name = name.toLowerCase(); map.put("Name", name);
        _actors.add(map);
    }
    
    _settings = new ArrayList();
    for(int i=0;i<settings.getNodeCount();i++) {
        JSONNode setting = settings.getNode(i);
        Map map = setting.getAsMap();
        String name = (String)map.get("Name"); name = name.toLowerCase(); map.put("Name", name);
        _settings.add(map);
    }
}

/**
 * Returns the actor with given name.
 */
public Map getActor(String aName)
{
    String name = aName.toLowerCase();

    for(Map actor : _actors) { String name2 = (String)actor.get("Name");
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
 * Returns the setting with given name.
 */
public Map getSetting(String aName)
{
    String name = aName.toLowerCase();

    for(Map setting : _settings) { String name2 = (String)setting.get("Name");
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