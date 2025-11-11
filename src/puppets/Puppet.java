package puppets;
import java.nio.file.Files;
import java.util.*;
import snap.geom.Point;
import snap.geom.Rect;
import snap.util.*;
import snap.web.WebURL;

/**
 * A class to hold information providing image parts of a graphic of a human.
 */
public class Puppet {

    // The puppet name
    private String _name;

    // The source of puppet
    private Object _source;

    // The source as URL
    private WebURL _sourceUrl;

    // The description of puppet parts and joints
    private PuppetSchema _schema = new PuppetSchema();

    // Cached parts
    private Map<String, PuppetPart> _parts = new HashMap<>();

    // Cached joints
    private Map<String, PuppetJoint> _joints = new HashMap<>();

    // The puppet that this puppet is based on
    private Puppet _parent;

    // The bounds of parts
    private Rect _bounds;

    // The bounds of joints
    private Rect _jntBnds;

    /**
     * Constructor.
     */
    public Puppet()
    {
        super();
    }

    /**
     * Creates a Puppet for source.
     */
    public Puppet(Object aSource)
    {
        setSource(aSource);
    }

    /**
     * Creates a Puppet for master.
     */
    public Puppet(Puppet aPuppet)
    {
        _parent = aPuppet;
    }

    /**
     * Returns the puppet name.
     */
    public String getName()
    {
        return _name;
    }

    /**
     * Sets the puppet name.
     */
    public void setName(String aName)
    {
        _name = aName;
    }

    /**
     * Returns the source.
     */
    public Object getSource()
    {
        return _source;
    }

    /**
     * Sets the source.
     */
    public void setSource(Object aSource)
    {
        _source = aSource;
    }

    /**
     * Returns the source as URL.
     */
    public WebURL getSourceURL()
    {
        // If already set, just return
        if (_sourceUrl != null) return _sourceUrl;

        // Get source - if null and name exists, create default
        Object src = getSource();
        if (src == null && getName() != null)
            src = PuppetUtils.ROOT + "chars/" + getName() + '/' + getName() + ".pup";

        // Create SourceURL from source and return
        _sourceUrl = WebURL.getUrl(src);
        return _sourceUrl;
    }

    /**
     * Returns the sub-path of the source relative to ROOT.
     */
    public String getSourceRelPath()
    {
        WebURL url = getSourceURL();
        if (url == null) return null;
        String path = url.getPath();
        if (path.startsWith(PuppetUtils.ROOT))
            path = path.substring(PuppetUtils.ROOT.length());
        return path;
    }

    /**
     * Sets the puppet path.
     */
    public void setSourceRelPath(String aPath)
    {
        String path = aPath;
        if (!path.startsWith(PuppetUtils.ROOT)) path = PuppetUtils.ROOT + path;
        setSource(path);
    }

    /**
     * Returns the URL of the directory.
     */
    public WebURL getSourceDirURL()
    {
        WebURL url = getSourceURL();
        if (url == null) return null;
        WebURL parURL = url.getParent();
        return parURL;
    }

    /**
     * Returns the URL path of the directory.
     */
    public String getSourceDirPath()
    {
        WebURL url = getSourceDirURL();
        String path = url != null ? url.getPath() : null;
        return path;
    }

    /**
     * Returns the schema.
     */
    public PuppetSchema getSchema()  { return _schema; }

    /**
     * Returns the parent.
     */
    public Puppet getParent()  { return _parent; }

    /**
     * Returns the part for given name.
     */
    public PuppetPart getPartForName(String aName)
    {
        // Get cached part (just return if found)
        PuppetPart part = _parts.get(aName);
        if (part != null)
            return part;

        // Try to create part
        part = createPart(aName);
        if (part == null)
            part = createDerivedPart(aName);
        if (part == null && _parent != null)
            part = _parent.getPartForName(aName);
        if (part == null) {
            System.out.println("Puppet.getPart: part not found " + getName() + ' ' + aName);
            return null;
        }

        // Add part to cache and return
        setPart(part);
        return part;
    }

