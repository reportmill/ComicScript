package puppets.puppet;

import java.util.*;

import snap.geom.Point;
import snap.util.*;

/**
 * A class to hold a pose.
 */
public class PuppetPose implements Cloneable {

    // The pose name
    String _name;

    // The pose marker maps
    Map<String, Point> _markers;

    /**
     * Creates a new pose for name.
     */
    public PuppetPose()
    {
        _markers = new LinkedHashMap();
    }

    /**
     * Creates a new pose for name.
     */
    public PuppetPose(String aName)
    {
        _name = aName;
        _markers = new LinkedHashMap();
    }

    /**
     * Creates a new pose for name and map or markers.
     */
    public PuppetPose(String aName, Map<String, Point> aMap)
    {
        _name = aName;
        _markers = aMap;
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
     * Returns the maps.
     */
    public Map<String, Point> getMarkers()
    {
        return _markers;
    }

    /**
     * Returns the point for given marker.
     */
    public Point getMarkerPoint(String aName)
    {
        Point pnt = _markers.get(aName);
        if (pnt == null && aName.equals(PuppetSchema.Anchor_Joint)) pnt = new Point();
        return pnt;
    }

    /**
     * Sets a point for given marker name.
     */
    public void setMarkerPoint(String aName, Point aPoint)
    {
        _markers.put(aName, aPoint);
    }

    /**
     * Returns the angle between two joints.
     */
    public double getAngle(String aJName0, String aJName1)
    {
        Point p0 = getMarkerPoint(aJName0);
        Point p1 = getMarkerPoint(aJName1);
        return getAngle(p0, p1);
    }

    /**
     * Sets the angle between two joints, preserving the distance.
     */
    public void setAngle(String aJName0, String aJName1, double anAng)
    {
        Point p0 = getMarkerPoint(aJName0);
        Point p1 = getMarkerPoint(aJName1);
        double dist = p0.getDistance(p1);
        double x2 = p0.x + Math.cos(anAng) * dist;
        double y2 = p0.y + Math.sin(anAng) * dist;
        p1.setXY(x2, y2);
    }

    /**
     * Returns the distance between two joints.
     */
    public double getDistance(String aJName0, String aJName1)
    {
        Point p0 = getMarkerPoint(aJName0);
        Point p1 = getMarkerPoint(aJName1);
        return p0.getDistance(p1);
    }

    /**
     * Sets the distance between two joints, preserving the angle.
     */
    public void setDistance(String aJName0, String aJName1, double aDist)
    {
        Point p0 = getMarkerPoint(aJName0);
        Point p1 = getMarkerPoint(aJName1);
        double ang = getAngle(p0, p1);
        double x2 = p0.x + Math.cos(ang) * aDist;
        double y2 = p0.y + Math.sin(ang) * aDist;
        p1.setXY(x2, y2);
    }

    /**
     * Sets the distance between two joints, preserving the angle.
     */
    public void setAngleAndDistance(String aJName0, String aJName1, double anAng, double aDist)
    {
        Point p0 = getMarkerPoint(aJName0);
        Point p1 = getMarkerPoint(aJName1);
        double x2 = p0.x + Math.cos(anAng) * aDist;
        double y2 = p0.y + Math.sin(anAng) * aDist;
        p1.setXY(x2, y2);
    }

    /**
     * Returns a blended pose with this pose and another pose at given ratio.
     */
    public PuppetPose getBlendPose(Puppet aPuppet, PuppetPose aPose, double aRatio)
    {
        // Copy this pose
        PuppetPose pose = clone();

        // Iterate over puppet root joint names
        PuppetSchema schema = aPuppet.getSchema();
        for (String name : schema.getRootJointNames()) {

            // Set blend marker point for root joint
            setBlendPoseMarker(pose, aPose, name, aRatio);

            // If next joint, set blend point for it
            String jointThis = name, jointNext = schema.getNextJointNameForName(name);
            while (jointNext != null) {
                pose.setBlendPoseMarker(this, aPose, jointThis, jointNext, aRatio);
                jointThis = jointNext;
                jointNext = schema.getNextJointNameForName(jointNext);
            }
        }

        // Return pose
        return pose;
    }

    /**
     * Returns a blended pose with this pose and another pose at given ratio.
     */
    void setBlendPoseMarker(PuppetPose aPose1, PuppetPose aPose2, String aJointName, double aRatio)
    {
        // Blend root names
        Point p0 = aPose1.getMarkerPoint(aJointName);
        Point p1 = aPose2.getMarkerPoint(aJointName);
        double x = p0.x + (p1.x - p0.x) * aRatio;
        double y = p0.y + (p1.y - p0.y) * aRatio;
        p0.setXY(x, y);
    }

    /**
     * Returns a blended pose with this pose and another pose at given ratio.
     */
    void setBlendPoseMarker(PuppetPose aPose1, PuppetPose aPose2, String aJointName1, String aJointName2, double aRatio)
    {
        // Get start points of start line and end line
        Point p0 = aPose1.getMarkerPoint(aJointName1);
        Point p1 = aPose2.getMarkerPoint(aJointName1);

        // Get end points of start line and end line
        Point p2 = aPose1.getMarkerPoint(aJointName2);
        Point p3 = aPose2.getMarkerPoint(aJointName2);
        if (p2.equals(p3)) return;

        // Get angle of start line, end line and blend line
        double ang0 = getAngle(p0, p2);
        double ang1 = getAngle(p1, p3);
        double dang = ang1 - ang0;
        if (Math.abs(dang) > Math.PI) dang = Math.copySign(2 * Math.PI - Math.abs(dang), -dang);
        double ang2 = ang0 + dang * aRatio;

        // Get distance of start line and calculate end point of blend line and set
        double dist = p1.getDistance(p3) + .2;
        Point p0b = getMarkerPoint(aJointName1);
        double x2 = p0b.x + Math.cos(ang2) * dist;
        double y2 = p0b.y + Math.sin(ang2) * dist;
        Point p1b = getMarkerPoint(aJointName2);
        p1b.setXY(x2, y2);
    }

    /**
     * Standard clone implementation.
     */
    public PuppetPose clone()
    {
        PuppetPose clone = null;
        try {
            clone = (PuppetPose) super.clone();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        clone._markers = new LinkedHashMap(_markers.size());
        for (String name : _markers.keySet())
            clone._markers.put(name, _markers.get(name).clone());
        return clone;
    }

    /**
     * Creates a clone pose ajusted to given puppet at scale.
     */
    public PuppetPose cloneForPuppetAtScale(Puppet toPup, double toScale)
    {
        // Get reference Puppet/Scale that pose was set from
        Puppet pup0 = PuppetUtils.getPuppetFile().getPuppet(0);
        double pup0Scale = 500 / pup0.getBounds().height;

        // Create new pose
        PuppetPose clone = clone();

        // Get poses for pup0 and pupX
        PuppetPose pose0 = pup0.getPose(pup0Scale);
        PuppetPose poseX = toPup.getPose(toScale);

        // Iterate over root joints
        for (String rkey : pup0.getSchema().getRootJointNames()) {

            // Get distance from anchor to root joint for pose0 && poseX
            double dist0 = pose0.getDistance(PuppetSchema.Anchor_Joint, rkey);
            double distX = poseX.getDistance(PuppetSchema.Anchor_Joint, rkey);
            double scale = distX / dist0;
            double distN = getDistance(PuppetSchema.Anchor_Joint, rkey) * scale;
            clone.setDistance(PuppetSchema.Anchor_Joint, rkey, distN);

            // Get angle from anchor to root joint for pose0 && poseX
            double ang0 = pose0.getAngle(PuppetSchema.Anchor_Joint, rkey);
            clone.setAngle(PuppetSchema.Anchor_Joint, rkey, ang0);

            // Get keys for this joint and next
            String thisKey = rkey;
            String nextKey = pup0.getSchema().getNextJointNameForName(thisKey);

            // While outer joint exists, adjust point
            while (nextKey != null) {

                // Get joint angle from original pose and joint distance from new pose puppet
                double ang = getAngle(thisKey, nextKey);
                double dist = getJointDistance(toPup, toScale, thisKey);

                // Update pose1 joint point with new angle and distance
                clone.setAngleAndDistance(thisKey, nextKey, ang, dist);

                // Get key for next joint
                thisKey = nextKey;
                nextKey = pup0.getSchema().getNextJointNameForName(thisKey);
            }
        }

        // Return clone
        return clone;
    }

    /**
     * Returns the distance between joints for given puppet, scale and joint name.
     */
    private static double getJointDistance(Puppet aPuppet, double aScale, String aJName)
    {
        PuppetJoint jnt0 = aPuppet.getJoint(aJName);
        PuppetJoint jnt1 = jnt0.getNext();
        return Point.getDistance(jnt0.getX(), jnt0.getY(), jnt1.getX(), jnt1.getY()) * aScale;
    }

    /**
     * XML Archival.
     */
    public XMLElement toXML(XMLArchiver anArchiver)
    {
        // Get new element with name
        XMLElement e = new XMLElement("Pose");
        e.add("Name", getName());

        // Iterate over markers and set
        for (String key : getMarkers().keySet()) {
            Point pnt = getMarkerPoint(key);
            String val = StringUtils.formatNum("#.##", pnt.x) + ' ' + StringUtils.formatNum("#.##", pnt.y);
            key = key.substring(0, key.length() - "Joint".length());
            e.add(key, val);
        }

        // Return element
        return e;
    }

    /**
     * XML unarchival.
     */
    public PuppetPose fromXML(XMLArchiver anArchiver, XMLElement anElement)
    {
        // Unarchive name
        String name = anElement.getAttributeValue("Name");
        setName(name);

        // Iterate over markers and set
        Map<String, Point> markers = new LinkedHashMap();
        for (XMLAttribute attr : anElement.getAttributes()) {
            String key = attr.getName(), valStr = attr.getValue();
            if (key.equals("Name")) continue;
            if (key.endsWith("Marker")) key = key.replace("Marker", "Joint"); // This can go soon
            else if (!key.endsWith("Joint")) key += "Joint";
            key = key.intern();
            String valStrs[] = valStr.split("\\s");
            Double val0 = Double.valueOf(valStrs[0]), val1 = Double.valueOf(valStrs[1]);
            Point pnt = new Point(val0, val1);
            _markers.put(key, pnt);
        }

        // Return this
        return this;
    }

    /**
     * Returns a full string.
     */
    public String getAsString()
    {
        StringBuffer sb = new StringBuffer();
        for (String key : _markers.keySet()) {
            Point pnt = getMarkerPoint(key);
            String x = StringUtils.formatNum("#.#", pnt.x), y = StringUtils.formatNum("#.#", pnt.y);
            String str = String.format("%s: [ %s %s ],\n", key, x, y);
            sb.append(str);
        }
        return sb.toString();
    }

    /**
     * Returns the angle for a set of line points.
     */
    private static double getAngle(Point p0, Point p1)
    {
        return getAngle(p0.x, p0.y, p1.x, p1.y);
    }

    /**
     * Returns the angle for a set of line point coordinates.
     */
    private static double getAngle(double x0, double y0, double x1, double y1)
    {
        return Math.atan2(y1 - y0, x1 - x0);
    }

}