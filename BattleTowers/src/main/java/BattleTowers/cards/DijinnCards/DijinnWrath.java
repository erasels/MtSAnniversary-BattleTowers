package BattleTowers.cards.DijinnCards;

import BattleTowers.BattleTowers;
import BattleTowers.monsters.Dijinn;
import basemod.abstracts.CustomCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

import static BattleTowers.BattleTowers.makeID;

public class DijinnWrath extends CustomCard {
    public static final String ID = makeID(DijinnWrath.class.getSimpleName());
    public static final String IMG = BattleTowers.makeCardPath(DijinnWrath.class.getSimpleName() + ".png");
    private static final CardStrings cardStrings = CardCrawlGame.languagePack.getCardStrings(ID);
    public static final String NAME = cardStrings.NAME;
    public static final String DESCRIPTION = cardStrings.DESCRIPTION;
    private static final int COST = -2;
    private Dijinn dijinn;

    public DijinnWrath(Dijinn dijinn, int magicNumber) {
        super(ID, NAME, IMG, COST, DESCRIPTION, CardType.SKILL, CardColor.COLORLESS, CardRarity.SPECIAL, CardTarget.NONE);
        this.dijinn = dijinn;
        this.magicNumber = baseMagicNumber = magicNumber;
        this.block = dijinn.BLOCK;
        this.rawDescription = this.rawDescription + cardStrings.EXTENDED_DESCRIPTION[dijinn.nextMove];
        this.initializeDescription();
    }

    @Override
    public void use(AbstractPlayer p, AbstractMonster m) {
    }

    @Override
    public void upgrade() {
    }

    public void onChoseThisOption(){
        dijinn.dijinnWrath();
    }


}
