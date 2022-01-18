package BattleTowers.cards;

import BattleTowers.powers.PawnBuffPower;
import basemod.abstracts.CustomCard;
import basemod.helpers.ModalChoice;
import basemod.helpers.ModalChoiceBuilder;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.actions.common.GainBlockAction;
import com.megacrit.cardcrawl.actions.common.MakeTempCardInDrawPileAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

import java.util.Iterator;

import static BattleTowers.BattleTowers.makeID;
import static BattleTowers.BattleTowers.makeCardPath;
public class WindStrike extends CustomCard implements ModalChoice.Callback {
    public static final String ID = makeID(WindStrike.class.getSimpleName());
    public static final String NAME;
    public static final String DESCRIPTION;
    public static final String IMG_PATH = makeCardPath("PawnsAdvance.png");
    private static final CardType TYPE = CardType.ATTACK;
    private static final CardRarity RARITY = CardRarity.BASIC;
    private static final CardTarget TARGET = CardTarget.SELF;
    private static final CardStrings cardStrings;
    private static final int COST = 1;
    public static String UPGRADED_DESCRIPTION;
    private ModalChoice modal;
    static {
        cardStrings = CardCrawlGame.languagePack.getCardStrings(ID);
        NAME = cardStrings.NAME;
        DESCRIPTION = cardStrings.DESCRIPTION;
        UPGRADED_DESCRIPTION = cardStrings.UPGRADE_DESCRIPTION;
    }

    public WindStrike() {
        super(ID, NAME, IMG_PATH, COST, DESCRIPTION, TYPE, CardColor.COLORLESS, RARITY, TARGET);
        baseDamage = damage = 4;
        baseBlock = block = 4;
        magicNumber = 3;
        modal = new ModalChoiceBuilder()
                .setCallback(this)
                .setColor(CardColor.COLORLESS)
                .setTitle("Razor Wind")
                .addOption("Deal " + (damage) + " damage increased by 3 for each Wind Strike",CardTarget.ENEMY)
                .setColor(CardColor.COLORLESS)
                .setTitle("Tranquility")
                .addOption("Gain " + (block) + " Block increased by 3 for each Wind Strike",CardTarget.SELF)
                .setColor(CardColor.COLORLESS)
                .setTitle("Storm Front")
                .addOption("Shuffle a copy of this into your draw pile",CardTarget.SELF)
                .create();
    }

    public void use(AbstractPlayer p, AbstractMonster m) {
        modal.open();
    }

    public AbstractCard makeCopy() {
        return new WindStrike();
    }

    public void upgrade() {
        if (!upgraded) {
            upgradeName();
            upgradeMagicNumber(1);
            upgradeDamage(1);
            upgradeBlock(1);
        }
    }
    @Override
    public void optionSelected(AbstractPlayer abstractPlayer, AbstractMonster abstractMonster, int i) {
        switch (i) {
            case 0:
                addToBot(new DamageAction(abstractMonster,new DamageInfo(abstractPlayer,damage+(magicNumber*countCards()), DamageInfo.DamageType.NORMAL)));
                break;
            case 1:
                addToBot(new GainBlockAction(abstractPlayer,block+(magicNumber*countCards())));
                break;
            case 2:
                addToBot(new MakeTempCardInDrawPileAction(this.makeStatEquivalentCopy(),1,true,false));
                break;
                default:
                    return;
        }
    }
    public void applyPowers() {
        int realBaseDamage = this.baseDamage;
        this.baseDamage += this.magicNumber * countCards();
        super.applyPowers();
        this.baseDamage = realBaseDamage;
        this.isDamageModified = this.damage != this.baseDamage;

        int realBaseBlock = baseBlock;
        this.baseBlock += magicNumber * countCards();
        super.applyPowers();
        baseBlock = realBaseBlock;
        isBlockModified = block != baseBlock;
        this.initializeDescription();
    }
    public void calculateCardDamage(AbstractMonster mo) {
        int realBaseDamage = this.baseDamage;
        this.baseDamage += this.magicNumber * countCards();
        super.calculateCardDamage(mo);
        this.baseDamage = realBaseDamage;
        this.isDamageModified = this.damage != this.baseDamage;
    }
    public int countCards() {
        int count = 0;
        Iterator var1 = AbstractDungeon.player.hand.group.iterator();

        AbstractCard c;
        while(var1.hasNext()) {
            c = (AbstractCard)var1.next();
            if (c instanceof WindStrike && c != this) {
                ++count;
            }
        }

        var1 = AbstractDungeon.player.drawPile.group.iterator();

        while(var1.hasNext()) {
            c = (AbstractCard)var1.next();
            if (c instanceof WindStrike && c != this) {
                ++count;
            }
        }

        var1 = AbstractDungeon.player.discardPile.group.iterator();

        while(var1.hasNext()) {
            c = (AbstractCard)var1.next();
            if (c instanceof WindStrike && c != this) {
                ++count;
            }
        }

        return count;
    }
}