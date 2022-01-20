package BattleTowers.powers;

import BattleTowers.actions.BonkChampionAction;
import BattleTowers.monsters.AspiringChampion;
import BattleTowers.util.TextureLoader;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.actions.common.DamageAllEnemiesAction;
import com.megacrit.cardcrawl.actions.utility.UseCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.PowerStrings;
import com.megacrit.cardcrawl.powers.AbstractPower;

import static BattleTowers.BattleTowers.makeID;
import static BattleTowers.BattleTowers.makePowerPath;

public class ClunkyPower extends AbstractPower {
    public static final String POWER_ID = makeID(ClunkyPower.class.getSimpleName());
    private static final PowerStrings powerStrings = CardCrawlGame.languagePack.getPowerStrings(POWER_ID);
    public static final String NAME = powerStrings.NAME;
    public static final String[] DESCRIPTIONS = powerStrings.DESCRIPTIONS;
    
    private boolean triggeredThisTurn = false;
    
    public ClunkyPower(AspiringChampion owner) {
        this.name = NAME;
        this.ID = POWER_ID;
        this.owner = owner;
        this.amount = 3;
        this.updateDescription();
        this.type = PowerType.DEBUFF;
        Texture normalTexture = TextureLoader.getTexture(makePowerPath("ClunkyPower32.png"));
        Texture hiDefImage = TextureLoader.getTexture(makePowerPath("ClunkyPower84.png"));
        if (hiDefImage != null) {
            region128 = new TextureAtlas.AtlasRegion(hiDefImage, 0, 0, hiDefImage.getWidth(), hiDefImage.getHeight());
            if (normalTexture != null)
                region48 = new TextureAtlas.AtlasRegion(normalTexture, 0, 0, normalTexture.getWidth(), normalTexture.getHeight());
        } else if (normalTexture != null) {
            this.img = normalTexture;
            region48 = new TextureAtlas.AtlasRegion(normalTexture, 0, 0, normalTexture.getWidth(), normalTexture.getHeight());
        }
    }
    
    @Override
    public void onUseCard(AbstractCard card, UseCardAction action)
    {
        if(this.amount > 0 && card.type == AbstractCard.CardType.ATTACK) {
            --this.amount;
            this.updateDescription();
            if (this.amount == 0) {
                this.flash();
                this.addToBot(new BonkChampionAction((AspiringChampion) this.owner));
            }
        }
    }
    
    public void atStartOfTurn() {
        this.amount = 3;
        this.updateDescription();
    }
    
    @Override
    public void atEndOfRound() {
        triggeredThisTurn = false;
    }
    
    @Override
    public void updateDescription() {
        this.description = DESCRIPTIONS[0] + this.amount + DESCRIPTIONS[1];
    }
}
