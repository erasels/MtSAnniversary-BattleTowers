package BattleTowers.monsters;

import BattleTowers.BattleTowers;
import BattleTowers.util.TextureLoader;
import BattleTowers.vfx.LouseMonsterParticle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

import java.util.ArrayList;

public class LouseHorde extends AbstractMonster {

    public static String ID = BattleTowers.makeID("LouseHorde");

    private ArrayList<LouseMonsterParticle> louseTextures = new ArrayList<>();


    public LouseHorde() {
        super("Temp", ID, 300, 0f, 0f, 200f, 200f, null, 0f, 0f);

        img = TextureLoader.getTexture("battleTowersResources/img/ui/emptyTexture.png");
    }

    @Override
    public void update() {
        super.update();

        // Add new louse to the screen
        if (louseTextures.size() < 25) {
            louseTextures.add(
                    new LouseMonsterParticle(
                            new Vector2(this.hb.cX, this.hb.cY), 200f
                    )
            );
        }

        // If monster is dying kill the particles too.
        if (isDying || isDead) {
            louseTextures.forEach(LouseMonsterParticle::setDying);
        }
        louseTextures.forEach(LouseMonsterParticle::update);
        louseTextures.removeIf(LouseMonsterParticle::isDead);
    }

    @Override
    public void render(SpriteBatch sb) {
        // actually render the louse
        louseTextures.forEach(louse -> louse.render(sb));

        super.render(sb);
    }

    @Override
    public void takeTurn() {

    }

    @Override
    protected void getMove(int i) {
        setMove((byte)0, Intent.MAGIC);
    }
}
