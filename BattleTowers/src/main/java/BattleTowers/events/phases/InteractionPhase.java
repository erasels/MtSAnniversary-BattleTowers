package BattleTowers.events.phases;

import BattleTowers.events.PhasedEvent;
import BattleTowers.util.Method;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.megacrit.cardcrawl.events.GenericEventDialog;

import java.util.function.Consumer;

public class InteractionPhase extends EventPhase {
    private final InteractionHandler handler;

    public InteractionPhase(Method update, Consumer<SpriteBatch> render) {
        this.handler = new InteractionHandler() {
            @Override
            public void update() {
                update.execute();
            }

            @Override
            public void render(SpriteBatch sb) {
                render.accept(sb);
            }
        };
    }
    public InteractionPhase(InteractionHandler handler) {
        this.handler = handler;
    }

    @Override
    public void transition(PhasedEvent event) {
        event.imageEventText.clearAllDialogs();
        GenericEventDialog.hide();
        handler.begin(event);
    }

    @Override
    public void update() {
        handler.update();
    }

    @Override
    public void render(SpriteBatch sb) {
        handler.render(sb);
    }

    @Override
    public void renderAboveTopPanel(SpriteBatch sb) {
        handler.renderAboveTopPanel(sb);
    }

    public interface InteractionHandler {
        default void begin(PhasedEvent event) {};
        default void update() {};
        default void render(SpriteBatch sb) {};
        default void renderAboveTopPanel(SpriteBatch sb) {};
    }
}
