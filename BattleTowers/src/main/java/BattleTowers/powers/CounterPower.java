package BattleTowers.powers;

import BattleTowers.monsters.Romeo;
import BattleTowers.powers.abstracts.AbstractBTPower;
import BattleTowers.util.UC;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.ChangeStateAction;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.localization.PowerStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

import static BattleTowers.BattleTowers.makeID;

public class CounterPower extends AbstractBTPower {
    public static final String POWER_ID = makeID("Counter");
    private static final PowerStrings powerStrings = CardCrawlGame.languagePack.getPowerStrings(POWER_ID);
    public static final String NAME = powerStrings.NAME;
    public static final String[] DESCRIPTIONS = powerStrings.DESCRIPTIONS;

    public CounterPower(AbstractCreature owner, int threshold, int counterDamage) {
        this.name = NAME;
        this.ID = POWER_ID;
        this.owner = owner;
        this.amount = threshold;
        this.amount2 = counterDamage;
        type = PowerType.BUFF;
        updateDescription();
        loadRegion("painfulStabs");
    }

    public void updateDescription() {
        this.description = String.format(DESCRIPTIONS[0], amount, amount2);
    }

    @Override
    public int onAttacked(DamageInfo info, int damageAmount) {
        if (damageAmount < amount && info.type != DamageInfo.DamageType.THORNS && info.type != DamageInfo.DamageType.HP_LOSS) {
            flashWithoutSound();
            if(owner instanceof Romeo)
                UC.atb(new ChangeStateAction((AbstractMonster) owner, "SLASH"));
            UC.atb(new DamageAction(info.owner, new DamageInfo(owner, amount2, DamageInfo.DamageType.NORMAL), AbstractGameAction.AttackEffect.SLASH_VERTICAL));
        }
        return super.onAttacked(info, damageAmount);
    }
}
