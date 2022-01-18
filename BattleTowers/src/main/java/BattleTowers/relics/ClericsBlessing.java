package BattleTowers.relics;

import BattleTowers.events.RoarOfTheCrowd;
import BattleTowers.room.BattleTowerRoom;
import BattleTowers.util.TextureLoader;
import basemod.abstracts.CustomRelic;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.DexterityPower;

import java.text.MessageFormat;

import static BattleTowers.BattleTowers.makeID;
import static BattleTowers.BattleTowers.makeRelicPath;

public class ClericsBlessing extends CustomRelic {
    public static final String ID = makeID(ClericsBlessing.class.getSimpleName());

    public ClericsBlessing() {
        super(ID, TextureLoader.getTexture(makeRelicPath(ClericsBlessing.class.getSimpleName() + ".png")), TextureLoader.getTexture(makeRelicPath("outline/" + ClericsBlessing.class.getSimpleName() + ".png")), RelicTier.SPECIAL, LandingSound.FLAT);
        this.counter = 1;
    }

    @Override
    public void atBattleStart() {
        if (this.counter != -1 && AbstractDungeon.getCurrRoom() instanceof BattleTowerRoom) {
            this.addToBot(new ApplyPowerAction(AbstractDungeon.player, AbstractDungeon.player, new DexterityPower(AbstractDungeon.player, RoarOfTheCrowd.DEXTERITY_FOR_TOWER)));
        }
    }

    @Override
    public void onVictory() {
        if (this.counter != -1 && AbstractDungeon.getCurrRoom() instanceof BattleTowerRoom) {
            if (AbstractDungeon.getCurrRoom().monsters != null
                    && AbstractDungeon.getCurrRoom().monsters.monsters != null
                    && AbstractDungeon.getCurrRoom().monsters.monsters.stream().anyMatch(m -> m.type == AbstractMonster.EnemyType.BOSS)) {
                this.counter = -1;
                this.usedUp();
            }
        }
    }

    @Override
    public String getUpdatedDescription() {
        return MessageFormat.format(DESCRIPTIONS[0], RoarOfTheCrowd.DEXTERITY_FOR_TOWER);
    }
}
