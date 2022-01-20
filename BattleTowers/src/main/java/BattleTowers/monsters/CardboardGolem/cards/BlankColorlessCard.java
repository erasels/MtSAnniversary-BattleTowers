package BattleTowers.monsters.CardboardGolem.cards;

import basemod.abstracts.CustomCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

import static BattleTowers.BattleTowers.makeCardPath;
import static BattleTowers.BattleTowers.makeID;

public class BlankColorlessCard extends CustomCard {
    public static final String ID = makeID(BlankColorlessCard.class.getSimpleName());
    public static CardStrings cardStrings = CardCrawlGame.languagePack.getCardStrings(makeID("BlankCard"));

    public BlankColorlessCard() {
        super(ID, cardStrings.NAME, makeCardPath("nothing.png"), 0, cardStrings.DESCRIPTION, CardType.STATUS, CardColor.COLORLESS, CardRarity.SPECIAL, CardTarget.NONE);
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
