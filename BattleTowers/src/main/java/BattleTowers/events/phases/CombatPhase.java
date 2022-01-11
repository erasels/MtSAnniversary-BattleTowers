package BattleTowers.events.phases;

import BattleTowers.events.PhasedEvent;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.MonsterHelper;

public class CombatPhase extends EventPhase {
    //For combat rewards: See AbstractRoom's update method.
    private final String encounterKey;
    private final boolean allowRewards;
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
        followupType = FollowupType.NONE;
    }

    public CombatPhase setNextPhase(EventPhase postCombat) {
        followup = postCombat;
        followupType = FollowupType.PHASE;
        return this;
    }
    public CombatPhase setNextKey(Object postCombatKey) {
        key = postCombatKey;
        followupType = FollowupType.KEY;
        return this;
    }

    public boolean hasFollowup() {
        return followupType != FollowupType.NONE;
    }

    public void postCombat(PhasedEvent event) {
        switch (followupType) {
            case PHASE:
                event.transitionPhase(followup);
                break;
            case KEY:
                event.transitionKey(key);
                break;
        }
    }

    @Override
    public void transition(PhasedEvent event) {
        AbstractDungeon.getCurrRoom().monsters = MonsterHelper.getEncounter(encounterKey);
        AbstractDungeon.lastCombatMetricKey = encounterKey;

        AbstractDungeon.getCurrRoom().rewards.clear();
        AbstractDungeon.getCurrRoom().rewardAllowed = allowRewards;

        if (encounterKey.equals("Shield and Spear")) {
            AbstractDungeon.player.movePosition((float) Settings.WIDTH / 2.0F, AbstractDungeon.floorY);
        } else {
            AbstractDungeon.player.movePosition((float)Settings.WIDTH * 0.25F, AbstractDungeon.floorY);
            AbstractDungeon.player.flipHorizontal = false;
        }
        event.enterCombat();

        if (followupType == FollowupType.NONE) {
            event.currentPhase = null;
        }
        else if (allowRewards) {
            //has a followup and has rewards
            event.waitTimer = 69; //Set to a non-0 value (see EventRoom's update method and patches.ShowEventLater)
        }
    }

    @Override
    public void hide(PhasedEvent event) {

    }
}
