package comics.gfx;

import snap.geom.*;
import snap.gfx.*;
import snap.text.TextBox;
import snap.util.MathUtils;
import snap.view.*;

/**
 * A view subclass to show speech/though balloons.
 */
public class SpeakView extends ParentView {

    // The textarea
    TextArea _textArea;

    // The tail angle
    double _tailAngle = 105;

    // The tail length from center
    double _tailLen = 90;

    // The bubble bounds in local coords
    Rect _bubbleBnds;

    // The text shape
    Shape _textShape;

    // The background shape
    Shape _backShape;

    /**
     * Creates a SpeakView.
     */
    public SpeakView()
    {
        // Create/configure/add TextArea
        _textArea = new TextArea();
        _textArea.setFont(Font.Arial10.copyForSize(24));
        _textArea.setAlign(Pos.CENTER);
        _textArea.setWrapLines(true);
        _textArea.setPadding(5, 5, 5, 5);
        addChild(_textArea);

        // Configure
        setFill(Color.WHITE);
        setBorder(Color.CLEAR, 1);
        setEffect(new ShadowEffect(6, Color.BLACK, 0, 0));
    }

    /**
     * Returns the text.
     */
    public String getText()
    {
        return _textArea.getText();
    }

    /**
     * Sets the text.
     */
    public void setText(String aStr)
    {
        _textArea.setText(aStr);
        if (getWidth() > 10) _textArea.scaleTextToFit();
    }

    /**
     * Returns the text area.
     */
    public TextArea getTextArea()
    {
        return _textArea;
    }

    /**
     * Returns the bubble bounds.
     */
    public Rect getBubbleBounds()
    {
        return localToParent(_bubbleBnds).getBounds();
    }

    /**
     * Sets the bubble bounds from given rect in parent coords.
     */
    public void setBubbleBounds(Rect aRect)
    {
        Rect bnds = getRectWithPointFromCenter(aRect, getTailAngle(), getTailLength());
        setBounds(bnds);
        _bubbleBnds = parentToLocal(aRect).getBounds();
    }

    /**
     * Sets the bubble bounds from given rect in parent coords.
     */
    public void setBubbleBounds(double aX, double aY, double aW, double aH)
    {
        setBubbleBounds(new Rect(aX, aY, aW, aH));
    }

    /**
     * Returns the tail angle.
     */
    public double getTailAngle()
    {
        return _tailAngle;
    }

    /**
     * Sets the tail angle.
     */
    public void setTailAngle(double aValue)
    {
        Rect bbnds = getBubbleBounds();
        _tailAngle = aValue;
        setBubbleBounds(bbnds);
    }

    /**
     * Returns the tail length.
     */
    public double getTailLength()
    {
        return _tailLen;
    }

    /**
     * Sets the tail length.
     */
    public void setTailLength(double aValue)
    {
        Rect bbnds = getBubbleBounds();
        _tailLen = aValue;
        setBubbleBounds(bbnds);
    }

    /**
     * Sets the tail angle by point in parent coords.
     */
    public void setTailAngleByPoint(double aX, double aY)
    {
        Rect r = getBubbleBounds();
        double dx = aX - r.getMidX(), dy = aY - r.getMidY();
        double angle = dx != 0 ? Math.toDegrees(Math.atan(dy / dx)) : 0;
        angle = MathUtils.mod(angle, 90);
        if (dx < 0) angle += 90;
        if (dy < 0) angle += 180;
        setTailAngle(angle);
    }

    /**
     * Actual method to layout children.
     */
    protected void layoutImpl()
    {
        // Reset text bounds, bounds path and scale text
        _textArea.setBounds(_bubbleBnds);

        // Reset text bounds path and scale text
        double x = _bubbleBnds.x;
        double y = _bubbleBnds.y;
        double w = _bubbleBnds.width;
        double h = _bubbleBnds.height;
        TextBox textBox = (TextBox) _textArea.getTextBlock();
        textBox.setBoundsPath(new Ellipse(0, 0, w, h));
        _textArea.scaleTextToFit();

        // Reset text shape
        _textShape = new Ellipse(x, y, w, h);

        // Reset background shape
        double p0x = x + w / 2 - 10, p0y = y + h / 2, p1x = x + w / 2 + 10, p1y = y + h / 2;
        Point tp = getTailPoint(_bubbleBnds, _tailAngle, _tailLen);
        Shape tailShape = new Polygon(p0x, p0y, p1x, p1y, tp.x, tp.y);
        _backShape = Shape.addShapes(_textShape, tailShape);
    }

    /**
     * Override to paint.
     */
    protected void paintBack(Painter aPntr)
    {
        aPntr.setPaint(getFill());
        aPntr.fill(_backShape);
        aPntr.setColor(Color.GRAY);
        aPntr.setStroke(Stroke.Stroke1);
        aPntr.draw(_backShape);
    }

    /**
     * Override to return bounds shape as bubble.
     */
    public Shape getBoundsShape()
    {
        return _backShape != null ? _backShape : super.getBoundsShape();
    }

    /**
     * Returns the tail point for given length.
     */
    protected static Point getTailPoint(Rect aRect, double anAngle, double aLen)
    {
        double x = aRect.x, y = aRect.y, w = aRect.width, h = aRect.height;
        double tpx = x + w / 2 + MathUtils.cos(anAngle) * aLen;
        tpx = Math.round(tpx);
        double tpy = y + h / 2 + MathUtils.sin(anAngle) * aLen;
        tpy = Math.round(tpy);
        return new Point(tpx, tpy);
    }

    /**
     * Returns the expanded rect for given rect with point at angle and length from center of rect.
     */
    private static Rect getRectWithPointFromCenter(Rect aRect, double anAngle, double aLen)
    {
        Point tp = getTailPoint(aRect, anAngle, aLen);
        Rect rect = aRect.clone();
        rect.add(tp.x, tp.y);
        return rect;
    }

}