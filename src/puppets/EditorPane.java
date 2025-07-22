package puppets;

import snap.geom.HPos;
import snap.geom.Point;
import snap.gfx.*;
import snap.view.*;
import snap.viewx.DialogBox;

/**
 * A class to manage editing of Puppet.
 */
public class EditorPane extends ViewOwner {

    // The AppPane
    PuppetsPane _puppetsPane;

    // The Puppet
    Puppet _puppet;

    // The ListView for Puppets
    ListView<String> _pupList;

    // The ListView for parts/joints
    ListView<String> _partsList;

    // The ListView for joints
    ListView<String> _jointsList;

    // The PuppetView
    PuppetView _puppetView;

    // The selected part name
    String _selName, _dragName;

    // Constants
    static Color SELECT_COLOR = Color.get("#039ed3");
    static Effect SELECT_EFFECT = new ShadowEffect(8, SELECT_COLOR, 0, 0);

    /**
     * Creates a EditorPane.
     */
    public EditorPane(PuppetsPane aAP)
    {
        _puppetsPane = aAP;
    }

    /**
     * Opens a puppet file.
     */
    public void open(Object aSource)
    {
        Puppet puppet = Puppet.getPuppetForSource(aSource);
        setPuppet(puppet);
    }

    /**
     * Returns the puppet.
     */
    public Puppet getPuppet()
    {
        return _puppet;
    }

    /**
     * Sets the puppet.
     */
    public void setPuppet(Puppet aPuppet)
    {
        _puppet = aPuppet;

        if (!isUISet()) return;

        _puppetView.setPuppet(aPuppet);
    }

    /**
     * Returns the puppet part at X/Y.
     */
    public PuppetPart getPuppetPartAtPoint(double aX, double aY)
    {
        View hitView = ViewUtils.getChildAt(_puppetView, aX, aY);
        String name = hitView != null ? hitView.getName() : null;
        PuppetPart part = isPartName(name) ? getPuppet().getPart(name) : null;
        return part;
    }

    /**
     * Returns the selected part.
     */
    public String getSelName()
    {
        return _selName;
    }

    /**
     * Sets the selected part name.
     */
    public void setSelName(String aName)
    {
        // Set name and update view effect
        if (getSelView() != null) getSelView().setEffect(null);
        _selName = aName;
        if (getSelView() != null) getSelView().setEffect(SELECT_EFFECT);

        // Update PartsList/JointsList selection
        _partsList.setSelItem(aName);
        _jointsList.setSelItem(aName);
    }

    /**
     * Returns the selected joint name.
     */
    public String getSelPartName()
    {
        return isPartName(_selName) ? _selName : null;
    }

    /**
     * Returns the selected part.
     */
    public PuppetPart getSelPart()
    {
        String name = getSelPartName();
        return name != null ? getPuppet().getPart(name) : null;
    }

    /**
     * Returns the selected joint name.
     */
    public String getSelJointName()
    {
        return isJointName(_selName) ? _selName : null;
    }

    /**
     * Returns the selected joint.
     */
    public PuppetJoint getSelJoint()
    {
        String name = getSelJointName();
        return name != null ? getPuppet().getJoint(name) : null;
    }

    /**
     * Returns whether name is joint.
     */
    private boolean isPartName(String aName)
    {
        return aName != null && !aName.endsWith("Joint");
    }

    private boolean isJointName(String aName)
    {
        return aName != null && aName.endsWith("Joint");
    }

    /**
     * Returns the selected view.
     */
    private View getSelView()
    {
        return _selName != null ? _puppetView.getChildForName(_selName) : null;
    }

    private View getDragView()
    {
        return _dragName != null ? _puppetView.getChildForName(_dragName) : null;
    }

    /**
     * Returns the drag part.
     */
    public String getDragName()
    {
        return _dragName;
    }

