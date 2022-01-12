package BattleTowers.cards;


import BattleTowers.BattleTowers;
import basemod.abstracts.CustomCard;
import com.megacrit.cardcrawl.actions.common.DrawCardAction;
import com.megacrit.cardcrawl.actions.common.GainBlockAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

import static BattleTowers.BattleTowers.makeID;


public class BishopsPrayer extends CustomCard {
    public static final String ID = makeID(BishopsPrayer.class.getSimpleName());
    public static final String NAME;
    public static final String DESCRIPTION;
    public static final String IMG_PATH = BattleTowers.makeCardPath("BishopsPrayer.png");
    private static final CardType TYPE = CardType.SKILL;
    private static final CardRarity RARITY = CardRarity.UNCOMMON;
    private static final CardTarget TARGET = CardTarget.SELF;
    private static final CardStrings cardStrings;
    private static final int COST = 1;
    public static String UPGRADED_DESCRIPTION;

    static {
        cardStrings = CardCrawlGame.languagePack.getCardStrings(ID);
        NAME = cardStrings.NAME;
        DESCRIPTION = cardStrings.DESCRIPTION;
        UPGRADED_DESCRIPTION = cardStrings.UPGRADE_DESCRIPTION;
    }

    public BishopsPrayer() {
        super(ID, NAME, IMG_PATH, COST, DESCRIPTION, TYPE, CardColor.COLORLESS, RARITY, TARGET);
        shuffleBackIntoDrawPile = true;
        baseBlock = 7;
    }

    public void use(AbstractPlayer p, AbstractMonster m) {
        addToBot(new GainBlockAction(p, block));
        addToBot(new DrawCardAction(p, 1));
    }

    public AbstractCard makeCopy() {
        return new BishopsPrayer();
    }

    public void upgrade() {
        if (!upgraded) {
            upgradeName();
            upgradeBlock(3);
        }
    }
}


