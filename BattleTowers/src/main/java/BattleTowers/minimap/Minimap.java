package BattleTowers.minimap;

import BattleTowers.towers.BattleTower;
import BattleTowers.util.TextureLoader;
import basemod.Pair;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.*;
import com.megacrit.cardcrawl.helpers.controller.CInputActionSet;
import com.megacrit.cardcrawl.helpers.input.InputHelper;
import com.megacrit.cardcrawl.localization.UIStrings;
import com.megacrit.cardcrawl.map.Legend;
import com.megacrit.cardcrawl.map.LegendItem;
import com.megacrit.cardcrawl.map.MapRoomNode;
import com.megacrit.cardcrawl.random.Random;
import com.megacrit.cardcrawl.vfx.FadeWipeParticle;
import com.megacrit.cardcrawl.vfx.MapCircleEffect;

import java.lang.reflect.Field;
import java.util.*;

import static BattleTowers.BattleTowers.*;

public class Minimap {
    //I Love Numbers
    private static final int IMG_WIDTH = (int)(64.0F * Settings.xScale);
    private static final float MIN_SPACING_X = Settings.isMobile ? IMG_WIDTH * 2.2F : IMG_WIDTH * 2.0F;
    private static final float MAX_SPACING_X = IMG_WIDTH * 3.0F;
    private static final float JITTER_X = Settings.isMobile ? 10.0F * Settings.xScale : 20.0F * Settings.xScale;
    private static final float JITTER_Y = Settings.isMobile ? 6.0F * Settings.xScale : 10.0F * Settings.xScale;
    private static final float BOSS_OFFSET_Y = 400.0f * Settings.scale;

    private static final float TOP_Y = Settings.HEIGHT - (20.0f * Settings.scale);
    private static final float RETICLE_DIST = 20.0F * Settings.scale;

    private static final Texture smallFire = TextureLoader.getTexture(makeUIPath("smallfire.png"));
    private static final Texture smallFireOutline = TextureLoader.getTexture(makeUIPath("smallfireoutline.png"));

    private static final UIStrings uiStrings = CardCrawlGame.languagePack.getUIString(makeID("FightPreview"));

    private static Field legendItemImg;
    static {
        try {
            legendItemImg = LegendItem.class.getDeclaredField("img");
            legendItemImg.setAccessible(true);
        } catch (NoSuchFieldException e) {
            logger.error("Failed to access field \"img\" of LegendItem");
            e.printStackTrace();
        }
    }

    //Map Stuff
    private final Texture mapTexture = TextureLoader.getTexture(makeUIPath("minimap.png"));
    private final Color baseMapColor;

    private final float height; //render height of map img for scaling with resolution
    private float renderY; //base positions
    private float maxScroll; //farthest it can scroll
    private float targetOffsetY, offsetY; //offset for scrolling. Min 0.
    private float grabStartY;
    private boolean grabbedScreen;
    private boolean clicked;
    private float clickTimer;
    private float clickStartX;
    private float clickStartY;
    private float transitionWaitTimer; //For waiting for map circle after clicking

    private float targetAlpha;
    private boolean interactable = false;

    private final Legend legend;
    private List<MinimapNode[]> map = new ArrayList<>();
    private Set<MinimapNode> available = new HashSet<>();
    public MinimapNode current = null, nextNode = null;
    public boolean isDone = false;

    //Tracks currently hovered node; only has an effect during controller control
    private MinimapNode hovered = null;

