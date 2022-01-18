package BattleTowers.relics;

import BattleTowers.cards.Granted;
import BattleTowers.util.TextureLoader;
import basemod.abstracts.CustomRelic;
import basemod.helpers.CardPowerTip;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.DrawCardAction;
import com.megacrit.cardcrawl.actions.common.MakeTempCardInDrawPileAction;
import com.megacrit.cardcrawl.actions.common.RelicAboveCreatureAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.powers.StrengthPower;
import com.megacrit.cardcrawl.rooms.AbstractRoom;

import static BattleTowers.BattleTowers.makeID;
import static BattleTowers.BattleTowers.makeRelicPath;

public class SweatyArmband extends CustomRelic {
    public static final String ID = makeID(SweatyArmband.class.getSimpleName());

    private final AbstractCard wish = new Granted();

    public SweatyArmband() {
        super(ID, TextureLoader.getTexture(makeRelicPath("Armband.png")), RelicTier.SPECIAL, LandingSound.MAGICAL);
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


    @Override
    public String getUpdatedDescription() {
        return DESCRIPTIONS[0];
    }
}
