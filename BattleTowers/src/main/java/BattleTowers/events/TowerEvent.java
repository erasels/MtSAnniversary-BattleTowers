package BattleTowers.events;

import BattleTowers.events.phases.InteractionPhase;
import BattleTowers.events.phases.TextPhase;
import BattleTowers.room.BattleTowerRoom;
import BattleTowers.towers.BattleTower;
import BattleTowers.util.TextureLoader;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.localization.EventStrings;

import static BattleTowers.BattleTowers.*;

public class TowerEvent extends PhasedEvent {
    private static final EventStrings eventStrings = CardCrawlGame.languagePack.getEventString(makeID("TowerEnter"));
    private static final String[] DESCRIPTIONS = eventStrings.DESCRIPTIONS;
    private static final String[] OPTIONS = eventStrings.OPTIONS;
    private static final String title = eventStrings.NAME;

    private static final int NUM_OPTIONS = 3;

    private MapHandler mapHandler;

    public TowerEvent(BattleTowerRoom room) {
        super(title, "images/events/theNest.jpg");

        TextPhase initial = new TextPhase(DESCRIPTIONS[0]);

        for (int i = 0; i < NUM_OPTIONS; ++i) {
            BattleTower t = new BattleTower(room.towerRng);
            initial.addOption(OPTIONS[0] + t.getTitle(), ()->this.transitionKey("map"));
        }
        registerPhase("enter", initial);
        registerPhase("map", new InteractionPhase(mapHandler = new MapHandler()));

        transitionKey("enter");
    }


    @Override
    public void onEnterRoom() {
        super.onEnterRoom();
    }


    private static class MapHandler implements InteractionPhase.InteractionHandler {
        private final Texture mapTexture = TextureLoader.getTexture(makeUIPath("minimap.png"));
        private final Color renderColor = Color.WHITE.cpy();

        @Override
        public void begin(PhasedEvent event) {

        }

        @Override
        public void update() {

        }

        @Override
        public void render(SpriteBatch sb) {
            sb.setColor(renderColor);
            sb.draw(mapTexture, 0, 0);
        }
    }
}
