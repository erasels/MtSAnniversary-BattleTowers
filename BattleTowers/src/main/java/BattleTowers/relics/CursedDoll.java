package BattleTowers.relics;

import BattleTowers.util.TextureLoader;
import basemod.abstracts.CustomRelic;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.DamageAllEnemiesAction;
import com.megacrit.cardcrawl.actions.common.RelicAboveCreatureAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.RelicStrings;
import com.megacrit.cardcrawl.rooms.AbstractRoom;

import java.util.Arrays;

import static BattleTowers.BattleTowers.makeID;
import static BattleTowers.BattleTowers.makeRelicPath;

public class CursedDoll extends CustomRelic {
    public static final String ID = makeID(CursedDoll.class.getSimpleName());
    private static RelicStrings relicStrings = CardCrawlGame.languagePack.getRelicStrings(ID);

    public CursedDoll() {
        super(ID, TextureLoader.getTexture(makeRelicPath("CursedDoll.png")), RelicTier.SPECIAL, LandingSound.MAGICAL);
        description = getUpdatedDescription();
    }

    public void wasHPLost(int damageAmount) {
        if (damageAmount > 0 && AbstractDungeon.getCurrRoom().phase == AbstractRoom.RoomPhase.COMBAT) {
            this.flash();
            this.addToTop(new RelicAboveCreatureAction(AbstractDungeon.player, this));
            int[] newMultiDamage = new int[AbstractDungeon.getCurrRoom().monsters.monsters.size()];
            Arrays.fill(newMultiDamage, damageAmount * 2);
            AbstractDungeon.actionManager.addToTop(new DamageAllEnemiesAction(AbstractDungeon.player, newMultiDamage, DamageInfo.DamageType.HP_LOSS, AbstractGameAction.AttackEffect.POISON));
        }
    }

    @Override
    public String getUpdatedDescription() {
        return DESCRIPTIONS[0];
    }
}
