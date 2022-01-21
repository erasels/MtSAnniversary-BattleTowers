package BattleTowers.monsters.CardboardGolem.powers;

import BattleTowers.cardmods.CardWillBeEatenMod;
import BattleTowers.util.TextureLoader;
import basemod.helpers.CardModifierManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.PowerStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.AbstractPower;

import static BattleTowers.BattleTowers.makeID;
import static BattleTowers.BattleTowers.makePowerPath;

public class CardEaterPower extends AbstractPower {
    public static final String POWER_ID = makeID(CardEaterPower.class.getSimpleName());
    private static final PowerStrings powerStrings = CardCrawlGame.languagePack.getPowerStrings(POWER_ID);
    public static final String NAME = powerStrings.NAME;
    public static final String[] DESCRIPTIONS = powerStrings.DESCRIPTIONS;

    public CardEaterPower(AbstractMonster owner, int amount) {
        name = NAME;
        ID = POWER_ID;
        this.owner = owner;
        this.amount = amount;
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
    public void atStartOfTurnPostDraw() {
        AbstractPower p = this;
        addToBot(new AbstractGameAction() {
            @Override
            public void update() {
                p.flash();
                isDone = true;
                AbstractCard chosen = AbstractDungeon.player.hand.getRandomCard(AbstractDungeon.cardRandomRng);
                CardModifierManager.addModifier(chosen, new CardWillBeEatenMod());
                chosen.flash(Color.RED.cpy());
            }
        });
    }

    @Override
    public void updateDescription() {
        description = DESCRIPTIONS[0] + amount + DESCRIPTIONS[1];
    }
}