    /**
     * Returns the part for given name.
     */
    protected PuppetPart createPart(String aName)
    {
        return null;
    }

    /**
     * Tries to create a missing part from an existing/composite part.
     */
    protected PuppetPart createDerivedPart(String aName)
    {
        return PuppetPart.createDerivedPart(this, aName);
    }

    /**
     * Sets a part.
     */
    public void setPart(PuppetPart aPart)
    {
        _parts.put(aPart.getName(), aPart);
        if (aPart._puppet == null) aPart._puppet = this;
    }

    /**
     * Returns the joint for given name.
     */
    public PuppetJoint getJointForName(String aName)
    {
        // Get cached joint (just return if found)
        PuppetJoint joint = _joints.get(aName);
        if (joint != null) return joint;

        // Try to create joint
        joint = createJoint(aName);
        if (joint == null && _parent != null)
            joint = _parent.getJointForName(aName);
        if (joint == null) {
            System.out.println("Puppet.getJoint: part not found " + aName);
            return null;
        }

        // Add joint to cache and return
        setJoint(joint);
        return joint;
    }

    /**
     * Returns the joint for given name.
     */
    protected PuppetJoint createJoint(String aName)
    {
        return null;
    }

    /**
     * Sets a joint.
     */
    public void setJoint(PuppetJoint aJoint)
    {
        _joints.put(aJoint.getName(), aJoint);
        if (aJoint._puppet == null) aJoint._puppet = this;
    }

    /**
     * Returns the parts in natural order.
     */
    public PuppetPart[] getParts()
    {
        return getPartsForNames(getSchema().getPartNamesNaturalOrder());
    }

    /**
     * Returns the parts in paint order.
     */
    public PuppetPart[] getPartsPaintOrder()
    {
        return getPartsForNames(getSchema().getPartNames());
    }

    /**
     * Returns the parts for given names.
     */
    public PuppetPart[] getPartsForNames(String[] theNames)
    {
        return ArrayUtils.map(theNames, this::getPartForName, PuppetPart.class);
    }

    /**
     * Returns the joints in natural order.
     */
    public PuppetJoint[] getJoints()
    {
        return getJointsForNames(getSchema().getJointNamesNaturalOrder());
    }

    /**
     * Returns the joints in paint order.
     */
    public PuppetJoint[] getJointsPaintOrder()
    {
        return getJointsForNames(getSchema().getJointNames());
    }

    /**
     * Returns the joints for given names.
     */
    public PuppetJoint[] getJointsForNames(String[] theNames)
    {
        return ArrayUtils.map(theNames, this::getJointForName, PuppetJoint.class);
    }

    /**
     * Returns the mother parts.
     */
    public PuppetPart[] getMotherParts()
    {
        // Iterate over parts and replace parts with their mother (if found)
        PuppetPart[] parts = getParts();
        List<PuppetPart> motherParts = new ArrayList<>(parts.length);
        for (PuppetPart part : parts) {
            if (part.getMotherPart() != null)
                part = part.getMotherPart();
            if (!motherParts.contains(part))
                motherParts.add(part);
        }

        // Return MotherParts as array
        return motherParts.toArray(new PuppetPart[0]);
    }

    /**
     * Returns the bounds.
     */
    public Rect getBounds()
    {
        // If already set, just return
        if (_bounds != null) return _bounds;

        // If not loaded, just return bounds for current loaded parts
        if (!isLoaded()) {
            PuppetPart[] parts = _parts.values().toArray(new PuppetPart[0]);
            Rect bnds = getBoundsForParts(parts);
            if (getParent() != null) {
                Rect pbnds = getParent().getBounds();
                bnds.union(pbnds);
            }
            return bnds;
        }

        // Return rect
        PuppetPart[] parts = getParts();
        return _bounds = getBoundsForParts(parts);
    }

