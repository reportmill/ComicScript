package puppets.puppet;

import puppets.puppet.ORAReader.Layer;
import puppets.puppet.ORAReader.Stack;
import snap.util.*;

/**
 * A Puppet subclass that reads from ORA (OpenRaster) file.
 */
public class ORAPuppet extends Puppet {

    // The stack of layers
    Stack _stack;

    // The stack of body part layers
    Stack _bodyStack;

    // The stack of layers
    Stack _jointStack;

    /**
     * Creates a PuppetView.
     */
    public ORAPuppet(String aSource)
    {
        setSource(aSource);
    }

    /**
     * Sets the source.
     */
    public void setSource(String aPath)
    {
        super.setSource(aPath);
        ORAReader rdr = new ORAReader();

        // Get name
        String name = FilePathUtils.getFileName(aPath);
        if (name.startsWith("CT")) name = name.substring(2);
        setName(name);

        // Get stack, body stack and joint stack
        _stack = rdr.readFile(aPath);
        _bodyStack = (Stack) getLayer("RL_Image");
        _jointStack = (Stack) getLayer("RL_Bone_Human");
    }

    /**
     * Returns the part for given name.
     */
    protected PuppetPart createPart(String aName)
    {
        String lname = getLayerNameForPuppetName(aName);
        Layer layer = getLayerForPartName(lname);
        if (layer == null) return null;
        return new ORAPart(aName, layer);
    }

    /**
     * Returns the joint for given name.
     */
    protected PuppetJoint createJoint(String aName)
    {
        String lname = getLayerNameForPuppetName(aName);
        Layer layer = getLayerForJointName(lname);
        if (layer == null) return null;
        return new PuppetJoint(aName, layer.x, layer.y);
    }

    /**
     * Returns layer name for puppet part name.
     */
    String getLayerNameForPuppetName(String aName)
    {
        switch (aName) {

            // Parts
            case PuppetSchema.Torso:
                return "Hip";
            case PuppetSchema.Head:
                return "RL_TalkingHead";
            case PuppetSchema.RArm:
                return "RArm";
            case PuppetSchema.RArmTop:
                return "RArmTop";
            case PuppetSchema.RArmBtm:
                return "RArmBtm";
            case PuppetSchema.RHand:
                return "RHand";
            case PuppetSchema.RLeg:
                return "RThigh";
            case PuppetSchema.RLegTop:
                return "RLegTop";
            case PuppetSchema.RLegBtm:
                return "RLegBtm";
            case PuppetSchema.RFoot:
                return "RFoot";
            case PuppetSchema.LArm:
                return "LArm";
            case PuppetSchema.LArmTop:
                return "LArmTop";
            case PuppetSchema.LArmBtm:
                return "LArmBtm";
            case PuppetSchema.LHand:
                return "LHand";
            case PuppetSchema.LLeg:
                return "LThigh";
            case PuppetSchema.LLegTop:
                return "LLegTop";
            case PuppetSchema.LLegBtm:
                return "LLegBtm";
            case PuppetSchema.LFoot:
                return "LFoot";

            // Joints
            case PuppetSchema.Anchor_Joint:
                return "ObjectPivot";
            case PuppetSchema.Head_Joint:
                return "Head";
            case PuppetSchema.HeadTop_Joint:
                return "Head_Nub";
            case PuppetSchema.RArm_Joint:
                return "RArm";
            case PuppetSchema.RArmMid_Joint:
                return "RForearm";
            case PuppetSchema.RHand_Joint:
                return "RHand";
            case PuppetSchema.RHandEnd_Joint:
                return "RHand_Nub";
            case PuppetSchema.RLeg_Joint:
                return "RThigh";
            case PuppetSchema.RLegMid_Joint:
                return "RShank";
            case PuppetSchema.RFoot_Joint:
                return "RFoot";
            case PuppetSchema.RFootEnd_Joint:
                return "RToe";
            case PuppetSchema.LArm_Joint:
                return "LArm";
            case PuppetSchema.LArmMid_Joint:
                return "LForearm";
            case PuppetSchema.LHand_Joint:
                return "LHand";
            case PuppetSchema.LHandEnd_Joint:
                return "LHand_Nub";
            case PuppetSchema.LLeg_Joint:
                return "LThigh";
            case PuppetSchema.LLegMid_Joint:
                return "LShank";
            case PuppetSchema.LFoot_Joint:
                return "LFoot";
            case PuppetSchema.LFootEnd_Joint:
                return "LToe";

            // Failure
            default:
                System.err.println("ORAPuppet.getLayerNameForPuppetName: failed for " + aName);
                return null;
        }
    }

    /**
     * Returns the layer for given name.
     */
    public Layer getLayer(String aName)
    {
        return getLayer(_stack, aName);
    }

    /**
     * Returns the layer for given name.
     */
    Layer getLayer(Layer aLayer, String aName)
    {
        if (aLayer.name != null && aLayer.name.equals(aName))
            return aLayer;
        if (aLayer instanceof Stack) {
            Stack stack = (Stack) aLayer;
            for (Layer l : stack.entries)
                if (getLayer(l, aName) != null)
                    return getLayer(l, aName);
        }
        return null;
    }

    /**
     * Returns the layer for given part name.
     */
    public Layer getLayerForPartName(String aName)
    {
        if (aName.equals("RL_TalkingHead"))
            return _stack.getLayer(aName);
        return _bodyStack.getLayer(aName);
    }

    /**
     * Returns the layer for given joint name.
     */
    public Layer getLayerForJointName(String aName)
    {
        return _jointStack.getLayer(aName);
    }

    /**
     * A PuppetPart subclass for ORAPuppet.
     */
    private class ORAPart extends PuppetPart {

        Layer _lyr;

        /**
         * Creates an ORAPart for given layer.
         */
        public ORAPart(String aName, Layer aLayer)
        {
            _name = aName;
            _lyr = aLayer;
            _x = aLayer.x;
            _y = aLayer.y;
        }

        /**
         * Returns the image.
         */
        protected snap.gfx.Image getImageImpl()
        {
            return _lyr.getImage();
        }

        /**
         * Returns the images that need to be loaded for this part.
         */
        protected Loadable getLoadable()
        {
            return _lyr.getLoadable();
        }
    }

}