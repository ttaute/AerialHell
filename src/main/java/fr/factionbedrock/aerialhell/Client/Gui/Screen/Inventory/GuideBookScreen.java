package fr.factionbedrock.aerialhell.Client.Gui.Screen.Inventory;

import fr.factionbedrock.aerialhell.AerialHell;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.List;

public class GuideBookScreen extends Screen
{
    private static final Identifier BOOK_TEXTURE = Identifier.fromNamespaceAndPath(AerialHell.MODID, "textures/gui/guide_book/guide_book_page.png");

    private record Tab(String name, int color, int page) {}

    private static final List<Tab> TABS_LEFT = List.of(
            new Tab("Mobs",  0xFF4CAF50, 1),
            new Tab("Bosses",  0xFFE53935, 2),
            new Tab("Items", 0xFFFFB300, 3));

    private static final List<Tab> TABS_RIGHT = List.of(
            new Tab("Armors",  0xFF1E88E5, 4),
            new Tab("Tools",    0xFFFF6D00, 5),
            new Tab("Utilities", 0xFF8E24AA, 6));

    //Book position
    private int bookLeft, bookTop;

    //Book dimensions
    private static final int BOOK_TEXTURE_WIDTH = 384;
    private static final int BOOK_TEXTURE_HEIGHT = 192;
    //Tabs dimensions
    private static final int TAB_WIDTH = 18;
    private static final int TAB_HEIGHT = 36;
    private static final int TAB_GAP = 10;

    //State
    private static final int PAGE_SUMMARY_INDEX = 0;
    private int currentPage = PAGE_SUMMARY_INDEX;

    public GuideBookScreen() {super(Component.empty());}

    @Override protected void init()
    {
        super.init();
        this.bookLeft = (this.width  - BOOK_TEXTURE_WIDTH) / 2;
        this.bookTop  = (this.height - BOOK_TEXTURE_HEIGHT) / 2;
    }

    @Override public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick)
    {
        for (int i = 0; i < TABS_LEFT.size(); i++)
        {
            if (isHoveringTab(event.x(), event.y(), i, true))
            {
                this.currentPage = TABS_LEFT.get(i).page();
                return true;
            }
        }
        for (int i = 0; i < TABS_RIGHT.size(); i++)
        {
            if (isHoveringTab(event.x(), event.y(), i, false))
            {
                currentPage = TABS_RIGHT.get(i).page();
                return true;
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    private boolean isHoveringTab(double mouseX, double mouseY, int index, boolean isLeft)
    {
        int[] pos = getTabPos(index, isLeft);
        return mouseX >= pos[0] && mouseX <= pos[0] + TAB_WIDTH && mouseY >= pos[1] && mouseY <= pos[1] + TAB_HEIGHT;
    }

    @Override public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick)
    {
        graphics.blit(RenderPipelines.GUI_TEXTURED, BOOK_TEXTURE, bookLeft, bookTop, 0f, 0f, BOOK_TEXTURE_WIDTH, BOOK_TEXTURE_HEIGHT, BOOK_TEXTURE_WIDTH, BOOK_TEXTURE_HEIGHT);

        for (int i = 0; i < TABS_LEFT.size(); i++) {this.renderTab(graphics, i, true, mouseX, mouseY);}
        for (int i = 0; i < TABS_RIGHT.size(); i++) {this.renderTab(graphics, i, false, mouseX, mouseY);}

        this.renderPageContent(graphics);

        super.extractBackground(graphics, mouseX, mouseY, partialTick);
    }

    private void renderTab(GuiGraphicsExtractor graphics, int index, boolean isLeft, int mouseX, int mouseY)
    {
        int[] pos = getTabPos(index, isLeft);
        int x = pos[0];
        int y = pos[1];
        Tab tab = isLeft ? TABS_LEFT.get(index) : TABS_RIGHT.get(index);
        boolean hovered = isHoveringTab(mouseX, mouseY, index, isLeft);

        int tabWidth = TAB_WIDTH + (hovered ? 4 : 0);
        int xDraw = isLeft ? x - (hovered ? 4 : 0) : x;

        graphics.fill(xDraw, y, xDraw + tabWidth, y + TAB_HEIGHT, tab.color());

        //Border
        graphics.fill(xDraw, y, xDraw + tabWidth, y + 1, 0xFF1A1A1A);
        graphics.fill(xDraw, y + TAB_HEIGHT - 1, xDraw + tabWidth, y + TAB_HEIGHT, 0xFF1A1A1A);
        graphics.fill(xDraw, y, xDraw + 1, y + TAB_HEIGHT, 0xFF1A1A1A);
        graphics.fill(xDraw + tabWidth - 1, y, xDraw + tabWidth, y + TAB_HEIGHT, 0xFF1A1A1A);

        //Hover text
        if (hovered)
        {
            int textX = isLeft ? xDraw - this.font.width(tab.name()) - 5 : xDraw + tabWidth + 3;
            int textY = y + (TAB_HEIGHT - 8) / 2;
            int textWidth = this.font.width(tab.name()) + 6;

            graphics.fill(textX - 3, textY - 2, textX + textWidth, textY + 10, 0xCC000000);
            graphics.text(this.font, Component.literal(tab.name()), textX, textY, 0xFFFFFFFF);
        }
    }

    private int[] getTabPos(int tabIndex, boolean isLeft)
    {
        int totalH = TABS_LEFT.size() * TAB_HEIGHT + (TABS_LEFT.size() - 1) * TAB_GAP;
        int startY = bookTop + (BOOK_TEXTURE_HEIGHT - totalH) / 2;
        int y = startY + tabIndex * (TAB_HEIGHT + TAB_GAP);
        int x = isLeft ? bookLeft - TAB_WIDTH : bookLeft + BOOK_TEXTURE_WIDTH;
        return new int[]{x, y};
    }

    private void renderPageContent(GuiGraphicsExtractor graphics)
    {
        if (this.currentPage == PAGE_SUMMARY_INDEX)
        {
            String summaryTitle = "- Welcome -";
            graphics.text(this.font, Component.literal(summaryTitle), bookLeft + (BOOK_TEXTURE_WIDTH / 2 - this.font.width(summaryTitle)) / 2, bookTop + 20, 0xFF5C3A1E);

            String summaryText = "Click on a tab to start exploring !";
            graphics.text(this.font, Component.literal(summaryText), bookLeft + (BOOK_TEXTURE_WIDTH / 2 - this.font.width(summaryText)) / 2, bookTop + 40, 0xFF7A5C3A);
        }
        else
        {
            Tab currentTab = null;
            for (Tab tab : TABS_LEFT)  if (tab.page() == currentPage) currentTab = tab;
            for (Tab tab : TABS_RIGHT) if (tab.page() == currentPage) currentTab = tab;

            if (currentTab != null)
            {
                String pageTitle = "- " + currentTab.name() + " -";
                graphics.text(this.font, Component.literal(pageTitle), bookLeft + (BOOK_TEXTURE_WIDTH / 2 - this.font.width(pageTitle)) / 2, bookTop + 20, 0xFF5C3A1E);

                String pageText = "WIP";
                graphics.text(this.font, Component.literal(pageText), bookLeft + 20, bookTop + 45, 0xFF7A5C3A);
            }
        }
    }

    @Override public boolean isPauseScreen() {return false;}
}