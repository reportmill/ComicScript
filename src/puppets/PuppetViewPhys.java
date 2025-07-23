package puppets;
import java.util.*;
import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.*;
import org.jbox2d.dynamics.joints.*;
import snap.geom.*;
import snap.util.ListUtils;
import snap.view.*;
import snap.view.EventListener;

/**
 * A class to run Box2D physics for a PuppetView.
 */
public class PuppetViewPhys {

    // The Snap View
    private PuppetView _puppetView;

    // The Box2D World
    private World _world;

    // The ratio of screen points to Box2D world meters.
    private double _pixelsToMeters = 720 / 10d;

    // Whether to freeze outer joints when moving a part
    protected boolean _freezeOuterJoints = true;

    // The Runner
    private Runnable _runner;

    // Listener to handle drags
    private EventListener _viewDraggingEventLsnr;

    // MouseJoint used for dragging
    private MouseJoint _dragJoint;

    // Static dummy body used by mouse joint for dragging
    private Body _dragGroundBody;

    // List of MouseJoints used to move joints into poses
    private List<MouseJoint> _poseMouseJoints = new ArrayList<>();

    //
    private long _poseMouseTime;

    // Constants
    private static int FRAME_DELAY_MILLIS = 20;
    private static float FRAME_DELAY_SECS = 20 / 1000f;

    /**
     * Create new PhysicsRunner.
     */
    public PuppetViewPhys(PuppetView aView)
    {
        // Set View
        _puppetView = aView;

        // Create world
        _world = new World(new Vec2(0, 0));

        // Get PuppetView, Puppet
        Puppet puppet = _puppetView.getPuppet();
        PuppetSchema pschema = puppet.getSchema();

        // Add bodies for view children
        List<View> joints = new ArrayList<>();
        List<View> markers = new ArrayList<>();
        for (View child : _puppetView.getChildren()) {
            ViewPhysics<?> phys = child.getPhysics(true);
            if (phys.isJoint())
                joints.add(child);
            else if (pschema.isMarkerName(child.getName()))
                markers.add(child);
            else if (child.isVisible()) {
                phys.setDynamic(true);
                createJboxBodyForView(child);
                enableDraggingForView(child);
            }
        }

        // Add joints
        for (View v : joints)
            createJboxJointForView(v);
        for (View v : markers)
            createMarker(v);
    }

    /**
     * Returns the puppet.
     */
    public Puppet getPuppet()  { return _puppetView.getPuppet(); }

    /**
     * Returns whether physics is running.
     */
    public boolean isRunning()  { return _runner != null; }

    /**
     * Sets whether physics is running.
     */
    public void setRunning(boolean aValue)
    {
        // If already set, just return
        if (aValue == isRunning()) return;

        // Set timer to call timerFired 25 times a second
        if (_runner == null)
            ViewEnv.getEnv().runIntervals(_runner = this::stepWorld, FRAME_DELAY_MILLIS);
        else {
            ViewEnv.getEnv().stopIntervals(_runner);
            _runner = null;
        }
    }

    /**
     * Called to process next world frame (usually by timer).
     */
    private void stepWorld()
    {
        // Update world
        _world.step(FRAME_DELAY_SECS, 20, 20);

        // Update world view children from jbox natives
        _puppetView.getChildren().forEach(this::updateViewFromJboxNative);

        // Clear PoseMouseJoints
        if (_poseMouseTime > 0 && System.currentTimeMillis() > _poseMouseTime + 800)
            clearMouseJoints();
    }

