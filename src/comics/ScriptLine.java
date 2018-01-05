package comics;
import snap.gfx.*;
import snap.view.*;
import snap.viewx.*;
import snap.util.*;
import snap.web.*;

/**
 * A class to represent a line of script.
 */
public class ScriptLine {
    
    // The Script
    Script     _script;
    
    // The line text
    String     _text;
    
    // The words
    String     _words[];
    
    // The word index
    int        _index;
    
    // The Stage
    SnapScene  _stage;
    
    // The start time
    int        _startTime;
    
    // The run time
    int        _runTime;
    
/**
 * Creates a new ScriptLine with given text.
 */
public ScriptLine(Script aScript, String aStr)
{
    _script = aScript; _text = aStr.toLowerCase();
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
    if(_words!=null) return _words;
    return _words = getText().split("\\s");
}

/**
 * Executes line for Stage.
 */
public int run(SnapScene aStage)
{
    _stage = aStage;
    _startTime = 0;//_script._time;
    
    String words[] = getWords();
    for(_index=0;_index<words.length;_index++) { String word = words[_index].toLowerCase();
    
        if(word.equals("setting"))
            runSetting();
        if(word.equals("walks"))
            runWalks();
        if(word.equals("says"))
            runSays();
        if(word.equals("explodes"))
            runExplodes();
    }
    
    return _runTime;
}

/**
 * Runs the setting.
 */
public void runSetting()
{
    ImageView iview = getNextImageView(); if(iview==null) return;
    iview.setSize(_stage.getWidth(), _stage.getHeight());
    _stage.addChild(iview);
}

/**
 * Runs a walk command.
 */
public void runWalks()
{
    _index = -1;
    ImageView iview = getNextImageView(); if(iview==null) return;
    
    if(ArrayUtils.contains(_words, "right")) {
        iview.setBounds(_stage.getWidth(),200,80,240);
        _stage.addChild(iview);
        iview.getAnim(_startTime).getAnim(_startTime+2000).setX(_stage.getWidth()/2+60).play();
    }
    
    else if(ArrayUtils.contains(_words, "out")) {
        iview = (ImageView)getView(); if(iview==null) return;
        iview.getAnim(_startTime).getAnim(_startTime+2000).setX(_stage.getWidth()).play();
    }
    
    else {
        iview.setBounds(-60,200,80,240);
        _stage.addChild(iview);
        iview.getAnim(_startTime).getAnim(_startTime+2000).setX(_stage.getWidth()/2-120).play();
    }
    
    _index = _words.length;
    _runTime = 2000;
}

/**
 * Runs a walk command.
 */
public void runSays()
{
    int index = _text.indexOf("says,");
    if(index<0) index = _text.indexOf("says"); if(index<0) return;

    String str = _text.substring(index+4).trim();

    TextArea text = new TextArea(); text.setFont(Font.Arial10.deriveFont(24));
    text.setFill(Color.WHITE);
    text.setAlign(Pos.CENTER);
    text.setBorder(Border.createLineBorder(Color.BLACK,2));
    text.setText(str);
    text.setBounds(_stage.getWidth()/2-150, 100, 300,60);
    text.scaleTextToFit();
    text.setOpacity(0);
    _stage.addChild(text);
    text.getAnim(_startTime).getAnim(_startTime+500).setOpacity(1).getAnim(_startTime+500+2000)
        .getAnim(_startTime+500+2000+500).setOpacity(0).play();
    _runTime = 3000;
}

/**
 * Runs a walk command.
 */
public void runExplodes()
{
    View child = getView(); if(child==null) return;
    
    Explode.explode(child, _startTime);
    _index = _words.length;
    _runTime = 2500;
}

/**
 * Returns the next image.
 */
public View getView()
{
    int ind = _index; _index = -1;
    Image img = getNextImage();
    String name = img!=null? img.getName() : null;
    View child = name!=null? _stage.getChild(name) : null;
    _index = ind;
    if(child!=null) child.getAnimCleared(0);
    return child;
}

/**
 * Returns the next image.
 */
public Image getNextImage()
{
    for(int i=_index+1;i<_words.length;i++) { String word = _words[i].toLowerCase();
        WebURL url = WebURL.getURL("/Temp/aday/" + word + ".jpg");
        WebFile file = url.getFile();
        if(file!=null)
            return Image.get(file);
    }
    _index = _words.length;
    return null;
}

/**
 * Returns the next image.
 */
public ImageView getNextImageView()
{
    Image img = getNextImage(); if(img==null) return null;
    ImageView iview = new ImageView(img); iview.setName(img.getName());
    return iview;
}

}