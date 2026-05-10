package fr.factionbedrock.aerialhell.Client.Gui.Screen.Inventory;

import fr.factionbedrock.aerialhell.AerialHell;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.stream.Stream;

public class GuideBookScreen extends Screen
{
    private static final Identifier BOOK_TEXTURE = Identifier.fromNamespaceAndPath(AerialHell.MODID, "textures/gui/guide_book/guide_book_page.png");
    private static final Identifier NAVIGATION_ARROW_PREVIOUS_PAGE = Identifier.fromNamespaceAndPath(AerialHell.MODID, "textures/gui/guide_book/navigation_arrow_previous_page.png");
    private static final Identifier NAVIGATION_ARROW_PREVIOUS_PAGE_HOVERED = Identifier.fromNamespaceAndPath(AerialHell.MODID, "textures/gui/guide_book/navigation_arrow_previous_page_hovered.png");
    private static final Identifier NAVIGATION_ARROW_NEXT_PAGE = Identifier.fromNamespaceAndPath(AerialHell.MODID, "textures/gui/guide_book/navigation_arrow_next_page.png");
    private static final Identifier NAVIGATION_ARROW_NEXT_PAGE_HOVERED = Identifier.fromNamespaceAndPath(AerialHell.MODID, "textures/gui/guide_book/navigation_arrow_next_page_hovered.png");

    private record Tab(String name, int color, int page) {}

    private static final Tab SUMMARY_TAB = new Tab("Welcome", 0x0, 0);

    private static final List<Tab> TABS_LEFT = List.of(
            new Tab("Mobs",  0xFF4CAF50, 1),
            new Tab("Bosses",  0xFFE53935, 2),
            new Tab("Items", 0xFFFFB300, 3));

    private static final List<Tab> TABS_RIGHT = List.of(
            new Tab("Armors",  0xFF1E88E5, 4),
            new Tab("Tools",    0xFFFF6D00, 5),
            new Tab("Utilities", 0xFF8E24AA, 6));

    private static final List<Tab> ALL_TABS = Stream.concat(Stream.of(SUMMARY_TAB), Stream.concat(TABS_LEFT.stream(), TABS_RIGHT.stream())).toList();

    //book position
    private int bookLeft, bookRight, bookTop, bookBottom;
    //navigation arrows position
    private int navigationArrowTop;
    private int navigationArrowBottom;
    private int leftNavigationArrowLeft;
    private int leftNavigationArrowRight;
    private int rightNavigationArrowLeft;
    private int rightNavigationArrowRight;

    //book dimensions
    private static final int BOOK_TEXTURE_WIDTH = 384;
    private static final int BOOK_TEXTURE_HEIGHT = 192;
    //tabs dimensions
    private static final int TAB_WIDTH = 18;
    private static final int TAB_HEIGHT = 36;
    private static final int TAB_GAP = 10;
    //navigation arrow dimension
    private static final int NAVIGATION_ARROW_SIZE = 20;

    //state
    private static final int PAGE_SUMMARY_INDEX = 0;
    private int currentPage = PAGE_SUMMARY_INDEX;

    public GuideBookScreen() {super(Component.empty());}

    @Override protected void init()
    {
        super.init();
        this.bookLeft = (this.width  - BOOK_TEXTURE_WIDTH) / 2;
        this.bookTop  = (this.height - BOOK_TEXTURE_HEIGHT) / 2;
        this.bookRight = this.bookLeft + BOOK_TEXTURE_WIDTH;
        this.bookBottom = this.bookTop + BOOK_TEXTURE_HEIGHT;
        this.navigationArrowBottom = this.bookBottom - 5;
        this.navigationArrowTop = this.navigationArrowBottom - NAVIGATION_ARROW_SIZE;
        this.leftNavigationArrowLeft = this.bookLeft + 5;
        this.leftNavigationArrowRight = this.leftNavigationArrowLeft + NAVIGATION_ARROW_SIZE;
        this.rightNavigationArrowRight = this.bookRight - 5;
        this.rightNavigationArrowLeft = this.rightNavigationArrowRight - NAVIGATION_ARROW_SIZE;
    }