    /**
     * Updates a view from a body.
     */
    private void updateViewFromJboxNative(View aView)
    {
        // Get ViewPhysics and body
        ViewPhysics<?> phys = aView.getPhysics(); if (phys == null) return;
        Object jboxNative = phys.getNative();

        // Handle Body
        if (jboxNative instanceof Body body) {
            if (!phys.isDynamic()) return;

            // Get/set position
            Vec2 pos = body.getPosition();
            Point posV = worldToView(pos.x, pos.y);
            aView.setXY(posV.x - aView.getWidth() / 2, posV.y - aView.getHeight() / 2);

            // Get set rotation
            float angle = body.getAngle();
            aView.setRotate(-Math.toDegrees(angle));
        }

        // Handle Joint: Get position from joint and set in view
        else if (jboxNative instanceof RevoluteJoint joint) {
            Vec2 pos = new Vec2(0, 0);
            joint.getAnchorA(pos);
            Point posV = worldToView(pos.x, pos.y);
            aView.setXY(posV.x - aView.getWidth() / 2, posV.y - aView.getHeight() / 2);
        }
    }

    /**
     * Returns whether any body is awake.
     */
    public boolean isAwake()
    {
        return ListUtils.hasMatch(_puppetView.getChildren(), this::isAwake);
    }

    /**
     * Returns whether given body is awake.
     */
    private boolean isAwake(View aView)
    {
        // Get ViewPhysics (just return if null or not dynamic)
        ViewPhysics<Body> phys = aView.getPhysics();
        if (phys == null || !phys.isDynamic())
            return false;

        // Get body and return whether awake
        Body nativeBody = phys.getNative();
        return nativeBody != null && nativeBody.isAwake();
    }

    /**
     * Returns a body for a view.
     */
    public Body createJboxBodyForView(View aView)
    {
        // Create BodyDef
        ViewPhysics<Body> phys = aView.getPhysics();
        BodyDef bdef = new BodyDef();
        bdef.type = phys.isDynamic() ? BodyType.DYNAMIC : BodyType.KINEMATIC;
        bdef.position.set(viewToWorld(aView.getMidX(), aView.getMidY()));
        bdef.angle = (float) Math.toRadians(-aView.getRotate());
        bdef.linearDamping = 10;
        bdef.angularDamping = 10;

        // Create Body
        Body body = _world.createBody(bdef);

        // Create PolygonShape
        Shape vshape = aView.getBoundsShape();
        List<org.jbox2d.collision.shapes.Shape> pshapes = createJboxShapeForShape(vshape);

        // Create FixtureDef
        for (org.jbox2d.collision.shapes.Shape pshp : pshapes) {
            FixtureDef fdef = new FixtureDef();
            fdef.shape = pshp;
            fdef.restitution = .25f;
            fdef.density = (float) phys.getDensity();
            fdef.filter.groupIndex = phys.getGroupIndex();
            body.createFixture(fdef);
        }

        // Return body
        phys.setNative(body);
        return body;
    }

    /**
     * Creates a Box2D shape for given snap shape.
     */
    public List<org.jbox2d.collision.shapes.Shape> createJboxShapeForShape(Shape aShape)
    {
        // Handle Rect (simple case)
        if (aShape instanceof Rect rect) {
            PolygonShape pshape = new PolygonShape();
            float pw = viewToWorld(rect.width / 2);
            float ph = viewToWorld(rect.height / 2);
            pshape.setAsBox(pw, ph);
            return List.of(pshape);
        }

        // Handle Ellipse
        if (aShape instanceof Ellipse elp && aShape.getWidth() == aShape.getHeight()) {
            CircleShape cshape = new CircleShape();
            cshape.setRadius(viewToWorld(elp.getWidth() / 2));
            return List.of(cshape);
        }

        // Handle Arc
        if (aShape instanceof Arc arc && aShape.getWidth() == aShape.getHeight()) {
            if (arc.getSweepAngle() == 360) {
                CircleShape cshape = new CircleShape();
                cshape.setRadius(viewToWorld(arc.getWidth() / 2));
                return List.of(cshape);
            }
        }

        // Handle Polygon if Simple, Convex and less than 8 points
        if (aShape instanceof Polygon poly) {
            org.jbox2d.collision.shapes.Shape pshape = createJboxShapeForPolygon(poly);
            if (pshape != null)
                return List.of(pshape);
        }

        // Get shape centered around shape midpoint
        Rect bnds = aShape.getBounds();
        Shape shape = aShape.copyFor(new Transform(-bnds.width / 2, -bnds.height / 2));

        // Get convex Polygons for shape
        List<Polygon> convexPolys = Polygon.getConvexPolygonsWithMaxSideCount(shape, 8);
        return ListUtils.mapNonNull(convexPolys, poly -> createJboxShapeForPolygon(poly));
    }

