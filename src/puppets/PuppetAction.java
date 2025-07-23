package puppets;

import java.util.*;

import snap.util.*;

/**
 * A class to define an animated action.
 */
public class PuppetAction {

    // The action name
    String _name;

    // A list of poses used by this action
    List<PuppetPose> _poses = new ArrayList<>();

    // A list of moves
    List<PuppetMove> _moves = new ArrayList<>();

    /**
     * Constructor.
     */
    public PuppetAction()
    {
    }

    /**
     * Constructor for given name.
     */
    public PuppetAction(String aName)
    {
        setName(aName);
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
     * Returns the list of poses defined for this action.
     */
    public List<PuppetPose> getPoses()  { return _poses; }

    /**
     * Adds a pose.
     */
    public void addPose(PuppetPose aPose)  { addPose(aPose, _poses.size()); }

    /**
     * Adds a pose.
     */
    public void addPose(PuppetPose aPose, int anIndex)
    {
        getPoses().add(anIndex, aPose);
    }

    /**
     * Removes a pose.
     */
    public void removePose(int anIndex)
    {
        _poses.remove(anIndex);
    }

    /**
     * Returns the pose with given name.
     */
    public PuppetPose getPoseForName(String aName)
    {
        return ListUtils.findMatch(_poses, pose -> pose.getName().equals(aName));
    }

    /**
     * Returns the list of moves defined for this action.
     */
    public List<PuppetMove> getMoves()  { return _moves; }

    /**
     * Returns the number of moves.
     */
    public int getMoveCount()  { return _moves.size(); }

    /**
     * Returns the individual move at given index.
     */
    public PuppetMove getMove(int anIndex)  { return _moves.get(anIndex); }

    /**
     * Returns the individual move pose at given index.
     */
    public PuppetPose getMovePose(int anIndex)
    {
        return getMove(anIndex).getPose();
    }

    /**
     * Adds a move for pose and time.
     */
    public PuppetMove addMoveForPoseAndTime(PuppetPose aPose, int aTime)
    {
        PuppetMove move = new PuppetMove(aPose, aTime);
        addMove(move);
        return move;
    }

    /**
     * Adds a move.
     */
    public void addMove(PuppetMove aMove)  { addMove(aMove, getMoveCount()); }

    /**
     * Adds a move at given index.
     */
    public void addMove(PuppetMove aMove, int anIndex)
    {
        // Add to moves list
        getMoves().add(anIndex, aMove);

        // If pose not in PoseList, add it
        String poseName = aMove.getPoseName();
        if (getPoseForName(poseName) == null)
            addPose(aMove.getPose());
    }

    /**
     * Removes a move.
     */
    public PuppetMove removeMove(int anIndex)
    {
        PuppetMove move = getMoves().remove(anIndex);
        return move;
    }

    /**
     * Returns the max time for action.
     */
    public int getMaxTime()
    {
        int t = 0;
        for (int i = 0, iMax = getMoveCount() - 1; i < iMax; i++) t += getMove(i).getTime();
        return t;
    }

    /**
     * Returns the move index for given time.
     */
    public int getMoveIndexAtTime(int aTime)
    {
        int time = 0;
        for (int i = 0, iMax = getMoveCount(); i < iMax; i++) {
            PuppetMove move = getMove(i);
            time += move.getTime();
            if (aTime < time)
                return i;
        }
        return getMoveCount() - 1;
    }

    /**
     * Returns the move index for given time.
     */
    public int getMoveStartTime(int anIndex)
    {
        int time = 0;
        for (int i = 0; i < anIndex; i++) time += getMove(i).getTime();
        return time;
    }

    /**
     * Returns the puppet pose for given global time.
     */
    public PuppetPose getPoseForTime(Puppet aPuppet, int aTime)
    {
        // If at start or end, just return appropriate pose
        if (getMoveCount() == 0) return null;
        if (aTime == 0) return getMovePose(0);
        if (aTime >= getMaxTime()) return getMovePose(getMoveCount() - 1);

        // Get surrounding moves, get blend pose and set
        int moveIndex = getMoveIndexAtTime(aTime);
        PuppetMove move0 = getMove(moveIndex);
        PuppetMove move1 = getMove(moveIndex + 1);
        double moveTime = aTime - getMoveStartTime(moveIndex);
        double moveRatio = moveTime / move0.getTime();

        // Get surrounding poses, get blend pose and set
        PuppetPose pose0 = move0.getPose();
        PuppetPose pose1 = move1.getPose();
        PuppetPose pose2 = pose0.getBlendPose(aPuppet, pose1, moveRatio);
        return pose2;
    }

    /**
     * Replaces first pose with second pose.
     */
    public void replacePose(String aName, PuppetPose aPose)
    {
        PuppetPose pose = getPoseForName(aName);
        PuppetPose pose2 = aPose.clone();
        pose2.setName(aName);
        int ind = getPoses().indexOf(pose);
        if (ind < 0) return;
        removePose(ind);
        addPose(pose2, ind);

        // Reset Move poses
        for (PuppetMove move : getMoves())
            move._pose = getPoseForName(move.getPoseName());
    }

    /**
     * Standard toString implementation.
     */
    public String toString()
    {
        return "PuppetAction:" + getName();
    }

    /**
     * XML Archival.
     */
    public XMLElement toXML(XMLArchiver anArchiver)
    {
        // Get new element with class name
        XMLElement e = new XMLElement("Action");
        e.add("Name", getName());

        // Create element for poses and iterate over poses and add each
        XMLElement posesXML = new XMLElement("Poses");
        e.add(posesXML);
        for (PuppetPose pose : getPoses()) {
            XMLElement poseXML = pose.toXML(anArchiver);
            posesXML.add(poseXML);
        }

        // Create element for steps and iterate over moves and add each
        XMLElement movesXML = new XMLElement("Moves");
        e.add(movesXML);
        for (PuppetMove move : getMoves()) {
            XMLElement moveXML = move.toXML(anArchiver);
            movesXML.add(moveXML);
        }

        // Return element
        return e;
    }

    /**
     * XML unarchival.
     */
    public PuppetAction fromXML(XMLArchiver anArchiver, XMLElement anElement)
    {
        // Unarchive name
        String name = anElement.getAttributeValue("Name");
        setName(name);

        // Iterate over poses element and load
        XMLElement posesXML = anElement.getElement("Poses");
        for (XMLElement poseXML : posesXML.getElements()) {
            PuppetPose pose = new PuppetPose().fromXML(anArchiver, poseXML);
            _poses.add(pose);
        }

        // Iterate over moves element and load
        XMLElement movesXML = anElement.getElement("Moves");
        if (movesXML == null) movesXML = anElement.getElement("Steps"); // Can go soon
        for (XMLElement moveXML : movesXML.getElements()) {
            PuppetMove move = new PuppetMove().fromXML(this, moveXML);
            _moves.add(move);
        }

        // Can go soon
        if (getMoveCount() == 0)
            for (PuppetPose pose : _poses) addMoveForPoseAndTime(pose, 500);

        // Return this
        return this;
    }

}