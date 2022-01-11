package BattleTowers.patches;

import BattleTowers.room.BattleTowerRoom;
import com.evacipated.cardcrawl.modthespire.lib.SpireInstrumentPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.rooms.EventRoom;
import com.megacrit.cardcrawl.ui.buttons.ProceedButton;
import javassist.CannotCompileException;
import javassist.NotFoundException;
import javassist.expr.ExprEditor;
import javassist.expr.Instanceof;

@SpirePatch(
        clz = ProceedButton.class,
        method = "update"
)
public class BasicallyAnEvent {
    @SpireInstrumentPatch
    public static ExprEditor thisKindToo() {
        return new ExprEditor() {
            boolean first = true;

            @Override
            public void edit(Instanceof i) throws CannotCompileException {
                if (first) {
                    try {
                        if (i.getType().getName().equals(EventRoom.class.getName())) {
                            i.replace("{$_ = ($proceed($$) || $1 instanceof " + BattleTowerRoom.class.getName() + ");}");
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