    /**
     * Creates a Box2D shape for given snap shape.
     */
    public org.jbox2d.collision.shapes.Shape createJboxShapeForPolygon(Polygon aPoly)
    {
        // If invalid, just return null
        if (aPoly.isSelfIntersecting() || !aPoly.isConvex() || aPoly.getPointCount() > 8) {
            System.err.println("PhysicsRunner:.createJboxShapeForPolygon: failure");
            return null;
        }

        // Create Box2D PolygonShape and return
        int pc = aPoly.getPointCount();
        Vec2[] vecs = new Vec2[pc];
        for (int i = 0; i < pc; i++) vecs[i] = viewToWorld(aPoly.getPointX(i), aPoly.getPointY(i));
        PolygonShape pshape = new PolygonShape();
        pshape.set(vecs, vecs.length);
        return pshape;
    }

    /**
     * Creates a Joint.
     */
    public void createJboxJointForView(View aView)
    {
        // Get shapes interesting joint view
        PuppetSchema pschema = getPuppet().getSchema();
        String name = aView.getName();
        String[] linkNames = pschema.getLinkNamesForJoint(name);
        if (linkNames.length < 2) {
            System.out.println("PhysicsRunner.createJoint: 2 Bodies not found for joint: " + name);
            return;
        }

        // Get linked views
        View viewA = getViewForName(linkNames[0]);
        View viewB = getViewForName(linkNames[1]);

        // Create joint def and set body A/B
        RevoluteJointDef jointDef = new RevoluteJointDef();
        jointDef.bodyA = (Body) viewA.getPhysics().getNative();
        jointDef.bodyB = (Body) viewB.getPhysics().getNative();
        jointDef.collideConnected = false;
        if (jointDef.bodyA == null || jointDef.bodyB == null)
            System.out.println("Hey");

        // Set anchors
        Point jointPnt = aView.localToParent(aView.getWidth() / 2, aView.getHeight() / 2);
        Point jointPntA = viewA.parentToLocal(jointPnt.x, jointPnt.y);
        Point jointPntB = viewB.parentToLocal(jointPnt.x, jointPnt.y);
        jointDef.localAnchorA = viewToBoxLocal(jointPntA.x, jointPntA.y, viewA);
        jointDef.localAnchorB = viewToBoxLocal(jointPntB.x, jointPntB.y, viewB);
        RevoluteJoint joint = (RevoluteJoint) _world.createJoint(jointDef);
        aView.getPhysics(true).setNative(joint);

        // Remove view for joint
        aView.setVisible(false);
    }

    /**
     * Creates a Joint.
     */
    public void createMarker(View aView)
    {
        // Get shapes interesting joint view
        PuppetSchema pschema = getPuppet().getSchema();
        String name = aView.getName();
        String[] linkNames = pschema.getLinkNamesForJoint(name);
        if (linkNames.length < 1)
            return;

        aView.getPhysics(true).setDynamic(true);
        createJboxBodyForView(aView);

        // Get linked views
        View viewA = getViewForName(linkNames[0]);
        View viewB = aView;

        // Create joint def and set body A/B
        RevoluteJointDef jointDef = new RevoluteJointDef();
        jointDef.bodyA = (Body) viewA.getPhysics().getNative();
        jointDef.bodyB = (Body) viewB.getPhysics().getNative();
        jointDef.collideConnected = false;

        // Set anchors
        Point jointPnt = aView.localToParent(aView.getWidth() / 2, aView.getHeight() / 2);
        Point jointPntA = viewA.parentToLocal(jointPnt.x, jointPnt.y);
        Point jointPntB = viewB.parentToLocal(jointPnt.x, jointPnt.y);
        jointDef.localAnchorA = viewToBoxLocal(jointPntA.x, jointPntA.y, viewA);
        jointDef.localAnchorB = viewToBoxLocal(jointPntB.x, jointPntB.y, viewB);
        RevoluteJoint joint = (RevoluteJoint) _world.createJoint(jointDef);

        // Remove view for joint
        aView.setVisible(false);
    }

