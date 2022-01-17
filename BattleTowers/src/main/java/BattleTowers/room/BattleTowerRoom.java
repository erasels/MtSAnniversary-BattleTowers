package BattleTowers.room;

import BattleTowers.events.TowerEvent;
import BattleTowers.util.TextureLoader;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.AbstractEvent;
import com.megacrit.cardcrawl.random.Random;
import com.megacrit.cardcrawl.rooms.AbstractRoom;

import static BattleTowers.BattleTowers.*;

public class BattleTowerRoom extends AbstractRoom {
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
        AbstractDungeon.RenderScene originalRs = AbstractDungeon.rs;
        AbstractDungeon.rs = null;

        AbstractEvent.type = AbstractEvent.EventType.IMAGE;
        this.event.onEnterRoom();
        if (AbstractDungeon.rs != null) {
            intendedRs = AbstractDungeon.rs;
        }
        AbstractDungeon.rs = originalRs;
    }

    @Override
    public void dropReward() {
        if (event instanceof TowerEvent) {
            ((TowerEvent) event).dropReward(this);
        }
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
