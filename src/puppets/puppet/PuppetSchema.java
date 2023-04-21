package puppets.puppet;

import java.util.*;

/**
 * A class to represent the parts and joints on a puppet.
 */
public class PuppetSchema {

    // Constants for human parts
    public static final String Torso = "Torso";
    public static final String Head = "Head";
    public static final String RArm = "RArm";
    public static final String RArmTop = "RArmTop";
    public static final String RArmBtm = "RArmBtm";
    public static final String RHand = "RHand";
    public static final String RLeg = "RLeg";
    public static final String RLegTop = "RLegTop";
    public static final String RLegBtm = "RLegBtm";
    public static final String RFoot = "RFoot";
    public static final String LArm = "LArm";
    public static final String LArmTop = "LArmTop";
    public static final String LArmBtm = "LArmBtm";
    public static final String LHand = "LHand";
    public static final String LLeg = "LLeg";
    public static final String LLegTop = "LLegTop";
    public static final String LLegBtm = "LLegBtm";
    public static final String LFoot = "LFoot";

    // Constants for human joints
    public static final String Anchor_Joint = "AnchorJoint";
    public static final String Head_Joint = "HeadJoint";
    public static final String HeadTop_Joint = "HeadTopJoint";
    public static final String RArm_Joint = "RArmJoint";
    public static final String RArmMid_Joint = "RArmMidJoint";
    public static final String RHand_Joint = "RHandJoint";
    public static final String RHandEnd_Joint = "RHandEndJoint";
    public static final String RLeg_Joint = "RLegJoint";
    public static final String RLegMid_Joint = "RLegMidJoint";
    public static final String RFoot_Joint = "RFoot_Joint";
    public static final String RFootEnd_Joint = "RFootEndJoint";
    public static final String LArm_Joint = "LArmJoint";
    public static final String LArmMid_Joint = "LArmMidJoint";
    public static final String LHand_Joint = "LHandJoint";
    public static final String LHandEnd_Joint = "LHandEndJoint";
    public static final String LLeg_Joint = "LLegJoint";
    public static final String LLegMid_Joint = "LLegMidJoint";
    public static final String LFoot_Joint = "LFoot_Joint";
    public static final String LFootEnd_Joint = "LFootEndJoint";

    /**
     * Returns the part names in paint order.
     */
    public String[] getNodeNames()
    {
        List<String> names = new ArrayList();
        Collections.addAll(names, getPartNames());
        Collections.addAll(names, getJointNames());
        return names.toArray(new String[names.size()]);
    }

    /**
     * Returns the part names in paint order.
     */
    public String[] getPartNames()
    {
        return new String[]{
                RArmTop, RArmBtm, RHand, RLegTop, RLegBtm, RFoot, Torso, Head,
                LLegTop, LLegBtm, LFoot, LArmTop, LArmBtm, LHand};
    }

    /**
     * Returns the joint names.
     */
    public String[] getJointNames()
    {
        return new String[]{Anchor_Joint, Head_Joint, HeadTop_Joint,
                RArm_Joint, RArmMid_Joint, RHand_Joint, RHandEnd_Joint,
                RLeg_Joint, RLegMid_Joint, RFoot_Joint, RFootEnd_Joint,
                LArm_Joint, LArmMid_Joint, LHand_Joint, LHandEnd_Joint,
                LLeg_Joint, LLegMid_Joint, LFoot_Joint, LFootEnd_Joint};
    }

    /**
     * Returns the part names in paint order.
     */
    public String[] getPartNamesNaturalOrder()
    {
        return new String[]{Head, Torso, RArmTop, RArmBtm, RHand, LArmTop, LArmBtm, LHand,
                RLegTop, RLegBtm, RFoot, LLegTop, LLegBtm, LFoot};
    }

    /**
     * Returns the joint names in paint order.
     */
    public String[] getJointNamesNaturalOrder()
    {
        return new String[]{Head_Joint, HeadTop_Joint,
                RArm_Joint, RArmMid_Joint, RHand_Joint, RHandEnd_Joint,
                LArm_Joint, LArmMid_Joint, LHand_Joint, LHandEnd_Joint,
                RLeg_Joint, RLegMid_Joint, RFoot_Joint, RFootEnd_Joint,
                LLeg_Joint, LLegMid_Joint, LFoot_Joint, LFootEnd_Joint, Anchor_Joint};
    }

    /**
     * Returns the root joint names.
     */
    public String[] getRootJointNames()
    {
        return new String[]{Head_Joint, RArm_Joint, RLeg_Joint, LArm_Joint, LLeg_Joint};
    }

