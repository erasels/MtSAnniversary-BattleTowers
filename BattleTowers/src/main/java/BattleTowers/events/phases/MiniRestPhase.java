package BattleTowers.events.phases;

import BattleTowers.events.PhasedEvent;
import BattleTowers.util.Method;
import BattleTowers.vfx.MiniCampfireSleepEffect;
import BattleTowers.vfx.MiniCampfireSmithEffect;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.dungeons.TheEnding;
import com.megacrit.cardcrawl.helpers.*;
import com.megacrit.cardcrawl.helpers.controller.CInputActionSet;
import com.megacrit.cardcrawl.helpers.controller.CInputHelper;
import com.megacrit.cardcrawl.helpers.input.InputHelper;
import com.megacrit.cardcrawl.localization.LocalizedStrings;
import com.megacrit.cardcrawl.localization.UIStrings;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.relics.RegalPillow;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import com.megacrit.cardcrawl.rooms.RestRoom;
import com.megacrit.cardcrawl.screens.mainMenu.ScrollBar;
import com.megacrit.cardcrawl.screens.mainMenu.ScrollBarListener;
import com.megacrit.cardcrawl.ui.buttons.ConfirmButton;
import com.megacrit.cardcrawl.ui.campfire.AbstractCampfireOption;
import com.megacrit.cardcrawl.ui.campfire.RestOption;
import com.megacrit.cardcrawl.ui.campfire.SmithOption;
import com.megacrit.cardcrawl.vfx.AbstractGameEffect;
import com.megacrit.cardcrawl.vfx.BobEffect;
import com.megacrit.cardcrawl.vfx.campfire.*;

import java.util.ArrayList;
import java.util.Iterator;

//Campfire options assume the current room to be a RestRoom and perform casts.
//campfireUI will have to be replaced with a mimicked version.
//It's heavily intertwined in AbstractCampfireOption.
//Replace the rest icon with a "smaller" fire to indicate you can't do as much at it?
//Only rest or smith?
public class MiniRestPhase extends EventPhase {
    private MiniCampfireUI campfireUI = null;
    public long fireSoundId;

    private PhasedEvent event;
    private Object followupKey;

    private boolean done = false;
    private float transitionTimer = 1.0f;

    public MiniRestPhase() {
    }

    @Override
    public void transition(PhasedEvent event) {
        this.event = event;
        AbstractDungeon.rs = AbstractDungeon.RenderScene.CAMPFIRE;

        if (!AbstractDungeon.id.equals(TheEnding.ID)) {
            CardCrawlGame.music.silenceBGM();
        }

        this.fireSoundId = CardCrawlGame.sound.playAndLoop("REST_FIRE_WET");
        RestRoom.lastFireSoundId = this.fireSoundId;
        campfireUI = new MiniCampfireUI(this);

        for (AbstractRelic r : AbstractDungeon.player.relics) {
            r.onEnterRestRoom();
        }
    }

    @Override
    public void hide(PhasedEvent event) {

    }

    public EventPhase setNextKey(Object key) {
        this.followupKey = key;
        return this;
    }

    public void reopen() {
        if (this.campfireUI != null) {
            this.campfireUI.reopen();
        }
    }

    @Override
    public void update() {
        if (this.campfireUI != null) {
            this.campfireUI.update();
        }
        if (done && transitionTimer > 0f) {
            transitionTimer -= Gdx.graphics.getDeltaTime();
            if (transitionTimer <= 0f) {
                if (event != null && followupKey != null) {
                    event.transitionKey(followupKey);
                }
                else {
                    AbstractDungeon.getCurrRoom().phase = AbstractRoom.RoomPhase.COMPLETE;
                }
            }
        }
    }

    @Override
    public void render(SpriteBatch sb) {
        if (this.campfireUI != null) {
            this.campfireUI.render(sb);
        }
    }

