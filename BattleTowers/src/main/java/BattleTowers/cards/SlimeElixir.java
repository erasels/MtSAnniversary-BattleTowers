package BattleTowers.cards;

import BattleTowers.BattleTowers;
import basemod.abstracts.CustomCard;
import com.megacrit.cardcrawl.actions.common.DrawCardAction;
import com.megacrit.cardcrawl.actions.common.MakeTempCardInDrawPileAction;
import com.megacrit.cardcrawl.cards.status.Slimed;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

import static BattleTowers.BattleTowers.makeID;

public class SlimeElixir extends CustomCard {
    public static final String ID = makeID(SlimeElixir.class.getSimpleName());
    public static final String IMG = BattleTowers.makeCardPath(SlimeElixir.class.getSimpleName() + ".png");
    private static final CardStrings cardStrings = CardCrawlGame.languagePack.getCardStrings(ID);
    public static final String NAME = cardStrings.NAME;
    public static final String DESCRIPTION = cardStrings.DESCRIPTION;
    private static final int COST = 0;
    private static final int CARDS = 2;
    private static final int UPGRADE_CARDS = 2;

    public SlimeElixir() {
        super(ID, NAME, IMG, COST, DESCRIPTION, CardType.SKILL, CardColor.COLORLESS, CardRarity.SPECIAL, CardTarget.NONE);
        this.magicNumber = this.baseMagicNumber = CARDS;
        this.selfRetain = true;
        this.exhaust = true;
    }

    @Override
    public void use(AbstractPlayer p, AbstractMonster m) {
        this.addToBot(new DrawCardAction(this.magicNumber));
        this.addToBot(new MakeTempCardInDrawPileAction(new Slimed(), 1, true, true));
    }

    @Override
    public void upgrade() {
        if (!upgraded) {
            this.upgradeMagicNumber(UPGRADE_CARDS);
            this.upgradeName();
        }
    }
}
