package puppets.puppet;

import snap.geom.Rect;
import snap.gfx.*;
import snap.util.*;
import snap.web.WebURL;

import java.util.Objects;

/**
 * A class to represent a part of a puppet.
 */
public class PuppetPart implements Loadable {

    // The name of the part
    String _name;

    // The location of the part
    double _x, _y, _w, _h;

    // The image
    Image _img;

    // The Puppet that owns this part
    Puppet _puppet;

    // The original part
    PuppetPart _origPart;

    // The mother part, if part was derived from another part
    PuppetPart _motherPart;

    /**
     * Creates a PuppetPart.
     */
    public PuppetPart()
    {
    }

    /**
     * Creates a PuppetPart.
     */
    public PuppetPart(String aName, Image anImage, double aX, double aY)
    {
        this(aName, anImage, aX, aY, 0, 0);
    }

    /**
     * Creates a PuppetPart.
     */
    public PuppetPart(String aName, Image anImage, double aX, double aY, double aW, double aH)
    {
        setName(aName);
        _x = aX;
        _y = aY;
        _w = aW;
        _h = aH;
        setImage(anImage);
    }

    /**
     * Returns the name.
     */
    public String getName()
    {
        return _name;
    }

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
    public double getX()
    {
        return _x;
    }

    /**
     * Returns the puppet Y.
     */
    public double getY()
    {
        return _y;
    }

    /**
     * Returns the puppet width.
     */
    public double getWidth()
    {
        return _w;
    }

    /**
     * Returns the puppet height.
     */
    public double getHeight()
    {
        return _h;
    }

    /**
     * Returns the puppet max X.
     */
    public double getMaxX()
    {
        return _x + _w;
    }

    /**
     * Returns the puppet max Y.
     */
    public double getMaxY()
    {
        return _y + _h;
    }

    /**
     * Returns the bounds.
     */
    public Rect getBounds()
    {
        return new Rect(_x, _y, _w, _h);
    }

    /**
     * Returns the scale.
     */
    public double getScale()
    {
        return _origPart != null ? getBounds().width / _origPart.getBounds().width : 1;
    }

    /**
     * Returns the image.
     */
    public Image getImage()
    {
        return _img != null ? _img : (_img = getImageImpl());
    }

    /**
     * Sets the image.
     */
    public void setImage(Image anImage)
    {
        _img = anImage;

        // Set size - probably don't need this
        if (_w == 0) {
            if (anImage.isLoaded()) {
                _w = anImage.getWidth();
                _h = anImage.getHeight();
            } else anImage.addLoadListener(() -> {
                _w = anImage.getWidth();
                _h = anImage.getHeight();
            });
        }
    }

    /**
     * Returns the image.
     */
    protected Image getImageImpl()
    {
        WebURL url = getImageURL();
        Image img = url != null ? Image.getImageForSource(url) : null;
        if (img == null) System.out.println("PuppetPart.getImage: Image not found for " + getName() + " at " + url);
        return img;
    }

    /**
     * Returns the image name.
     */
    public String getImageName()
    {
        String name = getName();
        String type = getImage().getType().toLowerCase();
        return name + '.' + type;
    }

    /**
     * Returns the image URL.
     */
    protected WebURL getImageURL()
    {
        // Get puppet dir
        Puppet pup = _puppet;
        if (pup == null) return null;
        WebURL dirURL = pup.getSourceDirURL();
        String iname = getName() + ".png";
        WebURL url = dirURL.getChildUrlForPath(iname);
        return url;
    }

    /**
     * Saves the image.
     */
    public void saveImage()
    {
        Image img = getImage();
        WebURL url = getImageURL();
        byte ibytes[] = img.getBytesPNG();
        byte ibytesOld[] = url.getBytes();
        if (!Objects.equals(ibytes, ibytesOld))
            SnapUtils.writeBytes(ibytes, url.getJavaFile());
    }

    /**
     * Returns the puppet that owns this part.
     */
    public Puppet getPuppet()
    {
        return _puppet;
    }

    /**
     * Returns the mother part, if this part was derived from another.
     */
    public PuppetPart getMotherPart()
    {
        return _motherPart;
    }

    /**
     * Returns whether resource is loaded.
     */
    public boolean isLoaded()
    {
        return getLoadable().isLoaded();
    }

    /**
     * Adds a callback to be triggered when resources loaded (cleared automatically when loaded).
     */
    public void addLoadListener(Runnable aRun)
    {
        getLoadable().addLoadListener(aRun);
    }

    /**
     * Returns the default loadable (the image).
     */
    protected Loadable getLoadable()
    {
        return getImage();
    }

    /**
     * Creates a clone with given image.
     */
    public PuppetPart cloneForImage(Image anImage)
    {
        PuppetPart clone = new PuppetPart(getName(), anImage, getX(), getY(), getWidth(), getHeight());
        return clone;
    }

    /**
     * Creates a clone with given scale.
     */
    public PuppetPart cloneForScale(double aScale)
    {
        // Calc new bounds for scale
        PuppetPart part1 = _origPart != null ? _origPart : this;
        Image img = part1.getImage();
        double w2 = Math.round(part1.getWidth() * aScale);
        double h2 = Math.round(part1.getHeight() * aScale);
        double x2 = Math.round(part1.getX() + part1.getWidth() / 2 - w2 / 2);
        double y2 = Math.round(part1.getY() + part1.getHeight() / 2 - h2 / 2);

        // Create clone for new bounds and return
        PuppetPart clone = new PuppetPart(getName(), img, x2, y2, w2, h2);
        clone._origPart = _origPart != null ? _origPart : this;
        return clone;
    }

