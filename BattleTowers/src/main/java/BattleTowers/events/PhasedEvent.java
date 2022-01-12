package BattleTowers.events;

import BattleTowers.BattleTowers;
import BattleTowers.events.phases.CombatPhase;
import BattleTowers.events.phases.EventPhase;
import BattleTowers.events.phases.ImageEventPhase;
import BattleTowers.events.phases.TextPhase;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.AbstractImageEvent;
import com.megacrit.cardcrawl.events.GenericEventDialog;
import com.megacrit.cardcrawl.events.RoomEventDialog;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import com.megacrit.cardcrawl.vfx.scene.EventBgParticle;

import java.util.HashMap;
import java.util.Map;

public abstract class PhasedEvent extends AbstractImageEvent {
    private final Map<Object, EventPhase> phases;
    public EventPhase currentPhase;
    public boolean allowRarityAltering = true;

    public PhasedEvent(String title, String imgUrl) {
        super(title, "", imgUrl);
        phases = new HashMap<>();
    }

    public void registerPhase(Object key, EventPhase phase) {
        phases.put(key, phase);
    }
    public void transitionKey(Object key) {
        transitionPhase(phases.get(key));
    }
    public void transitionPhase(EventPhase next) {
        if (currentPhase == null) {
            if (next instanceof TextPhase) {
                currentPhase = next;

                this.body = ((TextPhase) next).getBody();
                ((TextPhase) next).setOptions(this);
            }
            else {
                BattleTowers.logger.error("Attempted to start event with non-TextPhase.");
            }
        }
        else {
            currentPhase.hide(this);
            currentPhase = next;
            next.transition(this);
        }
    }

    @Override
    public void onEnterRoom() {
        super.onEnterRoom();
    }

    @Override
    protected void buttonEffect(int i) {
        if (currentPhase instanceof ImageEventPhase) {
            ((ImageEventPhase) currentPhase).optionChosen(i);
        }
    }

    @Override
    public void reopen() {
        if (currentPhase instanceof CombatPhase && ((CombatPhase) currentPhase).hasFollowup()) {
            AbstractDungeon.resetPlayer();
            AbstractDungeon.player.preBattlePrep();
            this.finishCombat();
            ((CombatPhase) currentPhase).postCombat(this);
        }
        else {
            AbstractDungeon.getCurrRoom().phase = AbstractRoom.RoomPhase.COMPLETE;
            openMap();
        }
    }

    private boolean started = false;
    @Override
    public void update() {
        if (!this.combatTime) {
            this.hasFocus = true;
            if (MathUtils.randomBoolean(0.1F)) {
                AbstractDungeon.effectList.add(new EventBgParticle());
            }

            if (!started) {
                this.waitTimer -= Gdx.graphics.getDeltaTime();
                if (this.waitTimer <= 0.0F) {
                    started = true;
                    this.imageEventText.show(this.title, this.body);
                    this.waitTimer = 0.0F;
                }
            }
            else {
                if (!GenericEventDialog.waitForInput) {
                    this.buttonEffect(GenericEventDialog.getSelectedOption());
                }
                else if (!RoomEventDialog.waitForInput) {
                    this.buttonEffect(this.roomEventText.getSelectedOption());
                }

                if (currentPhase != null)
                    currentPhase.update();
            }
        }
    }

    @Override
    public void render(SpriteBatch sb) {
        if (currentPhase != null)
            currentPhase.render(sb);
    }

    @Override
    public void renderAboveTopPanel(SpriteBatch sb) {
        if (currentPhase != null)
            currentPhase.renderAboveTopPanel(sb);
    }

    @Override
    public void enterCombat() {
        AbstractDungeon.getCurrRoom().smoked = false;
        AbstractDungeon.player.isEscaping = false;
        AbstractDungeon.getCurrRoom().phase = AbstractRoom.RoomPhase.COMBAT;
        AbstractDungeon.getCurrRoom().monsters.init();
        AbstractRoom.waitTimer = 0.1F;
        AbstractDungeon.player.preBattlePrep();
        this.hasFocus = false;
        CardCrawlGame.fadeIn(1.5F);
        AbstractDungeon.rs = AbstractDungeon.RenderScene.NORMAL;
        this.combatTime = true;
    }

    public void finishCombat() {
        AbstractDungeon.getCurrRoom().phase = AbstractRoom.RoomPhase.EVENT;
        AbstractDungeon.getCurrRoom().isBattleOver = false;
        AbstractDungeon.getCurrRoom().monsters.monsters.clear();
        AbstractDungeon.getCurrRoom().rewards.clear();
        //this.hasDialog = true; only relevant for non-ImageEvents
        this.hasFocus = true;
        this.combatTime = false;
        CardCrawlGame.fadeIn(1.5F);
    }

    //see patches.EventPlayerRender
    public boolean renderPlayer() {
        return currentPhase instanceof CombatPhase || AbstractDungeon.rs == AbstractDungeon.RenderScene.NORMAL;
    }

    public void resetCardRarity() {
        setCardRarity(3, 37);
    }
    public void setCardRarity(int baseUncommonChance, int baseRareChance) {
        AbstractDungeon.getCurrRoom().baseUncommonCardChance = baseUncommonChance;
        AbstractDungeon.getCurrRoom().baseRareCardChance = baseRareChance;
    }
}