    public void finish(boolean sounds) {
        if (!done) {
            if (followupKey != null && event != null) {
                AbstractDungeon.getCurrRoom().phase = AbstractRoom.RoomPhase.INCOMPLETE;
            }
            else {
                AbstractDungeon.getCurrRoom().phase = AbstractRoom.RoomPhase.COMPLETE;
            }
            if (sounds) {
                fadeIn();
                cutFireSound();
            }
            done = true;
        }
    }

    public void fadeIn() {
        if (!AbstractDungeon.id.equals(TheEnding.ID)) {
            CardCrawlGame.music.unsilenceBGM();
        }
    }

    public void cutFireSound() {
        CardCrawlGame.sound.fadeOut("REST_FIRE_WET", fireSoundId);
    }
    //updateAmbience method of RestRoom - unused


    public static class MiniCampfireUI implements ScrollBarListener {
        private static final UIStrings uiStrings = CardCrawlGame.languagePack.getUIString("CampfireUI");
        public static final String[] TEXT = uiStrings.TEXT;
        private static final float START_Y = (float)Settings.HEIGHT - 300.0F * Settings.scale;
        private static final float BUTTON_START_X = (float)Settings.WIDTH * 0.416F;
        private static final float BUTTON_SPACING_X = 300.0F * Settings.xScale;
        private static final float BUTTON_START_Y = (float)Settings.HEIGHT / 2.0F + 180.0F * Settings.scale;
        private static final float BUTTON_SPACING_Y = -200.0F * Settings.scale;
        private static final float BUTTON_EXTRA_SPACING_Y = -70.0F * Settings.scale;
        private static final int MAX_BUTTONS_BEFORE_SCROLL = 6;

        public static boolean hidden = false;

        public boolean somethingSelected = false;
        private float hideStuffTimer = 0.5F;
        private float charAnimTimer = 2.0F;
        private ArrayList<MiniCampfireOption> buttons = new ArrayList();
        private ArrayList<CampfireBubbleEffect> bubbles = new ArrayList();
        private float fireTimer = 0.0F;
        private static final float FIRE_INTERVAL = 0.08F;
        private ArrayList<AbstractGameEffect> flameEffects = new ArrayList();
        private int bubbleAmt;
        private String bubbleMsg;
        private BobEffect effect = new BobEffect(2.0F);
        private boolean grabbedScreen = false;
        private float grabStartY = 0.0F;
        private float scrollY;
        private float targetY;
        private float scrollLowerBound;
        private float scrollUpperBound;
        private ScrollBar scrollBar;
        public ConfirmButton confirmButton;
        public MiniCampfireOption touchOption;

        private MiniRestPhase parent;

        public MiniCampfireUI(MiniRestPhase restPhase) {
            this.parent = restPhase;

            this.scrollY = START_Y;
            this.targetY = this.scrollY;
            this.scrollLowerBound = (float)Settings.HEIGHT - 300.0F * Settings.scale;
            this.scrollUpperBound = 2400.0F * Settings.scale;
            this.confirmButton = new ConfirmButton();
            this.touchOption = null;
            this.scrollBar = new ScrollBar(this);
            hidden = false;
            this.initializeButtons();
            if (this.buttons.size() > 2) {
                this.bubbleAmt = 60;
            } else {
                this.bubbleAmt = 40;
            }

            this.bubbleMsg = this.getCampMessage();
        }