    public Minimap() {
        baseMapColor = Color.WHITE.cpy();
        baseMapColor.a = 0;
        targetAlpha = 0;

        height = (Settings.WIDTH / 1920.0f) * 1600.0f;
        renderY = TOP_Y - height;

        this.legend = new Legend();
        try {
            for (LegendItem item : legend.items) {
                if (ImageMaster.MAP_NODE_REST.equals(legendItemImg.get(item))) {
                    legendItemImg.set(item, smallFire);
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        this.targetOffsetY = offsetY = 0; //goes positive to scroll down (move map up)
        maxScroll = height - Settings.HEIGHT;
        if (maxScroll < 0)
            maxScroll = 0;

        this.grabStartY = 0;
        this.grabbedScreen = false;
        this.clicked = false;
        this.clickTimer = 0;
    }

    public void loadPathTaken(List<Pair<Integer, Integer>> pathTaken) {
        if (!pathTaken.isEmpty()) {
            available.clear();

            int index = 0;
            Pair<Integer, Integer> pos;
            MinimapNode[] row;
            MinimapNode last = null;
            MinimapEdge connectedEdge;

            for (; index < pathTaken.size() - 1; ++index) {
                pos = pathTaken.get(index);
                if (pos.getValue() >= 0 && pos.getValue() < map.size()) {
                    row = map.get(pos.getValue());
                    if (pos.getKey() >= 0 && pos.getKey() < row.length) {
                        current = row[pos.getKey()];
                        current.taken = true;
                        if (last != null) {
                            connectedEdge = last.getEdgeConnectedTo(current);
                            if (connectedEdge != null)
                                connectedEdge.markAsTaken();
                        }
                        last = current;
                    }
                    else {
                        logger.error("Invalid path index: " + pos.getValue());
                    }
                }
                else {
                    logger.error("Invalid path row: " + pos.getKey());
                }
            }

            pos = pathTaken.get(index); //Where you actually are
            if (pos.getValue() >= 0 && pos.getValue() < map.size()) {
                row = map.get(pos.getValue());
                if (pos.getKey() >= 0 && pos.getKey() < row.length) {
                    current = row[pos.getKey()];
                    current.taken = true;
                    if (last != null) {
                        connectedEdge = last.getEdgeConnectedTo(current);
                        if (connectedEdge != null)
                            connectedEdge.markAsTaken();
                    }
                    nextNode = null;

                    for (MinimapEdge edge : current.edges) {
                        available.add(edge.dst);
                    }

                    CardCrawlGame.music.fadeOutTempBGM();
                    isDone = true;
                }
                else {
                    logger.error("Invalid path index: " + pos.getValue());
                }
            }
            else {
                logger.error("Invalid path row: " + pos.getKey());
            }
        }
    }

    public void update() {
        if (nextNode != null) {
            this.transitionWaitTimer -= Gdx.graphics.getDeltaTime();
            if (this.transitionWaitTimer <= 0.0F) {
                if (current != null) {
                    MinimapEdge connectedEdge = current.getEdgeConnectedTo(nextNode);
                    if (connectedEdge != null) {
                        connectedEdge.markAsTaken();
                    }
                }
                current = nextNode;

                nextNode = null;
                transitionWaitTimer = 0;

                available.clear();
                for (MinimapEdge edge : current.edges) {
                    available.add(edge.dst);
                }

                CardCrawlGame.music.fadeOutTempBGM();
                isDone = true;
            }
        }
        offsetY = MathUtils.lerp(offsetY, this.targetOffsetY, Gdx.graphics.getDeltaTime() * 12.0F);

        legend.update(baseMapColor.a, interactable);
        baseMapColor.a = MathHelper.fadeLerpSnap(baseMapColor.a, targetAlpha);

        //Update boss hb if that's something that is done

        //Boss click logic
        //Boss hover color

        hovered = null;
        for (MinimapNode[] row : map) {
            for (MinimapNode n : row) {
                if (n.update()) //Checks if visible
                    hovered = n;
            }
        }
        updateReticle();

        if (interactable && !AbstractDungeon.isScreenUp) {
            updateYOffset();

            updateMouse();
            updateControllerInput();
        }
        else {
            this.clicked = false;
            this.clickTimer = 0.0f;
        }
    }
    private void updateYOffset() {
        if (this.grabbedScreen) {
            if (InputHelper.isMouseDown) {
                this.targetOffsetY = (float)InputHelper.mY - this.grabStartY;
            } else {
                this.grabbedScreen = false;
            }
        } else {
            if (InputHelper.scrolledDown) {
                this.targetOffsetY += Settings.MAP_SCROLL_SPEED;
            } else if (InputHelper.scrolledUp) {
                this.targetOffsetY -= Settings.MAP_SCROLL_SPEED;
            }

            if (InputHelper.justClickedLeft) {
                this.grabbedScreen = true;
                this.grabStartY = (float)InputHelper.mY - this.targetOffsetY;
            }
        }

        limitScrolling();
    }
    private void limitScrolling() {
        if (this.targetOffsetY < 0) {
            this.targetOffsetY = MathHelper.scrollSnapLerpSpeed(this.targetOffsetY, 0);
        } else if (this.targetOffsetY > maxScroll) {
            this.targetOffsetY = MathHelper.scrollSnapLerpSpeed(this.targetOffsetY, maxScroll);
        }
    }
    private void updateMouse() {
        if (this.clicked) {
            this.clicked = false;
        }

        if (InputHelper.justReleasedClickLeft && this.clickTimer < 0.4F && Vector2.dst(this.clickStartX, this.clickStartY, (float)InputHelper.mX, (float)InputHelper.mY) < Settings.CLICK_DIST_THRESHOLD) {
            this.clicked = true;
        }

        if (InputHelper.justClickedLeft || CInputActionSet.select.isJustPressed() && AbstractDungeon.topPanel.potionUi.isHidden && !AbstractDungeon.topPanel.selectPotionMode) {
            this.clickTimer = 0.0F;
            this.clickStartX = (float)InputHelper.mX;
            this.clickStartY = (float)InputHelper.mY;
        } else if (InputHelper.isMouseDown) {
            this.clickTimer += Gdx.graphics.getDeltaTime();
        }

        if (CInputActionSet.select.isJustPressed() && this.clickTimer < 0.4F && !AbstractDungeon.player.viewingRelics) {
            this.clicked = true;
            this.clickTimer = 1.0f;
        }
    }
    private void updateControllerInput() {
        if (Settings.isControllerMode && !AbstractDungeon.topPanel.selectPotionMode && AbstractDungeon.topPanel.potionUi.isHidden && !legend.isLegendHighlighted && !AbstractDungeon.player.viewingRelics) {
            if (!CInputActionSet.down.isJustPressed() && !CInputActionSet.altDown.isJustPressed()) {
                if (!CInputActionSet.up.isJustPressed() && !CInputActionSet.altUp.isJustPressed()) {
                    if (CInputActionSet.left.isJustPressed() || CInputActionSet.right.isJustPressed() || CInputActionSet.altRight.isJustPressed() || CInputActionSet.altLeft.isJustPressed()) {
                        if (Gdx.input.getY() > Settings.HEIGHT * 0.85F) {
                            this.targetOffsetY += Settings.SCROLL_SPEED * 2.0F;
                        } else if ((float)Gdx.input.getY() < (float)Settings.HEIGHT * 0.15F) {
                            this.targetOffsetY -= Settings.SCROLL_SPEED * 2.0F;
                        }

                        if (this.targetOffsetY > maxScroll) {
                            this.targetOffsetY = maxScroll;
                        } else if (this.targetOffsetY < 0) {
                            this.targetOffsetY = 0;
                        }
                    }

                    if (hovered == null && !map.isEmpty()) {
                        //set to center node of current available nodes
                    }
                    //TODO TODO TODO
                    /*
                    ArrayList<MapRoomNode> nodes = new ArrayList();// 182
                    Iterator var2;
                    MapRoomNode n;
                    if (!AbstractDungeon.firstRoomChosen) {// 183
                        var2 = this.visibleMapNodes.iterator();// 184

                        while(var2.hasNext()) {
                            n = (MapRoomNode)var2.next();
                            if (n.y == 0) {// 185
                                nodes.add(n);// 186
                            }
                        }
                    } else {
                        var2 = this.visibleMapNodes.iterator();// 190

                        label116:
                        while(true) {
                            boolean flightMatters;
                            do {
                                if (!var2.hasNext()) {
                                    break label116;
                                }

                                n = (MapRoomNode)var2.next();
                                flightMatters = AbstractDungeon.player.hasRelic("WingedGreaves") || ModHelper.isModEnabled("Flight");// 191
                            } while(!AbstractDungeon.currMapNode.isConnectedTo(n) && (!flightMatters || !AbstractDungeon.currMapNode.wingedIsConnectedTo(n)));// 193 194

                            nodes.add(n);// 195
                        }
                    }

                    boolean anyHovered = false;// 200
                    int index = 0;// 201

                    for(Iterator var8 = nodes.iterator(); var8.hasNext(); ++index) {// 202 207
                        MapRoomNode n = (MapRoomNode)var8.next();
                        if (n.hb.hovered) {// 203
                            anyHovered = true;// 204
                            break;// 205
                        }
                    }

                    if (!anyHovered && this.mapNodeHb == null && !nodes.isEmpty()) {// 211
                        Gdx.input.setCursorPosition((int)((MapRoomNode)nodes.get(nodes.size() / 2)).hb.cX, Settings.HEIGHT - (int)((MapRoomNode)nodes.get(nodes.size() / 2)).hb.cY);// 212 213 214
                        this.mapNodeHb = ((MapRoomNode)nodes.get(nodes.size() / 2)).hb;// 215
                    } else if (!anyHovered && nodes.isEmpty()) {// 218
                        Gdx.input.setCursorPosition((int)AbstractDungeon.dungeonMapScreen.map.bossHb.cX, Settings.HEIGHT - (int)AbstractDungeon.dungeonMapScreen.map.bossHb.cY);// 219
                        this.mapNodeHb = null;// 222
                    } else if (!CInputActionSet.left.isJustPressed() && !CInputActionSet.altLeft.isJustPressed()) {// 226
                        if (CInputActionSet.right.isJustPressed() || CInputActionSet.altRight.isJustPressed()) {// 236
                            ++index;// 237
                            if (index > nodes.size() - 1) {// 238
                                index = 0;// 239
                            }

                            Gdx.input.setCursorPosition((int)((MapRoomNode)nodes.get(index)).hb.cX, Settings.HEIGHT - (int)((MapRoomNode)nodes.get(index)).hb.cY);// 241 242 243
                            this.mapNodeHb = ((MapRoomNode)nodes.get(index)).hb;// 244
                        }
                    } else {
                        --index;// 227
                        if (index < 0) {// 228
                            index = nodes.size() - 1;// 229
                        }

                        Gdx.input.setCursorPosition((int)((MapRoomNode)nodes.get(index)).hb.cX, Settings.HEIGHT - (int)((MapRoomNode)nodes.get(index)).hb.cY);// 231 232 233
                        this.mapNodeHb = ((MapRoomNode)nodes.get(index)).hb;// 235
                    }*/

                } else {
                    this.targetOffsetY -= Settings.SCROLL_SPEED * 4.0F;
                }
            } else {
                this.targetOffsetY += Settings.SCROLL_SPEED * 4.0F;
            }
        }
    }

    public void render(SpriteBatch sb) {
        sb.setColor(baseMapColor);
        sb.draw(mapTexture, 0, renderY + offsetY, Settings.WIDTH, height);
        this.legend.render(sb);

        for (MinimapNode[] row : map) {
            for (MinimapNode n : row) {
                n.render(sb);
            }
        }
        //render boss icon

        renderControllerUI(sb);
    }

    private void renderControllerUI(SpriteBatch sb) {
        if (Settings.isControllerMode) {
            if (this.hovered != null) {
                this.renderReticle(sb, hovered.hb);
            }
        }
    }
    private final Color reticleShadowColor = Color.BLACK.cpy();
    private final Color reticleColor = Color.WHITE.cpy();
    private float reticleAlpha = 0.0f;
    private void updateReticle() {
        if (Settings.isControllerMode) {
            reticleAlpha = Math.min(1.0f, reticleAlpha + Gdx.graphics.getDeltaTime() * 3.0f);
        }
        else {
            reticleAlpha = Math.max(0f, reticleAlpha - Gdx.graphics.getDeltaTime() * 4.0f);
        }
    }
    private void renderReticle(SpriteBatch sb, Hitbox hb) {
        reticleShadowColor.a = reticleAlpha * baseMapColor.a / 4.0f;
        reticleColor.a = reticleAlpha * baseMapColor.a;
        sb.setColor(reticleShadowColor);
        this.renderReticleShadow(sb, -hb.width / 2.0F - RETICLE_DIST, hb.height / 2.0F + RETICLE_DIST, hb, false, false);
        this.renderReticleShadow(sb, hb.width / 2.0F + RETICLE_DIST, hb.height / 2.0F + RETICLE_DIST, hb, true, false);
        this.renderReticleShadow(sb, -hb.width / 2.0F - RETICLE_DIST, -hb.height / 2.0F - RETICLE_DIST, hb, false, true);
        this.renderReticleShadow(sb, hb.width / 2.0F + RETICLE_DIST, -hb.height / 2.0F - RETICLE_DIST, hb, true, true);
        sb.setColor(reticleColor);
        this.renderReticleCorner(sb, -hb.width / 2.0F - RETICLE_DIST, hb.height / 2.0F + RETICLE_DIST, hb, false, false);
        this.renderReticleCorner(sb, hb.width / 2.0F + RETICLE_DIST, hb.height / 2.0F + RETICLE_DIST, hb, true, false);
        this.renderReticleCorner(sb, -hb.width / 2.0F - RETICLE_DIST, -hb.height / 2.0F - RETICLE_DIST, hb, false, true);
        this.renderReticleCorner(sb, hb.width / 2.0F + RETICLE_DIST, -hb.height / 2.0F - RETICLE_DIST, hb, true, true);
    }
    private void renderReticleShadow(SpriteBatch sb, float x, float y, Hitbox hb, boolean flipX, boolean flipY) {
        sb.draw(ImageMaster.RETICLE_CORNER, hb.cX + x - 18.0F + 4.0F * Settings.scale, hb.cY + y - 18.0F - 4.0F * Settings.scale, 18.0F, 18.0F, 36.0F, 36.0F, Settings.scale, Settings.scale, 0.0F, 0, 0, 36, 36, flipX, flipY);
    }
    private void renderReticleCorner(SpriteBatch sb, float x, float y, Hitbox hb, boolean flipX, boolean flipY) {
        sb.draw(ImageMaster.RETICLE_CORNER, hb.cX + x - 18.0F, hb.cY + y - 18.0F, 18.0F, 18.0F, 36.0F, 36.0F, Settings.scale, Settings.scale, 0.0F, 0, 0, 36, 36, flipX, flipY);
    }

    public void show() {
        targetAlpha = 1;
        if (MathUtils.randomBoolean()) {
            CardCrawlGame.sound.play("MAP_OPEN", 0.1F);
        } else {
            CardCrawlGame.sound.play("MAP_OPEN_2", 0.1F);
        }
        interactable = true;

        this.clicked = false;
        this.clickTimer = 999.0F;
        this.grabbedScreen = false;

        this.legend.isLegendHighlighted = false;
        hovered = null;
        isDone = false;
    }
    public void hideInstantly() {
        this.targetAlpha = 0;
        this.baseMapColor.a = 0;
        this.legend.c.a = 0;
        this.legend.isLegendHighlighted = false;
        this.clicked = false;
        this.clickTimer = 999.0F;
        hovered = null;
        isDone = false;
        interactable = false;
    }

    public void generate(BattleTower tower, Random towerRng) {
        BattleTower.TowerLayout layout = tower.getLayout();
        BattleTower.TowerContents contents = tower.getContents();
        available.clear();
        map.clear();

        current = null;

        BattleTower.Node[] rowStructure;
        MinimapNode[] row;
        float[] x;
        float bossY = TOP_Y - BOSS_OFFSET_Y;
        float y = bossY - (Settings.MAP_DST_Y * (1 + layout.getRows().size()));
        offsetY = targetOffsetY = Math.max(0, Math.min(maxScroll, -y + 150.0f));

        int rowIndex = 0;
        for (List<BattleTower.Node[]> layoutRow : layout.getRows()) {
            rowStructure = layoutRow.get(0);
            row = new MinimapNode[rowStructure.length];
            x = generatePositions(rowStructure.length);

            for (int i = 0; i < row.length; ++i) {
                row[i] = new MinimapNode(rowStructure[i].type, x[i], y + MathUtils.random(-JITTER_Y, JITTER_Y), i, rowIndex);
                switch (row[i].getType()) {
                    case EVENT:
                        row[i].setKey(contents.getEvent(towerRng));
                        break;
                    case MONSTER:
                        row[i].setKey(contents.getNormalEncounter(towerRng));
                        break;
                    case ELITE:
                        row[i].setKey(contents.getEliteEncounter(towerRng));
                        break;
                }
            }

            map.add(row);

            ++rowIndex;
            y += Settings.MAP_DST_Y;
        }

        MinimapNode[] targetRow;
        for (int i = 0; i < layout.getRows().size() - 1; ++i) {
            rowStructure = layout.getRows().get(i).get(0);
            row = map.get(i);
            targetRow = map.get(i + 1);

            for (int j = 0; j < row.length; ++j) {
                for (int target : rowStructure[j].connections) {
                    row[j].edges.add(new MinimapEdge(row[j], targetRow[target], false));
                }
            }
        }

        //Boss
        BattleTower.BossInfo boss = contents.getBoss(towerRng);
        row = new MinimapNode[1];
        MinimapNode bossNode = new BossNode(Settings.WIDTH / 2.0f, bossY, 0, rowIndex);
        row[0] = bossNode;
        row[0].setKey(boss.id);
        loadBossImg(boss);
        map.add(row);

        //add edges to connect to boss node
        for (MinimapNode n : map.get(map.size() - 2)) {
            n.edges.add(new MinimapEdge(n, bossNode, true));
        }

        for (MinimapNode n : map.get(0)) {
            available.add(n);
        }
    }
    private float[] generatePositions(int count) {
        float[] positions = new float[count];
        float spacing = MathUtils.random(MIN_SPACING_X, MAX_SPACING_X);
        float x = Settings.WIDTH / 2.0f;
        x -= ((count - 1) / 2.0f) * spacing;

        for (int i = 0; i < positions.length; ++i) {
            positions[i] = x + MathUtils.random(-JITTER_X, JITTER_X);
            x += spacing;
        }

        return positions;
    }

    public class MinimapNode {
        private final Color HIGHLIGHT_COLOR = new Color(0.9F, 0.9F, 0.9F, 1.0F);
        protected final Color AVAILABLE_COLOR = new Color(0.09F, 0.13F, 0.17F, 1.0F);
        protected final Color NOT_TAKEN_COLOR = new Color(0.34F, 0.34F, 0.34F, 1.0F);
        private final Color OUTLINE_COLOR = Color.valueOf("8c8c80ff");

        protected boolean visible;
        private final List<MinimapEdge> edges; //probably need a custom similar class due to relying on indices for positioning
        private final BattleTower.NodeType type;
        protected Texture img;
        protected Texture outline;
        protected float cx;
        protected float cy;
        protected float scale;
        private float angle = MathUtils.random(360.0F);
        private float oscillateTimer;
        protected Color color;

        public final int mapX, mapY;

        protected boolean highlighted = false;
        public boolean taken = false;

        public Hitbox hb;
        private String key;

        public MinimapNode(BattleTower.NodeType type, float x, float y, int mapX, int mapY) {
            this.type = type;
            getImg();

            float hitbox_w = Settings.isMobile ? 114.0F * Settings.scale : 64.0F * Settings.scale; //well, probably not.
            this.hb = new Hitbox(hitbox_w, hitbox_w);
            this.cx = x;
            this.cy = y;
            this.scale = 0.5f;
            this.oscillateTimer = MathUtils.random(0.0F, 6.28F);

            this.mapX = mapX;
            this.mapY = mapY;

            this.visible = false;
            this.color = NOT_TAKEN_COLOR.cpy();

            edges = new ArrayList<>();
        }

        public BattleTower.NodeType getType() {
            return type;
        }

        public boolean update() {
            showPreviewIfHovered();
            highlighted = false;
            this.scale = MathHelper.scaleLerpSnap(this.scale, 0.5F);

            this.hb.move(this.cx, cy + offsetY);
            this.hb.update();

            //Check if visible
            visible = !(AbstractDungeon.screen == AbstractDungeon.CurrentScreen.MAP);
            //eh there's like 6 rows just render all of them unless totally covered

            for (MinimapEdge edge : edges) {
                if (!edge.taken)
                    edge.color = NOT_TAKEN_COLOR;
            }

            if (interactable) {
                if (this.equals(current)) {
                    for (MinimapEdge edge : edges) {
                        edge.color = AVAILABLE_COLOR;
                    }
                }

                if (available.contains(this)) {
                    if (this.hb.hovered) {
                        if (this.hb.justHovered) {
                            playNodeHoveredSound();
                        }

                        if (clicked && interactable) {
                            playNodeSelectedSound();
                            clicked = false;
                            clickTimer = 0;

                            if (Settings.FAST_MODE) {
                                transitionWaitTimer = 0.1F;
                            }
                            else {
                                transitionWaitTimer = 0.5F;
                                AbstractDungeon.topLevelEffects.add(new MapCircleEffect(hb.cX, hb.cY, this.angle));
                                AbstractDungeon.topLevelEffects.add(new FadeWipeParticle());
                            }

                            nextNode = this;
                            interactable = false;
                        }

                        this.highlighted = true;
                    } else {
                        this.color = AVAILABLE_COLOR.cpy();
                    }
                    this.oscillateColor();
                }
                else { //can't go here
                    if (this.hb.hovered && !this.taken) {
                        this.scale = 1.0F;
                        this.color = AVAILABLE_COLOR.cpy();
                    } else {
                        this.color = NOT_TAKEN_COLOR.cpy();
                    }
                }
            }
            else if (this.hb.hovered) { //hi again
                this.scale = 1.0F;
                this.color = AVAILABLE_COLOR.cpy();
            } else {
                this.color = NOT_TAKEN_COLOR.cpy();
            }

            if (this.equals(current)) {
                this.color = AVAILABLE_COLOR.cpy();
                this.scale = MathHelper.scaleLerpSnap(this.scale, 0.5F);
            }

            return hb.hovered;
        }
        private void oscillateColor() {
            if (!this.taken) {
                this.oscillateTimer += Gdx.graphics.getDeltaTime() * 5.0F;
                this.color.a = 0.66F + (MathUtils.cos(this.oscillateTimer) + 1.0F) / 6.0F;
                this.scale = 0.25F + this.color.a;
            } else {
                this.scale = MathHelper.scaleLerpSnap(this.scale, Settings.scale);
            }
        }
        public void render(SpriteBatch sb) {
            if (visible) {
                for (MinimapEdge edge : edges) {
                    edge.render(sb);
                }

                boolean legendHovered = legend.isIconHovered(this.getSymbol());
                if (legendHovered) {
                    this.scale = 0.68F;
                    sb.setColor(Color.LIGHT_GRAY);
                }
                else {
                    sb.setColor(highlighted ? HIGHLIGHT_COLOR : OUTLINE_COLOR);
                }

                if (!Settings.isMobile) {
                    sb.draw(outline, getRenderX(), getRenderY(), 64.0F, 64.0F, 128.0F, 128.0F, this.scale * Settings.scale, this.scale * Settings.scale, 0.0F, 0, 0, 128, 128, false, false);
                } else {
                    sb.draw(outline, getRenderX(), getRenderY(), 64.0F, 64.0F, 128.0F, 128.0F, this.scale * Settings.scale * 2.0F, this.scale * Settings.scale * 2.0F, 0.0F, 0, 0, 128, 128, false, false);
                }

                sb.setColor(legendHovered || taken ? AVAILABLE_COLOR : this.color);

                if (!Settings.isMobile) {
                    sb.draw(img, getRenderX(), getRenderY(), 64.0F, 64.0F, 128.0F, 128.0F, this.scale * Settings.scale, this.scale * Settings.scale, 0.0F, 0, 0, 128, 128, false, false);
                } else {
                    sb.draw(img, getRenderX(), getRenderY(), 64.0F, 64.0F, 128.0F, 128.0F, this.scale * Settings.scale * 2.0F, this.scale * Settings.scale * 2.0F, 0.0F, 0, 0, 128, 128, false, false);
                }

                if (taken) {// || is current node
                    sb.setColor(AVAILABLE_COLOR);
                    if (!Settings.isMobile) {
                        sb.draw(ImageMaster.MAP_CIRCLE_5, getRenderX() - 32.0f, getRenderY() - 32.0f, 96.0F, 96.0F, 192.0F, 192.0F, (this.scale * 0.95F + 0.2F) * Settings.scale, (this.scale * 0.95F + 0.2F) * Settings.scale, this.angle, 0, 0, 192, 192, false, false);
                    } else {
                        sb.draw(ImageMaster.MAP_CIRCLE_5, getRenderX() - 32.0f, getRenderY() - 32.0f, 96.0F, 96.0F, 192.0F, 192.0F, (this.scale * 0.95F + 0.2F) * Settings.scale * 2.0F, (this.scale * 0.95F + 0.2F) * Settings.scale * 2.0F, this.angle, 0, 0, 192, 192, false, false);
                    }
                }

                this.hb.render(sb);
            }
        }

        protected void showPreviewIfHovered() {
            List<BattleTower.NodeType> previewTypes = Arrays.asList(BattleTower.NodeType.MONSTER, BattleTower.NodeType.ELITE, BattleTower.NodeType.BOSS);
            if (this.hb.hovered && previewTypes.contains(this.type)) {
                String fightPreviewText = MonsterHelper.getEncounterName(this.getKey());
                TipHelper.renderGenericTip(this.cx + this.getPreviewTooltipXOffset(), this.cy + offsetY, uiStrings.TEXT[0], fightPreviewText);
            }
        }

        protected float getPreviewTooltipXOffset() {
            return this.img.getWidth() / 4.0f;
        }

        private float getRenderX() {
            return hb.cX - 64.0f;
        }
        private float getRenderY() {
            return hb.cY - 64.0f;
        }

        public boolean hasEdges() {
            return !edges.isEmpty();
        }
        public MinimapEdge getEdgeConnectedTo(MinimapNode nextNode) {
            for (MinimapEdge edge : edges) {
                if (nextNode.equals(edge.dst))
                    return edge;
            }
            return null;
        }

        protected void getImg() {
            switch (type) {
                case MONSTER:
                    img = ImageMaster.MAP_NODE_ENEMY;
                    outline = ImageMaster.MAP_NODE_ENEMY_OUTLINE;
                    break;
                case ELITE:
                    img = ImageMaster.MAP_NODE_ELITE;
                    outline = ImageMaster.MAP_NODE_ELITE_OUTLINE;
                    break;
                case REST:
                    img = smallFire;
                    outline = smallFireOutline;
                    break;
                case SHOP:
                    img = ImageMaster.MAP_NODE_MERCHANT;
                    outline = ImageMaster.MAP_NODE_MERCHANT_OUTLINE;
                    break;
                case EVENT:
                    img = ImageMaster.MAP_NODE_EVENT;
                    outline = ImageMaster.MAP_NODE_EVENT_OUTLINE;
                    break;
            }
        }
        private String getSymbol() {
            switch (type) {
                case MONSTER:
                    return "M";
                case ELITE:
                    return "E";
                case REST:
                    return "R";
                case SHOP:
                    return "$";
                case BOSS:
                    return "B";
                default:
                    return "?";
            }
        }

        public String getKey() {
            return key;
        }
        public void setKey(String key) {
            this.key = key;
        }
    }

    private static final float BOSS_W = Settings.isMobile ? 560.0F * Settings.scale : 512.0F * Settings.scale;
    private static Texture bossImg, bossImgOutline;
    public void loadBossImg(BattleTower.BossInfo boss) {
        if (bossImg != null) {
            bossImg.dispose();
            bossImg = null;
        }
        if (bossImgOutline != null) {
            bossImgOutline.dispose();
            bossImgOutline = null;
        }
        bossImg = boss.loadBossIcon();
        bossImgOutline = boss.loadBossIconOutline();
    }
    private class BossNode extends MinimapNode {
        public BossNode(float x, float y, int mapX, int mapY) {
            super(BattleTower.NodeType.BOSS, x, y, mapX, mapY);

            hb = new Hitbox(400.0F * Settings.scale, 360.0F * Settings.scale);
        }

        public boolean update() {
            showPreviewIfHovered();
            this.scale = Settings.scale;

            this.hb.move(this.cx, cy + offsetY);
            this.hb.update();

            visible = true;
            //no edges

            if (interactable) {
                if (this.hb.hovered || this.equals(current)) {
                    color = AVAILABLE_COLOR.cpy();
                }
                else {
                    this.color.lerp(NOT_TAKEN_COLOR, Gdx.graphics.getDeltaTime() * 8.0F);
                }

                if (available.contains(this) && this.hb.hovered && clicked) {
                    clicked = false;
                    clickTimer = 0;

                    transitionWaitTimer = 0.1F;
                    nextNode = this;
                    interactable = false;
                }
            }
            else if (this.hb.hovered || this.equals(current)) {
                color = AVAILABLE_COLOR.cpy();
            }
            else {
                this.color.lerp(NOT_TAKEN_COLOR, Gdx.graphics.getDeltaTime() * 8.0F);
            }

            return hb.hovered;
        }

        private final Color outlineColor = Color.WHITE.cpy();
        @Override
        public void render(SpriteBatch sb) {
            if (bossImg != null &&  bossImgOutline != null) {
                outlineColor.a = this.color.a;
                sb.draw(bossImgOutline, hb.cX - BOSS_W / 2.0F, hb.cY - BOSS_W / 2.0F, BOSS_W, BOSS_W);

                sb.setColor(this.color);
                sb.draw(bossImg, hb.cX - BOSS_W / 2.0F, hb.cY - BOSS_W / 2.0F, BOSS_W, BOSS_W);

                if (!AbstractDungeon.isScreenUp) {// 254
                    this.hb.render(sb);// 255
                }
            }
        }

        @Override
        protected float getPreviewTooltipXOffset() {
            return bossImg.getWidth() / 4.0f;
        }
    }

    private static final float ICON_SRC_RADIUS = 29.0F * Settings.scale;
    private static final float ICON_DST_RADIUS = 20.0F * Settings.scale;
    private static final float SPACING = Settings.isMobile ? 20.0F * Settings.xScale : 17.0F * Settings.xScale;
    private static final Color DISABLED_COLOR = new Color(0.0F, 0.0F, 0.0F, 0.25F);
    private class MinimapEdge {
        public MinimapNode src, dst;

        private final ArrayList<MinimapDot> dots = new ArrayList<>();
        public Color color;
        public boolean taken;

        public MinimapEdge(MinimapNode src, MinimapNode dest, boolean isBoss) {
            this.color = DISABLED_COLOR.cpy();
            this.taken = false;
            this.src = src;
            this.dst = dest;
            
            Vector2 vec2 = (new Vector2(dst.cx, dst.cy)).sub(new Vector2(src.cx, src.cy));
            float length = vec2.len();
            float start = SPACING * MathUtils.random() / 2.0F;
            float tmpRadius = ICON_DST_RADIUS;
            if (isBoss) {
                tmpRadius = 164.0F * Settings.scale;
            }

            for(float i = start + tmpRadius; i < length - ICON_SRC_RADIUS; i += SPACING) {
                vec2.clamp(length - i, length - i);
                if (i != start + tmpRadius && i <= length - ICON_SRC_RADIUS - SPACING) {
                    this.dots.add(new MinimapDot(src.cx + vec2.x, src.cy + vec2.y - 3, (new Vector2(src.cx - dst.cx, src.cy - dst.cy)).nor().angle() + 90.0F, true));
                } else {
                    this.dots.add(new MinimapDot(src.cx + vec2.x, src.cy + vec2.y - 3, (new Vector2(src.cx - dst.cx, src.cy - dst.cy)).nor().angle() + 90.0F, false));
                }
            }
        }

        public void markAsTaken() {
            this.taken = true;
            this.color = MapRoomNode.AVAILABLE_COLOR.cpy();
        }

        public void render(SpriteBatch sb) {
            sb.setColor(this.color);

            for (MinimapDot d : this.dots) {
                d.render(sb);
            }
        }
    }

    private static final int RAW_W = 16;
    private static final float DIST_JITTER = 4.0F * Settings.scale;
    private class MinimapDot {
        private final float x;
        private final float y;
        private final float rotation;

        public MinimapDot(float x, float y, float rotation, boolean jitter) {
            if (jitter) {
                this.x = x + MathUtils.random(-DIST_JITTER, DIST_JITTER);
                this.y = y + MathUtils.random(-DIST_JITTER, DIST_JITTER);
                this.rotation = rotation + MathUtils.random(-20.0F, 20.0F);
            } else {
                this.x = x;
                this.y = y;
                this.rotation = rotation;
            }
        }

        public void render(SpriteBatch sb) {
            sb.draw(ImageMaster.MAP_DOT_1, this.x - 8.0F, this.y - 8.0F + offsetY, 8.0F, 8.0F, 16.0F, 16.0F, Settings.scale, Settings.scale, this.rotation, 0, 0, RAW_W, RAW_W, false, false);
        }
    }

    private static void playNodeHoveredSound() {
        int roll = MathUtils.random(3);
        switch(roll) {
            case 0:
                CardCrawlGame.sound.play("MAP_HOVER_1");
                break;
            case 1:
                CardCrawlGame.sound.play("MAP_HOVER_2");
                break;
            case 2:
                CardCrawlGame.sound.play("MAP_HOVER_3");
                break;
            default:
                CardCrawlGame.sound.play("MAP_HOVER_4");
                break;
        }
    }
    private static void playNodeSelectedSound() {
        int roll = MathUtils.random(3);
        switch(roll) {
            case 0:
                CardCrawlGame.sound.play("MAP_SELECT_1");
                break;
            case 1:
                CardCrawlGame.sound.play("MAP_SELECT_2");
                break;
            case 2:
                CardCrawlGame.sound.play("MAP_SELECT_3");
                break;
            default:
                CardCrawlGame.sound.play("MAP_SELECT_4");
                break;
        }
    }
}
