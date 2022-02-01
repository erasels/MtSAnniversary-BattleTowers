package BattleTowers.powers;

import BattleTowers.BattleTowers;
import BattleTowers.monsters.Assassin;
import basemod.interfaces.CloneablePowerInterface;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.megacrit.cardcrawl.actions.common.RemoveAllBlockAction;
import com.megacrit.cardcrawl.actions.common.RemoveSpecificPowerAction;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.PowerStrings;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.powers.BarricadePower;
import com.megacrit.cardcrawl.powers.StrengthPower;


//Gain 1 dex for the turn for each card played.

public class HidePower extends AbstractPower implements CloneablePowerInterface {
    public AbstractCreature source;
    public float particleTimer;
    public float particleTimer2;

    public static final String POWER_ID = BattleTowers.makeID("HidePower");
    private static final PowerStrings powerStrings = CardCrawlGame.languagePack.getPowerStrings(POWER_ID);
    public static final String NAME = powerStrings.NAME;
    public static final String[] DESCRIPTIONS = powerStrings.DESCRIPTIONS;

    // We create 2 new textures *Using This Specific Texture Loader* - an 84x84 image and a 32x32 one.
    // There's a fallback "missing texture" image, so the game shouldn't crash if you accidentally put a non-existent file.
    //private static final Texture tex84 = TextureLoader.getTexture(makePowerPath("honeypower84.png"));
    //private static final Texture tex32 = TextureLoader.getTexture(makePowerPath("honeypower32.png"));

    public HidePower(final AbstractCreature owner, final AbstractCreature source, final int amount) {
        name = NAME;
        ID = POWER_ID;

        this.owner = owner;
        this.amount = amount;
        this.source = source;

        type = PowerType.BUFF;
        isTurnBased = false;

        // We load those txtures here.
        //this.region128 = new TextureAtlas.AtlasRegion(tex84, 0, 0, 84, 84);
        //this.region48 = new TextureAtlas.AtlasRegion(tex32, 0, 0, 32, 32);
        this.loadRegion("blur");
        updateDescription();
    }

    public void removeHide(){
        this.flash();
        if (this.owner.hasPower(StrengthPower.POWER_ID)) {
            if (this.owner.getPower(StrengthPower.POWER_ID).amount > 0) {
                AbstractDungeon.actionManager.addToBottom(new RemoveSpecificPowerAction(this.owner, this.owner, this.owner.getPower(StrengthPower.POWER_ID)));

            }
        }
        if (this.owner.hasPower(BarricadePower.POWER_ID)) {
                AbstractDungeon.actionManager.addToBottom(new RemoveSpecificPowerAction(this.owner, this.owner, this.owner.getPower(BarricadePower.POWER_ID)));
        }

        AbstractDungeon.actionManager.addToBottom(new RemoveSpecificPowerAction(this.owner, this.owner, this));
    }

    // Update the description when you apply this power. (i.e. add or remove an "s" in keyword(s))
    @Override
    public void updateDescription() {
        description = DESCRIPTIONS[0];
    }

    @Override
    public AbstractPower makeCopy() {
        return new HidePower(owner, source, amount);
    }

    @SpirePatch(
            clz= RemoveAllBlockAction.class,
            method="update",
            paramtypez = {}
    )
    public static class fuckingMelter {
        @SpirePrefixPatch
        public static void fuckingMelterPatch(RemoveAllBlockAction __instance) {
            if (__instance.target != null) {
                HidePower h = (HidePower) __instance.target.getPower(HidePower.POWER_ID);
                if (h != null) {
                    h.removeHide();
                    if (__instance.target instanceof Assassin) {
                        Assassin a = (Assassin) __instance.target;
                        a.changeState("ArmorBreak");
                    }
                }
            }
        }
    }
}