    /**
     * Sets the selected layer.
     */
    void setDragName(String aName)
    {
        if (getDragView() != null) getDragView().setEffect(null);
        _dragName = aName;
        if (getDragView() != null) getDragView().setEffect(SELECT_EFFECT);

        if (_dragName != null && getSelView() != null) getSelView().setEffect(null);
        else if (_dragName == null && getSelView() != null) getSelView().setEffect(SELECT_EFFECT);
    }

    /**
     * Initialize UI.
     */
    protected void initUI()
    {
        // Configure PuppetList
        _pupList = getView("PuppetList", ListView.class);
        _pupList.setItems(PuppetUtils.getPuppetFile().getPuppetNames());

        // Get/configure PupView
        Puppet puppet = getPuppet();
        _puppetView = new PuppetView(puppet);
        _puppetView.setBorder(Color.LIGHTGRAY, 1);
        _puppetView.addEventHandler(this::handlePuppetViewMousePressed, MousePress);
        _puppetView.addEventHandler(this::handlePuppetViewDragEvent, DragEvents);

        // Get PuppetBox and add PupView
        BoxView pupBox = getView("PuppetBox", BoxView.class);
        pupBox.setContent(_puppetView);

        // Get/configure PartsList
        String[] partNames = puppet.getSchema().getPartNamesNaturalOrder();
        _partsList = getView("PartsList", ListView.class);
        _partsList.setItems(partNames);

        // Configure JointsLabel
        Label label = getView("JointsLabel", Label.class);
        Button jntsBtn = new Button("Show");
        jntsBtn.setFont(Font.Arial10);
        jntsBtn.setLeanX(HPos.RIGHT);
        jntsBtn.addEventHandler(e -> toggleShowJointsList(), View.Action);
        label.setGraphicAfter(jntsBtn);

        // Get/configure JointsList
        String[] jointNames = puppet.getSchema().getJointNamesNaturalOrder();
        _jointsList = getView("JointsList", ListView.class);
        _jointsList.setPrefHeight(1);
        _jointsList.setVisible(false);
        _jointsList.setItems(jointNames);

        // Configure ScaleSpinner
        Spinner<?> scaleSpinner = getView("ScaleSpinner", Spinner.class);
        scaleSpinner.setStep(.1);
    }

    /**
     * Updates the UI controls from currently selected page.
     */
    public void resetUI()
    {
        // Update PupList selection, PartsList selection
        _pupList.setSelItem(getPuppet().getName());
        _partsList.setSelItem(getSelName());
        _jointsList.setSelItem(getSelName());

        // Update ScaleSpinner
        PuppetPart part = getSelPart();
        setViewValue("ScaleSpinner", part != null ? part.getScale() : 1);
        setViewEnabled("ScaleSpinner", part != null);
    }

    /**
     * Responds to UI.
     */
    public void respondUI(ViewEvent anEvent)
    {
        // Handle PupList
        if (anEvent.equals("PuppetList")) {
            String name = anEvent.getStringValue();
            Puppet puppet = PuppetUtils.getPuppetFile().getPuppetForName(name);
            setPuppet(puppet);
        }

        // Handle AddPuppetButton
        if (anEvent.equals("AddPuppetButton")) {
            String name = DialogBox.showInputDialog(getUI(), "Add Puppet", "Enter Puppet Name:", "Untitled");
            if (name == null || name.isEmpty())
                return;
            Puppet newPup = new Puppet(getPuppet());
            newPup.setName(name);
            PuppetUtils.getPuppetFile().addPuppet(newPup);
            _pupList.setItems(PuppetUtils.getPuppetFile().getPuppetNames());
            setPuppet(newPup);
        }

        // Handle PartsList
        if (anEvent.equals("PartsList"))
            setSelName(_partsList.getSelItem());

        // Handle JointsList
        if (anEvent.equals("JointsList"))
            setSelName(_jointsList.getSelItem());

        // Handle ScaleSpinner
        if (anEvent.equals("ScaleSpinner")) {
            PuppetPart part = getSelPart();
            if (part == null) return;
            PuppetPart part2 = part.cloneForScale(anEvent.getFloatValue());
            getPuppet().setPart(part2);
            _puppetView.rebuildChildren();
        }

        // Handle SaveButton
        if (anEvent.equals("SaveButton"))
            getPuppet().save();
    }

