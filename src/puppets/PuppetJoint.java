package puppets;
import snap.geom.Rect;
import snap.gfx.*;
import snap.util.*;

/**
 * A class to represent a location in a puppet that binds parts together or marks a location.
 */
public class PuppetJoint {

    // The name of the part
    private String _name;

    // The location of the part
    protected double _x, _y;

    // The image
    private Image _image;

    // The Puppet that owns this joint
    protected Puppet _puppet;

    /**
     * Constructor.
     */
    public PuppetJoint()
    {
    }

    /**
     * Constructor.
     */
    public PuppetJoint(String aName, double aX, double aY)
    {
        setName(aName);
        _x = aX;
        _y = aY;
    }

    /**
     * Returns the name.
     */
    public String getName()  { return _name; }

    /**
     * Sets the name.
     */
    public void setName(String aName)
    {
        _name = aName;
    }

    /**
     * Returns the puppet X.
     */
    public double getX()  { return _x; }

    /**
     * Returns the puppet Y.
     */
    public double getY()  { return _y; }

    /**
     * Returns the image.
     */
    public Image getImage()
    {
        if (_image != null) return _image;
        return _image = getImageImpl();
    }

    /**
     * Sets the image.
     */
    public void setImage(Image anImage)
    {
        _image = anImage;
    }

    /**
     * Returns the image.
     */
    protected Image getImageImpl()
    {
        if (_name == PuppetSchema.Anchor_Joint)
            return PuppetUtils.getAnchorImage();
        return PuppetUtils.getMarkerImage();
    }

    /**
     * Returns whether joint is really just a marker (associated with only one part).
     */
    public boolean isMarker()
    {
        return getPuppet().getSchema().isMarkerName(getName());
    }

    /**
     * Returns the bounds.
     */
    public Rect getBounds()
    {
        double w = getImage().getWidth() * 500 / 977d;
        double h = getImage().getHeight() * 500 / 977d;
        double x = getX() - w / 2;
        double y = getY() - h / 2;
        return new Rect(x, y, w, h);
    }

    /**
     * Returns the puppet that owns this joint.
     */
    public Puppet getPuppet()
    {
        return _puppet;
    }

    /**
     * Returns the next joint.
     */
    public PuppetJoint getNext()
    {
        String nextName = getPuppet().getSchema().getNextJointNameForName(getName());
        PuppetJoint next = nextName != null ? getPuppet().getJointForName(nextName) : null;
        return next;
    }

    /**
     * XML Archival.
     */
    public XMLElement toXML(XMLArchiver anArchiver)
    {
        // Get new element with part name
        XMLElement e = new XMLElement("Part");
        e.add("Name", getName());

        // Write bounds
        e.add("X", StringUtils.formatNum("#.##", getX()));
        e.add("Y", StringUtils.formatNum("#.##", getY()));

        // Return element
        return e;
    }

    /**
     * XML unarchival.
     */
    public PuppetJoint fromXML(XMLElement anElement)
    {
        // Unarchive name
        String name = anElement.getAttributeValue("Name");
        setName(name);

        // Unarchive bounds
        _x = anElement.getAttributeDoubleValue("X");
        _y = anElement.getAttributeDoubleValue("Y");

        // Unarchive Image
        _image = PuppetUtils.getMarkerImage();
        if (name.equals(PuppetSchema.Anchor_Joint)) _image = PuppetUtils.getAnchorImage();

        // Return this
        return this;
    }

    /**
     * Standard toString implementation.
     */
    public String toString()
    {
        return "Joint: name=" + _name + ", x=" + _x + ", y=" + _y;
    }
}