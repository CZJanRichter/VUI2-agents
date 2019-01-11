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
    AgentState state = AgentState.START;
    AgentDirection direction = AgentDirection.NORTH;
    Room currentRoom = new Room(0,0, RoomState.DOCK);
    Room edgeCheckHook;
    int edgeCheckTurns = 0;
    Action previousMovement;
    boolean gotToEdge = false;
    int noNewExploreStreak = 0;
    int goToDockTurns = 0;

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
                this.edgeCheckTurns++;
                if (this.currentRoom == this.edgeCheckHook && this.edgeCheckTurns > 4) {
                    this.state = AgentState.EXPLORE;
                    return this.turnRight();
                }
                return getActionToMoveAroundTheEdge(isWall);
            case EXPLORE:
                if (getRoomInFront().state == RoomState.UNKNOWN && !isWall) {
                    this.gotToEdge = false;
                    this.noNewExploreStreak = 0;
                    return forward();
                }
                switch(this.direction) {
                    case NORTH:
                        if (getRoomFromMemory(this.currentRoom.x+1, this.currentRoom.y).state == RoomState.UNKNOWN) return turnRight();
                        if (getRoomFromMemory(this.currentRoom.x-1, this.currentRoom.y).state == RoomState.UNKNOWN) return turnLeft();
                    case EAST:
                        if (getRoomFromMemory(this.currentRoom.x, this.currentRoom.y-1).state == RoomState.UNKNOWN) return turnRight();
                        if (getRoomFromMemory(this.currentRoom.x, this.currentRoom.y+1).state == RoomState.UNKNOWN) return turnLeft();
                    case SOUTH:
                        if (getRoomFromMemory(this.currentRoom.x-1, this.currentRoom.y).state == RoomState.UNKNOWN) return turnRight();
                        if (getRoomFromMemory(this.currentRoom.x+1, this.currentRoom.y).state == RoomState.UNKNOWN) return turnLeft();
                    case WEST:
                        if (getRoomFromMemory(this.currentRoom.x, this.currentRoom.y+1).state == RoomState.UNKNOWN) return turnRight();
                        if (getRoomFromMemory(this.currentRoom.x, this.currentRoom.y-1).state == RoomState.UNKNOWN) return turnLeft();
                    default:
                        this.noNewExploreStreak++;
                        if (this.noNewExploreStreak >= 500 || (this.timeToSimulation - this.turnNo) < 200) {
                            this.gotToEdge = false;
                            this.state = AgentState.GO_TO_DOCK;
                        }
                        if (!this.gotToEdge && !isWall) return forward();
                        else if (isWall) {
                            this.gotToEdge = true;
                            return turnRight();
                        }
                        return getActionToMoveAroundTheEdge(isWall);
                }
            case GO_TO_DOCK:
                this.goToDockTurns++;
                if (dock) return turnOff();
                if (getDirectPathToDock(this.currentRoom) != null) {
                    if (this.direction != getDirectPathToDock(this.currentRoom)) return turnRight();
                    return forward();
                }
                if (!this.gotToEdge && !isWall) return forward();
                else if (isWall) {
                    this.gotToEdge = true;
                    return this.turnRight();
                }
                if (this.goToDockTurns % 140 > 120) {
                    if (!isWall) return forward();
                    return turnLeft();
                }
                return getActionToMoveAroundTheEdge(isWall);
        }
        return null;
    }

    public Action forward() {
        this.currentRoom = this.getRoomInFront();
        this.previousMovement = Action.FORWARD;
        return Action.FORWARD;
    }

    public Action turnLeft() {
        switch(this.direction) {
            case EAST: this.direction = AgentDirection.NORTH; break;
            case NORTH: this.direction = AgentDirection.WEST; break;
            case WEST: this.direction = AgentDirection.SOUTH; break;
            case SOUTH: this.direction = AgentDirection.EAST; break;
        }
        this.previousMovement = Action.TURN_LEFT;
        return Action.TURN_LEFT;
    }

    public Action turnRight() {
        switch(this.direction) {
            case EAST: this.direction = AgentDirection.SOUTH; break;
            case SOUTH: this.direction = AgentDirection.WEST; break;
            case WEST: this.direction = AgentDirection.NORTH; break;
            case NORTH: this.direction = AgentDirection.EAST; break;
        }
        this.previousMovement = Action.TURN_RIGHT;
        return Action.TURN_RIGHT;
    }

    public Action turnOff(){
        System.out.println("The agent is turning off.");
        printMemory();
        printRoomMemorySchema();
        return Action.TURN_OFF;
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

    public Action getActionToMoveAroundTheEdge(boolean isWall) {
        switch(this.previousMovement) {
            case FORWARD:
                return turnLeft();
            case TURN_LEFT: case TURN_RIGHT:
                if (isWall) return turnRight();
                return forward();
        }
        return null;
    }

    public AgentDirection getDirectPathToDock(Room from) {
        boolean foundWall = false;
        Room savedRoom = from;
        do { // NORTH
            if (!isRoomInMemory(savedRoom.x, savedRoom.y+1)) break;
            savedRoom = getRoomFromMemory(savedRoom.x, savedRoom.y+1);
            if (savedRoom.state == RoomState.WALL) foundWall = true;
            if (savedRoom.state == RoomState.DOCK) return AgentDirection.NORTH;
        } while (!foundWall);
        foundWall = false;
        savedRoom = from;
        do { // EAST
            if (!isRoomInMemory(savedRoom.x+1, savedRoom.y)) break;
            savedRoom = getRoomFromMemory(savedRoom.x+1, savedRoom.y);
            if (savedRoom.state == RoomState.WALL) foundWall = true;
            if (savedRoom.state == RoomState.DOCK) return AgentDirection.EAST;
        } while (!foundWall);
        foundWall = false;
        savedRoom = from;
        do { // SOUTH
            if (!isRoomInMemory(savedRoom.x, savedRoom.y-1)) break;
            savedRoom = getRoomFromMemory(savedRoom.x, savedRoom.y-1);
            if (savedRoom.state == RoomState.WALL) foundWall = true;
            if (savedRoom.state == RoomState.DOCK) return AgentDirection.SOUTH;
        } while (!foundWall);
        foundWall = false;
        savedRoom = from;
        do { // WEST
            if (!isRoomInMemory(savedRoom.x-1, savedRoom.y)) break;
            savedRoom = getRoomFromMemory(savedRoom.x-1, savedRoom.y);
            if (savedRoom.state == RoomState.WALL) foundWall = true;
            if (savedRoom.state == RoomState.DOCK) return AgentDirection.WEST;
        } while (!foundWall);
        return null;
    }

    public Room getRoomFromMemory(int x, int y) {
        for (Room r : this.memory) {
            if (r.x == x && r.y == y) return r;
        }
        return null;
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

    public void printRoomMemorySchema() {
        int xmax = 0, xmin = 0, ymax = 0, ymin = 0;
        for (Room r : this.memory) {
            if (xmax < r.x) xmax = r.x;
            if (xmin > r.x) xmin = r.x;
            if (ymax < r.y) ymax = r.y;
            if (ymin > r.y) ymin = r.y;
        }
        char[][] roomSchema = new char[xmax-xmin+1][ymax-ymin+1];
        for (int y = 0; y < roomSchema.length; y++) {
            for (int x = 0; x < roomSchema[y].length; x++) {
                roomSchema[x][y] = '?';
            }
        }
        for (Room r : this.memory) {
            switch(r.state){
                case CLEAN: roomSchema[r.x-xmin][r.y-ymin] = '0'; break;
                case DOCK: roomSchema[r.x-xmin][r.y-ymin] = 'D'; break;
                case WALL: roomSchema[r.x-xmin][r.y-ymin] = 'W'; break;
            }
        }
        for (int y = roomSchema.length-1; y >= 0; y--) {
            for (int x = 0; x < roomSchema[y].length; x++) {
                System.out.print(roomSchema[x][y]);
            }
            System.out.println();
        }
    }
}
