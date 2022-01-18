package BattleTowers.relics;

import BattleTowers.cards.WindStrike;
import BattleTowers.util.TextureLoader;
import basemod.abstracts.CustomRelic;
import com.megacrit.cardcrawl.actions.common.DrawCardAction;
import com.megacrit.cardcrawl.actions.common.GainEnergyAction;
import com.megacrit.cardcrawl.actions.common.MakeTempCardInHandAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.RelicStrings;
import com.megacrit.cardcrawl.vfx.cardManip.ShowCardAndObtainEffect;

import static BattleTowers.BattleTowers.makeID;
import static BattleTowers.BattleTowers.makeRelicPath;

public class RubyFragment extends CustomRelic {
    public static final String ID = makeID(RubyFragment.class.getSimpleName());
    private static RelicStrings relicStrings = CardCrawlGame.languagePack.getRelicStrings(ID);
    public RubyFragment() {
        super(ID, TextureLoader.getTexture(makeRelicPath("Lucky.png")), RelicTier.SPECIAL, LandingSound.FLAT);
        description = getUpdatedDescription();
        counter = 0;
    }
    public void atBattleStart() {
        counter = 0;
        usedUp = false;
    }
    public void onAttack(DamageInfo info, int damageAmount, AbstractCreature target) {
        counter += damageAmount;
        if (counter > 15 && !usedUp){
            addToBot(new GainEnergyAction(2));
            addToBot(new DrawCardAction(2));
            usedUp = true;
        }
    }
    @Override
    public String getUpdatedDescription() {
        return DESCRIPTIONS[0];
    }
}