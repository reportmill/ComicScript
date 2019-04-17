package comics.app;
import org.teavm.jso.JSBody;
import snap.util.SnapUtils;

/**
 * The main class for app.
 */
public class App {
    
    // The Players
    static PlayerPane      _player, _players[] = new PlayerPane[20];
    
    // The number of Players
    static int             _playerCount;

/**
 * Standard main implementation.
 */
public static void main(String args[])
{
    snaptea.TV.set();
    
    // If not TeaVM, just show new player
    if(!SnapUtils.isTeaVM) {
        show(null, null); return; }
    
    // Get args
    String arg0 = getMainArg0();
    String arg1 = getMainArg1();
    
    if(arg0!=null && arg0.equals("showEditor"))
        showEditor(arg1);
    else show(arg0, null);
}

/**
 * Shows a comic in given container for given params.
 */
public static void show(String anEmtId, Object theParams)
{
    // Create new Player
    _player = _players[_playerCount++] = new PlayerPane();
    
    // If name provided, set as Window.Name
    if(anEmtId!=null && anEmtId.length()>0) _player.getWindow().setName(anEmtId);
    else { _player.getUI(); _player.setEditing(true); }
    
    // Show Player
    _player.showPlayer();
}

/**
 * Shows the editor for Player with given name (or last one, if null)
 */
public static void showEditor(String anEmtId)
{
    // If ElementId provided, toggle editing for player with for that element id
    if(anEmtId!=null) {
        for(int i=0;i<_playerCount;i++) { PlayerPane player = _players[i];
            if(anEmtId.equals(player.getWindow().getName()))
                player.setEditing(!player.isEditing());
        }
    }
    
    // If no element id, toggle Player.Editing for last player
    else if(_player!=null)
        _player.setEditing(!_player.isEditing());
}

@JSBody(params = { }, script = "return CSMainArg0!=null? CSMainArg0 : '';")
public static native String getMainArg0();

@JSBody(params = { }, script = "return CSMainArg1!=null? CSMainArg1 : '';")
public static native String getMainArg1();

}