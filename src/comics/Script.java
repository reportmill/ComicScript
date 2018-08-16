package comics;
import java.util.ArrayList;
import java.util.List;
import snap.viewx.*;

/**
 * A class to represent the instructions.
 */
public class Script {
    
    // The View text
    String             _text = "Setting is beach\n";

    // The Script lines
    List <ScriptLine>  _lines;
    
    // The current line index
    int                _lineIndex, _lineIndexMax;
    
    // The time
    int                _time;

/**
 * Returns the Script text.
 */
public String getText()  { return _text; }

/**
 * Sets the text.
 */
public void setText(String aStr)
{
    _text = aStr; _lines = null;
}

/**
 * Returns the lines.
 */
public List <ScriptLine> getLines()
{
    if(_lines!=null) return _lines;
    
    List slines = new ArrayList();
    String tlines[] = _text.split("\\n");
    
    for(String tline : tlines)
        slines.add(new ScriptLine(this, tline));

    return _lines = slines;
}

/**
 * Runs for Stage.
 */
public void run(SnapScene aScene, int lineIndex)
{
    _time = 0; _lineIndexMax = Math.min(lineIndex+1, getLines().size());
    runNextLine(aScene);
}

/**
 * Runs the next line.
 */
protected void runNextLine(SnapScene aScene)
{
    // If at LineIndexMax, return
    if(_lineIndex>=_lineIndexMax) return;
    System.out.println("Run Line " + _lineIndex + " of " + _lineIndexMax);

    // Get Line and run    
    ScriptLine sline = getLines().get(_lineIndex);
    int runTime = sline.run(aScene);
    _lineIndex++; _time += runTime;
}

}