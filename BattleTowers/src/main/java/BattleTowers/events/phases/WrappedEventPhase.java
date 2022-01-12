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

public class WrappedEventPhase extends ImageEventPhase {
    private PhasedEvent event;
    private Object followupKey = null;
    private final String eventKey;
    private boolean imageEvent = true;

    private AbstractEvent baseEvent = null;

    public WrappedEventPhase(String eventKey) {
        this.eventKey = eventKey;
    }

    @Override
    public void transition(PhasedEvent event) {
        this.event = event;
        baseEvent = EventHelper.getEvent(eventKey);
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
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public boolean finish() {
        if (followupKey != null && event != null) {
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
