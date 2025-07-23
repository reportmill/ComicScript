package puppets;
import puppets.ORAReader.Layer;
import puppets.ORAReader.Stack;
import snap.util.*;

/**
 * A Puppet subclass that reads from ORA (OpenRaster) file.
 */
public class ORAPuppet extends Puppet {

    // The stack of layers
    private Stack _stack;

    // The stack of body part layers
    private Stack _bodyStack;

    // The stack of layers
    private Stack _jointStack;

    /**
     * Constructor.
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
        String name = FilePathUtils.getFilename(aPath);
        if (name.startsWith("CT")) name = name.substring(2);
        setName(name);

        // Get stack, body stack and joint stack
        _stack = rdr.readFile(aPath);
        _bodyStack = (Stack) getLayerForName("RL_Image");
        _jointStack = (Stack) getLayerForName("RL_Bone_Human");
    }

    /**
     * Returns the part for given name.
     */
    protected PuppetPart createPart(String aName)
    {
        String lname = getLayerNameForPuppetName(aName);
        Layer layer = getLayerForPartName(lname);
        if (layer == null)
            return null;
        return new ORAPart(aName, layer);
    }

    /**
     * Returns the joint for given name.
     */
    protected PuppetJoint createJoint(String aName)
    {
        String lname = getLayerNameForPuppetName(aName);
        Layer layer = getLayerForJointName(lname);
        if (layer == null)
            return null;
        return new PuppetJoint(aName, layer.x, layer.y);
    }

    /**
     * Returns layer name for puppet part name.
     */
    String getLayerNameForPuppetName(String aName)
    {
        return switch (aName) {

            // Parts
            case PuppetSchema.Torso -> "Hip";
            case PuppetSchema.Head -> "RL_TalkingHead";
            case PuppetSchema.RArm -> "RArm";
            case PuppetSchema.RArmTop -> "RArmTop";
            case PuppetSchema.RArmBtm -> "RArmBtm";
            case PuppetSchema.RHand -> "RHand";
            case PuppetSchema.RLeg -> "RThigh";
            case PuppetSchema.RLegTop -> "RLegTop";
            case PuppetSchema.RLegBtm -> "RLegBtm";
            case PuppetSchema.RFoot -> "RFoot";
            case PuppetSchema.LArm -> "LArm";
            case PuppetSchema.LArmTop -> "LArmTop";
            case PuppetSchema.LArmBtm -> "LArmBtm";
            case PuppetSchema.LHand -> "LHand";
            case PuppetSchema.LLeg -> "LThigh";
            case PuppetSchema.LLegTop -> "LLegTop";
            case PuppetSchema.LLegBtm -> "LLegBtm";
            case PuppetSchema.LFoot -> "LFoot";

            // Joints
            case PuppetSchema.Anchor_Joint -> "ObjectPivot";
            case PuppetSchema.Head_Joint -> "Head";
            case PuppetSchema.HeadTop_Joint -> "Head_Nub";
            case PuppetSchema.RArm_Joint -> "RArm";
            case PuppetSchema.RArmMid_Joint -> "RForearm";
            case PuppetSchema.RHand_Joint -> "RHand";
            case PuppetSchema.RHandEnd_Joint -> "RHand_Nub";
            case PuppetSchema.RLeg_Joint -> "RThigh";
            case PuppetSchema.RLegMid_Joint -> "RShank";
            case PuppetSchema.RFoot_Joint -> "RFoot";
            case PuppetSchema.RFootEnd_Joint -> "RToe";
            case PuppetSchema.LArm_Joint -> "LArm";
            case PuppetSchema.LArmMid_Joint -> "LForearm";
            case PuppetSchema.LHand_Joint -> "LHand";
            case PuppetSchema.LHandEnd_Joint -> "LHand_Nub";
            case PuppetSchema.LLeg_Joint -> "LThigh";
            case PuppetSchema.LLegMid_Joint -> "LShank";
            case PuppetSchema.LFoot_Joint -> "LFoot";
            case PuppetSchema.LFootEnd_Joint -> "LToe";

            // Failure
            default -> {
                System.err.println("ORAPuppet.getLayerNameForPuppetName: failed for " + aName);
                yield null;
            }
        };
    }

    /**
     * Returns the layer for given name.
     */
    public Layer getLayerForName(String aName)  { return getLayer(_stack, aName); }

    /**
     * Returns the layer for given name.
     */
    private Layer getLayer(Layer aLayer, String aName)
    {
        if (aLayer.name != null && aLayer.name.equals(aName))
            return aLayer;
        if (aLayer instanceof Stack stack) {
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
    public Layer getLayerForJointName(String aName)  { return _jointStack.getLayer(aName); }

    /**
     * A PuppetPart subclass for ORAPuppet.
     */
    private static class ORAPart extends PuppetPart {

        private Layer _lyr;

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
        protected snap.gfx.Image getImageImpl()  { return _lyr.getImage(); }

        /**
         * Returns the images that need to be loaded for this part.
         */
        protected Loadable getLoadable()  { return _lyr.getLoadable(); }
    }
}