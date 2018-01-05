package comics;
import java.util.ArrayList;
import java.util.List;
import snap.view.ViewEnv;
import snap.viewx.*;

/**
 * A class to represent the instructions.
 */
public class Script {
    
    // The View text
    String             _text = "Setting is OvalOffice\n";

    // The Script lines
    List <ScriptLine>  _lines;
    
    // The current line index
    int                _lineIndex;
    
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
public void run(SnapScene aScene)
{
    _time = 0; _lineIndex = 0;
    runNextLine(aScene);
}

/**
 * Runs the next line.
 */
protected void runNextLine(SnapScene aScene)
{
    System.out.println("Run Line " + _lineIndex + " at " + _time);
    if(_lineIndex>=getLines().size()) return;
    
    ScriptLine sline = getLines().get(_lineIndex);
    int runTime = sline.run(aScene);
    _lineIndex++; _time += runTime;
    ViewEnv.getEnv().runDelayed(() -> runNextLine(aScene), runTime, true);
}

}