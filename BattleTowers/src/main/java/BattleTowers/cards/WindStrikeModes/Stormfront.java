package BattleTowers.cards.WindStrikeModes;

import BattleTowers.cards.WindStrike;
import basemod.AutoAdd;
import basemod.abstracts.CustomCard;
import com.megacrit.cardcrawl.actions.common.MakeTempCardInDrawPileAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

import static BattleTowers.BattleTowers.makeCardPath;
import static BattleTowers.BattleTowers.makeID;
@AutoAdd.Ignore
public class Stormfront extends CustomCard {
    public static final String ID = makeID(Stormfront.class.getSimpleName());
    public static final String NAME;
    public static final String DESCRIPTION;
    public static final String IMG_PATH = makeCardPath("PawnsAdvance.png");
    private static final CardType TYPE = CardType.ATTACK;
    private static final CardRarity RARITY = CardRarity.BASIC;
    private static final CardTarget TARGET = CardTarget.ENEMY;
    private static final CardStrings cardStrings;
    private static final int COST = 1;
    public static String UPGRADED_DESCRIPTION;
    public AbstractMonster target;

    static {
        cardStrings = CardCrawlGame.languagePack.getCardStrings(ID);
        NAME = cardStrings.NAME;
        DESCRIPTION = cardStrings.DESCRIPTION;
        UPGRADED_DESCRIPTION = cardStrings.UPGRADE_DESCRIPTION;
    }

    public Stormfront() {
        super(ID, NAME, IMG_PATH, COST, DESCRIPTION, TYPE, CardColor.COLORLESS, RARITY, TARGET);
        baseDamage = damage = 4;
        baseBlock = block = 4;
        magicNumber = baseMagicNumber = 3;
        purgeOnUse = true;
    }

    public void onChoseThisOption() {
        addToBot(new MakeTempCardInDrawPileAction(new WindStrike(), 1, true, false));
    }

    public void use(AbstractPlayer p, AbstractMonster m) {
        onChoseThisOption();
    }

    public AbstractCard makeCopy() {
        return new Stormfront();
    }

    public void upgrade() {
    }
}
