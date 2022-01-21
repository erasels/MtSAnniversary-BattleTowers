package BattleTowers.relics;

import BattleTowers.cards.Granted;
import BattleTowers.util.TextureLoader;
import basemod.abstracts.CustomRelic;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.RelicAboveCreatureAction;
import com.megacrit.cardcrawl.actions.utility.UseCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.powers.StrengthPower;
import com.megacrit.cardcrawl.powers.watcher.FreeAttackPower;
import com.megacrit.cardcrawl.rooms.AbstractRoom;

import static BattleTowers.BattleTowers.makeID;
import static BattleTowers.BattleTowers.makeRelicPath;

public class SteelboundCodex extends CustomRelic {
    public static final String ID = makeID(SteelboundCodex.class.getSimpleName());
    
    private final AbstractCard wish = new Granted();
    
    public SteelboundCodex() {
        super(ID, TextureLoader.getTexture(makeRelicPath("SteelboundCodex.png")), RelicTier.SPECIAL, LandingSound.CLINK);
        description = getUpdatedDescription();
    }
    
    @Override
    public int onAttacked(DamageInfo info, int damageAmount) {
        if (damageAmount > 0 && AbstractDungeon.getCurrRoom().phase == AbstractRoom.RoomPhase.COMBAT && info.type.equals(DamageInfo.DamageType.NORMAL)) {
            this.flash();
            this.addToTop(new ApplyPowerAction(AbstractDungeon.player, AbstractDungeon.player, new StrengthPower(AbstractDungeon.player, 1), 1));
        }
        return damageAmount;
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
