package comics.player;

/**
 * A class to represent a sentence fragment to modifies an action.
 */
public class Predicate {

    // The text
    String _text;

    /**
     * Creates a Predicate.
     */
    public Predicate(String aStr)
    {
        setText(aStr);
    }

    /**
     * Returns the text.
     */
    public String getText()
    {
        return _text;
    }

    /**
     * Sets the text.
     */
    protected void setText(String aStr)
    {
        _text = aStr;
    }

}