    /**
     * Moves a joint or marker to given XY in world view coords using mouse joint.
     */
    public void setJointOrMarkerToViewXY(String aName, double aX, double aY)
    {
        // Get joint or marker link name(s)
        PuppetSchema pschema = getPuppet().getSchema();
        String[] linkNames = pschema.getLinkNamesForJoint(aName);
        if (linkNames.length < 1) return;

        // Get Joint view
        View jview = getViewForName(aName);
        Point jpnt = jview.localToParent(jview.getWidth() / 2, jview.getHeight() / 2);
        if (equals(jpnt.x, jpnt.y, aX, aY)) return;
        Vec2 jointVec = viewToWorld(jpnt.x, jpnt.y);

        // Get linked view, body and X/Y in body coords
        View view = getViewForName(linkNames[0]);
        Body body = (Body) view.getPhysics().getNative();

        // Create MouseJoint and target X/Y
        MouseJointDef jdef = new MouseJointDef();
        jdef.bodyA = getDragGroundBody();
        jdef.bodyB = body;
        jdef.collideConnected = true;
        jdef.maxForce = 1000f * body.getMass();
        jdef.target.set(jointVec);
        MouseJoint mjnt = (MouseJoint) _world.createJoint(jdef);
        mjnt.setTarget(viewToWorld(aX, aY));
        body.setAwake(true);
        _poseMouseJoints.add(mjnt);
        _poseMouseTime = System.currentTimeMillis();
    }

    /**
     * Resolve mouse joints.
     */
    public void resolveMouseJoints()
    {
        // If no unresolved pose, just return
        if (_poseMouseTime == 0) return;

        // Iterate over 40 frames to resolve pose
        for (int i = 0; i < 40 && _poseMouseTime != 0; i++) {
            stepWorld();
            if (i > 20 && !isAwake())
                break; //System.out.println("PupViewPhys: Not awake at frame " + i);
        }

        // Clear mouse joints
        clearMouseJoints();
    }

    /**
     * Resolve mouse joints.
     */
    protected void resolveMouseJointsOverTime()
    {
        if (isRunning() || _poseMouseTime == 0) return;
        stepWorld();
        ViewUtils.runDelayed(() -> resolveMouseJointsOverTime(), FRAME_DELAY_MILLIS);
    }

    /**
     * Clear mouse joints.
     */
    private void clearMouseJoints()
    {
        for (MouseJoint mj : _poseMouseJoints) _world.destroyJoint(mj);
        _poseMouseJoints.clear();
        _poseMouseTime = 0;
    }

    /**
     * Enables user mouse dragging of given view.
     */
    private void enableDraggingForView(View aView)
    {
        if (_viewDraggingEventLsnr == null) _viewDraggingEventLsnr = this::handleViewMouseEventForDragging;
        aView.addEventFilter(_viewDraggingEventLsnr, View.MousePress, View.MouseDrag, View.MouseRelease);
    }

