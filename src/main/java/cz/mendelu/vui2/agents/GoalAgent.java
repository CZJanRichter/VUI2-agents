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
    Room edgeCheckHook;

    ArrayList<Room> memory = new ArrayList<Room>();

    public GoalAgent() {
        this.memory.add(this.currentRoom);
    }

    @Override
    public Action doAction(boolean canMove, boolean dirty, boolean dock) {
        this.turnNo++;
        this.addUnknownRoomsToMemory();
        //this.printInfo();
        if (dirty) return this.clean();
        if (!dock) this.currentRoom.state = RoomState.CLEAN;
        switch(this.state){
            case START:
                if (this.canMoveForward(canMove)) {
                    return this.forward();
                } else {
                    this.state = AgentState.EDGE_CHECK;
                    this.edgeCheckHook = this.currentRoom;
                    return this.turnRight();
                }
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
        switch(this.direction) {
            case EAST: this.currentRoom = this.getRoomFromMemory(this.currentRoom.x+1, this.currentRoom.y); break;
            case SOUTH: this.currentRoom = this.getRoomFromMemory(this.currentRoom.x, this.currentRoom.y-1); break;
            case WEST: this.currentRoom = this.getRoomFromMemory(this.currentRoom.x-1, this.currentRoom.y); break;
            case NORTH: this.currentRoom = this.getRoomFromMemory(this.currentRoom.x, this.currentRoom.y+1); break;
        }
        return Action.FORWARD;
    }

    public Action turnLeft() {
        this.turnsRightStreak = 0;
        this.turnsLeftStreak++;
        this.turnsLeft++;
        switch(this.direction) {
            case EAST: this.direction = AgentDirection.NORTH; break;
            case NORTH: this.direction = AgentDirection.WEST; break;
            case WEST: this.direction = AgentDirection.SOUTH; break;
            case SOUTH: this.direction = AgentDirection.EAST; break;
        }
        return Action.TURN_LEFT;
    }

    public Action turnRight() {
        this.turnsLeftStreak = 0;
        this.turnsRightStreak++;
        this.turnsRight++;
        switch(this.direction) {
            case EAST: this.direction = AgentDirection.SOUTH; break;
            case SOUTH: this.direction = AgentDirection.WEST; break;
            case WEST: this.direction = AgentDirection.NORTH; break;
            case NORTH: this.direction = AgentDirection.EAST; break;
        }
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

    public Room getRoomFromMemory(int x, int y) {
        for (Room r : this.memory) {
            if (r.x == x && r.y == y) return r;
        }
        return null; // shouldn't ever happen, use isRoomInMemory() to check before using get
    }

    public boolean isRoomInMemory(int x, int y) {
        for (Room r : this.memory) {
            if (r.x == x && r.y == y) return true;
        }
        return false;
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
