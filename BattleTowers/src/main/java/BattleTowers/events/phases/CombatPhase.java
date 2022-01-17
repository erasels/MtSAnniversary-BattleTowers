package BattleTowers.events.phases;

import BattleTowers.events.PhasedEvent;
import BattleTowers.patches.saveload.CombatPhaseOptions;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.AbstractEvent;
import com.megacrit.cardcrawl.helpers.MonsterHelper;
import com.megacrit.cardcrawl.rooms.AbstractRoom;

import static BattleTowers.BattleTowers.logger;

public class CombatPhase extends EventPhase {
    //For combat rewards: See AbstractRoom's update method.
    private final String encounterKey;
    private final boolean allowRewards;
    private boolean cardReward;
    private final boolean postCombatSave;
    public boolean waitingRewards;
    private EventPhase followup = null;
    private Object key = null;
    private boolean isBoss;

    private boolean completed = false; //For save loading

    private FollowupType followupType;

    private enum FollowupType {
        NONE,
        PHASE,
        KEY
    }

    public CombatPhase(String encounterKey, boolean allowRewards) {
        this(encounterKey, allowRewards, false);
    }
    public CombatPhase(String encounterKey, boolean allowRewards, boolean postCombatSave) {
        this.encounterKey = encounterKey;
        this.allowRewards = allowRewards;
        this.cardReward = true;
        this.postCombatSave = postCombatSave;

        this.isBoss = false;

        waitingRewards = false;
        followupType = FollowupType.NONE;
    }
    public CombatPhase noCard() {
        cardReward = false;
        return this;
    }
    public CombatPhase completed() {
        this.completed = true;
        return this;
    }
    public CombatPhase boss() {
        this.isBoss = true;
        return this;
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

    public boolean isBoss() {
        return isBoss;
    }

    public boolean hasFollowup() {
        return followupType != FollowupType.NONE;
    }

    public void postCombatTransition(PhasedEvent event) {
        CombatPhaseOptions.allowSave();
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
            logger.error("Reached postCombatTransition of CombatPhase with no follow up");
        }
    }

    @Override
    public void transition(PhasedEvent event) {
        AbstractDungeon.getCurrRoom().cannotLose = false;
        AbstractDungeon.getCurrRoom().rewardTime = false;
        AbstractDungeon.getCurrRoom().monsters = MonsterHelper.getEncounter(encounterKey);
        AbstractDungeon.lastCombatMetricKey = encounterKey;

        AbstractEvent.type = AbstractEvent.EventType.ROOM;
        event.resetCardRarity();
        if (isBoss)
            event.setCardRarity(0, 420);
        event.allowRarityAltering = true;
        event.noCardsInRewards = !cardReward;

        AbstractDungeon.getCurrRoom().rewards.clear();
        AbstractDungeon.getCurrRoom().rewardAllowed = allowRewards;

        if (encounterKey.equals(MonsterHelper.SHIELD_SPEAR_ENC)) {
            AbstractDungeon.player.movePosition((float) Settings.WIDTH / 2.0F, AbstractDungeon.floorY);
        } else {
            AbstractDungeon.player.movePosition((float)Settings.WIDTH * 0.25F, AbstractDungeon.floorY);
            AbstractDungeon.player.flipHorizontal = false;
        }
        if (!completed) {
            event.enterCombat(); //sets rs
        }
        else {
            AbstractDungeon.getCurrRoom().smoked = false;
            AbstractDungeon.player.isEscaping = false;
            AbstractDungeon.getCurrRoom().isBattleOver = false;
            AbstractDungeon.getCurrRoom().phase = AbstractRoom.RoomPhase.COMBAT;
            AbstractDungeon.getCurrRoom().monsters.init();
            AbstractRoom.waitTimer = 0.1F;
            event.hasFocus = false;
            CardCrawlGame.fadeIn(1.5F);
            AbstractDungeon.rs = AbstractDungeon.RenderScene.NORMAL;
            event.combatTime = true;
        }

        if (allowRewards) {
            //has a followup and has rewards
            waitingRewards = true;
        }

        if (!postCombatSave)
            CombatPhaseOptions.preventSave();
        else
            CombatPhaseOptions.allowSave();
    }

    @Override
    public boolean reopen(PhasedEvent phasedEvent) {
        if (waitingRewards) {
            AbstractDungeon.getCurrRoom().phase = AbstractRoom.RoomPhase.INCOMPLETE;
            waitingRewards = false;
            phasedEvent.waitTimer = 69; //will not reopen again until reward screen is finished
            if (!hasFollowup()) {
                phasedEvent.currentPhase = null; //nothing to reopen to; will immediately transition to map on proceeding
            }
        }
        else {
            AbstractDungeon.resetPlayer();
            phasedEvent.finishCombat();
            postCombatTransition(phasedEvent);
        }
        return true;
    }

    @Override
    public void hide(PhasedEvent event) {
        AbstractDungeon.getCurrRoom().monsters.monsters.clear();
        AbstractDungeon.getCurrRoom().rewards.clear();
        AbstractDungeon.getCurrRoom().cannotLose = false;
        AbstractDungeon.getCurrRoom().isBattleOver = false;
        AbstractDungeon.getCurrRoom().rewardTime = false;
        event.noCardsInRewards = false;
    }
}
