package BattleTowers.events;

import BattleTowers.BattleTowers;
import BattleTowers.events.phases.CombatPhase;
import BattleTowers.events.phases.EventPhase;
import BattleTowers.events.phases.TextPhase;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.AbstractImageEvent;
import com.megacrit.cardcrawl.events.GenericEventDialog;
import com.megacrit.cardcrawl.vfx.scene.EventBgParticle;

import java.util.HashMap;
import java.util.Map;

public abstract class PhasedEvent extends AbstractImageEvent {
    private final Map<Object, EventPhase> phases;
    private EventPhase currentPhase;

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
            currentPhase = next;

            next.transition(this);
        }
    }

    @Override
    protected void buttonEffect(int i) {
        if (currentPhase instanceof TextPhase) {
            ((TextPhase) currentPhase).optionChosen(i);
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
                    this.imageEventText.show(title, this.body);
                    this.waitTimer = 0.0F;
                }
            }
            else {
                if (currentPhase != null)
                    currentPhase.update();

                if (!GenericEventDialog.waitForInput) {
                    this.buttonEffect(GenericEventDialog.getSelectedOption());
                }
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

    public boolean renderPlayer() {
        return currentPhase instanceof CombatPhase;
    }
}