    /**
     * Tries to create a missing part from an existing/composite part.
     */
    static PuppetPart createDerivedPart(Puppet aPuppet, String aName)
    {
        if (aName == PuppetSchema.RArmTop || aName == PuppetSchema.RArmBtm)
            return PuppetPart.splitPartAroundJoint(aPuppet, PuppetSchema.RArm, PuppetSchema.RArmMid_Joint, aName);
        if (aName == PuppetSchema.RLegTop || aName == PuppetSchema.RLegBtm)
            return PuppetPart.splitPartAroundJoint(aPuppet, PuppetSchema.RLeg, PuppetSchema.RLegMid_Joint, aName);
        if (aName == PuppetSchema.LArmTop || aName == PuppetSchema.LArmBtm)
            return PuppetPart.splitPartAroundJoint(aPuppet, PuppetSchema.LArm, PuppetSchema.LArmMid_Joint, aName);
        if (aName == PuppetSchema.LLegTop || aName == PuppetSchema.LLegBtm)
            return PuppetPart.splitPartAroundJoint(aPuppet, PuppetSchema.LLeg, PuppetSchema.LLegMid_Joint, aName);
        return null;
    }

    /**
     * Splits a part around joint - for when given arm/leg as one piece instead of top/bottom.
     */
    static PuppetPart splitPartAroundJoint(Puppet aPuppet, String aPartName, String aJointName, String aName2)
    {
        // Get part to split
        boolean isTop = aName2.contains("Top");
        PuppetPart part = aPuppet.getPart(aPartName);
        if (part == null) {
            System.err.println("PuppetPart.splitPart: Part not found " + aPartName);
            return null;
        }

        // Get joint to split around
        PuppetJoint joint = aPuppet.getJoint(aJointName);
        if (joint == null) {
            System.err.println("PuppetPart.splitView: Joint not found " + aJointName);
            return null;
        }

        // Get bounds of new part
        Rect pbnds = getSplitBoundsForView(part, joint, isTop);

        // Get bounds in image
        Image img = part.getImage();
        double iw = img.getWidth();
        double ih = img.getHeight();
        double isx = iw / part.getWidth();
        double isy = ih / part.getHeight();

        // Get bounds of new part image in old image
        double pix = (pbnds.x - part.getX()) * isx;
        double piy = ih - (pbnds.getMaxY() - part.getY()) * isy;
        double piw = pbnds.width * isx;
        double pih = pbnds.height * isy;
        Image img1 = img.cloneForCropRect(pix, piy, piw, pih);

        // Create/add new parts
        PuppetPart np = new PuppetPart(aName2, img1, pbnds.x, pbnds.y, pbnds.width, pbnds.height);
        np._puppet = part.getPuppet();
        np._motherPart = part;
        return np;
    }

    /**
     * Returns the partial rect when splitting an arm/leg joint in two around joint for above method.
     */
    static Rect getSplitBoundsForView(PuppetPart aPart, PuppetJoint aJoint, boolean doTop)
    {
        // Get part and joint bounds
        Rect pbnds = aPart.getBounds(), jbnds = aJoint.getBounds();
        double x = pbnds.x, y = pbnds.y, w = pbnds.width, h = pbnds.height, asp = w / h;

        // Handle horizontal arm/leg
        if (asp < .3333) {
            if (doTop) {
                y = jbnds.y;
                h = pbnds.getMaxY() - y;
            } else h = jbnds.getMaxY() - y;
        }

        // Handle diagonal arm/leg
        else if (asp < 3) {

            // Handle Right arm/leg
            if (aPart.getName().startsWith("R")) {
                if (doTop) {
                    x = jbnds.x;
                    y = jbnds.y;
                    w = pbnds.getMaxX() - x;
                    h = pbnds.getMaxY() - y;
                } else {
                    w = jbnds.getMaxX() - x;
                    h = jbnds.getMaxY() - y;
                }
            }

            // Handle Left arm/leg
            else {
                if (doTop) {
                    y = jbnds.y;
                    w = jbnds.getMaxX() - x;
                    h = pbnds.getMaxY() - y;
                } else {
                    x = jbnds.x;
                    w = pbnds.getMaxX() - x;
                    h = jbnds.getMaxY() - y;
                }
            }
        }

        // Handle vertial arm/leg
        else {
            if (doTop) w = jbnds.getMaxX() - x;
            else {
                x = jbnds.x;
                w = pbnds.getMaxX() - x;
            }
        }

        // Return rect
        return new Rect(x, y, w, h);
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
        e.add("X", StringUtils.formatNum("#.##", _x));
        e.add("Y", StringUtils.formatNum("#.##", _y));
        e.add("Width", StringUtils.formatNum("#.##", _w));
        e.add("Height", StringUtils.formatNum("#.##", _h));

        // Write ImageName
        String iname = getImageName();
        e.add("Image", iname);

        // Return element
        return e;
    }

    /**
     * XML unarchival.
     */
    public PuppetPart fromXML(XMLElement anElement)
    {
        // Unarchive name
        String name = anElement.getAttributeValue("Name");
        setName(name);

        // Unarchive bounds
        _x = anElement.getAttributeDoubleValue("X");
        _y = anElement.getAttributeDoubleValue("Y");
        _w = anElement.getAttributeDoubleValue("Width");
        _h = anElement.getAttributeDoubleValue("Height");

        // Unarchive Image
        String iname = anElement.getAttributeValue("Image");

        // Return this
        return this;
    }

    /**
     * Standard toString implementation.
     */
    public String toString()
    {
        return "Part: name=" + _name + ", bounds=" + getBounds();
    }

}