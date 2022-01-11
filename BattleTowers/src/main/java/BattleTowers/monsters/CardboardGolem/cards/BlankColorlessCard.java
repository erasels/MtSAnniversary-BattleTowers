package BattleTowers.monsters.CardboardGolem.cards;

import basemod.abstracts.CustomCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

import static BattleTowers.BattleTowers.makeID;

public class BlankColorlessCard extends CustomCard {
    public static final String ID = makeID(BlankColorlessCard.class.getSimpleName());

    public BlankColorlessCard() {
        super(ID, "", "status/beta", 0, "", CardType.SKILL, CardColor.COLORLESS, CardRarity.SPECIAL, CardTarget.NONE);
    }

    @Override
    public void use(AbstractPlayer abstractPlayer, AbstractMonster abstractMonster) {

    }

    @Override
    public void upgrade() {
        if (!this.upgraded) {
            this.upgradeName();
        }
    }
}
