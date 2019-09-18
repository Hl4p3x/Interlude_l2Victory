package ru.j2dev.gameserver.ai;

public enum CtrlEvent {
    /**
     * Something has changed, usually a previous step has being completed
     * or maybe was completed, the AI must thing on next action
     */
    EVT_THINK, /**
     * The actor was attacked. This event comes each time a physical or magical
     * attack was done on the actor. NPC may start attack in responce, or ignore
     * this event if they already attack someone, or change target and so on.
     */
    EVT_ATTACKED,
    EVT_CLAN_ATTACKED, /**
     * Increase/decrease aggression towards a target, or reduce global aggression if target is null
     */
    EVT_AGGRESSION,
    EVT_MANIPULATION, /**
     * An event that previous action was completed. The action may be an attempt
     * to physically/magically hit an enemy, or an action that discarded
     * attack attempt has finished.
     */
    EVT_READY_TO_ACT, /**
     * The actor arrived to assigned location, or it's a time to modify
     * movement destination (follow, interact, random move and others intentions).
     */
    EVT_ARRIVED,
    EVT_ARRIVED_TARGET, /**
     * The actor cannot move anymore.
     */
    EVT_ARRIVED_BLOCKED, /**
     * Forgets an object (if it's used as attack target, follow target and so on
     */
    EVT_FORGET_OBJECT, /**
     * The character is dead
     */
    EVT_DEAD, /**
     * The character looks like dead
     */
    EVT_FAKE_DEATH, /**
     * The character finish casting
     **/
    EVT_FINISH_CASTING,
    EVT_SEE_SPELL,
    EVT_SPAWN,
    EVT_DESPAWN,
    EVT_TIMER,
    EVT_TELEPORTED,
    EVT_OUT_OF_MY_TERRITORY,
    EVT_SCRIPT_EVENT,
    EVT_NODE_ARRIVED,
    EVT_SPELLED,
    EVT_SEE_CREATURE,
    EVT_CREATURE_LOST,
    EVT_ABNORMAL_STATUS_CHANGED,
    EVT_PARTY_DIED,
    EVT_CLAN_DIED,
    EVT_DIE_SET,
    EVT_PARTY_ATTACKED,
    EVT_NO_DESIRE,
    EVT_TRAP_STEP_IN,
    EVT_TRAP_STEP_OUT,
    EVT_TRAP_ACTIVATED,
    EVT_TRAP_DETECTED,
    EVT_TRAP_DEFUSED,

    /**
     * Menu selector from L2OFF(PTS)
     */
    EVT_MENU_SELECTED,
    EVT_TIMER_FIRED_EX,
    /**
     * Door events
     */
    EVT_DBLCLICK,
    EVT_OPEN,
    EVT_CLOSE
}