    @Override public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick)
    {
        //navigation arrows
        if (this.isHoveringPrevArrow(event.x(), event.y()))
        {
            this.navigateToPreviousPage();
            return true;
        }

        if (this.isHoveringNextArrow(event.x(), event.y()))
        {
            this.navigateToNextPage();
            return true;
        }

        //tabs
        for (int i = 0; i < TABS_LEFT.size(); i++)
        {
            if (this.isHoveringTab(event.x(), event.y(), i, true))
            {
                this.navigateToPage(TABS_LEFT.get(i));
                return true;
            }
        }
        for (int i = 0; i < TABS_RIGHT.size(); i++)
        {
            if (this.isHoveringTab(event.x(), event.y(), i, false))
            {
                this.navigateToPage(TABS_RIGHT.get(i));
                return true;
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    private boolean isHoveringPrevArrow(double mouseX, double mouseY)
    {
        return mouseX >= this.leftNavigationArrowLeft && mouseX <= this.leftNavigationArrowRight  && mouseY >= this.navigationArrowTop && mouseY <= this.navigationArrowBottom;
    }

    private boolean isHoveringNextArrow(double mouseX, double mouseY)
    {
        return mouseX >= this.rightNavigationArrowLeft && mouseX <= this.rightNavigationArrowRight && mouseY >= this.navigationArrowTop && mouseY <= this.navigationArrowBottom;
    }

    private boolean isHoveringTab(double mouseX, double mouseY, int index, boolean isLeft)
    {
        int[] pos = getTabPos(index, isLeft);
        return mouseX >= pos[0] && mouseX <= pos[0] + TAB_WIDTH && mouseY >= pos[1] && mouseY <= pos[1] + TAB_HEIGHT;
    }

    @Override public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick)
    {
        graphics.blit(RenderPipelines.GUI_TEXTURED, BOOK_TEXTURE, this.bookLeft, this.bookTop, 0f, 0f, BOOK_TEXTURE_WIDTH, BOOK_TEXTURE_HEIGHT, BOOK_TEXTURE_WIDTH, BOOK_TEXTURE_HEIGHT);

        for (int i = 0; i < TABS_LEFT.size(); i++) {this.renderTab(graphics, i, true, mouseX, mouseY);}
        for (int i = 0; i < TABS_RIGHT.size(); i++) {this.renderTab(graphics, i, false, mouseX, mouseY);}

        this.renderPageContent(graphics);
        this.renderNavigationButtons(graphics, mouseX, mouseY);

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

        //border
        graphics.fill(xDraw, y, xDraw + tabWidth, y + 1, 0xFF1A1A1A);
        graphics.fill(xDraw, y + TAB_HEIGHT - 1, xDraw + tabWidth, y + TAB_HEIGHT, 0xFF1A1A1A);
        graphics.fill(xDraw, y, xDraw + 1, y + TAB_HEIGHT, 0xFF1A1A1A);
        graphics.fill(xDraw + tabWidth - 1, y, xDraw + tabWidth, y + TAB_HEIGHT, 0xFF1A1A1A);

        //hover text
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
        int x = isLeft ? bookLeft - TAB_WIDTH : bookRight;
        return new int[]{x, y};
    }

    private void renderPageContent(GuiGraphicsExtractor graphics)
    {
        Tab currentTab = null;
        for (Tab tab : ALL_TABS)  if (tab.page() == this.currentPage) currentTab = tab;

        if (currentTab != null)
        {
            String pageTitle = "- " + currentTab.name() + " -";
            graphics.text(this.font, Component.literal(pageTitle), bookLeft + (BOOK_TEXTURE_WIDTH / 2 - this.font.width(pageTitle)) / 2, bookTop + 20, 0xFF5C3A1E);

            String pageText = this.currentPage == 0 ? "Click on a tab to start exploring !" : "WIP";
            graphics.text(this.font, Component.literal(pageText), bookLeft + 20, bookTop + 45, 0xFF7A5C3A);
        }
    }

    private void renderNavigationButtons(GuiGraphicsExtractor graphics, int mouseX, int mouseY)
    {
        Identifier previousArrowTexture = this.isHoveringPrevArrow(mouseX, mouseY) ? NAVIGATION_ARROW_PREVIOUS_PAGE_HOVERED : NAVIGATION_ARROW_PREVIOUS_PAGE;
        Identifier nextArrowTexture = this.isHoveringNextArrow(mouseX, mouseY) ? NAVIGATION_ARROW_NEXT_PAGE_HOVERED : NAVIGATION_ARROW_NEXT_PAGE;

        //previous page arrow
        graphics.blit(RenderPipelines.GUI_TEXTURED, previousArrowTexture, this.leftNavigationArrowLeft, this.navigationArrowTop, 0f, 0f, NAVIGATION_ARROW_SIZE, NAVIGATION_ARROW_SIZE, NAVIGATION_ARROW_SIZE, NAVIGATION_ARROW_SIZE);

        //next page arrow
        graphics.blit(RenderPipelines.GUI_TEXTURED, nextArrowTexture, this.rightNavigationArrowLeft, this.navigationArrowTop, 0f, 0f, NAVIGATION_ARROW_SIZE, NAVIGATION_ARROW_SIZE, NAVIGATION_ARROW_SIZE, NAVIGATION_ARROW_SIZE);
    }

    private void navigateToPage(Tab tab)
    {
        this.currentPage = tab.page();
    }

    private void navigateToPreviousPage()
    {
        int nextIndex = getCurrentIndex() - 1;
        if (nextIndex >= 0 && nextIndex < ALL_TABS.size()) {this.navigateToPage(ALL_TABS.get(nextIndex));}
    }

    private void navigateToNextPage()
    {
        int nextIndex = getCurrentIndex() + 1;
        if (nextIndex >= 0 && nextIndex < ALL_TABS.size()) {this.navigateToPage(ALL_TABS.get(nextIndex));}
    }

    private int getCurrentIndex()
    {
        for (int i = 0; i < ALL_TABS.size(); i++)
        {
            if (ALL_TABS.get(i).page() == this.currentPage) {return i;}
        }
        return -1;
    }

    @Override public boolean isPauseScreen() {return false;}
}