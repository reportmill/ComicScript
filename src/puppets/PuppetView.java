package puppets;
import java.util.*;
import snap.geom.*;
import snap.gfx.*;
import snap.view.*;

/**
 * A View to display a puppet.
 */
public class PuppetView extends ParentView {

    // The puppet
    private Puppet _puppet;

    // The scale
    private double _scale = .87;

    // The puppet height in points
    //double _pupHeight = 500;

    // Whether poses should set over time (instead of instantly)
    private boolean _poseSmoothly = true;

    // The Physics runner
    protected PuppetViewPhys _phys;

    // Whether to show markers
    private boolean _showMarkers = true;

    /**
     * Constructor.
     */
    public PuppetView()
    {
        setPadding(50, 50, 50, 50);
        setBorder(Color.LIGHTGRAY, 1);
    }

    /**
     * Constructor for given source.
     */
    public PuppetView(String aSource)
    {
        this();
        Puppet puppet = new ORAPuppet(aSource);
        setPuppet(puppet);
    }

    /**
     * Constructor for given puppet.
     */
    public PuppetView(Puppet aPuppet)
    {
        this();
        setPuppet(aPuppet);
    }

    /**
     * Returns the puppet.
     */
    public Puppet getPuppet()  { return _puppet; }

    /**
     * Sets the puppet.
     */
    public void setPuppet(Puppet aPuppet)
    {
        // Set puppet
        _puppet = aPuppet;

        // Rebuild children
        if (aPuppet.isLoaded()) rebuildChildren();
        else aPuppet.addLoadListener(() -> rebuildChildren());
    }

    /**
     * Returns the puppet schema.
     */
    public PuppetSchema getSchema()  { return _puppet.getSchema(); }

    /**
     * Sets the puppet height.
     */
    public void setPuppetHeight(double aHeight)
    {
        _scale = .87 / 500 * aHeight;
        rebuildChildren();
    }

    /**
     * Returns a puppet point in local view coords.
     */
    public Point puppetToLocalForXY(double aX, double aY)
    {
        Transform xfm = getPuppetToLocal();
        return xfm.transformXY(aX, aY);
    }

    /**
     * Returns a local view point in puppet coords.
     */
    public Point localToPuppetForXY(double aX, double aY)
    {
        Transform xfm = getLocalToPuppet();
        return xfm.transformXY(aX, aY);
    }

    /**
     * Returns a puppet shape in local view coords.
     */
    public Shape puppetToLocalForShape(Shape aShape)
    {
        Transform xfm = getPuppetToLocal();
        return aShape.copyFor(xfm);
    }

    /**
     * Returns a local view shape in puppet coords.
     */
    public Shape localToPuppetForShape(Shape aShape)
    {
        Transform xfm = getLocalToPuppet();
        Shape shp = aShape.copyFor(xfm);
        return shp;
    }

    /**
     * Returns the transform from puppet to local.
     */
    public Transform getLocalToPuppet()
    {
        Transform xfm = getPuppetToLocal();
        xfm.invert();
        return xfm;
    }

    /**
     * Returns the transform from puppet to local.
     */
    public Transform getPuppetToLocal()
    {
        // Get puppet bounds and fromBounds of transform
        Rect pbnds = getPuppet().getBounds();
        Rect fromBnds = new Rect(pbnds.x, pbnds.getMaxY(), pbnds.width, -pbnds.height);

        // Get bounds of puppet in view
        Insets ins = getInsetsAll();
        double pw = Math.round(pbnds.width * _scale);
        double ph = Math.round(pbnds.height * _scale);
        Rect toBnds = new Rect(ins.left, ins.top, pw, ph);

        // Create transform and return
        double sx = toBnds.width / fromBnds.width;
        double sy = toBnds.height / fromBnds.height;
        double tx = toBnds.x - fromBnds.x * sx;
        double ty = toBnds.y - fromBnds.y * sy;
        Transform xfm = new Transform(sx, 0, 0, sy, tx, ty);
        return xfm;
    }

    /**
     * Calculates the preferred width.
     */
    protected double getPrefWidthImpl(double aH)
    {
        Insets ins = getInsetsAll();
        Rect bnds = getPuppet().getBounds();
        return ins.getWidth() + Math.round(bnds.width * _scale);
    }

    /**
     * Calculates the preferred height.
     */
    protected double getPrefHeightImpl(double aW)
    {
        Insets ins = getInsetsAll();
        Rect bnds = getPuppet().getBounds();
        return ins.getHeight() + Math.round(bnds.height * _scale);
    }

    /**
     * Rebuilds children.
     */
    public void rebuildChildren()
    {
        // Reset size
        setSize(getPrefSize());

        // Remove children
        removeChildren();

        // Iterate over parts and add PartView for each
        for (PuppetPart part : _puppet.getPartsPaintOrder()) {
            PartView partView = new PartView(part);
            Rect pbnds = part.getBounds();
            Rect vbnds = puppetToLocalForShape(pbnds).getBounds();
            partView.setBounds(vbnds);
            addChild(partView);
        }

        // Iterate over joints and add joint view for each
        for (PuppetJoint joint : _puppet.getJointsPaintOrder()) {
            JointView jointView = new JointView(joint);
            double w = jointView.getWidth() * 500 / 977;
            double h = jointView.getHeight() * 500 / 977;
            Point jntPnt = puppetToLocalForXY(joint.getX(), joint.getY());
            jointView.setBounds(jntPnt.x - w / 2, jntPnt.y - h / 2, w, h);
            addChild(jointView);
            jointView.setVisible(isShowMarkers());
        }

        // Make Torso really dense
        getChildForName(PuppetSchema.Torso).getPhysics().setDensity(1000);
    }

