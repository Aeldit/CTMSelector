package fr.aeldit.ctms.gui;

import com.terraformersmc.modmenu.gui.widget.LegacyTexturedButtonWidget;
import fr.aeldit.ctms.gui.widgets.PacksListWidget;
import fr.aeldit.ctms.textures.entryTypes.CTMPack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

import java.io.File;
import java.util.Comparator;
import java.util.Objects;

import static fr.aeldit.ctms.Utils.*;

@Environment(EnvType.CLIENT)
public class CTMSScreen extends Screen
{
    private final Screen parent;

    public CTMSScreen(Screen parent)
    {
        super(Text.translatable("ctms.screen.title"));
        this.parent = parent;
    }

    @Override
    public void close()
    {
        Objects.requireNonNull(client).setScreen(parent);
    }

    @Override
    public void render(DrawContext DrawContext, int mouseX, int mouseY, float delta)
    {
        super.render(DrawContext, mouseX, mouseY, delta);
        DrawContext.drawCenteredTextWithShadow(textRenderer, title, width / 2, 12, 0xffffff);
    }

    //? if <1.20.6 {
    /*@Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta)
    {
        super.renderBackgroundTexture(context);
    }
    *///?}

    @Override
    protected void init()
    {
        TEXTURES_HANDLING.load();
        PacksListWidget list = new PacksListWidget(
                //? if <1.20.4 {
                /*client, width, height - 64, 28, 32, this
                 *///?} else {
                client, width, height - 64, 32, 32, this
                //?}
        );
        addDrawableChild(list);

        CTM_PACKS.getAvailableCTMPacks().stream()
                 .sorted(Comparator.comparing(CTMPack::getName))
                 .forEachOrdered(list::add);

        ButtonWidget optionsButton = new LegacyTexturedButtonWidget(
                width / 2 + 160, height - 28, 20, 20, 0, 0, 20,
                //? if <1.21 {
                new Identifier(CTMS_MODID, "textures/gui/options.png"),
                //?} else {
                /*Identifier.of(CTMS_MODID, "textures/gui/options.png"),
                 *///?}
                20, 40,
                button -> MinecraftClient.getInstance().setScreen(this),
                Text.empty()
        );
        addDrawableChild(optionsButton);

        addDrawableChild(
                ButtonWidget.builder(
                                    Text.translatable("ctms.screen.openResourcePacksFolder"),
                                    button -> Util.getOperatingSystem().open(new File(
                                            FabricLoader.getInstance().getGameDir().toFile(), "resourcepacks")
                                    )
                            )
                            .dimensions(width / 2 - 154, height - 28, 150, 20)
                            .build()
        );

        addDrawableChild(
                ButtonWidget.builder(ScreenTexts.DONE, button -> close())
                            .dimensions(width / 2 + 4, height - 28, 150, 20)
                            .build()
        );

        ButtonWidget reloadButton = new LegacyTexturedButtonWidget(
                width / 2 - 180, height - 28, 20, 20, 0, 0, 20,
                //? if <1.21 {
                new Identifier(CTMS_MODID, "textures/gui/reload.png"),
                //?} else {
                /*Identifier.of(CTMS_MODID, "textures/gui/reload.png"),
                 *///?}
                20, 40,
                button -> {
                    TEXTURES_HANDLING.load();
                    MinecraftClient.getInstance().setScreen(this);
                },
                Text.empty()
        );
        reloadButton.setTooltip(Tooltip.of(Text.translatable("ctms.screen.reload.tooltip")));
        addDrawableChild(reloadButton);
    }
}
