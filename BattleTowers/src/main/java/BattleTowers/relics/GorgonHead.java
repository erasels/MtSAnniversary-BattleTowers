package BattleTowers.relics;

import BattleTowers.util.TextureLoader;
import basemod.ReflectionHacks;
import basemod.abstracts.CustomRelic;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.RelicAboveCreatureAction;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.EnemyMoveInfo;
import com.megacrit.cardcrawl.powers.ArtifactPower;
import com.megacrit.cardcrawl.powers.GainStrengthPower;
import com.megacrit.cardcrawl.powers.StrengthPower;

import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Set;

import static BattleTowers.BattleTowers.makeID;
import static BattleTowers.BattleTowers.makeRelicPath;

public class GorgonHead extends CustomRelic {
    public static final String ID = makeID(GorgonHead.class.getSimpleName());

    private static final int STRENGTH_LOSS = 1;

    private Set<AbstractMonster> doneMonsters;

    public GorgonHead() {
        super(ID, TextureLoader.getTexture(makeRelicPath(GorgonHead.class.getSimpleName() + ".png")), TextureLoader.getTexture(makeRelicPath("outline/" + GorgonHead.class.getSimpleName() + ".png")), RelicTier.SPECIAL, LandingSound.FLAT);
        this.doneMonsters = new HashSet<>();
    }

    @Override
    public void atBattleStart() {
        this.doneMonsters.clear();
    }

    @Override
    public void atTurnStart() {
        for (AbstractMonster m : AbstractDungeon.getMonsters().monsters) {
            if (!doneMonsters.contains(m)) {
                EnemyMoveInfo emi = ReflectionHacks.getPrivate(m, AbstractMonster.class, "move");
                if (emi != null && emi.isMultiDamage && emi.multiplier > 1) {
                    this.flash();
                    this.addToBot(new RelicAboveCreatureAction(m, this));
                    this.addToBot(new ApplyPowerAction(m, AbstractDungeon.player, new StrengthPower(m, -STRENGTH_LOSS), -STRENGTH_LOSS, true, AbstractGameAction.AttackEffect.NONE));
                    if (!m.hasPower(ArtifactPower.POWER_ID)) {
                        this.addToBot(new ApplyPowerAction(m, AbstractDungeon.player, new GainStrengthPower(m, STRENGTH_LOSS), STRENGTH_LOSS, true, AbstractGameAction.AttackEffect.NONE));
                    }
                    this.doneMonsters.add(m);
                }
            }
        }
    }

    @Override
    public void onVictory() {
        this.doneMonsters.clear();
    }

    @Override
    public String getUpdatedDescription() {
        return MessageFormat.format(DESCRIPTIONS[0], STRENGTH_LOSS);
    }
}
