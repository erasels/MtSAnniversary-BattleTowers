package BattleTowers.relics;

import BattleTowers.util.TextureLoader;
import basemod.abstracts.CustomRelic;
import com.megacrit.cardcrawl.actions.common.DrawCardAction;
import com.megacrit.cardcrawl.actions.common.GainEnergyAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.localization.RelicStrings;

import static BattleTowers.BattleTowers.makeID;
import static BattleTowers.BattleTowers.makeRelicPath;

public class RubyFragment extends CustomRelic {
    public static final String ID = makeID(RubyFragment.class.getSimpleName());
    private static RelicStrings relicStrings = CardCrawlGame.languagePack.getRelicStrings(ID);
    public RubyFragment() {
        super(ID, TextureLoader.getTexture(makeRelicPath("RubyFragment.png")), RelicTier.SPECIAL, LandingSound.FLAT);
        description = getUpdatedDescription();
        counter = -1;
    }
    public void atBattleStart() {
        counter = 0;
        usedUp = false;
    }
    public void onAttack(DamageInfo info, int damageAmount, AbstractCreature target) {
        if(!usedUp)
            counter += damageAmount;
        if (counter > 15 && !usedUp){
            addToBot(new GainEnergyAction(1));
            addToBot(new DrawCardAction(2));
            usedUp = true;
            counter = -1;
            grayscale = true;
        }
    }

    @Override
    public void onVictory() {
        grayscale = false;
    }

    @Override
    public String getUpdatedDescription() {
        return DESCRIPTIONS[0];
    }
}