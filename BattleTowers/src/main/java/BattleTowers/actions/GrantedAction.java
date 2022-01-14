package BattleTowers.actions;

import basemod.ReflectionHacks;
import com.evacipated.cardcrawl.mod.stslib.patches.core.AbstractCreature.TempHPField;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.cards.blue.AllForOne;
import com.megacrit.cardcrawl.cards.blue.ThunderStrike;
import com.megacrit.cardcrawl.cards.green.BulletTime;
import com.megacrit.cardcrawl.cards.green.Burst;
import com.megacrit.cardcrawl.cards.green.CorpseExplosion;
import com.megacrit.cardcrawl.cards.green.DieDieDie;
import com.megacrit.cardcrawl.cards.green.Doppelganger;
import com.megacrit.cardcrawl.cards.green.GrandFinale;
import com.megacrit.cardcrawl.cards.green.PhantasmalKiller;
import com.megacrit.cardcrawl.cards.green.StormOfSteel;
import com.megacrit.cardcrawl.cards.purple.LessonLearned;
import com.megacrit.cardcrawl.cards.red.Barricade;
import com.megacrit.cardcrawl.cards.red.DemonForm;
import com.megacrit.cardcrawl.cards.red.DoubleTap;
import com.megacrit.cardcrawl.cards.red.Exhume;
import com.megacrit.cardcrawl.cards.red.Feed;
import com.megacrit.cardcrawl.cards.red.FiendFire;
import com.megacrit.cardcrawl.cards.red.Immolate;
import com.megacrit.cardcrawl.cards.red.LimitBreak;
import com.megacrit.cardcrawl.cards.red.Reaper;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.CardLibrary;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.orbs.AbstractOrb;
import com.megacrit.cardcrawl.orbs.Lightning;
import com.megacrit.cardcrawl.powers.StrengthPower;
import com.megacrit.cardcrawl.relics.ChemicalX;
import com.megacrit.cardcrawl.ui.panels.EnergyPanel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class GrantedAction extends AbstractGameAction {

    private HashMap<String, Integer> cardPointsMap = new HashMap<String, Integer>();
    private int totalDamage;
    private int numHits;

    public GrantedAction() {
        this.duration = Settings.ACTION_DUR_FAST;
        this.actionType = ActionType.CARD_MANIPULATION;
        getInfo();
    }

    public void update() {
        ArrayList<AbstractCard> allCards = CardLibrary.getCardList(CardLibrary.LibraryType.RED);
        allCards.addAll(CardLibrary.getCardList(CardLibrary.LibraryType.GREEN));
        allCards.addAll(CardLibrary.getCardList(CardLibrary.LibraryType.BLUE));
        allCards.addAll(CardLibrary.getCardList(CardLibrary.LibraryType.PURPLE));

        ArrayList<AbstractCard> rareCardsAttack = new ArrayList<>();
        ArrayList<AbstractCard> rareCardsSkill = new ArrayList<>();
        ArrayList<AbstractCard> rareCardsPower = new ArrayList<>();
        for (AbstractCard card : allCards) {
            AbstractCard cardCopy = card.makeCopy();
            cardCopy.upgrade();
            boolean addToList = false;
            boolean addMultiple = false; //add multiple copies of a card so it has a greater chance to be selected
            boolean addSuperMultiple = false; //for when just multiple isn't enough
            if (card.rarity == AbstractCard.CardRarity.RARE && card.costForTurn <= EnergyPanel.getCurrentEnergy()) {
                addToList = true;
            }
            if (card.cardID.equals(Feed.ID) || card.cardID.equals(LessonLearned.ID)) {
                if (isLethal(cardCopy)) {
                    addSuperMultiple = true;
                } else {
                    addToList = false;
                }
            }
            if (card.cardID.equals(Immolate.ID) || card.cardID.equals(DieDieDie.ID) || card.cardID.equals(CorpseExplosion.ID)) {
                int numAlive = 0;
                for (AbstractMonster mo :AbstractDungeon.getCurrRoom().monsters.monsters) {
                    if (!mo.isDeadOrEscaped()) {
                        numAlive++;
                    }
                }
                if (numAlive >= 2) {
                    addMultiple = true;
                } else {
                    addToList = false;
                }
            }
            if (card.cardID.equals(FiendFire.ID) || card.cardID.equals(BulletTime.ID) || card.cardID.equals(StormOfSteel.ID)) {
                if (AbstractDungeon.player.hand.size() >= 7) {
                    addMultiple = true;
                } else {
                    addToList = false;
                }
            }
            if (card.cardID.equals(Reaper.ID)) {
                if (AbstractDungeon.player.currentHealth <= AbstractDungeon.player.maxHealth * 0.75f) {
                    int numAlive = 0;
                    for (AbstractMonster mo :AbstractDungeon.getCurrRoom().monsters.monsters) {
                        if (!mo.isDeadOrEscaped()) {
                            numAlive++;
                        }
                    }
                    if (numAlive >= 2 || (AbstractDungeon.player.hasPower(StrengthPower.POWER_ID) && AbstractDungeon.player.getPower(StrengthPower.POWER_ID).amount >= 4)) {
                        addMultiple = true;
                    } else {
                        addToList = false;
                    }
                }
            }
            if (card.cardID.equals(DoubleTap.ID)) {
                int numAttacks = 0;
                for (AbstractCard handCard : AbstractDungeon.player.hand.group) {
                    if (handCard.type == AbstractCard.CardType.ATTACK) {
                        numAttacks++;
                    }
                }
                if (numAttacks >= 2 && EnergyPanel.getCurrentEnergy() >= 2) {
                    addMultiple = true;
                } else {
                    addToList = false;
                }
            }
            if (card.cardID.equals(Exhume.ID)) {
                int numGoodCards = 0;
                for (AbstractCard exhaustCard : AbstractDungeon.player.exhaustPile.group) {
                    if (exhaustCard.type != AbstractCard.CardType.CURSE && exhaustCard.type != AbstractCard.CardType.STATUS && exhaustCard.rarity != AbstractCard.CardRarity.BASIC) {
                        if (exhaustCard.costForTurn >= 0 && exhaustCard.costForTurn <= EnergyPanel.getCurrentEnergy()) {
                            numGoodCards++;
                        }
                    }
                }
                if (numGoodCards >= 3) {
                    addMultiple = true;
                } else {
                    addToList = false;
                }
            }
            if (card.cardID.equals(LimitBreak.ID)) {
                if ((AbstractDungeon.player.hasPower(StrengthPower.POWER_ID) && AbstractDungeon.player.getPower(StrengthPower.POWER_ID).amount >= 4)) {
                    addMultiple = true;
                } else {
                    addToList = false;
                }
            }
            if (card.cardID.equals(Barricade.ID)) {
                if (AbstractDungeon.player.currentBlock - totalDamage >= 20) {
                    addMultiple = true;
                } else {
                    addToList = false;
                }
            }
            if (card.cardID.equals(DemonForm.ID) || card.cardID.equals(PhantasmalKiller.ID)) {
                if (!(checkAttackDamageRatio() >= 0.3f)) {
                    addToList = false;
                }
            }
            if (card.cardID.equals(GrandFinale.ID)) {
                if (AbstractDungeon.player.drawPile.size() == 0) {
                    addSuperMultiple = true;
                } else {
                    addToList = false;
                }
            }
            if (card.cardID.equals(Burst.ID)) {
                int numSkills = 0;
                for (AbstractCard handCard : AbstractDungeon.player.hand.group) {
                    if (handCard.type == AbstractCard.CardType.SKILL) {
                        numSkills++;
                    }
                }
                if (numSkills >= 2 && EnergyPanel.getCurrentEnergy() >= 2) {
                    addMultiple = true;
                } else {
                    addToList = false;
                }
            }
            if (card.cardID.equals(Doppelganger.ID)) {
                if (AbstractDungeon.player.hasRelic(ChemicalX.ID)) {
                    addSuperMultiple = true;
                }
            }
            if (card.cardID.equals(AllForOne.ID)) {
                if ((check0CostCardRatio() >= 0.5f)) {
                    addSuperMultiple = true;
                } else if ((check0CostCardRatio() >= 0.25f)) {
                    addMultiple = true;
                } else {
                    addToList = false;
                }
            }
            if (card.cardID.equals(ThunderStrike.ID)) {
                int numLightning = 0;
                for (AbstractOrb o : AbstractDungeon.actionManager.orbsChanneledThisCombat) {
                    if (o instanceof Lightning) {
                        numLightning++;
                    }
                }
                if (numLightning >= 8) {
                    addSuperMultiple = true;
                } else if (numLightning >= 4) {
                    addMultiple = true;
                } else {
                    addToList = false;
                }
            }

        }
        this.tickDuration();
    }

    private float checkAttackDamageRatio() {
        int numAttackCards = 0;
        for (AbstractCard card : AbstractDungeon.player.masterDeck.group) {
            if (card.type == AbstractCard.CardType.ATTACK) {
                numAttackCards++;
            }
        }
        return (float)numAttackCards / AbstractDungeon.player.masterDeck.size();
    }

    private float check0CostCardRatio() {
        int num0CostCards = 0;
        for (AbstractCard card : AbstractDungeon.player.masterDeck.group) {
            if (card.costForTurn == 0) {
                num0CostCards++;
            }
        }
        return (float)num0CostCards / AbstractDungeon.player.masterDeck.size();
    }

    private void getInfo() {
        totalDamage = 0;
        numHits = 0;
        for (AbstractMonster mo : AbstractDungeon.getCurrRoom().monsters.monsters) {
            if (!mo.isDeadOrEscaped()) {
                if (mo.getIntentBaseDmg() >= 0) {
                    int moDamage = (Integer) ReflectionHacks.getPrivate(mo, AbstractMonster.class, "intentDmg");
                    if ((Boolean) ReflectionHacks.getPrivate(mo, AbstractMonster.class, "isMultiDmg")) {
                        int hitCount = (Integer) ReflectionHacks.getPrivate(mo, AbstractMonster.class, "intentMultiAmt");
                        moDamage *= hitCount;
                        numHits += hitCount;
                    } else {
                        numHits++;
                    }
                    totalDamage += moDamage;
                }
            }
        }
        System.out.println("Total incoming damage of " + totalDamage + " with " + numHits + " hits");
        int playerTotalMitigation = AbstractDungeon.player.currentBlock + TempHPField.tempHp.get(AbstractDungeon.player);
        System.out.println("Total player mitigation: " + playerTotalMitigation);
    }

    private boolean isLethal(AbstractCard card) {
        int baseDamage = card.baseDamage;
        DamageInfo info = new DamageInfo(AbstractDungeon.player, baseDamage);
        for (AbstractMonster mo : AbstractDungeon.getCurrRoom().monsters.monsters) {
            if (!mo.isDeadOrEscaped()) {
                info.applyPowers(AbstractDungeon.player, mo);
                if (info.output >= mo.currentHealth + mo.currentBlock) {
                    return true;
                }
            }
        }
        return false;
    }

}
