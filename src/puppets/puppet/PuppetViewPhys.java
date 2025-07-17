package puppets.puppet;
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
    PuppetView _pupView;

    // The Box2D World
    World _world;

    // The ratio of screen points to Box2D world meters.
    double _scale = 720 / 10d;

    // Whether to freeze outer joints when moving a part
    boolean _freezeOuterJoints = true;

    // The Runner
    Runnable _runner;

    // Transforms
    Transform _localToBox;

    // Listener to handle drags
    EventListener _dragFilter = e -> handleDrag(e);

    // Ground Body
    Body _groundBody;

    // MouseJoint used for dragging
    MouseJoint _dragJoint;

    // List of MouseJoints used to move joints into poses
    List<MouseJoint> _poseMouseJoints = new ArrayList<>();

    //
    long _poseMouseTime;

    // Constants
    static int FRAME_DELAY_MILLIS = 20;
    static float FRAME_DELAY_SECS = 20 / 1000f;

    /**
     * Create new PhysicsRunner.
     */
    public PuppetViewPhys(PuppetView aView)
    {
        // Set View
        _pupView = aView;

        // Create world
        _world = new World(new Vec2(0, 0));//-9.8f));

        // Get PuppetView, Puppet
        Puppet puppet = _pupView.getPuppet();
        PuppetSchema pschema = puppet.getSchema();

        // Add bodies for view children
        List<View> joints = new ArrayList<>();
        List<View> markers = new ArrayList<>();
        for (View child : _pupView.getChildren()) {
            ViewPhysics<?> phys = child.getPhysics(true);
            if (phys.isJoint())
                joints.add(child);
            else if (pschema.isMarkerName(child.getName()))
                markers.add(child);
            else if (child.isVisible()) {
                phys.setDynamic(true);
                createJboxBodyForView(child);
                addDragger(child);
            }
        }

        // Add joints
        for (View v : joints)
            createJoint(v);
        for (View v : markers)
            createMarker(v);

        // Add sidewalls
        double vw = _pupView.getWidth();
        double vh = _pupView.getHeight();
        RectView r0 = new RectView(-1, -900, 1, vh + 900);
        r0.getPhysics(true);  // Left
        _groundBody = createJboxBodyForView(r0); //createBody(r1); createBody(r2);
    }

    /**
     * Returns the scale of the world in screen points to Box2D world meters.
     */
    public double getScreenPointsToWorldMeters(double aScale)
    {
        return _scale;
    }

    /**
     * Sets the scale of the world in screen points to Box2D world meters.
     * <p>
     * So if you want your 720 point tall window to be 10m, set scale to be 720/10d (the default).
     */
    public void setScreenPointsToWorldMeters(double aScale)
    {
        _scale = aScale;
    }

    /**
     * Returns a view for given name.
     */
    public View getView(String aName)
    {
        return _pupView.getChildForName(aName);
    }

    /**
     * Returns the puppet.
     */
    public Puppet getPuppet()
    {
        return _pupView.getPuppet();
    }

    /**
     * Returns whether physics is running.
     */
    public boolean isRunning()
    {
        return _runner != null;
    }

    /**
     * Sets whether physics is running.
     */
    public void setRunning(boolean aValue)
    {
        // If already set, just return
        if (aValue == isRunning()) return;

        // Set timer to call timerFired 25 times a second
        if (_runner == null)
            ViewEnv.getEnv().runIntervals(_runner = () -> timerFired(), FRAME_DELAY_MILLIS);
        else {
            ViewEnv.getEnv().stopIntervals(_runner);
            _runner = null;
        }
    }

    /**
     * Called when world timer fires.
     */
    void timerFired()
    {
        // Update world
        _world.step(FRAME_DELAY_SECS, 20, 20);

        // Update Dynamics
        for (int i = 0, iMax = _pupView.getChildCount(); i < iMax; i++)
            updateView(_pupView.getChild(i));

        // Clear PoseMouseJoints
        if (_poseMouseTime > 0 && System.currentTimeMillis() > _poseMouseTime + 800)
            clearMouseJoints();
    }

    /**
     * Updates a view from a body.
     */
    public void updateView(View aView)
    {
        // Get ViewPhysics and body
        ViewPhysics<Body> phys = aView.getPhysics();
        if (phys == null) return;
        Object ntv = phys.getNative();

        // Handle Body
        if (ntv instanceof Body) {
            Body body = (Body) ntv;
            if (!phys.isDynamic()) return;

            // Get/set position
            Vec2 pos = body.getPosition();
            Point posV = worldToView(pos.x, pos.y);
            aView.setXY(posV.x - aView.getWidth() / 2, posV.y - aView.getHeight() / 2);

            // Get set rotation
            float angle = body.getAngle();
            aView.setRotate(-Math.toDegrees(angle));
        }

        // Handle Joint
        else if (ntv instanceof RevoluteJoint) {
            RevoluteJoint joint = (RevoluteJoint) ntv;

            // Get/set position
            Vec2 pos = new Vec2(0, 0);
            joint.getAnchorA(pos);
            Point posV = worldToView(pos.x, pos.y);
            aView.setXY(posV.x - aView.getWidth() / 2, posV.y - aView.getHeight() / 2);

            // Get set rotation
            //float angle = joint.getAngle(); aView.setRotate(-Math.toDegrees(angle));
        }
    }

    /**
     * Returns whether any body is awake.
     */
    public boolean isAwake()
    {
        for (int i = 0, iMax = _pupView.getChildCount(); i < iMax; i++) {
            View view = _pupView.getChild(i);
            if (isAwake(view))
                return true;
        }
        return false;
    }

    /**
     * Returns whether given body is awake.
     */
    public boolean isAwake(View aView)
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
    public void createJoint(View aView)
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
        View viewA = getView(linkNames[0]);
        View viewB = getView(linkNames[1]);

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
        String name = aView.getName(), linkNames[] = pschema.getLinkNamesForJoint(name);
        if (linkNames.length < 1)
            return;

        aView.getPhysics(true).setDynamic(true);
        createJboxBodyForView(aView);

        // Get linked views
        View viewA = getView(linkNames[0]);
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
        String linkNames[] = pschema.getLinkNamesForJoint(aName);
        if (linkNames.length < 1) return;

        // Get Joint view
        View jview = getView(aName);
        Point jpnt = jview.localToParent(jview.getWidth() / 2, jview.getHeight() / 2);
        if (equals(jpnt.x, jpnt.y, aX, aY)) return;
        Vec2 jointVec = viewToWorld(jpnt.x, jpnt.y);

        // Get linked view, body and X/Y in body coords
        View view = getView(linkNames[0]);
        Body body = (Body) view.getPhysics().getNative();

        // Create MouseJoint and target X/Y
        MouseJointDef jdef = new MouseJointDef();
        jdef.bodyA = _groundBody;
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
            timerFired();
            if (i > 20 && !isAwake())
                break; //System.out.println("PupViewPhys: Not awake at frame " + i);
        }

        // Clear mouse joints
        clearMouseJoints();
    }

    /**
     * Resolve mouse joints.
     */
    void resolveMouseJointsOverTime()
    {
        if (isRunning() || _poseMouseTime == 0) return;
        timerFired();
        ViewUtils.runDelayed(() -> resolveMouseJointsOverTime(), FRAME_DELAY_MILLIS);
    }

    /**
     * Clear mouse joints.
     */
    void clearMouseJoints()
    {
        for (MouseJoint mj : _poseMouseJoints) _world.destroyJoint(mj);
        _poseMouseJoints.clear();
        _poseMouseTime = 0;
    }

    /**
     * Adds DragFilter to view.
     */
    void addDragger(View aView)
    {
        aView.addEventFilter(_dragFilter, View.MousePress, View.MouseDrag, View.MouseRelease);
    }

    /**
     * Called when View gets drag event.
     */
    void handleDrag(ViewEvent anEvent)
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
            jdef.bodyA = _groundBody;
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
    void setOuterJointLimitsEnabledForBodyName(String aName, boolean isEnabled)
    {
        // Get outer joint names
        PuppetSchema schema = getPuppet().getSchema();
        String jname = schema.getNextJointNameForName(aName);
        String jnameNext = jname != null ? schema.getNextJointNameForName(jname) : null;

        // Iterate over joint names and set limit enabled/disabled
        while (jnameNext != null) {
            View view = getView(jname);
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
     * Return Vec2 for snap Point.
     */
    Vec2 getVec(Point aPnt)
    {
        return new Vec2((float) aPnt.x, (float) aPnt.y);
    }

    /**
     * Convert View coord to Box2D world.
     */
    float viewToWorld(double aValue)
    {
        return (float) (aValue / _scale);
    }

    /**
     * Convert View coord to Box2D world.
     */
    Vec2 viewToWorld(double aX, double aY)
    {
        return getVec(getViewToWorld().transformXY(aX, aY));
    }

    /**
     * Returns a Vec2 in world coords for a point in View coords.
     */
    Vec2 viewToBoxLocal(double aX, double aY, View aView)
    {
        float x = viewToWorld(aX - aView.getWidth() / 2);
        float y = viewToWorld(aView.getHeight() / 2 - aY);
        return new Vec2(x, y);
    }

    /**
     * Convert Box2D world coord to View.
     */
    double worldToView(double aValue)
    {
        return aValue * _scale;
    }

    /**
     * Convert Box2D world coord to View.
     */
    Point worldToView(double aX, double aY)
    {
        return getWorldToView().transformXY(aX, aY);
    }

    /**
     * Returns transform from View coords to Box coords.
     */
    public Transform getViewToWorld()
    {
        // If already set, just return
        if (_localToBox != null) return _localToBox;

        // Create transform from WorldView bounds to World bounds
        Rect r0 = _pupView.getBoundsLocal();
        Rect r1 = new Rect(0, 0, r0.width / _scale, -r0.height / _scale);
        double bw = r0.width, bh = r0.height;
        double sx = bw != 0 ? r1.width / bw : 0, sy = bh != 0 ? r1.height / bh : 0;
        Transform trans = Transform.getScale(sx, sy);
        trans.translate(r1.x - r0.x, r1.y - r0.y);
        return trans;
    }

    /**
     * Returns transform from Box coords to View coords.
     */
    public Transform getWorldToView()
    {
        return getViewToWorld().getInverse();
    }

    /**
     * Returns whether given points are equal
     */
    public static boolean equals(double x0, double y0, double x1, double y1)
    {
        return equals(x0, x1) && equals(y0, y1);
    }

    /**
     * Returns whether given coords are equal
     */
    public static boolean equals(double a, double b)
    {
        return Math.abs(a - b) < 0.5;
    }

}