package puppets.puppet;

import snap.gfx.*;
import snap.view.*;

/**
 * A class to edit puppet.
 */
public class ActionView extends PuppetView {

    // The PuppetAction currently playing
    PuppetAction _action;

    // The currently configured move index
    int _moveIndex = -1;

    // The current action time
    int _actTime;

    // Whether timer loops
    boolean _loops;

    // Whether ActionView is showing pose not assoicated with action/move
    boolean _timeless;

    // The ViewTimer
    ViewTimer _timer;

    // The action time when play last started
    int _startTime;

    // Constants for properties
    public static final String Action_Prop = "Action";
    public static final String ActionTime_Prop = "ActionTime";
    public static final String MoveIndex_Prop = "MoveIndex";

    // Constants
    static int FRAME_DELAY_MILLIS = 20;
    static float FRAME_DELAY_SECS = 20 / 1000f;

    /**
     * Creates an ActionView.
     */
    public ActionView(Puppet aPuppet)
    {
        setPuppet(aPuppet);
        setFill(new Color(.95));
        setBorder(Color.GRAY, 1);

        // Make markers not visible
        setShowMarkers(false);
    }

    /**
     * Returns the PuppetAction currently playing.
     */
    public PuppetAction getAction()
    {
        return _action;
    }

    /**
     * Sets the PuppetAction currently playing.
     */
    public void setAction(PuppetAction anAction)
    {
        // If already set, just return
        if (anAction == _action) return;

        // Cache old, set new
        PuppetAction oldVal = _action;
        _action = anAction;

        // Fire prop change
        firePropChange(Action_Prop, oldVal, _action);

        // Clear MoveIndex, ActionTime and initialize
        _moveIndex = _actTime = -1;
        setActionTime(0);
    }

    /**
     * Returns the current puppet move.
     */
    public PuppetMove getMove()
    {
        return _moveIndex >= 0 ? _action.getMove(_moveIndex) : null;
    }

    /**
     * Returns the move index.
     */
    public int getMoveIndex()
    {
        return _moveIndex;
    }

    /**
     * Sets the move index.
     */
    protected void setMoveIndex(int anIndex)
    {
        // If already set, just return
        if (anIndex == _moveIndex) return;

        // Cache old, set new
        int oldVal = _moveIndex;
        _moveIndex = anIndex;

        // Fire prop change
        firePropChange(MoveIndex_Prop, oldVal, _moveIndex);
    }

    /**
     * Returns the action time.
     */
    public int getActionTime()
    {
        return _actTime;
    }

    /**
     * Sets the action time.
     */
    public void setActionTime(int aTime)
    {
        // Clear Timeless and just return if value already set
        setTimeless(false);
        //if(aTime==_actTime) return; Can't really do this until setTimeless(true) sets action time to -1 or something

        // Cache old, set new time (constrained to Action.MaxTime)
        int oldVal = _actTime;
        _actTime = Math.min(aTime, _action.getMaxTime());

        // Update MoveIndex and Pose
        setMoveForTime(_actTime);
        setPoseForTime(_actTime);

        // Fire prop change
        firePropChange(ActionTime_Prop, oldVal, _actTime);
    }

    /**
     * Sets the action time for given move.
     */
    public void setActionTimeForMove(PuppetMove aMove)
    {
        int moveIndex = _action.getMoves().indexOf(aMove);
        int time = _action.getMoveStartTime(moveIndex);
        setActionTime(time);
    }

    /**
     * Sets the move for given time.
     */
    protected void setMoveForTime(int aTime)
    {
        int moveIndex = _action.getMoveIndexAtTime(aTime);
        setMoveIndex(moveIndex);
    }

    /**
     * Sets the pose for given time.
     */
    protected void setPoseForTime(int aTime)
    {
        PuppetPose pose = _action.getPoseForTime(getPuppet(), aTime);
        if (pose == null) return;
        setPose(pose);
    }

    /**
     * Returns whether ActionView is showing pose not associated with current Action/Move.
     */
    public boolean isTimeless()
    {
        return _timeless;
    }

    /**
     * Sets whether ActionView is showing pose not associated with current Action/Move.
     */
    public void setTimeless(boolean aValue)
    {
        _timeless = aValue;
        if (aValue) setMoveIndex(-1);
    }

    /**
     * Returns whether action is playing.
     */
    public boolean isPlaying()
    {
        return _timer != null;
    }

    /**
     * Starts playing the ActionView action with option to loop.
     */
    public void playAction(boolean doLoop)
    {
        // If already playing or insufficient moves, just return
        if (isPlaying() || _action.getMoveCount() < 2) return;

        // Create timer and start
        _loops = doLoop;
        _startTime = getActionTime();
        _timer = new ViewTimer(this::timerFired, FRAME_DELAY_MILLIS);
        _timer.start();
    }

    /**
     * Stops the currently running action.
     */
    public void stopAction()
    {
        // Stop/clear timer
        if (_timer != null) _timer.stop();
        _timer = null;

        // Reset time
        setActionTime(_startTime);
    }

    /**
     * Called when timer fires.
     */
    void timerFired()
    {
        // Get timer time (adjust if looping) and set
        int time = _timer.getTime();
        if (_loops) time = time % _action.getMaxTime();

        // Set time
        boolean isSmooth = isPoseSmoothly();
        setPoseSmoothly(false);
        setActionTime(time);
        setPoseSmoothly(isSmooth);


        // If beyond Action.MaxTime, stop anim
        if (!_loops && time > _action.getMaxTime())
            getEnv().runLater(() -> stopAction());
    }

    /**
     * Finishes posing.
     */
    public void finishPose()
    {
        _phys.resolveMouseJoints();
    }

}