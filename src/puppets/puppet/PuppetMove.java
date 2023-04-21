package puppets.puppet;

import snap.util.*;

/**
 * A class to represent a pose over a given time.
 */
public class PuppetMove implements Cloneable {

    // The pose
    PuppetPose _pose;

    // The time span in milliseconds for the change
    int _time;

    /**
     * Creates a PuppetMove.
     */
    public PuppetMove()
    {
    }

    /**
     * Creates a PoseChange.
     */
    public PuppetMove(PuppetPose aPose, int theTime)
    {
        _pose = aPose;
        _time = theTime;
    }

    /**
     * Returns the pose.
     */
    public PuppetPose getPose()
    {
        return _pose;
    }

    /**
     * Returns the pose name.
     */
    public String getPoseName()
    {
        return _pose.getName();
    }

    /**
     * Returns the time interval.
     */
    public int getTime()
    {
        return _time;
    }

    /**
     * Sets the time interval.
     */
    public void setTime(int aValue)
    {
        _time = aValue;
    }

    /**
     * Standard clone implementation.
     */
    public PuppetMove clone()
    {
        PuppetMove clone = null;
        try {
            clone = (PuppetMove) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
        return clone;
    }

    /**
     * Standard toString implementation.
     */
    public String toString()
    {
        return "PuppetMove:" + getPoseName();
    }

    /**
     * XML Archival.
     */
    public XMLElement toXML(XMLArchiver anArchiver)
    {
        XMLElement e = new XMLElement("Move");
        e.add("Pose", getPose().getName());
        e.add("Time", getTime());
        return e;
    }

    /**
     * XML unarchival.
     */
    public PuppetMove fromXML(PuppetAction anAction, XMLElement anElement)
    {
        String name = anElement.getAttributeValue("Pose");
        _pose = anAction.getPoseForName(name);
        _time = anElement.getAttributeIntValue("Time");
        return this;
    }

}