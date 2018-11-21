package cz.mendelu.vui2.agents;

import cz.mendelu.vui2.agents.greenfoot.AbstractAgent;

public class ReactionAgent extends AbstractAgent {

    int turnNo = 0;
    int forwardStreakLimit = 4;

    int cleanCounter = 0;
    int forwardStreak = 0;
    int turnsLeft = 0;
    int turnsLeftStreak = 0;
    int turnsRight = 0;
    int turnsRightStreak = 0;
    int notMovedStreak = 0;
    Action previousAction = Action.TURN_OFF;

    @Override
    public Action doAction(boolean canMove, boolean dirty, boolean dock) {
        this.turnNo++;
        this.forwardStreakLimit = (this.turnNo % 5) + 2;
        this.printInfo();

        if (dirty) return this.clean();
        if (dock && (this.turnNo > (this.timeToSimulation / 2) || this.cleanCounter > 9)) return Action.TURN_OFF;
        if (canMoveForward(canMove)) return this.forward();

        // Prevent 360° turn in one spot
        if (this.notMovedStreak >= 4 && this.turnsRightStreak >= 3) return this.turnLeft();
        if (this.notMovedStreak >= 4 && this.turnsLeftStreak >= 3) return this.turnRight();

        // Prevent getting stuck by turning right and then left
        if (this.previousAction == Action.TURN_LEFT) return this.turnLeft();
        if (this.previousAction == Action.TURN_RIGHT) return this.turnRight();

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
        return Action.CLEAN;
    }

    public Action forward() {
        this.notMovedStreak = 0;
        this.forwardStreak++;
        this.previousAction = Action.FORWARD;
        return Action.FORWARD;
    }

    public Action turnLeft() {
        this.forwardStreak = 0;
        this.turnsRightStreak = 0;
        this.turnsLeftStreak++;
        this.notMovedStreak++;
        this.turnsLeft++;
        this.previousAction = Action.TURN_LEFT;
        return Action.TURN_LEFT;
    }

    public Action turnRight() {
        this.forwardStreak = 0;
        this.turnsLeftStreak = 0;
        this.turnsRightStreak++;
        this.notMovedStreak++;
        this.turnsRight++;
        this.previousAction = Action.TURN_RIGHT;
        return Action.TURN_RIGHT;
    }

    public void printInfo() {
        System.out.println("--- Turn number " + Integer.toString(this.turnNo) + " ---");
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
