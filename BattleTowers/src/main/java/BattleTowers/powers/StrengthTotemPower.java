package BattleTowers.powers;

import BattleTowers.BattleTowers;
import BattleTowers.monsters.Necrototem;
import BattleTowers.util.UC;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.localization.PowerStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.powers.StrengthPower;

import java.util.Optional;

import static BattleTowers.BattleTowers.makeID;

public class StrengthTotemPower extends AbstractPower {
    public static final String POWER_ID = makeID(StrengthTotemPower.class.getSimpleName());
    private static final PowerStrings powerStrings = CardCrawlGame.languagePack.getPowerStrings(POWER_ID);
    public static final String NAME = powerStrings.NAME;
    public static final String[] DESCRIPTIONS = powerStrings.DESCRIPTIONS;

    public StrengthTotemPower(AbstractCreature owner, int amount) {
        this.name = NAME;
        this.ID = POWER_ID;
        this.owner = owner;
        this.amount = amount;
        this.updateDescription();
        this.type = PowerType.BUFF;
        BattleTowers.LoadPowerImage(this);
    }

    @Override
    public void updateDescription() {
        this.description = String.format(DESCRIPTIONS[0], FontHelper.colorString(owner.name, "y"), amount);
    }

    private Optional<AbstractMonster> getNecrototem()
    {
        return UC.getAliveMonsters().stream()
                .filter(m -> Necrototem.ID.equals(m.id))
                .findFirst();
    }

    @Override
    public void onInitialApplication()
    {
        getNecrototem().ifPresent(abstractMonster -> UC.doPow(new StrengthPower(abstractMonster, amount)));
    }

    @Override
    public void stackPower(int stackAmount)
    {
        super.stackPower(stackAmount);
        getNecrototem().ifPresent(abstractMonster -> UC.doPow(new StrengthPower(abstractMonster, stackAmount)));
    }

    @Override
    public void reducePower(int reduceAmount)
    {
        super.reducePower(reduceAmount);
        getNecrototem().ifPresent(abstractMonster -> UC.doPow(new StrengthPower(abstractMonster, -reduceAmount)));
    }

    @Override
    public void onDeath()
    {
        getNecrototem().ifPresent(abstractMonster -> UC.doPow(new StrengthPower(abstractMonster, -amount)));
    }
}
