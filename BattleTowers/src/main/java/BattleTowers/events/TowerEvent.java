package BattleTowers.events;

import BattleTowers.events.phases.InteractionPhase;
import BattleTowers.events.phases.TextPhase;
import BattleTowers.room.BattleTowerRoom;
import BattleTowers.towers.BattleTower;
import BattleTowers.util.TextureLoader;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.helpers.MathHelper;
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


    private static class MapHandler implements InteractionPhase.InteractionHandler {
        private static final Color NOT_TAKEN_COLOR = new Color(0.34F, 0.34F, 0.34F, 1.0F);
        private static final float MAX_Y = Settings.HEIGHT - (80.0f * Settings.scale);

        private final Texture mapTexture = TextureLoader.getTexture(makeUIPath("minimap.png"));
        private Color baseMapColor;

        private float targetAlpha;
        private float height;
        private float renderY;

        public MapHandler() {
            baseMapColor = Color.WHITE.cpy();
            baseMapColor.a = 0;
            targetAlpha = 0;

            height = (Settings.WIDTH / 1920.0f) * 1600.0f;
            renderY = MAX_Y - height;
        }

        @Override
        public void begin(PhasedEvent event) {
            targetAlpha = 1;
            if (MathUtils.randomBoolean()) {
                CardCrawlGame.sound.play("MAP_OPEN", 0.1F);
            } else {
                CardCrawlGame.sound.play("MAP_OPEN_2", 0.1F);
            }
        }

        @Override
        public void update() {
            this.baseMapColor.a = MathHelper.fadeLerpSnap(this.baseMapColor.a, this.targetAlpha);
        }

        @Override
        public void render(SpriteBatch sb) {
            sb.setColor(baseMapColor);
            sb.draw(mapTexture, 0, renderY, Settings.WIDTH, height);
        }
    }
}
