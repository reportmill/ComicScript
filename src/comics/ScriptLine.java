package comics;

/**
 * A class to represent a line of script.
 */
public class ScriptLine {
    
    // The Script
    Script      _script;
    
    // The line text
    String      _text;
    
    // The words
    String      _words[];
    
/**
 * Creates a new ScriptLine with given text.
 */
public ScriptLine(Script aScript, String aStr)
{
    _script = aScript; _text = aStr;
}

/**
 * Returns the text.
 */
public String getText()  { return _text; }

/**
 * Returns the words.
 */
public String[] getWords()
{
    // If already set, just return
    if(_words!=null) return _words;
    
    // Get text and words
    String text = getText().toLowerCase();
    String words[] = text.split("\\s");
    for(int i=0;i<words.length;i++) words[i] = words[i].replace(",","").replace("\"","");
    return _words = words;
}

/**
 * Returns the index.
 */
public int getIndex()  { return _script.getLines().indexOf(this); }

/**
 * Standard toString implementation.
 */
public String toString()
{
    String str = getText(); if(str.trim().length()==0) str = "(empty)";
    return "SriptLine: " + str;
}

}