    /**
     * Returns a Puppet pose for current puppet articulation.
     */
    public PuppetPose getPose()
    {
        // Get anchor point
        View anchorView = getChildForName(PuppetSchema.Anchor_Joint);
        Point anchor = anchorView.localToParent(anchorView.getWidth() / 2, anchorView.getHeight() / 2);

        // Iterate over pose keys and add pose marker and x/y location to poseKeyPoints map
        Map<String, Point> poseKeyPoints = new LinkedHashMap<>();
        for (String pkey : getSchema().getPoseKeys()) {
            View pview = getChildForName(pkey);
            Point pnt = pview.localToParent(pview.getWidth() / 2, pview.getHeight() / 2);
            pnt.x = pnt.x - anchor.x;
            pnt.y = anchor.y - pnt.y;
            poseKeyPoints.put(pkey, pnt);
        }

        // Return map wrapped in map to get Pose { ... }
        return new PuppetPose("Untitled", poseKeyPoints);
    }

    /**
     * Sets a Puppet pose (resolves immediately).
     */
    public void setPose(PuppetPose aPose)
    {
        // Make sure last pose is resolved
        _phys.resolveMouseJoints();

        // Get anchor point so we can make convert pose points to view
        View anchorView = getChildForName(PuppetSchema.Anchor_Joint);
        Point anchor = anchorView.localToParent(anchorView.getWidth() / 2, anchorView.getHeight() / 2);

        // Get pose for view puppet and scale
        PuppetPose pose = aPose.cloneForPuppetAtScale(getPuppet(), _scale);

        // Iterate over pose keys and add pose marker and x/y location to map
        for (String poseKey : getSchema().getPoseKeys()) {
            //View pview = getChildForName(poseKey);
            Point pnt = pose.getMarkerPoint(poseKey);
            double px = pnt.x + anchor.x;
            double py = anchor.y - pnt.y;
            _phys.setJointOrMarkerToViewXY(poseKey, px, py);
        }

        if (isPoseSmoothly())
            _phys.resolveMouseJointsOverTime();
        else _phys.resolveMouseJoints();
    }

    /**
     * Returns whether poses are set over time (animated) as opposed to instantly.
     */
    public boolean isPoseSmoothly()  { return _poseSmoothly; }

    /**
     * Sets whether poses are set over time (animated) as opposed to instantly.
     */
    public void setPoseSmoothly(boolean aValue)
    {
        _poseSmoothly = aValue;
    }

    /**
     * Returns whether to show markers.
     */
    public boolean isShowMarkers()  { return _showMarkers; }

    /**
     * Sets whether to show markers.
     */
    public void setShowMarkers(boolean aValue)
    {
        // If already set, just return, otherwise set
        if (aValue == _showMarkers) return;
        _showMarkers = aValue;

        // Iterate over children and toggle visible if Joint
        for (View child : getChildren()) {
            if (child instanceof JointView)
                child.setVisible(aValue);
        }
    }

    /**
     * Returns whether puppet is posable via user interaction.
     */
    public boolean isPosable()  { return _phys != null; }

    /**
     * Sets whether puppet is posable via user interaction.
     */
    public void setPosable(boolean aValue)
    {
        // If already set, just return
        if (aValue == isPosable()) return;

        // Create/start PhysRunner
        if (aValue) _phys = new PuppetViewPhys(this); //_physRunner.setRunning(true);

            // Stop/clear PhysRunner
        else _phys = null; //_physRunner.setRunning(false);
    }

    /**
     * Returns whether to Freeze outer joints on drag.
     */
    public boolean isFreezeOuterJoints()  { return _phys != null && _phys._freezeOuterJoints; }

    /**
     * Sets whether to Freeze outer joints on drag.
     */
    public void setFreezeOuterJoints(boolean aValue)
    {
        if (_phys != null) _phys._freezeOuterJoints = aValue;
    }

    /**
     * A view to display puppet parts.
     */
    protected static class PartView extends ImageView {

        // The PuppetPart
        PuppetPart _part;

        /**
         * Creates a PartView for given PuppetPart.
         */
        public PartView(PuppetPart aPart)
        {
            _part = aPart;
            setImage(aPart.getImage());
            setName(aPart.getName());
            getPhysics(true).setGroupIndex(-1);
        }
    }

    /**
     * A view to display puppet joints.
     */
    protected static class JointView extends ImageView {

        // The PuppetJoint
        PuppetJoint _joint;

        /**
         * Creates a JointView for given PuppetJoint.
         */
        public JointView(PuppetJoint aJoint)
        {
            _joint = aJoint;
            setImage(aJoint.getImage());
            setName(aJoint.getName());
            setSize(getPrefSize());
            if (aJoint.isMarker()) getPhysics(true).setGroupIndex(-1);
            else getPhysics(true).setJoint(true);
        }
    }
}