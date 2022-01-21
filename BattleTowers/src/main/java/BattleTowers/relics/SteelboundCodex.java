package BattleTowers.relics;

import BattleTowers.cards.Granted;
import BattleTowers.util.TextureLoader;
import basemod.abstracts.CustomRelic;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.RelicAboveCreatureAction;
import com.megacrit.cardcrawl.actions.utility.UseCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.powers.watcher.FreeAttackPower;

import static BattleTowers.BattleTowers.makeID;
import static BattleTowers.BattleTowers.makeRelicPath;

public class SteelboundCodex extends CustomRelic {
    public static final String ID = makeID(SteelboundCodex.class.getSimpleName());
    
    private final AbstractCard wish = new Granted();
    
    public SteelboundCodex() {
        super(ID, TextureLoader.getTexture(makeRelicPath("SteelboundCodex.png")), RelicTier.SPECIAL, LandingSound.CLINK);
        description = getUpdatedDescription();
    }
    
    
    public void atTurnStart() {
        this.counter = 0;
    }
    
    public void onUseCard(AbstractCard card, UseCardAction action) {
        if (card.type == AbstractCard.CardType.ATTACK && !card.freeToPlayOnce) {
            if (card.costForTurn > 0) {
                this.counter += card.costForTurn;
            }
            if (card.cost == -1) {
                this.counter += card.energyOnUse;
            }
            while (this.counter >= 3) {
                this.counter -= 3;
                this.flash();
                this.addToBot(new RelicAboveCreatureAction(AbstractDungeon.player, this));
                this.addToBot(new ApplyPowerAction(AbstractDungeon.player, AbstractDungeon.player, new FreeAttackPower(AbstractDungeon.player, 1), 1));
            }
        }
    }
    
    public void onVictory() {
        this.counter = -1;
    }
    
    @Override
    public String getUpdatedDescription() {
        return DESCRIPTIONS[0];
    }
}
