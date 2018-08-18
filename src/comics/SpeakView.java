package comics;
import snap.gfx.*;
import snap.util.MathUtils;
import snap.view.*;

/**
 * A view subclass to show speech/though balloons.
 */
public class SpeakView extends ParentView {

    // The textarea
    TextArea    _textArea;
    
    // The tail angle
    double      _tailAngle = 105;
    
    // The tail length from center
    double      _tailLen = 80;
    
    // The text shape
    Shape       _textShape;
    
    // The background shape
    Shape       _backShape;
    
/**
 * Creates a SpeakView.
 */
public SpeakView()
{
    // Create/configure/add TextArea
    _textArea = new TextArea(); _textArea.setFont(Font.Arial10.deriveFont(24)); _textArea.setAlign(Pos.CENTER);
    _textArea.setWrapText(true); _textArea.setPadding(5,5,5,5);
    addChild(_textArea);
    
    // Configure
    setFill(Color.WHITE);
    setEffect(new ShadowEffect(6,Color.BLACK,0,0));
}

/**
 * Returns the text.
 */
public String getText()  { return _textArea.getText(); }

/**
 * Sets the text.
 */
public void setText(String aStr)
{
    _textArea.setText(aStr);
    if(getWidth()>10) _textArea.scaleTextToFit();
}

/**
 * Returns the rich text.
 */
public RichText getRichText()  { return getTextBox().getRichText(); }

/**
 * Returns the text area.
 */
public TextArea getTextArea()  { return _textArea; }

/**
 * Returns the text box.
 */
public TextBox getTextBox()  { return _textArea.getTextBox(); }

/**
 * Returns the tail angle.
 */
public double getTailAngle()  { return _tailAngle; }

/**
 * Sets the tail angle.
 */
public void setTailAngle(double aValue)  { _tailAngle = aValue; }

/**
 * Sets the tail angle by point in parent coords.
 */
public void setTailAngleByPoint(double aX, double aY)
{
    Rect r = getBoundsParent();
    double dx = aX - r.getMidX(), dy = aY - r.getMidY();
    double angle = dx!=0? Math.toDegrees(Math.atan(dy/dx)) : 0; angle = MathUtils.mod(angle,90);
    if(dx<0) angle += 90; if(dy<0) angle += 180;
    setTailAngle(angle);
}

/**
 * Returns the tail point for given length in local coords.
 */
protected Point getTailPoint(double aLen)
{
    double w = getWidth(), h = getHeight();
    double tpx = w/2 + MathUtils.cos(_tailAngle)*aLen; tpx = Math.round(tpx);
    double tpy = h/2 + MathUtils.sin(_tailAngle)*aLen; tpy = Math.round(tpy);
    return new Point(tpx,tpy);
}

/**
 * Actual method to layout children.
 */
protected void layoutImpl()
{
    _textArea.setSize(getWidth(), getHeight());
    _textArea.scaleTextToFit();
}

/**
 * Override to extend paint to cover tail.
 */
public void repaint()
{
    Point tp = getTailPoint(_tailLen+2);
    Rect r = getBoundsLocal(); r.add(tp.x,tp.y);
    repaint(r.x,r.y,r.width,r.height);
}

/**
 * Override to paint.
 */
protected void paintBack(Painter aPntr)
{
    // Create text shape and tail shape
    double w = getWidth(), h = getHeight();
    _textShape = new Ellipse(0,0,w,h);
    double p0x = w/2-10, p0y = h/2, p1x = w/2+10, p1y = h/2;
    Point tp = getTailPoint(_tailLen);
    Shape tailShape = new Polygon(p0x,p0y,p1x,p1y,tp.x,tp.y);
    Shape all = Shape.add(_textShape, tailShape);
    
    aPntr.setPaint(getFill()); aPntr.fill(all);
    aPntr.setColor(Color.GRAY); aPntr.setStroke(Stroke.Stroke1); aPntr.draw(all);
}

}