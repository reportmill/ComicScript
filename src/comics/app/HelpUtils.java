package comics.app;
import comics.player.ScriptLine;
import java.util.*;
import snap.util.Range;

/**
 * A class to assist in providing suggestsion for ScriptLine Star, Action, Predicates.
 */
public class HelpUtils {

    // Constants
    public static final int FRAG_STAR = 0;
    public static final int FRAG_ACTION = 1;
    public static final int FRAG_PREDICATE = 2;

/**
 * Returns the frag type name at index.
 */
public static String getFragTypeNameAtCharIndex(ScriptLine aLine, int anIndex)
{
    int part = HelpUtils.getFragTypeAtCharIndex(aLine, anIndex);
    String name = part==FRAG_STAR? "Subjects" : part==FRAG_ACTION? "Actions" : "Predicates";
    return name;    
}

/**
 * Returns the line part at char index (Star, Action or Predicate).
 */
public static int getFragTypeAtCharIndex(ScriptLine aLine, int anIndex)
{
    // If index is in star name range, return star
    String text = aLine.getText().toLowerCase();
    String starName = aLine.getStarName(); if(starName==null) return FRAG_STAR;
    int starEndInd = text.indexOf(starName) + starName.length() + 1;
    if(anIndex<starEndInd)
        return FRAG_STAR;
    
    // If index is in action name range, return star
    String actName = aLine.getActionName(); if(actName==null) return FRAG_ACTION;
    int actEndInd = text.indexOf(actName, starEndInd) + actName.length() + 1;
    if(anIndex<actEndInd)
        return FRAG_ACTION;

    // Return predicate
    return FRAG_PREDICATE;
}

/**
 * Returns the line part at char index (Star, Action or Predicate).
 */
public static Range getFragRangeAtCharIndex(ScriptLine aLine, int anIndex)
{
    // If index is in star name range, return star
    String text = aLine.getText().toLowerCase();
    String starName = aLine.getStarName(); if(starName==null) return new Range(0,0);
    int starInd = text.indexOf(starName), starEnd = starInd + starName.length();
    if(anIndex<starEnd+1)
        return new Range(starInd, starEnd);
    
    // If index is in action name range, return star
    String actName = aLine.getActionName(); if(actName==null) return new Range(starEnd+1, starEnd+1);
    int actInd = text.indexOf(actName, starEnd+1), actEnd = actInd + actName.length();
    if(anIndex<actEnd+1)
        return new Range(actInd, actEnd);
        
    // Get predicate start by to next valid char and return range to end of text
    int predInd = actEnd;
    while(predInd<text.length()) { char c = text.charAt(predInd);
        if(c==',' || Character.isWhitespace(c)) predInd++; else break; }
    return new Range(predInd, text.length());
}

/**
 * Returns a list of Help strings.
 */
public static String[] getHelpItems(ScriptLine aLine, int anIndex)
{
    // Get all help items for frag type at index
    int type = getFragTypeAtCharIndex(aLine, anIndex);
    String helpItems[] = getHelpItemsForType(aLine, type);
        
    // If index at end of range, filter list
    Range range = getFragRangeAtCharIndex(aLine, anIndex);
    if(anIndex==range.end && !range.isEmpty()) {
        String fragStr = aLine.getText().substring(range.start, range.end).toLowerCase();
        List <String> helpItemsList = new ArrayList();
        for(String s : helpItems)
            if(s.toLowerCase().startsWith(fragStr))
                helpItemsList.add(s);
        helpItems = helpItemsList.toArray(new String[helpItemsList.size()]);
    }
        
    // Return help items
    return helpItems;
}

/**
 * Returns a list of Help strings.
 */
public static String[] getHelpItemsForType(ScriptLine aLine, int aType)
{
    if(aType==FRAG_STAR)
        return ScriptEditor._stars;
    if(aType==FRAG_ACTION) {
        return aLine.getStar()!=null? aLine.getStar().getActionNames() : new String[0]; }
    if(aType==FRAG_PREDICATE) {
        return aLine.getAction()!=null? aLine.getAction().getPredicateStrings() : new String[0]; }
    System.err.println("HelpUtils.getHelpItemsForType: Unknown type: " + aType);
    return new String[0];
}

/**
 * Returns a whether help items are filtered for this line and index.
 */
public static boolean isHelpItemsFiltered(ScriptLine aLine, int anIndex)
{
    // Get all help items for frag type at index
    int type = getFragTypeAtCharIndex(aLine, anIndex);
    String helpItems[] = getHelpItemsForType(aLine, type);
    if(helpItems.length==0) return false;
        
    // If index at end of range, filter list
    Range range = getFragRangeAtCharIndex(aLine, anIndex);
    return anIndex==range.end && !range.isEmpty();
}

/**
 * Returns the frag remainder string for this line and index.
 */
public static String getFragCompletion(ScriptLine aLine, int anIndex, String aFragStr)
{
    Range range = getFragRangeAtCharIndex(aLine, anIndex);
    return aFragStr.substring(range.length);
}

}