    /**
     * Handles PuppetView MousePressed events.
     */
    private void handlePuppetViewMousePressed(ViewEvent anEvent)
    {
        Point pnt = anEvent.getPoint();
        View hitView = ViewUtils.getChildAt(_puppetView, pnt.x, pnt.y);
        if (hitView != null)
            setSelName(hitView.getName());
    }

    /**
     * Handles PuppetView Drag events.
     */
    private void handlePuppetViewDragEvent(ViewEvent anEvent)
    {
        // Handle drag over
        if (anEvent.isDragOver()) {
            Clipboard cb = anEvent.getClipboard();
            if (!cb.hasFiles())
                return;
            if (getPuppetPartAtPoint(anEvent.getX(), anEvent.getY()) == null)
                return;
            anEvent.acceptDrag();
            PuppetPart part = getPuppetPartAtPoint(anEvent.getX(), anEvent.getY());
            setDragName(part != null ? part.getName() : null);
        }

        // Handle drop
        if (anEvent.isDragDrop()) {
            Clipboard cb = anEvent.getClipboard();
            if (!cb.hasFiles())
                return;
            if (getPuppetPartAtPoint(anEvent.getX(), anEvent.getY()) == null)
                return;
            anEvent.acceptDrag();
            ClipboardData cdata = cb.getFiles().get(0);
            dropFile(anEvent, cdata);
            anEvent.dropComplete();
        }

        // Handle drop done
        if (anEvent.isDragExit())
            setDragName(null);
    }

    /**
     * Called when ShowJointsListButton pressed.
     */
    private void toggleShowJointsList()
    {
        // Update button label
        boolean visible = _jointsList.isVisible();
        getView("JointsLabel", Label.class).getGraphicAfter().setText(visible ? "Show" : "Hide");
        _jointsList.setClipToBounds(visible); // Shouldn't need this, but focus ring can leave artifacts
        _jointsList.getParent().repaint(); // Or this

        // If visible, animate close
        if (visible) {
            if (_jointsList.isFocused())
                _puppetView.requestFocus();
            ViewAnim anim = _jointsList.getAnimCleared(500);
            anim.setPrefHeight(1);
            anim.setOnFinish(() -> _jointsList.setVisible(false));
            anim.play();
        }

        // If hidden, animate open
        else {
            _jointsList.setVisible(true);
            _jointsList.getAnimCleared(500).setPrefHeight(160).play();
        }
    }

    /**
     * Called to handle a file drop on the editor.
     */
    private void dropFile(ViewEvent anEvent, ClipboardData aFile)
    {
        // If file not loaded, come back when it is
        if (!aFile.isLoaded()) {
            aFile.addLoadListener(f -> dropFile(anEvent, aFile));
            return;
        }

        // Get path and extension (set to empty string if null)
        String ext = aFile.getFileType();
        if (ext == null) return;
        ext = ext.toLowerCase();
        if (!Image.canRead(ext)) return;

        // Get image
        Object imgSrc = aFile.getSourceURL() != null ? aFile.getSourceURL() : aFile.getBytes();
        Image img = Image.getImageForSource(imgSrc);
        dropImage(anEvent, img);
    }

    /**
     * Called to handle a file drop on the editor.
     */
    private void dropImage(ViewEvent anEvent, Image anImage)
    {
        // If file not loaded, come back when it is
        if (!anImage.isLoaded()) {
            anImage.addLoadListener(() -> dropImage(anEvent, anImage));
            return;
        }

        // Set new image for puppet part
        PuppetPart part = getPuppetPartAtPoint(anEvent.getX(), anEvent.getY());
        if (part == null) return;
        PuppetPart part2 = part.cloneForImage(anImage);
        getPuppet().setPart(part2);
        _puppetView.rebuildChildren();
        runLater(() -> setSelName(part2.getName()));
    }

}