    /**
     * Called when View gets drag event.
     */
    private void handleViewMouseEventForDragging(ViewEvent anEvent)
    {
        // Get View, ViewPhysics, Body and Event point in page view
        View view = anEvent.getView();
        ViewPhysics<Body> phys = view.getPhysics();
        Body body = phys.getNative();
        Point pnt = anEvent.getPoint(view.getParent());
        anEvent.consume();

        // Handle MousePress: Create & install drag MouseJoint
        if (anEvent.isMousePress()) {
            setRunning(true);
            MouseJointDef jdef = new MouseJointDef();
            jdef.bodyA = getDragGroundBody();
            jdef.bodyB = body;
            jdef.collideConnected = true;
            jdef.maxForce = 1000f * body.getMass();
            jdef.target.set(viewToWorld(pnt.x, pnt.y));
            _dragJoint = (MouseJoint) _world.createJoint(jdef);
            body.setAwake(true);
            if (_freezeOuterJoints)
                setOuterJointLimitsEnabledForBodyName(view.getName(), true);
        }

        // Handle MouseDrag: Update drag MouseJoint
        else if (anEvent.isMouseDrag()) {
            Vec2 target = viewToWorld(pnt.x, pnt.y);
            _dragJoint.setTarget(target);
        }

        // Handle MouseRelease: Remove drag MouseJoint
        else if (anEvent.isMouseRelease()) {
            _world.destroyJoint(_dragJoint);
            _dragJoint = null;
            setRunning(false);
            if (_freezeOuterJoints)
                setOuterJointLimitsEnabledForBodyName(view.getName(), false);
        }
    }

    /**
     * Freezes outer joints for a body name.
     */
    private void setOuterJointLimitsEnabledForBodyName(String aName, boolean isEnabled)
    {
        // Get outer joint names
        PuppetSchema schema = getPuppet().getSchema();
        String jname = schema.getNextJointNameForName(aName);
        String jnameNext = jname != null ? schema.getNextJointNameForName(jname) : null;

        // Iterate over joint names and set limit enabled/disabled
        while (jnameNext != null) {
            View view = getViewForName(jname);
            ViewPhysics<Joint> phys = view.getPhysics();
            RevoluteJoint joint = (RevoluteJoint) phys.getNative();
            joint.enableLimit(isEnabled);
            if (isEnabled) joint.setLimits(joint.getJointAngle(), joint.getJointAngle());
            else joint.setLimits(0, 0);
            jname = jnameNext;
            jnameNext = schema.getNextJointNameForName(jnameNext);
        }
    }

    /**
     * Returns a view for given name.
     */
    private View getViewForName(String aName)  { return _puppetView.getChildForName(aName); }

    /**
     * Convert View coord to Box2D world.
     */
    private float viewToWorld(double aValue)  { return (float) (aValue / _pixelsToMeters); }

    /**
     * Convert View coord to Box2D world.
     */
    private Vec2 viewToWorld(double aX, double aY)
    {
        double jboxX = aX / _pixelsToMeters;
        double jboxY = -aY / _pixelsToMeters;
        return new Vec2((float) jboxX, (float) jboxY);
    }

    /**
     * Returns a Vec2 in world coords for a point in View coords.
     */
    private Vec2 viewToBoxLocal(double aX, double aY, View aView)
    {
        float jboxX = viewToWorld(aX - aView.getWidth() / 2);
        float jboxY = viewToWorld(aView.getHeight() / 2 - aY);
        return new Vec2(jboxX, jboxY);
    }

    /**
     * Convert Box2D world coord to View.
     */
    private Point worldToView(double aX, double aY)
    {
        double viewX = aX * _pixelsToMeters;
        double viewY = -aY * _pixelsToMeters;
        return new Point(viewX, viewY);
    }

    /** Returns the static dummy body used by mouse joint for dragging. */
    private Body getDragGroundBody()
    {
        if (_dragGroundBody != null) return _dragGroundBody;
        return _dragGroundBody = _world.createBody(new BodyDef());
    }

    /**
     * Returns whether given coords or points are equal
     */
    private static boolean equals(double a, double b)  { return Math.abs(a - b) < 0.5; }
    private static boolean equals(double x0, double y0, double x1, double y1)  { return equals(x0, x1) && equals(y0, y1); }
}