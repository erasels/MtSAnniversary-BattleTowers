package BattleTowers.cards;


import BattleTowers.BattleTowers;
import basemod.abstracts.CustomCard;
import com.megacrit.cardcrawl.actions.common.DrawCardAction;
import com.megacrit.cardcrawl.actions.unique.LoseEnergyAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

import static BattleTowers.BattleTowers.makeID;


public class Greedy extends CustomCard {
    public static final String ID = makeID(Greedy.class.getSimpleName());
    public static final String NAME;
    public static final String DESCRIPTION;
    public static final String IMG_PATH = BattleTowers.makeCardPath("Greedy.png");
    private static final CardType TYPE = CardType.CURSE;
    private static final CardRarity RARITY = CardRarity.CURSE;
    private static final CardTarget TARGET = CardTarget.NONE;
    private static final CardStrings cardStrings;
    private static final int COST = -2;
    public static String UPGRADED_DESCRIPTION;

    static {
        cardStrings = CardCrawlGame.languagePack.getCardStrings(ID);
        NAME = cardStrings.NAME;
        DESCRIPTION = cardStrings.DESCRIPTION;
        UPGRADED_DESCRIPTION = cardStrings.UPGRADE_DESCRIPTION;
    }

    public Greedy() {
        super(ID, NAME, IMG_PATH, COST, DESCRIPTION, TYPE, CardColor.CURSE, RARITY, TARGET);

        this.isEthereal = true;
        this.magicNumber = baseMagicNumber = 7;
    }

    public void use(AbstractPlayer p, AbstractMonster m) {
    }

    public void triggerWhenDrawn()
          {
              AbstractDungeon.player.loseGold(magicNumber);
              this.superFlash();
        }

    public AbstractCard makeCopy() {
        return new Greedy();
    }

    public void upgrade() {
    }
}


