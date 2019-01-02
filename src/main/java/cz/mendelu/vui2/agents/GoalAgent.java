package cz.mendelu.vui2.agents;

import cz.mendelu.vui2.agents.greenfoot.AbstractAgent;
import java.util.*;

public class GoalAgent extends AbstractAgent {
    enum AgentState {START, EDGE_CHECK, EXPLORE, GO_TO_DOCK}
    enum AgentDirection {NORTH, SOUTH, WEST, EAST}
    enum RoomState {UNKNOWN, CLEAN, MESSY, WALL, DOCK}
    class Room {
        public int x;
        public int y;
        public RoomState state;

        Room(int x, int y, RoomState state){
            this.x = x;
            this.y = y;
            this.state = state;
        }
    }

    int turnNo = 0;
    int cleanCounter = 0;
    int turnsLeft = 0;
    int turnsLeftStreak = 0;
    int turnsRight = 0;
    int turnsRightStreak = 0;
    boolean turnAround = false;
    boolean backtrackingStart = true;
    AgentState state = AgentState.START;
    AgentDirection direction = AgentDirection.NORTH;
    Room currentRoom = new Room(0,0, RoomState.DOCK);

    ArrayList<Room> memory = new ArrayList<Room>();

    public GoalAgent() {
        this.memory.add(this.currentRoom);
    }

    @Override
    public Action doAction(boolean canMove, boolean dirty, boolean dock) {
        this.turnNo++;
        this.addUnknownRoomsToMemory();
        this.setCurrentRoomState(dirty);
        this.printInfo();
        switch(this.state){
            case START:
                break;
            case EDGE_CHECK:
                break;
            case EXPLORE:
                break;
            case GO_TO_DOCK:
                break;
        }
        return null;
    }

    // ---- Condition methods ----

    public boolean canMoveForward(boolean isWallInFront) {
        return !isWallInFront;
    }

    // ---- Action methods ----

    public Action clean() {
        this.cleanCounter++;
        this.currentRoom.state = RoomState.CLEAN;
        return Action.CLEAN;
    }

    public Action forward() {
        return Action.FORWARD;
    }

    public Action turnLeft() {
        this.turnsRightStreak = 0;
        this.turnsLeftStreak++;
        this.turnsLeft++;
        return Action.TURN_LEFT;
    }

    public Action turnRight() {
        this.turnsLeftStreak = 0;
        this.turnsRightStreak++;
        this.turnsRight++;
        return Action.TURN_RIGHT;
    }

    public void addUnknownRoomsToMemory() {
        if (!isRoomInMemory(this.currentRoom.x-1, this.currentRoom.y))
            this.memory.add(new Room(this.currentRoom.x-1, this.currentRoom.y, RoomState.UNKNOWN)); // west
        if (!isRoomInMemory(this.currentRoom.x+1, this.currentRoom.y))
            this.memory.add(new Room(this.currentRoom.x+1, this.currentRoom.y, RoomState.UNKNOWN)); // east
        if (!isRoomInMemory(this.currentRoom.x, this.currentRoom.y+1))
            this.memory.add(new Room(this.currentRoom.x, this.currentRoom.y+1, RoomState.UNKNOWN)); // north
        if (!isRoomInMemory(this.currentRoom.x, this.currentRoom.y-1))
            this.memory.add(new Room(this.currentRoom.x, this.currentRoom.y-1, RoomState.UNKNOWN)); // south
    }

    public boolean isRoomInMemory(int x, int y) {
        for (Room r : this.memory) {
            if (r.x == x && r.y == y) return true;
        }
        return false;
    }

    public void setCurrentRoomState(boolean dirty) {
        if (this.currentRoom.state == RoomState.UNKNOWN)
            this.currentRoom.state = dirty ? RoomState.MESSY : RoomState.CLEAN;
    }

    public void printInfo() {
        System.out.println("--- Turn number " + Integer.toString(this.turnNo) + "---");
        System.out.println("* Cleaned garbage: " + Integer.toString(this.cleanCounter));
        System.out.println("* Total turns left: " + Integer.toString(this.turnsLeft));
        System.out.println("* Turn left streak: " + Integer.toString(this.turnsLeftStreak));
        System.out.println("* Total turns right: " + Integer.toString(this.turnsRight));
        System.out.println("* Turn right streak: " + Integer.toString(this.turnsRightStreak));
        System.out.println("-----------------------------------------------");
    }
}
