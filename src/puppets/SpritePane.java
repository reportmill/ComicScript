package puppets;
import java.util.*;

import snap.geom.Transform;
import snap.view.*;
import snap.gfx.*;

/**
 * A class to manage UI to generate sprites for puppets.
 */
public class SpritePane extends ViewOwner {

    // The AppPane
    private PuppetsPane _puppetsPane;

    // The image view
    private ImageView _imgView;

    // A ListView to show actions
    private ListView<PuppetAction> _actionList;

    // Whether to flip image
    private boolean _flipImage;

    // Whether to loop anim
    private boolean _loopAnim;

    /**
     * Constructor.
     */
    public SpritePane(PuppetsPane puppetsPane)
    {
        _puppetsPane = puppetsPane;
    }

    /**
     * Initialize UI.
     */
    protected void initUI()
    {
        // Create ImageView
        _imgView = new ImageView();

        // Get PuppetBox and add ActionView
        BoxView pupBox = getView("PuppetBox", BoxView.class);
        pupBox.setContent(_imgView);

        // Set ActionList
        _actionList = getView("ActionList", ListView.class);
        _actionList.setItemTextFunction(PuppetAction::getName);
        _actionList.setItems(PuppetUtils.getActionFile().getActions());
        _actionList.setSelIndex(0);

        getUI().addPropChangeListener(pc -> runLater(() -> setSpriteImage()), View.Showing_Prop);
    }

    /**
     * Respond to UI.
     */
    protected void respondUI(ViewEvent anEvent)
    {
        switch (anEvent.getName()) {

            // Handle ActionList
            case "ActionList" -> runLater(() -> runLater(() -> setSpriteImage()));

            // Handle PlayButton
            case "PlayButton" -> playAnim();

            // Handle PlayButton
            case "PlayLoopButton" -> {
                _loopAnim = anEvent.getBoolValue();
                if (_loopAnim) playAnim();
                else stopAnim();
            }

            // Handle FlipXSwitch
            case "FlipXSwitch" -> {
                _flipImage = anEvent.getBoolValue();
                runLater(() -> runLater(() -> setSpriteImage()));
            }
        }
    }

    /**
     * Sets the sprite image.
     */
    protected void setSpriteImage()
    {
        Puppet puppet = _puppetsPane.getPuppet();
        PuppetAction action = _actionList.getSelItem();
        Image img = getImage(puppet, action);
        if (_flipImage)
            img = getImagesFlipped(img);

        // Create/set new ImageView
        _imgView = new ImageView(img);
        _imgView.setBorder(Color.LIGHTGRAY, 1);
        BoxView pupBox = getView("PuppetBox", BoxView.class);
        pupBox.setContent(_imgView);

        // Play anim
        playAnim();
    }

    /**
     * Play anim.
     */
    private void playAnim()
    {
        PuppetAction action = _actionList.getSelItem();
        int time = action.getMaxTime();
        _imgView.getAnimCleared(time).setValue("Frame", _imgView.getFrameCount()).setLinear().play();
        if (_loopAnim)
            _imgView.getAnim(0).setOnFinish(this::handleAnimFinished);
    }

    /**
     * Stop Anim.
     */
    private void stopAnim()
    {
        _imgView.getAnimCleared(0).stop();
        _imgView.setFrame(0);
    }

    /**
     * Called when anim is done.
     */
    private void handleAnimFinished()
    {
        if (_loopAnim) {
            playAnim();
            //_imgView.setAnimTimeDeep(200);
        }
    }

    /**
     * Returns an image for given action.
     */
    public Image getImage(Puppet aPuppet, PuppetAction anAction)
    {
        ActionView actView = new ActionView(aPuppet);
        actView.setPuppetHeight(200);
        actView.setPuppet(aPuppet);
        actView.setPosable(true);
        actView.setAction(anAction);
        actView.setFill(null);
        actView.setBorder(null);

        int frameCount = anAction.getMaxTime() / 25 + 1;
        List<Image> images = new ArrayList<>();
        for (int i = 0; i < frameCount; i++) {
            actView.setActionTime(i * 25);
            actView.finishPose();
            Image img = ViewUtils.getImage(actView);
            images.add(img);
        }

        ImageSet imgSet = new ImageSet(images);
        return imgSet.getImage(0);
    }

    /**
     * Returns the flipped image.
     */
    public Image getImageFlipped(Image anImage)
    {
        int imageW = (int) Math.round(anImage.getWidth());
        int imageH = (int) Math.round(anImage.getHeight());
        Image flippedImage = Image.getImageForSize(imageW, imageH, anImage.hasAlpha());
        Painter pntr = flippedImage.getPainter();
        Transform xfm = new Transform(imageW / 2, imageH / 2);
        xfm.scale(-1, 1);
        xfm.translate(-imageW / 2, -imageH / 2);
        pntr.transform(xfm);
        pntr.drawImage(anImage, 0, 0);
        return flippedImage;
    }

    /**
     * Returns the flipped image.
     */
    public Image getImagesFlipped(Image anImage)
    {
        ImageSet imageSet = anImage.getImageSet();
        int count = imageSet.getCount();
        List<Image> imgs2 = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            Image img = imageSet.getImage(i);
            imgs2.add(getImageFlipped(img));
        }
        ImageSet iset2 = new ImageSet(imgs2);
        return iset2.getImage(0);
    }
}