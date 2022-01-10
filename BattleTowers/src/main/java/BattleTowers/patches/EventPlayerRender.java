package BattleTowers.patches;

import BattleTowers.events.PhasedEvent;
import com.evacipated.cardcrawl.modthespire.lib.SpireInstrumentPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import com.megacrit.cardcrawl.rooms.EventRoom;
import javassist.CannotCompileException;
import javassist.NotFoundException;
import javassist.expr.ExprEditor;
import javassist.expr.Instanceof;

@SpirePatch(
        clz = AbstractRoom.class,
        method = "render"
)
public class EventPlayerRender {
    @SpireInstrumentPatch
    public static ExprEditor thisKindToo() {
        return new ExprEditor() {
            boolean first = true;

            @Override
            public void edit(Instanceof i) throws CannotCompileException {
                if (first) {
                    try {
                        if (i.getType().getName().equals(EventRoom.class.getName())) {
                            i.replace("{$_ = " +
                                    "($proceed($$) || " +
                                        "(((" + AbstractRoom.class.getName() + ")$1).event instanceof " + PhasedEvent.class.getName() + " && " +
                                        "!((" + PhasedEvent.class.getName() + ")((" + AbstractRoom.class.getName() + ")$1).event).renderPlayer())" +
                                    ");}");
                            first = false;
                        }
                    } catch (NotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
    }
}
