package com.wudji.xplusautofish.scheduler;

import net.minecraft.Util;

import java.util.Objects;
import java.util.function.LongSupplier;

public class Action {
    private final ActionType actionType;
    private final long delay;
    private long timeToComplete;
    private final Runnable runnable;
    private final LongSupplier nowMillis;

    public Action(ActionType actionType, long delay, Runnable runnable) {
        this(actionType, delay, runnable, Util::getMillis);
    }

    public Action(ActionType actionType, long delay, Runnable runnable, LongSupplier nowMillis) {
        this.actionType = Objects.requireNonNull(actionType, "actionType");
        this.delay = delay;
        this.runnable = Objects.requireNonNull(runnable, "runnable");
        this.nowMillis = Objects.requireNonNull(nowMillis, "nowMillis");
        this.timeToComplete = nowMillis.getAsLong() + delay;

//        System.out.println("regd " + actionType + ". Complete in " + delay + " at " + timeToComplete);
    }

    /**
     * @return true if the action was completed
     */
    public boolean tryExecute() {

        if(nowMillis.getAsLong() >= timeToComplete){

            runnable.run();

            //If this is a repeating action, we need to reset the timer
            if(actionType == ActionType.REPEATING_ACTION){
                resetTimer();
            }

            return true;
        }
        return false;
    }

    /**
     * Compatibility alias used by the scheduler's existing tick loop.
     */
    public boolean tick() {
        return tryExecute();
    }

    public ActionType getActionType() {
        return actionType;
    }

    public void resetTimer(){
        this.timeToComplete = nowMillis.getAsLong() + delay;
    }
}
