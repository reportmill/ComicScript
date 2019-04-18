package comics.script;
import java.util.*;
import snap.util.StringUtils;
import snap.web.WebURL;

/**
 * A class to manage some sample scripts.
 */
public class Samples {

    // The Map of samples
    static Map <String, String[]>  _samples;

/**
 * Returns the samples.
 */
public static Map <String,String[]> getSamples()
{
    if(_samples!=null) return _samples;
    
    String text = WebURL.getURL(Samples.class, "Samples.txt").getText();
    String lines[] = text.split("\n");
    
    _samples = new HashMap();
    String scriptName = null;
    List <String> script = new ArrayList();
    
    for(String line : lines) { line = line.trim(); if(line.length()==0) continue;
    
        if(line.startsWith("Sample ")) {
            if(scriptName!=null) _samples.put(scriptName, script.toArray(new String[0]));
            scriptName = line.substring("Sample ".length()).trim();
            script.clear();
        }
        
        else script.add(line);
    }
    
    if(scriptName!=null && script.size()>0)
        _samples.put(scriptName, script.toArray(new String[0]));
    return _samples;
}

/**
 * Returns the Samples text.
 */
public static String getSample(String aName)
{
    String lines[] = getSamples().get(aName);
    String text = StringUtils.join(lines, "\n");
    return text;
}

}