    /**
     * Returns the bounds for given parts.
     */
    protected Rect getBoundsForParts(PuppetPart[] theParts)
    {
        // Iterate over parts and expand bounds
        double x = Float.MAX_VALUE;
        double y = Float.MAX_VALUE;
        double mx = -Float.MAX_VALUE, my = mx;
        for (PuppetPart part : theParts) {
            x = Math.min(x, part.getX());
            y = Math.min(y, part.getY());
            mx = Math.max(mx, part.getMaxX());
            my = Math.max(my, part.getMaxY());
        }

        // Return rect
        return new Rect(x, y, mx - x, my - y);
    }

    /**
     * Returns the joint bounds.
     */
    public Rect getJointBounds()
    {
        // If already set, just return
        if (_jntBnds != null) return _jntBnds;

        // Iterate over parts and expand bounds
        double x = Float.MAX_VALUE;
        double y = Float.MAX_VALUE;
        double mx = -Float.MAX_VALUE, my = mx;
        for (PuppetJoint jnt : getJoints()) {
            double jx = jnt.getX(), jy = jnt.getY();
            x = Math.min(x, jx);
            y = Math.min(y, jy);
            mx = Math.max(mx, jx);
            my = Math.max(my, jy);
        }

        // Return rect
        return _jntBnds = new Rect(x, y, mx - x, my - y);
    }

    /**
     * Returns a pose of puppet as defined.
     */
    public PuppetPose getPose(double aScale)
    {
        // Iterate over pose keys and add pose marker and x/y location to map
        Map<String, Point> map = new LinkedHashMap<>();
        for (String pkey : getSchema().getPoseKeys()) {
            PuppetJoint pjnt = getJointForName(pkey);
            Point pnt = new Point(pjnt.getX() * aScale, pjnt.getY() * aScale);
            map.put(pkey, pnt);
        }

        // Return map wrapped in map to get Pose { ... }
        return new PuppetPose("Untitled", map);
    }

    /**
     * Returns whether resource is loaded.
     */
    public boolean isLoaded()
    {
        Loadable ldb = getLoadable();
        return ldb == null || ldb.isLoaded();
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
        String[] names = {PuppetSchema.RArm, PuppetSchema.RHand, PuppetSchema.RLeg, PuppetSchema.RFoot, PuppetSchema.Torso,
                PuppetSchema.Head, PuppetSchema.LLeg, PuppetSchema.LFoot, PuppetSchema.LArm, PuppetSchema.LHand};
        PuppetPart[] parts = getPartsForNames(names);
        return Loadable.getAsLoadable(parts);
    }

    /**
     * Reads the puppet.
     */
    public void readSource()
    {
        // Get file string as XMLElement
        WebURL url = getSourceURL();
        if (url == null) {
            System.err.println("Puppet.readSource: No source"); return; }
        XMLElement puppetXML = XMLElement.readXmlFromUrl(url);

        // Read puppet
        fromXML(puppetXML);
    }

    /**
     * Saves the puppet.
     */
    public void save()
    {
        if (SnapEnv.isTeaVM) return;

        // Create dir
        WebURL url = getSourceURL();
        if (url == null) {
            System.err.println("Puppet.save: No source set");
            return;
        }
        String dirPath = getSourceDirPath();
        java.io.File dir = FileUtils.getDirForSource(dirPath, true);
        if (dir == null) {
            System.err.println("Puppet.save: Failed to create dir: " + dirPath);
            return;
        }

        // Create element for puppet, get as bytes and write to file
        XMLElement puppetXML = toXML(null);
        byte[] bytes = puppetXML.getBytes();
        try { Files.write(url.getJavaFile().toPath(), bytes); }
        catch (Exception e) { throw new RuntimeException(e); }

        // Write images
        PuppetPart[] motherParts = getMotherParts();
        for (PuppetPart part : motherParts) {
            if (part.getPuppet() != this) continue;
            part.saveImage();
        }
    }

