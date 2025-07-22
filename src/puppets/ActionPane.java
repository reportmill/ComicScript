package puppets;
import java.util.List;
import snap.props.PropChange;
import snap.view.*;
import snap.viewx.DialogBox;

/**
 * A class to manage UI to create and edit puppet animations.
 */
public class ActionPane extends ViewOwner {

    // The AppPane
    private PuppetsPane _puppetsPane;

    // The puppet action view
    private ActionView _actView;

    // A List of Puppet Actions
    private PuppetUtils.ActionFile _actions;

    // A ListView to show actions
    private ListView<PuppetAction> _actionList;

    // A TableView to show moves
    private TableView<PuppetMove> _moveTable;

    // The last copied move
    private PuppetMove _copyMove;

    /**
     * Constructor.
     */
    public ActionPane(PuppetsPane puppetsPane)
    {
        super();
        _puppetsPane = puppetsPane;
        _actions = PuppetUtils.getActionFile();
    }

    /**
     * Returns the puppet.
     */
    public Puppet getPuppet()  { return _actView.getPuppet(); }

    /**
     * Returns the action.
     */
    public PuppetAction getAction()  { return _actView.getAction(); }

    /**
     * Selects action and move.
     */
    private void setActionAndMove(PuppetAction anAction, PuppetMove aMove)
    {
        // Set ActView Action
        _actView.setAction(anAction);
        _actionList.setItems(_actions.getActions());
        _actionList.setSelItem(anAction);

        // Set MoveTable items
        List<PuppetMove> moves = anAction != null ? anAction.getMoves() : null;
        int moveIndex = moves != null ? moves.indexOf(aMove) : -1;
        if (aMove == null && anAction != null && anAction.getMoveCount() > 0)
            moveIndex = 0;
        _moveTable.setItems(moves);
        _moveTable.setSelIndex(moveIndex);

        // Set time
        _actView.setActionTimeForMove(aMove);
    }

    /**
     * Initialize UI.
     */
    protected void initUI()
    {
        // Create ActionView
        _actView = new ActionView(_puppetsPane.getPuppet());
        _actView.addEventFilter(e -> _actView.setTimeless(true), MouseRelease);
        _actView.addPropChangeListener(pc -> resetLater(), ActionView.MoveIndex_Prop);

        // Get PuppetBox and add ActionView
        BoxView pupBox = getView("PuppetBox", BoxView.class);
        pupBox.setContent(_actView);

        // Set ActionList
        _actionList = getView("ActionList", ListView.class);
        _actionList.setItemTextFunction(PuppetAction::getName);
        _actionList.setItems(_actions.getActions());

        // Set MoveTable
        _moveTable = getView("MoveTable", TableView.class);
        _moveTable.getCol(0).setItemTextFunction(PuppetMove::getPoseName);
        _moveTable.getCol(1).setItemTextFunction(move -> String.valueOf(move.getTime()));
        _moveTable.setEditable(true);
        _moveTable.addPropChangeListener(this::handleMoveTableEditingCellChange, TableView.EditingCell_Prop);

        // Make PuppetView interactive
        _actView.setPosable(true);

        // Configure
        PuppetAction action = _actions.getActionCount() > 0 ? _actions.getAction(0) : null;
        setActionAndMove(action, null);
    }

    /**
     * Reset UI.
     */
    protected void resetUI()
    {
        // Update MoveTable.SelIndex
        int ind = _actView.getMoveIndex();
        _moveTable.setSelIndex(ind);

        // Update TimeSlider
        getView("TimeSlider", Slider.class).setMax(getAction().getMaxTime());
        setViewValue("TimeSlider", _actView.isTimeless() ? 0 : _actView.getActionTime());

        // Update ShowMarkersCheckBox, FreezeOuterJointsCheckBox
        setViewValue("ShowMarkersCheckBox", _actView.isShowMarkers());
        setViewValue("FreezeOuterJointsCheckBox", _actView.isFreezeOuterJoints());
    }

