package BattleTowers.events.phases;

import BattleTowers.events.PhasedEvent;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.AbstractEvent;
import com.megacrit.cardcrawl.helpers.MonsterHelper;

import static BattleTowers.BattleTowers.logger;

public class CombatPhase extends EventPhase {
    //For combat rewards: See AbstractRoom's update method.
    private final String encounterKey;
    private final boolean allowRewards;
    public boolean waitingRewards;
    //patch line 440 of AbstractRoom to prevent save if followup exists even if reward is allowed
    //(Or to adjust the save to load properly mid-tower)
    private EventPhase followup = null;
    private Object key = null;

    private FollowupType followupType;
    private enum FollowupType {
        NONE,
        PHASE,
        KEY
    }

    public CombatPhase(String encounterKey, boolean allowRewards) {
        this.encounterKey = encounterKey;
        this.allowRewards = allowRewards;
        waitingRewards = false;
        followupType = FollowupType.NONE;
    }

    public CombatPhase setNextPhase(EventPhase postCombat) {
        followup = postCombat;
        if (followup != null)
            followupType = FollowupType.PHASE;
        return this;
    }
    public CombatPhase setNextKey(Object postCombatKey) {
        key = postCombatKey;
        if (key != null)
            followupType = FollowupType.KEY;
        return this;
    }

    public boolean hasFollowup() {
        return followupType != FollowupType.NONE;
    }

    public void postCombat(PhasedEvent event) {
        if (hasFollowup()) {
            switch (followupType) {
                case PHASE:
                    event.transitionPhase(followup);
                    break;
                case KEY:
                    event.transitionKey(key);
                    break;
            }
        }
        else {
            logger.error("Reached postCombat of CombatPhase with no follow up");
        }
    }

    @Override
    public void transition(PhasedEvent event) {
        AbstractDungeon.getCurrRoom().monsters = MonsterHelper.getEncounter(encounterKey);
        AbstractDungeon.lastCombatMetricKey = encounterKey;

        AbstractEvent.type = AbstractEvent.EventType.ROOM;
        event.resetCardRarity();
        event.allowRarityAltering = true;

        AbstractDungeon.getCurrRoom().rewards.clear();
        AbstractDungeon.getCurrRoom().rewardAllowed = allowRewards;

        if (encounterKey.equals("Shield and Spear")) {
            AbstractDungeon.player.movePosition((float) Settings.WIDTH / 2.0F, AbstractDungeon.floorY);
        } else {
            AbstractDungeon.player.movePosition((float)Settings.WIDTH * 0.25F, AbstractDungeon.floorY);
            AbstractDungeon.player.flipHorizontal = false;
        }
        event.enterCombat(); //sets rs

        if (allowRewards) {
            //has a followup and has rewards
            waitingRewards = true;
        }
        /*if (followupType == FollowupType.NONE) {
            event.currentPhase = null;
        }*/
    }

    @Override
    public void hide(PhasedEvent event) {
        AbstractDungeon.getCurrRoom().monsters.monsters.clear();
        AbstractDungeon.getCurrRoom().rewards.clear();
        AbstractDungeon.getCurrRoom().cannotLose = false;
        AbstractDungeon.getCurrRoom().isBattleOver = false;
        AbstractDungeon.getCurrRoom().rewardTime = false;
    }
}
