package BattleTowers.events.phases;

import BattleTowers.events.PhasedEvent;
import BattleTowers.util.Method;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.events.GenericEventDialog;
import com.megacrit.cardcrawl.relics.AbstractRelic;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class TextPhase extends EventPhase {
    private final String body;
    private final List<OptionInfo> options;
    private final List<Method> optionResults;

    public TextPhase(String bodyText) {
        body = bodyText;
        options = new ArrayList<>();
        optionResults = new ArrayList<>();
    }

    public void transition(PhasedEvent event) {
        event.imageEventText.updateBodyText(getBody());
        setOptions(event);
    }

    public EventPhase addOption(String optionText, Method onClick) {
        options.add(new OptionInfo(optionText));
        optionResults.add(onClick);
        return this;
    }
    public EventPhase addOption(OptionInfo option, Method onClick) {
        options.add(option);
        optionResults.add(onClick);
        return this;
    }


    public String getBody() {
        return body;
    }
    public void setOptions(PhasedEvent e) {
        e.imageEventText.clearAllDialogs();
        for (OptionInfo option : options) {
            option.set(e.imageEventText);
        }
    }
    public void optionChosen(int index) {
        if (index <= optionResults.size()) {
            optionResults.get(index).execute();
        }
    }

    public static class OptionInfo {
        private final OptionType type;
        private final String text;
        private final AbstractCard card;
        private final AbstractRelic relic;

        private Supplier<Boolean> condition;

        public OptionInfo(String text) {
            this.type = OptionType.TEXT;
            this.text = text;
            this.card = null;
            this.relic = null;
        }
        public OptionInfo(String text, AbstractCard c) {
            this.type = OptionType.CARD;
            this.text = text;
            this.card = c;
            this.relic = null;
        }
        public OptionInfo(String text, AbstractRelic r) {
            this.type = OptionType.RELIC;
            this.text = text;
            this.card = null;
            this.relic = r;
        }
        public OptionInfo(String text, AbstractCard c, AbstractRelic r) {
            this.type = OptionType.BOTH;
            this.text = text;
            this.card = c;
            this.relic = r;
        }
        public OptionInfo enabledCondition(Supplier<Boolean> enabledCondition) {
            this.condition = enabledCondition;
            return this;
        }

        private boolean disabled() {
            return condition != null && !condition.get();
        }

        public void set(GenericEventDialog dialog) {
            switch (type) {
                case CARD:
                    dialog.setDialogOption(text, disabled(), card);
                    break;
                case RELIC:
                    dialog.setDialogOption(text, disabled(), relic);
                    break;
                case BOTH:
                    dialog.setDialogOption(text, disabled(), card, relic);
                    break;
                default:
                    dialog.setDialogOption(text, disabled());
                    break;
            }
        }


        private enum OptionType {
            TEXT,
            CARD,
            RELIC,
            BOTH
        }
    }
}