        private void initializeButtons() {
            int heal = getHealAmount();
            String description = ModHelper.isModEnabled("Night Terrors") ? RestOption.TEXT[1] + heal + ")" + LocalizedStrings.PERIOD :
                    RestOption.TEXT[3] + heal + ")" + LocalizedStrings.PERIOD;
            AbstractRelic pillow = AbstractDungeon.player.getRelic(RegalPillow.ID);
            if (pillow != null)
                description += "\n+15" + RestOption.TEXT[2] + pillow.name + LocalizedStrings.PERIOD;

            this.buttons.add(new MiniCampfireOption(true, ImageMaster.CAMPFIRE_REST_BUTTON,
                    ()->{
                        CardCrawlGame.sound.play("SLEEP_BLANKET");
                        AbstractDungeon.effectList.add(new MiniCampfireSleepEffect(this.parent));

                        for(int i = 0; i < 30; ++i) {
                            AbstractDungeon.topLevelEffects.add(new CampfireSleepScreenCoverEffect());
                        }

                    }, new RestOption(true))
                    .label(RestOption.TEXT[0]).description(description).unusableDescription(RestOption.TEXT[4]));

            this.buttons.add(new MiniCampfireOption(AbstractDungeon.player.masterDeck.getUpgradableCards().size() > 0 && !ModHelper.isModEnabled("Midas"), ImageMaster.CAMPFIRE_SMITH_BUTTON,
                    ()->AbstractDungeon.effectList.add(new MiniCampfireSmithEffect(this.parent)), new SmithOption(true))
                    .label(SmithOption.TEXT[0]).description(SmithOption.TEXT[1]).unusableDescription(SmithOption.TEXT[2]));

            //No adding options with relics. This fire is too teeny.

            AbstractCampfireOption dummy;
            for (MiniCampfireOption option : this.buttons) {
                if (!option.usable) {
                    option.unusable();
                    continue;
                }
                dummy = option.getDummy();
                for (AbstractRelic r : AbstractDungeon.player.relics) {
                    if (!r.canUseCampfireOption(dummy)) {
                        option.unusable();
                        break;
                    }
                }
            }

            for (MiniCampfireOption button : this.buttons) {
                if (button.usable)
                    return;
            }

            //Can't do anything.
            AbstractRoom.waitTimer = 0.0F;
            parent.finish(true);
        }

        private int getHealAmount() {
            int healAmt;

            if (ModHelper.isModEnabled("Night Terrors")) {
                healAmt = AbstractDungeon.player.maxHealth;
            } else {
                healAmt = (int)(AbstractDungeon.player.maxHealth * 0.3F);
            }

            if (Settings.isEndless && AbstractDungeon.player.hasBlight("FullBelly")) {
                healAmt /= 2;
            }

            return healAmt;
        }


        public void update() {
            this.updateCharacterPosition();
            this.updateTouchscreen();
            this.updateControllerInput();
            if (!this.scrollBar.update()) {
                this.updateScrolling();
            }

            this.effect.update();
            if (!hidden) {
                this.updateBubbles();
                this.updateFire();

                for (MiniCampfireOption button : this.buttons) {
                    button.update();
                }
            }

            if (this.somethingSelected) {
                this.hideStuffTimer -= Gdx.graphics.getDeltaTime();
                if (this.hideStuffTimer < 0.0F) {
                    hidden = true;
                }
            }

        }

        private void updateTouchscreen() {
            if (Settings.isTouchScreen) {
                this.confirmButton.update();
                if (this.confirmButton.hb.clicked && this.touchOption != null) {
                    this.confirmButton.hb.clicked = false;
                    this.confirmButton.hb.clickStarted = false;
                    this.confirmButton.isDisabled = true;
                    this.confirmButton.hide();
                    this.touchOption.useOption.execute();
                    this.somethingSelected = true;
                    this.touchOption = null;
                } else if (InputHelper.justReleasedClickLeft && this.touchOption != null) {
                    this.touchOption = null;
                    this.confirmButton.isDisabled = true;
                    this.confirmButton.hide();
                }

            }
        }

