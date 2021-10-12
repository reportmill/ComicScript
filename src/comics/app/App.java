package comics.app;
import comics.player.*;

/**
 * The main class for app.
 */
public class App {
    
    /**
     * Standard main implementation.
     */
    public static void main(String[] args)
    {
        PlayerPane player = new PlayerPane();
        player.getUI();
        player.setEditing(true);
        player.showPlayer();
    }
}