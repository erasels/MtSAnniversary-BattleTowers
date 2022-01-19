package BattleTowers.powers;

import BattleTowers.monsters.ZastraszTheJusticar;
import BattleTowers.util.UC;
import basemod.interfaces.CloneablePowerInterface;
import com.evacipated.cardcrawl.mod.stslib.patches.NeutralPowertypePatch;
import com.evacipated.cardcrawl.mod.stslib.powers.abstracts.TwoAmountPower;
import com.evacipated.cardcrawl.mod.stslib.powers.interfaces.InvisiblePower;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.PowerStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.AbstractPower;

import static BattleTowers.BattleTowers.makeID;

public class JudgementPower extends TwoAmountPower implements CloneablePowerInterface, InvisiblePower {
    public static final String POWER_ID = makeID(JudgementPower.class.getSimpleName());
    private static final PowerStrings powerStrings = CardCrawlGame.languagePack.getPowerStrings(POWER_ID);
    public static final String NAME = powerStrings.NAME;
    public static final String[] DESCRIPTIONS = powerStrings.DESCRIPTIONS;

    public JudgementPower(AbstractCreature owner) {
        this.name = NAME;
        this.ID = POWER_ID;
        this.owner = owner;
        this.amount = 0;
        this.amount2 = 30;
        this.updateDescription();
        this.type = NeutralPowertypePatch.NEUTRAL;
        this.loadRegion("mantra");
    }
    public void atStartOfTurn() {
        if (amount >= amount2){
            amount -= amount2;
            for (AbstractMonster M : AbstractDungeon.getCurrRoom().monsters.monsters){
                if (M instanceof ZastraszTheJusticar){
                    ((ZastraszTheJusticar) M).GainDivineFavor();
                }
            }
            AbstractPower p = getParentPower();
            p.amount = amount;
            p.updateDescription();
            p.flash();
        }
    }
    public int onAttacked(DamageInfo info, int damageAmount) {
        amount += damageAmount;
        AbstractPower p = getParentPower();
        p.amount = amount;
        p.updateDescription();
        return damageAmount;
    }
    @Override
    public void onRemove() {
        flash();
    }

    @Override
    public void updateDescription() {
        this.description = DESCRIPTIONS[0] + amount2 + DESCRIPTIONS[1] + amount2 + DESCRIPTIONS[2] + DESCRIPTIONS[3];
    }

    private InquisitorPower getParentPower() {
        for(AbstractMonster m : UC.getAliveMonsters()) {
            if(m instanceof ZastraszTheJusticar) {
                for(AbstractPower p : m.powers) {
                    if(p instanceof InquisitorPower) {
                        return (InquisitorPower) p;
                    }
                }
            }
        }

        return null;
    }

    @Override
    public AbstractPower makeCopy() {
        return new JudgementPower(owner);
    }
}