    /**
     * Respond to UI.
     */
    protected void respondUI(ViewEvent anEvent)
    {
        // Handle PlayLoopButton
        ToggleButton playLoopButton = getView("PlayLoopButton", ToggleButton.class);
        if (!anEvent.equals("PlayLoopButton") && playLoopButton.isSelected()) {
            _actView.stopAction();
            playLoopButton.setSelected(false);
        }

        // Handle ShowMarkersCheckBox, FreezeOuterJointsCheckBox
        if (anEvent.equals("ShowMarkersCheckBox"))
            _actView.setShowMarkers(anEvent.getBoolValue());
        if (anEvent.equals("FreezeOuterJointsCheckBox"))
            _actView.setFreezeOuterJoints(anEvent.getBoolValue());

        // Handle ActionList
        if (anEvent.equals("ActionList")) {
            PuppetAction action = _actionList.getSelItem();
            setActionAndMove(action, null);
            runLater(() -> _actView.playAction(false));
        }

        // Handle AddActionButton
        if (anEvent.equals("AddActionButton")) {
            String name = DialogBox.showInputDialog(_puppetsPane.getUI(), "Add Action", "Enter Action Name:", "Untitled");
            if (name == null || name.isEmpty())
                return;
            PuppetAction action = new PuppetAction(name);
            _actions.addAction(action);
            setActionAndMove(action, null);
        }

        // Handle DeleteActionMenu
        if (anEvent.equals("DeleteActionMenu")) {

            // Get selected action index and remove
            int selActionIndex = _actionList.getSelIndex();
            if (selActionIndex < 0) {
                beep(); return; }
            _actions.removeAction(selActionIndex);

            // Select next action
            int newSelIndex = selActionIndex < _actions.getActionCount() ? selActionIndex : _actions.getActionCount() - 1;
            PuppetAction newSelAction = newSelIndex >= 0 ? _actions.getAction(newSelIndex) : null;
            setActionAndMove(newSelAction, null);

            // Save actions
            _actions.saveActions();
        }

        // Handle MoveUpActionMenu, MoveDownActionMenu
        if (anEvent.equals("MoveUpActionMenu") || anEvent.equals("MoveDownActionMenu")) {

            // Get selected action index
            int selActionIndex = _actionList.getSelIndex();
            if (selActionIndex < 0) {
                beep(); return; }

            int ind2 = anEvent.equals("MoveUpActionMenu") ? (selActionIndex - 1) : (selActionIndex + 1);
            PuppetAction action = _actions.removeAction(selActionIndex);
            _actions.addAction(action, ind2);
            setActionAndMove(action, _moveTable.getSelItem());

            // Save actions
            _actions.saveActions();
        }

        // Handle MoveTable
        if (anEvent.equals("MoveTable")) {
            setActionAndMove(_actionList.getSelItem(), _moveTable.getSelItem());
        }

        // Handle AddMoveButton
        if (anEvent.equals("AddMoveButton")) {
            PuppetAction action = _actionList.getSelItem();
            if (action == null)
                return;
            String name = DialogBox.showInputDialog(_puppetsPane.getUI(), "Add Move", "Enter Pose Name:", "Untitled");
            if (name == null || name.isEmpty())
                return;
            PuppetPose pose = action.getPoseForName(name);
            if (pose == null) {
                pose = _actView.getPose();
                pose.setName(name);
            }
            PuppetMove move = action.addMoveForPoseAndTime(pose, 200);
            setActionAndMove(action, move);
            _actions.saveActions();
        }

        // Handle CopyMoveMenu
        if (anEvent.equals("CopyMoveMenu")) {
            PuppetMove move = _moveTable.getSelItem();
            if (move == null) {
                move = new PuppetMove(_actView.getPose(), 200);
                move.getPose().setName("Untitled");
            }
            _copyMove = move.clone();
        }

        // Handle PasteMoveMenu
        if (anEvent.equals("PasteMoveMenu")) {
            PuppetAction action = _actionList.getSelItem();
            if (action == null) return;
            PuppetMove move = _copyMove != null ? _copyMove.clone() : null;
            if (move == null) {
                beep();
                return;
            }
            int ind = _moveTable.getSelIndex() + 1;
            action.addMove(move, ind);
            setActionAndMove(action, move);
            _actions.saveActions();
        }

        // Handle PastePoseMenu
        if (anEvent.equals("PastePoseMenu")) {
            PuppetAction action = _actionList.getSelItem();
            if (action == null) return;
            PuppetMove srcMove = _copyMove != null ? _copyMove.clone() : null;
            if (srcMove == null) {
                beep(); return; }
            PuppetMove dstMove = _moveTable.getSelItem();
            if (dstMove == null) {
                beep(); return; }
            action.replacePose(dstMove.getPoseName(), srcMove.getPose());
            _actions.saveActions();
            _actView.setPose(dstMove.getPose());
        }

        // Handle DeleteMoveMenu
        if (anEvent.equals("DeleteMoveMenu")) {
            PuppetAction action = _actionList.getSelItem();
            if (action == null) return;
            int ind = _moveTable.getSelIndex();
            if (ind < 0) {
                beep();
                return;
            }
            action.removeMove(ind);
            int ind2 = ind < action.getMoveCount() ? ind : action.getMoveCount() - 1;
            setActionAndMove(action, ind2 >= 0 ? action.getMove(ind2) : null);
            _actions.saveActions();
        }

        // Handle MoveUpMoveMenu, MoveDownMoveMenu
        if (anEvent.equals("MoveUpMoveMenu") || anEvent.equals("MoveDownMoveMenu")) {
            PuppetAction action = _actionList.getSelItem();
            if (action == null) return;
            int ind = _moveTable.getSelIndex();
            if (ind < 1) {
                beep();
                return;
            }
            int ind2 = anEvent.equals("MoveUpMoveMenu") ? (ind - 1) : (ind + 1);
            PuppetMove move = action.removeMove(ind);
            action.addMove(move, ind2);
            setActionAndMove(action, move);
            _actions.saveActions();
        }

        // Handle PlayButton
        if (anEvent.equals("PlayButton"))
            _actView.playAction(false);

        // Handle PlayLoopButton
        if (anEvent.equals("PlayLoopButton")) {
            if (anEvent.getBoolValue()) _actView.playAction(true);
            else _actView.stopAction();
        }

        // Handle TimeSlider
        if (anEvent.equals("TimeSlider"))
            _actView.setActionTime(anEvent.getIntValue());
    }

    /**
     * Called when cell editing changes.
     */
    private void handleMoveTableEditingCellChange(PropChange aPC)
    {
        // Get cell that finished editing (just return if none)
        ListCell<PuppetMove> cell = (ListCell<PuppetMove>) aPC.getOldValue();

        // Get row/col and make sure there are series/points to cover it
        PuppetMove move = cell.getItem();
        String text = cell.getText();
        int col = cell.getCol();

        // If Time column, set time
        if (col == 1) {
            move.setTime(Integer.parseInt(text));
            _moveTable.updateItem(move);
            _actions.saveActions();
            resetLater();
        }
    }
}