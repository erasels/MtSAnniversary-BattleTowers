//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package BattleTowers.events;

import BattleTowers.BattleTowers;
import BattleTowers.cardmods.*;
import BattleTowers.cards.CursedTapestry;
import BattleTowers.events.phases.CombatPhase;
import BattleTowers.events.phases.TextPhase;
import BattleTowers.monsters.BurningShambler;
import BattleTowers.monsters.CultistArmorer;
import BattleTowers.monsters.Encounters;
import BattleTowers.monsters.MinotaurGladiator;
import BattleTowers.relics.*;
import basemod.abstracts.AbstractCardModifier;
import basemod.cardmods.EtherealMod;
import basemod.cardmods.ExhaustMod;
import basemod.cardmods.InnateMod;
import basemod.cardmods.RetainMod;
import basemod.helpers.CardModifierManager;
import com.badlogic.gdx.math.MathUtils;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.CardGroup;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.cards.blue.Defend_Blue;
import com.megacrit.cardcrawl.cards.blue.Strike_Blue;
import com.megacrit.cardcrawl.cards.curses.Pain;
import com.megacrit.cardcrawl.cards.green.Defend_Green;
import com.megacrit.cardcrawl.cards.green.Strike_Green;
import com.megacrit.cardcrawl.cards.red.Defend_Red;
import com.megacrit.cardcrawl.cards.red.Strike_Red;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.AbstractImageEvent;
import com.megacrit.cardcrawl.helpers.MonsterHelper;
import com.megacrit.cardcrawl.localization.EventStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.MonsterGroup;
import com.megacrit.cardcrawl.monsters.beyond.Exploder;
import com.megacrit.cardcrawl.monsters.beyond.Repulsor;
import com.megacrit.cardcrawl.monsters.beyond.Spiker;
import com.megacrit.cardcrawl.monsters.exordium.Cultist;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.relics.CultistMask;
import com.megacrit.cardcrawl.rewards.RewardItem;
import com.megacrit.cardcrawl.vfx.cardManip.PurgeCardEffect;
import com.megacrit.cardcrawl.vfx.cardManip.ShowCardAndObtainEffect;
import com.megacrit.cardcrawl.vfx.cardManip.ShowCardBrieflyEffect;
import com.sun.org.apache.xpath.internal.operations.Bool;

import javax.smartcardio.Card;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Iterator;

import static BattleTowers.BattleTowers.makeID;

public class ArmorerEvent extends PhasedEvent {
    public static final String ID = makeID("ArmorerEvent");
    public static final String NAME;
    public static final String[] DESCRIPTIONS;
    public static final String[] OPTIONS;
    private boolean[] alreadyClaimed = new boolean[12];
    private static final EventStrings eventStrings;

    static {
        eventStrings = CardCrawlGame.languagePack.getEventString(ID);
        NAME = eventStrings.NAME;
        DESCRIPTIONS = eventStrings.DESCRIPTIONS;
        OPTIONS = eventStrings.OPTIONS;
    }

    private int screenNum = 0;

    private int numberOfTimesUsed;

    private actionType currentAction;

    private enum actionType {
        APPLYVULNERABLE,
        APPLYWEAK,
        DRAW,
        GAINFRAIL,
        GAINVULNERABLE,
        GAINWEAK,
        EXHAUST,
        ETHEREAL,
        LOWERVALUES,
        INNATE,
        RETAIN,
        RAISEVALUES
    }


    public ArmorerEvent() {
        super(NAME, BattleTowers.makeImagePath("events/armorer.png"));

        registerPhase(0, new TextPhase(DESCRIPTIONS[14]).addOption(OPTIONS[0], null).addOption(OPTIONS[7], null, new ArmorersMask()));

        registerPhase("CombatEnd", new TextPhase(DESCRIPTIONS[22]).addOption(OPTIONS[6], (t)->this.openMap()));
        registerPhase("fight", new CombatPhase(Encounters.CULTIST_ARMORER, true).setNextKey("CombatEnd"));

        transitionKey(0);

        this.imageEventText.setDialogOption(OPTIONS[0]);
        this.imageEventText.setDialogOption(OPTIONS[7], new ArmorersMask());

    }

