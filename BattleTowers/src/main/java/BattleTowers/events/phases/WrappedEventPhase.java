package BattleTowers.events.phases;

import BattleTowers.events.PhasedEvent;
import BattleTowers.patches.EventWrapping;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.AbstractEvent;
import com.megacrit.cardcrawl.events.AbstractImageEvent;
import com.megacrit.cardcrawl.events.GenericEventDialog;
import com.megacrit.cardcrawl.helpers.EventHelper;
import com.megacrit.cardcrawl.rooms.AbstractRoom;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static BattleTowers.BattleTowers.logger;

//Note - probably only works right for pure image events.
//Ones with weird stuff like colosseum will probably not work right.
public class WrappedEventPhase extends ImageEventPhase {
    private PhasedEvent event;
    private final String eventKey;
    private boolean imageEvent = true;

    private AbstractEvent baseEvent = null;

    public Object followupKey = null;
    public WrappedEventPhase(String eventKey) {
        this.eventKey = eventKey;
    }

    @Override
    public void transition(PhasedEvent event) {
        this.event = event;
        baseEvent = EventHelper.getEvent(eventKey);
        if (!(baseEvent instanceof AbstractImageEvent)) {
            logger.warn("Wrapped event phase used for a non-image event");
        }
        AbstractDungeon.getCurrRoom().phase = AbstractRoom.RoomPhase.EVENT;

        event.resetCardRarity();
        event.allowRarityAltering = true;

        if (baseEvent instanceof AbstractImageEvent) {
            AbstractDungeon.rs = AbstractDungeon.RenderScene.EVENT;
        }
        else {
            imageEvent = false;
            AbstractDungeon.rs = AbstractDungeon.RenderScene.NORMAL;
        }

        if (baseEvent != null) {
            EventWrapping.Field.wrapper.set(baseEvent, this);
            event.imageEventText = baseEvent.imageEventText;
            event.roomEventText = baseEvent.roomEventText;

            baseEvent.onEnterRoom();

            if (imageEvent) {
                GenericEventDialog.show();
            }
            else {
                event.hasDialog = true;
            }
        }
        else {
            logger.error("Attempted to transition to wrapped event with non-existent key " + eventKey);
        }
    }

    @Override
    public void hide(PhasedEvent event) {
        super.hide(event);
        if (!imageEvent) {
            event.roomEventText.clear();
            event.roomEventText.hide();
        }
    }

    public EventPhase setNextKey(Object key) {
        this.followupKey = key;
        return this;
    }

    private static Method buttonEffect;
    static {
        try {
            buttonEffect = AbstractEvent.class.getDeclaredMethod("buttonEffect", int.class);
            buttonEffect.setAccessible(true);
        }
        catch (NoSuchMethodException e) {
            logger.error("Failed to access AbstractEvent's buttonEffect method.");
            e.printStackTrace();
        }
    }
    @Override
    public void optionChosen(int i) {
        try {
            buttonEffect.invoke(baseEvent, i);

            if (baseEvent != null) { //becomes null when openMap is called
                event.combatTime = baseEvent.combatTime;
                event.hasFocus = baseEvent.hasFocus;
                if (baseEvent instanceof PhasedEvent) {
                    event.allowRarityAltering = ((PhasedEvent) baseEvent).allowRarityAltering;
                }
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean reopen(PhasedEvent phasedEvent) {
        if (baseEvent != null) {
            baseEvent.reopen();
            if (baseEvent != null) {
                phasedEvent.waitTimer = baseEvent.waitTimer;
                phasedEvent.combatTime = baseEvent.combatTime;
                phasedEvent.hasFocus = baseEvent.hasFocus;
            }
            else if (followupKey != null) { //base event opened map, called finish and is now done
                AbstractDungeon.resetPlayer();
                phasedEvent.finishCombat();
            }
            else { //base event is finished, no followup
                return false;
            }
            return true;
        }
        return false;
    }

    public boolean finish() {
        if (followupKey != null && event != null) {
            baseEvent = null;
            event.transitionKey(followupKey);
            return true;
        }
        return false;
    }

    @Override
    public void update() {
        if (baseEvent != null)
            baseEvent.update();
    }

    @Override
    public void render(SpriteBatch sb) {
        if (baseEvent != null)
            baseEvent.render(sb);
    }

    @Override
    public void renderAboveTopPanel(SpriteBatch sb) {
        if (baseEvent != null)
            baseEvent.renderAboveTopPanel(sb);
    }
}
