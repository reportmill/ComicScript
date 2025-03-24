package puppets.puppet;
import java.util.*;
import snap.geom.Arc;
import snap.geom.Shape;
import snap.geom.Transform;
import snap.gfx.*;
import snap.util.*;
import snap.web.WebURL;

/**
 * A class to handle miscellaneous functionality for Puppet.
 */
public class PuppetUtils {

    // Constants
    public static String ROOT = "/Users/jeff/Dev/ComicLib/";

    // The PuppetFile
    private static PuppetFile _puppetFile;

    // The ActionFile
    private static ActionFile _actionFile;

    // The marker image
    private static Image _markerImg, _anchorImage;

    /**
     * Initialization.
     */
    static {
        if (SnapEnv.isTeaVM) ROOT = "https://reportmill.com/ComicLib/";
        System.out.println("Root: " + ROOT);
    }

    /**
     * Returns the PuppetFile.
     */
    public static PuppetFile getPuppetFile()
    {
        return _puppetFile != null ? _puppetFile : (_puppetFile = new PuppetFile());
    }

    /**
     * Returns the ActionFile.
     */
    public static ActionFile getActionFile()
    {
        return _actionFile != null ? _actionFile : (_actionFile = new ActionFile());
    }

    /**
     * Returns the flipped image.
     */
    public static Image getImageFlipped(Image anImage)
    {
        int w = (int) Math.round(anImage.getWidth()), h = (int) Math.round(anImage.getHeight());
        Image img = Image.getImageForSize(w, h, anImage.hasAlpha());
        Painter pntr = img.getPainter();
        Transform xfm = new Transform(w / 2, h / 2);
        xfm.scale(-1, 1);
        xfm.translate(-w / 2, -h / 2);
        pntr.transform(xfm);
        pntr.drawImage(anImage, 0, 0);
        return img;
    }

    /**
     * Returns the flipped image.
     */
    public static Image getImagesFlipped(Image anImage)
    {
        // Get image set (if null, just return flipped image)
        ImageSet iset = anImage.getImageSet();
        if (iset == null)
            return getImageFlipped(anImage);

        int count = iset.getCount();
        List<Image> imgs2 = new ArrayList(count);
        for (int i = 0; i < count; i++) {
            Image img = iset.getImage(i);
            imgs2.add(getImageFlipped(img));
        }
        ImageSet iset2 = new ImageSet(imgs2);
        return iset2.getImage(0);
    }

    /**
     * Returns the joint/marker image.
     */
    public static Image getMarkerImage()
    {
        if (_markerImg != null) return _markerImg;
        double s = 23;
        Image img = Image.getImageForSize(s, s, true);
        Painter pntr = img.getPainter();
        Shape arc = new Arc(0, 0, s, s, 0, 360);
        Color fill = new Color("#E49956D0"); // Yellow: #EFD969E0, Orange: #E49956E0
        pntr.setColor(fill);
        pntr.fill(arc);
        pntr.setColor(fill.darker().darker());
        pntr.setStroke(Stroke.getStroke(.5));
        pntr.draw(arc);
        pntr.setColor(Color.BLACK);
        pntr.setStroke(Stroke.Stroke2);
        pntr.drawLine(5, s / 2, s - 5, s / 2);
        pntr.drawLine(s / 2, 5, s / 2, s - 5);
        return _markerImg = img;
    }

    /**
     * Returns the anchor image.
     */
    public static Image getAnchorImage()
    {
        if (_anchorImage != null) return _anchorImage;
        double s = 90;
        Image img = Image.getImageForSize((int) s, (int) s, true);
        Painter pntr = img.getPainter();
        pntr.setStroke(Stroke.getStroke(4));
        Color red = new Color("#D54438"), blue = new Color("#4B77B8");
        pntr.setColor(red);
        pntr.drawLine(5, s / 2, s - 5, s / 2);
        pntr.setColor(blue);
        pntr.drawLine(s / 2, 5, s / 2, s - 5);
        return _anchorImage = img;
    }

    /**
     * A class to manage list of Puppets.
     */
    public static class PuppetFile {

        // A List of Puppets
        private List<PuppetEntry> _pupEnts;

        // The puppet names
        private String _names[];

        // The file path
        private String _path = ROOT + "Puppets.xml";

        /**
         * Returns the list of puppet entries.
         */
        public List<PuppetEntry> getEntries()
        {
            return _pupEnts != null ? _pupEnts : (_pupEnts = readPuppets());
        }

        /**
         * Returns the puppet names.
         */
        public String[] getPuppetNames()
        {
            if (_names != null) return _names;
            _names = new String[getPuppetCount()];
            for (int i = 0; i < getPuppetCount(); i++) _names[i] = getEntry(i).getName();
            return _names;
        }

        /**
         * Returns the number of puppets.
         */
        public int getPuppetCount()
        {
            return getEntries().size();
        }

        /**
         * Returns the individual puppet at given index.
         */
        public Puppet getPuppet(int anIndex)
        {
            return getEntry(anIndex).getPuppet();
        }

        /**
         * Returns the individual PuppetEntry at given index.
         */
        public PuppetEntry getEntry(int anIndex)
        {
            return getEntries().get(anIndex);
        }

        /**
         * Returns the puppet with given name.
         */
        public Puppet getPuppetForName(String aName)
        {
            for (PuppetEntry pupEnt : getEntries())
                if (pupEnt.getName().equalsIgnoreCase(aName))
                    return pupEnt.getPuppet();
            return null;
        }

        /**
         * Adds an puppet.
         */
        public void addPuppet(Puppet aPuppet)
        {
            addPuppet(aPuppet, getPuppetCount());
        }