    public ArrayList<AbstractCard> getCurrentActionValidCards(actionType action) {

        ArrayList<AbstractCard> allCards = AbstractDungeon.player.masterDeck.group;
        ArrayList<AbstractCard> validCards = new ArrayList<>();

        for (AbstractCard c : allCards
        ) {
            if (c.cost > -2 && c.type == AbstractCard.CardType.ATTACK || c.type == AbstractCard.CardType.SKILL) {
                switch (action) {
                    case EXHAUST: {
                        if (!c.exhaust) {
                            validCards.add(c);
                            break;
                        }
                        break;
                    }
                    case ETHEREAL: {
                        if (!c.isEthereal && c.cost != 0) {
                            validCards.add(c);
                            break;
                        }
                        break;
                    }
                    case LOWERVALUES: {
                        if (!CardModifierManager.hasModifier(c, LowerValuesMod.ID) &&
                                !CardModifierManager.hasModifier(c, RaiseValuesMod.ID)
                                && (c.baseDamage > 0 || c.baseBlock > 0)) {
                            validCards.add(c);
                            break;
                        }
                        break;
                    }
                    case GAINVULNERABLE: {
                        if (!CardModifierManager.hasModifier(c, GainVulnerableMod.ID)) {
                            validCards.add(c);
                            break;
                        }
                        break;
                    }
                    case GAINWEAK: {
                        if (!CardModifierManager.hasModifier(c, GainWeakMod.ID)) {
                            validCards.add(c);
                            break;
                        }
                        break;
                    }
                    case GAINFRAIL: {
                        if (!CardModifierManager.hasModifier(c, GainFrailMod.ID)) {
                            validCards.add(c);
                            break;
                        }
                        break;
                    }
                    case INNATE: {
                        if (!c.isInnate) {
                            validCards.add(c);
                            break;
                        }
                        break;
                    }
                    case RETAIN: {
                        if (!c.selfRetain) {
                            validCards.add(c);
                            break;
                        }
                        break;
                    }
                    case RAISEVALUES: {
                        if (!CardModifierManager.hasModifier(c, LowerValuesMod.ID) &&
                                !CardModifierManager.hasModifier(c, RaiseValuesMod.ID)
                                && (c.baseDamage > 0 || c.baseBlock > 0)) {
                            validCards.add(c);
                            break;
                        }
                        break;
                    }
                    case DRAW: {
                        if (!CardModifierManager.hasModifier(c, DrawCardMod.ID)) {
                            validCards.add(c);
                            break;
                        }
                        break;
                    }
                    case APPLYVULNERABLE: {
                        if (!CardModifierManager.hasModifier(c, ApplyVulnerableMod.ID)
                                && (c.target == AbstractCard.CardTarget.ENEMY || c.target == AbstractCard.CardTarget.SELF_AND_ENEMY)) {
                            validCards.add(c);
                            break;
                        }
                        break;
                    }
                    case APPLYWEAK: {
                        if (!CardModifierManager.hasModifier(c, ApplyWeakMod.ID)
                                && (c.target == AbstractCard.CardTarget.ENEMY || c.target == AbstractCard.CardTarget.SELF_AND_ENEMY)) {
                            validCards.add(c);
                            break;
                        }
                        break;
                    }
                }
            }
        }

        return validCards;
    }


    public String getStringForOption(actionType action) {
        return DESCRIPTIONS[enumToIndex(action)];
    }

    public int enumToIndex(actionType action) {
        switch (action) {
            case EXHAUST:
                return 6;
            case ETHEREAL:
                return 7;
            case LOWERVALUES:
                return 8;
            case GAINFRAIL:
                return 3;
            case GAINVULNERABLE:
                return 4;
            case GAINWEAK:
                return 5;
            case INNATE:
                return 9;
            case RETAIN:
                return 10;
            case RAISEVALUES:
                return 11;
            case DRAW:
                return 2;
            case APPLYVULNERABLE:
                return 0;
            case APPLYWEAK:
                return 1;
        }
        return 0;
    }