        private void updateControllerInput() {
            if (Settings.isControllerMode && !AbstractDungeon.player.viewingRelics && !AbstractDungeon.topPanel.selectPotionMode && AbstractDungeon.topPanel.potionUi.isHidden && !this.somethingSelected && !this.buttons.isEmpty()) {
                boolean anyHovered = false;
                int index = 0;

                for (MiniCampfireOption o : this.buttons) {
                    if (o.hb.hovered) {
                        anyHovered = true;
                        break;
                    }
                }

                if (!anyHovered) {
                    CInputHelper.setCursor(this.buttons.get(0).hb);
                } else if (!CInputActionSet.left.isJustPressed() && !CInputActionSet.altLeft.isJustPressed()) {
                    if (!CInputActionSet.right.isJustPressed() && !CInputActionSet.altRight.isJustPressed()) {
                        if ((CInputActionSet.up.isJustPressed() || CInputActionSet.altUp.isJustPressed()) && this.buttons.size() > 2) {
                            if (this.buttons.size() == 5) {
                                index -= 2;
                                if (index < 0) {
                                    index = 4;
                                }
                            } else if (this.buttons.size() == 3) {
                                if (index == 0) {
                                    index = 2;
                                } else if (index == 2) {
                                    index = 0;
                                }
                            } else if (index == 0) {
                                index = 2;
                            } else if (index == 2) {
                                index = 0;
                            } else if (index == 3) {
                                index = 1;
                            } else {
                                index = 3;
                            }

                            CInputHelper.setCursor(this.buttons.get(index).hb);
                        } else if ((CInputActionSet.down.isJustPressed() || CInputActionSet.altDown.isJustPressed()) && this.buttons.size() > 2) {
                            if (this.buttons.size() == 5) {
                                if (index == 4) {
                                    index = 0;
                                } else if (index > 2) {
                                    index = 4;
                                } else {
                                    index += 2;
                                }
                            } else if (this.buttons.size() == 4) {
                                if (index >= 2) {
                                    index -= 2;
                                } else {
                                    index += 2;
                                }
                            } else if (index != 0 && index != 1) {
                                index = 0;
                            } else {
                                index = 2;
                            }

                            CInputHelper.setCursor(this.buttons.get(index).hb);
                        }
                    } else {
                        ++index;
                        if (index > this.buttons.size() - 1) {
                            if (this.buttons.size() == 2) {
                                index = 0;
                            } else if (index == 3) {
                                index = 2;
                            } else {
                                index = 0;
                            }
                        }

                        CInputHelper.setCursor(this.buttons.get(index).hb);
                    }
                } else {
                    --index;
                    if (index < 0) {
                        if (this.buttons.size() == 2) {
                            index = 1;
                        } else {
                            index = 0;
                        }
                    } else if (index == 1) {
                        if (this.buttons.size() == 4) {
                            index = 3;
                        } else {
                            index = 2;
                        }
                    }

                    CInputHelper.setCursor(this.buttons.get(index).hb);
                }
            }
        }

        private void updateScrolling() {
            int y = InputHelper.mY;
            if (!this.grabbedScreen) {
                if (InputHelper.scrolledDown) {
                    this.targetY += Settings.SCROLL_SPEED;
                } else if (InputHelper.scrolledUp) {
                    this.targetY -= Settings.SCROLL_SPEED;
                }

                if (InputHelper.justClickedLeft) {
                    this.grabbedScreen = true;
                    this.grabStartY = (float)y - this.targetY;
                }
            } else if (InputHelper.isMouseDown) {
                this.targetY = (float)y - this.grabStartY;
            } else {
                this.grabbedScreen = false;
            }

            this.scrollY = MathHelper.scrollSnapLerpSpeed(this.scrollY, this.targetY);
            this.resetScrolling();
            this.updateBarPosition();
        }

        private void resetScrolling() {
            if (this.targetY < this.scrollLowerBound) {
                this.targetY = MathHelper.scrollSnapLerpSpeed(this.targetY, this.scrollLowerBound);
            } else if (this.targetY > this.scrollUpperBound) {
                this.targetY = MathHelper.scrollSnapLerpSpeed(this.targetY, this.scrollUpperBound);
            }
        }

        private void updateCharacterPosition() {
            this.charAnimTimer -= Gdx.graphics.getDeltaTime();
            if (this.charAnimTimer < 0.0F) {
                this.charAnimTimer = 0.0F;
            }

            AbstractDungeon.player.animX = Interpolation.exp10In.apply(0.0F, -300.0F * Settings.scale, this.charAnimTimer / 2.0F);
        }

