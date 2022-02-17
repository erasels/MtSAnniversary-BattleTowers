package BattleTowers.cards.WindStrikeModes;

import basemod.AutoAdd;
import basemod.abstracts.CustomCard;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.actions.common.GainBlockAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

import static BattleTowers.BattleTowers.makeCardPath;
import static BattleTowers.BattleTowers.makeID;
@AutoAdd.Ignore
public class RazorWind extends CustomCard {
    public static final String ID = makeID(RazorWind.class.getSimpleName());
    public static final String NAME;
    public static final String DESCRIPTION;
    public static final String IMG_PATH = makeCardPath("WindStrike.png");
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

    public RazorWind() {
        super(ID, NAME, IMG_PATH, COST, DESCRIPTION, TYPE, CardColor.COLORLESS, RARITY, TARGET);
        baseDamage = damage = 5;
        baseBlock = block = 5;
        magicNumber = baseMagicNumber = 3;
        purgeOnUse = true;
    }

    public RazorWind(int Damage, int Block, AbstractMonster m) {
        super(ID, NAME, IMG_PATH, COST, DESCRIPTION, TYPE, CardColor.COLORLESS, RARITY, TARGET);
        baseDamage = damage = Damage;
        baseBlock = block = Block;
        magicNumber = baseMagicNumber = 3;
        target = m;
    }
    public void onChoseThisOption() {
        addToBot(new DamageAction(target, new DamageInfo(AbstractDungeon.player, damage, DamageInfo.DamageType.NORMAL)));
        addToBot(new GainBlockAction(AbstractDungeon.player,block));
    }
    public void use(AbstractPlayer p, AbstractMonster m) {
        onChoseThisOption();
    }
    public void onChosethisCard(){}

    public AbstractCard makeCopy() {
        return new RazorWind(damage,block,target);
    }

    public void upgrade() {
    }
}