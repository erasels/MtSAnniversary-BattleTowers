package BattleTowers.relics;

import BattleTowers.events.RoarOfTheCrowd;
import BattleTowers.room.BattleTowerRoom;
import BattleTowers.util.TextureLoader;
import basemod.abstracts.CustomRelic;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.DamageRandomEnemyAction;
import com.megacrit.cardcrawl.actions.common.GainBlockAction;
import com.megacrit.cardcrawl.actions.utility.UseCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

import java.text.MessageFormat;

import static BattleTowers.BattleTowers.makeID;
import static BattleTowers.BattleTowers.makeRelicPath;

public class QueensPawn extends CustomRelic {
    public static final String ID = makeID(QueensPawn.class.getSimpleName());

    int stateForTurn = 0;

    public QueensPawn() {
        super(ID, TextureLoader.getTexture(makeRelicPath(QueensPawn.class.getSimpleName() + ".png")), TextureLoader.getTexture(makeRelicPath("outline/" + QueensPawn.class.getSimpleName() + ".png")), RelicTier.SPECIAL, LandingSound.FLAT);
    }

    @Override
    public void atBattleStart() {
        stateForTurn = 0;
        this.img = TextureLoader.getTexture(makeRelicPath(QueensPawn.class.getSimpleName() + ".png"));
    }

    @Override
    public void atTurnStart(){
            stateForTurn = 0;
            this.img = TextureLoader.getTexture(makeRelicPath(QueensPawn.class.getSimpleName() + ".png"));
    }

    @Override
    public void onUseCard(AbstractCard targetCard, UseCardAction useCardAction) {
        if (stateForTurn == 0){
            switch (targetCard.type){
                case ATTACK:
                    stateForTurn = 1;
                    this.img = TextureLoader.getTexture(makeRelicPath(QueensPawn.class.getSimpleName() + "Black.png"));
                    break;
                case SKILL:
                    stateForTurn = 2;
                    this.img = TextureLoader.getTexture(makeRelicPath( QueensPawn.class.getSimpleName() + "White.png"));
                    break;
            }
        }
    }

    @Override
    public void onPlayerEndTurn() {
        switch(stateForTurn){
            case 1:
                if (!AbstractDungeon.actionManager.cardsPlayedThisCombat.isEmpty() && ((AbstractCard)AbstractDungeon.actionManager.cardsPlayedThisCombat.get(AbstractDungeon.actionManager.cardsPlayedThisCombat.size() - 1)).type == AbstractCard.CardType.ATTACK) {
                    AbstractDungeon.actionManager.addToBottom(new DamageRandomEnemyAction(new DamageInfo(AbstractDungeon.player, 5, DamageInfo.DamageType.THORNS), AbstractGameAction.AttackEffect.BLUNT_LIGHT));
                    this.flash();
                }
                break;
            case 2:
                if (!AbstractDungeon.actionManager.cardsPlayedThisCombat.isEmpty() && ((AbstractCard)AbstractDungeon.actionManager.cardsPlayedThisCombat.get(AbstractDungeon.actionManager.cardsPlayedThisCombat.size() - 1)).type == AbstractCard.CardType.SKILL) {
                    AbstractDungeon.actionManager.addToBottom(new GainBlockAction(AbstractDungeon.player, 5));
                    this.flash();
                }
        }
    }



    @Override
    public String getUpdatedDescription() {
        return MessageFormat.format(DESCRIPTIONS[0], RoarOfTheCrowd.GOLD_AT_END);
    }
}
