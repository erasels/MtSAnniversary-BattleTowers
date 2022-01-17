package BattleTowers.monsters;

import BattleTowers.powers.CursedTotemPower;
import com.megacrit.cardcrawl.actions.common.RollMoveAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.localization.MonsterStrings;

import static BattleTowers.BattleTowers.makeID;
import static BattleTowers.BattleTowers.makeImagePath;

public class NecrototemMinion extends AbstractBTMonster
{
    public static final String ID = makeID(NecrototemMinion.class.getSimpleName());
    private static final MonsterStrings monsterStrings = CardCrawlGame.languagePack.getMonsterStrings(ID);
    public static final String NAME = monsterStrings.NAME;
    public static final String[] MOVES = monsterStrings.MOVES;
    public static final String[] DIALOG = monsterStrings.DIALOG;

    //name of the monster's moves
    private static final byte NOTHING = 0;

    public NecrototemMinion(final float x, final float y) {
        super(NAME, ID, 10, -8.0F, 10.0f, 130.0f, 200.0f, makeImagePath("monsters/Necrototem/necromanticTotemMinion.png"), x, y);

        setHp(calcAscensionTankiness(5));

        addMove(NOTHING, Intent.NONE);
    }

    @Override
    protected void setUpMisc() {
        super.setUpMisc();
        this.type = EnemyType.NORMAL;
    }

    @Override
    public void takeTurn() {
        addToBot(new RollMoveAction(this));
    }

    @Override
    protected void getMove(final int num) {
        setMoveShortcut(NOTHING);
    }

    @Override
    public void damage(DamageInfo info)
    {
        if (info.output > 1 && hasPower(CursedTotemPower.POWER_ID)) {
            info.output = 1;
        }
        super.damage(info);
    }
}