package comics.app;
import snap.geom.Pos;
import snap.geom.Size;
import snap.geom.VPos;
import snap.gfx.*;
import snap.util.*;
import snap.view.*;
import snap.viewx.DialogBox;
import snap.viewx.DialogSheet;
import snap.web.WebURL;
import java.util.concurrent.CompletableFuture;

/**
 * A class to show samples.
 */
public class SamplesPane extends ViewOwner {

    //
    private EditorPane _epane;
    private DialogSheet _dbox;

    // The selected index
    private int _selIndex;

    // The effects
    private ShadowEffect _shadow, _selShadow;

    // Constant for samples
    private static final String SAMPLES_ROOT = "https://reportmill.com/jars/ccsamples/";
    private static final Size SAMPLES_DOC_SIZE = new Size(144, 81);

    // Samples
    private static final String S1 = "Blank.txt";
    private static final String S2 = "Welcome.txt";
    private static final String S3 = "AllActions.txt";
    private static final String[] SALL = {S1, S2, S3};
    private static final Image[] DOC_IMAGES = new Image[SALL.length];

    /**
     * Shows the samples pane.
     */
    public void showSamples(EditorPane anEP)
    {
        _epane = anEP;
        ChildView aView = (ChildView) anEP.getUI();

        _dbox = new DialogSheet();
        _dbox.setContent(getUI());
        _dbox.showConfirmDialog(aView);
        _dbox.addPropChangeListener(pc -> dialogBoxClosed(), DialogBox.Showing_Prop);
    }

    /**
     * Called when dialog box closed.
     */
    void dialogBoxClosed()
    {
        _epane._scriptEditor._scriptView.requestFocus();

        if (_dbox.isCancelled()) return;

        String script = getDoc(_selIndex);
        if (script != null) {
            _epane.getPlayer().setRunLine(0);
            _epane.getScript().setText(script);
        }
    }

    /**
     * Creates UI.
     */
    protected View createUI()
    {
        // Create Shadows
        _shadow = new ShadowEffect();
        _selShadow = new ShadowEffect(10, Color.get("#038ec3"), 0, 0);

        // Create main ColView to hold RowViews for samples
        ColView colView = new ColView();
        colView.setSpacing(25);
        colView.setPadding(25, 15, 20, 15);
        colView.setAlign(Pos.TOP_CENTER);
        colView.setFillWidth(true);
        colView.setFill(new Color(.97, .97, 1d));
        colView.setBorder(Color.GRAY, 1);

        // Create RowViews
        RowView rowView = null;
        for (int i = 0; i < SALL.length; i++) {
            String name = SALL[i];

            // Create/add new RowView for every three samples
            if (i % 3 == 0) {
                rowView = new RowView();
                rowView.setAlign(Pos.CENTER);
                colView.addChild(rowView);
            }

            // Create ImageViewX for sample
            ImageView iview = new ImageView();
            iview.setPrefSize(SAMPLES_DOC_SIZE);
            iview.setFill(Color.WHITE);
            iview.setName("ImageView" + i);
            iview.setEffect(i == 0 ? _selShadow : _shadow);

            // Create label for sample
            Label label = new Label(name);
            label.setFont(Font.Arial13);
            label.setPadding(3, 4, 3, 4);
            label.setLeanY(VPos.BOTTOM);
            if (i == 0) {
                label.setFill(Color.BLUE);
                label.setTextColor(Color.WHITE);
            }

            // Create/add ItemBox for Sample and add ImageView + Label
            ColView ibox = new ColView();
            ibox.setPrefSize(175, 130);
            ibox.setAlign(Pos.TOP_CENTER);
            ibox.setChildren(iview, label);
            ibox.setPadding(0, 0, 8, 0);
            ibox.setName("ItemBox" + i);
            ibox.addEventHandler(e -> itemBoxWasPressed(ibox, e), MousePress);
            rowView.addChild(ibox);
        }

        // Create ScrollView
        ScrollView scroll = new ScrollView(colView);
        scroll.setPrefHeight(420);
        scroll.setShowHBar(false);
        scroll.setShowVBar(true);

        // Create top level box to hold ColView and label
        ColView boxView = new ColView();
        boxView.setSpacing(8);
        boxView.setFillWidth(true);
        Label label = new Label("Select a script:");
        label.setFont(Font.Arial16.getBold());
        boxView.setChildren(label, scroll);
        return boxView;
    }

    /**
     * Initialize UI.
     */
    protected void initUI()
    {
        CompletableFuture.runAsync(this::loadImages);
    }

    /**
     * Called when template ItemBox is clicked.
     */
    void itemBoxWasPressed(ColView anItemBox, ViewEvent anEvent)
    {
        String name = anItemBox.getName();
        int index = StringUtils.intValue(name);
        ColView oldItemBox = getView("ItemBox" + _selIndex, ColView.class);
        oldItemBox.getChild(0).setEffect(_shadow);
        Label oldLabel = (Label) oldItemBox.getChild(1);
        oldLabel.setFill(null);
        oldLabel.setTextColor(null);
        anItemBox.getChild(0).setEffect(_selShadow);
        Label newLabel = (Label) anItemBox.getChild(1);
        newLabel.setFill(Color.BLUE);
        newLabel.setTextColor(Color.WHITE);
        _selIndex = index;
        if (anEvent.getClickCount() > 1) _dbox.confirm();
    }

    /**
     * Returns the doc at given index.
     */
    static String getDoc(int anIndex)
    {
        String name = SALL[anIndex];
        WebURL url = WebURL.createUrl(SAMPLES_ROOT + name);
        String str = url.getText();
        if (str == null) System.err.println("SamplesPane.getDoc: Couldn't load " + url);
        return str;
    }

    /**
     * Returns the doc thumnail image at given index.
     */
    public Image getDocImage(int anIndex)
    {
        // If image already set, just return
        Image img = DOC_IMAGES[anIndex];
        if (img != null) return img;

        // Get image name, URL string, URL and Image. Then make sure image is loaded by requesting Image.Native.
        String name = SALL[anIndex].replace(".txt", ".png");
        WebURL imgURL = WebURL.getUrl(SAMPLES_ROOT + name);
        img = DOC_IMAGES[anIndex] = Image.getImageForSource(imgURL);
        img.getNative();
        return img;
    }

    /**
     * Loads the thumbnail image for each sample in background thread.
     */
    void loadImages()
    {
        // Iterate over sample names and load/set images
        for (int i = 0; i < SALL.length; i++) {
            int index = i;
            Image img = getDocImage(i);
            runLater(() -> setImage(img, index));
        }
    }

    /**
     * Called after an image is loaded to set in ImageView in app thread.
     */
    void setImage(Image anImg, int anIndex)
    {
        ImageView iview = getView("ImageView" + anIndex, ImageView.class);
        iview.setImage(anImg);
    }
}