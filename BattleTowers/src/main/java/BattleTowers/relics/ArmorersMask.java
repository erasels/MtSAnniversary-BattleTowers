package BattleTowers.relics;

import BattleTowers.util.TextureLoader;
import BattleTowers.util.UC;
import BattleTowers.vfx.Hats;
import basemod.ReflectionHacks;
import basemod.abstracts.CustomRelic;
import com.esotericsoftware.spine.Skeleton;
import com.megacrit.cardcrawl.actions.animations.TalkAction;
import com.megacrit.cardcrawl.actions.common.GainBlockAction;
import com.megacrit.cardcrawl.actions.common.RelicAboveCreatureAction;
import com.megacrit.cardcrawl.actions.utility.SFXAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.RelicStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.DexterityPower;

import static BattleTowers.BattleTowers.makeID;
import static BattleTowers.BattleTowers.makeRelicPath;

public class ArmorersMask extends CustomRelic {
    public static final String ID = makeID(ArmorersMask.class.getSimpleName());
    private static RelicStrings relicStrings = CardCrawlGame.languagePack.getRelicStrings(ID);

   // private boolean hatLoaded;

    public ArmorersMask() {
        super(ID, TextureLoader.getTexture(makeRelicPath("ArmorersMask.png")), RelicTier.SPECIAL, LandingSound.FLAT);
        description = getUpdatedDescription();
    }


  /*
    @Override
    public void update() {
        super.update();
        if (!hatLoaded) {
            if (AbstractDungeon.player != null) {
                Skeleton sk = null;
                sk = ReflectionHacks.getPrivate(AbstractDungeon.player, AbstractCreature.class, "skeleton");
                if (sk != null) {
                    loadHat();
                    hatLoaded = true;
                }
            }
        }
    }

   */

    public void atBattleStart() {
        flash();
        addToBot(new RelicAboveCreatureAction(com.megacrit.cardcrawl.dungeons.AbstractDungeon.player, this));
        addToBot(new SFXAction("VO_CULTIST_1A"));
        addToBot(new TalkAction(true, this.DESCRIPTIONS[1], 1.0F, 2.0F));
        UC.doPow(new DexterityPower(UC.p(), 1));
    }

    /*
    public void loadHat() {
        super.onEquip();

        Hats.addHat(AbstractDungeon.player,
                ReflectionHacks.getPrivate(AbstractDungeon.player, AbstractCreature.class, "skeleton"),
                "armorersMask",
                makeRelicPath("ArmorersMask_Hat.png"));
    }

     */

    @Override
    public String getUpdatedDescription() {
        return DESCRIPTIONS[0];
    }
}