        /**
         * Adds an puppet at given index.
         */
        public void addPuppet(Puppet aPuppet, int anIndex)
        {
            PuppetEntry pe = new PuppetEntry(aPuppet);
            getEntries().add(anIndex, pe);
            _names = null;
            savePuppets();
        }

        /**
         * Removes an puppet.
         */
        public Puppet removePuppet(int anIndex)
        {
            PuppetEntry pe = getEntries().remove(anIndex);
            _names = null;
            savePuppets();
            return pe.getPuppet();
        }

        /**
         * Reads puppets from file.
         */
        protected List<PuppetEntry> readPuppets()
        {
            // Get file string as XMLElement
            WebURL url = WebURL.getURL(_path);
            String fileStr = url.getText();
            if (fileStr == null) return new ArrayList();
            XMLElement puppetsXML = XMLElement.readFromXMLSource(url);

            // Iterate over actions
            List<PuppetEntry> puppets = new ArrayList();
            for (XMLElement pupXML : puppetsXML.getElements()) {
                PuppetEntry pe = new PuppetEntry().fromXML(null, pupXML);
                puppets.add(pe);
            }

            // Return puppets
            return puppets;
        }

        /**
         * Saves actions to file.
         */
        public void savePuppets()
        {
            if (SnapEnv.isTeaVM) return;

            // Create element for puppets and iterate over puppets and add each
            XMLElement puppetsXML = new XMLElement("Puppets");
            for (PuppetEntry pupEnt : getEntries()) {
                XMLElement pupEntXML = pupEnt.toXML(null);
                puppetsXML.add(pupEntXML);
            }

            // Get as bytes and write to file
            byte[] bytes = puppetsXML.getBytes();
            SnapUtils.writeBytes(bytes, _path);
        }
    }

    /**
     * A class to manage a puppet entry in a puppet file.
     */
    public static class PuppetEntry {

        // The name
        String _name;

        // The path
        String _path;

        // The puppet
        Puppet _puppet;

        /**
         * Creates PuppetEntry.
         */
        public PuppetEntry()
        {
        }

        /**
         * Creates PuppetEntry for puppet.
         */
        public PuppetEntry(Puppet aPuppet)
        {
            _name = aPuppet.getName();
            _puppet = aPuppet;
        }

        /**
         * Return name.
         */
        public String getName()
        {
            return _name;
        }

        /**
         * Return path.
         */
        public String getPath()
        {
            return _puppet != null ? _puppet.getSourceRelPath() : _path;
        }

        /**
         * The puppet.
         */
        public Puppet getPuppet()
        {
            if (_puppet != null) return _puppet;

            //if(_name.equals("Man") || _name.equals("Lady")) _puppet = Puppet.getPuppetForSource(_name); else
            _puppet = Puppet.getPuppetForSource(ROOT + _path);
            return _puppet;
        }

        /**
         * XML Archival.
         */
        public XMLElement toXML(XMLArchiver anArchiver)
        {
            XMLElement e = new XMLElement("Puppet");
            e.add("Name", getName());
            e.add("Path", getPath());
            return e;
        }

        /**
         * XML unarchival.
         */
        public PuppetEntry fromXML(XMLArchiver anArchiver, XMLElement anElement)
        {
            _name = anElement.getAttributeValue("Name");
            _path = anElement.getAttributeValue("Path");
            return this;
        }
    }

    /**
     * A class to manage list of Puppet Actions.
     */
    public static class ActionFile {

        // A List of actions
        List<PuppetAction> _actions;

        // The file path
        String _path = ROOT + "HumanActions.xml";

        /**
         * Returns the list of actions.
         */
        public List<PuppetAction> getActions()
        {
            return _actions != null ? _actions : (_actions = loadActions());
        }

        /**
         * Returns the number of actions.
         */
        public int getActionCount()
        {
            return getActions().size();
        }

        /**
         * Returns the individual actions at given index.
         */
        public PuppetAction getAction(int anIndex)
        {
            return getActions().get(anIndex);
        }

        /**
         * Returns the action with given name.
         */
        public PuppetAction getActionForName(String aName)
        {
            for (PuppetAction act : getActions())
                if (act.getName().equalsIgnoreCase(aName))
                    return act;
            return null;
        }

        /**
         * Adds an action.
         */
        public void addAction(PuppetAction anAction)
        {
            addAction(anAction, getActionCount());
        }

        /**
         * Adds an action.
         */
        public void addAction(PuppetAction anAction, int anIndex)
        {
            getActions().add(anIndex, anAction);
            saveActions();
        }

        /**
         * Removes an action.
         */
        public PuppetAction removeAction(int anIndex)
        {
            PuppetAction action = getActions().remove(anIndex);
            saveActions();
            return action;
        }

        /**
         * Loads actions from file.
         */
        protected List<PuppetAction> loadActions()
        {
            // Get file string as XMLElement
            WebURL url = WebURL.getURL(_path);
            String fileStr = url.getText();
            if (fileStr == null) return new ArrayList<>();
            XMLElement actionsXML = XMLElement.readFromXMLSource(url);

            // Iterate over actions
            List<PuppetAction> actions = new ArrayList<>();
            for (XMLElement actionXML : actionsXML.getElements()) {
                PuppetAction action = new PuppetAction().fromXML(null, actionXML);
                actions.add(action);
            }

            // Return actions
            return actions;
        }

        /**
         * Saves actions to file.
         */
        public void saveActions()
        {
            if (SnapEnv.isTeaVM) return;

            // Create element for actions and iterate over actions and add each
            XMLElement actionsXML = new XMLElement("Actions");
            for (PuppetAction action : getActions()) {
                XMLElement actionXML = action.toXML(null);
                actionsXML.add(actionXML);
            }

            // Get as bytes and write to file
            byte[] bytes = actionsXML.getBytes();
            SnapUtils.writeBytes(bytes, _path);
        }
    }
}