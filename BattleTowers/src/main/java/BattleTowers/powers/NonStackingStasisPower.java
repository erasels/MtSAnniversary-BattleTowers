package BattleTowers.powers;

import com.evacipated.cardcrawl.mod.stslib.powers.interfaces.NonStackablePower;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.powers.StasisPower;

public class NonStackingStasisPower extends StasisPower implements NonStackablePower {
    public NonStackingStasisPower(AbstractCreature owner, AbstractCard card) {
        super(owner, card);
    }

}