    /**
     * Returns the puppet joint names that define a pose for puppet.
     */
    public String[] getPoseKeys()
    {
        return new String[]{HeadTop_Joint, Head_Joint,
                RArm_Joint, RArmMid_Joint, RHand_Joint, RHandEnd_Joint,
                RLeg_Joint, RLegMid_Joint, RFoot_Joint, RFootEnd_Joint,
                LArm_Joint, LArmMid_Joint, LHand_Joint, LHandEnd_Joint,
                LLeg_Joint, LLegMid_Joint, LFoot_Joint, LFootEnd_Joint};
    }

    /**
     * Returns names of parts linked to given joint name.
     */
    public String[] getLinkNamesForJoint(String aName)
    {
        switch (aName) {
            case Head_Joint:
                return new String[]{Head, Torso};
            case HeadTop_Joint:
                return new String[]{Head};
            case RArm_Joint:
                return new String[]{Torso, RArmTop};
            case RArmMid_Joint:
                return new String[]{RArmTop, RArmBtm};
            case RHand_Joint:
                return new String[]{RArmBtm, RHand};
            case RHandEnd_Joint:
                return new String[]{RHand};
            case RLeg_Joint:
                return new String[]{Torso, RLegTop};
            case RLegMid_Joint:
                return new String[]{RLegTop, RLegBtm};
            case RFoot_Joint:
                return new String[]{RLegBtm, RFoot};
            case RFootEnd_Joint:
                return new String[]{RFoot};
            case LArm_Joint:
                return new String[]{Torso, LArmTop};
            case LArmMid_Joint:
                return new String[]{LArmTop, LArmBtm};
            case LHand_Joint:
                return new String[]{LArmBtm, LHand};
            case LHandEnd_Joint:
                return new String[]{LHand};
            case LLeg_Joint:
                return new String[]{Torso, LLegTop};
            case LLegMid_Joint:
                return new String[]{LLegTop, LLegBtm};
            case LFoot_Joint:
                return new String[]{LLegBtm, LFoot};
            case LFootEnd_Joint:
                return new String[]{LFoot};
            default:
                return new String[0];
        }
    }

    /**
     * Returns names of parts linked to given joint name.
     */
    public String[] getOuterJointNamesForPartName(String aName)
    {
        switch (aName) {
            case RArmTop:
                return new String[]{RArmMid_Joint, RHand_Joint};
            case RArmBtm:
                return new String[]{RHand_Joint};
            case RLegTop:
                return new String[]{RLegMid_Joint, RFoot_Joint};
            case RLegBtm:
                return new String[]{RFoot_Joint};
            case LArmTop:
                return new String[]{LArmMid_Joint, LHand_Joint};
            case LArmBtm:
                return new String[]{LHand_Joint};
            case LLegTop:
                return new String[]{LLegMid_Joint, LFoot_Joint};
            case LLegBtm:
                return new String[]{LFoot_Joint};
            default:
                return new String[0];
        }
    }

    /**
     * Returns names of parts linked to given joint name.
     */
    public String getNextJointNameForName(String aName)
    {
        switch (aName) {
            case Head_Joint:
            case Head:
                return HeadTop_Joint;
            case RArm_Joint:
            case RArm:
            case RArmTop:
                return RArmMid_Joint;
            case RArmMid_Joint:
            case RArmBtm:
                return RHand_Joint;
            case RHand_Joint:
            case RHand:
                return RHandEnd_Joint;
            case RLeg_Joint:
            case RLeg:
            case RLegTop:
                return RLegMid_Joint;
            case RLegMid_Joint:
            case RLegBtm:
                return RFoot_Joint;
            case RFoot_Joint:
            case RFoot:
                return RFootEnd_Joint;
            case LArm_Joint:
            case LArm:
            case LArmTop:
                return LArmMid_Joint;
            case LArmMid_Joint:
            case LArmBtm:
                return LHand_Joint;
            case LHand_Joint:
            case LHand:
                return LHandEnd_Joint;
            case LLeg_Joint:
            case LLeg:
            case LLegTop:
                return LLegMid_Joint;
            case LLegMid_Joint:
            case LLegBtm:
                return LFoot_Joint;
            case LFoot_Joint:
            case LFoot:
                return LFootEnd_Joint;
            default:
                return null;
        }
    }

    /**
     * Returns whether given name is marker name.
     */
    public boolean isMarkerName(String aName)
    {
        return aName.endsWith("Joint") && getNextJointNameForName(aName) == null;
    }

}