        private void updateBubbles() {
            if (this.bubbles.size() < this.bubbleAmt) {
                int s = this.bubbleAmt - this.bubbles.size();

                for(int i = 0; i < s; ++i) {
                    this.bubbles.add(new CampfireBubbleEffect(this.bubbleAmt == 60)); //TODO: Make this smaller too.
                }
            }

            Iterator<CampfireBubbleEffect> i = this.bubbles.iterator();

            while(i.hasNext()) {
                CampfireBubbleEffect bubble = i.next();
                bubble.update();
                if (bubble.isDone) {
                    i.remove();
                }
            }
        }

        private void updateFire() {
            this.fireTimer -= Gdx.graphics.getDeltaTime();
            while (this.fireTimer < 0.0F) {
                this.fireTimer += FIRE_INTERVAL;
                //TODO: Make the fire smaller.
                if (AbstractDungeon.id.equals(TheEnding.ID)) {
                    this.flameEffects.add(new CampfireEndingBurningEffect());
                    this.flameEffects.add(new CampfireEndingBurningEffect());
                    this.flameEffects.add(new CampfireEndingBurningEffect());
                    this.flameEffects.add(new CampfireEndingBurningEffect());
                } else {
                    this.flameEffects.add(new CampfireBurningEffect());
                    this.flameEffects.add(new CampfireBurningEffect());
                }
            }

            Iterator<AbstractGameEffect> i = this.flameEffects.iterator();

            while (i.hasNext()) {
                AbstractGameEffect fires = i.next();
                fires.update();
                if (fires.isDone) {
                    i.remove();
                }
            }
        }

        public void reopen() { //SEE: CancelButton's update method, reopening on cancel smith
            hidden = false;
            this.hideStuffTimer = 0.5F;
            this.somethingSelected = false;
        }

        public void render(SpriteBatch sb) {
            if (!hidden) {
                this.renderFire(sb);
                AbstractDungeon.player.render(sb);

                for (CampfireBubbleEffect e : this.bubbles) {
                    e.render(sb, 950.0F * Settings.xScale, (float) Settings.HEIGHT / 2.0F + 60.0F * Settings.yScale + this.effect.y / 4.0F);
                }

                FontHelper.renderFontCentered(sb, FontHelper.losePowerFont, this.bubbleMsg, 950.0F * Settings.xScale, (float)Settings.HEIGHT / 2.0F + 310.0F * Settings.scale + this.effect.y / 3.0F, Settings.CREAM_COLOR, 1.2F);
                this.renderCampfireButtons(sb);
                if (this.shouldShowScrollBar()) {
                    this.scrollBar.render(sb);
                }

                if (Settings.isTouchScreen) {
                    this.confirmButton.render(sb);
                }
            }
        }

        private String getCampMessage() {
            //TODO: Modify, maybe add a custom message or two, maybe just remove some
            ArrayList<String> msgs = new ArrayList<>();
            msgs.add(TEXT[0]);
            msgs.add(TEXT[1]);
            msgs.add(TEXT[2]);
            msgs.add(TEXT[3]);
            if (this.buttons.size() > 2) {
                msgs.add(TEXT[4]);
            }

            if (AbstractDungeon.player.currentHealth < AbstractDungeon.player.maxHealth / 2) {
                msgs.add(TEXT[5]);
                msgs.add(TEXT[6]);
            }

            return msgs.get(MathUtils.random(msgs.size() - 1));
        }

        private void renderFire(SpriteBatch sb) {
            for (AbstractGameEffect e : this.flameEffects) {
                e.render(sb);
            }
        }

