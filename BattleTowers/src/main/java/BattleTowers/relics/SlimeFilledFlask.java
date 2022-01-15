package BattleTowers.relics;

import BattleTowers.cards.SlimeElixir;
import BattleTowers.util.TextureLoader;
import basemod.ReflectionHacks;
import basemod.abstracts.CustomRelic;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.MakeTempCardInHandAction;
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

public class SlimeFilledFlask extends CustomRelic {
    public static final String ID = makeID(SlimeFilledFlask.class.getSimpleName());

    public SlimeFilledFlask() {
        super(ID, TextureLoader.getTexture(makeRelicPath(SlimeFilledFlask.class.getSimpleName() + ".png")), TextureLoader.getTexture(makeRelicPath("outline/" + SlimeFilledFlask.class.getSimpleName() + ".png")), RelicTier.SPECIAL, LandingSound.FLAT);
    }

    @Override
    public void atBattleStart() {
        this.flash();
        this.addToTop(new MakeTempCardInHandAction(new SlimeElixir()));
    }

    @Override
    public String getUpdatedDescription() {
        return DESCRIPTIONS[0];
    }
}
