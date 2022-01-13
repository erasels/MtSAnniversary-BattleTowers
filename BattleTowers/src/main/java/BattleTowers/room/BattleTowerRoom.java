package BattleTowers.room;

import BattleTowers.events.TowerEvent;
import BattleTowers.util.TextureLoader;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.random.Random;
import com.megacrit.cardcrawl.rooms.AbstractRoom;

import static BattleTowers.BattleTowers.*;

public class BattleTowerRoom extends AbstractRoom {
    //Todo- handle save and quit mid-tower
    //see - nextRoomTransition of AbstractDungeon used when loading a save
    public Random towerRng;

    private AbstractDungeon.RenderScene intendedRs = null;

    //When created, determine tower properties using map rng
    public BattleTowerRoom() {
        super();

        long seed = AbstractDungeon.mapRng.randomLong();
        towerRng = new Random(seed);

        this.phase = RoomPhase.EVENT;

        this.mapSymbol = "@"; //█
        this.mapImg = TextureLoader.getTexture(makeUIPath("replacethis.png"));
        this.mapImgOutline = TextureLoader.getTexture(makeUIPath("replacethisoutline.png"));

        this.event = new TowerEvent(this);

        logger.info("Battle Tower generated with seed " + seed);
    }

    @Override
    public void onPlayerEntry() {
        AbstractDungeon.overlayMenu.proceedButton.hide();
        this.event.onEnterRoom();
        intendedRs = AbstractDungeon.rs;
    }

    public void update() {
        if (intendedRs != null) {
            AbstractDungeon.rs = intendedRs;
            intendedRs = null;
        }

        super.update();
        if (!AbstractDungeon.isScreenUp) {
            this.event.update();
        }

        if (this.event.waitTimer == 0.0F && !this.event.hasFocus && this.phase != RoomPhase.COMBAT) {
            this.event.reopen();
        }
    }

    public void render(SpriteBatch sb) {
        if (this.event != null) {
            this.event.render(sb);
        }

        super.render(sb);
    }

    public void renderAboveTopPanel(SpriteBatch sb) {
        super.renderAboveTopPanel(sb);
        if (this.event != null) {
            this.event.renderAboveTopPanel(sb);
        }
    }
}
