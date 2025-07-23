package puppets;
import java.util.*;
import snap.geom.Insets;
import snap.geom.Transform;
import snap.gfx.*;
import snap.view.*;

/**
 * A class to render puppet images.
 */
public class PuppetImager {

    // The frame count
    int _frameCount;

    // The first image
    Image _image;

    // The images
    List<Image> _images;

    // The Puppet
    Puppet _puppet;

    // The Action
    PuppetAction _action;

    // The ActionView
    ActionView _actView;

    // A list of PuppetImagers
    private static List<PuppetImager> _imagers = new ArrayList<>();

    // The thread to process imagers
    private static Thread _imagerThread;

    /**
     * Constructor.
     */
    public PuppetImager(Puppet aPuppet, PuppetAction anAction, double aScale, Insets theIns)
    {
        _puppet = aPuppet;
        _action = anAction;

        // Create action view
        _actView = new ActionView(aPuppet);
        _actView.setFill(null);
        _actView.setBorder(null);
        if (theIns == null) theIns = Insets.EMPTY;
        _actView.setPadding(theIns);
        _actView.setPuppetHeight(400);
        _actView.setPuppet(aPuppet);
        _actView.setPoseSmoothly(false);

        // Get the frame count
        int FRAME_DELAY_MILLIS = 25;
        _frameCount = anAction.getMaxTime() / FRAME_DELAY_MILLIS + 1;

        // Create first image (empty)
        double vw = _actView.getWidth();
        double vh = _actView.getHeight();
        _image = Image.getImageForSizeAndDpiScale(vw, vh, true, 0);
        _image.setLoaded(false);
        addImager(this);
    }

    /**
     * Returns the image.
     */
    public Image getImage()  { return _image; }

    /**
     * Returns the frame count.
     */
    public int getFrameCount()  { return _frameCount; }

    /**
     * Loads the images.
     */
    private void loadImages()
    {
        Puppet puppet = _actView.getPuppet();
        while (!puppet.isLoaded()) {
            System.out.println("Puppet not loaded: " + puppet.getName() + " (doing yield)");
            Thread.yield();
        }
        System.out.println("PuppetImager: Loading images for " + puppet.getName() + " " + _action.getName());

        // Create list of images and fill with empty images
        _images = new ArrayList<>(_frameCount);
        _images.add(_image);
        double imageW = _image.getWidth();
        double imageH = _image.getHeight();
        for (int i = 1; i < _frameCount; i++) {
            Image img = Image.getImageForSizeAndDpiScale(imageW, imageH, true, 0);
            _images.add(img);
        }

        // Set initial action
        _actView.setPosable(true);
        _actView.setAction(_action);

        // Iterate over frames, set action pose and paint view into frame image
        for (int i = 0; i < _frameCount; i++) {
            Image img = _images.get(i);
            _actView.setActionTime(i * 25);
            _actView.finishPose();
            paintViewInImage(_actView, img);
            Thread.yield();
        }

        // Create ImageSet and set image loaded
        ImageSet imgSet = new ImageSet(_images);
        _image.setLoaded(true);
    }

    /**
     * Returns the flipped image.
     */
    public static Image getImageFlipped(Image anImage)
    {
        int imageW = (int) Math.round(anImage.getWidth());
        int imageH = (int) Math.round(anImage.getHeight());
        Image img = Image.getImageForSize(imageW, imageH, anImage.hasAlpha());
        Painter pntr = img.getPainter();
        Transform xfm = new Transform(imageW / 2, imageH / 2);
        xfm.scale(-1, 1);
        xfm.translate(-imageW / 2, -imageH / 2);
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
        ImageSet imageSet = anImage.getImageSet();
        if (imageSet == null)
            return getImageFlipped(anImage);

        int count = imageSet.getCount();
        List<Image> imgs2 = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            Image img = imageSet.getImage(i);
            imgs2.add(getImageFlipped(img));
        }
        ImageSet iset2 = new ImageSet(imgs2);
        return iset2.getImage(0);
    }

    /**
     * Returns an image for a View (1 = 72 dpi, 2 = 144 dpi, 0 = device dpi).
     */
    private static Image getImage(View aView)
    {
        Image img = Image.getImageForSizeAndDpiScale(aView.getWidth(), aView.getHeight(), true, 0);
        Painter pntr = img.getPainter();
        ViewUtils.paintView(aView, pntr);
        return img;
    }

    /**
     * Returns an image for a View (1 = 72 dpi, 2 = 144 dpi, 0 = device dpi).
     */
    private static void paintViewInImage(View aView, Image anImage)
    {
        Painter pntr = anImage.getPainter();
        ViewUtils.paintView(aView, pntr);
    }

    /**
     * Adds a puppet imager.
     */
    private static synchronized void addImager(PuppetImager aPI)
    {
        _imagers.add(aPI);

        if (_imagers.size() == 1)
            startImagerThread();
    }

    /**
     * Returns a puppet imager.
     */
    private static synchronized PuppetImager getImager()
    {
        if (_imagers.isEmpty()) return null;
        PuppetImager pi = _imagers.get(0);
        return pi;
    }

    /**
     * Removes a puppet imager.
     */
    private static synchronized PuppetImager removeImager()
    {
        if (_imagers.isEmpty()) return null;
        PuppetImager pi = _imagers.remove(0);
        return pi;
    }

    /**
     * Starts the Imager thread.
     */
    private static void startImagerThread()
    {
        System.out.println("Starting ImagerThread");
        _imagerThread = new Thread(() -> processImagers());
        _imagerThread.start();
    }

    private static void processImagers()
    {
        while (!_imagers.isEmpty()) {
            PuppetImager pi = getImager();
            if (pi == null) break;
            if (!pi._puppet.isLoaded()) {
                pi._puppet.addLoadListener(() -> startImagerThread());
                break;
            }
            pi.loadImages();
            removeImager();
        }
        _imagerThread = null;
    }
}