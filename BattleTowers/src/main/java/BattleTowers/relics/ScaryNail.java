package BattleTowers.relics;

import BattleTowers.util.TextureLoader;
import BattleTowers.util.UC;
import basemod.abstracts.CustomRelic;
import com.megacrit.cardcrawl.actions.common.RelicAboveCreatureAction;
import com.megacrit.cardcrawl.powers.StrengthPower;

import static BattleTowers.BattleTowers.makeID;
import static BattleTowers.BattleTowers.makeRelicPath;

public class ScaryNail extends CustomRelic {
    public static final String ID = makeID(ScaryNail.class.getSimpleName());

    private static final int STR_PER = 1;

    private boolean setup = false;
    private int monsters;

    public ScaryNail() {
        super(ID, TextureLoader.getTexture(makeRelicPath("ScaryNail.png")), RelicTier.SPECIAL, LandingSound.CLINK);
        description = getUpdatedDescription();
    }

    @Override
    public void atBattleStart() {
        UC.atb(new RelicAboveCreatureAction(UC.p(), this));
        monsters = UC.getAliveMonsters().size();
        UC.doPow(new StrengthPower(UC.p(), monsters));
        setup = true;
    }

    @Override
    public void onVictory() {
        setup = false;
    }

    @Override
    public void update() {
        super.update();
        if(setup) {
            int new_monsters = UC.getAliveMonsters().size();
            if(new_monsters != monsters) {
                UC.doPow(new StrengthPower(UC.p(), new_monsters - monsters));
                flash();
                monsters = new_monsters;
            }
        }
    }

    @Override
    public String getUpdatedDescription() {
        return String.format(DESCRIPTIONS[0], STR_PER);
    }
}