        private void renderCampfireButtons(SpriteBatch sb) {
            float buttonX;
            float buttonY;
            int maxPossibleStartingIndex = this.buttons.size() + 1 - MAX_BUTTONS_BEFORE_SCROLL;
            int indexToStartAt = Math.max(Math.min((int)((float)(maxPossibleStartingIndex + 1) * MathHelper.percentFromValueBetween(this.scrollLowerBound, this.scrollUpperBound, this.scrollY)), maxPossibleStartingIndex), 0);
            indexToStartAt = MathUtils.ceil((float)(indexToStartAt / 2)) * 2;

            for (MiniCampfireOption button : this.buttons) {
                if (this.buttons.indexOf(button) >= indexToStartAt && this.buttons.indexOf(button) < indexToStartAt + MAX_BUTTONS_BEFORE_SCROLL) {
                    if (this.buttons.indexOf(button) == this.buttons.size() - 1 && (this.buttons.size() - indexToStartAt) % 2 == 1) {
                        buttonX = BUTTON_START_X + BUTTON_SPACING_X / 2.0F;
                    } else if ((this.buttons.indexOf(button) - indexToStartAt) % 2 == 0) {
                        buttonX = BUTTON_START_X;
                    } else {
                        buttonX = BUTTON_START_X + BUTTON_SPACING_X;
                    }

                    if ((this.buttons.indexOf(button) - indexToStartAt) / 2 == 0) {
                        buttonY = BUTTON_START_Y;
                    } else {
                        buttonY = BUTTON_START_Y + BUTTON_SPACING_Y * (float) MathUtils.floor((float) ((this.buttons.indexOf(button) - indexToStartAt) / 2)) + BUTTON_EXTRA_SPACING_Y;
                    }
                } else {
                    buttonX = (float) Settings.WIDTH * 2.0F;
                    buttonY = (float) Settings.HEIGHT * 2.0F;
                }

                button.setPosition(buttonX, buttonY);
                button.render(sb);
            }
        }

        public void scrolledUsingBar(float newPercent) {
            this.scrollY = MathHelper.valueFromPercentBetween(this.scrollLowerBound, this.scrollUpperBound, newPercent);
            this.targetY = this.scrollY;
            this.updateBarPosition();
        }

        private void updateBarPosition() {
            float percent = MathHelper.percentFromValueBetween(this.scrollLowerBound, this.scrollUpperBound, this.scrollY);
            this.scrollBar.parentScrolledToPercent(percent);
        }

        private boolean shouldShowScrollBar() {
            return this.buttons.size() > MAX_BUTTONS_BEFORE_SCROLL;
        }

        
        
        //campfire option stuff
        private static final int W = 256;
        private static final float SHDW_X = 11.0F * Settings.scale;
        private static final float SHDW_Y = -8.0F * Settings.scale;
        private static final float NORM_SCALE = 0.9F * Settings.scale;
        private static final float HOVER_SCALE = Settings.scale;
        private class MiniCampfireOption {
            protected String label;
            protected String description, unusableDescription;
            protected Texture img;
            private Color color;
            private Color descriptionColor;
            private float scale;
            public Hitbox hb;
            public boolean usable;

            private Method useOption;
            private AbstractCampfireOption dummy;

            public MiniCampfireOption(boolean usable, Texture img, Method useOption, AbstractCampfireOption dummy) {
                this.color = Color.WHITE.cpy();
                this.descriptionColor = Settings.CREAM_COLOR.cpy();
                this.scale = NORM_SCALE;
                this.hb = new Hitbox(216.0F * Settings.scale, 140.0F * Settings.scale);
                this.usable = usable;

                this.useOption = useOption;
                this.img = img;
                this.dummy = dummy;
            }
            public MiniCampfireOption description(String s) {
                this.description = s;
                return this;
            }
            public MiniCampfireOption unusableDescription(String s) {
                this.unusableDescription = s;
                return this;
            }
            public MiniCampfireOption label(String s) {
                this.label = s;
                return this;
            }

            public void setPosition(float x, float y) {
                this.hb.move(x, y);
            }

            public AbstractCampfireOption getDummy() {
                return dummy;
            }