    public void refreshOptionForArmorer(actionType action, boolean good) {
        String result = "";

        boolean locked = false;
        if (alreadyClaimed[enumToIndex(action)]) {
            result = OPTIONS[5];
            locked = true;
        } else {
            if (getCurrentActionValidCards(action).size() == 0) {
                result = OPTIONS[4];
                locked = true;
            } else {
                if (good) {
                    result = DESCRIPTIONS[12];
                } else {
                    result = DESCRIPTIONS[21];
                }
                result = result + getStringForOption(action) + DESCRIPTIONS[13];

            }
        }
        this.imageEventText.setDialogOption(result, locked);
    }

    public void applyCurrentActionToCard(AbstractCard c) {

        AbstractCardModifier newMod = null;
        switch (currentAction) {
            case EXHAUST: {
                newMod = new ExhaustMod();
                break;
            }
            case ETHEREAL: {
                newMod = new EtherealMod();
                break;
            }
            case LOWERVALUES: {
                newMod = new LowerValuesMod();
                break;
            }
            case GAINFRAIL: {
                newMod = new GainFrailMod();
                break;
            }
            case GAINVULNERABLE: {
                newMod = new GainVulnerableMod();
                break;
            }
            case GAINWEAK: {
                newMod = new GainWeakMod();
                break;
            }
            case INNATE: {
                newMod = new InnateMod();
                break;
            }
            case RETAIN: {
                newMod = new RetainMod();
                break;
            }
            case RAISEVALUES: {
                newMod = new RaiseValuesMod();
                break;
            }
            case DRAW: {
                newMod = new DrawCardMod();
                break;
            }
            case APPLYVULNERABLE: {
                newMod = new ApplyVulnerableMod();
                break;
            }
            case APPLYWEAK: {
                newMod = new ApplyWeakMod();
                break;
            }
        }
        CardModifierManager.addModifier(c, newMod);
        alreadyClaimed[enumToIndex(currentAction)] = true;
    }

    public void update() {
        super.update();
        if (!AbstractDungeon.gridSelectScreen.selectedCards.isEmpty()) {
            AbstractCard c = AbstractDungeon.gridSelectScreen.selectedCards.get(0);
            applyCurrentActionToCard(c);
            this.screenNum++;
            AbstractDungeon.gridSelectScreen.selectedCards.clear();
            if (this.screenNum == 2) {
                refreshGoodOptions();
                this.imageEventText.updateBodyText(DESCRIPTIONS[16]);
            }
            if (this.screenNum == 3) {
                numberOfTimesUsed++;
                if (numberOfTimesUsed == 3) {
                    this.imageEventText.setDialogOption(OPTIONS[6]);
                    this.imageEventText.updateBodyText(DESCRIPTIONS[18]);
                    this.screenNum = 4;
                } else {
                    this.imageEventText.setDialogOption(OPTIONS[1] + OPTIONS[3]);
                    if (AbstractDungeon.player.gold >= 40) {
                        this.imageEventText.setDialogOption(OPTIONS[2] + OPTIONS[3]);
                    } else {
                        this.imageEventText.setDialogOption(OPTIONS[8], true);
                    }
                    this.imageEventText.setDialogOption(OPTIONS[6]);
                    if (numberOfTimesUsed == 1) {
                        this.imageEventText.updateBodyText(DESCRIPTIONS[17]);
                    } else {
                        this.imageEventText.updateBodyText(DESCRIPTIONS[20]);
                    }
                }

            }
        }

    }

    public void refreshBadOptions() {
        refreshOptionForArmorer(actionType.EXHAUST, false);
        refreshOptionForArmorer(actionType.ETHEREAL, false);
        refreshOptionForArmorer(actionType.LOWERVALUES, false);
        refreshOptionForArmorer(actionType.GAINFRAIL, false);
        refreshOptionForArmorer(actionType.GAINWEAK, false);
        refreshOptionForArmorer(actionType.GAINVULNERABLE, false);
    }

