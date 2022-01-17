package BattleTowers.patches;

import BattleTowers.events.PhasedEvent;
import BattleTowers.events.phases.MiniRestPhase;
import BattleTowers.room.BattleTowerRoom;
import com.evacipated.cardcrawl.modthespire.lib.SpireInstrumentPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.rooms.RestRoom;
import javassist.CannotCompileException;
import javassist.NotFoundException;
import javassist.expr.ExprEditor;
import javassist.expr.Instanceof;

@SpirePatch(
        clz = AbstractPlayer.class,
        method = "render"
)
public class PlayerRestRender {
    @SpireInstrumentPatch
    public static ExprEditor thisIsKindaSortaARestRoom() {
        return new ExprEditor() {
            @Override
            public void edit(Instanceof i) throws CannotCompileException {
                try {
                    if (i.getType().getName().equals(RestRoom.class.getName())) {
                        i.replace("{$_ = ($proceed($$) || " + PlayerRestRender.class.getName() + ".isRest());}");
                    }
                } catch (NotFoundException e) {
                    e.printStackTrace();
                }
            }
        };
    }

    public static boolean isRest() {
        return AbstractDungeon.currMapNode != null && AbstractDungeon.getCurrRoom() != null && AbstractDungeon.getCurrRoom().event instanceof PhasedEvent && ((PhasedEvent) AbstractDungeon.getCurrRoom().event).currentPhase instanceof MiniRestPhase;
    }
}
