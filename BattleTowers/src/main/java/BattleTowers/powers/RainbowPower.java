package BattleTowers.powers;

import BattleTowers.powers.abstracts.AbstractBTPower;
import BattleTowers.util.UC;
import basemod.interfaces.CloneablePowerInterface;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.evacipated.cardcrawl.mod.stslib.actions.tempHp.AddTemporaryHPAction;
import com.megacrit.cardcrawl.actions.utility.UseCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.PowerStrings;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.powers.BufferPower;
import com.megacrit.cardcrawl.powers.LoseStrengthPower;
import com.megacrit.cardcrawl.powers.StrengthPower;

import static BattleTowers.BattleTowers.makeID;

public class RainbowPower extends AbstractBTPower implements CloneablePowerInterface {
    public static final String POWER_ID = makeID(RainbowPower.class.getSimpleName());
    private static final PowerStrings powerStrings = CardCrawlGame.languagePack.getPowerStrings(POWER_ID);
    public static final String NAME = powerStrings.NAME;
    public static final String[] DESCRIPTIONS = powerStrings.DESCRIPTIONS;
    private final int timerOffset;

    private static final int BUFFER = 1;
    private static final int STR = 2;
    private static final int TEMP_HP = 3;

    public RainbowPower(AbstractCreature owner) {
        this(owner, AbstractDungeon.miscRng.random(0, 5000));
    }

    public RainbowPower(AbstractCreature owner, int timerOffset) {
        this.name = NAME;
        this.ID = POWER_ID;
        this.owner = owner;
        this.updateDescription();
        this.type = PowerType.BUFF;
        setImage("Rainbow_big.png", "Rainbow_small.png");
        this.timerOffset = timerOffset;
    }
    public void updateDescription() {
        description = String.format(DESCRIPTIONS[0], STR, TEMP_HP, BUFFER);
    }

    @Override
    public void renderIcons(SpriteBatch sb, float x, float y, Color c) {
        Color backup = new Color(c);
        c.set(
                (MathUtils.cosDeg((float)((System.currentTimeMillis() + timerOffset) / 10L % 360L)) + 1.25F) / 2.3F,
                (MathUtils.cosDeg((float)((System.currentTimeMillis() + 1000L + timerOffset) / 10L % 360L)) + 1.25F) / 2.3F,
                (MathUtils.cosDeg((float)((System.currentTimeMillis() + 2000L + timerOffset) / 10L % 360L)) + 1.25F) / 2.3F,
                c.a);
        super.renderIcons(sb, x, y, c);
        c.set(backup);
    }

    @Override
    public void onAfterUseCard(AbstractCard card, UseCardAction action) {
        switch (card.type) {
            case POWER:
                UC.doPow(owner, owner, new BufferPower(owner, BUFFER), false);
                break;
            case SKILL:
                UC.atb(new AddTemporaryHPAction(owner, owner, TEMP_HP));
                break;
            case ATTACK:
                UC.doPow(owner, owner, new StrengthPower(owner, STR), false);
                UC.doPow(owner, owner, new LoseStrengthPower(owner, STR), false);
        }
    }

    @Override
    public AbstractPower makeCopy() {
        return new RainbowPower(owner, timerOffset);
    }
}