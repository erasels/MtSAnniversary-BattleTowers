package BattleTowers.events.phases;

import BattleTowers.events.PhasedEvent;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public abstract class EventPhase {
    public abstract void transition(PhasedEvent event);
    public abstract void hide(PhasedEvent event);

    public void update() {

    }
    public void render(SpriteBatch sb) {

    }
    public void renderAboveTopPanel(SpriteBatch sb) {

    }

}
