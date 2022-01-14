package BattleTowers.powers;

import BattleTowers.monsters.ZastraszTheJusticar;
import basemod.interfaces.CloneablePowerInterface;
import com.evacipated.cardcrawl.mod.stslib.StSLib;
import com.evacipated.cardcrawl.mod.stslib.patches.NeutralPowertypePatch;
import com.evacipated.cardcrawl.mod.stslib.powers.abstracts.TwoAmountPower;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.actions.common.ReducePowerAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.PowerStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.AbstractPower;

import static BattleTowers.BattleTowers.makeID;

public class JudgementPower extends TwoAmountPower implements CloneablePowerInterface {
    public static final String POWER_ID = makeID(JudgementPower.class.getSimpleName());
    private static final PowerStrings powerStrings = CardCrawlGame.languagePack.getPowerStrings(POWER_ID);
    public static final String NAME = powerStrings.NAME;
    public static final String[] DESCRIPTIONS = powerStrings.DESCRIPTIONS;

    public JudgementPower(AbstractCreature owner) {
        this.name = NAME;
        this.ID = POWER_ID;
        this.owner = owner;
        this.amount = 15;
        this.amount2 = 0;
        this.updateDescription();
        this.type = NeutralPowertypePatch.NEUTRAL;
        this.loadRegion("mantra");
    }
    public void atStartOfTurn() {
        if (amount2 >= amount){
            addToBot(new ReducePowerAction(owner,owner,this,15));
            for (AbstractMonster M : AbstractDungeon.getCurrRoom().monsters.monsters){
                if (M instanceof ZastraszTheJusticar){
                    ((ZastraszTheJusticar) M).GainDivineFavor();
                }
            }
        }
        this.flash();
    }
    public int onAttacked(DamageInfo info, int damageAmount) {
        amount2 += damageAmount/2;
        return damageAmount;
    }
    @Override
    public void onRemove() {
        flash();
    }

    @Override
    public void updateDescription() {
        this.description = DESCRIPTIONS[0] + amount + DESCRIPTIONS[1] + amount + DESCRIPTIONS[2] + DESCRIPTIONS[3];
    }

    @Override
    public AbstractPower makeCopy() {
        return new BurnPower(owner,amount);
    }
}