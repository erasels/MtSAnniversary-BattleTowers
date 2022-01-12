package BattleTowers.cards;


import BattleTowers.BattleTowers;
import basemod.abstracts.CustomCard;
import com.evacipated.cardcrawl.mod.stslib.actions.common.SelectCardsCenteredAction;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.DexterityPower;
import com.megacrit.cardcrawl.powers.StrengthPower;

import java.util.ArrayList;

import static BattleTowers.BattleTowers.makeCardPath;
import static BattleTowers.BattleTowers.makeID;


public class KnightsManeuver extends CustomCard {
    public static final String ID = makeID(KnightsManeuver.class.getSimpleName());
    public static final String NAME;
    public static final String DESCRIPTION;
    public static final String IMG_PATH = BattleTowers.makeCardPath("KnightsManeuver.png");
    private static final CardType TYPE = CardType.POWER;
    private static final CardRarity RARITY = CardRarity.UNCOMMON;
    private static final CardTarget TARGET = CardTarget.SELF;
    private static final CardStrings cardStrings;
    private static final int COST = 2;
    public static String UPGRADED_DESCRIPTION;
    public static String[] EXTENDED_DESCRIPTION;

    static {
        cardStrings = CardCrawlGame.languagePack.getCardStrings(ID);
        NAME = cardStrings.NAME;
        DESCRIPTION = cardStrings.DESCRIPTION;
        UPGRADED_DESCRIPTION = cardStrings.UPGRADE_DESCRIPTION;
        EXTENDED_DESCRIPTION = cardStrings.EXTENDED_DESCRIPTION;
    }

    public KnightsManeuver() {
        super(ID, NAME, IMG_PATH, COST, DESCRIPTION, TYPE, CardColor.COLORLESS, RARITY, TARGET);
        shuffleBackIntoDrawPile = true;
        baseBlock = 7;
    }

    public void use(AbstractPlayer p, AbstractMonster m) {
        ArrayList<AbstractCard> choices = new ArrayList<>();
        choices.add(new EasyModalChoiceCard(EXTENDED_DESCRIPTION[0], EXTENDED_DESCRIPTION[1], makeCardPath("KnightsManeuver.png"), () -> {
            addToTop(new ApplyPowerAction(p, p, new DexterityPower(p, 3), 3));
            addToTop(new ApplyPowerAction(p, p, new StrengthPower(p, -1), -1));
        }));
        choices.add(new EasyModalChoiceCard(EXTENDED_DESCRIPTION[2], EXTENDED_DESCRIPTION[3], makeCardPath("KnightsManeuver.png"), () -> {
            addToTop(new ApplyPowerAction(p, p, new StrengthPower(p, 3), 3));
            addToTop(new ApplyPowerAction(p, p, new DexterityPower(p, -1), -1));
        }));
        addToBot(new SelectCardsCenteredAction(choices, 1, EXTENDED_DESCRIPTION[4], (cards) -> {
            cards.get(0).onChoseThisOption();
        }));
    }

    public AbstractCard makeCopy() {
        return new KnightsManeuver();
    }

    public void upgrade() {
        if (!upgraded) {
            upgradeName();
            upgradeBaseCost(1);
        }
    }
}