    /**
     * XML Archival.
     */
    public XMLElement toXML(XMLArchiver anArchiver)
    {
        // Get new element with puppet Name, Path
        XMLElement e = new XMLElement("Puppet");
        e.add("Name", getName());
        e.add("Path", getSourceRelPath());
        e.add("Version", 1);

        // Set parent puppet name (if parent exists)
        if (getParent() != null)
            e.add("Parent", getParent().getName());

        // Create element for parts and iterate over poses and add each
        XMLElement partsXML = new XMLElement("Parts");
        e.add(partsXML);
        PuppetPart[] motherParts = getMotherParts();
        for (PuppetPart part : motherParts) {
            if (part.getPuppet() != this) continue;
            XMLElement partXML = part.toXML(anArchiver);
            partsXML.add(partXML);
        }

        // Create element for joints and iterate over joints and add each
        XMLElement jointsXML = new XMLElement("Joints");
        e.add(jointsXML);
        for (PuppetJoint joint : getJoints()) {
            if (joint.getPuppet() != this) continue;
            XMLElement jointXML = joint.toXML(anArchiver);
            jointsXML.add(jointXML);
        }

        // Return element
        return e;
    }

    /**
     * XML unarchival.
     */
    public Puppet fromXML(XMLElement anElement)
    {
        // Unarchive Name, Path
        String name = anElement.getAttributeValue("Name");
        setName(name);
        String path = anElement.getAttributeValue("Path");
        setSourceRelPath(path);

        // Set Parent puppet if parent name found
        if (anElement.hasAttribute("Parent")) {
            String pname = anElement.getAttributeValue("Parent");
            Puppet par = PuppetUtils.getPuppetFile().getPuppetForName(pname);
            _parent = par;
        }

        // Iterate over parts element and load
        XMLElement partsXML = anElement.getElement("Parts");
        for (XMLElement partXML : partsXML.getElements()) {
            PuppetPart part = new PuppetPart().fromXML(partXML);
            setPart(part);
        }

        // Iterate over joints element and load
        XMLElement jointsXML = anElement.getElement("Joints");
        for (XMLElement jointXML : jointsXML.getElements()) {
            PuppetJoint joint = new PuppetJoint().fromXML(jointXML);
            setJoint(joint);
        }

        // Convert part and joints to zero
        int version = anElement.getAttributeIntValue("Version", 0);
        if (version == 0) {
            PuppetJoint anchor = _joints.get(PuppetSchema.Anchor_Joint);
            double ancX = anchor != null ? anchor.getX() : 419;
            double ancY = anchor != null ? anchor.getY() : 1063;
            double scale = 575d / 977;

            for (PuppetPart part : _parts.values()) {
                part._x = (part._x - ancX) * scale;
                part._y = (ancY - part.getMaxY()) * scale;
                part._w *= scale;
                part._h *= scale;
            }
            for (PuppetJoint jnt : _joints.values()) {
                jnt._x = (jnt._x - ancX) * scale;
                jnt._y = (ancY - jnt._y) * scale;
            }
        }

        // Return this
        return this;
    }

    /**
     * Standard toString implementation.
     */
    public String toString()
    {
        return getClass().getSimpleName() + ": " + getName();
    }

    /**
     * Returns a puppet for given source.
     */
    public static Puppet getPuppetForSource(Object aSource)
    {
        // Handle String
        if (aSource instanceof String sourceStr) {

            // Handle old ORA puppets
            if (sourceStr.equals("Man") || sourceStr.equals("Lady")) {
                sourceStr = PuppetUtils.ROOT + "chars/CT" + sourceStr;
                return new ORAPuppet(sourceStr);
            }

            Puppet puppet = new Puppet();
            puppet.setSource(sourceStr);
            puppet.readSource();
            return puppet;
        }

        // Handle unknown source
        System.err.println("Puppet.getPuppetForSource: Unknown source " + aSource);
        return null;
    }
}