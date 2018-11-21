package cz.mendelu.vui2.agents;

import cz.mendelu.vui2.agents.greenfoot.AbstractAgent;
import java.util.Stack;

public class WorldAgent extends AbstractAgent {

    int turnNo = 0;
    int forwardStreakLimit = 4;
    Stack<Action> actionStack = new Stack<>();
    Stack<Action> backtrackingActionStack = new Stack<>();
    int lastTurnInDock = 0;

    int cleanCounter = 0;
    int forwardStreak = 0;
    int turnsLeft = 0;
    int turnsLeftStreak = 0;
    int turnsRight = 0;
    int turnsRightStreak = 0;
    int notMovedStreak = 0;
    boolean turnAround = false;
    boolean backtrackingStart = true;


    @Override
    public Action doAction(boolean canMove, boolean dirty, boolean dock) {
        this.turnNo++;
        this.forwardStreakLimit = (this.turnNo % 5) + 2;
        if (dock) this.lastTurnInDock = this.turnNo;
        this.printInfo();

        if (this.turnAround) {
            this.turnAround = false;
            return this.turnRight();
        }

        // MODE: Battery panic (need to get to the dock ASAP)
        if ((this.timeToSimulation - this.turnNo - (this.turnNo - this.lastTurnInDock)) < 5) {
            System.out.println("BATTERY PANIC MODE ON");
            if (dock) return Action.TURN_OFF;
            if (this.backtrackingStart) {
                this.turnAround = true;
                this.backtrackingStart = false;
                this.backtrackingActionStack.addAll(this.actionStack);
                return this.turnRight();
            }
            Action whatHaveIDone = this.backtrackingActionStack.pop();
            switch (whatHaveIDone) {
                case CLEAN: return null;
                case TURN_LEFT: return this.turnRight();
                case TURN_RIGHT: return this.turnLeft();
                case FORWARD: return this.forward();
            }
        }

        // MODE: Classic movement
        if (dirty) return this.clean();
        if (canMoveForward(canMove)) return this.forward();

        // Prevent 360° turn in one spot
        if (this.notMovedStreak >= 4 && this.turnsRightStreak >= 3) return this.turnLeft();
        if (this.notMovedStreak >= 4 && this.turnsLeftStreak >= 3) return this.turnRight();

        // Prevent getting stuck by turning right and then left
        if (this.actionStack.peek() == Action.TURN_LEFT) return this.turnLeft();
        if (this.actionStack.peek() == Action.TURN_RIGHT) return this.turnRight();

        if ((this.turnNo % 4) == 0) return this.turnLeft();
        return this.turnRight();
    }

    // ---- Condition methods ----

    public boolean canMoveForward(boolean isWallInFront) {
        return !isWallInFront && this.forwardStreak < this.forwardStreakLimit;
    }

    // ---- Action methods ----

    public Action clean() {
        this.cleanCounter++;
        this.notMovedStreak++;
        this.previousAction = Action.CLEAN;
        this.actionStack.push(Action.CLEAN);
        return Action.CLEAN;
    }

    public Action forward() {
        this.notMovedStreak = 0;
        this.forwardStreak++;
        this.previousAction = Action.FORWARD;
        this.actionStack.push(Action.FORWARD);
        return Action.FORWARD;
    }

    public Action turnLeft() {
        this.forwardStreak = 0;
        this.turnsRightStreak = 0;
        this.turnsLeftStreak++;
        this.notMovedStreak++;
        this.turnsLeft++;
        this.previousAction = Action.TURN_LEFT;
        this.actionStack.push(Action.TURN_LEFT);
        return Action.TURN_LEFT;
    }

    public Action turnRight() {
        this.forwardStreak = 0;
        this.turnsLeftStreak = 0;
        this.turnsRightStreak++;
        this.notMovedStreak++;
        this.turnsRight++;
        this.previousAction = Action.TURN_RIGHT;
        this.actionStack.push(Action.TURN_RIGHT);
        return Action.TURN_RIGHT;
    }

    public void printInfo() {
        System.out.println("--- Turn number " + Integer.toString(this.turnNo) + " (Last in dock: " +
                this.lastTurnInDock + ")---");
        System.out.println("* Cleaned garbage: " + Integer.toString(this.cleanCounter));
        System.out.println("* Forward streak: " + Integer.toString(this.forwardStreak) + " (Limit: " +
                Integer.toString(this.forwardStreakLimit) + ")");
        System.out.println("* Total turns left: " + Integer.toString(this.turnsLeft));
        System.out.println("* Turn left streak: " + Integer.toString(this.turnsLeftStreak));
        System.out.println("* Total turns right: " + Integer.toString(this.turnsRight));
        System.out.println("* Turn right streak: " + Integer.toString(this.turnsRightStreak));
        System.out.println("* Not moved forward for: " + Integer.toString(this.notMovedStreak));
        System.out.println("-----------------------------------------------");
    }
}
