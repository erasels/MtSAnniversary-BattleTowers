package BattleTowers.cards;

import BattleTowers.cards.WindStrikeModes.RazorWind;
import BattleTowers.cards.WindStrikeModes.Stormfront;
import BattleTowers.util.UC;
import basemod.abstracts.CustomCard;
import com.megacrit.cardcrawl.actions.watcher.ChooseOneAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;
import java.util.stream.Stream;

import static BattleTowers.BattleTowers.makeCardPath;
import static BattleTowers.BattleTowers.makeID;
public class WindStrike extends CustomCard {
    public static final String ID = makeID(WindStrike.class.getSimpleName());
    public static final String NAME;
    public static final String DESCRIPTION;
    public static final String IMG_PATH = makeCardPath("WindStrike.png");
    private static final CardType TYPE = CardType.ATTACK;
    private static final CardRarity RARITY = CardRarity.BASIC;
    private static final CardTarget TARGET = CardTarget.SELF_AND_ENEMY;
    private static final CardStrings cardStrings;
    private static final int COST = 1;
    public static String UPGRADED_DESCRIPTION;
    static {
        cardStrings = CardCrawlGame.languagePack.getCardStrings(ID);
        NAME = cardStrings.NAME;
        DESCRIPTION = cardStrings.DESCRIPTION;
        UPGRADED_DESCRIPTION = cardStrings.UPGRADE_DESCRIPTION;
    }

    public WindStrike() {
        super(ID, NAME, IMG_PATH, COST, DESCRIPTION, TYPE, CardColor.COLORLESS, RARITY, TARGET);
        baseDamage = damage = 5;
        baseBlock = block = 5;
        magicNumber = baseMagicNumber = 3;
    }

    public void use(AbstractPlayer p, AbstractMonster m) {
        ArrayList<AbstractCard> choices = new ArrayList<>();
        choices.add(new RazorWind(damage,block,m));
        choices.add(new Stormfront());
        addToBot(new ChooseOneAction(choices));
    }

    public AbstractCard makeCopy() {
        return new WindStrike();
    }

    public void upgrade() {
        if (!upgraded) {
            upgradeName();
            upgradeDamage(3);
            upgradeBlock(3);
            upgradeMagicNumber(3);
        }
    }
    public void applyPowers() {
        int realBaseDamage = this.baseDamage;
        int realBaseBlock = baseBlock;
        int inc = magicNumber * countCards();
        this.baseDamage += inc;
        this.baseBlock += inc;
        super.applyPowers();
        this.baseDamage = realBaseDamage;
        baseBlock = realBaseBlock;
        this.isDamageModified = this.damage != this.baseDamage;
        isBlockModified = block != baseBlock;

        this.initializeDescription();
    }
    public void calculateCardDamage(AbstractMonster mo) {
        int realBaseDamage = this.baseDamage;
        int realBaseBlock = baseBlock;
        int inc = magicNumber * countCards();
        this.baseDamage += inc;
        this.baseBlock += inc;
        super.calculateCardDamage(mo);
        this.baseDamage = realBaseDamage;
        baseBlock = realBaseBlock;
        this.isDamageModified = this.damage != this.baseDamage;
        isBlockModified = block != baseBlock;
    }
    public int countCards() {
        UUID uid = uuid;
        int count = (int) Stream.of(UC.hand().group, UC.p().discardPile.group, UC.p().drawPile.group)
                .flatMap(Collection::stream)
                .filter(c -> c instanceof WindStrike && !uid.equals(c.uuid))
                .count();

        return count;
    }
}