package com.rift.client.client;

import com.rift.client.client.modules.Module;
import com.rift.client.client.modules.ModuleManager;
import com.rift.client.client.ui.RiftModuleCard;
import com.rift.client.client.ui.RiftSidebar;
import com.rift.client.client.ui.RiftTheme;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import java.util.ArrayList;
import java.util.List;

public class RiftScreen extends Screen {
    private int page = 0;
    private Module settingsModule;
    private static final int W = 720, H = 420;

    public RiftScreen() { super(Component.literal("RIFT")); }

    private List<Module> visibleModules() {
        List<Module> result = new ArrayList<>();
        for (Module m : ModuleManager.getModules()) {
            if (page == 6 ? m.isPinned() : m.getCategory().equalsIgnoreCase(RiftSidebar.CATEGORIES[page])) result.add(m);
        }
        return result;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
        super.extractRenderState(g, mouseX, mouseY, delta);
        int x = (width-W)/2, y = (height-H)/2;
        g.fill(0,0,width,height,0xDD05030A);
        g.fill(x,y,x+W,y+H,RiftTheme.BACKGROUND);
        g.outline(x,y,W,H,RiftTheme.PURPLE);
        RiftSidebar.render(g,minecraft.font,x,y,page);
        RiftTheme.text(g,minecraft.font,RiftSidebar.CATEGORIES[page],x+175,y+22,RiftTheme.WHITE);
        RiftTheme.text(g,minecraft.font,"Manage your RIFT modules",x+175,y+42,RiftTheme.GRAY);
        RiftTheme.panel(g,x+175,y+65,515,27,false);
        RiftTheme.text(g,minecraft.font,"Search modules — coming soon",x+188,y+75,RiftTheme.GRAY);

        List<Module> modules=visibleModules();
        if(modules.isEmpty()) RiftTheme.text(g,minecraft.font,"No modules in this category yet",x+190,y+135,RiftTheme.GRAY);
        for(int i=0;i<modules.size();i++){
            Module m=modules.get(i);
            int cx=x+175+(i%2)*260, cy=y+110+(i/2)*72;
            boolean hover=mouseX>=cx&&mouseX<=cx+235&&mouseY>=cy&&mouseY<=cy+65;
            RiftModuleCard.render(g,minecraft.font,cx,cy,m.getName(),m.getDescription(),m.isEnabled(),hover);
            if(m.isPinned()) RiftTheme.text(g,minecraft.font,"★",cx+215,cy+5,RiftTheme.PURPLE);
        }
        if(settingsModule!=null){
            RiftTheme.panel(g,x+175,y+310,515,75,true);
            RiftTheme.text(g,minecraft.font,"MODULE",x+190,y+325,RiftTheme.PURPLE);
            RiftTheme.text(g,minecraft.font,settingsModule.getName(),x+190,y+345,RiftTheme.WHITE);
            RiftTheme.text(g,minecraft.font,"Right-click its card to pin or unpin it",x+190,y+363,RiftTheme.GRAY);
        }
        RiftTheme.text(g,minecraft.font,"RIFT CLIENT  |  1.0.0",x+175,y+400,RiftTheme.GRAY);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mx=event.x(), my=event.y();
        int x=(width-W)/2, y=(height-H)/2;
        for(int i=0;i<RiftSidebar.CATEGORIES.length;i++){
            int iy=y+72+i*43;
            if(mx>=x+9&&mx<=x+145&&my>=iy-8&&my<=iy+20){page=i;settingsModule=null;return true;}
        }
        List<Module> modules=visibleModules();
        for(int i=0;i<modules.size();i++){
            int cx=x+175+(i%2)*260, cy=y+110+(i/2)*72;
            if(mx>=cx&&mx<=cx+235&&my>=cy&&my<=cy+65){
                Module m=modules.get(i);
                if(event.button()==1){m.setPinned(!m.isPinned());settingsModule=m;return true;}
                if(event.button()==0){
                    if(mx>=cx+180) settingsModule=m;
                    else {m.toggle();settingsModule=null;}
                    return true;
                }
            }
        }
        return super.mouseClicked(event,doubleClick);
    }
    @Override public boolean isPauseScreen(){return false;}
}
