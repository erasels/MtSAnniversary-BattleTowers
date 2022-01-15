package BattleTowers.powers;

import BattleTowers.util.TextureLoader;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.localization.PowerStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.AbstractPower;

import static BattleTowers.BattleTowers.makeID;
import static BattleTowers.BattleTowers.makePowerPath;

public class NotificationPower extends AbstractPower {
    public static final String POWER_ID = makeID(NotificationPower.class.getSimpleName());
    private static final PowerStrings powerStrings = CardCrawlGame.languagePack.getPowerStrings(POWER_ID);
    public static final String NAME = powerStrings.NAME;
    public static final String[] DESCRIPTIONS = powerStrings.DESCRIPTIONS;

    public NotificationPower(AbstractMonster owner) {
        name = NAME;
        ID = POWER_ID;
        this.owner = owner;
        this.amount = -1;
        canGoNegative = false;
        type = PowerType.BUFF;
        Texture normalTexture = TextureLoader.getTexture(makePowerPath("CardEater_32.png"));
        Texture hiDefImage = TextureLoader.getTexture(makePowerPath("CardEater_84.png"));
        if (hiDefImage != null) {
            region128 = new TextureAtlas.AtlasRegion(hiDefImage, 0, 0, hiDefImage.getWidth(), hiDefImage.getHeight());
            if (normalTexture != null)
                region48 = new TextureAtlas.AtlasRegion(normalTexture, 0, 0, normalTexture.getWidth(), normalTexture.getHeight());
        } else if (normalTexture != null) {
            this.img = normalTexture;
            region48 = new TextureAtlas.AtlasRegion(normalTexture, 0, 0, normalTexture.getWidth(), normalTexture.getHeight());
        }
        updateDescription();
    }


    @Override
    public void updateDescription() {
        description = DESCRIPTIONS[0];
    }
}