            public void unusable() {
                this.usable = false;
                this.description = unusableDescription;
            }

            public void update() {
                this.hb.update();
                boolean canClick = !somethingSelected && this.usable;
                if (this.hb.hovered && canClick) {
                    if (this.hb.justHovered) {
                        CardCrawlGame.sound.play("UI_HOVER");
                    }

                    if (InputHelper.justClickedLeft) {
                        CardCrawlGame.sound.play("UI_CLICK_1");
                        this.hb.clickStarted = true;
                    }

                    if (!this.hb.clickStarted) {
                        this.scale = MathHelper.scaleLerpSnap(this.scale, HOVER_SCALE);
                        this.scale = MathHelper.scaleLerpSnap(this.scale, HOVER_SCALE);
                    } else {
                        this.scale = MathHelper.scaleLerpSnap(this.scale, NORM_SCALE);
                    }
                } else {
                    this.scale = MathHelper.scaleLerpSnap(this.scale, NORM_SCALE);
                }

                if (this.hb.clicked || CInputActionSet.select.isJustPressed() && canClick && this.hb.hovered) {
                    this.hb.clicked = false;
                    if (!Settings.isTouchScreen) {
                        useOption.execute();
                        somethingSelected = true;
                    } else {
                        if (touchOption != this) {
                            touchOption = this;
                            confirmButton.hideInstantly();
                            confirmButton.isDisabled = false;
                            confirmButton.show();
                        }
                    }
                }
            }
            public void render(SpriteBatch sb) {
                float scaler = (this.scale - NORM_SCALE) * 10.0F / Settings.scale;
                sb.setColor(new Color(0.0F, 0.0F, 0.0F, this.color.a / 5.0F));
                sb.draw(this.img, this.hb.cX - 128.0F + SHDW_X, this.hb.cY - 128.0F + SHDW_Y, 128.0F, 128.0F, 256.0F, 256.0F, this.scale, this.scale, 0.0F, 0, 0, W, W, false, false);
                sb.setColor(new Color(1.0F, 0.93F, 0.45F, scaler));
                sb.draw(ImageMaster.CAMPFIRE_HOVER_BUTTON, this.hb.cX - 128.0F, this.hb.cY - 128.0F, 128.0F, 128.0F, 256.0F, 256.0F, this.scale * 1.075F, this.scale * 1.075F, 0.0F, 0, 0, W, W, false, false);
                sb.setColor(this.color);
                if (!this.usable) {
                    ShaderHelper.setShader(sb, ShaderHelper.Shader.GRAYSCALE);
                }

                sb.draw(this.img, this.hb.cX - 128.0F, this.hb.cY - 128.0F, 128.0F, 128.0F, 256.0F, 256.0F, this.scale, this.scale, 0.0F, 0, 0, W, W, false, false);
                if (!this.usable) {
                    ShaderHelper.setShader(sb, ShaderHelper.Shader.DEFAULT);
                }

                if (!this.usable) {
                    FontHelper.renderFontCenteredTopAligned(sb, FontHelper.topPanelInfoFont, this.label, this.hb.cX, this.hb.cY - 60.0F * Settings.scale - 50.0F * Settings.scale * (this.scale / Settings.scale), Color.LIGHT_GRAY);
                } else {
                    FontHelper.renderFontCenteredTopAligned(sb, FontHelper.topPanelInfoFont, this.label, this.hb.cX, this.hb.cY - 60.0F * Settings.scale - 50.0F * Settings.scale * (this.scale / Settings.scale), Settings.GOLD_COLOR);
                }

                this.descriptionColor.a = scaler;
                FontHelper.renderFontCenteredTopAligned(sb, FontHelper.topPanelInfoFont, this.description, 950.0F * Settings.xScale, (float)Settings.HEIGHT / 2.0F + 20.0F * Settings.scale, this.descriptionColor);
                this.hb.render(sb);
            }
        }
    }
}