    public void refreshGoodOptions() {
        refreshOptionForArmorer(actionType.INNATE, true);
        refreshOptionForArmorer(actionType.RETAIN, true);
        refreshOptionForArmorer(actionType.RAISEVALUES, true);
        refreshOptionForArmorer(actionType.DRAW, true);
        refreshOptionForArmorer(actionType.APPLYWEAK, true);
        refreshOptionForArmorer(actionType.APPLYVULNERABLE, true);
    }

    public void showChoicesGrid() {
        CardGroup choices = new CardGroup(CardGroup.CardGroupType.UNSPECIFIED);
        for (AbstractCard c : getCurrentActionValidCards(currentAction)) {
            choices.addToBottom(c);
        }
        String display = DESCRIPTIONS[12] + getStringForOption(currentAction) + DESCRIPTIONS[13];
        display = display.replace("#y","");
        display = display.replace("#r","");
        display = display.replace("#g","");
        AbstractDungeon.gridSelectScreen.open(choices, 1, display, false);
    }

    protected void buttonEffect(int buttonPressed) {
        switch (this.screenNum) {
            case 0: //First Screen
                switch (buttonPressed) {
                    case 0:
                        this.screenNum = 1;
                        this.imageEventText.clearAllDialogs();
                        refreshBadOptions();
                        this.imageEventText.updateBodyText(DESCRIPTIONS[15]);
                        return;
                    case 1:
                      //  currentPhase = new TextPhase("");
                        transitionPhase(new CombatPhase(Encounters.CULTIST_ARMORER, true));
                        AbstractDungeon.getCurrRoom().rewards.add(new RewardItem(new ArmorersMask()));
                        return;
                }
            case 1: //Choose A Bad

                switch (buttonPressed) {
                    case 0:
                        currentAction = actionType.EXHAUST;
                        break;
                    case 1:
                        currentAction = actionType.ETHEREAL;
                        break;
                    case 2:
                        currentAction = actionType.LOWERVALUES;
                        break;
                    case 3:
                        currentAction = actionType.GAINFRAIL;
                        break;
                    case 4:
                        currentAction = actionType.GAINWEAK;
                        break;
                    case 5:
                        currentAction = actionType.GAINVULNERABLE;
                        break;
                }
                showChoicesGrid();
                this.imageEventText.clearAllDialogs();
                return;
            case 2: //Choose A Good

                switch (buttonPressed) {
                    case 0:
                        currentAction = actionType.INNATE;
                        break;
                    case 1:
                        currentAction = actionType.RETAIN;
                        break;
                    case 2:
                        currentAction = actionType.RAISEVALUES;
                        break;
                    case 3:
                        currentAction = actionType.DRAW;
                        break;
                    case 4:
                        currentAction = actionType.APPLYWEAK;
                        break;
                    case 5:
                        currentAction = actionType.APPLYVULNERABLE;
                        break;
                }
                showChoicesGrid();
                this.imageEventText.clearAllDialogs();
                return;
            case 3: //Repeat Actions
                switch (buttonPressed) {
                    case 0:
                        this.screenNum = 1;
                        this.imageEventText.clearAllDialogs();
                        refreshBadOptions();
                        AbstractDungeon.player.damage(new DamageInfo((AbstractCreature) null, 10, DamageInfo.DamageType.HP_LOSS));
                        CardCrawlGame.sound.play("BLOOD_SPLAT");
                        this.imageEventText.updateBodyText(DESCRIPTIONS[15]);
                        return;
                    case 1:
                        this.screenNum = 1;
                        this.imageEventText.clearAllDialogs();
                        refreshBadOptions();
                        AbstractDungeon.player.loseGold(40);
                        this.imageEventText.updateBodyText(DESCRIPTIONS[15]);
                        return;
                    case 2:
                        this.screenNum = 4;
                        this.imageEventText.clearAllDialogs();
                        this.imageEventText.updateBodyText(DESCRIPTIONS[19]);
                        this.imageEventText.setDialogOption(OPTIONS[6]);
                        return;

                }
                return;
            case 4:
                this.openMap();


        }

    }
}
