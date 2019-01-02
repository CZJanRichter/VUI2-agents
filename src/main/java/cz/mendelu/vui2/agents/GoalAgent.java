package cz.mendelu.vui2.agents;

import cz.mendelu.vui2.agents.greenfoot.AbstractAgent;
import java.util.*;

public class GoalAgent extends AbstractAgent {
    enum AgentState {START, EDGE_CHECK, EXPLORE, GO_TO_DOCK}
    enum AgentDirection {NORTH, SOUTH, WEST, EAST}
    enum RoomState {UNKNOWN, CLEAN, WALL, DOCK}
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
    public Action doAction(boolean isWall, boolean dirty, boolean dock) {
        this.turnNo++;
        this.addUnknownRoomsToMemory();

        if (dirty) return Action.CLEAN;
        if (!dock) this.currentRoom.state = RoomState.CLEAN;
        if (isWall) this.getRoomInFront().state = RoomState.WALL;
        this.printMemory();
        switch(this.state){
            case START:
                if (!isWall) {
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

    public Action forward() {
        this.currentRoom = this.getRoomInFront();
        return Action.FORWARD;
    }

    public Action turnLeft() {
        switch(this.direction) {
            case EAST: this.direction = AgentDirection.NORTH; break;
            case NORTH: this.direction = AgentDirection.WEST; break;
            case WEST: this.direction = AgentDirection.SOUTH; break;
            case SOUTH: this.direction = AgentDirection.EAST; break;
        }
        return Action.TURN_LEFT;
    }

    public Action turnRight() {
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
        return null; // shouldn't ever happen
    }

    public Room getRoomInFront() {
        switch(this.direction) {
            case EAST: return this.getRoomFromMemory(this.currentRoom.x+1, this.currentRoom.y);
            case SOUTH: return this.getRoomFromMemory(this.currentRoom.x, this.currentRoom.y-1);
            case WEST: return this.getRoomFromMemory(this.currentRoom.x-1, this.currentRoom.y);
            case NORTH: return this.getRoomFromMemory(this.currentRoom.x, this.currentRoom.y+1);
        }
        return null;
    }

    public boolean isRoomInMemory(int x, int y) {
        for (Room r : this.memory) {
            if (r.x == x && r.y == y) return true;
        }
        return false;
    }

    public void printMemory() {
        System.out.println("---Memory dump---");
        System.out.println("Current room: (" + this.currentRoom.x + ";" + currentRoom.y + ") " + currentRoom.state);
        for (Room r : this.memory) System.out.println("(" + r.x + ";" + r.y + ") " + r.state);
    }
}
